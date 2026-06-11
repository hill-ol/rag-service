package com.formapp.rag_service.service;

import com.formapp.rag_service.model.DecisionOutput;
import com.formapp.rag_service.model.UserContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

//the brain of the system - reads the user context and computes what the recommendation needs to
// know before Claude is involved - takes raw data to structured decisions
@Service
public class DecisionEngineService {

  // How much each factor contributes to your recovery score
  private static final double SLEEP_HOURS_WEIGHT = 0.40;
  private static final double SLEEP_QUALITY_WEIGHT = 0.20;
  private static final double REST_DAYS_WEIGHT = 0.25;
  private static final double MOOD_WEIGHT = 0.15;

  // If recovery drops below this, suggest a deload
  private static final double DELOAD_RECOVERY_THRESHOLD = 0.45;

  // If streak is this long with no deload, suggest one
  private static final int DELOAD_STREAK_THRESHOLD = 20;

  public DecisionOutput compute(UserContext ctx) {
    double recoveryScore = computeRecoveryScore(ctx);
    String recoveryLevel = classifyRecovery(recoveryScore);
    String intensity = suggestIntensity(recoveryScore);
    boolean shouldDeload = shouldDeload(recoveryScore, ctx);
    List<String> readyToProgress = computeProgressionReadiness(ctx);
    Map<String, Double> suggestedWeights = computeSuggestedWeights(ctx, readyToProgress);
    List<String> weakMuscles = identifyWeakPoints(ctx);
    String phase = determinePhase(ctx, shouldDeload);
    String searchQuery = buildSearchQuery(ctx, readyToProgress, phase);
    int daysSinceLastSession = daysSinceLastSession(ctx);

    return DecisionOutput.builder()
        .recoveryScore(recoveryScore)
        .recoveryLevel(recoveryLevel)
        .suggestedIntensity(intensity)
        .shouldDeload(shouldDeload)
        .readyToProgress(readyToProgress)
        .suggestedWeights(suggestedWeights)
        .weakMuscleGroups(weakMuscles)
        .currentPhase(phase)
        .daysSinceLastSession(daysSinceLastSession)
        .searchQuery(searchQuery)
        .build();
  }

  // Weighted average of sleep, quality, rest days, and mood
  // Each factor is normalized to 0.0-1.0 before weighting
  private double computeRecoveryScore(UserContext ctx) {
    double sleepScore = Math.min(ctx.getSleepHours() / 8.0, 1.0);
    double qualityScore = (ctx.getSleepQuality() - 1) / 4.0;
    double restScore = Math.min(daysSinceLastSession(ctx) / 2.0, 1.0);
    double moodScore = (ctx.getMoodScore() - 1) / 4.0;

    return (sleepScore * SLEEP_HOURS_WEIGHT)
        + (qualityScore * SLEEP_QUALITY_WEIGHT)
        + (restScore * REST_DAYS_WEIGHT)
        + (moodScore * MOOD_WEIGHT);
  }

  private String classifyRecovery(double score) {
    if (score < 0.45) return "low";
    if (score < 0.70) return "moderate";
    return "high";
  }

  private String suggestIntensity(double score) {
    if (score < 0.45) return "light";
    if (score < 0.60) return "moderate";
    if (score < 0.80) return "hard";
    return "pr-attempt";
  }

  private boolean shouldDeload(double recoveryScore, UserContext ctx) {
    if (recoveryScore < DELOAD_RECOVERY_THRESHOLD) return true;
    if (ctx.getCurrentStreak() >= DELOAD_STREAK_THRESHOLD) return true;
    return false;
  }

  // Double progression: if you hit target reps for 2+ consecutive sessions
  // at the same weight, you are ready to increase
  private List<String> computeProgressionReadiness(UserContext ctx) {
    List<String> ready = new ArrayList<>();
    if (ctx.getExerciseHistory() == null) return ready;

    for (UserContext.ExerciseHistory ex : ctx.getExerciseHistory()) {
      if (ex.getConsecutiveSessionsAtWeight() >= 2) {
        boolean allRepsHit = ex.getRecentSets() != null
            && ex.getRecentSets().stream()
            .limit(2)
            .allMatch(UserContext.SetRecord::isCompletedAllReps);
        if (allRepsHit) ready.add(ex.getExerciseId());
      }
    }
    return ready;
  }

