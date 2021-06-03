package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptTemplate;
import com.jd.bdp.datadev.domain.DataDevScriptTemplateShare;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptTemplateService {

    /**
     * 搜索接口，根据名称和描述搜索，根据scriptType过滤
     * @param pythonType
     * {@link com.jd.bdp.datadev.domain.DataDevScriptTemplate#pythonType}
     */
    List<DataDevScriptTemplate> searchScriptTemplate(Integer scriptType,Integer pythonType,String key,String erp)throws Exception;



    /**
     * 查询单条信息
     */
    DataDevScriptTemplate getScriptTemplateById(Long id);


    DataDevScriptTemplate insertScriptTemplate(String name,Integer scriptType,Integer pythonType,Integer templateType,String erp,String desc,String args,Long showOrder,Long scriptFileId,Integer status) ;

    void initScriptTemplate()throws Exception;


    DataDevScriptTemplate getTemplateByFileId(Long scriptFileId)throws Exception;


    /**
     * 获取系统项目id
     * @return
     * @throws Exception
     */
    Long getTemPlateProjectId()throws Exception;


    void insertTemplateByScript(DataDevScriptFile file)throws Exception;


    List<DataDevScriptTemplateShare> getSharesInfos( Long templateId)throws Exception;

    /**
     * 是否存在模板名
     * @param templateName
     * @return
     * @throws Exception
     */
    boolean existTemplateName(Long templateId,String templateName)throws Exception;


    DataDevScriptTemplate saveScriptTemplate(DataDevScriptTemplate template,String erp)throws Exception;


    /**
     * 删除模板 置deleted字段为1
     * @param templateId
     * @throws Exception
     */
    public DataDevScriptTemplate deleteTemplate(String erp,Long templateId)throws Exception;


    /**
     * 置顶
     * @param erp
     * @param templateId
     * @param toTop 是否置顶  true置顶  false取消置顶
     * @return
     * @throws Exception
     */
    public DataDevScriptTemplate topTemplate(String erp,Long templateId,boolean toTop)throws Exception;

    /**
     *
     * @param templateId
     * @param template
     * @throws Exception
     */
    public void updateTemplate(Long templateId,DataDevScriptTemplate template)throws Exception;

    public void shareTemplate(DataDevScriptTemplate template , String erp) ;
}
