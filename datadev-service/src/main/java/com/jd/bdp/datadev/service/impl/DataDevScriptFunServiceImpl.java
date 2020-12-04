package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevScriptFunDao;
import com.jd.bdp.datadev.dao.DataDevScriptFunDirDao;
import com.jd.bdp.datadev.domain.DataDevFunDetail;
import com.jd.bdp.datadev.domain.DataDevFunDir;
import com.jd.bdp.datadev.domain.ZtreeNode;
import com.jd.bdp.datadev.enums.ScriptTypeEnum;
import com.jd.bdp.datadev.service.DataDevScriptFunService;
import com.jd.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DataDevScriptFunServiceImpl implements DataDevScriptFunService {
    @Autowired
    private DataDevScriptFunDirDao funDirDao;

    @Autowired
    private DataDevScriptFunDao funDao;

//    @Autowired
//    private WordbookInterface wordbookInterface;
//    @Value("${datadev.appId}")
//    private String appId;//字典服务appId
//    @Value("${datadev.token}")
//    private String token;//访问字典服务token
//    @Value("${editor.shell.key}")
//    private String shellKey;
//    @Value("${editor.python.key}")
//    private String pythonKey;
    @Override
    public List<ZtreeNode> getFunsByDirId(Long dirId) throws Exception {
        List<DataDevFunDetail> funList = funDao.getDetailByDirId(dirId);
        List<DataDevFunDir> dirList = funDirDao.getDirsByDirId(dirId);
        List<ZtreeNode> resList = new ArrayList<ZtreeNode>();
        for (DataDevFunDir tmpFunDir : dirList) {
            resList.add(new ZtreeNode(tmpFunDir));
        }
        for (DataDevFunDetail tmpFun : funList) {
            resList.add(new ZtreeNode(tmpFun));
        }
        return resList;
    }

    @Override
    public DataDevFunDetail findById(Long id) throws Exception {
        return funDao.findById(id);
    }

    @Override
    public List<ZtreeNode> getFunsByKeyword(String keyword) throws Exception {
        List<ZtreeNode> resList = new ArrayList<ZtreeNode>();
        List<DataDevFunDetail> funList = funDao.getDetailByKeyword(keyword);
        if (funList != null && funList.size() > 0) {
            Set<Long> dirIds = new HashSet<Long>();
            for (DataDevFunDetail dataDevFunDetail : funList) {
                dirIds.add(dataDevFunDetail.getFunDirId());
                resList.add(new ZtreeNode(dataDevFunDetail));
            }
            if (dirIds != null && dirIds.size() > 0) {
                findParentDir(dirIds, resList);
            }

        }
        if (resList.size() < 1) {
            List<DataDevFunDir> minDirs = funDirDao.getMinFunDirIds();
            if (minDirs != null && minDirs.size() > 0) {
                for(DataDevFunDir dir : minDirs){
                    resList.add(new ZtreeNode(dir));
                }
            }
        }
        return resList;
    }

    private void findParentDir(Set<Long> dirIds, List<ZtreeNode> resList) {
        Set<Long> parentDirIds = new HashSet<Long>();
        if (dirIds != null && dirIds.size() > 0) {
            for (Long dirId : dirIds) {
                DataDevFunDir dir = funDirDao.getFunDirById(dirId);
                if (dir != null && dir.getpId() != null && dir.getpId() > 0L) {
                    parentDirIds.add(dir.getpId());
                }
                if (dir != null) {
                    resList.add(new ZtreeNode(dir));
                }
            }
        }
        if (parentDirIds != null && parentDirIds.size() > 0) {
            findParentDir(parentDirIds, resList);
        }
    }

    @Override
    public String getFunTipsByScriptType(String scriptType)throws Exception {
        String funTip="";
        if(ScriptTypeEnum.SQL.toName().equalsIgnoreCase(scriptType)){
            List<String> funTips = funDao.getDataDevFunNameList();
            if(funTips!=null&&funTips.size()>0) {//转换成js配置中的格式
                funTip=StringUtils.join(funTips, "|")+"|";
            }
        }else {
//            String wordbookKey="";
//            if (ScriptTypeEnum.Shell.toName().equalsIgnoreCase(scriptType)) {
//                wordbookKey=shellKey;
//            } else if (ScriptTypeEnum.Python2.toName().equalsIgnoreCase(scriptType)) {
//                wordbookKey=pythonKey;
//            }
//            try {
//                //从字典中读取
//                WordbookApiParam wordbookApiParam = new WordbookApiParam();
//                String time = String.valueOf(new Date().getTime());
//                wordbookApiParam.setKey(wordbookKey);
//                wordbookApiParam.setErp("wxywxy");
//                String wordBookJsonStr = wordbookInterface.getValue(appId, token, time, wordbookApiParam);
//                JSONObject wordbookObject = JSONObject.fromObject(wordBookJsonStr);
//                if ((wordbookObject != null && wordbookObject.get("code") != null && StringUtils.equals(wordbookObject.get("code").toString(), "0"))) {
//                    String obj = wordbookObject.getString("obj");
//                    if(StringUtils.isNotEmpty(obj)){
//                        funTip=obj;
//                    }
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("获取字典key:"+wordbookKey+"失败",e);
//            }
        }
        return funTip;
    }
}
