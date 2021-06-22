package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.component.UrmStaticUtil;
import com.jd.bdp.datadev.dao.DataDevScriptTemplateDao;
import com.jd.bdp.datadev.dao.DataDevScriptTemplateShareDao;
import com.jd.bdp.datadev.dao.DataDevScriptTemplateShowDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptTemplateService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.*;


@Service
public class DataDevScriptTemplateServiceImpl implements DataDevScriptTemplateService {

    @Autowired
    private DataDevScriptTemplateDao dataDevScriptTemplateDao;
    @Autowired
    private DataDevScriptTemplateShowDao scriptTemplateShowDao;
    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevScriptTemplateShareDao dataDevScriptTemplateShareDao;
    @Autowired
    private DataDevGitProjectService gitProjectService;

    private static final Long TEMPLATE_PROJECT_ID = 46945L;

    private static final Logger logger = Logger.getLogger(DataDevScriptTemplateServiceImpl.class);

    @Override
    public List<DataDevScriptTemplate> searchScriptTemplate(Integer scriptType, Integer pythonType, String key, String erp) throws Exception {
        //负责人是自己的模板
        Long start = new Date().getTime();
        List<DataDevScriptTemplate> ownedlist = dataDevScriptTemplateDao.searchOwnedScriptTemplate(scriptType, pythonType == 0 ? null : pythonType, key, erp);
        //直接分享给erp的，分享项目成员包含erp，分享的组成员包含erp
        Long end1 = new Date().getTime();


        List<DataDevGitProject> dataDevGitProjectList = gitProjectService.getErpProjectBySearch(erp, null,-1);
        List<String> allUsers = gitProjectService.getAllUserHaveSameProject(dataDevGitProjectList);


        List<DataDevScriptTemplate> shareGitList = dataDevScriptTemplateDao.searchSharedGitScriptTemplate(scriptType, pythonType == 0 ? null : pythonType, key, erp, allUsers);
        List<DataDevScriptTemplate> shareErpList = dataDevScriptTemplateDao.searchSharedErpScriptTemplate(scriptType, pythonType == 0 ? null : pythonType, key, erp, allUsers);
        Long end2 = new Date().getTime();
        logger.error("==============ownTime:" + (end1 - start));
        logger.error("==============shareTime:" + (end2 - end1));
        List<DataDevScriptTemplate> resultList = new ArrayList<>();

        for (DataDevScriptTemplate template : ownedlist) {
            if (!resultList.contains(template)) {
                String creator = StringUtils.isNotBlank(template.getCreator()) ? template.getCreator() : "";
                String zhName = UrmStaticUtil.getNameByErp(template.getCreator());
                if (StringUtils.isNotBlank(zhName)) {
                    creator = zhName + "(" + creator + ")";
                }
                template.setCreator(creator);
                template.setTemplateFrom(0);
                resultList.add(template);
            }

        }
        for (DataDevScriptTemplate template : shareGitList) {
            if (!resultList.contains(template)) {
                String creator = StringUtils.isNotBlank(template.getCreator()) ? template.getCreator() : "";
                String zhName = UrmStaticUtil.getNameByErp(template.getCreator());
                if (StringUtils.isNotBlank(zhName)) {
                    creator = zhName + "(" + creator + ")";
                }
                template.setCreator(creator);
                template.setTemplateFrom(1);
                resultList.add(template);
            }
        }
        for (DataDevScriptTemplate template : shareErpList) {
            if (!resultList.contains(template)) {
                String creator = StringUtils.isNotBlank(template.getCreator()) ? template.getCreator() : "";
                String zhName = UrmStaticUtil.getNameByErp(template.getCreator());
                if (StringUtils.isNotBlank(zhName)) {
                    creator = zhName + "(" + creator + ")";
                }
                template.setCreator(creator);
                template.setTemplateFrom(1);
                resultList.add(template);
            }
        }

        if (resultList.size() > 0) {
            Collections.sort(resultList, new Comparator<DataDevScriptTemplate>() {
                @Override
                public int compare(DataDevScriptTemplate o1, DataDevScriptTemplate o2) {
                    return Collator.getInstance(Locale.SIMPLIFIED_CHINESE).compare(o1.getName(), o2.getName());
                }
            });
            Long index =0L;
            for(DataDevScriptTemplate template:resultList){
                template.setWordOrder(++index);
            }
            Collections.sort(resultList, new Comparator<DataDevScriptTemplate>() {
                @Override
                public int compare(DataDevScriptTemplate o1, DataDevScriptTemplate o2) {
                    if (o1.getTemplateType() == null) {
                        o1.setTemplateType(0);
                    }
                    if (o2.getTemplateType() == null) {
                        o2.setTemplateType(0);
                    }
                    if (o1.getShowOrder() == null) {
                        o1.setShowOrder(0L);
                    }
                    if (o2.getShowOrder() == null) {
                        o2.setShowOrder(0L);
                    }
                    if (o1.getTemplateType() != o2.getTemplateType()) {
                        return o1.getTemplateType() - o2.getTemplateType();
                    }
                    if (o1.getTemplateType() == 0) {
                        return Collator.getInstance(Locale.SIMPLIFIED_CHINESE).compare(o1.getName(), o2.getName());
                    } else if (o1.getShowOrder() != o2.getShowOrder()) {
                        return o2.getShowOrder() - o1.getShowOrder() > 0 ? 1 : -1;
                    } else {
                        return Collator.getInstance(Locale.SIMPLIFIED_CHINESE).compare(o1.getName(), o2.getName());
                    }
                }
            });
        }



        if (resultList.size() > 0) {

        }

        return resultList;
    }

