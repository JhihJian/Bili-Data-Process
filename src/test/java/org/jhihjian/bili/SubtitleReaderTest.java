package org.jhihjian.bili;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class SubtitleReaderTest {
    String path = "src/test/resources/SubtitleFiles/797860601.txt";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getTotalText() throws IOException {
        SubtitleReader subtitleReader = new SubtitleReader();
        subtitleReader.getTotalText(path);
    }

    @Test
    public void getTimeText() throws IOException {
        SubtitleReader subtitleReader = new SubtitleReader();
        Map<Long, String> result = subtitleReader.getTimeText(path);
        System.out.println();
    }

    @Test
    public void castTimestamp() {
        Assert.assertEquals("00:05", SubtitleReader.castTimestamp(5000l));
    }
}