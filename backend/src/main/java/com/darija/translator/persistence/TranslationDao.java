package com.darija.translator.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for the Persistence Tier.
 * Decouples business logic from the underlying storage mechanism.
 */
public interface TranslationDao {
    TranslationRecord save(TranslationRecord record);

    Optional<TranslationRecord> findById(String id);

    List<TranslationRecord> findAll();

    List<TranslationRecord> findByUsername(String username);

    void clear();
}

