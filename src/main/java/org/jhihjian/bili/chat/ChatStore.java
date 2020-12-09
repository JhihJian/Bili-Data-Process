package org.jhihjian.bili.chat;

import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.EscapeSql;
import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

public class ChatStore {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private MySQL mysql;

  public ChatStore(MySQL mysql) {

    this.mysql = mysql;
  }

  public boolean store(Long av, Chat chat) {
    Connection conn = null;
    try {
      conn = mysql.getConnection();
      String update =
          "INSERT INTO `bili_chat` (`av`, `play_time`,`text`,`send_time`,`user_name`) VALUES (?,?,?,?,?)";
      PreparedStatement statement = conn.prepareStatement(update);
      for (int i = 0; i < chat.getCount(); i++) {
        statement.setLong(1, av);
        long playTime = (long) (chat.getTime().get(i) * 1000);
        statement.setLong(2, playTime);
        statement.setString(3, EscapeSql.escape(chat.getChats().get(i)));
        statement.setTimestamp(4, new Timestamp(chat.getDate().get(i)));
        statement.setString(5, EscapeSql.escape(chat.getUsers().get(i)));
        statement.addBatch();
      }
      statement.executeBatch();
      statement.close();
    } catch (SQLException e) {
      logger.error("", e);
      return false;
    } finally {
      if (Objects.nonNull(conn)) {
        try {
          conn.close();
        } catch (SQLException throwables) {
          logger.error("", throwables);
        }
      }
    }
    return true;
  }
}
