package org.jhihjian.bili;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SubtitleReader {
    private final Logger logger= LoggerFactory.getLogger(this.getClass().getName());
    public String getTotalText(String filePath) throws IOException {
        logger.info("extract text from {}",filePath);
        FileReader fileReader=new FileReader(filePath);
        Map<Long,String> timeTextMap=new TreeMap<>();
        String preText="";
        for (CSVRecord line : CSVFormat.DEFAULT.parse(fileReader)) {
            Long timestamp;
            String text;
            try {
                timestamp = Long.parseLong(line.get(0));
                text=line.get(1);
            }catch (Exception e){
                logger.error("timestamp parse error,line:{}",line);
                continue;
            }
            if(!Strings.isNullOrEmpty(text)&&!preText.equals(text)){
                timeTextMap.put(timestamp, text);
            }
            preText=text;
        }
        return Joiner.on(",").join( timeTextMap.values());
    }
}
