package com.zyl.medical.com.zyl.utils;

public class Constants {



    public final static String USER_NAME = "17704055853";
    public final static String PASS_WORD = "123456";

    /**
     *  化学品数据，查询的目录相关
     * */
    //危险性分类
    public final static String RISK_CATEGORY = "ghs";
    //危险品目录
    public final static String RISK_MENU = "dan";
    // 职业接触限值
    public final static String OEL = "gbz";
    //安全防护指南
    public final static String SAFETY_GUIDE = "guide";
    //中国监管目录
    public final static String CHINA_REGULATORY_DIRECTORY = "ccs";
    //现有物质目录
    public final static String LIST_OF_EXISTING_SUBSTANCES = "ics";


    /**
     *  化学品法规
     *  中国法规，国外法规，国际规章
     * */
    public final static String[] CHEMICALS_LAWS_URLS = {"http://www.hgmsds.com/getChinaLawList.action"
    ,"http://www.hgmsds.com/getForeignLawList.action", "http://www.hgmsds.com/getInternationalLawList.action"};

}
