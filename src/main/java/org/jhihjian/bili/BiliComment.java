package org.jhihjian.bili;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jhihjian.bili.util.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BiliComment {
    private final Logger logger= LoggerFactory.getLogger(this.getClass().getName());
    //test url "https://httpbin.org/post";
    private final static String COMMENT_URL="https://api.bilibili.com/x/v2/reply/add";
    private final static String COOKIES;
    static {
        Conf conf=new Conf();
        COOKIES=conf.getProperty("cookies");
    }
    public void commentAv(String message,String av) throws Exception {
        URI uri=URI.create(COMMENT_URL);
        Map<String,String> params= Maps.newHashMap();
        params.put("oid",av);
        params.put("type","1");
        params.put("plat","1");
        params.put("message",message);
        params.put("ordering","heat");
        params.put("jsonp","jsonp");
        params.put("csrf",getCsrfFromCookies(COOKIES));
        String[] headers={
                "content-type","application/x-www-form-urlencoded; " +
                "charset=UTF-8",
                "referer","https://www.bilibili.com",
                "user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
                "origin","https://www.bilibili.com",
                "cookie",COOKIES
        };
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .version(HttpClient.Version.HTTP_2)
//                .cookieHandler(new CookieManager(initCookies(uri,cookies).getCookieStore(), CookiePolicy.ACCEPT_ALL))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .headers(headers)
                .version(HttpClient.Version.HTTP_2)
                .POST(HttpRequest.BodyPublishers.ofString(getParamsString(params)))
                .build();

        HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("comment av:{} message:{} result:{}",av,message,response.body());
    }
    public static String getCsrfFromCookies(String cookies){
        cookies=cookies.replaceAll(" ","");
        for(String line: cookies.split(";")){
            String[] keyValue=line.split("=");
            if("bili_jct".equals( keyValue[0])){
                return keyValue[1];
            }
        }
        return "";
    }
    public static void main(String[] args) throws Exception {
        String av="287761565";
        String message="再发一个评论";
        BiliComment biliComment=new BiliComment();
        biliComment.commentAv(av,message);

    }
    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
