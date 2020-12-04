package com.jd.bdp.datadev.web.worker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class SimpleHttpUtils {
    public static String get(String url, Map<String, String> headers) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpClient = new DefaultHttpClient();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
        httpGet.addHeader("Cookie", "_c_k_u=\"R2vDEDkOUMWncENWaJHyYQ==\"; _c_v_p=\"ioSkfFt0/25CGZ7Lw8OBOoNpSfH33An3in8WzsIGc9Lo5IsiQ8sIBg==\"; sso.jd.com=63806365a2284028a95cf821f6c5f55e; _bdp_erp=zhanglei847; __jdv=193554344|test.bdp.jd.com|-|referral|-|1536913219071; __jdu=15369132190701510351286; zhanglei847_m=210; JSESSIONID=8FA5EED29B38CA576069DE6453D34867.s1; __jda=122992088.15369132190701510351286.1536913219.1537510935.1537514150.26; __jdc=122992088; __jdb=122992088.1.15369132190701510351286|26.1537514150");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity());
    }

}
