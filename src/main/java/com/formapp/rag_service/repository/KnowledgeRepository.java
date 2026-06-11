package com.formapp.rag_service.repository;

import com.formapp.rag_service.model.KnowledgeEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class KnowledgeRepository {

  private final JdbcTemplate jdbcTemplate;

  public KnowledgeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // Temporary keyword search until embedding model is added
  // Searches content, category, and subcategory for any of the query words
  public List<KnowledgeEntry> findSimilar(
      float[] embedding,
      double threshold,
      int limit
  ) {
    return new ArrayList<>();
  }

  // Active method for now â€” keyword-based search
  public List<KnowledgeEntry> findByKeywords(String query, int limit) {
    if (query == null || query.isBlank()) return new ArrayList<>();

    // Split query into individual words and search for each
    String[] words = query.toLowerCase().split("\\s+");

    // Build a WHERE clause that checks for any keyword match
    StringBuilder whereClause = new StringBuilder();
    List<Object> params = new ArrayList<>();

    for (int i = 0; i < words.length; i++) {
      if (i > 0) whereClause.append(" OR ");
      whereClause.append(
          "LOWER(content) LIKE ? OR LOWER(category) LIKE ? OR LOWER(subcategory) LIKE ?"
      );
      String pattern = "%" + words[i] + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }

    String sql = String.format("""
            SELECT id::text, category, subcategory, content, source, year
            FROM exercise_knowledge
            WHERE %s
            LIMIT ?
            """, whereClause);

    params.add(limit);

    try {
      return jdbcTemplate.query(
          sql,
          params.toArray(),
          (rs, rowNum) -> {
            KnowledgeEntry entry = new KnowledgeEntry();
            entry.setId(rs.getString("id"));
            entry.setCategory(rs.getString("category"));
            entry.setSubcategory(rs.getString("subcategory"));
            entry.setContent(rs.getString("content"));
            entry.setSource(rs.getString("source"));
            entry.setYear(rs.getInt("year"));
            entry.setSimilarity(1.0);
            return entry;
          }
      );
    } catch (Exception e) {
      // If table is empty or doesn't exist yet, return empty list
      return new ArrayList<>();
    }
  }
}