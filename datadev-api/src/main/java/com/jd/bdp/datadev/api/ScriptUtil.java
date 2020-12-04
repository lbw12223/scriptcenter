package com.jd.bdp.datadev.api;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.enums.ScriptTypeEnum;
import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.util.ApiEnv;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.datadev.util.MD5Util;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class ScriptUtil {
    private static final String UPLOAD_URL = "datadev/api/uploadScript";

    private static final String DOWNLOAD_URL = "datadev/api/downloadScript";

    /**
     * 上传脚本文件
     *
     * @param appId     系统调用的唯一标识, 例如 域名、系统名 www.jd.com 需要申请
     * @param userToken 校验请求用户：1、erp单点登录后获取的tiken；2、找我们申请普通用户的urmToken或虚拟账号的urmToken
     * @param time      时间戳，判断请求有效期：
     * @param sign      生成规则：MD5（appToken+userToken+time） appToken：根据appId生成唯一token
     * @param file      上传的文件
     * @param data      脚本参数 ：gitProjectId：项目id（必需）
     *                  gitProjectFilePath：脚本路径,为空时为上传新脚本；非空时则更新相应脚本的版本（非必需）
     *                  gitProjectDirPath：脚本目录路径，当gitProjectFilePath为空时这个字段决定脚本的目录，为空则脚本放在根目录下，当gitProjectFilePath不为空时，这个字段无意义（非必需）
     *                  name:脚本名字（必需）
     *                  type：脚本类型详情见{@link com.jd.bdp.datadev.enums.ScriptRunStatusEnum}（必需）
     *                  startShellPath:脚本类型为ZIP类型时必填，启动shell脚本的路径（非必需）
     *                  owner：负责人erp，如果为空则获取userToken对应的erp（非必需）
     *                  description：脚本描述（非必需）
     * @return 上传结果
     */
    public static ApiResult uploadScript(String appId, String userToken, Long time, String sign, File file, Script data) throws Exception {
        InputStream inputStream = new FileInputStream(file);
        ApiResult apiResult = uploadScript(appId, userToken, time, sign, inputStream, data);
        return apiResult;
    }

    /**
     * 上传脚本文件
     *
     * @param appId
     * @param userToken
     * @param data
     * @return
     */
    public static ApiResult uploadScript(String appId, String userToken, Long time, String sign, InputStream inputStream, Script data) throws Exception {
        if (StringUtils.isBlank(appId)) {
            throw new RuntimeException("appId不能为空");
        }
        if (StringUtils.isBlank(userToken)) {
            throw new RuntimeException("userToken不能为空");
        }
        if (time == null) {
            throw new RuntimeException("time不能为空");
        }
        if (StringUtils.isBlank(sign)) {
            throw new RuntimeException("sign");
        }
        if (StringUtils.isBlank(data.getName())) {
            throw new RuntimeException("脚本名字(name)不能为空");
        }
        if (data.getType() == null) {
            throw new RuntimeException("脚本类型(type)不能为空");
        }
//        if ((data.getGitProjectId() == null || data.getGitProjectId() <= 0L) && StringUtils.isBlank(data.getGitProjectPath())) {
//            throw new RuntimeException("gitProjectId 或者gitProjectPath不能同时为null");
//        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("gitProjectPath", data.getGitProjectPath());
        paramMap.put("gitProjectId", data.getGitProjectId());
        paramMap.put("gitProjectFilePath", data.getGitProjectFilePath());
        paramMap.put("gitProjectDirPath", data.getGitProjectDirPath());
        paramMap.put("name", data.getName());
        paramMap.put("type", data.getType());
        paramMap.put("appId", appId);
        paramMap.put("id", data.getId());
        paramMap.put("userToken", userToken);
        paramMap.put("time", time);
        paramMap.put("sign", sign);
        paramMap.put("owner", data.getOwner());
        paramMap.put("description", data.getDescription());
        paramMap.put("startShellPath", data.getStartShellPath());
        String res = HttpUtil.postFiles(ApiEnv.getDomain() + UPLOAD_URL, inputStream, paramMap);
        ApiResult apiResult = JSONObject.parseObject(res, ApiResult.class);
        if (apiResult != null && apiResult.getObj() != null) {
            String scriptStr = apiResult.getObj().toString();
            Script scr = JSONObject.parseObject(scriptStr, Script.class);
            apiResult.setObj(scr);
        }
        return apiResult;
    }


    /**
     * 下载脚本信息
     *
     * @param appId
     * @param userToken
     * @param script
     * @return
     */
    public static ApiResult loadScript(String appId, String userToken, Long time, String sign, String dirPath, Script script) throws Exception {
        return loadScript(appId, userToken, time, sign, dirPath, null, script);
    }

    public static ApiResult loadScript(String appId, String userToken, Long time, String sign, String dirPath, String fileName, Script script, String domain) throws Exception {
        ApiResult apiResult = new ApiResult();
        try {
            if (StringUtils.isBlank(appId)) {
                throw new RuntimeException("appId不能为空");
            }
            if (StringUtils.isBlank(userToken)) {
                throw new RuntimeException("userToken不能为空");
            }
            if (time == null) {
                throw new RuntimeException("time不能为空");
            }
            if (StringUtils.isBlank(sign)) {
                throw new RuntimeException("sign");
            }
            if (script.getId() == null) {
                if (script.getGitProjectId() == null && StringUtils.isBlank(script.getGitProjectPath())) {
                    throw new RuntimeException("脚本gitId(gitProjectId)不能为空");
                }
                if (StringUtils.isBlank(script.getGitProjectFilePath()) && StringUtils.isBlank(script.getGitProjectDirPath())) {
                    throw new RuntimeException("脚本路径(gitProjectFilePath)或者目录(gitProjectDirPath)不能同时为空");
                }
            }
            String paramUrl = script.getId() != null ? ("?id=" + script.getId()) : ("?gitProjectId=" + script.getGitProjectId());
            String url = domain + DOWNLOAD_URL + paramUrl + "&appId=" + appId + "&userToken=" + userToken + "&time=" + time + "&sign=" + sign;
            if (StringUtils.isNotBlank(script.getVersion())) {
                url += "&version=" + script.getVersion().trim();
            }
            if (StringUtils.isNotBlank(script.getGitProjectFilePath())) {
                url += "&gitProjectFilePath=" + URLEncoder.encode(URLEncoder.encode(script.getGitProjectFilePath(), "utf-8"), "utf-8");
            }
            if (StringUtils.isNotBlank(script.getGitProjectDirPath())) {
                url += "&gitProjectDirPath=" + URLEncoder.encode(URLEncoder.encode(script.getGitProjectDirPath(), "utf-8"), "utf-8");
            }
            if (script.getRunDetailId() != null) {
                url += "&runDetailId=" + script.getRunDetailId();
            }
            File file = HttpUtil.getFile(url, dirPath, fileName);
            apiResult.setCode(0);
            apiResult.setSuccess(true);
            apiResult.setMessage("下载成功");
            apiResult.setObj(file != null ? file.getAbsolutePath() : "");
        } catch (Exception e) {
            try {
                apiResult = JSONObject.parseObject(e.getMessage(), ApiResult.class);
            } catch (Exception json) {
                apiResult.setCode(1);
                apiResult.setSuccess(false);
                apiResult.setMessage("下载失败" + e.getMessage() != null ? e.getMessage() : "");
            }

        }
        return apiResult;
    }

    public static ApiResult loadScript(String appId, String userToken, Long time, String sign, String dirPath, String fileName, Script script) throws Exception {
        return loadScript(appId, userToken, time, sign, dirPath, fileName, script, ApiEnv.getDomain());

    }


    public static ApiResult loadScript(String appId, String userToken, Long time, String sign, Script script) throws Exception {
        ApiResult apiResult = new ApiResult();
        try {
            if (StringUtils.isBlank(appId)) {
                throw new RuntimeException("appId不能为空");
            }
            if (StringUtils.isBlank(userToken)) {
                throw new RuntimeException("userToken不能为空");
            }
            if (time == null) {
                throw new RuntimeException("time不能为空");
            }
            if (StringUtils.isBlank(sign)) {
                throw new RuntimeException("sign");
            }
            if (script.getId() == null) {
                if (script.getGitProjectId() == null && StringUtils.isBlank(script.getGitProjectPath())) {
                    throw new RuntimeException("脚本gitId(gitProjectId)不能为空");
                }
                if (StringUtils.isBlank(script.getGitProjectFilePath()) && StringUtils.isBlank(script.getGitProjectDirPath())) {
                    throw new RuntimeException("脚本路径(gitProjectFilePath)或者目录(gitProjectDirPath)不能同时为空");
                }
            }
            String paramUrl = script.getId() != null ? ("?id=" + script.getId()) : ("?gitProjectId=" + script.getGitProjectId());
            String url = ApiEnv.getDomain() + DOWNLOAD_URL + paramUrl + "&appId=" + appId + "&userToken=" + userToken + "&time=" + time + "&sign=" + sign;
            if (StringUtils.isNotBlank(script.getVersion())) {
                url += "&version=" + script.getVersion().trim();
            }
            if (StringUtils.isNotBlank(script.getGitProjectFilePath())) {
                url += "&gitProjectFilePath=" + URLEncoder.encode(URLEncoder.encode(script.getGitProjectFilePath(), "utf-8"), "utf-8");
            }
            if (StringUtils.isNotBlank(script.getGitProjectDirPath())) {
                url += "&gitProjectDirPath=" + URLEncoder.encode(URLEncoder.encode(script.getGitProjectDirPath(), "utf-8"), "utf-8");
            }
            if (script.getRunDetailId() != null) {
                url += "&runDetailId=" + script.getRunDetailId();
            }
            InputStream inputStream = HttpUtil.getStream(url);
            apiResult.setCode(0);
            apiResult.setSuccess(true);
            apiResult.setMessage("下载成功");
            apiResult.setObj(inputStream);
        } catch (Exception e) {
            apiResult.setCode(1);
            apiResult.setSuccess(false);
            apiResult.setMessage("下载失败" + e.getMessage() != null ? e.getMessage() : "");
        }
        return apiResult;
    }

    public static void main(String[] args) throws Exception {
//        String name="";
//        String code=URLEncoder.encode(name,"utf-8");
//        String encode= URLDecoder.decode(code,"utf-8");
//        System.out.println(encode);
//        ApiEnv.setEnv("dev");
////
//        System.out.println(System.currentTimeMillis());
//
        //test upload
//        ApiEnv.setEnv("local");
//        File file = new File("C:\\Users\\zhanglei68\\Desktop\\codemirror\\321323.py");
//        Script script = new Script();
////        script.setGitProjectId(23057L);
////        script.setGitProjectPath("bdp_pre/data_dev");
//        script.setType(3);
//        script.setName("321323.py");
////        script.setGitProjectFilePath("321323.py");
//        script.setDescription("哈哈哈啊哈啊哈");
//        script.setStartShellPath("startbinpath");
////        script.setId(7L);
//        try {
//            String appId = "saasupload.jd.care";
//            String appToken = "bfaeeb0e73da311d72d87dc37baa52d2";
//            String userToken = "URM769194c1d18a7359611307b5d166af77";
////            String appId = "datadev";
////            String appToken="a664284f9a0048ba206f3ea2629bd9fa";
////            String userToken="URMd91c37903bab9a5c87a250d7eaa0dc71";
//            Long time = new Date().getTime();
//            String timeStr = Long.toString(time);
//            String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);
//            System.out.println("appid===" + appId);
//            System.out.println("userToken===" + userToken);
//            System.out.println("time===" + timeStr);
//            System.out.println("sign===" + sign);
////            ApiResult apiResult = uploadScript(appId, userToken, time, sign, file, script);
//            System.out.println("chengong");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        test load
        ApiEnv.setEnv("test");
        Script script = new Script();
//        script.setGitProjectPath("bdp_pre/data_dev");
        script.setGitProjectId(30010L);
        script.setGitProjectFilePath("wangxiaoli76_20181025181624.zip");
        String appId = "saasupload.jd.care";
        String appToken = "bfaeeb0e73da311d72d87dc37baa52d2";
//        String userToken = "URMd91c37903bab9a5c87a250d7eaa0dc71";
//        String userToken = "3e0f84f84153456b93a361e484aa71c2";
        String userToken = "URM18088116748a9b25d9dceb72c772172b";
        Long time = new Date().getTime();
        String timeStr = Long.toString(time);
        String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);
        testUp();
//        ApiResult apiResult = loadScript(appId, userToken, time, sign, "C:\\Users\\zhanglei68\\Desktop\\codemirror\\","test.zip", script);

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


//        Date date = new Date();
////        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar c = Calendar.getInstance();
//        c.setTime(date);
//        int dayForWeek = 0;
//        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
//            dayForWeek = 7;
//        } else {
//            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
//        }
//        System.out.println(dayForWeek);


    }
    public static void testUp() throws Exception {
        ApiEnv.setEnv("test");
        Script script = new Script();
        script.setGitProjectId(30010L);
        script.setGitProjectDirPath("/zhangrui/testdir/156");
        script.setName("test_zzzzzzzzz.py");

        script.setType(ScriptTypeEnum.Python2.toCode());
        String appId = "saasupload.jd.care";
        String appToken = "bfaeeb0e73da311d72d87dc37baa52d2";
        String userToken = "URM18088116748a9b25d9dceb72c772172b";
        Long time = new Date().getTime();
        String timeStr = Long.toString(time);
        String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);

        ApiResult apiResult = uploadScript(appId, userToken, time, sign, new File("/Users/zhangrui25/test_zzzzzzzzz.py"), script);

        System.out.println(JSONObject.toJSONString(apiResult));
    }
}
