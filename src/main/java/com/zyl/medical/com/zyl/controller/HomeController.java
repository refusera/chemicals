package com.zyl.medical.com.zyl.controller;

import com.zyl.medical.com.zyl.service.HomeService;
import com.zyl.medical.com.zyl.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

    @RequestMapping("/search")
@RestController
public class HomeController {

    @Autowired
    private HomeService homeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     *  通过检索的单词，入库当前平台的ID（因为后续的所有查询，都是基于他们平台的ID的）
     *
     *  当前这一步并不是查询信息，而是通过产品名称，搜索到这个产品在当前平台的ID，后续所有操作都是基于他们平台查询到的这个ID进行查询
     *
     * @param chemicals : 检索的产品名字
     * */
    @RequestMapping(value = "/before", method = RequestMethod.GET)
    @ResponseBody
    public String before(@RequestParam String chemicals, HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.beforeSigle(chemicals);
    }


    /**
     *  这一步具体还不清楚要做啥，目前的做法是拿到匹配库的产品进行检索生成的一张表（对应他们平台对于这个产品生成的ID），用这张表所有产品的ID都进行检索，然后分别入库
     * */
    @RequestMapping(value = "/chemicalsData", method = RequestMethod.GET)
    @ResponseBody
    public String chemicalsData(HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.chemicalsData();
    }


    /**
     *
     *
     * */
    @RequestMapping(value = "/riskCategoryBaseInfo", method = RequestMethod.GET)
    @ResponseBody
    public String riskCategoryBaseInfo(@RequestParam String chemicals, HttpServletRequest request){

        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.riskCategoryBaseInfo(chemicals);
    }

    /**
     *  查询产品的危险货物分类
     * */
    @RequestMapping(value = "riskCargoCategory", method = RequestMethod.GET)
    @ResponseBody
    public String riskCargoCategory(HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.riskCargoCategory();
    }
}
