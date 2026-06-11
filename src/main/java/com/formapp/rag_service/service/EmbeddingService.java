package com.formapp.rag_service.service;

import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

  // Placeholder â€” returns empty array until real embedding model is added
  // Real implementation will call OpenAI text-embedding-3-small
  public float[] embed(String text) {
    return new float[0];
  }

  public String getRawQuery(String text) {
    return text;
  }
}