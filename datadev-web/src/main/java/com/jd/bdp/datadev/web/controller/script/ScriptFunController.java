package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.domain.DataDevFunDetail;
import com.jd.bdp.datadev.domain.ZtreeNode;
import com.jd.bdp.datadev.enums.DataDevScriptFunTypeEnum;
import com.jd.bdp.datadev.service.DataDevScriptFunService;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.List;

@Controller
@RequestMapping("/scriptcenter/funtion/")
public class ScriptFunController {
    private static final Logger logger = Logger.getLogger(ScriptFunController.class);
    @Autowired
    private DataDevScriptFunService funService;
    @Autowired
    private UrmUtil urmUtil;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @ExceptionMessageAnnotation(errorMessage = "获取函数列表")
    @RequestMapping("getFunsByDirId.ajax")
    @ResponseBody
    public JSONObject getFunsByDirId(Long dirId) throws Exception {
        List<ZtreeNode> ztreeNodeList = funService.getFunsByDirId(dirId);
        return JSONObjectUtil.getSuccessResult(ztreeNodeList);
    }

    @ExceptionMessageAnnotation(errorMessage = "搜索函数")
    @RequestMapping("searchFunctions.ajax")
    @ResponseBody
    public JSONObject getFunsByDirId(String keyword) throws Exception {
        List<ZtreeNode> ztreeNodeList = funService.getFunsByKeyword(keyword);
        return JSONObjectUtil.getSuccessResult(ztreeNodeList);
    }

    @RequestMapping("getFunById.ajax")
    @ResponseBody
    public JSONObject getFunById(Long id) throws Exception {
        DataDevFunDetail devFunDetail = funService.findById(id);
        DataDevScriptFunTypeEnum typeEnum = DataDevScriptFunTypeEnum.enumValueOf(devFunDetail.getType());
        if (typeEnum != null) {
            devFunDetail.setTypeStr(typeEnum.toDesc());
        }
        if (DataDevScriptFunTypeEnum.SystemFun.tocode().equals(typeEnum.tocode())) {
            devFunDetail.setMender("系统提供");
            devFunDetail.setOwner("系统提供");
        } else {
            devFunDetail.setMender(urmUtil.getNameByErp(devFunDetail.getMender()) + "(" + devFunDetail.getMender() + ")");
            devFunDetail.setOwner(urmUtil.getNameByErp(devFunDetail.getOwner()) + "(" + devFunDetail.getOwner() + ")");
        }
        devFunDetail.setModifiedStr(devFunDetail.getModified() != null ? sdf.format(devFunDetail.getModified()) : "");

        return JSONObjectUtil.getSuccessResult(devFunDetail);
    }

    @ExceptionMessageAnnotation(errorMessage = "获取函数提示")
    @RequestMapping("getFunTips.ajax")
    @ResponseBody
    public JSONObject getFunTips(String scriptType) throws Exception{

        if (StringUtils.isEmpty(scriptType)) {
            throw new RuntimeException("参数scriptType为空");
        }
        String funTipStr = funService.getFunTipsByScriptType(scriptType);
        return JSONObjectUtil.getSuccessResult(funTipStr);

    }
//
}
