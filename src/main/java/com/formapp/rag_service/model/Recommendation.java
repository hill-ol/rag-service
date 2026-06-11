ackage com.formapp.rag_service.model;

import java.util.List;

public class Recommendation {
  private String summary;
  private List<ExerciseRecommendation> exercises;
  private String recoveryInsight;
  private String weeklyPlanSuggestion;
  private List<String> sources;
  private double recoveryScore;
  private String intensity;

  public static Builder builder() { return new Builder(); }

  public String getSummary() { return summary; }
  public List<ExerciseRecommendation> getExercises() { return exercises; }
  public String getRecoveryInsight() { return recoveryInsight; }
  public String getWeeklyPlanSuggestion() { return weeklyPlanSuggestion; }
  public List<String> getSources() { return sources; }
  public double getRecoveryScore() { return recoveryScore; }
  public String getIntensity() { return intensity; }

  public static class Builder {
    private final Recommendation r = new Recommendation();
    public Builder summary(String v) { r.summary = v; return this; }
    public Builder exercises(List<ExerciseRecommendation> v) { r.exercises = v; return this; }
    public Builder recoveryInsight(String v) { r.recoveryInsight = v; return this; }
    public Builder weeklyPlanSuggestion(String v) { r.weeklyPlanSuggestion = v; return this; }
    public Builder sources(List<String> v) { r.sources = v; return this; }
    public Builder recoveryScore(double v) { r.recoveryScore = v; return this; }
    public Builder intensity(String v) { r.intensity = v; return this; }
    public Recommendation build() { return r; }
  }

  public static class ExerciseRecommendation {
    private String exerciseName;
    private String exerciseId;
    private double suggestedWeightLbs;
    private int suggestedSets;
    private String repRange;
    private String rationale;
    private boolean isProgression;

    public static Builder builder() { return new Builder(); }

    public String getExerciseName() { return exerciseName; }
    public String getExerciseId() { return exerciseId; }
    public double getSuggestedWeightLbs() { return suggestedWeightLbs; }
    public int getSuggestedSets() { return suggestedSets; }
    public String getRepRange() { return repRange; }
    public String getRationale() { return rationale; }
    public boolean isProgression() { return isProgression; }

    public static class Builder {
      private final ExerciseRecommendation e = new ExerciseRecommendation();
      public Builder exerciseName(String v) { e.exerciseName = v; return this; }
      public Builder exerciseId(String v) { e.exerciseId = v; return this; }
      public Builder suggestedWeightLbs(double v) { e.suggestedWeightLbs = v; return this; }
      public Builder suggestedSets(int v) { e.suggestedSets = v; return this; }
      public Builder repRange(String v) { e.repRange = v; return this; }
      public Builder rationale(String v) { e.rationale = v; return this; }
      public Builder isProgression(boolean v) { e.isProgression = v; return this; }
      public ExerciseRecommendation build() { return e; }
    }
  }
}