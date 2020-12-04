package com.jd.bdp.datadev.component;

import com.jd.bdp.common.enums.SystemCodeEnum;
import com.jd.bdp.common.utils.HttpRequestDTO;
import com.jd.bdp.common.utils.HttpResultDto;
import com.jd.bdp.common.utils.MD5Util;
import com.jd.bdp.datadev.util.HttpUtil;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/6/21.
 */
public class HttpClientUtil extends com.jd.bdp.common.utils.HttpClientUtil {

    private static Log logger = LogFactory.getLog(HttpClientUtil.class);


    public static byte[] getStream(String url, HttpRequestDTO requestDTO, String erp) throws Exception {
        Map<String, String> params = getFixContent(requestDTO, erp);
        if (params != null && params.size() > 0) {
            String paramStr = "";
            for (String key : params.keySet()) {
                paramStr += "&" + key + "=" + URLEncoder.encode(params.get(key).toString());
            }
            if (paramStr.length() > 0) {
                url += "?" + paramStr.substring(1);
            }
        }
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        HttpClient client = new DefaultHttpClient();// 获取HttpClient对象
        try {
            response = client.execute(httpGet);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }

    public static JSONObject doGet(String url, HttpRequestDTO requestDTO, String erp) throws Exception {
        Map param = getFixContent(requestDTO, erp);
        ArrayList valuePairs = new ArrayList(param.size());
        Iterator formEntity = param.entrySet().iterator();
        while (formEntity.hasNext()) {
            Map.Entry result = (Map.Entry) formEntity.next();
            BasicNameValuePair resultDto = new BasicNameValuePair((String) result.getKey(), String.valueOf(result.getValue()));
            valuePairs.add(resultDto);
        }

        UrlEncodedFormEntity formEntity1 = new UrlEncodedFormEntity(valuePairs, "UTF-8");
        String result1 = sendPostRequest(url, formEntity1);
        JSONObject resultDto1 = new JSONObject();
        if (result1 != null && !result1.equals("")) {
            JSONObject json = JSONObject.fromObject(result1);
            return json;
        } else {
            logger.error("Http返回结果为空！");
            resultDto1.put("code", SystemCodeEnum.SystemError.toCode());
            resultDto1.put("message", "Http返回结果为空!");
            return resultDto1;
        }
    }


    public static Map<String, String> getFixContent(HttpRequestDTO requestDTO, String erp) throws Exception {
        HashMap param = new HashMap();
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setJsonPropertyFilter(new PropertyFilter() {
            public boolean apply(Object source, String name, Object value) {
                return value == null;
            }
        });
        JSONObject content = JSONObject.fromObject(requestDTO.getData(), jsonConfig);
        String userToken = SpringContextUtil.getBean(UrmUtil.class).UserTokenByErp(null, erp);
        param.put("data", content.toString());
        param.put("appId", requestDTO.getAppId());
        param.put("userToken", userToken);
        param.put("time", String.valueOf(requestDTO.getTime()));
        param.put("sign", MD5Util.getMD5Str(requestDTO.getToken() + userToken + requestDTO.getTime()));
        return param;
    }
}
