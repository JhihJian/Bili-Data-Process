package org.jhihjian.bili.ocr;

public class TextResult {
  private double confidence;
  private String text;
  private int[][] text_region;
  private int status;

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int[][] getText_region() {
    return text_region;
  }

  public void setText_region(int[][] text_region) {
    this.text_region = text_region;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
