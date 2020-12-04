package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptTemplateDao {


    /**
     * 获取系统模板跟自己创建的模板
     * 根据名称和描述搜索，根据scriptType过滤
     *
     */
    List<DataDevScriptTemplate> searchOwnedScriptTemplate(@Param("scriptType")Integer scriptType, @Param("pythonType") Integer pythonType,
                                                     @Param("key")String key,@Param("erp")String erp);
    /**
     * 获取别人分享给自己的模板
     * 根据名称和描述搜索，根据scriptType过滤
     *
     */
    List<DataDevScriptTemplate> searchSharedGitScriptTemplate(@Param("scriptType")Integer scriptType, @Param("pythonType") Integer pythonType,
                                                          @Param("key")String key,@Param("erp")String erp,
                                                              @Param("users")List<String> users);
    /**
     * 获取别人分享给自己的模板
     * 根据名称和描述搜索，根据scriptType过滤
     *
     */
    List<DataDevScriptTemplate> searchSharedErpScriptTemplate(@Param("scriptType")Integer scriptType, @Param("pythonType") Integer pythonType,
                                                          @Param("key")String key,@Param("erp")String erp,
                                                              @Param("users")List<String> users);

    /**
     * 查询单条信息
     */
    DataDevScriptTemplate getScriptTemplateById(@Param("id")Long id);
    /**
     * 根据脚本id查询模板
     */
    DataDevScriptTemplate getTemplateByScriptId(@Param("scriptFileId")Long scriptFileId);



    void insertScriptTemplate(DataDevScriptTemplate dataDevScriptTemplate);


    /**
     * 获取系统模板
     * @return
     */

    List<DataDevScriptTemplate> getAllSystemTemplate();


    void updateScriptTemplate(@Param("id")Long id,@Param("template") DataDevScriptTemplate template);




    DataDevScriptTemplate getTemplateByName(@Param("templateId") Long templateId , @Param("templateName") String templateName);



}
