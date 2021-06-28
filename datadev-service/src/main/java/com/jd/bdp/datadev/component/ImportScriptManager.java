package com.jd.bdp.datadev.component;


import com.jd.bdp.common.utils.HttpRequestDTO;
import com.jd.bdp.common.utils.HttpResultDto;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.jdgit.JDGitMembers;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.jdgit.JDGitUser;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import net.bytebuddy.asm.Advice;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zhangrui25 on 2018/6/21.
 */
@Component
public class ImportScriptManager {

    private static Log logger = LogFactory.getLog(ImportScriptManager.class);


    @Value("${datadev.appId}")
    public String appId;

    @Value("${datadev.token}")
    public String token;

    @Value("${smp.app.id}")
    public String smpAppId;

    @Value("${smp.app.token}")
    public String smpAppToken;

    @Value("${jd.bdp.jsd.domain}")
    public String appDomain;

    @Value("${jd.bdp.script.domain}")
    public String scriptDomian;

    @Value("${buffalo.domain.name}")
    private String buffalo4Prefix;

    public static final Integer OWNER = 50;

    public static final Integer MASTER = 40;

    public static final Integer DEVELOPER = 30;

    @Autowired
    public DataDevScriptPublishService dataDevScriptPublishService;

    @Autowired
    public DataDevScriptDirService dataDevScriptDirService;

    @Autowired
    public DataDevScriptFileService dataDevScriptFileService;

    @Autowired
    public DataDevGitProjectMemberService dataDevGitProjectMemberService;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private UrmUtil urmUtil;


    private static final String SYNC_APPGROUP_ID_KEY = "%s_bdp_data_dev_sync_appgroup_key_%s";

    @Autowired
    private Cluster jimClient;

    @Autowired
    private BuffaloComponent buffaloComponent;

    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao;

    @Autowired
    private ImportScriptManagerRedis importScriptManagerRedis;

    public static void main(String args[]) throws Exception {
        //   getAppGroupMemebers(10075L);
        // getScriptList(10075L);
    }


    /**
     * 清除同步
     *
     * @param appGroupId
     */
    public void clearSync(Long appGroupId) {
        importScriptManagerRedis.endImport(appGroupId);
    }

    /**
     * 获取RedisKey
     *
     * @param appGroupId
     * @return
     */
    public String getSyncRedisValue(Long appGroupId) {
        return importScriptManagerRedis.current(appGroupId);
    }

    /**
     * 1.check 当前项目空间下的文件是否 已经和 这次需要导入的脚本列表是否重复
     * 2.同步脚本
     * 3.同步脚本中心的人员
     * 同步脚本中心的数据到数据开发平台
     */
    @Deprecated
    public com.alibaba.fastjson.JSONObject syncScriptToDataDev(final Long gitProjectId, final String dirPath, final Long appGroupId, final String erp, final boolean syncMember, String fileName, Long fileId, String version, boolean isSync) throws Exception {
        String syncKey = String.format(SYNC_APPGROUP_ID_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), appGroupId);

        if (StringUtils.isNotBlank(jimClient.get(syncKey))) {
            throw new RuntimeException("当前项目空间正在同步!.");
        }

        final JSONArray managerScriptFiles = filterSynced(getScriptList(appGroupId, erp, fileId));
        if (managerScriptFiles == null || managerScriptFiles.size() < 1) {
            throw new RuntimeException("当前项目空间无待同步脚本.");
        }
        //创建目录
        if (StringUtils.isNotBlank(dirPath)) {
            dataDevScriptDirService.createScriptDir(gitProjectId, dirPath, erp);
        }
        //检查文件是否重复
        checkScriptFileExits(managerScriptFiles, gitProjectId, dirPath);

        final com.alibaba.fastjson.JSONObject valueObject = new com.alibaba.fastjson.JSONObject();

