package com.jd.bdp.datadev.web.controller.script;

import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.urm2.api.dto.JSFResultDTO;
import com.jd.bdp.urm2.api.user.UrmUserInterface;
import com.jd.bdp.urm2.domain.user.TblBaseUser;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenqianqian on 16-12-8.
 */
@Controller
@RequestMapping("/scriptcenter/common/user")
public class UserController {


    private static final Logger logger = Logger.getLogger(UserController.class);

    @Value("${datadev.appId}")
    private String appCode;
    @Value("${datadev.token}")
    private String token;
    @Autowired
    private UrmUserInterface urmUserInterface;

    /**
     * 搜索erp列表
     *
     * @param keyword
     * @return
     */
    @RequestMapping("list_user.ajax")
    @ResponseBody
    public JSONObject listUser(String keyword) {

        List<TblBaseUser> userList = new ArrayList<TblBaseUser>();
        try {
            keyword= URLDecoder.decode(keyword,"utf-8");
            keyword= URLDecoder.decode(keyword,"utf-8");
            TblBaseUser user = new TblBaseUser();
            user.setSearchValue(keyword);
            String data = urmUserInterface.list(appCode, token, System.currentTimeMillis(), user);
            Map<String,Class> map = new HashMap<String, Class>();
            map.put("obj", TblBaseUser.class);
            JSFResultDTO dto = (JSFResultDTO) JSONObject.toBean(JSONObject.fromObject(data), JSFResultDTO.class,map);
            if (dto !=null && dto.getCode() != null && dto.getCode().intValue() == 0) {
                userList = dto.getList();
            }
            return AjaxUtil.list2Json(userList);
        } catch (Exception e) {
            logger.error("获取用户列表错误", e);
            return AjaxUtil.list2Json(userList);
        }
    }

    /**
     * 根据erp列表获取对应用户信息
     *
     * @param erps
     * @return
     */
    @RequestMapping("list_users_by_erps.ajax")
    @ResponseBody
    public JSONObject listUsersByErps(String erps) {
        List<TblBaseUser> userList = new ArrayList<TblBaseUser>();
        try {
            String[] erpArr = StringUtils.split(erps, ",");
            for (String erp : erpArr) {

                if (StringUtils.isEmpty(erp)) {
                    continue;
                }
                TblBaseUser user = new TblBaseUser();
                user.setErp(erp);
                String data = urmUserInterface.find(appCode, token, System.currentTimeMillis(), user);
                Map<String,Class> map = new HashMap<String, Class>();
                map.put("obj", TblBaseUser.class);
                JSFResultDTO dto = (JSFResultDTO) JSONObject.toBean(JSONObject.fromObject(data), JSFResultDTO.class,map);
                if (dto !=null && dto.getCode() != null && dto.getCode().intValue() == 0) {
                    TblBaseUser tmp = (TblBaseUser)dto.getObj();
                    userList.add(tmp);
                }
                if (user != null) {
                    userList.add(user);
                }
            }
            return AjaxUtil.list2Json(userList);
        } catch (Exception e) {
            logger.error("获取用户列表错误", e);
            return AjaxUtil.list2Json(userList);
        }
    }

    @RequestMapping("list_groups.ajax")
    @ResponseBody
    public JSONObject listGroups(String keyWord) {
        List<DataDevGitGroup> list = new ArrayList<DataDevGitGroup>();
        try {
            if (keyWord != null && keyWord.length() != 0)
                keyWord= URLDecoder.decode(keyWord,"utf-8");
            JDGitGroups jdGitGroups = new JDGitGroups();
            jdGitGroups.setOwned(false);
            list = jdGitGroups.listGroups(keyWord);
            return AjaxUtil.list2Json(list);
        } catch (Exception e) {
            logger.error("组列表获取失败", e);
            return AjaxUtil.list2Json(list);
        }
    }

    public static void main(String[] args) {
        System.out.println(null instanceof String);
    }

}
