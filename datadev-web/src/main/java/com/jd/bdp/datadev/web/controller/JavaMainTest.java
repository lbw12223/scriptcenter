package com.jd.bdp.datadev.web.controller;


import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.ArgsImportTypeEnum;
import com.jd.bdp.datadev.enums.ScriptTypeEnum;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import jnr.ffi.annotations.In;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/3/5.
 */

public class JavaMainTest {

    public static void ssss() {
        String aa = "{\"compareMode\":70,\"templateType\":0,\"runMarketCode\":\"mart_sch\",\"runType\":20,\"dbName\":\"dev\",\"engineType\":\"hive\",\"appendSqlDesc\":\"\",\"expectValue\":\"10\",\"templateId\":19,\"tableName\":\"hd_wxl_test_sc_aof_mart_risk_aof_ide_zhangrui156_11111\",\"partitionExp\":\"维表\",\"runAccountCode\":\"mart_sch\",\"appCallId\":6682,\"ruleDesc\":\"当wxl_card_id空值占比不属于区间10 - 20 出现橙色告警\",\"runInstanceId\":3343283871522226181,\"modified\":\"2020-07-08 17:53:00\",\"ruleName\":\"空值占比1111\",\"compareModeName\":\"不属于区间\",\"startTime\":\"2020-07-08 17:53:00\",\"tablePartitionRunDetailId\":6084,\"id\":12745,\"ruleId\":884,\"tablePartitionId\":69,\"creator\":\"zhangrui156\",\"level\":1,\"appName\":\"试跑\",\"templateDesc\":\"校验字段(wxl_card_id) 空值占比\",\"levelName\":\"橙色告警\",\"sqlContent\":\"SELECT COUNT(CASE WHEN wxl_card_id is null then 1 WHEN wxl_card_id = '' then 1 ELSE null END)  / COUNT(*) FROM dev.hd_wxl_test_sc_aof_mart_risk_aof_ide_zhangrui156_11111 WHERE 1=1 \",\"partitionExpVal\":\"1=1\",\"runClusterCode\":\"hope\",\"dispatchTime\":\"2020-07-08 17:52:06\",\"field\":\"wxl_card_id\",\"templateName\":\"空值占比\",\"expectValueMax\":\"20\",\"mender\":\"zhangrui156\",\"userField\":\"\",\"tableId\":41,\"userSql\":\"\",\"runQueueCode\":\"bdp_jmart_sch_union.bdp_jmart_sch_formal\",\"status\":20}";

        System.out.println(JSONObject.parseObject(aa).getString("ruleDesc"));
    }
    private static String replaceSqlParams(DataDevScriptRunDetail dataDevScriptRunDetail, String content) {
        if (dataDevScriptRunDetail.getArgsImportType().equals(ArgsImportTypeEnum.STANDARD.toCode())
                && dataDevScriptRunDetail.getType().equals(ScriptTypeEnum.SQL.toCode())
                && StringUtils.isNotBlank(dataDevScriptRunDetail.getArgs())
                && !dataDevScriptRunDetail.getArgs().equals(String.valueOf("{}"))) {
            String args = dataDevScriptRunDetail.getArgs();
            JSONObject jsonObject = JSONObject.parseObject(args);
            for (String key : jsonObject.keySet()) {
                String fixKey = key.replace("\\", "\\\\").replace("*", "\\*")
                        .replace("+", "\\+").replace("|", "\\|")
                        .replace("{", "\\{").replace("}", "\\}")
                        .replace("(", "\\(").replace(")", "\\)")
                        .replace("^", "\\^").replace("$", "\\$")
                        .replace("[", "\\[").replace("]", "\\]")
                        .replace("?", "\\?").replace(",", "\\,")
                        .replace(".", "\\.").replace("&", "\\&");
                String replaceKey = String.valueOf("\\$\\{" + fixKey + "\\}");
                content = content.replaceAll(replaceKey, fixString(jsonObject.getString(key)));

            }
        }
        return content;
    }

