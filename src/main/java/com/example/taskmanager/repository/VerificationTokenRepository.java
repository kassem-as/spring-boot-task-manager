package com.example.taskmanager.repository;

import com.example.taskmanager.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now AND t.used = false")
    void deleteExpiredTokens(LocalDateTime now);
}
