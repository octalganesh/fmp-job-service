package com.octal.fsm.repositories;

import com.octal.fsm.entities.QuickBooksToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuickBooksTokenRepository extends JpaRepository<QuickBooksToken, Long> {
    Optional<QuickBooksToken> findByRealmId(String realmId);
}
