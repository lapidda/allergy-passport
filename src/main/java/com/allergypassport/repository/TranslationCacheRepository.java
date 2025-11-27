package com.allergypassport.repository;

import com.allergypassport.entity.TranslationCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TranslationCacheRepository extends JpaRepository<TranslationCache, Long> {

    /**
     * Find a cached translation by source text hash and language pair.
     */
    Optional<TranslationCache> findBySourceTextHashAndSourceLangAndTargetLang(
            String sourceTextHash, String sourceLang, String targetLang);

    /**
     * Delete old translations that haven't been accessed in a while.
     * This helps manage database size and keep only frequently used translations.
     */
    @Modifying
    @Query("DELETE FROM TranslationCache t WHERE t.lastAccessed < :cutoffDate")
    int deleteOldTranslations(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count total translations in cache (for monitoring).
     */
    @Query("SELECT COUNT(t) FROM TranslationCache t")
    long countTotalTranslations();
}
