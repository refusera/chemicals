package com.zyl.medical;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public class AutoLogin {

    public static void main(String[] args) {

        String url = "http://www.hgmsds.com/hgLoginCheck";
        String userName = "17704055853";
        String passWord = "123456";
        try {
           Map<String, String> cookieMap = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .referrer("http://www.hgmsds.com/")
                    .data("username", userName)
                    .data("password", passWord).method(Connection.Method.POST).execute().cookies();

           Map<String, String> map = Jsoup.connect("http://www.hgmsds.com/").ignoreContentType(true).ignoreHttpErrors(true).cookies(cookieMap).execute().cookies();
            System.out.println(map);
            System.out.println(cookieMap);


            System.out.println();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
