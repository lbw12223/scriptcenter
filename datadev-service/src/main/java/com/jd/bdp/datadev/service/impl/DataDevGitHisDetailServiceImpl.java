package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.api.urm.UserInterface;
import com.jd.bdp.api.urm.UserJsfInterface;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.ConvertErp2UserName;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.dao.DataDevGitHisDetailDao;
import com.jd.bdp.datadev.domain.DataDevGitHis;
import com.jd.bdp.datadev.domain.DataDevGitHisDetail;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.service.DataDevGitHisDetailService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.domain.urm.TblBaseUser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class DataDevGitHisDetailServiceImpl implements DataDevGitHisDetailService {

    private static final Logger logger = Logger.getLogger(DataDevGitHisDetailServiceImpl.class);

    @Autowired
    private DataDevGitHisDetailDao dataDevGitHisDetailDao;


    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    private static final int PER_INSERT_SIZE = 100;

    @Autowired
    private UserJsfInterface userInterface;

    @Autowired
    private UrmUtil urmUtil ;

    @Override
    public void batchInsertDataDevGitHisDetails(List<DataDevGitHisDetail> insertDataDevGitHisDetail) {


       // changeErp(insertDataDevGitHisDetail);
        changeEmails(insertDataDevGitHisDetail);

        Integer size = insertDataDevGitHisDetail.size();
        if (size > 0) {
            Integer page = size / PER_INSERT_SIZE + (size % PER_INSERT_SIZE == 0 ? 0 : 1);
            for (int index = 0; index < page; index++) {
                List<DataDevGitHisDetail> insertList = insertDataDevGitHisDetail.subList(index * PER_INSERT_SIZE, Math.min(size, (index + 1) * PER_INSERT_SIZE));
                dataDevGitHisDetailDao.batchInsertGitHisDetail(insertList);
            }
        }
    }

    /**
     * 修改email
     *
     * @param insertDataDevGitHisDetail
     */
    private void changeEmails(List<DataDevGitHisDetail> insertDataDevGitHisDetail) {
        Set<String> erps = new HashSet<String>();
        for (DataDevGitHisDetail temp : insertDataDevGitHisDetail) {
            erps.add(temp.getCreator());
        }
        Map<String, String> erpAndEmails = new HashMap<String, String>();
        List<TblBaseUser> tblBaseUsers = userInterface.findUserByErps(erps.toArray(new String[]{}));
        for (TblBaseUser tblBaseUser : tblBaseUsers) {
            erpAndEmails.put(tblBaseUser.getErp(), tblBaseUser.getEmail());
        }
        for (DataDevGitHisDetail dataDevGitHisDetail : insertDataDevGitHisDetail) {
            if (dataDevGitHisDetail.isCommitByBDPIDE()) {
                //bdp ide 提交都是虚拟账号，所以需要修改email
                String fixEmail = erpAndEmails.get(dataDevGitHisDetail.getCreator());
                if (StringUtils.isNotBlank(fixEmail)) {
                    dataDevGitHisDetail.setEmail(fixEmail);
                }
            }

        }
    }

    /**
     * 通过email 修改erp
     *
     * @param insertDataDevGitHisDetail
     */
    private void changeErp(List<DataDevGitHisDetail> insertDataDevGitHisDetail) {
        Set<String> emails = new HashSet<String>();
        for (DataDevGitHisDetail temp : insertDataDevGitHisDetail) {
            emails.add(temp.getEmail());
        }
        Map<String, String> emailAndErp = new HashMap<String, String>();
        for (String email : emails) {
            TblBaseUser tblBaseUser = userInterface.getUserByEmail(email);
            if (tblBaseUser != null) {

                emailAndErp.put(tblBaseUser.getEmail(), tblBaseUser.getErp());
            }
        }

        for (DataDevGitHisDetail dataDevGitHisDetail : insertDataDevGitHisDetail) {
            if (dataDevGitHisDetail.isCommitByBDPIDE()) {
                //通过BDP IDE 平台提交的都是虚拟账号
                return;
            }
            String fixErp = emailAndErp.get(dataDevGitHisDetail.getEmail());
            if (StringUtils.isNotBlank(fixErp)) {
                dataDevGitHisDetail.setCreator(fixErp);
            }
        }
    }

    /**
     * @param gitProjectId
     */
    @Override
    public void deleteDataDevGitHisDetail(Long gitProjectId) {
        dataDevGitHisDetailDao.deleteGitHisDetail(gitProjectId);
    }

    @Override
    public void insertDataDevGitHisDetail(DataDevGitHisDetail dataDevGitHisDetail) {


    }

    @Override
    public PageResultDTO queryDataDevGitHisDetail(DataDevGitHisDetail dataDevGitHisDetail, Pageable pageable) {
        PageResultDTO pageResultDTO = new PageResultDTO();
        dataDevGitHisDetail.setStart(pageable.getOffset());
        dataDevGitHisDetail.setLimit(pageable.getPageSize());
        List<DataDevGitHisDetail> list = null;
        Long count = 0L;
        Long gitProjectId = dataDevGitHisDetail.getGitProjectId();
        String gitProjectFilePath = dataDevGitHisDetail.getGitProjectFilePath();

        if (dataDevGitHisDetail.isDir()) {
            count = dataDevGitHisDetailDao.countDirCommit(dataDevGitHisDetail);
            if (count > 0) {
                list = dataDevGitHisDetailDao.listDirCommit(dataDevGitHisDetail);
            }
        } else {
            DataDevScriptFile dataDevScriptFile = dataDevScriptFileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
            if (dataDevScriptFile != null) {
                count = dataDevGitHisDetailDao.countFileCommit(dataDevGitHisDetail);
                if (count > 0) {
                    list = dataDevGitHisDetailDao.listFileCommit(dataDevGitHisDetail);
                }
            }
        }

        try{
            urmUtil.covertUserErp2UserName(list,new ConvertErp2UserName<DataDevGitHisDetail>(){
                @Override
                public String getErp(DataDevGitHisDetail dataDevGitHisDetail) {
                    return dataDevGitHisDetail.getCreator();
                }

                @Override
                public void setErpUserName(DataDevGitHisDetail dataDevGitHisDetail, String userNames) {
                    if(StringUtils.isNotBlank(userNames)){
                        String erp = dataDevGitHisDetail.getCreator();
                        dataDevGitHisDetail.setCreator(userNames + "("+erp+")");
                    }
                }
            });
        }catch (Exception e){
            logger.error("queryDataDevGitHisDetail",e);
        }

        pageResultDTO.setRecords(count);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(list);
        return pageResultDTO;
    }

    @Override
    public List<DataDevGitHisDetail> queryDataDevGitHisDetailByCommitId(Long gitProjectId, String commitId) {
        return dataDevGitHisDetailDao.selectGitHisDetailByCommitId(gitProjectId, commitId);
    }

    @Override
    public DataDevGitHisDetail queryDataDevGitHisDetailById(Long id) {
        return dataDevGitHisDetailDao.selectGitHisDetailById(id);
    }

    @Override
    public Long countGitHisDetailByCommitId(String commitId) {
        return dataDevGitHisDetailDao.countGitHisDetailByCommitId(commitId);
    }
}
