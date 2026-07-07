package com.santiago.base.modules.auth.refresh.repository;

import com.santiago.base.modules.auth.refresh.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByFamilyAndRevokedAtIsNull(UUID family);

    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE rt.family = :family AND rt.revokedAt IS NULL")
    int revokeFamily(UUID family);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    int revokeAllForUser(Long userId);
}