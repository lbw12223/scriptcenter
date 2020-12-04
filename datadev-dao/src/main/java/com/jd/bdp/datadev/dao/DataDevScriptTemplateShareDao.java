package com.jd.bdp.datadev.dao;


import com.jd.bdp.datadev.domain.DataDevScriptTemplateShare;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptTemplateShareDao {

    List<DataDevScriptTemplateShare> getSharesByTemplateId(Long templateId);

    DataDevScriptTemplateShare getShareGitTemplate(@Param("templateId") Long templateId,@Param("shareType") Integer shareType);

    void deleteByTemplateId(Long templateId);

    void insertTemplateShares(@Param("shares") List<DataDevScriptTemplateShare> shares);



}
