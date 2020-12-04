package com.jd.bdp.datadev.web.controller;

import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.RedisComponent;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.service.ClientBaseQueryService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClientController {

    @Autowired
    ClientBaseQueryService ClientBaseQueryService;


    @Autowired
    private RedisComponent redisComponent ;

    @RequestMapping("/scriptcenter/client.html")
    public String queryBase(Model model) throws Exception{

        Map<String,String> cacheValue = new HashMap<String,String>();
        cacheValue.put(RedisComponent.KEY_OF_REDIS_DO_SQL_TOOL_CHECK,redisComponent.getValue(RedisComponent.KEY_OF_REDIS_DO_SQL_TOOL_CHECK,Boolean.TRUE.toString()));
        model.addAttribute("cacheValue",cacheValue);
        return "/scriptcenter/home/clientQuery";
    }

    @RequestMapping({"/scriptcenter/setCacheValue.ajax"})
    @ResponseBody
    public com.alibaba.fastjson.JSONObject setCacheValueAjax(String key , String value) {
        redisComponent.setValue(key,value);
        return JSONObjectUtil.getSuccessResult("成功");
    }


    @RequestMapping("/scriptcenter/clientInfo.html")
    public String clientInfoQuery(Model model, String ip) throws Exception{
        List<DataDevClientBase> dataDevClientInfo = ClientBaseQueryService.queryClientInfo(ip);
        model.addAttribute("clientInfo", com.alibaba.fastjson.JSONObject.toJSONStringWithDateFormat(dataDevClientInfo, "yyyy-MM-dd HH:mm:ss"));
        return "/scriptcenter/home/clientInfoQuery";
    }
    @RequestMapping("/scriptcenter/modifyStatus.ajax")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject modifyStatus(Model model, Integer status, Long id) throws Exception{
        ClientBaseQueryService.modifyStatus(id,status);
        return JSONObjectUtil.getSuccessResult("成功","fds");
    }
    @RequestMapping("/scriptcenter/clientQuery.ajax")
    @ResponseBody
    public JSONObject clientQuery(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "rows", defaultValue = "10") int rows) throws Exception{
        PageResultDTO pageResultDTO = new PageResultDTO();
        Pageable pageable = new PageRequest(page - 1, rows);
        try {
            pageResultDTO = ClientBaseQueryService.list4page(pageable);
        } catch (Exception e) {
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }
        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
//        return AjaxUtil.gridJson(pageResultDTO);
        return AjaxUtil.gridJson(pageResultDTO);
    }
}
