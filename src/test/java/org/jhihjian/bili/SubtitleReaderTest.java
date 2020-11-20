package org.jhihjian.bili;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SubtitleReaderTest {
    String path="src/test/resources/SubtitleFiles/457721499.txt";
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getTotalText() throws IOException {
        SubtitleReader subtitleReader=new SubtitleReader();
        subtitleReader.getTotalText(path);
    }

}