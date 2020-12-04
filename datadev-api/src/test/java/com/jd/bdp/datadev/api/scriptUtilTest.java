
package com.jd.bdp.datadev.api;

import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.util.ApiEnv;
import com.jd.bdp.datadev.util.MD5Util;
import org.junit.Test;

import java.io.File;
import java.util.Date;

public class scriptUtilTest {

    @Test
    public void downLoadTest() throws Exception {
//        //        test load
//        ApiEnv.setEnv("test");
//        Script script = new Script();
//        script.setGitProjectFilePath("接口上传脚本测试/王晓丽测试-勿删/sqltest20180731-01.sql");
//        script.setGitProjectId(23057l);
//        script.setVersion("1005");
////        script.setGitProjectFilePath("fsdfs1df1529397281763.zip");
//        String appId = "saasupload.jd.care";
//        String appToken = "bfaeeb0e73da311d72d87dc37baa52d2";
////        String userToken = "URMd91c37903bab9a5c87a250d7eaa0dc71";
////        String userToken = "3e0f84f84153456b93a361e484aa71c2";
//        String userToken = "URM769194c1d18a7359611307b5d166af77";
//        Long time = new Date().getTime();
//        String timeStr = Long.toString(time);
//        String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);
//        ApiResult apiResult = ScriptUtil.loadScript(appId, userToken, time, sign, "C:\\Users\\zhanglei68\\Desktop\\codemirror\\", script);

//        JSONObject jsonObject=new JSONObject();
//        jsonObject.put("code",1);
//        Script script=new Script();
//        script.setVersion("100");
//        script.setId(222L);
//        jsonObject.put("obj",script);
//        String jsonStr=jsonObject.toJSONString();
//        ApiResult apiResult = JSONObject.parseObject(jsonStr, ApiResult.class);
//        Script ss=JSONObject.parseObject(apiResult.getObj().toString(),Script.class);
//        apiResult.setObj(ss);
//        Script tmp=(Script) apiResult.getObj();
    }

//    @Test
//    public void upLoadTest() throws Exception{
////        ApiEnv.setEnv("test");
//        Script script = new Script();
//        script.setGitProjectId(29106L);
//        script.setName("ate111st.sql");
//        script.setType(1);
////        script.setGitProjectFilePath("fsdfs1df1529397281763.zip");
//        String appId = "bdt.jd.com";
//        String appToken = "50ae5a5c419af1bd5331f7007ae01bdb";
////        String userToken = "URMd91c37903bab9a5c87a250d7eaa0dc71";
////        String userToken = "3e0f84f84153456b93a361e484aa71c2";
//        String userToken = "URM16835c0c9e2dca9100e331488b1170f6";
//        File file=new File("C:\\Users\\zhanglei68\\Desktop\\codemirror\\11112wqe111w.sql");
//        Long time = new Date().getTime();
//        String timeStr = Long.toString(time);
//        String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);
//        ApiResult apiResult = ScriptUtil.uploadScript(appId, userToken, time, sign, file, script);
//
//    }
}

