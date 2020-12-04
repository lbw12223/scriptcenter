package com.jd.bdp.datadev.service.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.dao.DataDevGitGroupMemberDao;
import com.jd.bdp.datadev.domain.DataDevGitGroupMember;
import com.jd.bdp.datadev.jdgit.GitHttpResponse;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.datadev.service.DataDevGitGroupMemberService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
@Service
public class DataDevGitGroupMemberServiceImpl implements DataDevGitGroupMemberService {

    private static final Logger logger = Logger.getLogger(DataDevGitGroupMemberServiceImpl.class);

    @Autowired
    private DataDevGitGroupMemberDao dataDevGitGroupMemberDao;

    @Override
    public void batchInsert(List<DataDevGitGroupMember> dataDevGitGroupMemberList) {
        dataDevGitGroupMemberDao.batchInsert(dataDevGitGroupMemberList);
    }

    @Override
    public void deleteAll(Long groupId) {
        dataDevGitGroupMemberDao.deleteByGroupId(groupId);
    }

    @Override
    public List<DataDevGitGroupMember> queryFromGroupId(Long groupId) throws Exception {
        List<DataDevGitGroupMember> list = dataDevGitGroupMemberDao.queryByGroupId(groupId);
        return list;
    }

    @Override
    public List<DataDevGitGroupMember> findGroupId(String erp) throws Exception {
        List<DataDevGitGroupMember> list = dataDevGitGroupMemberDao.findGroupId(erp);
        return list;
    }

    @Override
    public Long getMemberIdByErp(String erp) throws Exception {
        if (StringUtils.isNotBlank(erp)) {
            DataDevGitGroupMember dataDevGitGroupMember = dataDevGitGroupMemberDao.getMerberIdByErp(erp);
            if (dataDevGitGroupMember != null) {
                Long memberId = dataDevGitGroupMember.getGitMemberId();
                return memberId;
            }
        }
        return null;
    }

    @Override
    public DataDevGitGroupMember getDataDevGitGroupMebByErp(String erp, Long groupId, Integer gitOrCodingCode) throws Exception {
        if (StringUtils.isNotBlank(erp) && groupId != null) {
            Long memberId = null;
//            DataDevGitGroupMember dataDevGitGroupMember = dataDevGitGroupMemberDao.getMerberIdByErp(erp);
//            DataDevGitGroupMember dataDevGitGroupMember = null;//或者获取的时候区分git Or Coding
//            if (dataDevGitGroupMember != null) {//该用户已经使用过IDE
//                memberId = dataDevGitGroupMember.getGitMemberId();
//                return getGitMemByMemIdAndGroupId(memberId, groupId, gitOrCodingCode);
//            } else {//没有使用过的话去拿userId
                JDGitGroups jdGitGroups = new JDGitGroups();
                jdGitGroups.setJdGroupId(groupId);
                jdGitGroups.setGitOrCodingCode(gitOrCodingCode);
                List<DataDevGitGroupMember> listMember = jdGitGroups.listGroupMembers();
                for (DataDevGitGroupMember ddggm : listMember) {
                    String name = ddggm.getGitMemberUserName();
                    if (StringUtils.equals(name, erp)) {
                        return ddggm;
                    }
                }
//            }

        }
        return null;
    }

    private DataDevGitGroupMember getGitMemByMemIdAndGroupId(Long memberId, Long groupId, Integer gitOrCodingCode) throws Exception {
        logger.error("getGitMemByMemIdAndGroupId memberId="+memberId+";groupId="+groupId+";gitOrCodingCode="+gitOrCodingCode);
        Map<String, String> params = new HashMap<String, String>();
        if (memberId != null && groupId != null) {
            GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doGet("groups/" + GitHttpUtil.getRealGroupId(groupId) + "/members/" + memberId.longValue(), params);
            if (gitHttpResponse.getResponseCode().equals(200)) {
                String responseMessage = gitHttpResponse.getResponseMessage();
                JSONObject jsonObject = JSONObject.parseObject(responseMessage);
                DataDevGitGroupMember dataDevGitGroupMember = new DataDevGitGroupMember();
                dataDevGitGroupMember.setId(jsonObject.containsKey("id") ? jsonObject.getLong("id") : memberId);
                dataDevGitGroupMember.setGitMemberName(jsonObject.containsKey("name") ? jsonObject.getString("name") : "");
                dataDevGitGroupMember.setGitMemberUserName(jsonObject.containsKey("username") ? jsonObject.getString("username") : "");
                dataDevGitGroupMember.setAccessLevel(jsonObject.containsKey("access_level") ? jsonObject.getInteger("access_level") : 0);
                dataDevGitGroupMember.setGitGroupId(groupId);
                dataDevGitGroupMember.setState(jsonObject.containsKey("state") ? jsonObject.getString("state") : "");
                return dataDevGitGroupMember;

            } else {
                logger.error("getGitMemByMemIdAndGroupId:param:memeberId：" + memberId + "groupId:" + groupId + "结果：" + gitHttpResponse.getResponseCode() + "   " + gitHttpResponse.getResponseMessage());
            }
        }

        return null;
    }
}
