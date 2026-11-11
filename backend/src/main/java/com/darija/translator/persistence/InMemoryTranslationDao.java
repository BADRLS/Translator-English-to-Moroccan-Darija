package com.darija.translator.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory DAO implementation for logging translation history and user requests.
 * Satisfies the 3-tier Persistence Layer contract without requiring an external RDBMS setup.
 */
public class InMemoryTranslationDao implements TranslationDao {

    private static final InMemoryTranslationDao INSTANCE = new InMemoryTranslationDao();
    private final List<TranslationRecord> storage = new CopyOnWriteArrayList<>();

    private InMemoryTranslationDao() {
        // Seed initial sample record for verification
        storage.add(new TranslationRecord(
                "system",
                "English",
                "Hello, how are you doing today?",
                "السلام، كيف داير اليوم؟",
                "Salam, kif dayer lyoum?",
                "Sa-lam, keef da-yer l-youm?",
                "gemini-1.5-flash"
        ));
    }

    public static InMemoryTranslationDao getInstance() {
        return INSTANCE;
    }

    @Override
    public TranslationRecord save(TranslationRecord record) {
        storage.add(0, record); // Newest first
        return record;
    }

    @Override
    public Optional<TranslationRecord> findById(String id) {
        return storage.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<TranslationRecord> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage));
    }

    @Override
    public List<TranslationRecord> findByUsername(String username) {
        return storage.stream()
                .filter(r -> r.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        storage.clear();
    }
}

