package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.DownLoadFileUtil;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.component.ZipUtil;
import com.jd.bdp.datadev.dao.DataDevDependencyDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.exception.DependencyDetailNotFoundException;
import com.jd.bdp.datadev.exception.FileNotFoundException;
import com.jd.bdp.datadev.jdgit.Base64Util;
import com.jd.bdp.datadev.service.DataDevDependencyService;
import com.jd.bdp.datadev.service.DataDevScriptDirService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.util.MD5Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.SimpleFormatter;

@Service
public class DataDevDependencyServiceImpl implements DataDevDependencyService {
    @Autowired
    private DataDevDependencyDao dataDevDependencyDao;
    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private DataDevScriptDirService dirService;
    private static final Logger logger = Logger.getLogger(DataDevDependencyServiceImpl.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<DataDevDependencyDetail> transferJsonArray(JSONArray jsonArray, Long gitProjectId) throws Exception {
        List<DataDevDependencyDetail> resultList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getInteger("parChl") == 1) {
                DataDevDependencyDetail detail = new DataDevDependencyDetail();
                String path = jsonObject.getString("path");
                detail.setDependencyGitProjectFilePath(path);
                detail.setGitProjectId(gitProjectId);
                detail.setDependencyVersion(jsonObject.get("version") != null ? jsonObject.getString("version") : null);
                resultList.add(detail);
            } else if (jsonObject.getInteger("fullDir") == 1) {
                List<ZtreeNode> ztreeNodeList = dirService.getAllDirsAndFiles(gitProjectId, jsonObject.getString("path"));
                for (ZtreeNode ztreeNode : ztreeNodeList) {
                    if (!ztreeNode.isDir()) {
                        String path = ztreeNode.getPath();
                        DataDevDependencyDetail detail = new DataDevDependencyDetail();
                        detail.setDependencyGitProjectFilePath(path);
                        detail.setGitProjectId(gitProjectId);
                        resultList.add(detail);
                    }
                }
            }
        }
        return resultList;
    }


    @Override
    @Transactional
    public HoldDoubleValue<Boolean, DataDevDependency> saveDetails(List<DataDevDependencyDetail> devDependencyDetails, Long gitProjectId, String gitProjectFilePath, String erp) throws Exception {
        DataDevScriptFile scriptFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if(scriptFile==null){
            scriptFile=fileService.getScriptByGitProjectIdAndFilePathIgnoreDelete(gitProjectId,gitProjectFilePath,null);
        }
        String md5 = "";
        md5 = getMd5ByDependencyDetails(devDependencyDetails);
        if (scriptFile.getDependencyId() != null && scriptFile.getDependencyId() > 0) {
            DataDevDependency dataDevDependency = dataDevDependencyDao.getById(scriptFile.getDependencyId());
            if (dataDevDependency == null) {
                throw new RuntimeException("所属依赖" + scriptFile.getDependencyId() + "不存在！");
            }
            dataDevDependency.setDependencyDetails(devDependencyDetails);
            logger.error(md5 + "============" + dataDevDependency.getMd5());
            if (md5.equals(dataDevDependency.getMd5())) {
                return new HoldDoubleValue<>(false, dataDevDependency);
            }
        }
        DataDevDependency newDependency = new DataDevDependency();
        newDependency.setMd5(md5);
        newDependency.setCreator(erp);
        newDependency.setGitProjectFilePath(gitProjectFilePath);
        newDependency.setGitProjectId(gitProjectId);
        newDependency.setScriptId(scriptFile.getId());
        dataDevDependencyDao.saveDependency(newDependency);
        for (DataDevDependencyDetail detail : devDependencyDetails) {
            detail.setDependencyId(newDependency.getId());
        }
        dataDevDependencyDao.saveDependencyDetails(devDependencyDetails);
        newDependency.setDependencyDetails(devDependencyDetails);
        return new HoldDoubleValue<>(true, newDependency);
    }

    @Override
    public DataDevDependency getById(Long id) throws Exception {
        return dataDevDependencyDao.getById(id);
    }

    /**
     * 根据文件获取他依赖的文件列表
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param version
     * @return
     * @throws Exception
     */
    @Override
    public List<DataDevDependencyDetail> getDetails(Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath, version);
        if (file == null) {
            throw new FileNotFoundException(gitProjectId, gitProjectFilePath);
        }
        Long dependencyId = file.getDependencyId();
        List<DataDevDependencyDetail> list = dataDevDependencyDao.getDependencyDetailsByDependId(dependencyId);
        if (list == null) {
            list = new ArrayList<>();
        }
        boolean isHasFile = false;
        for (DataDevDependencyDetail detail : list) {
            if (detail.getDependencyGitProjectFilePath().equals(gitProjectFilePath)) {
                isHasFile = true;
                break;
            }
        }
        if (!isHasFile) {
            DataDevDependencyDetail detail = new DataDevDependencyDetail();
            detail.setGitProjectFilePath(gitProjectFilePath);
            detail.setDependencyGitProjectFilePath(gitProjectFilePath);
            detail.setDependencyVersion(file.getVersion());
            detail.setScriptId(file.getId());
            detail.setDependencyId(file.getDependencyId());
            detail.setGitProjectId(file.getGitProjectId());
            list.add(detail);
        }
        return list;
    }

    @Override
    public List<DataDevDependencyDetail> getDetails(Long dependencyId) throws Exception {
        List<DataDevDependencyDetail> list = dataDevDependencyDao.getDependencyDetailsByDependId(dependencyId);
        return list;
    }

    @Override
    public String getMd5ByDependencyDetails(List<DataDevDependencyDetail> list) throws Exception {
        Collections.sort(list, new Comparator<DataDevDependencyDetail>() {
            @Override
            public int compare(DataDevDependencyDetail o1, DataDevDependencyDetail o2) {
                return o1.getDependencyGitProjectFilePath().compareTo(o2.getDependencyGitProjectFilePath());
            }
        });
        String md5 = "";
        for (DataDevDependencyDetail detail : list) {
            md5 += (detail.getGitProjectId() + "/" + Base64Util.formatName(detail.getDependencyGitProjectFilePath()) + "_" + detail.getDependencyVersion());
        }
        logger.error("=============" + md5);
        md5 = MD5Util.getMD5Str(md5);
        return md5;
    }

    @Override
    public List<DataDevDependencyDetail> updateNewVersion(List<DataDevDependencyDetail> needUpdate, DataDevDependency dataDevDependency) throws Exception {
        List<DataDevDependencyDetail> oldDetails  = dataDevDependencyDao.getDependencyDetailsByDependId(dataDevDependency.getId());
        Map<String, DataDevDependencyDetail> detailHashMap = new HashMap<>();
        for (DataDevDependencyDetail detail : oldDetails) {
            detailHashMap.put(detail.getDependencyGitProjectFilePath(), detail);
        }
        if (needUpdate != null && needUpdate.size() > 0) {
            for (DataDevDependencyDetail updateItem : needUpdate) {
                if (detailHashMap.containsKey(updateItem.getDependencyGitProjectFilePath())) {
                    DataDevDependencyDetail detail = detailHashMap.get(updateItem.getDependencyGitProjectFilePath());
                    detail.setDependencyVersion(updateItem.getDependencyVersion());
                }
            }
        }
        return oldDetails;
    }

    @Override
    public List<DataDevDependencyDetail> getNewVersionDetails(List<DataDevDependencyDetail> list, Long gitProjectId, String gitProjectFilePath ) throws Exception {
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if (file == null) {
            throw new FileNotFoundException(gitProjectId, gitProjectFilePath);
        }
        Long time = System.currentTimeMillis();
        Long dependencyId = file.getDependencyId();
        Map<String,DataDevScriptFile> fileMap =new HashMap<>();
        List<DataDevScriptFile> files = fileService.getDependencyFiles(dependencyId);
        for(DataDevScriptFile dFile:files){
            fileMap.put(dFile.getGitProjectFilePath(),dFile);
        }
        for (DataDevDependencyDetail detail : list) {
            DataDevScriptFile tmpFile = fileMap.get(detail.getDependencyGitProjectFilePath());
            if(tmpFile==null){
                tmpFile = fileService.getScriptByGitProjectIdAndFilePath(detail.getGitProjectId(), detail.getDependencyGitProjectFilePath());
            }
            if (tmpFile == null) {
                if (dependencyId != null) {
                    DataDevDependencyDetail oldDetail = dataDevDependencyDao.getDetailByDependencyIdAndPath(dependencyId, gitProjectId, detail.getDependencyGitProjectFilePath());
                    detail.setScriptId(oldDetail != null ? oldDetail.getScriptId() : null);
                }
                detail.setGitProjectFilePath(gitProjectFilePath);
                detail.setDependencyVersion(fileService.getNoVersion());
            } else {
                detail.setScriptId(tmpFile.getId());
                detail.setDependencyVersion(tmpFile.getVersion());
                detail.setGitProjectFilePath(gitProjectFilePath);
            }
            detail.setDependencyId(file.getDependencyId());
        }
        logger.error("更新最新版本花费时间:"+(System.currentTimeMillis()-time));
        return list;
    }

    @Override
    public byte[] packZip(List<DataDevDependencyDetail> list, String erp,Map<String,byte[]> replaceMap) throws Exception {
        if(replaceMap == null){
            replaceMap = new HashMap<>();
        }
        Long time1 = System.currentTimeMillis();
        Set<String> set = new HashSet<String>();
        BlockingQueue<DataDevDependencyDetail> blockingQueue = new LinkedBlockingQueue<>();
        DownLoadFileUtil.State state = new DownLoadFileUtil.State();
        state.setTotal(new AtomicInteger(list.size()));
        Integer nums = list.size();
        Integer threads = Math.min((nums/DownLoadFileUtil.TASK_NUM_PER_THREAD+1),20);
        state.setThreadNum(threads);
        DownLoadFileUtil.downFile(blockingQueue, state, erp);
        for (DataDevDependencyDetail detail : list) {
            // 版本号为noversion 说明脚本当时不存在 不打包
            if(!fileService.getNoVersion().equals(detail.getDependencyVersion())){
                List<String> dirList = Base64Util.getDirsOfFile("", detail.getDependencyGitProjectFilePath());
                set.addAll(dirList);
                blockingQueue.put(detail);
            }else {
                state.getTotal().getAndDecrement();
            }
        }
        state.setFinish();
        Map<String, byte[]> fileMap = state.getResult();
        for(Map.Entry<String,byte[]> entry:replaceMap.entrySet()){
            String path = entry.getKey();
            byte[] bytes = entry.getValue();
            if(fileMap.containsKey(path)){
                fileMap.put(path,bytes);
            }
        }
        Long time2 = System.currentTimeMillis();
        byte[] results = ZipUtil.compress(set, fileMap,state.getFileSize());
        Long time3 = System.currentTimeMillis();
        logger.error("=============================================下载或者缓存时间："+(time2-time1));
        logger.error("=============================================压缩文件时间："+(time3-time2));
        return results;
    }

    @Override
    public void deleteDependency(Long gitProjectId, String gitProjectFilePath) throws Exception {
        DataDevScriptFile file = new DataDevScriptFile();
        file.setDependencyId(-1L);
        fileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, file);
    }

    @Override
    public void setDisabledStatus(List<ZtreeNode> list, String gitProjectFilePath) throws Exception {
        if (StringUtils.isNotBlank(gitProjectFilePath)) {
            for (ZtreeNode ztreeNode : list) {
                String dirpath = ztreeNode.isDir() ? (ztreeNode.getPath().endsWith("/") ? ztreeNode.getPath() : (ztreeNode.getPath() + "/")) : "";
                if (gitProjectFilePath.equals(ztreeNode.getPath()) || (StringUtils.isNotBlank(dirpath) && gitProjectFilePath.startsWith(dirpath))) {
                    ztreeNode.setChkDisabled(true);
                }
            }
        }
    }

    @Override
    public List<ZtreeNode> getScriptsByDetails(List<DataDevDependencyDetail> list) throws Exception {
        final List<ZtreeNode> result = new ArrayList<>();
        Set<String> dirSet = new HashSet<>();
        Map<String,DataDevScriptFile> fileMap = new HashMap<>();
        if (list != null && list.size() > 0) {
            List<DataDevScriptFile> files = fileService.getDependencyFiles(list.get(0).getDependencyId());
            for(DataDevScriptFile file:files){
                fileMap.put(file.getGitProjectFilePath(),file);
            }
            for (DataDevDependencyDetail detail : list) {
                if(fileService.getNoVersion().equals(detail.getDependencyVersion())){
                    continue;
                }
                Integer level = 0;
                String parentPath = "";
                List<String> dirList = Base64Util.getDirsOfFile(parentPath, detail.getDependencyGitProjectFilePath());
                for (String dirPath : dirList) {
                    if (!dirSet.contains(dirPath)) {
                        ZtreeNode dirNode = new ZtreeNode();
                        dirNode.setGitProjectId(detail.getGitProjectId());
                        dirNode.setParChl(0);
                        dirNode.setPath(dirPath);
                        dirNode.setParentPath(parentPath);
                        dirNode.setTableLevel(level);
                        dirNode.setType(-1);
                        dirNode.setName(Base64Util.splitFilePath(dirPath).b);
                        dirSet.add(dirPath);
                        result.add(dirNode);
                    }
                    level++;
                    parentPath = dirPath;
                }
                DataDevScriptFile file = fileMap.get(detail.getDependencyGitProjectFilePath());
                ZtreeNode fileNode = null;
                if (file != null && (file.getDeleted() == null || file.getDeleted() == 0)) {
                    fileNode = new ZtreeNode(file);
                    fileNode.setVersion(detail.getDependencyVersion());
                    fileNode.setTableLevel(level);
                    fileNode.setLastVersion(file.getVersion());
                    fileNode.setLastModified(SIMPLE_DATE_FORMAT.format(file.getModified()));
                    String mender = file.getMender();
                    String name = urmUtil.getNameByErp(mender);
                    name = StringUtils.isNotBlank(name) ? (name + "(" + mender + ")") : mender;
                    fileNode.setModifier(name);
                    fileNode.setDeleted(0);
                } else {
                    fileNode = new ZtreeNode();
                    fileNode.setParChl(1);
                    fileNode.setPath(detail.getDependencyGitProjectFilePath());
                    fileNode.setVersion(detail.getDependencyVersion());
                    fileNode.setTableLevel(level);
                    fileNode.setLastVersion("（无法找到该文件）");
                    fileNode.setLastModified(file != null ? SIMPLE_DATE_FORMAT.format(file.getModified()) : "");
                    fileNode.setParentPath(Base64Util.splitFilePath(fileNode.getPath()).a);
                    fileNode.setName(Base64Util.splitFilePath(fileNode.getPath()).b);
                    fileNode.setType(DataDevScriptTypeEnum.getFileNameScriptType((fileNode.getName())).toCode());
                    String mender = file != null ? file.getMender() : "";
                    String name = StringUtils.isNotBlank(mender) ? urmUtil.getNameByErp(mender) : "";
                    name = StringUtils.isNotBlank(name) ? (name + "(" + mender + ")") : mender;
                    fileNode.setModifier(name);
                    fileNode.setDeleted(1);
                }
                result.add(fileNode);
            }
        }
        return result;
    }

    public void preOrderTree(ZtreeNode node, List<ZtreeNode> result) {
        if (node != null) {
            result.add(node);
            List<ZtreeNode> children = node.getChildren();
            if (children != null && children.size() > 0) {
                //先排序 目录在前
                Collections.sort(children, new Comparator<ZtreeNode>() {
                    @Override
                    public int compare(ZtreeNode o1, ZtreeNode o2) {
                        return o1.getParChl() - o2.getParChl();
                    }
                });
                for (ZtreeNode ztreeNode : children) {
                    preOrderTree(ztreeNode, result);
                }
                node.setChildren(null);
            }
        }
    }

    @Override
    public void dealDeletedFile(List<ZtreeNode> ztreeNodes, String[] selectFilePaths) throws Exception {
        if (selectFilePaths != null && selectFilePaths.length > 0) {
            Set<String> dirSet = new HashSet<>();
            Set<String> fileSet = new HashSet<>();
            for (ZtreeNode ztreeNode : ztreeNodes) {
                if (ztreeNode.isDir()) {
                    dirSet.add(ztreeNode.getPath());
                } else {
                    fileSet.add(ztreeNode.getPath());
                }
            }
            for (String filePath : selectFilePaths) {
                if (fileSet.contains(filePath)) {
                    continue;
                }
                ZtreeNode deletedFile = new ZtreeNode();
                deletedFile.setParChl(1);
                deletedFile.setPath(filePath);
                deletedFile.setParentPath(Base64Util.splitFilePath(filePath).a);
                deletedFile.setType(DataDevScriptTypeEnum.getFileNameScriptType(filePath).toCode());
                deletedFile.setDeleted(1);
                deletedFile.setSelected(1);
                deletedFile.setName(Base64Util.splitFilePath(filePath).b + "（无法找到该文件）");
                ztreeNodes.add(deletedFile);
                fileSet.add(filePath);
                List<String> dirs = Base64Util.getDirsOfFile("", filePath);
                for (String dir : dirs) {
                    if (!dirSet.contains(dir)) {
                        ZtreeNode deletedDir = new ZtreeNode();
                        deletedDir.setParChl(0);
                        deletedDir.setPath(dir);
                        deletedDir.setParentPath(Base64Util.splitFilePath(dir).a);
                        deletedDir.setDeleted(1);
                        deletedDir.setName(Base64Util.splitFilePath(dir).b);
                        dirSet.add(dir);
                        ztreeNodes.add(deletedDir);
                    }
                }
            }
        }
    }
}
