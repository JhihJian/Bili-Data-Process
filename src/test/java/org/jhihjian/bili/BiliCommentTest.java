package org.jhihjian.bili;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class BiliCommentTest {
    String testCookies="_uuid=C11111-75F2-34B2-AF65-11111111infoc; buvid3=123123-37FF-433E-A76C-111111111infoc; rpdid=|(J|kYYuYu)~0J'ulmJklmuYu; sid=111111; LIVE_BUVID=AUT1111111; blackside_state=1; CURRENT_FNVAL=80; _ga=GA1.1.11111.1111; DedeUserID=ddddddd; DedeUserID__ckMd5=bbbbb; SESSDATA=kkkkkk; bili_jct=199999999999991; bp_t_offset_50111776=1111111; CURRENT_QUALITY=11; bp_video_offset_50839776=111111; PVID=3";
    @Test
    public void getCsrfFromCookies() {
    Assert.assertEquals("199999999999991",BiliComment.getCsrfFromCookies(testCookies));
    }
}