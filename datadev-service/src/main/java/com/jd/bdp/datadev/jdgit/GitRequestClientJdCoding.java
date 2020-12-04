package com.jd.bdp.datadev.jdgit;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitRequestClientJdCoding extends GitRequestClient {


    private static final Integer perPage = 80;


    private static String PRIVATE_TOKEN; //"j9e-suFgk6p7di-rgKFg";
    public static String PRIVETE_TOKEN_USER;
    private static final String REQUEST_URI_PRE = "http://coding.jd.com/api/v4/";//"http://git.jd.com/api/v4/";

    @Override
    String getPrivetToken() {
        if (PRIVATE_TOKEN == null) {
            PRIVATE_TOKEN = SpringPropertiesUtils.getPropertiesValue("${coding.private.token}");
        }
        return PRIVATE_TOKEN;
    }

    @Override
    String getRequestUriPre() {
        return REQUEST_URI_PRE;
    }

    @Override
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
            gitHttpResponse.setResponseMessage(stringResponse);
        }

        gitHttpResponse.setHeader(responseHeader);
        return gitHttpResponse;
    }

    /**
     * 查询单页数据
     * params 包含per_page,page
     * <p>
     * coding 目前的有些API不支持分页查询，直接返回所有的数据
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
        if (total > 0L || com.jd.jsf.gd.util.StringUtils.isNotBlank(responseContent)) {
            JSONArray datas = JSONArray.parseArray(responseContent);
            for (int index = 0; index < datas.size(); index++) {
                JSONObject temp = datas.getJSONObject(index);
                T instant = gitConvertToDomain.covertGitDomain(temp);
                if (instant != null) {
                    arrayLists.add(instant);
                }
            }
        }
        return new HoldDoubleValue<Integer, List<T>>(total > 0L ? total : arrayLists.size(), arrayLists);
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

}