    private static String fixString(String value) {
        String fixValue = value.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
        return fixValue;
    }



    public static void modifyFile(String path) throws Exception{

        Collection<File> files =  FileUtils.listFiles(new File(path),new String[]{"txt"},true);
        if(files != null && files.size() > 0){
            Iterator<File> it = files.iterator();
            while(it.hasNext()){
                File temp = it.next() ;
                FileUtils.write(temp,"\ntestssss11111codingcommit-3",true);

            }
        }
    }
    public static void main(String[] args) throws Exception {

        File[] convertFiles =  new File[]{
                new File("/Users/zhangrui25/Documents/work/repository/scriptcenter/datadev-web/src/main/webapp/scriptcenter/statics/jdDataDevTab/"),
                new File("/Users/zhangrui25/Documents/work/repository/scriptcenter/datadev-web/src/main/webapp/scriptcenter/statics/pages")};

        File outPut = new File("/Users/zhangrui25/Documents/work/repository/scriptcenter/datadev-web/src/main/webapp/scriptcenter/statics/bdp/css/g_.css");
        outPut.createNewFile();


        for(File convertFile : convertFiles){
            Collection<File> files = FileUtils.listFiles(convertFile, new String[]{"css"}, true);
            for(File file : files){

                FileUtils.write(outPut,"/*********************"+file.getName()+"*********************/ \r\n" , true);


                List<String> list = FileUtils.readLines(file);
                List<String> arrays = new ArrayList<String>();

                for(String eachLine : list){
                    eachLine = eachLine.toLowerCase();
                    if(eachLine.contains("border")
                            || eachLine.contains("background")
                            || eachLine.contains("color")
                            || eachLine.contains("box-shadow")
                            || eachLine.contains("-webkit-box-shadow")
                            || eachLine.contains("-webkit-box-shadow")
                            || eachLine.contains("{")
                            || eachLine.contains("}")
                            || eachLine.contains(".")
                            || eachLine.contains("#")){
                        arrays.add(eachLine);
                    }
                }
                FileUtils.writeLines(outPut,list,true);

            }
        }



    }




    public static String readLastCommit() throws Exception {
        System.out.println(new String(GitHttpUtil.createClientByCode(1).getBytes("http://git.jd.com/zhangrui156/data_dev_dev/commits/bdp_ide_branch/中文目录/中文.py")));
        String pageContent = FileUtils.readFileToString(new File("d:/sssssssssssss.html"));
        Pattern p = Pattern.compile("data-clipboard-text=\"([\\w]*)\"");
        Matcher m = p.matcher(pageContent);
        if (m.find()) {
            return (m.group(1));
        }
        return null;
    }

    public static String formatSqlToSrc(String sql) throws Exception {

        SQLinForm_200.SQLForm form = new SQLinForm_200.SQLForm();
        //  SQLForm form = new SQLForm();
        form.setLinebreakCase(false);
        form.setLinebreakKeyword(true);
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
        form.setUserKeywords("@location@LOCATION");
        form.setLinebreakKeyword(true);
        form.setAlignmentKeyword(true);

        String formatSql = form.formatSQLAsString(sql);
        //特殊处理!=符号
        formatSql = formatSql.replace("! =", " != ");
//        formatSql = formatSql.replaceAll("\\s{1,},"," ,");
        return formatSql;
    }

    public static HoldDoubleValue<String, String> dirPath(String filePath) {
        int lastIndex = filePath.lastIndexOf("/");
        if (lastIndex != -1) {
            return new HoldDoubleValue<String, String>(filePath.substring(0, lastIndex), filePath.substring(lastIndex + 1));
        }
        return new HoldDoubleValue<String, String>("", filePath);
    }

    public static void parseSql() {
        String sql = "select * from users,fdd left join t on t.d = users.d where aaa";
        sql = "select count(*) from zeus.uuid_content_click_info_daily where content_type='10'and page_id='SuperiorAlbum_NewListDetail' and dt='2018-12-04' limit 100;";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        SQLStatement statement = parser.parseStatement();
        statement.accept(visitor);
        System.out.println(visitor.getTables());
        System.out.println(visitor.getCurrentTable());

    }

