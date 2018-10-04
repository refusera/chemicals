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
                        logger.info("HomeService.beforeSigle，入库成功：{}", jsonObject.getString("nameCh"));
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
        String querySql = "SELECT id,web_id webId, hg_id hgId, cas_no casNo, name_en nameEn, name_ch nameCh FROM base_info WHERE is_processed=0 LIMIT 10;";
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


    /**
     *  UN编号查询
     *
     *  编号范围： 1000-6000
     * */
    public String queryByUn(int totalCount) {

        String selectSql = "select id,un_number unNumber from query_by_un where is_processed=0 limit " + totalCount;
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql);
        for (Map<String, Object> map : mapList) {
            int unNumber = Integer.parseInt(map.get("unNumber").toString());
            logger.info("HomeService.queryByUn，当前查询UN编号为：{}", unNumber);
            try {
                Document document = DownWebIdUtils.downQueryByUn(unNumber);
                if (!ObjectUtils.isEmpty(document.getElementsByClass("ehstab"))) {
                    Map<String, Object> allDataMap = new HashMap<>();
                    Elements listTable = document.getElementsByClass("ehstab");
                    for (int j = 0; j < listTable.size(); j++) {
                        Elements listTr = listTable.get(j).getElementsByTag("tr");
                        Map<String, String> dataMap = new HashMap<>();
                        for (Element tr : listTr) {
                            if (tr.children().size() == 2) {
                                dataMap.put(tr.child(0).text().trim(), tr.child(1).text().trim());
                            }
                        }
                        allDataMap.put("data" + j, dataMap);
                    }

                    //插入数据库
                    String insertSql = "update query_by_un set json=? where id = " + Integer.parseInt(map.get("id").toString());
                    jdbcTemplate.update(insertSql, JSON.toJSONString(allDataMap));
                    //修改状态
                    String updateSql = "update query_by_un set is_processed=1 where id=" + Integer.parseInt(map.get("id").toString());
                    jdbcTemplate.execute(updateSql);
                    logger.info("HomeService.queryByNo，插入数据库成功：{}，病修改状态为1", unNumber);
                } else {
                    String updateSql = "update query_by_un set is_processed=2 where id=" + Integer.parseInt(map.get("id").toString());
                    jdbcTemplate.execute(updateSql);
                    logger.info("HomeService.queryByNo，当前编号无信息：{}，并修改状态为2", unNumber);
                }
            } catch (Exception e) {
                logger.error("HomeService.queryByNo，查询失败：{}", e);
            }
        }
        return null;
    }
    /**
     *  食品接触材料原辅料查询
     *  两个板块：
     *      第一部分：查询所有产品的ID
     * */
    public String queryRawMaterialId(String product){
        String content = "";
        try {
            content = DownWebIdUtils.findWebInfo(product, 3);
            if (!StringUtils.isEmpty(content)){
                JSONArray jsonArray = JSONArray.parseArray(content);
                for (int i=0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String insertSql = "insert into rawmaterial_id(cas, cname, fca, web_id) values (?,?,?,?);";
                    try {
                        jdbcTemplate.update(insertSql, jsonObject.getString("cas"), jsonObject.getString("cname"), jsonObject.getString("fca"), java.net.URLDecoder.decode(jsonObject.getString("id"), "UTF-8"));
                        logger.info("HomeService.queryRawMaterialId， 插入数据库成功：{}", jsonObject.getString("cname"));
                    }catch (Exception e){
                        logger.info("HomeService.queryRawMaterialId， 插入的数据已存在：{}", jsonObject.getString("cname"));
                    }
                }
            }
        }catch (Exception e){
            logger.error("HomeService.queryRawMaterialId，查询失败：{}", e);
        }
        return content;
    }
    /**
     *  食品接触材料原辅料查询
     *  两个板块：
     *      第二部分：通过产品ID，查询商品
     * */
    public String queryRawMaterialInfo(){
        String content = "";
        try {
            String selectSql = "SELECT id,cname, web_id webId FROM `rawmaterial_id` where is_processed=0 limit 10";
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql);
            for (Map<String, Object> map : mapList){
                String webId = map.get("webId").toString();
                Document document = DownWebIdUtils.findRawMaterialDoc(webId);
                if (!ObjectUtils.isEmpty(document.getElementById("fcmL"))){
                    List<Map<String, String>> endList = new ArrayList<>();

                    if (!ObjectUtils.isEmpty(document.getElementById("fcmL").getElementsByTag("table"))){  //GB9685
                        Elements trList = document.getElementById("fcmL").getElementsByTag("table").get(0).getElementsByTag("tr");
                        for (int i=1; i<trList.size(); i++){
                            Element tr = trList.get(i);
                            if (tr.children().size() == 10){
                                Map<String, String> dataMap = new HashMap<>();
                                dataMap.put("表序", tr.child(0).text().trim());
                                dataMap.put("FCA号", tr.child(1).text().trim());
                                dataMap.put("中文名称", tr.child(2).text().trim());
                                dataMap.put("CAS号", tr.child(3).text().trim());
                                dataMap.put("使用范围和最大使用量/%", tr.child(4).text().trim());
                                dataMap.put("SML/QM/(mg/kg)", tr.child(5).text().trim());
                                dataMap.put("SML(T)/(mg/kg)", tr.child(6).text().trim());
                                dataMap.put("SML(T)分组编号", tr.child(7).text().trim());
                                dataMap.put("其它要求", tr.child(8).text().trim());
                                dataMap.put("GB 9685中页码", tr.child(9).text().trim());
                                endList.add(dataMap);
                            }
                        }
                    }else if (!ObjectUtils.isEmpty(document.getElementById("fcmfaL").getElementsByTag("table"))){  // GB 4806
                        Elements trList = document.getElementById("fcmfaL").getElementsByTag("table").get(0).getElementsByTag("tr");
                        for (int i=1; i<trList.size(); i++){
                            Element tr = trList.get(i);
                            if (tr.children().size() == 9){
                                Map<String, String> dataMap = new HashMap<>();
                                dataMap.put("表序", tr.child(0).text().trim());
                                dataMap.put("序号", tr.child(1).text().trim());
                                dataMap.put("中文名称", tr.child(2).text().trim());
                                dataMap.put("CAS号", tr.child(3).text().trim());
                                dataMap.put("通用类别名", tr.child(4).text().trim());
                                dataMap.put("SML/QM/(mg/kg)", tr.child(5).text().trim());
                                dataMap.put("SML(T)/(mg/kg)", tr.child(6).text().trim());
                                dataMap.put("SML(T)分组编号", tr.child(7).text().trim());
                                dataMap.put("其它要求", tr.child(8).text().trim());
                                endList.add(dataMap);
                            }
                        }
                    }/*else if (!ObjectUtils.isEmpty(document.getElementById("fcmwjwL").getElementsByTag("table"))){
                        Elements trList = document.getElementById("fcmwjwL").getElementsByTag("table").get(0).getElementsByTag("tr");
                        for (int i=1; i<trList.size(); i++){
                            Element tr = trList.get(i);
                            if (tr.children().size() == 9){
                                Map<String, String> dataMap = new HashMap<>();
                                dataMap.put("表序", tr.child(0).text().trim());
                                dataMap.put("序号", tr.child(1).text().trim());
                                dataMap.put("中文名称", tr.child(2).text().trim());
                                dataMap.put("CAS号", tr.child(3).text().trim());
                                dataMap.put("通用类别名", tr.child(4).text().trim());
                                dataMap.put("SML/QM/(mg/kg)", tr.child(5).text().trim());
                                dataMap.put("SML(T)/(mg/kg)", tr.child(6).text().trim());
                                dataMap.put("SML(T)分组编号", tr.child(7).text().trim());
                                dataMap.put("其它要求", tr.child(8).text().trim());
                                endList.add(dataMap);
                            }
                        }
                    }*/
                    content = JSON.toJSONString(endList);
                    String insertSql = "insert into rawmaterial_info(rid, json) values (?,?);";
                    jdbcTemplate.update(insertSql, map.get("id"), JSON.toJSONString(endList));
                    String updateSqi = "update rawmaterial_id set is_processed=1 where id="+map.get("id");
                    jdbcTemplate.execute(updateSqi);
                    logger.info("HomeService.queryRawMaterialInfo，数据插入成功：{}，修改状态为1", map.get("cname").toString());
                }else {
                    String updateSqi = "update rawmaterial_id set is_processed=2 where id="+map.get("id");
                    jdbcTemplate.execute(updateSqi);
                    logger.info("HomeService.queryRawMaterialInfo，当前商品无信息：{}，修改状态为2", map.get("cname").toString());
                }
            }
        }catch (Exception e){
            logger.error("HomeService.queryRawMaterialInfo，查询失败：{}", e);
        }
        return content;
    }
}
