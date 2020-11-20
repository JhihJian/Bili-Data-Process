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
        Assert.assertEquals( "钱包都快跪下来求我当人了,民族荣誉招你煮你了,这是一个有底线的企业,但凡是工业品,每一个环节都存在风险",subtitleReader.getTotalText(path));
    }
}