package org.jhihjian.bili.ocr;

public class OcrResponse {
  private String msg;
  private TextResult[][] results;

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public TextResult[][] getResults() {
    return results;
  }

  public void setResults(TextResult[][] results) {
    this.results = results;
  }
}
