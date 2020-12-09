package org.jhihjian.bili.process;

import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class MqContext {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private MySQL mySQL;

  public MySQL getMySQL() {
    return mySQL;
  }

  public void setMySQL(MySQL mySQL) {
    this.mySQL = mySQL;
  }

  public void destroy() {
    try {
      if (mySQL != null) {
        mySQL.closeConnection();
      }
    } catch (SQLException throwables) {
      logger.error("", throwables);
    }
  }
}
