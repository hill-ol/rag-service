ackage com.formapp.rag_service.model;

import java.util.List;

public class UserContext {
  private double sleepHours;
  private int sleepQuality;
  private int moodScore;
  private int currentStreak;
  private int weeklyCompleted;
  private int weeklyGoal;
  private String todayDayType;
  private List<RecentSession> recentSessions;
  private List<ExerciseHistory> exerciseHistory;

  public double getSleepHours() { return sleepHours; }
  public void setSleepHours(double v) { this.sleepHours = v; }
  public int getSleepQuality() { return sleepQuality; }
  public void setSleepQuality(int v) { this.sleepQuality = v; }
  public int getMoodScore() { return moodScore; }
  public void setMoodScore(int v) { this.moodScore = v; }
  public int getCurrentStreak() { return currentStreak; }
  public void setCurrentStreak(int v) { this.currentStreak = v; }
  public int getWeeklyCompleted() { return weeklyCompleted; }
  public void setWeeklyCompleted(int v) { this.weeklyCompleted = v; }
  public int getWeeklyGoal() { return weeklyGoal; }
  public void setWeeklyGoal(int v) { this.weeklyGoal = v; }
  public String getTodayDayType() { return todayDayType; }
  public void setTodayDayType(String v) { this.todayDayType = v; }
  public List<RecentSession> getRecentSessions() { return recentSessions; }
  public void setRecentSessions(List<RecentSession> v) { this.recentSessions = v; }
  public List<ExerciseHistory> getExerciseHistory() { return exerciseHistory; }
  public void setExerciseHistory(List<ExerciseHistory> v) { this.exerciseHistory = v; }

  public static class RecentSession {
    private String date;
    private String dayType;
    private String name;
    private int durationMinutes;
    private int mood;

    public String getDate() { return date; }
    public void setDate(String v) { this.date = v; }
    public String getDayType() { return dayType; }
    public void setDayType(String v) { this.dayType = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int v) { this.durationMinutes = v; }
    public int getMood() { return mood; }
    public void setMood(int v) { this.mood = v; }
  }

  public static class ExerciseHistory {
    private String exerciseId;
    private String exerciseName;
    private String muscleGroup;
    private List<SetRecord> recentSets;
    private int consecutiveSessionsAtWeight;
    private double currentWeightLbs;

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String v) { this.exerciseId = v; }
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String v) { this.exerciseName = v; }
    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String v) { this.muscleGroup = v; }
    public List<SetRecord> getRecentSets() { return recentSets; }
    public void setRecentSets(List<SetRecord> v) { this.recentSets = v; }
    public int getConsecutiveSessionsAtWeight() { return consecutiveSessionsAtWeight; }
    public void setConsecutiveSessionsAtWeight(int v) { this.consecutiveSessionsAtWeight = v; }
    public double getCurrentWeightLbs() { return currentWeightLbs; }
    public void setCurrentWeightLbs(double v) { this.currentWeightLbs = v; }
  }

  public static class SetRecord {
    private String date;
    private int reps;
    private double weightLbs;
    private boolean completedAllReps;

    public String getDate() { return date; }
    public void setDate(String v) { this.date = v; }
    public int getReps() { return reps; }
    public void setReps(int v) { this.reps = v; }
    public double getWeightLbs() { return weightLbs; }
    public void setWeightLbs(double v) { this.weightLbs = v; }
    public boolean isCompletedAllReps() { return completedAllReps; }
    public void setCompletedAllReps(boolean v) { this.completedAllReps = v; }
  }
}