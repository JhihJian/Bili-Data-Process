package org.jhihjian.bili.util;

/**
 * 计算相似度
 *
 */
public interface ISimilarity {
    /**
     * 计算相似度
     *
     * @param word1 词语1
     * @param word2 词语2
     * @return 相似度值
     */
    double getSimilarity(String word1, String word2);
}