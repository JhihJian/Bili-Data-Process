package org.jhihjian.bili;

import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.EscapeSql;
import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SubtitleStore {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final String INSERT_SQL = "INSERT INTO `bili_total_text` (`av`, `total_text`) VALUES (\"%s\",\"%s\")";
    private final String INSERT_BILI_TEXT_SQL =
            "INSERT INTO `bili_text` (`av`,`play_time`, `text`) VALUES (\"%s\",\"%s\",\"%s\")";
    private final String QUERY_SQL = "SELECT * FROM bili_total_text WHERE av='%s'";
    private final String QUERY_BILI_TEXT_SQL = "SELECT * FROM bili_text WHERE av='%s'";
    private final String QUERY_ALL_SQL = "SELECT * FROM bili_total_text";
    private final MySQL mysql;

    public SubtitleStore() {
        Conf conf = new Conf();

        String url = conf.getProperty("url");
        String user = conf.getProperty("username");
        String pw = conf.getProperty("password");
        // Create instance
        mysql = new MySQL(url, user, pw);
    }

    public boolean storeText(Long av, String text) {
        try {
            int resultCode = mysql.update(String.format(INSERT_SQL, av, EscapeSql.escape(text)));
            return resultCode == 1;
        } catch (SQLException e) {
            logger.error("", e);
            return false;
        }
    }

    public Map<Long, String> queryTimeTextByAv(Long av) {
        String query = String.format(QUERY_BILI_TEXT_SQL, av);
        Map<Long, String> timeTextMap = new TreeMap<>();
        try (ResultSet results = mysql.query(query)) {
            while (results.next()) {
                // Do something with the ResultSet
                String text = results.getString("text");
                Long timestamp = results.getLong("play_time");
                timeTextMap.put(timestamp, text);
            }
            // Then close statement (the ResultSet will close itself)
            results.getStatement().close();
        } catch (SQLException e) {
            logger.error("", e);
        }
        return timeTextMap;
    }

    public String queryTextByAv(Long av) {
        String query = String.format(QUERY_SQL, av);
        try (ResultSet results = mysql.query(query)) {
            if (results.next()) {
                // Do something with the ResultSet
                return results.getString("total_text");
            }
            // Then close statement (the ResultSet will close itself)
            results.getStatement().close();
        } catch (SQLException e) {
            logger.error("", e);
        }
        return "";
    }

    public boolean storeText(Long av, Map<Long, String> timeText) {
        Connection conn = null;
        try {
            conn = mysql.getConnection();
            String update = "INSERT INTO `bili_text` (`av`,`play_time`, `text`) VALUES (?,?,?)";
            PreparedStatement statement = conn.prepareStatement(update);
            for (Map.Entry<Long, String> entry : timeText.entrySet()) {
                statement.setLong(1, av);
                statement.setLong(2, entry.getKey());
                statement.setString(3, EscapeSql.escape(entry.getValue()));
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


    public List<Long> queryAllAv() {
        List<Long> result = new ArrayList<>();
        try (ResultSet results = mysql.query(QUERY_ALL_SQL)) {
            if (results.next()) {
                // Do something with the ResultSet
                result.add(results.getLong("av"));
            }
            // Then close statement (the ResultSet will close itself)
            results.getStatement().close();
        } catch (SQLException e) {
            logger.error("", e);
        }
        return result;
    }

}
