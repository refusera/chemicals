package com.zyl.medical.com.zyl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zyl.medical.com.zyl.utils.DownWebIdUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     *  化学品数据的产品入库
     * */
    public String beforeSigle(String chemicals){

        logger.info("HomeService.beforeSigle，检索的产品为：{}", chemicals);
        //带入产品名称进行爬虫（可指定自己的库中产品名称进行检索）
        String jsonContent = DownWebIdUtils.findWebInfo(chemicals, 1);
        try {
            if (!StringUtils.isEmpty(jsonContent)){
                JSONArray jsonArray = JSONArray.parseArray(jsonContent);
                for (int i=0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    try {
                        StringBuffer sql = new StringBuffer("INSERT INTO base_info (web_id, hg_id, cas_no, name_en, name_ch) VALUES (\"");
                        sql.append(java.net.URLDecoder.decode(jsonObject.getString("id"), "UTF-8")+"\","+jsonObject.getInteger("hgId")+",\""+jsonObject.getString("casNo")+"\",\"");
                        sql.append(jsonObject.getString("nameEn") + "\",\"" + jsonObject.getString("nameCh") + "\")");
                        jdbcTemplate.execute(sql.toString());
                        logger.info("HomeService.beforeSigle，入库成功：{}", sql.toString());
                    }catch (Exception e){
                        logger.warn("HomeService.beforeSigle，库中已存在：{}", jsonObject.getString("nameCh"));
                    }
                }
            }
        }catch (Exception e){
            logger.error("HomeService.beforeSigle，处理失败：{}", e);
        }
        return jsonContent;
    }

    /**
     *  测试 先用 1 条数据进行测试
     * */
    public String chemicalsData(){
        Map<String, Object> resultMap = new HashMap<>();
        //查询生产的产品库
        String querySql = "SELECT id,web_id webId, hg_id hgId, cas_no casNo, name_en nameEn, name_ch nameCh FROM base_info WHERE is_processed=0 LIMIT 1;";
        try {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql);
            logger.info("HomeService.productInfo，查询所有未经过处理的产品条数：{}", mapList.size());
            if (!ObjectUtils.isEmpty(mapList)){
                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(mapList));
                for (int i=0; i<jsonArray.size(); i++){

                    /**
                     *  分为七个目录进行展示：
                     *      第一：化工词典  基本信息和物性信息
                     * */
                    Map<String, String> baseMap = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String webId = jsonObject.getString("webId");
                    Document document = DownWebIdUtils.chemicalsBase(webId);
                    Elements listTable = document.getElementsByClass("ehstab");
                    for (Element table : listTable){
                        Elements listTr = table.getElementsByTag("tr");
                        for (Element tr : listTr){
                            if (tr.children().size() == 2){
                                baseMap.put(tr.child(0).text().trim(), tr.child(1).text().trim());
                            }
                        }
                    }
                    resultMap.put("baseInfo", baseMap);
                    /**
                     *  第二部：其他六个目录
                     *  包括 1=危险性分类，2=危险品目录，3=职业接触限值，4=安全防护指南，5=中国监管目录，6=现有物质目录
                     * */
                    for (int j=1; j<=6; j++){
                        String otherInfo = DownWebIdUtils.otherInfo(jsonObject, baseMap.get("EC 号："), j);
                        resultMap.put("data"+j, otherInfo);
                    }

                    //把获取到的数据进行入库，并修改状态
                    String insertSql = "INSERT INTO chemicals_data (base_id, json) VALUES (?,?);";
                    jdbcTemplate.update(insertSql, jsonObject.getInteger("id"), JSON.toJSONString(resultMap));
                    logger.info("HomeService.productInfo，入库成功：{}", JSON.toJSONString(resultMap));

                    //入库成功，修改状态值
                    String updateSql = "UPDATE base_info SET is_processed=1 where id= " + jsonObject.getInteger("id");
                    jdbcTemplate.update(updateSql);
                    logger.info("HomeService.productInfo，修改状态成功，修改的ID为：{}", jsonObject.getInteger("id"));
                }
            }
        }catch (Exception e){
            logger.error("HomeService.productInfo，查询失败：{}", e);
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     *  危险货物分类的数据入库
     * */
    public String riskCategoryBaseInfo(String chemicals){
        logger.info("HomeService.riskCategoryBaseInfo，检索的产品为：{}", chemicals);
        String jsonContent = DownWebIdUtils.findWebInfo(chemicals,2);
        try {
            if (!StringUtils.isEmpty(jsonContent)){
                JSONArray jsonArray = JSONArray.parseArray(jsonContent);
                for (int i=0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    try {
                        String sql = "INSERT INTO risk_base_info (web_id, hg_id, cas_no, name_en, name_ch) VALUES (?,?,?,?,?);";
                        jdbcTemplate.update(sql, jsonObject.getInteger("id"), java.net.URLDecoder.decode(jsonObject.getString("hgId"), "UTF-8"),
                                jsonObject.getString("casNo"), jsonObject.getString("nameEn"), jsonObject.getString("nameCh"));
                        logger.info("HomeService.riskCategoryBaseInfo，入库成功：{}", jsonObject.getString("nameCh"));
                    }catch (Exception e){
                        logger.warn("HomeService.riskCategoryBaseInfo，库中已存在：{}", jsonObject.getString("nameCh"));
                    }
                }
            }
        }catch (Exception e){
            logger.error("HomeService.riskCategoryBaseInfo，处理失败：{}", e);
        }
        return jsonContent;
    }
    /**
     *  危险货物分类
     *
     *  先拿一条数据进行测试
     * */
    public String riskCargoCategory(){

        String querySql = "SELECT id,web_id webId, hg_id hgId, cas_no casNo, name_en nameEn, name_ch nameCh FROM risk_base_info WHERE is_processed=0 LIMIT 10;";
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql);
            logger.info("HomeService.riskCargoCategory，查询未经处理的数据条数：{}", mapList.size());
            if (!ObjectUtils.isEmpty(mapList)){
                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(mapList));
                for (int i=0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Document document = DownWebIdUtils.riskCargoCategory(jsonObject.getString("hgId"));
                    Elements listTable = document.getElementsByClass("ehstab");
                    Map<String, String> baseMap = new HashMap<>();
                    List<Map<String, String[]>> cargoList = new ArrayList<>();
                    for (Element table : listTable){
                        Elements listTr = table.getElementsByTag("tr");
                        for (Element tr : listTr){
                            if (tr.children().size() == 2){
                                baseMap.put(tr.child(0).text().trim(), tr.child(1).text().trim());
                            }else if (tr.children().size() == 3){
                                Map<String, String[]> map = new HashMap<>();
                                map.put(tr.child(0).text().trim(), new String[]{tr.child(1).text().trim(), tr.child(2).text().trim()});
                                cargoList.add(map);
                            }
                        }
                    }
                    resultMap.put("baseInfo", baseMap);
                    resultMap.put("riskCargo", cargoList);
                    if (ObjectUtils.isEmpty(resultMap.get("baseInfo")) && ObjectUtils.isEmpty(resultMap.get("riskCargo"))){
                        //若为空，则代表当前产品无危险产品信息
                        String isNullSql = "UPDATE risk_base_info SET is_processed=2 where id=" + jsonObject.getInteger("id");
                        jdbcTemplate.execute(isNullSql);
                        logger.info("HomeService.riskCargoCategory，该产品无危险货物描述：{}", jsonObject.getString("nameCh"));
                    }else {
                        String insertSql = "INSERT INTO risk_chemicals_data (base_id, json) VALUES (?,?);";
                        jdbcTemplate.update(insertSql, jsonObject.getInteger("id"), JSON.toJSONString(resultMap));
                        String updateStatus = "UPDATE risk_base_info SET is_processed=1 where id=" + jsonObject.getInteger("id");
                        jdbcTemplate.execute(updateStatus);
                        logger.info("HomeService.riskCargoCategory，入库成功：{}", jsonObject.getString("nameCh"));
                    }
                }
            }
        }catch (Exception e){
            logger.error("HomeService.riskCargoCategory，查询失败：{}", e);
        }
        return JSON.toJSONString(resultMap);
    }
}
