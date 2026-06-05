package com.formapp.rag_service.controller;

import com.formapp.rag_service.model.Recommendation;
import com.formapp.rag_service.model.UserContext;
import com.formapp.rag_service.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class RecommendationController {

  private final RecommendationService recommendationService;

  public RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  // Main endpoint — receives UserContext JSON, returns Recommendation JSON
  @PostMapping("/recommend")
  public ResponseEntity<Recommendation> recommend(
      @RequestBody UserContext context
  ) {
    Recommendation recommendation = recommendationService.recommend(context);
    return ResponseEntity.ok(recommendation);
  }

  // Health check — lets us verify the service is running
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("FORM RAG Service is running");
  }
}