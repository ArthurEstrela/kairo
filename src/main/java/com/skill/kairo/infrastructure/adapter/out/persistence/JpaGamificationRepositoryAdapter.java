package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataGamificationRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.mapper.GamificationMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaGamificationRepositoryAdapter implements GamificationRepository {

    private final SpringDataGamificationRepository springDataRepository;
    private final GamificationMapper mapper;

    public JpaGamificationRepositoryAdapter(SpringDataGamificationRepository springDataRepository, GamificationMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<GamificationProfile> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId)
                .map(mapper::toDomain); // Se encontrar, traduz para o Domínio
    }

    @Override
    public void save(GamificationProfile profile) {
        var entity = mapper.toEntity(profile); // Traduz do Domínio para JPA
        springDataRepository.save(entity);     // O Spring guarda na base de dados
    }
}