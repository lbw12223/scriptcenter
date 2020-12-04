
package com.jd.bdp.datadev.dao;

import org.apache.ibatis.annotations.Param;

public interface DataDevScriptTemplateShowDao{


    Long getMaxShowOrder(@Param("erp")String erp)throws Exception;

    void  updateTemplateShow(@Param("erp")String erp,@Param("templateId") Long templateId ,@Param("showOrder") Long showOrder )throws Exception;

    void deletedBytemplateId(Long templateId);
}