        jimClient.set(syncKey, valueObject.toString());
        valueObject.put("currentIndex", 1);
        valueObject.put("currentFile", managerScriptFiles.getJSONObject(0).getString("fileName"));
        valueObject.put("total", managerScriptFiles.size());
        valueObject.put("failCount", 0);
        valueObject.put("successCount", 0);
        valueObject.put("gitProjectId", gitProjectId);
        if (isSync) {
            List<ZtreeNode> ztreeNodeList = handSyncScript(gitProjectId, appGroupId, managerScriptFiles, dirPath, erp, valueObject);
            syncMember(gitProjectId, appGroupId, syncMember);
            valueObject.put("ztreeNodeList", ztreeNodeList);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //同步脚本
                    handSyncScript(gitProjectId, appGroupId, managerScriptFiles, dirPath, erp, valueObject);
                    //同步人员
                    syncMember(gitProjectId, appGroupId, syncMember);
                }
            }).start();
        }
        return valueObject;
    }


    /**
     * 1.check 当前项目空间下的文件是否 已经和 这次需要导入的脚本列表是否重复
     * 2.同步脚本
     * 3.同步脚本中心的人员
     * 同步脚本中心的数据到数据开发平台
     */
    public JSONObject syncScriptToDataDevNew(final Long gitProjectId, final Long appGroupId, final String erp) throws Exception {


        final JSONArray managerScriptFiles = buffaloComponent.getScriptListNew(appGroupId);  // getScriptList(appGroupId, erp, 0L);
        if (managerScriptFiles == null || managerScriptFiles.size() < 1) {
            throw new RuntimeException("当前项目空间无待同步脚本.");
        }
        if (!importScriptManagerRedis.isCanSyncProjectId(appGroupId)) {
            throw new RuntimeException("当前项目空间正在同步!.");
        }

        //创建目录
        String startImport = importScriptManagerRedis.startImport(appGroupId, managerScriptFiles.size());

        new Thread(new Runnable() {
            @Override
            public void run() {
                //同步脚本
                handSyncScriptLocal(gitProjectId, appGroupId, managerScriptFiles, erp);
            }
        }).start();

        return JSONObject.fromObject(startImport);
    }

    /**
     * 过滤掉已经通过的 文件
     *
     * @param managerScriptFiles
     * @return
     */
    private JSONArray filterSynced(JSONArray managerScriptFiles) {
        JSONArray result = new JSONArray();
        for (int index = 0; index < managerScriptFiles.size(); index++) {
            JSONObject temp = managerScriptFiles.getJSONObject(index);
            if (temp.containsKey("dataDevProjectId") && temp.get("dataDevProjectId") != null) {
                continue;
            }
            result.add(temp);
        }
        return result;
    }

    public void syncMember(Long gitProjectId, Long appGroupId, Boolean syncMember) {

        if (syncMember) {
            List<HoldTreeValue<String, String, Long>> erpAndNameAndUserId = getSyncProjectMembers(gitProjectId, appGroupId);
            if (erpAndNameAndUserId != null && erpAndNameAndUserId.size() > 0) {
                JDGitProjects jdGitProjects = new JDGitProjects();
                jdGitProjects.setGitProjectId(gitProjectId);
                JDGitMembers jdGitMembers = new JDGitMembers();
                for (HoldTreeValue<String, String, Long> treeValue : erpAndNameAndUserId) {
                    try {
                        jdGitMembers.setGitProjectId(gitProjectId);
                        jdGitMembers.setGitUserId(treeValue.c);
                        jdGitMembers.setAccessLevel(ImportScriptManager.DEVELOPER);
                        jdGitMembers.setName(treeValue.a);
                        jdGitProjects.addProjectMember(jdGitMembers);

                        DataDevGitProjectMember member = new DataDevGitProjectMember();
                        member.setGitProjectId(gitProjectId);
                        member.setAccessLevel(ImportScriptManager.DEVELOPER);
                        member.setGitMemberId(treeValue.c);
                        member.setGitMemberName(treeValue.a);
                        member.setState("active");
                        member.setGitMemberUserName(treeValue.a);
                        dataDevGitProjectMemberService.insert(Arrays.asList(member));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }
    }

    /**
     * 同步脚本
     * <p>
     * 同时给Redis里面写入值{currentIndex , currentFile, total , failCount , successCount}
     */
    private List<ZtreeNode> handSyncScript(final Long gitProjectId, final Long appGroupId, final JSONArray managerScriptFiles, final String dirPath, final String erp, final com.alibaba.fastjson.JSONObject valueObject) {
        String syncKey = String.format(SYNC_APPGROUP_ID_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), appGroupId);
        List<ZtreeNode> ztreeNodeList = new ArrayList<ZtreeNode>();
        try {
            DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);


            for (int index = 0; index < managerScriptFiles.size(); index++) {
                //修改Redis
                valueObject.put("currentIndex", (index + 1));
                valueObject.put("currentFile", managerScriptFiles.getJSONObject(index).getString("fileName"));
                jimClient.set(syncKey, valueObject.toString());
                ZtreeNode ztreeNode = handSyncScriptSingle(dataDevGitProject, valueObject, managerScriptFiles.getJSONObject(index), appGroupId, dirPath, erp);
                ztreeNodeList.add(ztreeNode);
            }
            return ztreeNodeList;
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
//                Thread.sleep(1000 * 8);
                jimClient.del(syncKey);
            } catch (Exception e) {
            }
        }
        return ztreeNodeList;
    }

    private List<ZtreeNode> handSyncScriptLocal(final Long gitProjectId, final Long appGroupId, final JSONArray managerScriptFiles, String erp) {
        List<ZtreeNode> ztreeNodeList = new ArrayList<ZtreeNode>();

        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        for (int index = 0; index < managerScriptFiles.size(); index++) {
            try {
                Thread.sleep(1000);
                JSONObject obj = managerScriptFiles.getJSONObject(index);
                ZtreeNode ztreeNode = handSyncScriptSingleLocal(dataDevGitProject, obj, appGroupId, erp);
                ztreeNodeList.add(ztreeNode);
            } catch (Exception e) {
                logger.error("handSyncScriptLocal", e);
            }
        }
      //  importScriptManagerRedis.endImport(appGroupId);

        return ztreeNodeList;

    }


    /**
     * 下载脚本
     *
     * @param erp
     * @param fileId
     * @param version
     * @return
     * @throws Exception
     */
    public byte[] loadFile(String erp, Long fileId, String version) throws Exception {
        HttpRequestDTO dto = new HttpRequestDTO();
        String url = scriptDomian + "/api/v2/buffalo4/script/downloadScript";
        dto.setAppId(smpAppId);
        dto.setToken(smpAppToken);
        dto.setTime(System.currentTimeMillis());
        JSONObject data = new JSONObject();
        data.put("fileId", fileId);
        if (StringUtils.isNotBlank(version) && !version.equalsIgnoreCase("null")) {
            data.put("version", version);
        }
        dto.setData(data);
        return HttpClientUtil.getStream(url, dto, erp);
    }

    /**
     * 处理单个脚本完成的时候同步redis
     * 3.git同步脚本
     * 2.hbase同步脚本
     * <p>
     * 1.数据库创建脚本
     * 4.大文件不同步
     */
    private ZtreeNode handSyncScriptSingle(DataDevGitProject dataDevGitProject, com.alibaba.fastjson.JSONObject redisValue, JSONObject script, Long appGroupId, String dirPath, String erp) {
        String syncKey = String.format(SYNC_APPGROUP_ID_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), appGroupId);
        ZtreeNode ztreeNode = null;
        try {
            erp = script.get("creator") != null ? script.getString("creator") : erp;
            String fileName = script.getString("fileName");
            Long gitProjectId = dataDevGitProject.getGitProjectId();
            Integer scriptType = DataDevScriptTypeEnum.getFileNameScriptType(fileName).toCode();
            byte[] bytes = loadFile(erp, script.getLong("fileId"), script.getString("version"));
            if (bytes == null || bytes.length < 1) {
                bytes = "".getBytes();
            }
            String gitProjectFilePath = StringUtils.isNotBlank(dirPath) ? (dirPath + "/" + fileName) : fileName;
            String description = script.get("description") != null ? script.get("description").toString() : "";
            String startShellPath = script.get("scriptPackagePath") != null ? script.get("scriptPackagePath").toString() : "";
            /**
             * 插入数据库,hbase
             */
            ztreeNode = dataDevScriptFileService.createNewFile(gitProjectId, gitProjectFilePath, scriptType, erp, 0, bytes, description, startShellPath);
            Long size = (long) bytes.length;
            if (SplitFileUtils.isBigFile(size) == 0) {
                /**
                 * 同步到Git 大文件不同步到Git
                 */
                try {
                    dataDevScriptFileService.createGitFile(dataDevGitProject, fileName, gitProjectFilePath, erp, scriptType, bytes);

                } catch (Exception e) {
                    logger.error("======================createGitFile:" + e.getMessage());
                }
            }
            callBackScript(script, gitProjectId, gitProjectFilePath, erp, null);
            redisValue.put("successCount", redisValue.getIntValue("successCount") + 1);

            insertPublish(erp, script, dataDevGitProject, gitProjectFilePath);
            return ztreeNode;
        } catch (Exception e) {
            redisValue.put("failCount", redisValue.getIntValue("failCount") + 1);
            logger.error(e);
        } finally {
            jimClient.set(syncKey, redisValue.toString());
        }
        return ztreeNode;
    }

    private ZtreeNode handSyncScriptSingleLocal(DataDevGitProject dataDevGitProject, JSONObject script, Long appGroupId, String erp) {
        ZtreeNode ztreeNode = null;
        try {
            erp = script.get("managers") != null ? script.getString("managers") : erp;
            String fileName = script.getString("fileName");
            Long gitProjectId = dataDevGitProject.getGitProjectId();
            Integer scriptType = DataDevScriptTypeEnum.getFileNameScriptType(fileName).toCode();
            byte[] bytes = loadFile(erp, script.getLong("fileId"), script.getString("curVersion"));
            if (bytes == null || bytes.length < 1) {
                bytes = "".getBytes();
               // throw new RuntimeException("empty file");
            }
            String gitProjectFilePath = StringUtils.isNotBlank(erp) ? (erp + "/" + fileName) : fileName;
            String description = script.get("description") != null ? script.get("description").toString() : "";
            String startShellPath = script.get("scriptPackagePath") != null ? script.get("scriptPackagePath").toString() : "";
            /**
             * 插入数据库,hbase
             */

            dataDevScriptFileDao.realDeleteSingleScriptFile(gitProjectId, gitProjectFilePath);

            ztreeNode = dataDevScriptFileService.createNewFile(gitProjectId, gitProjectFilePath, scriptType, erp, 0, bytes, description, startShellPath);
            callBackScriptNew(script, gitProjectId, gitProjectFilePath, erp, null);

            importScriptManagerRedis.increaseSuccess(appGroupId);
            insertPublish(erp, script, dataDevGitProject, gitProjectFilePath);


            return ztreeNode;
        } catch (Exception e) {
            importScriptManagerRedis.increaseFailed(appGroupId);
            logger.error(e);
        } finally {
        }
        return ztreeNode;
    }

    /**
     * 处理单个脚本完成的时候同步redis
     * 3.git同步脚本
     * 2.hbase同步脚本
     * <p>
     * 1.数据库创建脚本
     * 4.大文件不同步
     */
    private ZtreeNode handSyncScriptSingleNew(DataDevGitProject dataDevGitProject, com.alibaba.fastjson.JSONObject redisValue, JSONObject script, Long appGroupId, String dirPath, String erp) {
        String syncKey = String.format(SYNC_APPGROUP_ID_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), appGroupId);
        ZtreeNode ztreeNode = null;
        try {
            erp = script.get("creator") != null ? script.getString("creator") : erp;
            String fileName = script.getString("fileName");
            Long gitProjectId = dataDevGitProject.getGitProjectId();
            Integer scriptType = DataDevScriptTypeEnum.getFileNameScriptType(fileName).toCode();
            byte[] bytes = loadFile(erp, script.getLong("fileId"), script.getString("curVersion"));
            if (bytes == null || bytes.length < 1) {
                throw new RuntimeException("empty file");
            }
            String gitProjectFilePath = StringUtils.isNotBlank(dirPath) ? (dirPath + "/" + fileName) : fileName;
            String description = script.get("description") != null ? script.get("description").toString() : "";
            String startShellPath = script.get("scriptPackagePath") != null ? script.get("scriptPackagePath").toString() : "";
            /**
             * 插入数据库,hbase
             */
            ztreeNode = dataDevScriptFileService.createNewFile(gitProjectId, gitProjectFilePath, scriptType, erp, 0, bytes, description, startShellPath);
            Long size = (long) bytes.length;
            if (SplitFileUtils.isBigFile(size) == 0) {
                /**
                 * 同步到Git 大文件不同步到Git
                 */
                try {
                    dataDevScriptFileService.createGitFile(dataDevGitProject, fileName, gitProjectFilePath, erp, scriptType, bytes);
                } catch (Exception e) {
                    logger.error("======================createGitFile:" + e.getMessage());
                }
            }
            callBackScriptNew(script, gitProjectId, gitProjectFilePath, erp, null);
            redisValue.put("successCount", redisValue.getIntValue("successCount") + 1);

            insertPublish(erp, script, dataDevGitProject, gitProjectFilePath);
            return ztreeNode;
        } catch (Exception e) {
            redisValue.put("failCount", redisValue.getIntValue("failCount") + 1);
            logger.error(e);
        } finally {
            jimClient.set(syncKey, redisValue.toString());
        }
        return ztreeNode;
    }

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param scriptId           buffalo脚本id
     * @param scriptName
     * @param content
     * @param erp
     * @return
     */
    public ZtreeNode syncScriptByMerge(Long gitProjectId, String gitProjectFilePath, Long scriptId, String scriptName, String content, String erp) {
        ZtreeNode ztreeNode = null;
        try {
            Integer scriptType = DataDevScriptTypeEnum.getFileNameScriptType(scriptName).toCode();
            byte[] bytes;
            if (content != null) {
                bytes = content.getBytes("utf-8");
            } else {
                bytes = loadFile(erp, scriptId, null);
            }
            if (bytes == null || bytes.length < 1) {
                bytes = new byte[0];
            }
            JSONObject jsonObject = getOneScript(erp, scriptId);
            DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
            String description = jsonObject.get("description") != null ? jsonObject.get("description").toString() : "";
            String startShellPath = jsonObject.get("scriptPackagePath") != null ? jsonObject.get("scriptPackagePath").toString() : "";
            /**
             * 插入数据库,hbase
             */
            HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = dataDevScriptFileService.tryUpdateFile(gitProjectId, gitProjectFilePath, erp, bytes, null, null, true, description, startShellPath, null);
            ztreeNode = dataDevScriptDirService.createFilePathZtreeNode(gitProjectId, gitProjectFilePath, null);
            Long size = (long) bytes.length;
            if (SplitFileUtils.isBigFile(size) == 0) {
                /**
                 * 同步到Git 大文件不同步到Git
                 */
                try {
                    dataDevScriptFileService.createGitFile(dataDevGitProject, scriptName, gitProjectFilePath, erp, scriptType, bytes);
                } catch (Exception e) {
                    logger.error("======================createGitFile:" + e.getMessage());
                }
            }
            callBackScript(jsonObject, gitProjectId, gitProjectFilePath, erp, null);
            insertPublish(erp, jsonObject, dataDevGitProject, gitProjectFilePath, holdDoubleValue.b.getVersion(), 0, "");
            return ztreeNode;
        } catch (Exception e) {
            logger.error(e);
        } finally {
        }
        return ztreeNode;
    }

    private void insertPublish(String erp, JSONObject script, DataDevGitProject dataDevGitProject, String gitProjectFilePath) {
        insertPublish(erp, script, dataDevGitProject, gitProjectFilePath, "1000", 0, null);
    }

    private void insertPublish(String erp, JSONObject script, DataDevGitProject dataDevGitProject, String gitProjectFilePath, String version, Integer runType, String gitProjectDirPath) {
        DataDevScriptFilePublish insertPublish = new DataDevScriptFilePublish();
        insertPublish.setGitProjectId(dataDevGitProject.getGitProjectId());
        insertPublish.setGitProjectFilePath(gitProjectFilePath);
        insertPublish.setStatus(DataDevScriptPublishStatusEnum.Success.toCode());
        insertPublish.setApplicationId(script.getLong("jsdAppgroupId"));
        insertPublish.setComment(script.get("verDescription") != null ? script.get("verDescription").toString() : "--");
        insertPublish.setVersion(version);
        insertPublish.setPublisher(erp);
        insertPublish.setPublishSys("buffalo");
        insertPublish.setCreator(erp);
        insertPublish.setMender(erp);
        insertPublish.setRunType(runType);
        insertPublish.setBuffaloScriptId(script.getLong("fileId"));
        String buffaloVersion = script.get("curVersion") != null ? script.get("curVersion").toString() : (script.get("version") != null ? script.get("version").toString() : "0");
        insertPublish.setBuffaloScriptVersion(buffaloVersion);
        dataDevScriptPublishService.insert(insertPublish);
    }

    /**
     * 更新回脚本中西
     *
     * @param script
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @throws Exception
     */
    private void callBackScript(JSONObject script, Long gitProjectId, String gitProjectFilePath, String erp, String version) throws Exception {
        callBackScript(script.getLong("fileId"), script.getString("version"), gitProjectId, gitProjectFilePath, erp, version);
    }


    /**
     * @param fileId
     * @param buffaloVersion
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @param version
     * @throws Exception
     */
    public void callBackScript(Long fileId, String buffaloVersion, Long gitProjectId, String gitProjectFilePath, String erp, String version) throws Exception {
        HttpRequestDTO dto = new HttpRequestDTO();
        String url = scriptDomian + "/api/v2/buffalo4/script/updateScriptVersion";
        dto.setAppId(smpAppId);
        dto.setToken(smpAppToken);
        dto.setTime(System.currentTimeMillis());
        JSONObject data = new JSONObject();
        data.put("fileId", fileId);
//        data.put("version", buffaloVersion);
        data.put("dataDevProjectId", gitProjectId);
        data.put("dataDevProjectFilePath", gitProjectFilePath);
        data.put("dataDevScriptVersion", StringUtils.isBlank(version) ? 1000L : version);

        dto.setData(data);
        JSONObject httpResultDto = HttpClientUtil.doGet(url, dto, erp);
        if (httpResultDto.getInt("code") != 0) {
            throw new RuntimeException(httpResultDto.getString("message"));
        }
    }


    private void callBackScriptNew(JSONObject script, Long gitProjectId, String gitProjectFilePath, String erp, String version) throws Exception {
        Long fileId = script.getLong("fileId");
        String curVersion = script.getString("curVersion");
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);

        buffaloComponent.syncBuffloScriptCallback(fileId, curVersion, dataDevScriptFile, version, dataDevScriptFile.getFileMd5());
    }


    /**
     * 删除脚本
     * https://cf.jd.com/pages/viewpage.action?pageId=521864137
     *
     * @param dataDevScriptFile
     * @param jsdAppgroupId
     * @param erp
     * @throws Exception
     */
    public JSONObject deleteBuffaloScript(DataDevScriptFile dataDevScriptFile, Long jsdAppgroupId, String erp) throws Exception {
        Long gitProjectId = dataDevScriptFile.getGitProjectId();
        String gitProjectFilePath = dataDevScriptFile.getGitProjectFilePath();

        HttpRequestDTO dto = new HttpRequestDTO();
        String url = scriptDomian + "/api/v2/buffalo4/script/deleteScript";
        dto.setAppId(smpAppId);
        dto.setToken(smpAppToken);
        dto.setTime(System.currentTimeMillis());
        JSONObject data = new JSONObject();

//        data.put("jsdAppgroupId", jsdAppgroupId);
        data.put("dataDevProjectId", gitProjectId);
        data.put("dataDevProjectFilePath", gitProjectFilePath);
        if (dataDevScriptFile.getType().equals(DataDevScriptTypeEnum.SQL.toCode())) {
            data.put("model", "006");
        }
        dto.setData(data);
        JSONObject httpResultDto = HttpClientUtil.doGet(url, dto, erp);
        if (httpResultDto.getInt("code") != 0) {
            throw new RuntimeException("项目空间:" + httpResultDto.getString("message"));
        }
        return httpResultDto;
    }

    /**
     * 检查脚本是否已经存在
     *
     * @param managerScriptFiles
     * @param gitProjectId
     * @param dirPath
     */
    private void checkScriptFileExits(JSONArray managerScriptFiles, Long gitProjectId, String dirPath) {

        List<DataDevScriptFile> scriptFiles = dataDevScriptFileService.getScriptsByGitProjectIdAndDirPath(gitProjectId, dirPath);
        if (scriptFiles != null && scriptFiles.size() > 0) {
            for (DataDevScriptFile temp : scriptFiles) {
                if (isFileInScriptManager(temp.getName(), managerScriptFiles)) {
                    throw new RuntimeException(temp.getName() + "已经存在,删除后在同步!");
                }
            }
        }
    }

    /**
     * 文件是否在脚本中心存在
     *
     * @param name
     * @param managerScriptFiles
     * @return
     */
    private boolean isFileInScriptManager(String name, JSONArray managerScriptFiles) {
        if (managerScriptFiles != null && managerScriptFiles.size() > 0) {
            for (int index = 0; index < managerScriptFiles.size(); index++) {
                if (managerScriptFiles.getJSONObject(index).getString("fileName").equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取需要同步到project的人员
     * HoldTreeValue erp,userName ,userGitId
     *
     * @param appGroupId
     * @param gitProjectId
     * @return
     */
    public List<HoldTreeValue<String, String, Long>> getSyncProjectMembers(Long gitProjectId, Long appGroupId) {
        List<HoldTreeValue<String, String, Long>> result = new ArrayList<HoldTreeValue<String, String, Long>>();
        try {
            List<String> erps = getNeedAddProjectMember(gitProjectId, appGroupId);
            if (erps != null && erps.size() > 0) {
                for (String erp : erps) {
                    try {
                        JDGitUser jdGitUser = new JDGitUser();
                        jdGitUser.setName(erp);
                        jdGitUser.setGitProjectId(gitProjectId);
                        JDGitUser searchJdUser = jdGitUser.searchUser();
                        if (searchJdUser == null) {
                            throw new RuntimeException("用户[" + erp + "在git未激活，请在<a href='http://git.jd.com'target='blank'>git.jd.com</a>登录一次进行激活！");
                        }

                        result.add(new HoldTreeValue<String, String, Long>(erp, null, searchJdUser.getId()));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
            /**
             * 添加中文名
             */
            urmUtil.covertUserErp2UserName(result, new ConvertErp2UserName<HoldTreeValue<String, String, Long>>() {
                @Override
                public String getErp(HoldTreeValue<String, String, Long> stringStringLongHoldTreeValue) {
                    return stringStringLongHoldTreeValue.a;
                }

                @Override
                public void setErpUserName(HoldTreeValue<String, String, Long> stringStringLongHoldTreeValue, String userNames) {
                    stringStringLongHoldTreeValue.b = userNames;
                }
            });
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }

    /**
     * * 获取需要添加ProjectMember
     *
     * @param gitProjectId
     * @param appGroupId
     * @return
     * @throws Exception
     */
    private List<String> getNeedAddProjectMember(Long gitProjectId, Long appGroupId) throws Exception {
        List<String> erps = getAppGroupMemebers(appGroupId);
        List<DataDevGitProjectMember> exitsErps = dataDevGitProjectMemberService.findAll(gitProjectId);
        List<String> needAddProjectMember = new ArrayList<String>();

        for (String erp : erps) {
            boolean hasExits = false;
            for (DataDevGitProjectMember projectMember : exitsErps) {
                if (projectMember.getGitMemberName().equals(erp)) {
                    hasExits = true;
                    break;
                }
            }
            if (!hasExits) {
                needAddProjectMember.add(erp);
            }
        }
        return needAddProjectMember;
    }

    public JSONObject getOneScript(String erp, Long fileId) throws Exception {
        HttpRequestDTO dto = new HttpRequestDTO();
        String oneUrl = scriptDomian + "/api/v2/buffalo4/script/getScriptFileInfoById";
        logger.error("================oneUrl" + oneUrl);
        dto.setAppId(smpAppId);
        dto.setToken(smpAppToken);
        dto.setTime(System.currentTimeMillis());
        JSONObject data1 = new JSONObject();
        data1.put("fileId", fileId);
        dto.setData(data1);
        JSONObject resDto = HttpClientUtil.doGet(oneUrl, dto, erp);
        if (resDto.get("code") != null && resDto.getInt("code") == 0) {
            JSONObject temp = resDto.getJSONObject("obj");
            // fileName,version,fileId,jsdAppgroupId,curVersion
            return temp;
        } else {
            throw new RuntimeException(resDto.get("message") != null ? resDto.getString("message") : "获取调度脚本详情失败");
        }
    }

    /**
     * 获取某个项目空间下面的脚本
     *
     * @param appGroupId
     * @param erp
     * @throws Exception
     */
    public JSONArray getScriptList(Long appGroupId, String erp, Long fileId) throws Exception {
        //单独一个文件的处理
        if (fileId != null && fileId > 0L) {
            JSONArray result = new JSONArray();
            result.add(getOneScript(erp, fileId));
            return result;
        }
        HttpRequestDTO dto = new HttpRequestDTO();
        String url = scriptDomian + "/api/v2/buffalo4/script/getScriptList";
        dto.setAppId(smpAppId);
        dto.setToken(smpAppToken);
        dto.setTime(System.currentTimeMillis());
        JSONObject data = new JSONObject();
        data.put("jsdAppgroupId", appGroupId);
        dto.setData(data);
        JSONObject httpResultDto = HttpClientUtil.doGet(url, dto, erp);
        if (httpResultDto.getInt("code") == 0) {
            if (httpResultDto.get("list") != null) {
                return httpResultDto.getJSONArray("list");
            } else {
                return new JSONArray();
            }
        } else {
            throw new RuntimeException(httpResultDto.getString("message"));
        }
    }


    /**
     * 获取项目空间人员
     *
     * @param appGroupId
     * @return
     * @throws Exception
     */
    private List<String> getAppGroupMemebers(Long appGroupId) throws Exception {
        HttpRequestDTO dto = new HttpRequestDTO();
        String url = appDomain + "/api/jsd/appgroup/memberlist";

        dto.setAppId(appId);
        dto.setToken(token);
        dto.setTime(System.currentTimeMillis());

        JSONObject data = new JSONObject();
        data.put("appgroupId", appGroupId);
        dto.setData(data);
        HttpResultDto httpResultDto = HttpClientUtil.request(url, dto);
        if (httpResultDto.getCode() == 0) {
            System.out.println(httpResultDto.getList());
            return httpResultDto.getList();
        }
        return null;
    }


}
