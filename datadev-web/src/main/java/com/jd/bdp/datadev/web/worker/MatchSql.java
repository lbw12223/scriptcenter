////package com.jd.bdp.datadev.web.worker;
//
//
//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.expression.Alias;
//import net.sf.jsqlparser.expression.ExpressionVisitor;
//import net.sf.jsqlparser.parser.CCJSqlParserManager;
//import net.sf.jsqlparser.schema.Table;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.StatementVisitor;
//import net.sf.jsqlparser.statement.create.table.CreateTable;
//import net.sf.jsqlparser.statement.delete.Delete;
//import net.sf.jsqlparser.statement.drop.Drop;
//import net.sf.jsqlparser.statement.insert.Insert;
//import net.sf.jsqlparser.statement.replace.Replace;
//import net.sf.jsqlparser.statement.select.*;
//import net.sf.jsqlparser.statement.truncate.Truncate;
//import net.sf.jsqlparser.statement.update.Update;
//import net.sf.jsqlparser.util.TablesNamesFinder;
//import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
//import net.sf.jsqlparser.util.deparser.SelectDeParser;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.test.context.jdbc.SqlConfig;
//
//
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//
///*interface TablePicker {
//
//    String pickTable(String sql, SqlConfig.ErrorMode mode, Map<String, Object> params);
//}
//
//class TablePickerImpl implements TablePicker {
//
//    private static Logger logger = LoggerFactory.getLogger(TablePickerImpl.class);
//    @Override
//    public String pickTable(String sql, SqlConfig.ErrorMode mode, Map<String, Object> params) {
//
//        CCJSqlParserManager parser = new CCJSqlParserManager();
//        StringBuilder buffer = new StringBuilder();
//        try {
//            Statement stmt = parser.parse(new StringReader(sql));
//
//            // 查询语句处理
//            if (stmt instanceof Select) {
//                Select statement = (Select) stmt;
//                logger.debug("解析sql的语句: {} ",statement.toString());
//                //Start of value modification
//                ExpressionDeParser expressionDeParser = new ExpressionDeParser();
//                SelectDeParser deparser = new MatchSql(expressionDeParser, buffer);
//                expressionDeParser.setSelectVisitor(deparser);
//                expressionDeParser.setBuffer(buffer);
//                statement.getSelectBody().accept(deparser);
//                logger.debug("替换成功，文本为: {} ",buffer.toString());
//                return buffer.toString();
//            }
//            // 插入语句处理
//            if (stmt instanceof Insert) {
//                Insert statement = (Insert) stmt;
//                logger.debug("解析sql的语句: {} ",statement.toString());
//
//                Table t = new Table();
//                t.setSchemaName("user");
//                t.setName("age");
//                statement.setTable(t);
//
//                // 获取insert语句中的查询语句，如果没有则不替换select
//                Select select = statement.getSelect();
//                if (select != null) {
//                    ExpressionDeParser expressionDeParser = new ExpressionDeParser();
//                    SelectDeParser deparser = new MatchSql(expressionDeParser, buffer);
//                    expressionDeParser.setSelectVisitor(deparser);
//                    expressionDeParser.setBuffer(buffer);
//                    select.getSelectBody().accept(deparser);
//                }
//                logger.debug("替换成功，文本为: {} ",statement.toString());
//                return statement.toString();
//            }
//        } catch (JSQLParserException e) {
//            logger.error(e.getMessage());
//        }
//        // 如果四种条件都不匹配则抛出异常。
//        throw new RuntimeException("error sql can not be parse!check your sql!");
//    }
//
//}*/
//
//
//
//public class MatchSql extends SelectDeParser {
//
//    public static List<String> getTableNameBySql(String sql) throws JSQLParserException{
//        CCJSqlParserManager parser=new CCJSqlParserManager();
//        List<String> list=new ArrayList<String>();
//        Statement stmt = parser.parse(new StringReader(sql));
//        if (stmt instanceof Select) {
//            Select selectStatement = (Select) stmt;
//            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
//            List tableList = tablesNamesFinder.getTableList(selectStatement);
//            for (Iterator iter = tableList.iterator(); iter.hasNext();) {
//                String tableName = iter.next().toString();
//                list.add(tableName);
//            }
//        }
//        return list;
//    }
//
//
//    public static void main(String[] args) throws Exception{
//        MatchSql ms = new MatchSql();
//        /*System.out.println(getTableNameBySql("select * from abc where id = 1"));
//        System.out.println(getTableNameBySql("select * from git_project a INNER JOIN git_project_member b \n" +
//                "on a.git_project_id = b.git_project_id "));
//        System.out.println(getTableNameBySql("select git_project_id from (select * from git_project where git_project_name = 'data_dev') t1"));
//        System.out.println(getTableNameBySql("select * from git_project as t1 where git_project_id = 26533;"));*/
//        System.out.println(getTableNameBySql("select\n" +
//                "\trel.*,\n" +
//                "   log.queue_time,\n" +
//                "   log.run_time,\n" +
//                "\tlog.end_time,\n" +
//                "\tlog.exec_long\n" +
//                "from\n" +
//                "\t(\n" +
//                "\t\tselect\n" +
//                "\t\t\t*\n" +
//                "\t\tfrom\n" +
//                "\t\t\tgdm.gdm_m99_buffalo_task_dep_rel\n" +
//                "\t\twhere\n" +
//                "\t\t\tdt = sysdate( - 1)\n" +
//                "\t\t\tand deleted != 1\n" +
//                "\t\t\tand disabled != 1\n" +
//                "\t\t\tand mart_name = 'mart_sz'\n" +
//                "         and queue='root.bdp_jmart_sz_union.bdp_jmart_sz_dm_service_formal'\n" +
//                "\t)\n" +
//                "\trel\n" +
//                "left outer join\n" +
//                "\t(\n" +
//                "\t\tselect\n" +
//                "\t\t\ta.task_id as task_id,\n" +
//                "\t\t\ta.queue_time as queue_time,\n" +
//                "\t\t\ta.run_time as run_time,\n" +
//                "\t\t\ta.end_time as end_time,\n" +
//                "\t\t\ta.exec_long as exec_long,\n" +
//                "\t\t\t'buffalo3.5' as task_type\n" +
//                "\t\tfrom\n" +
//                "\t\t\t(\n" +
//                "\t\t\t\tselect\n" +
//                "\t\t\t\t\ttask_id,\n" +
//                "\t\t\t\t\tqueue_time,\n" +
//                "\t\t\t\t\trun_time,\n" +
//                "\t\t\t\t\tend_time,\n" +
//                "\t\t\t\t\texec_long,\n" +
//                "\t\t\t\t\tstatus,\n" +
//                "\t\t\t\t\trow_number() over(partition by task_id order by\n" +
//                "\t\t\t\t\tend_time) as rank\n" +
//                "\t\t\t\tfrom\n" +
//                "\t\t\t\t\tfdm.fdm_dispatch_1_d_task_run_log_new_chain\n" +
//                "\t\t\t\twhere\n" +
//                "\t\t\t\t\tstart_date <= sysdate( - 1)\n" +
//                "\t\t\t\t\tand end_date > sysdate( - 1)\n" +
//                "              and status in('fail', 'wait')\n" +
//                "\t\t\t\t\t--and status in('wait')\n" +
//                "\t\t\t\t\tand\n" +
//                "\t\t\t\t\t(\n" +
//                "\t\t\t\t\t\tsubstr(run_time, 1, 10) = sysdate( - 1)\n" +
//                "\t\t\t\t\t\tor substr(end_time, 1, 10) = sysdate( - 1)\n" +
//                "\t\t\t\t\t)\n" +
//                "\t\t\t)\n" +
//                "\t\t\ta\n" +
//                "\t\twhere\n" +
//                "\t\t\ta.rank = 1\n" +
//                "\t\t\n" +
//                "\t\tunion all\n" +
//                "\t\t\n" +
//                "\t\tselect\n" +
//                "\t\t\tttt.task_def_id as task_id,\n" +
//                "\t\t\tttt.queue_time as queue_time,\n" +
//                "\t\t\tttt.run_time as run_time,\n" +
//                "\t\t\tttt.end_time as end_time,\n" +
//                "\t\t\tunix_timestamp(ttt.end_time) - unix_timestamp(ttt.run_time) as exec_long,\n" +
//                "\t\t\t'buffalo4.0' as task_type\n" +
//                "\t\tfrom\n" +
//                "\t\t\t(\n" +
//                "\t\t\t\tselect\n" +
//                "\t\t\t\t\t*,\n" +
//                "\t\t\t\t\trow_number() over(partition by task_def_id order by\n" +
//                "\t\t\t\t\tend_time) as rank\n" +
//                "\t\t\t\tfrom\n" +
//                "\t\t\t\t\tfdm.fdm_dispatch_1_b_run_log_chain\n" +
//                "\t\t\t\twhere\n" +
//                "\t\t\t\t\tstart_date <= sysdate( - 1)\n" +
//                "\t\t\t\t\tand end_date > sysdate( - 1)\n" +
//                "\t\t\t\t\tand run_status in('fail', 'success')\n" +
//                "              --and run_status in('success')\n" +
//                "\t\t\t\t\tand\n" +
//                "\t\t\t\t\t(\n" +
//                "\t\t\t\t\t\tsubstr(run_time, 1, 10) = sysdate( - 1)\n" +
//                "\t\t\t\t\t\tor substr(end_time, 1, 10) = sysdate( - 1)\n" +
//                "\t\t\t\t\t)\n" +
//                "\t\t\t\t\tand instance_type = 2\n" +
//                "\t\t\t)\n" +
//                "\t\t\tttt\n" +
//                "\t\twhere\n" +
//                "\t\t\tttt.rank = 1\n" +
//                "\t)\n" +
//                "\tlog\n" +
//                "on\n" +
//                "\trel.task_version = log.task_type\n" +
//                "\tand rel.task_id = log.task_id"));
//
//        /*System.out.println(getTableNameBySql("select case when total_score<200 then '(-∞-200)'\n" +
//                "       when total_score>=200 and total_score<5000 then '[200-5000)'\n" +
//                "       when total_score>=5000 and total_score<10000 then '[5000-10000)' \n" +
//                "       when total_score>=10000 and total_score<20000 then '[10000-20000)' \n" +
//                "       when total_score>=20000 then '[20000+)' end as \"京享值\",\n" +
//                "       count(distinct scan.pin) as \"用户数\",\n" +
//                "       count(distinct case when cnt>=2 then scan.pin end) as \"回访用户数\"\n" +
//                "from  \n" +
//                "(select pin,count(distinct dt) as cnt\n" +
//                " from dev.fyt_scan_huiyuanfuli_app where dt>=sysdate(-14) and dt<=sysdate(-1)\n" +
//                "group by pin\n" +
//                ") scan\n" +
//                "join \n" +
//                "(select lower(trim(pin)) as pin,total_score\n" +
//                " from fdm.fdm_user_level_score_1_user_level_score_chain\n" +
//                " where dp='ACTIVE' and pin not like '*yhd_%') jxz\n" +
//                "on scan.pin=jxz.pin\n" +
//                "group by case when total_score<200 then '(-∞-200)'\n" +
//                "       when total_score>=200 and total_score<5000 then '[200-5000)'\n" +
//                "       when total_score>=5000 and total_score<10000 then '[5000-10000)' \n" +
//                "       when total_score>=10000 and total_score<20000 then '[10000-20000)' \n" +
//                "       when total_score>=20000 then '[20000+)' end\t"));*/
//
//    }
//
//}
