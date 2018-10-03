package com.zyl.medical;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.util.StringUtils;

public class ChemicalsTest {

    public static void main(String[] args) {
        testJsoup("甲醛");
    }

    private static String  testJsoup(String productName){

        String resultStr = "";
        try {
            resultStr = Jsoup.connect("http://www.hgmsds.com/showChemicalDetails")
                    .ignoreContentType(true).ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3554.0 Safari/537.36")
                    .referrer("http://www.hgmsds.com/hg-ehs-index?decrypt=JDb2r9L2JJ3A3u4PN5Byyg%3D%3D")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.8")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Host", "www.hgmsds.com")
                    .header("Origin", "http://www.hgmsds.com")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("inputValue", productName)
                    .method(Connection.Method.POST)
                    .followRedirects(true)
                    .timeout(5000)
                    .execute().body();

        }catch (Exception e){
            e.printStackTrace();
        }
        return resultStr;
    }
}
