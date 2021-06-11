package com.jd.bdp.datadev.jdgit;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.SpringContextUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import com.jd.jim.cli.Cluster;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class GitHttpUtil {

    private static Logger log = Logger.getLogger(GitHttpUtil.class);

    public static final Long _9YI = 900000000L;
    public static final Long _10YI = 1000000000L;

    private static final String encoding = "UTF-8";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private static String PRIVATE_TOKEN = null; //"j9e-suFgk6p7di-rgKFg";
    private static String PRIVETE_TOKEN_USER = null;//"zhangrui156";
    private static final String REQUEST_URI_PRE = "http://git.jd.com/api/v4/";

    private static final Integer perPage = 80;

    private static final String MEMBER_PROCESS = "data_dev_project_%s_member_syn_num";
    private static final String MEMBER_TOTAL = "data_dev_project_%s_member_total";
    private static Cluster jimClient = null;


    public static String getPrivetToken() {
        if (PRIVATE_TOKEN == null) {
            PRIVATE_TOKEN = SpringPropertiesUtils.getPropertiesValue("${git.private.token}");
        }
        return PRIVATE_TOKEN;
    }

    public static String getPrivetUser() {
        if (PRIVETE_TOKEN_USER == null) {
            PRIVETE_TOKEN_USER = SpringPropertiesUtils.getPropertiesValue("${git.private.user}");
        }
        return PRIVETE_TOKEN_USER;
    }

    public static GitRequestClient createClient(Long gitProjectId) {
        if (gitProjectId > _9YI) {
            return new GitRequestClientJdCoding();
        }
        return new GitRequestClientJdGit();
    }

    public static boolean isCoding(Long gitProjectId){
        return gitProjectId > _9YI ;
    }
    public static boolean isCodingOrGit(Long gitProjectId){
        return gitProjectId < _10YI ;
    }

    public static GitRequestClient createClientByCode(Integer gitOrCodingCode) {
        if (gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
            return new GitRequestClientJdCoding();
        }
        return new GitRequestClientJdGit();
    }

    public static Long getRealProjectId(Long gitProjectId) {
        if (gitProjectId > _9YI) {
            return gitProjectId - _9YI;
        }
        return gitProjectId;
    }
    public static Long getRealGroupId(Long groupId) {
        if (groupId > _9YI) {
            return groupId - _9YI;
        }
        return groupId;
    }

//
//    /*    public static Integer getTotalMembers(Long gitProjectId){
//            Integer total =0;
//            try {
//                Cluster cluster=getJimClient();
//                total = Integer.valueOf(cluster.get(String.format(MEMBER_TOTAL,gitProjectId)));
//            }catch (Exception e){
//                total = 0;
//                throw new RuntimeException(e.getMessage());
//            }
//            return total;
//        }
//        public static Integer getProcessMembers(Long gitProjectId){
//            Integer procee =0;
//            try {
//                Cluster cluster=getJimClient();
//                procee = Integer.valueOf(cluster.get(String.format(MEMBER_PROCESS,gitProjectId)));
//            }catch (Exception e){
//                procee = 0;
//                throw new RuntimeException(e.getMessage());
//            }
//            return procee;
//        }*/
//    private static HttpClient getClient() {
//        HttpClient client = new DefaultHttpClient();// 获取HttpClient对象
//        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 6000);//连接时间
//        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3 * 60 * 1000);//数据传输时间
//        return client;
//    }
//
//
//
///*    public static GitHttpResponse putFile(String url, Map<String, Object> params) throws Exception {
//        url = REQUEST_URI_PRE + url;
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.setCharset(Charset.forName(encoding));// 设置请求的编码格式
//        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
//        ContentType contentType = ContentType.create("text/plain", Charset.forName("utf-8"));
//
//        if (params != null) {
//            for (String key : params.keySet()) {
//                if (params.get(key) != null) {
//                    builder.addTextBody(key, params.get(key).toString(), contentType);
//                }
//            }
//            HttpPut postMethod = new HttpPut(url);
//            postMethod.setHeader("PRIVATE-TOKEN", getPrivetToken());
//            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
//            postMethod.setEntity(builder.build());
//            return exctueRequest(postMethod);
//        }
//        return null;
//    }*/
//
//    /**
//     * 上传文件
//     *
//     * @param
//     * @param params
//     * @return
//     */
//    public static GitHttpResponse doPost(String url, JSONObject params) throws Exception {
//        url = REQUEST_URI_PRE + url;
//        StringEntity stringEntity = new StringEntity(params.toJSONString(), "application/json", "utf-8");
//        HttpPost postMethod = new HttpPost(url);
//        postMethod.setHeader("PRIVATE-TOKEN", getPrivetToken());
//        postMethod.setHeader("Content-Type", "application/json");
//        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
//        postMethod.setEntity(stringEntity);
//        return exctueRequest(postMethod);
//    }
//
//    /**
//     * http删除操作
//     *
//     * @param url
//     * @return
//     */
//    public static GitHttpResponse doDelete(String url) throws Exception {
//        GitHttpResponse gitHttpResponse = null;
//        url = REQUEST_URI_PRE + url;
//        HttpDelete httpDelete = new HttpDelete(url);
//        httpDelete.setHeader("PRIVATE-TOKEN", getPrivetToken());
//        httpDelete.setHeader("Content-Type", "application/json");
//        httpDelete.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
//        gitHttpResponse = exctueRequest(httpDelete);
//
//        return gitHttpResponse;
//    }
//
//
//    /**
//     * 查询单页数据
//     * params 包含per_page,page
//     *
//     * @param url
//     * @param params
//     * @param gitConvertToDomain
//     * @param <T>
//     * @return
//     * @throws Exception
//     */
//    public static <T> HoldDoubleValue<Integer, List<T>> getOnePage(String url, Map<String, String> params, JSONObjectCovertToGitDomain<T> gitConvertToDomain) throws Exception {
//        GitHttpResponse gitHttpResponse = doGet(url, params);
//        if (gitHttpResponse.getResponseCode().equals(403)) {
//            throw new RuntimeException("无权限 !");
//        }
//        Map<String, String> header = gitHttpResponse.getHeader();
//        Integer total = Integer.parseInt(header.get("X-Total"));
//        String responseContent = gitHttpResponse.getResponseMessage();
//        List<T> arrayLists = new ArrayList<T>();
//        if (com.jd.jsf.gd.util.StringUtils.isNotBlank(responseContent)) {
//            JSONArray datas = JSONArray.parseArray(responseContent);
//            for (int index = 0; index < datas.size(); index++) {
//                JSONObject temp = datas.getJSONObject(index);
//                T instant = gitConvertToDomain.covertGitDomain(temp);
//                if (instant != null) {
//                    arrayLists.add(instant);
//                }
//            }
//        }
//        if (gitConvertToDomain instanceof JDGitMembers) {
//            JDGitMembers jdGitMembers = (JDGitMembers) gitConvertToDomain;
//            Long gitProjectId = jdGitMembers.getGitProjectId();
//            Cluster jimClient = getJimClient();
//            Integer page = Integer.valueOf(params.get("page"));
//            Integer process = (page - 1) * perPage + arrayLists.size();
//            String totalKey = String.format(MEMBER_TOTAL, gitProjectId);
//            String processKey = String.format(MEMBER_PROCESS, gitProjectId);
//            jimClient.set(processKey, process.toString());
//            jimClient.set(totalKey, total.toString());
//        }
//        return new HoldDoubleValue<Integer, List<T>>(total, arrayLists);
//    }
//
//
//    public static GitHttpResponse doGet(String url, Map<String, String> params) throws Exception {
//        StringBuilder paramstr = new StringBuilder();
//        if (params != null && params.size() > 0) {
//            for (String key : params.keySet()) {
//                paramstr.append("&").append(key).append("=").append(params.get(key));
//            }
//        }
//        url = REQUEST_URI_PRE + url + (paramstr.length() > 0 ? "?" + paramstr.substring(1) : "");
//
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
//        return exctueRequest(httpGet);
//    }
//
//
//    /**
//     * 通过分页查询所有的数据
//     *
//     * @param gitConvertToDomain
//     * @param <T>
//     * @return
//     * @throws Exception
//     */
//    public static <T> List<T> pageAll(String url, Map<String, String> params, JSONObjectCovertToGitDomain<T> gitConvertToDomain) throws Exception {
//        List<T> result = new ArrayList<T>();
//        Integer page = 1;
//        params.put("page", String.valueOf(page));
//        params.put("per_page", String.valueOf(perPage));
//        HoldDoubleValue<Integer, List<T>> queryResult = getOnePage(url, params, gitConvertToDomain);
//        if (queryResult.b != null && queryResult.b.size() > 0) {
//            result.addAll(queryResult.b);
//        }
//        if (queryResult.b != null && queryResult.a > queryResult.b.size()) {
//            page = 2;
//            int loopTimes = queryResult.a / perPage == 0 ? queryResult.a / perPage : queryResult.a / perPage + 1;
//            for (; page <= loopTimes; page++) {
//                params.put("page", String.valueOf(page));
//                HoldDoubleValue<Integer, List<T>> loopQuery = getOnePage(url, params, gitConvertToDomain);
//                result.addAll(loopQuery.b);
//            }
//        }
//        return result;
//    }
//
//
//    private static GitHttpResponse covertResponse(HttpResponse response) throws Exception {
//        GitHttpResponse gitHttpResponse = new GitHttpResponse();
//        Header[] headers = response.getAllHeaders();
//        Map<String, String> responseHeader = new HashMap<String, String>();
//        if (headers != null && headers.length > 0) {
//            for (Header temp : headers) {
//                responseHeader.put(temp.getName(), temp.getValue());
//            }
//        }
//
//        if (response.getEntity() != null) {
//            String stringResponse = (EntityUtils.toString(response.getEntity()));
//            gitHttpResponse.setResponseCode(response.getStatusLine().getStatusCode());
//            stringResponse = new String(stringResponse.getBytes("ISO-8859-1"), "UTF-8");
//            gitHttpResponse.setResponseMessage(stringResponse);
//        }
//
//        gitHttpResponse.setHeader(responseHeader);
//        return gitHttpResponse;
//    }
//
//    public static InputStream getStream(String url) throws Exception {
//        url = REQUEST_URI_PRE + url;
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
//        HttpResponse response = null;
//        try {
//            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
//            int statuscode = response.getStatusLine().getStatusCode();
//            if ((statuscode == HttpStatus.SC_OK)) {
//                HttpEntity entity = response.getEntity();// 响应实体/内容
//                InputStream is = entity.getContent();
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                byte[] buffer = new byte[4096 * 10];
//                int len;
//                while ((len = is.read(buffer)) > -1) {
//                    byteArrayOutputStream.write(buffer, 0, len);
//                }
//                byteArrayOutputStream.flush();
//                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//            }
//            throw new RuntimeException(response.getStatusLine().toString());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            releaseConnection(httpGet);//释放连接
//        }
//    }
//
//    public static byte[] getBytes(String url) throws Exception {
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("PRIVATE-TOKEN", getPrivetToken());
//        HttpResponse response = null;
//        try {
//            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
//            int statuscode = response.getStatusLine().getStatusCode();
//            if ((statuscode == HttpStatus.SC_OK)) {
//                HttpEntity entity = response.getEntity();// 响应实体/内容
//                InputStream is = entity.getContent();
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                byte[] buffer = new byte[4096 * 10];
//                int len;
//                while ((len = is.read(buffer)) > -1) {
//                    byteArrayOutputStream.write(buffer, 0, len);
//                }
//                byteArrayOutputStream.flush();
//                return byteArrayOutputStream.toByteArray();
//            }
//            throw new RuntimeException(response.getStatusLine().toString());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            releaseConnection(httpGet);//释放连接
//        }
//    }
//
///*    public static File getFile(String url, String dirPath, String fileName) throws Exception {
//        HttpGet httpGet = new HttpGet(url);
//        HttpResponse response = null;
//        try {
//            response = getClient().execute(httpGet);//执行请求，获取HttpResponse对象
//            int statuscode = response.getStatusLine().getStatusCode();
//            if ((statuscode == HttpStatus.SC_OK)) {
//                HttpEntity entity = response.getEntity();// 响应实体/内容
//                InputStream is = entity.getContent();
//                ByteArrayOutputStream output = new ByteArrayOutputStream();
//                byte[] buffer = new byte[4096];
//                int r = 0;
//                while ((r = is.read(buffer)) > 0) {
//                    output.write(buffer, 0, r);
//                }
//                Header contentHeader = response.getFirstHeader("Content-Disposition");
//                if (StringUtils.isBlank(fileName)) {
//                    if (contentHeader != null) {
//                        HeaderElement[] values = contentHeader.getElements();
//                        if (values.length == 1) {
//                            NameValuePair param = values[0].getParameterByName("filename");
//                            if (param != null) {
//                                try {
//                                    fileName = new String(param.getValue().toString().getBytes(), "utf-8");
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                }
//
//                File file = new File(dirPath, fileName);
//                File parent = file.getParentFile();
//                if (!parent.exists()) {
//                    parent.mkdirs();
//                }
//                FileOutputStream fos = new FileOutputStream(file);
//                output.writeTo(fos);
//                output.flush();
//                output.close();
//                fos.close();
//                EntityUtils.consume(entity);
//                return file;
//            } else if (statuscode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
//                HttpEntity entity = response.getEntity();// 响应实体/内容
//                InputStream is = entity.getContent();
//                StringBuffer out = new StringBuffer();
//                byte[] b = new byte[4096];
//                for (int n; (n = is.read(b)) != -1; ) {
//                    out.append(new String(b, 0, n));
//                }
//                try {
//                    JSONObject jsonObject = JSONObject.parseObject(out.toString());
//                    throw new RuntimeException(out.toString());
//                } catch (Exception e) {
//                    throw new RuntimeException("下载失败");
//                }
//            }
//            return null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        } finally {
//            releaseConnection(httpGet);//释放连接
//        }
//    }*/
//
//    private static GitHttpResponse exctueRequest(HttpRequestBase request) throws Exception {
//        GitHttpResponse gitHttpResponse = null;
//        HttpResponse response = null;
//        try {
//            response = getClient().execute(request);//执行请求，获取HttpResponse对象
//
//            gitHttpResponse = covertResponse(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        } finally {
//            releaseConnection(request);//释放连接
//        }
//        return gitHttpResponse;
//    }
//
//    private static void releaseConnection(HttpRequestBase request) {
//        if (request != null) {
//            request.releaseConnection();
//        }
//    }
//
//    public static Cluster getJimClient() {
//        if (GitHttpUtil.jimClient == null) {
//            Cluster jimClient = SpringContextUtil.getBean(Cluster.class);
//            GitHttpUtil.jimClient = jimClient;
//        }
//        return jimClient;
//    }

}