    @Override
    public DataDevScriptTemplate getScriptTemplateById(Long id) {
        return dataDevScriptTemplateDao.getScriptTemplateById(id);
    }


    @Override
    public DataDevScriptTemplate insertScriptTemplate(String name, Integer scriptType, Integer pythonType, Integer templateType, String erp, String desc, String args, Long showOrder, Long scriptFileId, Integer status) {
        DataDevScriptTemplate template = new DataDevScriptTemplate();
        template.setName(name);
        template.setScriptType(scriptType);
        template.setPythonType(pythonType);
        template.setTemplateType(templateType);
        template.setCreator(erp);
        template.setMender(erp);
        template.setIncharge(erp);
        template.setDesc(desc);
        template.setArgs(args);
        template.setScriptFileId(scriptFileId);
        template.setStatus(status);
        dataDevScriptTemplateDao.insertScriptTemplate(template);
        return template;
    }

    @Override
    @Transactional
    public void initScriptTemplate() throws Exception {
        List<DataDevScriptTemplate> list = dataDevScriptTemplateDao.getAllSystemTemplate();
        DataDevScriptTemplate updateObj = new DataDevScriptTemplate();
        for (DataDevScriptTemplate template : list) {
            if (template.getScriptFileId() == null || template.getScriptFileId() <= 0) {
                Thread.sleep(2000);
                byte[] bytes = StringUtils.isNotBlank(template.getContent()) ? template.getContent().getBytes("utf-8") : new byte[0];
                ZtreeNode ztreeNode = fileService.createNewFile(TEMPLATE_PROJECT_ID, null, template.getScriptType(), UrmStaticUtil.getBdpManager(), 1, bytes, "", null,null,template.getArgs(),null,null);
                updateObj.setScriptFileId(ztreeNode.getId());
                updateObj.setId(template.getId());
                updateObj.setIncharge(UrmStaticUtil.getBdpManager());
                updateObj.setMender(UrmStaticUtil.getBdpManager());
                updateObj.setCreator(UrmStaticUtil.getBdpManager());
                dataDevScriptTemplateDao.updateScriptTemplate(template.getId(), updateObj);
            }
        }

    }


    @Override
    public DataDevScriptTemplate getTemplateByFileId(Long scriptFileId) throws Exception {
        return dataDevScriptTemplateDao.getTemplateByScriptId(scriptFileId);
    }

    @Override
    public Long getTemPlateProjectId() throws Exception {
        return TEMPLATE_PROJECT_ID;
    }

