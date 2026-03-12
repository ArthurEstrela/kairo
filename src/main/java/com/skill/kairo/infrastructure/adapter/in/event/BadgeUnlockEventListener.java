package com.skill.kairo.infrastructure.adapter.in.event;

import com.skill.kairo.domain.event.XpAwardedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BadgeUnlockEventListener {

    // O Spring Boot deteta esta anotação e liga este método ao barramento de eventos
    @EventListener
    public void onXpAwarded(XpAwardedEvent event) {
        System.out.println("\n🎉 [EVENT LISTENER] O utilizador " + event.userId() + 
                " acabou de ganhar " + event.amountAdded() + " XP!");
        
        // Exemplo de lógica desacoplada:
        // Se a pessoa passou de 1000 XP, podemos dar-lhe uma conquista (Badge) automaticamente.
        if (event.newTotalXp() >= 1000) {
            System.out.println("🏆 [EVENT LISTENER] O utilizador ultrapassou os 1000 XP! Desbloquear Badge de Veterano!");
            // No futuro: badgeUnlockUseCase.execute(event.userId(), "VETERANO_BADGE");
        }
    }
}