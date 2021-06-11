package com.jd.bdp.datadev.util;

import com.squareup.okhttp.*;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class HttpUtil {

    private static Logger log = Logger.getLogger(HttpUtil.class);

    private static final String encoding = "UTF-8";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String fileSeparator = "_";

    private static HttpClient getClient() {
        HttpClient client = new DefaultHttpClient();// 获取HttpClient对象
        return client;
    }

    public static String doPost(String uri, Map<String, String> params) throws Exception {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            formparams.add(new BasicNameValuePair(key, params.get(key)));
        }
        UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, Charset.forName(encoding));
        HttpPost postMethod = new HttpPost(uri);
        postMethod.setEntity(uefEntity);
        return exctueRequest(postMethod);
    }

    public static String doGet(String uri, Map<String, String> params) {
        String param = "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            param += String.format("%s=%s&", entry.getKey(), entry.getValue());
        }
        param = param.substring(0, param.length() - 1);
        param = param.replace("{", "%7B").replace("}", "%7D").replace("\"", "%22");
        uri += param;
        HttpGet getMethod = new HttpGet(uri);
        return exctueRequest(getMethod);
    }

    public static String doPostWithParamAndBody(String uri, Map<String, String> params, com.alibaba.fastjson.JSONObject body) throws Exception {
        // 将param拼接到uri
        if (params != null && params.size() > 0) {
            String param = "?";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                param += String.format("%s=%s&", entry.getKey(), entry.getValue());
            }
            param = param.substring(0, param.length() - 1);
            uri += param;
        }

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, body.toJSONString());
        Request request = new Request.Builder()
                .url(uri)
                .method("POST", requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 上传文件
     *
     * @param
     * @param inputStream
     * @param params
     * @return
     */
    public static String postFiles(String url, InputStream inputStream, Map<String, Object> params) throws Exception {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(encoding));// 设置请求的编码格式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
        ContentType contentType=ContentType.create("text/plain",Charset.forName("utf-8"));
        builder.addBinaryBody("file", inputStream, ContentType.MULTIPART_FORM_DATA,"");
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key) != null) {
                    builder.addTextBody(key, params.get(key).toString(),contentType);
                }
            }
            HttpPost postMethod = new HttpPost(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");

            postMethod.setEntity(builder.build());
            return exctueRequest(postMethod);
        }
        return null;
    }


    public static InputStream getStream(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                Header lengthHeader = response.getFirstHeader("Content-Length");
                String length = lengthHeader.getValue();
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(StringUtils.isNotBlank(length)?Integer.parseInt(length):1024);
                byte[] buffer = new byte[1024*500];
                int len;
                while ((len = is.read(buffer)) > -1 ) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            } else if (statuscode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                StringBuffer out = new StringBuffer();
                byte[] b = new byte[4096];
                for (int n; (n = is.read(b)) != -1; ) {
                    out.append(new String(b, 0, n,"utf-8"));
                }
                throw new RuntimeException(out.toString());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            releaseConnection(httpGet);//释放连接
        }
    }

    public static File getFile(String url, String dirPath, String fileName) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                Header contentHeader = response.getFirstHeader("Content-Disposition");
                if (StringUtils.isBlank(fileName)) {
                    if (contentHeader != null) {
                        HeaderElement[] values = contentHeader.getElements();
                        if (values.length == 1) {
                            NameValuePair param = values[0].getParameterByName("filename");
                            if (param != null) {
                                try {
                                    fileName = new String(param.getValue().toString().getBytes(), "utf-8");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (StringUtils.isBlank(fileName)) {
                        fileName = simpleDateFormat.format(new Date()) + fileSeparator + "noname.sql";
                    }
                }

                File file = new File(dirPath, fileName);
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                byte[] buffer = new byte[1024*500];
                int r = 0;
                while ((r = is.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, r);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                EntityUtils.consume(entity);
                return file;
            } else if (statuscode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                StringBuffer out = new StringBuffer();
                byte[] b = new byte[4096];
                for (int n; (n = is.read(b)) != -1; ) {
                    out.append(new String(b, 0, n,"utf-8"));
                }
                throw new RuntimeException(out.toString());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            releaseConnection(httpGet);//释放连接
        }
    }

    public static String exctueRequest(HttpRequestBase request) {
        String stringResponse = "";
        HttpResponse response = null;
        try {
            response = getClient().execute(request);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                stringResponse = (EntityUtils.toString(entity));
            }
            return stringResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            releaseConnection(request);//释放连接
        }
    }

    private static void releaseConnection(HttpRequestBase request) {
        if (request != null) {
            request.releaseConnection();
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> parmas = new HashMap<String, String>();
        parmas.put("userToken", "URM5779191de6f2a268f638997c9abc30da");
//        parmas.put("password", "111111");
//        parmas.put("dirId", "dirName");

          String res=HttpUtil.doPost("http://smp.bdp.jd.com/api/user/verifyUserToken",parmas);
          System.out.println(res);
          JSONObject jsonObject=JSONObject.fromObject(res);
    }
}
