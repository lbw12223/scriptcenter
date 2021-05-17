package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevGitGroupDao;
import com.jd.bdp.datadev.dao.DataDevGitProjectDao;
import com.jd.bdp.datadev.domain.DataDevGitDto;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.enums.DataDevGitInitFlag;
import com.jd.bdp.datadev.enums.DataDevProjectTypeEnum;
import com.jd.bdp.datadev.jdgit.JDGitCommits;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptTemplateService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
@Service
public class DataDevGitProjectServiceImpl implements DataDevGitProjectService {


    @Autowired
    private DataDevGitProjectDao dataDevGitProjectDao;
    @Autowired
    private DataDevGitGroupDao dataDevGitGroupDao;
    @Autowired
    private DataDevScriptFileService fileService;
    @Value("${datadev.env}")
    private String env;
    @Autowired
    private DataDevScriptTemplateService dataDevScriptTemplateService;

    @Override
    public List<DataDevGitProject> listAll() {
        return dataDevGitProjectDao.listAll();
    }


    @Override
    public void insert(List<DataDevGitProject> inserts) {
        if (inserts != null && inserts.size() > 0) {
            for (DataDevGitProject dataDevGitProject : inserts) {
                dataDevGitProjectDao.insertDataDevGitProject(dataDevGitProject);
            }
        }
    }


    @Override
    public void delete(List<DataDevGitProject> deletes) {
        if (deletes != null && deletes.size() > 0) {
            StringBuilder ids = new StringBuilder();
            for (DataDevGitProject dataDevGitProject : deletes) {
                dataDevGitProjectDao.batchDelete(dataDevGitProject.getGitProjectId());
            }
        }
    }

    @Override
    public void updateAllProjectInitMember() {
        dataDevGitProjectDao.updateAllProjectInitMember();
    }

    @Override
    public DataDevGitProject getNeedInitMemeberDataDevGitProject() {
        return dataDevGitProjectDao.getNeedInitMember();
    }


    @Override
    public void upateDataDevGitProject(DataDevGitProject dataDevGitProject) {
        dataDevGitProjectDao.updateDataDevGitProject(dataDevGitProject);
    }

    /**
     * 查询erp可以看见,操作的project
     *
     * @param erp
     * @return
     */
    @Override
    public List<DataDevGitProject> getErpProject(String erp) {
        return  getErpProjectBySearch(erp,null,-1);
    }

    @Override
    public void verifyUserAuthority(String erp, Long gitProjectId) throws Exception {
        if(erp.equals(fileService.getDefaultUserToken())){
            return;
        }
        if(!("online").equals(env)){
            return;
        }
        if(dataDevScriptTemplateService.getTemPlateProjectId().equals(gitProjectId)){
            return;
        }
        DataDevGitProject project=getGitProjectBy(gitProjectId);
        if(project==null){
            throw new RuntimeException("id为"+gitProjectId+"的项目不存在");
        }
        if(gitProjectId!=null && StringUtils.isNotBlank(erp)){
            List<DataDevGitProject> projects=getErpProject(erp);
            for(DataDevGitProject gitProject:projects){
                if(gitProjectId.equals(gitProject.getGitProjectId())){
                    return;
                }
            }
        }
        throw new RuntimeException("用户："+erp+"没有项目"+project.getGitProjectPath()+"("+gitProjectId+")的权限");
    }

    public DataDevGitProjectDao getDataDevGitProjectDao() throws Exception{
        return dataDevGitProjectDao;
    }

    public void setDataDevGitProjectDao(DataDevGitProjectDao dataDevGitProjectDao) {
        this.dataDevGitProjectDao = dataDevGitProjectDao;
    }

