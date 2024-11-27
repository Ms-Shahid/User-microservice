package com.userservice.repository;

import com.userservice.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepo extends JpaRepository<Token, Long> {

    @Query("SELECT t FROM Token t WHERE t.value = :value AND t.deleted = :deleted AND t.expiryAt > :expiryAt")
    Optional<Token> findByValueAndDeletedExpiryAtGreaterThan(
                                                             @Param("value") String value,
                                                             @Param("deleted") boolean deleted,
                                                             @Param("expiryAt") long expiryAt
    );
}
