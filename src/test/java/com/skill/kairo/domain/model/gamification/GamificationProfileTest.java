package com.skill.kairo.domain.model.gamification;

import com.skill.kairo.domain.event.ChallengeCompletedEvent;
import com.skill.kairo.domain.event.DomainEvent;
import com.skill.kairo.domain.event.LeaguePromotedEvent;
import com.skill.kairo.domain.event.LifeLostEvent;
import com.skill.kairo.domain.event.XpAwardedEvent;
import com.skill.kairo.domain.exception.OutOfLivesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class GamificationProfileTest {

    private UUID userId;
    private GamificationProfile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profile = new GamificationProfile(UUID.randomUUID(), userId);
    }

    // ─── XP ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("awardXp")
    class AwardXpTests {

        @Test
        @DisplayName("incrementa XP pelo valor correto")
        void shouldIncreaseXp() {
            profile.awardXp(100);
            assertThat(profile.getCurrentXp()).isEqualTo(100);
        }

        @Test
        @DisplayName("XP acumula em múltiplas chamadas")
        void shouldAccumulateXp() {
            profile.awardXp(200);
            profile.awardXp(300);
            assertThat(profile.getCurrentXp()).isEqualTo(500);
        }

        @Test
        @DisplayName("publica XpAwardedEvent com os valores corretos")
        void shouldPublishXpAwardedEvent() {
            profile.awardXp(150);

            List<DomainEvent> events = profile.pullDomainEvents();
            assertThat(events).hasAtLeastOneElementOfType(XpAwardedEvent.class);

            XpAwardedEvent event = events.stream()
                    .filter(e -> e instanceof XpAwardedEvent)
                    .map(e -> (XpAwardedEvent) e)
                    .findFirst().orElseThrow();

            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.amountAdded()).isEqualTo(150);
            assertThat(event.newTotalXp()).isEqualTo(150);
        }

        @Test
        @DisplayName("pullDomainEvents limpa a lista de eventos")
        void pullEventsClearsQueue() {
            profile.awardXp(10);
            profile.pullDomainEvents(); // consome os eventos
            assertThat(profile.pullDomainEvents()).isEmpty();
        }
    }

    // ─── LIGAS ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("league promotion")
    class LeaguePromotionTests {

        @Test
        @DisplayName("começa em BRONZE")
        void startsInBronze() {
            assertThat(profile.getTier()).isEqualTo(LeagueTier.BRONZE);
        }

        @Test
        @DisplayName("promove para SILVER ao atingir 500 XP")
        void promotesToSilverAt500Xp() {
            profile.awardXp(500);
            assertThat(profile.getTier()).isEqualTo(LeagueTier.SILVER);
        }

        @Test
        @DisplayName("promove para GOLD ao atingir 1500 XP")
        void promotesToGoldAt1500Xp() {
            profile.awardXp(1500);
            assertThat(profile.getTier()).isEqualTo(LeagueTier.GOLD);
        }

        @Test
        @DisplayName("promove para PLATINUM ao atingir 3500 XP")
        void promotesToPlatinumAt3500Xp() {
            profile.awardXp(3500);
            assertThat(profile.getTier()).isEqualTo(LeagueTier.PLATINUM);
        }

        @Test
        @DisplayName("promove para DIAMOND ao atingir 7500 XP")
        void promotesToDiamondAt7500Xp() {
            profile.awardXp(7500);
            assertThat(profile.getTier()).isEqualTo(LeagueTier.DIAMOND);
        }

        @Test
        @DisplayName("publica LeaguePromotedEvent ao subir de liga")
        void publishesLeaguePromotedEvent() {
            profile.awardXp(500);

            List<DomainEvent> events = profile.pullDomainEvents();
            assertThat(events).hasAtLeastOneElementOfType(LeaguePromotedEvent.class);

            LeaguePromotedEvent event = events.stream()
                    .filter(e -> e instanceof LeaguePromotedEvent)
                    .map(e -> (LeaguePromotedEvent) e)
                    .findFirst().orElseThrow();

            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.newTier()).isEqualTo(LeagueTier.SILVER);
        }

        @Test
        @DisplayName("não publica LeaguePromotedEvent quando XP não cruza limiar")
        void noEventBelowThreshold() {
            profile.awardXp(499);

            boolean hasLeagueEvent = profile.pullDomainEvents().stream()
                    .anyMatch(e -> e instanceof LeaguePromotedEvent);
            assertThat(hasLeagueEvent).isFalse();
        }

        @Test
        @DisplayName("promoção atravessa múltiplos limiares de uma só vez")
        void promotionSkipsMultipleTiers() {
            profile.awardXp(7500);
            // Deve ir de BRONZE direto para DIAMOND com apenas um evento (o tier final)
            assertThat(profile.getTier()).isEqualTo(LeagueTier.DIAMOND);
        }
    }

    // ─── VIDAS ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("failChallenge (perder vida)")
    class FailChallengeTests {

        @Test
        @DisplayName("decrementa uma vida ao falhar")
        void shouldDecrementLife() {
            profile.failChallenge();
            assertThat(profile.getCurrentLives()).isEqualTo(4);
        }

        @Test
        @DisplayName("reset do streak ao falhar")
        void shouldResetStreak() {
            profile.maintainStreak();
            profile.maintainStreak();
            profile.failChallenge();
            assertThat(profile.getCurrentStreak()).isEqualTo(0);
        }

        @Test
        @DisplayName("publica LifeLostEvent ao falhar")
        void shouldPublishLifeLostEvent() {
            profile.failChallenge();

            List<DomainEvent> events = profile.pullDomainEvents();
            assertThat(events).hasAtLeastOneElementOfType(LifeLostEvent.class);

            LifeLostEvent event = events.stream()
                    .filter(e -> e instanceof LifeLostEvent)
                    .map(e -> (LifeLostEvent) e)
                    .findFirst().orElseThrow();

            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.livesRemaining()).isEqualTo(4);
        }

        @Test
        @DisplayName("lança OutOfLivesException ao falhar sem vidas")
        void shouldThrowWhenNoLivesLeft() {
            // Esgotar as 5 vidas
            for (int i = 0; i < 5; i++) {
                profile.failChallenge();
                profile.pullDomainEvents(); // limpar eventos entre iterações
            }
            assertThatThrownBy(() -> profile.failChallenge())
                    .isInstanceOf(OutOfLivesException.class);
        }
    }

    // ─── STREAK ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("streak")
    class StreakTests {

        @Test
        @DisplayName("começa em zero")
        void startsAtZero() {
            assertThat(profile.getCurrentStreak()).isEqualTo(0);
        }

        @Test
        @DisplayName("incrementa a cada sucesso")
        void incrementsOnSuccess() {
            profile.maintainStreak();
            profile.maintainStreak();
            profile.maintainStreak();
            assertThat(profile.getCurrentStreak()).isEqualTo(3);
        }
    }

    // ─── RESTAURO DE VIDAS ────────────────────────────────────────────────────

    @Nested
    @DisplayName("restoreOneLife / restoreLives")
    class RestoreLifeTests {

        @Test
        @DisplayName("restoreOneLife devolve true e adiciona uma vida")
        void restoreOneLifeWhenBelowMax() {
            profile.failChallenge();
            profile.pullDomainEvents();

            boolean restored = profile.restoreOneLife();

            assertThat(restored).isTrue();
            assertThat(profile.getCurrentLives()).isEqualTo(5);
        }

        @Test
        @DisplayName("restoreOneLife devolve false quando já está no máximo")
        void restoreOneLifeWhenFull() {
            boolean restored = profile.restoreOneLife();
            assertThat(restored).isFalse();
            assertThat(profile.getCurrentLives()).isEqualTo(5);
        }

        @Test
        @DisplayName("restoreLives repõe todas as vidas imediatamente")
        void restoreLivesRestoresFull() {
            for (int i = 0; i < 3; i++) {
                profile.failChallenge();
                profile.pullDomainEvents();
            }
            profile.restoreLives();
            assertThat(profile.getCurrentLives()).isEqualTo(5);
        }
    }

    // ─── QUOTA DE TRILHAS ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("track generation quota")
    class TrackQuotaTests {

        @Test
        @DisplayName("novo perfil começa com 3 gerações disponíveis")
        void newProfileHas3Generations() {
            assertThat(profile.getAvailableTrackGenerations()).isEqualTo(3);
        }

        @Test
        @DisplayName("hasTrackQuota retorna true quando há gerações")
        void hasQuotaWhenPositive() {
            assertThat(profile.hasTrackQuota()).isTrue();
        }

        @Test
        @DisplayName("consumeTrackGeneration decrementa em 1")
        void consumeDecrementsBy1() {
            profile.consumeTrackGeneration();
            assertThat(profile.getAvailableTrackGenerations()).isEqualTo(2);
        }

        @Test
        @DisplayName("consumeTrackGeneration lança TrackGenerationLimitException quando esgotado")
        void consumeThrowsWhenExhausted() {
            profile.consumeTrackGeneration();
            profile.consumeTrackGeneration();
            profile.consumeTrackGeneration();
            assertThatThrownBy(profile::consumeTrackGeneration)
                .isInstanceOf(com.skill.kairo.domain.exception.TrackGenerationLimitException.class);
        }

        @Test
        @DisplayName("enableUnlimitedGenerations define null (Premium ilimitado)")
        void enableUnlimitedSetsNull() {
            profile.enableUnlimitedGenerations();
            assertThat(profile.getAvailableTrackGenerations()).isNull();
        }

        @Test
        @DisplayName("hasTrackQuota retorna true quando null (Premium)")
        void hasQuotaWhenNull() {
            profile.enableUnlimitedGenerations();
            assertThat(profile.hasTrackQuota()).isTrue();
        }

        @Test
        @DisplayName("consumeTrackGeneration não decrementa quando null (Premium)")
        void consumeDoesNotDecrementWhenNull() {
            profile.enableUnlimitedGenerations();
            profile.consumeTrackGeneration(); // should not throw
            assertThat(profile.getAvailableTrackGenerations()).isNull();
        }

        @Test
        @DisplayName("resetQuota repõe gerações e define próxima data de reset")
        void resetQuotaRestoresCount() {
            profile.consumeTrackGeneration();
            profile.consumeTrackGeneration();
            java.time.Instant nextReset = java.time.Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS);
            profile.resetQuota(3, nextReset);
            assertThat(profile.getAvailableTrackGenerations()).isEqualTo(3);
            assertThat(profile.getQuotaResetDate()).isEqualTo(nextReset);
        }
    }
}
