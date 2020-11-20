package org.jhihjian.bili;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
        String content=Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        content=content.replace("\"","");
        List<String> result= Arrays.stream(content.split("\n")).filter(s->!Strings.isNullOrEmpty(s)).filter(s->!s.equals("\r")&&!s.equals("\n")).collect(Collectors.toList());
        //result:{"42960,钱包都快跪下来求我当人了"}
        Map<Long,String> timeTextMap=new TreeMap<>();
        String preText="";
        for(String line:result){
            Long timestamp;
            String text;
            try {
                int index=line.indexOf(',');
                if(index<0){
                    logger.error("not found splitter,line:{}",line);
                    continue;
                }
                 timestamp=Long.parseLong( line.substring(0,index));
                 text=line.substring(index+1);
            }
            catch (Exception e){
                logger.error("timestamp parse error,line:{}",line);
                continue;
            }
            if( Strings.isNullOrEmpty(text)||"\r".equals(text)){
                continue;
            }
            //去重
            if(!preText.equals(text)){
                //"www.thelancet.com Vol391 June 2"
                timeTextMap.put(timestamp, text);
            }
            preText=text;
        }
        System.out.println(Joiner.on(",").join( timeTextMap.values()));
        return Joiner.on(",").join( timeTextMap.values());
    }
}
