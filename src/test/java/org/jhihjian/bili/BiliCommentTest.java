package org.jhihjian.bili;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class BiliCommentTest {
    String testCookies = "_uuid=C11111-75F2-34B2-AF65-11111111infoc; buvid3=123123-37FF-433E-A76C-111111111infoc; rpdid=|(J|kYYuYu)~0J'ulmJklmuYu; sid=111111; LIVE_BUVID=AUT1111111; blackside_state=1; CURRENT_FNVAL=80; _ga=GA1.1.11111.1111; DedeUserID=ddddddd; DedeUserID__ckMd5=bbbbb; SESSDATA=kkkkkk; bili_jct=199999999999991; bp_t_offset_50111776=1111111; CURRENT_QUALITY=11; bp_video_offset_50839776=111111; PVID=3";

    @Test
    public void getCsrfFromCookies() {
        Assert.assertEquals("199999999999991", BiliComment.getCsrfFromCookies(testCookies));
    }

    @Test
    public void splitMessage() {
        StringBuilder str = new StringBuilder();
        int length = 1000;
        for (int i = 0; i < length; i++) {
            str.append("9");
        }
        Assert.assertEquals(1, BiliComment.splitMessage(str.toString()).size());
        Assert.assertEquals(str.toString(), BiliComment.splitMessage(str.toString()).get(0));
        str = new StringBuilder();
        length = 900;
        for (int i = 0; i < length; i++) {
            str.append("9");
        }
        str.append(",");
        for (int i = 0; i < length; i++) {
            str.append("8");
        }
        Assert.assertEquals(2, BiliComment.splitMessage(str.toString()).size());
        StringBuilder result1 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result1.append("9");
        }
        //此处逗号是消失的
        StringBuilder result2 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result2.append("8");
        }

        Assert.assertEquals(result1.toString(), BiliComment.splitMessage(str.toString()).get(0));
        Assert.assertEquals(result2.toString(), BiliComment.splitMessage(str.toString()).get(1));
    }
}