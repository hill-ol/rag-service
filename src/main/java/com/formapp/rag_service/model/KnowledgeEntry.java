ackage com.formapp.rag_service.model;

public class KnowledgeEntry {
  private String id;
  private String category;
  private String subcategory;
  private String content;
  private String source;
  private Integer year;
  private double similarity;

  public String getId() { return id; }
  public void setId(String v) { this.id = v; }
  public String getCategory() { return category; }
  public void setCategory(String v) { this.category = v; }
  public String getSubcategory() { return subcategory; }
  public void setSubcategory(String v) { this.subcategory = v; }
  public String getContent() { return content; }
  public void setContent(String v) { this.content = v; }
  public String getSource() { return source; }
  public void setSource(String v) { this.source = v; }
  public Integer getYear() { return year; }
  public void setYear(Integer v) { this.year = v; }
  public double getSimilarity() { return similarity; }
  public void setSimilarity(double v) { this.similarity = v; }
}