    /**
     * 查询erp可以看见,操作的project
     *
     * @param erp
     * @return
     */
    @Override
    public List<DataDevGitProject> getErpProjectBySearch(String erp ,String keyword , Integer projectType) {


        DataDevProjectTypeEnum dataDevProjectTypeEnum = DataDevProjectTypeEnum.enumValueOf(projectType);

        List<DataDevGitProject> result = new ArrayList<DataDevGitProject>();
        Set<Long> projectIds = new HashSet<Long>();
        List<DataDevGitProject> erpProjects = dataDevGitProjectDao.getErpDataDevGitProject(erp,keyword,dataDevProjectTypeEnum.idMin,dataDevProjectTypeEnum.idMax);
        List<DataDevGitProject> sharedGroupProjects = dataDevGitProjectDao.getErpDataDevGitProjectBySharedGroup(erp,keyword,dataDevProjectTypeEnum.idMin,dataDevProjectTypeEnum.idMax);

        if (erpProjects != null && erpProjects.size() > 0) {
            for (DataDevGitProject dataDevGitProject : erpProjects) {
                if (!projectIds.contains(dataDevGitProject.getGitProjectId())) {
                    result.add(dataDevGitProject);
                    projectIds.add(dataDevGitProject.getGitProjectId());
                }
            }
        }
        if (sharedGroupProjects != null && sharedGroupProjects.size() > 0) {
            for (DataDevGitProject dataDevGitProject : sharedGroupProjects) {
                if (!projectIds.contains(dataDevGitProject.getGitProjectId())) {
                    result.add(dataDevGitProject);
                    projectIds.add(dataDevGitProject.getGitProjectId());
                }
            }
        }
        return result;
    }

    @Override
    public DataDevGitProject getGitProjectBy(Long gitProjectId) {
        return dataDevGitProjectDao.getDataDevScriptProjectById(gitProjectId);
    }


    @Override
    public DataDevGitProject getGitProjectBy(String gitProjectPath) {
        return dataDevGitProjectDao.getGitProjectByPath(gitProjectPath);
    }

    /**
     * 创建 项目
     * 插入数据库中
     * <p>
     *
     * @param jdGitProjects
     */
    @Override
    public DataDevGitProject createProject(JDGitProjects jdGitProjects) throws Exception {
        DataDevGitProject insertDataDevGitProject = jdGitProjects.createProject();
        insertDataDevGitProject.setFinishProjectMemberFlag(DataDevGitInitFlag.NEED_INIT.tocode());
        insertDataDevGitProject.setFinishProjectTreeFlag(DataDevGitInitFlag.NEED_INIT.tocode());
        dataDevGitProjectDao.insertDataDevGitProject(insertDataDevGitProject);
        return insertDataDevGitProject;
    }

    @Override
    public void insertOneProject(DataDevGitProject insertDataDevGitProject) throws Exception {
        insertDataDevGitProject.setFinishProjectMemberFlag(DataDevGitInitFlag.NEED_INIT.tocode());
        insertDataDevGitProject.setFinishProjectTreeFlag(DataDevGitInitFlag.NEED_INIT.tocode());
        dataDevGitProjectDao.insertDataDevGitProject(insertDataDevGitProject);
    }

    @Override
    public List<DataDevGitDto> getGitAndGroupByKeyWord( String erp,String keyWord) throws Exception {
        if(StringUtils.isNotBlank(keyWord)){
            keyWord=keyWord.replaceAll("\\_","\\\\_");
            keyWord=keyWord.replaceAll("\\%","\\\\%");
        }
        List<DataDevGitDto> dataDevGitDtos = new ArrayList<>();
        List<DataDevGitProject> projects = dataDevGitProjectDao.getAllGitProjectByKeyWord(keyWord,erp,10);
        List<DataDevGitGroup> groups = dataDevGitGroupDao.getAllGitGroupByKeyWord(keyWord,erp,10);
        for(DataDevGitProject project : projects){
            DataDevGitDto gitDto = new DataDevGitDto(project);
            if(!dataDevGitDtos.contains(gitDto)){
                dataDevGitDtos.add(gitDto);
            }
        }
        for(DataDevGitGroup group : groups){
            DataDevGitDto gitDto = new DataDevGitDto(group);
            if(!dataDevGitDtos.contains(gitDto)){
                dataDevGitDtos.add(gitDto);
            }
        }
        final String upperKeyWord = keyWord.toUpperCase();
        Collections.sort(dataDevGitDtos, new Comparator<DataDevGitDto>() {
            @Override
            public int compare(DataDevGitDto o1, DataDevGitDto o2) {
                Integer index1 = o1.getPath().toUpperCase().indexOf(upperKeyWord);
                Integer index2 = o2.getPath().toUpperCase().indexOf(upperKeyWord);
                if(index1.equals(index2)){
                    return o1.getPath().length() - o2.getPath().length();
                }
                return index1 - index2;
            }
        });
        if(dataDevGitDtos.size()>10){
            return dataDevGitDtos.subList(0,10);
        }else {
            return dataDevGitDtos;
        }
    }

    @Override
    public List<String> getAllUserHaveSameProject(List<DataDevGitProject> projects) throws Exception {
        return dataDevGitProjectDao.getAllUserHaveSameProject(projects);
    }
}
