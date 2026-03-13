package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.user.User;
import com.skill.kairo.domain.repository.UserRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataUserRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(User user) {
        UserEntity entity = new UserEntity(
                user.getId(),
                user.getCompanyId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getCreatedAt()
        );
        springDataRepository.save(entity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    private User toDomain(UserEntity entity) {
        User user = new User(
                entity.getId(),
                entity.getCompanyId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getName()
        );
        return user;
    }
}
