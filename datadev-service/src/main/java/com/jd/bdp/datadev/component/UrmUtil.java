package com.jd.bdp.datadev.component;

import com.jd.bdp.api.urm.UserInterface;
import com.jd.bdp.api.urm.UserJsfInterface;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.domain.urm.TblBaseUser;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class UrmUtil {

    private static final Logger logger = Logger.getLogger(UrmUtil.class);

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Value("${smp.domain.name}")
    private String smpUrl;

    @Autowired
    private UserJsfInterface userInterface;

    private final String BDP_MANAGER = "bdp_ide";

    public String getNameByErp(String erp) throws Exception {
        try {
            TblBaseUser user = userInterface.findUserByErp(erp);
            if (user != null) {
                return user.getName();
            }
        } catch (Exception e) {
            logger.error("===============================usertoken:"+e);
        }
        return "";
    }
    public String getErpAndNameByErp(String erp) throws Exception {
        String result = "";
        try {
            String[]erps = erp.split(",");
            for(String item:erps){
                if(StringUtils.isNotBlank(item)){
                    TblBaseUser user = userInterface.findUserByErp(item);
                    String name = (user!=null && StringUtils.isNotBlank(user.getName()))?(user.getName()+"("+item+")"):item;
                    result+=name+",";
                }
            }
            if (StringUtils.isNotBlank(result)) {
                result=result.substring(0,result.length()-1);
            }
        } catch (Exception e) {
            logger.error("===============================usertoken:"+e);
        }
        return result;
    }
    public TblBaseUser getErpByEmail(String email) throws Exception {
        try {
            TblBaseUser user = userInterface.getUserByEmail(email);
            return user;
        } catch (Exception e) {
            logger.error("===============================userByEmail:"+e);
        }
        return null;
    }

    /**
     * @param lists
     * @param cnvertErp2UserName
     * @param <T>
     * @throws Exception
     */
    public <T> void covertUserErp2UserName(Iterable<T> lists, ConvertErp2UserName<? super T> cnvertErp2UserName) throws Exception {
        /**
         * 过滤出需要查询的erp
         */
        if (lists == null) {
            return;
        }
        Set<String> erpSet = new HashSet<String>();
        for (T t : lists) {
            String erp = cnvertErp2UserName.getErp(t);
            if (StringUtils.isNotBlank(erp)) {
                erpSet.add(erp.trim());
            }
        }
        //获取列表的userName
        Map<String, String> erpName = new HashMap<String, String>();
        if (erpSet != null && erpSet.size() > 0) {
            List<TblBaseUser> tblBaseUserList = userInterface.findUserByErps(erpSet.toArray(new String[]{}));
            if (tblBaseUserList != null && tblBaseUserList.size() > 0) {
                for (TblBaseUser baseUser : tblBaseUserList) {
                    erpName.put(baseUser.getErp(), baseUser.getName());
                }
            }
        }

        //设置userName
        for (T t : lists) {
            String erp = cnvertErp2UserName.getErp(t);
            String userName = erpName.get(erp);
            cnvertErp2UserName.setErpUserName(t, userName);
        }
    }

    public String UserTokenByErp(HttpServletRequest request, String erp) throws Exception {
        try {
            TblBaseUser user = userInterface.findUserByErp(erp);
            if (user != null) {
                return "URM" + user.getPwd();
            }
//            return "URMd91c37903bab9a5c87a250d7eaa0dc71";
//            return "URM16fbcf7dc6458dd6da8aba523b69cc22";
//            Cookie[] c = request.getCookies();
//            if (c == null) {
//                return null;
//            } else {
//                Cookie[] arr$ = c;
//                int len$ = c.length;
//                for (int i$ = 0; i$ < len$; ++i$) {
//                    Cookie cookie = arr$[i$];
//                    if ("sso.jd.com".equals(cookie.getName())) {
//                        return cookie.getValue();
//                    }
//                }
//                return null;
//            }
        } catch (Exception e) {
            logger.error("===============================usertoken:"+e);

        }
        return "";
    }

    public String getErpByUserToken(String userToken) {
        try {
            logger.error("=========== userToken:" + userToken);
            Map<String, String> params = new HashMap();
            params.put("userToken", userToken);
            String json = HttpUtil.doPost(smpUrl + "/api/user/verifyUserToken", params);
            if (StringUtils.isNotEmpty(json)) {
                JSONObject userJson = JSONObject.fromObject(json);
                if (userJson != null && userJson.size() > 0 && userJson.get("code").equals("0") && userJson.get("erp") != null) {
                    String userErp = userJson.get("erp").toString();
                    return userErp;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    public String getBdpManager(){
        return BDP_MANAGER;
    }

}
