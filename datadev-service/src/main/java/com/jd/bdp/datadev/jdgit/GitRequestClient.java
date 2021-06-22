package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GitRequestClient {

    private static final Integer perPage = 80;


    abstract String getPrivetToken();

    abstract String getRequestUriPre();


    private HttpClient getClient() {
        HttpClient client = new DefaultHttpClient();// 获取HttpClient对象
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 6000);//连接时间
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3 * 60 * 1000);//数据传输时间
        return client;
    }

    public GitHttpResponse doPost(String url, JSONObject params) throws Exception {
        url = getRequestUriPre() + url;
        StringEntity stringEntity = new StringEntity(params.toJSONString(), "application/json", "utf-8");
        HttpPost postMethod = new HttpPost(url);
        postMethod.setHeader("PRIVATE-TOKEN", getPrivetToken());
        postMethod.setHeader("Content-Type", "application/json");
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        postMethod.setEntity(stringEntity);
        return exctueRequest(postMethod);
    }

    /**
     * http删除操作
     *
     * @param url
     * @return
     */
    public GitHttpResponse doDelete(String url) throws Exception {
        GitHttpResponse gitHttpResponse = null;
        url = getRequestUriPre() + url;
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("PRIVATE-TOKEN", getPrivetToken());
        httpDelete.setHeader("Content-Type", "application/json");
        httpDelete.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        gitHttpResponse = exctueRequest(httpDelete);

        return gitHttpResponse;
    }

    public GitHttpResponse doGet(String url, Map<String, String> params) throws Exception {
        StringBuilder paramstr = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                paramstr.append("&").append(key).append("=").append(params.get(key));
            }
        }
        url = getRequestUriPre() + url + (paramstr.length() > 0 ? "?" + paramstr.substring(1) : "");

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
        httpGet.setHeader("content-type","text/json;charset=utf-8");
        return exctueRequest(httpGet);
    }

    /**
     * @param url
     * @return
     * @throws Exception
     */
    public InputStream getStream(String url) throws Exception {
        url = getRequestUriPre() + url;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
        HttpResponse response = null;
        try {
            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096 * 10];
                int len;
                while ((len = is.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
            throw new RuntimeException(response.getStatusLine().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseConnection(httpGet);//释放连接
        }
    }

    /**
     * 查询单页数据
     * params 包含per_page,page
     *
     * @param url
     * @param params
     * @param gitConvertToDomain
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> HoldDoubleValue<Integer, List<T>> getOnePage(String url, Map<String, String> params, JSONObjectCovertToGitDomain<T> gitConvertToDomain) throws Exception {
        GitHttpResponse gitHttpResponse = doGet(url, params);
        if (gitHttpResponse.getResponseCode().equals(403)) {
            throw new RuntimeException("无权限 !");
        }
        Map<String, String> header = gitHttpResponse.getHeader();
        Integer total = header.containsKey("X-Total") ? Integer.parseInt(header.get("X-Total")) : 0;
        String responseContent = gitHttpResponse.getResponseMessage();
        List<T> arrayLists = new ArrayList<T>();
        if (total > 0L && com.jd.jsf.gd.util.StringUtils.isNotBlank(responseContent)) {
            JSONArray datas = JSONArray.parseArray(responseContent);
            for (int index = 0; index < datas.size(); index++) {
                JSONObject temp = datas.getJSONObject(index);
                T instant = gitConvertToDomain.covertGitDomain(temp);
                if (instant != null) {
                    arrayLists.add(instant);
                }
            }
        }
        return new HoldDoubleValue<Integer, List<T>>(total, arrayLists);
    }

    /**
     * 通过分页查询所有的数据
     *
     * @param gitConvertToDomain
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> pageAll(String url, Map<String, String> params, JSONObjectCovertToGitDomain<T> gitConvertToDomain) throws Exception {
        List<T> result = new ArrayList<T>();
        Integer page = 1;
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(perPage));
        HoldDoubleValue<Integer, List<T>> queryResult = getOnePage(url, params, gitConvertToDomain);
        if (queryResult.b != null && queryResult.b.size() > 0) {
            result.addAll(queryResult.b);
        }
        if (queryResult.b != null && queryResult.a > queryResult.b.size()) {
            page = 2;
            int loopTimes = queryResult.a / perPage == 0 ? queryResult.a / perPage : queryResult.a / perPage + 1;
            for (; page <= loopTimes; page++) {
                params.put("page", String.valueOf(page));
                HoldDoubleValue<Integer, List<T>> loopQuery = getOnePage(url, params, gitConvertToDomain);
                result.addAll(loopQuery.b);
            }
        }
        return result;
    }

    public byte[] getBytes(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
        HttpResponse response = null;
        try {
            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
            int statuscode = response.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_OK)) {
                HttpEntity entity = response.getEntity();// 响应实体/内容
                InputStream is = entity.getContent();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096 * 10];
                int len;
                while ((len = is.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            }
            throw new RuntimeException(response.getStatusLine().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseConnection(httpGet);//释放连接
        }
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */

    private GitHttpResponse exctueRequest(HttpRequestBase request) throws Exception {
        GitHttpResponse gitHttpResponse = null;
        HttpResponse response = null;
        try {
            response = getClient().execute(request);//执行请求，获取HttpResponse对象

            gitHttpResponse = covertResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            releaseConnection(request);//释放连接
        }
        return gitHttpResponse;
    }

    /**
     * @param response
     * @return
     * @throws Exception
     */
    protected GitHttpResponse covertResponse(HttpResponse response) throws Exception {
        GitHttpResponse gitHttpResponse = new GitHttpResponse();
        Header[] headers = response.getAllHeaders();
        Map<String, String> responseHeader = new HashMap<String, String>();
        if (headers != null && headers.length > 0) {
            for (Header temp : headers) {
                responseHeader.put(temp.getName(), temp.getValue());
            }
        }

        if (response.getEntity() != null) {
            String stringResponse = (EntityUtils.toString(response.getEntity()));
            gitHttpResponse.setResponseCode(response.getStatusLine().getStatusCode());
            stringResponse = new String(stringResponse.getBytes("ISO-8859-1"), "UTF-8");
            gitHttpResponse.setResponseMessage(stringResponse);
        }

        gitHttpResponse.setHeader(responseHeader);
        return gitHttpResponse;
    }

    /**
     * @param request
     */
    private void releaseConnection(HttpRequestBase request) {
        if (request != null) {
            request.releaseConnection();
        }
    }


}
