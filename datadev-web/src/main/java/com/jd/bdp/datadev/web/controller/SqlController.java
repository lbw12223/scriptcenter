package com.jd.bdp.datadev.web.controller;

import com.jd.bdp.common.json.WebJsonConfig;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.datadev.web.exception.NoAuthorizeException;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jim.cli.Cluster;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Controller
@RequestMapping("/scriptcenter/sql/")
public class SqlController {

    private static final Log logger = LogFactory.getLog(SqlController.class);


    @Value("${sql.token}")
    private String sqlToken ;

    @Autowired
    @Qualifier("datadev_dataSource")
    DataSource dataSource;

    @RequestMapping("index.html")
    public String index(@RequestParam(value = "token", defaultValue = "") String token , Model model) {
        if(!sqlToken.equalsIgnoreCase(token)){
            throw new NoAuthorizeException("无权限");
        }
        model.addAttribute("token",sqlToken);
        return "scriptcenter/sql/index";
    }




    /**
     * 通用查询
     *
     * @param sql sql语句
     * @return Json；
     */
    @RequestMapping("query.ajax")
    @ResponseBody
    public JSONObject query(String sql, HttpServletRequest request, UrmUserHolder cvUser ,
                            @RequestParam(value = "token", defaultValue = "") String token) {

        if(!sqlToken.equalsIgnoreCase(token)){
            throw new NoAuthorizeException("无权限");
        }

        logger.debug("开始执行通用查询，sql=[" + sql + "]......");

        if (StringUtils.isBlank(sql)) {
            return AjaxUtil.failure("SQL语句不能为空值！");
        }


        if (StringUtils.isBlank(sql)) {
            return AjaxUtil.failure("SQL语句不能为空值！");
        }

        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();
            int num = resultSet.getMetaData().getColumnCount();
            int count = 0;
            JSONArray jsonArray = new JSONArray();


            while (resultSet.next()) {

                count++;
                JSONObject jo = new JSONObject();

                for (int i = 0; i < num; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i + 1);
                    String val = resultSet.getString(columnName);

                    logger.debug("=======" + columnName + "==========" + val);

                    if (StringUtils.isBlank(val)) {
                        jo.put(columnName, "NULL");
                    } else {
                        jo.put(columnName, val);
                    }

                    logger.debug("====================>" + jo);
                }

                jsonArray.add(jo, WebJsonConfig.getInstance());

                if (count > 5000) {
                    logger.error("通用查询返回记录最大为5000！！！");
                    JSONObject jsonObject = AjaxUtil.page2Json(count, jsonArray);
                    jsonObject.put("errorMsg", "通用查询返回记录最大为5000！！！");
                    return jsonObject;
                }
            }
            return AjaxUtil.page2Json(count, jsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.toString());
            return AjaxUtil.failure(e.toString());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.toString());
            }
        }

    }

    /**
     * 通用查询更新操作
     *
     * @param sql
     * @return
     */
    @RequestMapping("update.ajax")
    @ResponseBody
    public JSONObject update(String sql, UrmUserHolder cvUser ,
                             @RequestParam(value = "token", defaultValue = "") String token) {

        logger.debug("开始执行通用查询，sql=[" + sql + "]......");
        if(!sqlToken.equalsIgnoreCase(token)){
            throw new NoAuthorizeException("无权限");
        }

        if (StringUtils.isBlank(sql)) {
            return AjaxUtil.failure("SQL语句不能为空值！");
        }

        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            int update = statement.executeUpdate(sql);

            return AjaxUtil.success("更新sql语句成功，更新了[" + update + "]条记录！");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.toString());
            return AjaxUtil.failure(e.toString());
        } finally {
            try {

                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.toString());
            }
        }

    }
}
