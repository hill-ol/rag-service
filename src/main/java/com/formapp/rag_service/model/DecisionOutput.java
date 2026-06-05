package com.formapp.rag_service.model;

import java.util.List;
import java.util.Map;

public class DecisionOutput {
  private double recoveryScore;
  private String recoveryLevel;
  private String suggestedIntensity;
  private boolean shouldDeload;
  private List<String> readyToProgress;
  private Map<String, Double> suggestedWeights;
  private List<String> weakMuscleGroups;
  private String currentPhase;
  private int daysSinceLastSession;
  private String searchQuery;

  public static Builder builder() { return new Builder(); }

  public double getRecoveryScore() { return recoveryScore; }
  public String getRecoveryLevel() { return recoveryLevel; }
  public String getSuggestedIntensity() { return suggestedIntensity; }
  public boolean isShouldDeload() { return shouldDeload; }
  public List<String> getReadyToProgress() { return readyToProgress; }
  public Map<String, Double> getSuggestedWeights() { return suggestedWeights; }
  public List<String> getWeakMuscleGroups() { return weakMuscleGroups; }
  public String getCurrentPhase() { return currentPhase; }
  public int getDaysSinceLastSession() { return daysSinceLastSession; }
  public String getSearchQuery() { return searchQuery; }

  public static class Builder {
    private final DecisionOutput o = new DecisionOutput();
    public Builder recoveryScore(double v) { o.recoveryScore = v; return this; }
    public Builder recoveryLevel(String v) { o.recoveryLevel = v; return this; }
    public Builder suggestedIntensity(String v) { o.suggestedIntensity = v; return this; }
    public Builder shouldDeload(boolean v) { o.shouldDeload = v; return this; }
    public Builder readyToProgress(List<String> v) { o.readyToProgress = v; return this; }
    public Builder suggestedWeights(Map<String, Double> v) { o.suggestedWeights = v; return this; }
    public Builder weakMuscleGroups(List<String> v) { o.weakMuscleGroups = v; return this; }
    public Builder currentPhase(String v) { o.currentPhase = v; return this; }
    public Builder daysSinceLastSession(int v) { o.daysSinceLastSession = v; return this; }
    public Builder searchQuery(String v) { o.searchQuery = v; return this; }
    public DecisionOutput build() { return o; }
  }
}