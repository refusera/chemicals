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


    /********************************************************************************************************************************************************************/
    /***************************************************************          化学品数据          ***********************************************************************/
    /********************************************************************************************************************************************************************/
    /**
     *  第一板块：化学品数据
     *      第一部分：获取当前板块的所有产品ID
     * @param chemicals : 检索的产品名称
     * */
    @RequestMapping(value = "/before", method = RequestMethod.GET)
    @ResponseBody
    public String before(@RequestParam String chemicals, HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.beforeSigle(chemicals);
    }


    /**
     *
     *  第一板块：化学品数据
     *      第二部分：通过产品ID，进行检索查询
     * */
    @RequestMapping(value = "/chemicalsData", method = RequestMethod.GET)
    @ResponseBody
    public String chemicalsData(HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.chemicalsData();
    }



    /********************************************************************************************************************************************************************/
    /***************************************************************          危险货物分类          ***********************************************************************/
    /********************************************************************************************************************************************************************/
    /**
     *  第二板块：危险货物分类
     *      第一部分：获取所有产品的ID
     * */
    @RequestMapping(value = "/riskCategoryBaseInfo", method = RequestMethod.GET)
    @ResponseBody
    public String riskCategoryBaseInfo(@RequestParam String chemicals, HttpServletRequest request){

        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.riskCategoryBaseInfo(chemicals);
    }

    /**
     *  第二板块：危险货物分类
     *      第二部分：通过获取的产品ID，进行检索查询
     * */
    @RequestMapping(value = "riskCargoCategory", method = RequestMethod.GET)
    @ResponseBody
    public String riskCargoCategory(HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.riskCargoCategory();
    }


    /********************************************************************************************************************************************************************/
    /***************************************************************          危险货物分类          ***********************************************************************/
    /********************************************************************************************************************************************************************/

    /**
     *  UN编号查询
     * @param totalCount 一次性选择处理多少条  （0-5000）
     * */
    @RequestMapping(value = "/queryByUn", method = RequestMethod.GET)
    @ResponseBody
    public String queryByUn(@RequestParam int totalCount, HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.queryByUn(totalCount);
    }

    /**
     *  食品接触材料原辅料查询
     *  两个板块：
     *      第一部分：查询所有产品的ID
     * */
    @RequestMapping(value = "/queryRawMaterialId", method = RequestMethod.GET)
    @ResponseBody
    public String queryRawMaterialId(@RequestParam String product, HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.queryRawMaterialId(product);
    }

    /**
     *  食品接触材料原辅料查询
     *  两个板块：
     *      第二部分：通过产品ID，查询商品
     * */
    @RequestMapping(value = "/queryRawMaterialInfo", method = RequestMethod.GET)
    @ResponseBody
    public String queryRawMaterialInfo(HttpServletRequest request){
        logger.info("X-Real-IP >> " + (IpUtils.clientIp(request)) + request.getRequestURI());
        return homeService.queryRawMaterialInfo();
    }
}