    @Override
    public void insertTemplateByScript(DataDevScriptFile file) throws Exception {
        byte[] bytes = StringUtils.isNotBlank(file.getContent()) ? file.getContent().getBytes("utf-8") : new byte[0];
        ZtreeNode ztreeNode = fileService.createNewFile(TEMPLATE_PROJECT_ID, null, file.getType(), UrmStaticUtil.getBdpManager(), 1, bytes, "", null);
    }

    @Override
    public List<DataDevScriptTemplateShare> getSharesInfos(Long templateId) throws Exception {
        List<DataDevScriptTemplateShare> resultShares = new ArrayList<>();
        Map<String, DataDevScriptTemplateShare> resultMap = new HashMap<>();
        List<DataDevScriptTemplateShare> shares = dataDevScriptTemplateShareDao.getSharesByTemplateId(templateId);
        for (DataDevScriptTemplateShare share : shares) {
            String value = share.getShareType() + share.getShareTarget();
            if (!resultMap.containsKey(value)) {
                resultShares.add(share);
            }
        }
        shares.clear();
        resultMap.clear();
        return resultShares;
    }

    @Override
    public boolean existTemplateName(Long templateId, String templateName) throws Exception {
        DataDevScriptTemplate template = dataDevScriptTemplateDao.getTemplateByName(templateId, templateName);
        return template != null;
    }


    @Override
    public DataDevScriptTemplate shareTemplate(DataDevScriptTemplate template , String operator){
        if (template != null && template.getId() <= 0) {
            try {
                template = saveScriptTemplate(template, operator);
                return template;
            } catch (Exception e) {
                logger.error("模板保存失败：", e);
                throw new RuntimeException("模板保存失败！");
            }
        }
        boolean shareGits= template.getShareGits();
        String shareErps = template.getShareErps();
        List<DataDevScriptTemplateShare> templateShares = new ArrayList<>();
        if (StringUtils.isNotBlank(shareErps)) {
            String[] erpArray = shareErps.split(",");
            for (String erp : erpArray) {
                if (StringUtils.isNotBlank(erp) && !erp.equals(operator)) {
                    templateShares.add(new DataDevScriptTemplateShare(template.getId(), 1L, erp, operator));
                }
            }
        }
        if (shareGits) {
            templateShares.add(new DataDevScriptTemplateShare(template.getId(), 2L, null, operator));
        }
        dataDevScriptTemplateShareDao.deleteByTemplateId(template.getId());
        if (templateShares != null && templateShares.size() > 0) {
            dataDevScriptTemplateShareDao.insertTemplateShares(templateShares);
        }
        template.setStatus(0);
        dataDevScriptTemplateDao.updateScriptTemplate(template.getId(), template);
        return template;
    }
    /**
     * @param template template.shareGits {@link com.jd.bdp.datadev.domain.DataDevGitDto}
     * @param operator
     * @return
     * @throws Exception
     */
    @Override
    @Transactional
    public DataDevScriptTemplate saveScriptTemplate(DataDevScriptTemplate template, String operator) throws Exception {
        if (!(template.getId() != null && template.getId() > 0) && template.getGitProjectId() != null && StringUtils.isNotBlank(template.getGitProjectFilePath())) {
            DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(template.getGitProjectId(), template.getGitProjectFilePath());
            if (file == null) {
                throw new RuntimeException("脚本不存在：" + template.getGitProjectFilePath());
            }
            String content = fileService.getScriptContent(template.getGitProjectId(), template.getGitProjectFilePath(), file.getVersion(), operator);
            ZtreeNode ztreeNode = fileService.createNewFile(getTemPlateProjectId(), template.getGitProjectFilePath(), template.getScriptType(), operator, 1, content.getBytes("utf-8"), file.getDescription(), null, "", file.getArgs(), null, null);
            DataDevScriptTemplate dataDevScriptTemplate = insertScriptTemplate(template.getName(), template.getScriptType(), template.getPythonType(), 1, operator, template.getDesc(), file.getArgs(), 0L, ztreeNode.getId(), 0);
            template.setGitProjectId(ztreeNode.getGitProjectId());
            template.setGitProjectFilePath(ztreeNode.getPath());
            template.setId(dataDevScriptTemplate.getId());
        }
        if (template.getId() != null && template.getId() > 0) {
            DataDevScriptTemplate oldTemplate = dataDevScriptTemplateDao.getScriptTemplateById(template.getId());
            if (oldTemplate == null) {
                throw new RuntimeException("模板不存在");
            }
            if (oldTemplate.getTemplateType() != null && oldTemplate.getTemplateType() == 0 || !oldTemplate.getIncharge().equals(operator)) {
                throw new RuntimeException("无权限操作模板");
            }
            String shareErps = template.getShareErps();
            boolean shareGits = template.getShareGits();
            List<DataDevScriptTemplateShare> templateShares = new ArrayList<>();
            if (StringUtils.isNotBlank(shareErps)) {
                String[] erpArray = shareErps.split(",");
                for (String erp : erpArray) {
                    if (StringUtils.isNotBlank(erp) && !erp.equals(operator)) {
                        templateShares.add(new DataDevScriptTemplateShare(template.getId(), 1L, erp, operator));
                    }
                }
            }
            if (shareGits) {
                templateShares.add(new DataDevScriptTemplateShare(template.getId(), 2L, null, operator));
            }
            dataDevScriptTemplateShareDao.deleteByTemplateId(template.getId());
            if (templateShares != null && templateShares.size() > 0) {
                dataDevScriptTemplateShareDao.insertTemplateShares(templateShares);
            }
            template.setStatus(0);
            dataDevScriptTemplateDao.updateScriptTemplate(template.getId(), template);
        }
        return template;
    }


