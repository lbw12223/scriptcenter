package com.jd.bdp.datadev.service;

import SQLinForm_200.SQLForm;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

public class SqlFormatTest {
    @Test
    public void testSql(){
        String sql = "CREATE  TABLE `tmp_dev.tmp_dev_m14_wireless_click_log`(`report_type` string COMMENT '上报类型', `report_time` string COMMENT '上报时间', `report_ts` string COMMENT '上报时间戳', `project_id` string COMMENT '项目ID', `browser_uniq_id` string COMMENT '用户唯一标识', `os_plant` string COMMENT '操作系统平台', `app_version` string COMMENT '客户端版本号', `screen_resolution` string COMMENT '分辨率', `device_type` string COMMENT '设备型号', `os_version` string COMMENT '操作系统版本', `chan_info` string COMMENT '渠道信息', `longitude` string\n" +
                "COMMENT '经度', `latitude` string COMMENT '纬度', `net_type` string COMMENT '网络类型', `user_visit_ip` string COMMENT '用户IP', `user_site_cy_name` string COMMENT '用户所在国家名称', `user_site_province_nam` string COMMENT '用户所在省份名称', `user_site_city_name` string COMMENT '用户所在城市名称', `click_time` string COMMENT '点击事件发生时间', `click_ts` string COMMENT '点击事件发生时间戳', `user_log_acct` string COMMENT '用户登录账号', `page_name` string COMMENT '当前页面类名', `page_param` string COMMENT '当前页面参数', `event_id` string COMMENT '自定义事件ID',\n" +
                "`event_func` string COMMENT '点击事件函数名称', `event_param` string COMMENT '事件参数', `next_page_name` string COMMENT '要去的页面类名', `next_page_param` string COMMENT '要去的页面参数', `pinid` string COMMENT '加密后登录帐号', `app_device` string COMMENT 'PP客户端类型', `build_version` string COMMENT '版本build号', `abtest_id` string COMMENT 'emini实验ID', `abtest_label` string COMMENT 'emini实验版本', `test_gray` string COMMENT '灰度测试id', `user_gender` string COMMENT '性别', `biz_type` string COMMENT '业务类型', `mba_muid` string COMMENT\n" +
                "'首次访问M页和Jshop', `mba_sid` string COMMENT '新访次ID', `reserved1` string COMMENT '预留字段1', `reserved2` string COMMENT '预留字段2', `reserved3` string COMMENT '预留字段3', `reserved4` string COMMENT '预留字段4', `mba_seq` string COMMENT '页PV自增序列', `pv_sid` string COMMENT 'V访问次数id', `pv_seq` string COMMENT 'V访问页面路径序号', `idfa` string COMMENT 'OS广告标示符', `imei` string COMMENT '手机IMEI', `imsi` string COMMENT 'IM卡唯一标示，用以区分中国移动/联通/电信，无sim卡时传空', `page_id` string COMMENT '页面id，由各页面写入', `sku_tag` string COMMENT\n" +
                "'商品活动属性标签', `shop_id` string COMMENT 'hopid', `sourcetype` string COMMENT '外部openAPP协议带入sourcetype', `sourcevalue` string COMMENT '外部openAPP协议带入sourcevalue', `browser` string COMMENT '浏览器类型', `borwser_ver` string COMMENT '浏览器版本号', `mobile_model` string COMMENT '手机操作系统', `user_agent` string COMMENT '用户代理User', `uid_hash` string COMMENT '用户唯一标识hash值', `device_brand` string COMMENT '手机品牌', `device_model` string COMMENT '手机型号', `ext_columns` string COMMENT '附加列数据', `sale_ord_id` string COMMENT\n" +
                "'销售订单编号', `item_sku_id` string COMMENT '商品sku编号', `jdv` string COMMENT '广告JDV', `abt` string COMMENT '广告部abt参数', `unpl` string COMMENT 'unpl', `mjds` string COMMENT 'mjds', `operator` string COMMENT '运营商', `aid` string COMMENT '', `refer_page_id` string COMMENT '', `event_param_json` string COMMENT '') PARTITIONED BY(`dt` string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' NULL DEFINED\n" +
                "AS\n" +
                "''\n" +
                "WITH\n" +
                "SERDEPROPERTIES('escape.delim' = '\n" +
                "') STORED AS INPUTFORMAT 'com.hadoop.mapred.DeprecatedLzoTextInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' LOCATION 'hdfs://ns1/user/dd_edw/tmp_dev.db/tmp_dev_m14_wireless_click_log' TBLPROPERTIES('mart_name' = 'dd_edw', 'transient_lastDdlTime' = '1522119078')"
                ;
        System.out.println(SQLUtils.formatMySql(sql));

        System.out.println(SqlFormatTest.format(sql));

    }
    public static String format(String ss){
        SQLForm form = new SQLForm();
        form.setLinebreakCase(true);
        form.setCase(false, false);
        form.setLowerCase(false);
        form.setGraphLevel(false);
        form.setSuppressSpace(true);
        form.setQuoteCharacter("'");
        form.setSuppressEmptyLine(true);
        form.setFormatLanguage("SQL");
        form.setBracketSpaces("noSpacesAroundBracket");
        form.setCommaSpaces("oneSpaceAfterComma");
        form.setEqualSpaces("oneSpaceAroundEqual");
        form.setSmallSQLWidth(80);
        form.setPageWidth(500);
        form.setAndOrIndention(true);
        form.setInitialIndentation(0);
        form.setIndention(4, true);
        form.setAlignmentEqual(false);
        form.setAlignmentComma(false);
        form.setAlignmentComment(false);
        form.setAlignmentAs(false);
        form.setAlignmentOperator(false);
        String formatSql = form.formatSQLAsString(ss);
        //特殊处理!=符号
        formatSql = formatSql.replace("! =", " != ");
//        formatSql = formatSql.replaceAll("\\s{1,},"," ,");
        return formatSql;
    }
}
