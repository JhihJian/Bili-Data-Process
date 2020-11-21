package org.jhihjian.bili.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class HashSimilarityTest {

    @Test
    public void getSimilarity() {
        ISimilarity iSimilarity=new HashSimilarity();
        System.out.println( iSimilarity.getSimilarity("第一是海量的信息","第一是海量的信息"));
        System.out.println( iSimilarity.getSimilarity("本期视频可能会改变你的认知","期视频可能会改变你的认知"));
        System.out.println( iSimilarity.getSimilarity("第,一是海量的信息","第一是海量的信息"));
        System.out.println( iSimilarity.getSimilarity("第一是海量的信息","第,一是海量的信息"));
        System.out.println( iSimilarity.getSimilarity("网上蹈跃出了大量的优秀作品","网上踊跃出了天量的优秀作品"));
        Assert.assertTrue(0.1> iSimilarity.getSimilarity("","网上踊跃出了天量的优秀作品"));
    System.out.println(
        iSimilarity.getSimilarity(
            "五、支持中华传统武术技艺和文化传承不得以“拜师收徒\"不得利用虚假宣传、炒作等手段骗取钱财，不得借武术之名从事违背社会公序良俗及违法国武协还发出了行业自律倡议书以上倡议，望武术人共守之",
            "五、支持中华传统武术技艺和文化传承，不得以“拜师收徒\"“贺寿庆典\"等为名剑财，不得利用虚假宣传、炒作等手段骗取钱财，不得借武术之名从事违背社会公序良俗及违法违规活动。11月6日以上倡议，望武术人共守之。"));
    }
}