  // Upper body: increase by 2.5 lbs (smaller muscles, finer control)
  // Lower body: increase by 5 lbs (larger muscles, more capacity)
  private Map<String, Double> computeSuggestedWeights(
      UserContext ctx,
      List<String> readyToProgress
  ) {
    Map<String, Double> suggestions = new HashMap<>();
    if (ctx.getExerciseHistory() == null) return suggestions;

    for (UserContext.ExerciseHistory ex : ctx.getExerciseHistory()) {
      if (readyToProgress.contains(ex.getExerciseId())) {
        double increment = isUpperBody(ex.getMuscleGroup()) ? 2.5 : 5.0;
        suggestions.put(ex.getExerciseId(),
            ex.getCurrentWeightLbs() + increment);
      }
    }
    return suggestions;
  }

  private boolean isUpperBody(String muscleGroup) {
    if (muscleGroup == null) return true;
    Set<String> lowerBody = Set.of("quads", "hamstrings", "glutes", "calves");
    return !lowerBody.contains(muscleGroup.toLowerCase());
  }

  // Find muscle groups that haven't appeared in recent sessions
  private List<String> identifyWeakPoints(UserContext ctx) {
    List<String> weak = new ArrayList<>();
    if (ctx.getRecentSessions() == null) return weak;

    Map<String, Integer> muscleCount = new HashMap<>();
    for (UserContext.RecentSession s : ctx.getRecentSessions()) {
      switch (s.getDayType()) {
        case "push" -> {
          muscleCount.merge("chest", 1, Integer::sum);
          muscleCount.merge("shoulders", 1, Integer::sum);
          muscleCount.merge("triceps", 1, Integer::sum);
        }
        case "pull" -> {
          muscleCount.merge("back", 1, Integer::sum);
          muscleCount.merge("biceps", 1, Integer::sum);
        }
        case "legs" -> {
          muscleCount.merge("quads", 1, Integer::sum);
          muscleCount.merge("hamstrings", 1, Integer::sum);
          muscleCount.merge("glutes", 1, Integer::sum);
        }
      }
    }

    List<String> allMuscles = List.of(
        "chest", "shoulders", "triceps",
        "back", "biceps",
        "quads", "hamstrings", "glutes", "core"
    );

    for (String muscle : allMuscles) {
      if (muscleCount.getOrDefault(muscle, 0) == 0) {
        weak.add(muscle);
      }
    }
    return weak;
  }

  // Determines what training phase you're in based on context
  private String determinePhase(UserContext ctx, boolean shouldDeload) {
    if (shouldDeload) return "deload";
    String dayType = ctx.getTodayDayType();
    if ("cardio".equals(dayType) || "yoga".equals(dayType)) return "recovery";
    if (ctx.getCurrentStreak() < 8) return "strength";
    return "hypertrophy";
  }

  private int daysSinceLastSession(UserContext ctx) {
    if (ctx.getRecentSessions() == null || ctx.getRecentSessions().isEmpty()) return 3;
    try {
      LocalDate last = LocalDate.parse(ctx.getRecentSessions().get(0).getDate());
      return (int) ChronoUnit.DAYS.between(last, LocalDate.now());
    } catch (Exception e) {
      return 1;
    }
  }

  // Builds a natural language search query for the RAG system
  // This query gets embedded and used to find relevant research
  private String buildSearchQuery(
      UserContext ctx,
      List<String> readyToProgress,
      String phase
  ) {
    StringBuilder query = new StringBuilder();
    query.append(phase).append(" training ");
    query.append(ctx.getTodayDayType()).append(" day ");
    if (!readyToProgress.isEmpty()) {
      query.append("progressive overload weight increase ");
    }
    if (ctx.getSleepHours() < 6) {
      query.append("sleep deprivation recovery performance ");
    }
    if ("deload".equals(phase)) {
      query.append("deload protocol volume reduction ");
    }
    return query.toString().trim();
  }
}