    @Override
    @Transactional
    public DataDevScriptTemplate deleteTemplate(String erp, Long templateId) throws Exception {
        DataDevScriptTemplate template = dataDevScriptTemplateDao.getScriptTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getTemplateType() != null && template.getTemplateType() == 0 || !template.getIncharge().equals(erp)) {
            throw new RuntimeException("无权限操作模板");
        }
        DataDevScriptTemplate update = new DataDevScriptTemplate();
        update.setId(templateId);
        update.setDeleted(1);
        dataDevScriptTemplateDao.updateScriptTemplate(templateId, update);
        dataDevScriptTemplateShareDao.deleteByTemplateId(templateId);
        scriptTemplateShowDao.deletedBytemplateId(templateId);
        if (template.getScriptFileId() != null) {
            DataDevScriptFile file = fileService.findById(template.getScriptFileId());
            if (file != null) {
                template.setGitProjectId(file.getGitProjectId());
                template.setGitProjectFilePath(file.getGitProjectFilePath());
            }
        }
        return template;
    }

    @Override
    public DataDevScriptTemplate topTemplate(String erp, Long templateId, boolean toTop) throws Exception {
        DataDevScriptTemplate template = dataDevScriptTemplateDao.getScriptTemplateById(templateId);
        if (template.getTemplateType() != null && template.getTemplateType() == 0) {
            throw new RuntimeException("无权限操作模板");
        }
        Long maxShowOrder = 0L;
        if (toTop) {
            maxShowOrder = scriptTemplateShowDao.getMaxShowOrder(erp);
            maxShowOrder = maxShowOrder != null ? ++maxShowOrder : 1;
        }
        scriptTemplateShowDao.updateTemplateShow(erp, templateId, maxShowOrder);
        return template;
    }

    @Override
    public void updateTemplate(Long templateId, DataDevScriptTemplate template) throws Exception {
        dataDevScriptTemplateDao.updateScriptTemplate(templateId,template);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("张反倒是");
        list.add("王反倒是");
        list.add("1");
        list.add("反倒是");
        list.add("r");
        list.add("孙无");
        list.add("两个");
        list.add("a");
        list.add("阿莫");
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Collator.getInstance(Locale.CHINESE).compare(o1, o2);
            }
        });
        for (String str : list) {
            System.out.println(str);
        }

    }
}
