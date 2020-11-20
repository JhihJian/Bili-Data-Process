package org.jhihjian.bili;

import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubtitleStore {
    private final Logger logger= LoggerFactory.getLogger(this.getClass().getName());
    private final String INSERT_SQL="INSERT INTO `bili_total_text` (`av`, `total_text`) VALUES ('%s','%s')";
    private final String QUERY_SQL="SELECT * FROM bili_total_text WHERE av='%s'";
    private final String QUERY_ALL_SQL="SELECT * FROM bili_total_text";
    private final MySQL mysql;
    public SubtitleStore(){
        Conf conf=new Conf();

        String url= conf.getProperty("url");
        String user=conf.getProperty("username");
        String pw=conf.getProperty("password");
        // Create instance
        mysql = new MySQL(url, user, pw);
    }
    public boolean storeText(Long av,String text){
        try {
            int resultCode = mysql.update(String.format(INSERT_SQL,av,text));
            return resultCode==1;
        } catch (SQLException e) {
            logger.error("",e);
            return false;
        }
    }
    public String queryTextByAv(Long av){
        String query=String.format( QUERY_SQL,av);
        try (ResultSet results = mysql.query(query)) {
            if(results.next()) {
                // Do something with the ResultSet
                return results.getString("total_text");
            }
            // Then close statement (the ResultSet will close itself)
            results.getStatement().close();
        } catch (SQLException e) {
            logger.error("",e);
        }
        return "";
    }
    public List<Long> queryAllAv(){
        List<Long> result=new ArrayList<>();
        try (ResultSet results = mysql.query(QUERY_ALL_SQL)) {
            if(results.next()) {
                // Do something with the ResultSet
                result.add(results.getLong("av"));
            }
            // Then close statement (the ResultSet will close itself)
            results.getStatement().close();
        } catch (SQLException e) {
            logger.error("",e);
        }
        return result;
    }

}