    private static void covertMarket() {

        //
        Map<Integer, Integer> ideAndJobQueueMap = new HashMap();
        ideAndJobQueueMap.put(411, 950);
        ideAndJobQueueMap.put(412, 951);
        ideAndJobQueueMap.put(413, 952);
        ideAndJobQueueMap.put(416, 953);
        ideAndJobQueueMap.put(417, 954);
        ideAndJobQueueMap.put(419, 955);
        ideAndJobQueueMap.put(463, 956);
        ideAndJobQueueMap.put(464, 957);
        ideAndJobQueueMap.put(635, 958);
        ideAndJobQueueMap.put(653, 959);
        ideAndJobQueueMap.put(654, 960);
        ideAndJobQueueMap.put(762, 961);
        ideAndJobQueueMap.put(763, 962);
        ideAndJobQueueMap.put(773, 963);
        ideAndJobQueueMap.put(774, 964);
        ideAndJobQueueMap.put(775, 965);


        Map<Integer, Integer> ideAndJobAccountMap = new HashMap<Integer, Integer>();
        ideAndJobAccountMap.put(555,2084);
        ideAndJobAccountMap.put(556,2085);
        ideAndJobAccountMap.put(557,2086);
        ideAndJobAccountMap.put(561,2087);
        ideAndJobAccountMap.put(565,2088);
        ideAndJobAccountMap.put(566,2089);
        ideAndJobAccountMap.put(573,2090);
        ideAndJobAccountMap.put(613,2091);
        ideAndJobAccountMap.put(619,2092);
        ideAndJobAccountMap.put(620,2093);
        ideAndJobAccountMap.put(679,2094);
        ideAndJobAccountMap.put(687,2095);
        ideAndJobAccountMap.put(692,2096);
        ideAndJobAccountMap.put(710,2097);
        ideAndJobAccountMap.put(723,2098);
        ideAndJobAccountMap.put(725,2099);
        ideAndJobAccountMap.put(730,2100);
        ideAndJobAccountMap.put(800,2101);
        ideAndJobAccountMap.put(870,2102);
        ideAndJobAccountMap.put(987,2103);
        ideAndJobAccountMap.put(1030,2104);
        ideAndJobAccountMap.put(1092,2105);
        ideAndJobAccountMap.put(1125,2106);
        ideAndJobAccountMap.put(1126,2107);
        ideAndJobAccountMap.put(1129,2108);
        ideAndJobAccountMap.put(1137,2109);
        ideAndJobAccountMap.put(1148,2110);
        ideAndJobAccountMap.put(1162,2111);
        ideAndJobAccountMap.put(1236,2112);
        ideAndJobAccountMap.put(1237,2113);
        ideAndJobAccountMap.put(1327,2114);
        ideAndJobAccountMap.put(1915,2115);





        //update account ;
        String updateQueueIdSql = "update script_config set queue_id=%s where market_id=179  and queue_id=%s;";
        for (Integer queueId : ideAndJobQueueMap.keySet()) {
            Integer newQueueId = ideAndJobQueueMap.get(queueId);
            System.out.println(String.format(updateQueueIdSql,newQueueId,queueId));

        }

        String updateAccountSql = "update script_config set account_id =%s where market_id=179  and account_id=%s;";
        for (Integer accountId : ideAndJobAccountMap.keySet()) {
            Integer newAccountId = ideAndJobAccountMap.get(accountId);
            System.out.println(String.format(updateAccountSql,newAccountId,accountId));
        }



        String updateMarketSql = "update script_config set market_id =292 , cluster_code = '10k' , run_cluster_code = '10k' where market_id=179 ;";


        String updateRunHistory = "update script_config set market_id =292 , cluster_code = '10k' , run_cluster_code = '10k' where market_id=179 ;";

        System.out.println(updateMarketSql);

    }

}
