package com.jd.bdp.datadev.component;

import com.jd.bdp.domain.urm.TblBaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class UrmStaticUtil {
    private static UrmUtil urmUtil;

    @Autowired
    public void setDatabase(UrmUtil urmUtil) {
        this.urmUtil = urmUtil;
    }
    public  static String getNameByErp(String erp) throws Exception {
        return urmUtil.getNameByErp(erp);
    }
    public static String getErpAndNameByErp(String erp) throws Exception {
        return urmUtil.getErpAndNameByErp(erp);
    }
    public static TblBaseUser getErpByEmail(String email) throws Exception {
        return urmUtil.getErpByEmail(email);
    }

    public static String UserTokenByErp(HttpServletRequest request, String erp) throws Exception {
        return urmUtil.UserTokenByErp(request,erp);
    }

    public static String getErpByUserToken(String userToken) {
        return urmUtil.getErpByUserToken(userToken);
    }
    public static String getBdpManager(){
        return urmUtil.getBdpManager();
    }



}
