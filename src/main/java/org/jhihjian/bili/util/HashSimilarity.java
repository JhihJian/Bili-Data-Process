package org.jhihjian.bili.util;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public class HashSimilarity implements ISimilarity{
    @Override
    public double getSimilarity(String word1, String word2) {
        Set<Character> set1=new HashSet<>();
        Set<Character> set2=new HashSet<>();
        for(char s: word1.toCharArray()){
            set1.add(s);
        }
        for(char s: word2.toCharArray()){
            set2.add(s);
        }
        Set<Character> set = new HashSet<>();
        set.addAll(set1);
        set.retainAll(set2);
        float size=Math.max(set1.size(),set2.size());
        return set.size()/size;
    }
}
