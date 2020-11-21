package org.jhihjian.bili;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jhihjian.bili.util.HashSimilarity;
import org.jhihjian.bili.util.ISimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SubtitleReader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final double SIMILAR_VALUE = 0.81;

    public String getTotalText(String filePath) throws IOException {
        logger.info("extract text from {}", filePath);
        FileReader fileReader = new FileReader(filePath);
        Map<Long, String> timeTextMap = new TreeMap<>();
        for (CSVRecord line : CSVFormat.DEFAULT.parse(fileReader)) {
            Long timestamp;
            String text;
            try {
                timestamp = Long.parseLong(line.get(0));
                text = line.get(1);
            } catch (Exception e) {
                logger.error("timestamp parse error,line:{}", line);
                continue;
            }
            if (!Strings.isNullOrEmpty(text)) {
                timeTextMap.put(timestamp, text);
            }
        }
        List<String> result = Lists.newArrayList(timeTextMap.values());
        Iterator<String> iterator = result.iterator();
        String preText = "";
        ISimilarity similarity = new HashSimilarity();
        while (iterator.hasNext()) {
            String text = iterator.next();
            if (similarity.getSimilarity(preText, text) > SIMILAR_VALUE) {
                iterator.remove();
            }
            preText = text;
        }
        String totalText = Joiner.on(",").join(result);
        totalText = totalText.replaceAll(",,", "~~~");
        totalText = totalText.replaceAll(",", "");
        totalText = totalText.replaceAll("~~~", ",");
        return totalText;
    }

    public Map<Long, String> getTimeText(String filePath) throws IOException {
        logger.info("extract time text from {}", filePath);
        FileReader fileReader = new FileReader(filePath);
        Map<Long, String> timeTextMap = new TreeMap<>();
        for (CSVRecord line : CSVFormat.DEFAULT.parse(fileReader)) {
            Long timestamp;
            String text;
            try {
                timestamp = Long.parseLong(line.get(0));
                text = line.get(1);
                text = text.replaceAll(",", "");
            } catch (Exception e) {
                logger.error("timestamp parse error,line:{}", line);
                continue;
            }
            if (!Strings.isNullOrEmpty(text)) {
                timeTextMap.put(timestamp, text);
            }
        }
        List<Map.Entry<Long, String>> result = Lists.newArrayList(timeTextMap.entrySet());
        Iterator<Map.Entry<Long, String>> iterator = result.iterator();
        String preText = "";
        ISimilarity similarity = new HashSimilarity();
        Set<Long> needToRemoveKey = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<Long, String> keyValue = iterator.next();
            if (similarity.getSimilarity(preText, keyValue.getValue()) > SIMILAR_VALUE) {
//                timeTextMap.remove(keyValue.getKey());
                needToRemoveKey.add(keyValue.getKey());
            }
            preText = keyValue.getValue();
        }
        for (Long key : needToRemoveKey) {
            timeTextMap.remove(key);
        }
        return timeTextMap;
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("mm:ss");

    //timestamp to 1:23 形式
    public static String castTimestamp(Long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
}
