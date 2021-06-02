package com.jd.bdp.datadev.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevApplication;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AppGroupUtil {

    private static final Logger logger = Logger.getLogger(AppGroupUtil.class);

    public static final String BDP_SYS = "bdp_sys";


    @Value("${app.group.domain}")
    private String domain;
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    private String encrypterToken = "!@#$QWER";

    @Autowired
    private AppGroupProjectUtil appGroupProjectUtil;

    private List<String> getErpsByAppID(String appId, String token, String domain, Long id, Integer roleType) {
        List<String> list = new ArrayList<String>();
        HttpClient httpClient = new HttpClient();//初始化http请求客户端

        String data = "{appgroupId:" + id + ",erp:'mazeguo'}";
        if (roleType != 0) {
            data = "{appgroupId:" + id + ",erp:'mazeguo',roleType:" + roleType + "}";
        }
        NameValuePair[] nameValuePairs = new NameValuePair[4];
        nameValuePairs[0] = new NameValuePair("appId", appId);//鉴权appId
        nameValuePairs[1] = new NameValuePair("token", token);///鉴权token
        nameValuePairs[2] = new NameValuePair("time", String.valueOf(System.currentTimeMillis()));//请求时间 当前时间毫秒数
        nameValuePairs[3] = new NameValuePair("data", data);//请求参数
        try {
            String url = domain + "/api/jsd/appgroup/memberlist";
            PostMethod postMethod = new PostMethod(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            postMethod.addParameters(nameValuePairs);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                String res = postMethod.getResponseBodyAsString();
                JSONObject json = JSONObject.parseObject(res);
                if (json != null && json.get("list") != null) {
                    JSONArray jsonArray = json.getJSONArray("list");
                    for (int index = 0; index < jsonArray.size(); index++) {
                        String erp = jsonArray.get(index) != null ? jsonArray.get(index).toString() : "";
                        list.add(erp);
                    }
                }
            } else {
                logger.error("获取应用成员列表失败");
            }

        } catch (Exception e) {
            logger.error("获取应用列表失败,失败原因:" + e.getMessage());
        }
        return list;
    }


    public List<String> getErpsByAppID(Long id) {
        //return getErpsByAppID(appId, appToken, domain, id, 0);
        return appGroupProjectUtil.getErpsByAppId(id);
    }


    private List<DataDevApplication> getAppsByErp(String appId, String token, String domain, String erp) throws Exception {

        HttpClient httpClient = new HttpClient();//初始化http请求客户端
        String url = domain + "/api/jsd/appgroup/list";
        //请求参数数据
        String data = "{erp:'" + erp + "',type:2}";
        //将Map类型的参数转换成NameValuePair数组
        NameValuePair[] nameValuePairs = new NameValuePair[4];
        nameValuePairs[0] = new NameValuePair("appId", appId);//鉴权appId
        nameValuePairs[1] = new NameValuePair("token", token);///鉴权token
        nameValuePairs[2] = new NameValuePair("time", String.valueOf(System.currentTimeMillis()));//请求时间 当前时间毫秒数
        nameValuePairs[3] = new NameValuePair("data", data);//请求参数
        List<DataDevApplication> list = new ArrayList<DataDevApplication>();
        try {
            PostMethod postMethod = new PostMethod(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            // 将值放入postMethod中
            postMethod.setRequestBody(nameValuePairs);
            // 执行postMethod
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                String res = postMethod.getResponseBodyAsString();
                JSONObject json = JSONObject.parseObject(res);
                if (json != null && json.get("list") != null) {
                    JSONArray jsonArray = json.getJSONArray("list");
                    for (int index = 0; index < jsonArray.size(); index++) {
                        JSONObject obj = jsonArray.getJSONObject(index);
                        if (obj != null && obj.get("appgroupId") != null && obj.get("appgroupName") != null) {
                            DataDevApplication application = new DataDevApplication();
                            application.setAppgroupName(obj.get("appgroupName").toString());
                            application.setAppgroupId(Integer.parseInt(obj.get("appgroupId").toString()));
                            list.add(application);
                        }
                    }
                }
            } else {
                logger.error("HTTP请求失败！返回状态为：" + statusCode);
            }
        } catch (Exception ex) {
            logger.error("HTTP POST请求失败！", ex);
        }
        return list;
    }

    public List<DataDevApplication> getAppsByErp(String erp) throws Exception {
        // return getAppsByErp(appId, appToken, domain, erp);
         return appGroupProjectUtil.getAppsByErp(erp);
    }

    private DataDevApplication getAppInfo(String appId, String token, String domain, Long id) throws Exception {
        HttpClient httpClient = new HttpClient();//初始化http请求客户端
        String url = domain + "/api/jsd/appgroup/findbyid";
        //请求参数数据
        String data = "{appgroupId:'" + id + "',operator:'mazeguo'}";
        //将Map类型的参数转换成NameValuePair数组
        NameValuePair[] nameValuePairs = new NameValuePair[4];
        nameValuePairs[0] = new NameValuePair("appId", appId);//鉴权appId
        nameValuePairs[1] = new NameValuePair("token", token);///鉴权token
        nameValuePairs[2] = new NameValuePair("time", String.valueOf(System.currentTimeMillis()));//请求时间 当前时间毫秒数
        nameValuePairs[3] = new NameValuePair("data", data);//请求参数
        DataDevApplication application = new DataDevApplication();
        try {
            PostMethod postMethod = new PostMethod(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            // 将值放入postMethod中
            postMethod.setRequestBody(nameValuePairs);
            // 执行postMethod
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                String res = postMethod.getResponseBodyAsString();
                JSONObject json = JSONObject.parseObject(res);
                if (json != null && json.get("obj") != null) {
                    JSONObject obj = (JSONObject) json.get("obj");
                    if (obj != null) {
//                        application.setBuName(obj.get("buName").toString());
//                        application.setProdlineName(obj.get("prodlineName").toString());
//                        application.setAppgroupName(obj.get("appgroupName").toString());
                        application = JSONObject.toJavaObject(obj, DataDevApplication.class);
                    }
                }
            } else {
                logger.error("HTTP请求失败！返回状态为：" + statusCode);
            }
        } catch (Exception ex) {
            logger.error("HTTP POST请求失败！", ex);
        }
        return application;
    }

    public static void main(String[] args) throws Exception {
        HttpClient httpClient = new HttpClient();//初始化http请求客户端
        String url = "http://ugdap.jd.com/jmartadnew/think/api/queryProductionAccountPower.json";
        //请求参数数据
        //将Map类型的参数转换成NameValuePair数组
        NameValuePair[] nameValuePairs = new NameValuePair[3];
        nameValuePairs[0] = new NameValuePair("cluster", "evil");//鉴权appId
        nameValuePairs[1] = new NameValuePair("martUser", "mart_sz");///鉴权token
        nameValuePairs[2] = new NameValuePair("erp", "bjyuanz");//请求时间 当前时间毫秒数
        List<DataDevApplication> list = new ArrayList<DataDevApplication>();
        try {
            PostMethod postMethod = new PostMethod(url);
//              GetMethod postMethod = new GetMethod(url);

            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            // 将值放入postMethod中
//            postMethod.setRequestBody(nameValuePairs);
            // 执行postMethod
            int statusCode = httpClient.executeMethod(postMethod);
//            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                String res = postMethod.getResponseBodyAsString();
                JSONObject json = JSONObject.parseObject(res);

            } else {
                logger.error("HTTP请求失败！返回状态为：" + statusCode);
            }
        } catch (Exception ex) {
            logger.error("HTTP POST请求失败！", ex);
        }


    }

    public DataDevApplication getAppInfo(Long id) throws Exception {
        // return getAppInfo(appId, appToken, domain, id);
        return appGroupProjectUtil.getAppInfo(id);
    }


}
