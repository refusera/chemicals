package com.zyl.medical.com.zyl.utils;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DownWebIdUtils {

    /**
     *  通过产品名称查询他们平台的产品ID，因为后续操作都是基于他们平台的ID进行查询的
     * @param type  1=化学品数据，2=危险货物分类，3=食品接触材料原辅料查询
     * */
    public static String  findWebInfo(String productName, int type){

        String resultStr = "";
        String url = "";
        try {
            switch (type){
                case 1 : url = "http://www.hgmsds.com/showChemicalDetails";break;
                case 2 : url = "http://www.hgmsds.com/showChemicalWxDetails";break;
                case 3 : url = "http://www.hgmsds.com/showChemicalFcmDetails";break;
                default:break;
            }
            resultStr = Jsoup.connect(url)
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
                    .timeout(10*1000)
                    .execute().body();

        }catch (Exception e){
            e.printStackTrace();
        }
        return resultStr;
    }

    /**
     *  化学品数据 - 化工词典
     * */
    public static Document chemicalsBase(String webId){
        String url = "http://www.hgmsds.com/hg-ehs-index";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                    .referrer("http://www.hgmsds.com/hg-ehs-index?decrypt=pxdVpuS4%2FzackeRlyimHHA%3D%3D")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .data("decrypt", webId)
                    .timeout(10*1000)
                    .get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return doc;
    }

    /**
     *  化学品数据 - 相关
     * @param type 1=危险性分类，2=危险品目录，3=职业接触限值，4=安全防护指南，5=中国监管目录，6=现有物质目录
     * */
    public static String otherInfo(JSONObject jsonObject, String ecNo, int type){
        String url = "http://www.hgmsds.com/getEhsDetails";
        String jsonContent = null;
        try {
            jsonContent = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .referrer("http://www.hgmsds.com/hg-ehs-index?decrypt=pxdVpuS4%2FzackeRlyimHHA%3D%3D")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .data(dataMap(jsonObject, ecNo, type))
                    .method(Connection.Method.POST).execute().body();
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonContent;
    }

    /**
     *  危险货物分类
     * */
    public static Document riskCargoCategory(String hgId){
        String url = "http://www.hgmsds.com/hg-ehs-wx";
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    //.ignoreContentType(true).ignoreHttpErrors(true)
                    .referrer("http://www.hgmsds.com/hg-ehs-wx")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .cookies(cookieMap())
                    .data("decrypt", hgId)
                    .timeout(10*1000)
                    .get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return doc;
    }


    private static Map<String, String> dataMap(JSONObject jsonObject, String ecNo, int type){
        Map<String, String> dataMap = new HashMap<>();
        switch (type){
            case 1 : dataMap.put("type", Constants.RISK_CATEGORY);break;
            case 2 : dataMap.put("type", Constants.RISK_MENU);break;
            case 3 : dataMap.put("type", Constants.OEL);break;
            case 4 : dataMap.put("type", Constants.SAFETY_GUIDE);break;
            case 5 : dataMap.put("type", Constants.CHINA_REGULATORY_DIRECTORY);break;
            case 6 : dataMap.put("type", Constants.LIST_OF_EXISTING_SUBSTANCES);break;
            default:break;
        }
        dataMap.put("casno", jsonObject.getString("casNo"));
        dataMap.put("ecno", ecNo==null?"":ecNo);
        dataMap.put("nameCh", jsonObject.getString("nameCh"));
        dataMap.put("nameEn", jsonObject.getString("nameEn"));
        return dataMap;
    }

    /**
     *  获取自动登录的Cookie
     * */
    public static Map<String, String> cookieMap(){
        Map<String, String> cookieMap = new HashMap<>();
        try {
            cookieMap = Jsoup.connect("http://www.hgmsds.com/hgLoginCheck")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .referrer("http://www.hgmsds.com/")
                    .data("username", Constants.USER_NAME)
                    .data("password", Constants.PASS_WORD)
                    .method(Connection.Method.POST)
                    .execute()
                    .cookies();
        }catch (Exception e){
            e.printStackTrace();
        }
        return cookieMap;
    }


    public static Document downQueryByUn(int nuNo){
        String url = "http://www.hgmsds.com/hg-ehs-transportation?decrypt="+EcryptAESUtils.encode(nuNo+"")+"&unno="+nuNo;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).ignoreHttpErrors(true).ignoreContentType(true).get();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return doc;
    }

    public static Document findRawMaterialDoc(String webId){

        String url = "http://www.hgmsds.com/hg-ehs-fcm";
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    //.ignoreContentType(true).ignoreHttpErrors(true)
                    .referrer("http://www.hgmsds.com/hg-ehs-wx")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .cookies(cookieMap())
                    .data("decrypt", webId)
                    .timeout(10*1000)
                    .get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return document;
    }
}
