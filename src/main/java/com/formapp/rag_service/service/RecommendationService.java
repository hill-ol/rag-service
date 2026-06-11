package com.formapp.rag_service.service;

import com.formapp.rag_service.model.*;
import com.formapp.rag_service.repository.KnowledgeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

  private final DecisionEngineService decisionEngine;
  private final KnowledgeRepository knowledgeRepository;

  @Value("${spring.ai.anthropic.api-key}")
  private String apiKey;

  public RecommendationService(
      DecisionEngineService decisionEngine,
      KnowledgeRepository knowledgeRepository
  ) {
    this.decisionEngine = decisionEngine;
    this.knowledgeRepository = knowledgeRepository;
  }

  public Recommendation recommend(UserContext ctx) {
    // Step 1: Run decision engine â€” pure logic
    DecisionOutput decision = decisionEngine.compute(ctx);

    // Step 2: Retrieve relevant research using keyword search
    String searchQuery = decision.getSearchQuery();
    List<KnowledgeEntry> knowledge = knowledgeRepository.findByKeywords(searchQuery, 6);

    // Step 3: Build prompts
    String systemPrompt = buildSystemPrompt(knowledge);
    String userPrompt = buildUserPrompt(ctx, decision);

    // Step 4: Call Anthropic API directly
    String response = callClaude(systemPrompt, userPrompt);

    // Step 5: Package everything into the Recommendation object
    return buildRecommendation(decision, knowledge, response);
  }

  private String callClaude(String systemPrompt, String userPrompt) {
    RestTemplate rest = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("x-api-key", apiKey);
    headers.set("anthropic-version", "2023-06-01");
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = new HashMap<>();
    body.put("model", "claude-haiku-4-5-20251001");
    body.put("max_tokens", 1000);
    body.put("system", systemPrompt);
    body.put("messages", List.of(Map.of("role", "user", "content", userPrompt)));

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = rest.exchange(
        "https://api.anthropic.com/v1/messages",
        HttpMethod.POST,
        entity,
        Map.class
    );

    List<Map> content = (List<Map>) response.getBody().get("content");
    return (String) content.get(0).get("text");
  }

  private String buildSystemPrompt(List<KnowledgeEntry> knowledge) {
    String knowledgeContext = knowledge.isEmpty()
        ? "No specific research retrieved. Use general exercise science principles."
        : knowledge.stream()
        .map(k -> String.format("[%s - %s, %d]\n%s",
            k.getSource(), k.getCategory(), k.getYear(), k.getContent()))
        .collect(Collectors.joining("\n\n"));

    return """
            You are a precise, research-backed fitness coach for a personal fitness app called FORM.
            You are coaching Olivia, a college student who trains 4-5 days per week.

            RULES:
            - Use ONLY the research context below to justify recommendations
            - Always cite the source when referencing research
            - Be specific: name exact exercises, exact weights, exact rep ranges
            - Keep your summary under 3 sentences
            - Never use em-dashes
            - Sound like a knowledgeable friend, not a bot
            - If the user should deload, say so clearly and explain why

            RESEARCH CONTEXT:
            """ + knowledgeContext;
  }

  private String buildUserPrompt(UserContext ctx, DecisionOutput decision) {
    StringBuilder prompt = new StringBuilder();

    prompt.append(String.format(
        "Generate a workout recommendation for Olivia's %s session today.\n\n",
        ctx.getTodayDayType()
    ));

    prompt.append(String.format(
        "RECOVERY: %.0f%% (%s) | Sleep: %.1fh | Mood: %d/5 | Days rested: %d\n",
        decision.getRecoveryScore() * 100,
        decision.getRecoveryLevel(),
        ctx.getSleepHours(),
        ctx.getMoodScore(),
        decision.getDaysSinceLastSession()
    ));

    prompt.append(String.format(
        "INTENSITY: %s | Phase: %s | Deload needed: %s\n\n",
        decision.getSuggestedIntensity(),
        decision.getCurrentPhase(),
        decision.isShouldDeload()
    ));

    if (!decision.getReadyToProgress().isEmpty()) {
      prompt.append("READY TO INCREASE WEIGHT:\n");
      for (String exId : decision.getReadyToProgress()) {
        Double suggested = decision.getSuggestedWeights().get(exId);
        if (suggested != null) {
          prompt.append(String.format(
              "- %s: try %.1f lbs\n", exId, suggested
          ));
        }
      }
      prompt.append("\n");
    }

    if (ctx.getExerciseHistory() != null && !ctx.getExerciseHistory().isEmpty()) {
      prompt.append("RECENT EXERCISE HISTORY:\n");
      for (UserContext.ExerciseHistory ex : ctx.getExerciseHistory()) {
        if (ex.getRecentSets() != null && !ex.getRecentSets().isEmpty()) {
          UserContext.SetRecord last = ex.getRecentSets().get(0);
          prompt.append(String.format(
              "- %s: last session %.1f lbs x %d reps (%s)\n",
              ex.getExerciseName(),
              last.getWeightLbs(),
              last.getReps(),
              last.isCompletedAllReps() ? "completed" : "missed reps"
          ));
        }
      }
    }

    if (!decision.getWeakMuscleGroups().isEmpty()) {
      prompt.append(String.format(
          "\nNEGLECTED MUSCLES: %s\n",
          String.join(", ", decision.getWeakMuscleGroups())
      ));
    }

    prompt.append("""
            \nProvide:
            1. A 2-3 sentence summary of today's recommendation
            2. Specific weight and rep suggestions for each exercise
            3. One recovery insight backed by the research
            """);

    return prompt.toString();
  }

  private Recommendation buildRecommendation(
      DecisionOutput decision,
      List<KnowledgeEntry> knowledge,
      String llmResponse
  ) {
    List<String> sources = knowledge.stream()
        .map(k -> k.getSource() + " (" + k.getYear() + ")")
        .distinct()
        .collect(Collectors.toList());

    List<Recommendation.ExerciseRecommendation> exercises =
        decision.getSuggestedWeights().entrySet().stream()
            .map(e -> Recommendation.ExerciseRecommendation.builder()
                .exerciseId(e.getKey())
                .exerciseName(formatExerciseName(e.getKey()))
                .suggestedWeightLbs(e.getValue())
                .suggestedSets(3)
                .repRange("8-12")
                .isProgression(true)
                .rationale("Hit target reps for 2+ consecutive sessions")
                .build())
            .collect(Collectors.toList());

    return Recommendation.builder()
        .summary(llmResponse)
        .exercises(exercises)
        .sources(sources)
        .recoveryScore(decision.getRecoveryScore())
        .intensity(decision.getSuggestedIntensity())
        .build();
  }

  private String formatExerciseName(String id) {
    String[] words = id.replace("-", " ").split(" ");
    StringBuilder result = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        result.append(Character.toUpperCase(word.charAt(0)))
            .append(word.substring(1))
            .append(" ");
      }
    }
    return result.toString().trim();
  }
}