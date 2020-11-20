package org.jhihjian.bili.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final  class Conf {
    private Properties prop;
    public Conf(){
        String filePath = "conf/config.properties";
        prop= new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
        } catch (FileNotFoundException ex) {
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
        }
    }

    public  String getProperty(String key){
        return prop.getProperty(key);
    }
}
