package com.jd.bdp.datadev.web.worker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


public class DeleteDsdColumns {
    private static final Logger logger = Logger.getLogger(DeleteDsdColumns.class);
    private static final String sdsQuerySql="select si.sd_uuid from meta_sds_info  si  left join meta_table_info ti on ti.sd_uuid = si.sd_uuid  where ti.tb_uuid is null limit ";
    private static final String sdsDeleteSql="DELETE from meta_sds_info where sd_uuid in (";
    private static final String sds = "sd_uuid";
    private static final String colQuerySql="select ci.cd_uuid,ci.db_type from meta_column_info  ci  left join meta_sds_info si on si.cd_uuid = ci.cd_uuid  where si.cd_uuid is null and limit ";
    private static final String colDeleteSql="DELETE from meta_column_info where cd_uuid in (";
    private static final String col = "cd_uuid";



    public static void main(String[] args) throws Exception {

        // 创建10个任务并执行
        int queryNumber = 5000;
        int startNumber = 0 ;
        for ( int i = 0; i < 10000; i++) {
            startNumber += queryNumber * i;
            Set<String> temp =  DeleteDsdColumns.hand(startNumber, 100);
            if(temp.size() > 0){
                deleteWrap(temp) ;
            }else{
                continue;
            }
        }
    }

    public static void deleteWrap(Set<String> total) throws Exception {
        int deletePreNumber = 100;
        HashSet<String> sub = new HashSet<String>();
        for (String temp : total) {
            sub.add(temp);
            if (sub.size() >= deletePreNumber) {
                deleteSdUUid(sub);
                sub = new HashSet<String>();
            }
        }
        deleteSdUUid(sub);
    }

    /**
     * @param queryNumber
     * @throws Exception
     */
    public static Set<String> hand(Integer startNumber, Integer queryNumber) throws Exception {
        String querySql = colQuerySql + startNumber + "," + queryNumber;
        JSONObject queryData = exeQuery(querySql);
        Set<String> sdUUids = new HashSet<String>();
        if (queryData != null && queryData.getJSONArray("dataList") != null) {
            JSONArray dataList = queryData.getJSONArray("dataList");
            if (dataList != null && dataList.size() > 0) {
                for (int index = 0; index < dataList.size(); index++) {
                    String dbType = dataList.getJSONObject(index).getString("db_type");
                    if("3".equals(dbType)){
                        sdUUids.add(dataList.getJSONObject(index).getString(col));
                    }
                }
            }
            System.out.println(" sdUUids query size : " + sdUUids.size());
            return sdUUids;
        }
        return null;

    }


    private static void deleteSdUUid(final Set<String> uuids) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String temp : uuids) {
            sb.append(",'").append(temp).append("'");
        }
        String params = sb.length() > 1 ? sb.substring(1) : "";
        if (params.length() > 0) {
            final String deleteSql = colDeleteSql + params + ");";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DeleteDsdColumns.exeDelete(deleteSql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }

    public static JSONObject exeDelete(String sql) throws Exception {
        Long start = System.currentTimeMillis() / 1000;
        String url = "http://bdp.jd.com/think/sys/sql/update.ajax?sql=";
        String result = SimpleHttpUtils.get(url + java.net.URLEncoder.encode(sql),null);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Long end = System.currentTimeMillis() / 1000;
        System.out.println(" 时间" + (end - start) + "  结果 : " + jsonObject.getString("errorMessage") + "  " + sql);
        return jsonObject;
    }

    public static JSONObject exeQuery(String sql) throws Exception {
        String url = "http://bdp.jd.com/think/sys/sql/query.ajax?sql=";
        url = url + (java.net.URLEncoder.encode(sql));
        String result = SimpleHttpUtils.get(url,null);
        JSONObject jsonObject = JSONObject.parseObject(result);
        return jsonObject;
    }

}
