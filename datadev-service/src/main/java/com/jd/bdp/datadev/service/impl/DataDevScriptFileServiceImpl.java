package com.jd.bdp.datadev.service.impl;

import SQLinForm_200.SQLForm;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.HbaseScript;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.component.ZipUtil;
import com.jd.bdp.datadev.dao.*;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevHisTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptFileWhereIsNewEnum;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.exception.FileNotFoundException;
import com.jd.bdp.datadev.exception.GitFileNotFoundException;
import com.jd.bdp.datadev.jdgit.Base64Util;
import com.jd.bdp.datadev.jdgit.JDGitCommits;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.jdgit.JDGitRepositories;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.MD5Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DataDevScriptFileServiceImpl implements DataDevScriptFileService, InitializingBean {


    private static final Logger logger = Logger.getLogger(DataDevScriptFileServiceImpl.class);
    private static final SimpleDateFormat TEMP_NOW_TIME_FORMATE = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Long DEFAULT_PROJECT_ID = 23057L;
    @Value("${default.erp}")
    public String USERTOKEN_ERP;//传给客户端下载
    private static String SYSTEM_TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
    private static String TMP_DIR_PATH = StringUtils.isNotBlank(SYSTEM_TMP_DIR_PATH) ? (SYSTEM_TMP_DIR_PATH.endsWith(File.separator) ? SYSTEM_TMP_DIR_PATH : (SYSTEM_TMP_DIR_PATH + File.separator)) : "";
    private static String DATA_DEV_TMP_DIR_PATH = TMP_DIR_PATH + "datadevtmp" + File.separator;
    private static Pattern FILE_STATUS_PATTERN = Pattern.compile("^(\\S+)_(\\d+)$");
    private static Integer TMP_FILE_LIVE_TIME = 1000 * 60 * 60 * 24 * 1;//7天
    private static Integer TMP_FILE_WRITING_STSUTS = 0;
    private static Integer TMP_FILE_WRITEDONE_STSUTS = 1;
    private static String TMP_SEPARATOR = "_";
    private static ConcurrentHashMap<String, ReentrantReadWriteLock> tmpLockMap = new ConcurrentHashMap<>();
    private static String NO_VERSION = "-1";
    private static Integer PER_INSERT_SIZE = 100;


    private static Integer MAX_FILE_SIZE = 1024 * 1024 * 10; //10m
    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao;
    @Autowired
    private DataDevScriptPublishDao publishDao;
    @Autowired
    private HbaseScript hbaseScript;
    @Autowired
    private UrmUtil urmUtil;

    @Autowired
    private DataDevScriptDirService dataDevScriptDirService;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private DataDevScriptFileHisDao dataDevScriptFileHisDao;

    @Autowired
    private ImportScriptManager importScriptManager;

    @Autowired
    private DataDevScriptPublishService dataDevScriptPublishService;

    @Autowired
    private DataDevScriptRunDetailService dataDevScriptRunDetailService;

    @Autowired
    private DataDevDependencyService devDependencyService;

    @Autowired
    private DataDevGitHisDao dataDevGitHisDao;

    @Autowired
    private DataDevScriptRunDetailDao dataDevScriptRunDetailDao;

    @Override
    public Long getDefaultProjectId() throws Exception {
        return DEFAULT_PROJECT_ID;
    }

    @Override
    public String getDefaultUserToken() throws Exception {
        return USERTOKEN_ERP;
    }

    @Override
    public DataDevScriptFile findById(Long id) throws Exception {
        return dataDevScriptFileDao.findById(id);
    }

    @Override
    public DataDevScriptFile findTmpByRelationDependencyId(Long relationDependencyId) {
        return dataDevScriptFileDao.findTmpByRelationDependencyId(relationDependencyId);
    }

    @Override
    public void checkAndTransferScriptParam(Script script) throws Exception {
        if (StringUtils.isNotBlank(script.getGitProjectFilePath())) {
            DataDevScriptFile oldFile = dataDevScriptFileDao.getSingleScriptFile(script.getGitProjectId(), script.getGitProjectFilePath());
            if (oldFile == null) {
                throw new RuntimeException("gitProjectId:" + script.getGitProjectId() + ":" + script.getGitProjectFilePath() + "的脚本不存在");
            }
        }
        if (script.getType() == null) {
            throw new RuntimeException("脚本类型字段：type不能为空");
        } else {
            DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(script.getType());
            if (typeEnum == null) {
                throw new RuntimeException("脚本类型不存在");
            }
        }
        if (StringUtils.isBlank(script.getName())) {
            throw new RuntimeException("脚本名字不能为空");
        }
        if (script.getType() == DataDevScriptTypeEnum.Zip.toCode() && StringUtils.isBlank(script.getStartShellPath())) {
            throw new RuntimeException("startShellPath  must not be null when script'type is zip");
        }
    }


    @Override
    public List<DataDevScriptFile> findScriptsByFilter(DataDevScriptFile file) throws Exception {
        return dataDevScriptFileDao.findScriptsByFilter(file);
    }


    @Override
    public ZtreeNode copyScriptFile(DataDevScriptFile file) throws Exception {
        DataDevScriptFile oldFile = getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("脚本不存在");
        }
        byte[] res = getScriptBytes(file.getGitProjectId(), file.getGitProjectFilePath(), null, file.getCreator());
        String noSuffixPath = DataDevScriptTypeEnum.getNoSuffixName(file.getGitProjectFilePath());
        String suffix = file.getGitProjectFilePath().substring(noSuffixPath.length());
        noSuffixPath += "_copy";
        List<DataDevScriptFile> list = dataDevScriptFileDao.findScriptsByFuzzy(file.getGitProjectId(), noSuffixPath);
        if (list != null && list.size() > 0) {
            Long max = 0L;
            int length = noSuffixPath.length();
            for (DataDevScriptFile tmp : list) {
                if (StringUtils.isNotBlank(tmp.getGitProjectFilePath()) && tmp.getGitProjectFilePath().length() > length) {
                    Pattern r = Pattern.compile("^_(\\d+)" + suffix + "$");
                    Matcher m = r.matcher(tmp.getGitProjectFilePath().substring(length));
                    if (m.find()) {
                        Long index = Long.parseLong(m.group(1));
                        max = Math.max(index, max);
                    }
                }
            }
            max++;
            noSuffixPath += "_" + max;
        }
        String args = oldFile.getArgs();
        return createNewFile(oldFile.getGitProjectId(), noSuffixPath + suffix, oldFile.getType(), oldFile.getCreator(), 0, res, oldFile.getDescription(), oldFile.getStartShellPath(), null, args, null, null);
    }

    /**
     * 获取相似文件的后缀最大数
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @Override
    public Long getMaxFileCount(Long gitProjectId, String gitProjectFilePath) throws Exception {
        String noSuffixPath = DataDevScriptTypeEnum.getNoSuffixName(gitProjectFilePath);
        String suffix = gitProjectFilePath.substring(noSuffixPath.length());
        List<DataDevScriptFile> list = dataDevScriptFileDao.findScriptsByFuzzy(gitProjectId, noSuffixPath);
        if (list != null && list.size() > 0) {
            Long max = 0L;
            int length = noSuffixPath.length();
            for (DataDevScriptFile tmp : list) {
                if (StringUtils.isNotBlank(tmp.getGitProjectFilePath()) && tmp.getGitProjectFilePath().length() > length) {
                    Pattern r = Pattern.compile("^_(\\d+)" + suffix + "$");
                    String sss = tmp.getGitProjectFilePath().substring(length);
                    Matcher m = r.matcher(sss);
                    if (m.find()) {
                        Integer index = Integer.parseInt(m.group(1));
                        max = Math.max(index, max);
                    }
                }
            }
            max++;
            return max;
        }
        return 0L;
    }

    @Override
    public PageResultDTO list4page(DataDevScriptFileHis his, Pageable pageable) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        Long count = 0L;
        DataDevScriptFile file = dataDevScriptFileDao.getSingleScriptFile(his.getGitProjectId(), his.getGitProjectFilePath());
        if (file == null) {
            throw new RuntimeException("脚本不存在");
        }
        his.setFileId(file.getId());
        List<DataDevScriptFileHis> list = new ArrayList<DataDevScriptFileHis>();
        count = dataDevScriptFileHisDao.count4page(his);
        if (count > 0) {
            his.setStart(pageable.getOffset());
            his.setLimit(pageable.getPageSize());
            list = dataDevScriptFileHisDao.list4page(his);
            for (DataDevScriptFileHis tmp : list) {
                tmp.setScriptOperatorTypeStr(DataDevHisTypeEnum.enumValueOf(tmp.getScriptOperatorType()).toDesc());
                if (StringUtils.isNotBlank(tmp.getCreator())) {
                    String name = urmUtil.getNameByErp(tmp.getCreator());
                    if (StringUtils.isNotBlank(name)) {
                        tmp.setCreator(name + "(" + tmp.getCreator() + ")");
                    }
                }
            }
        }
        pageResultDTO.setRecords(count);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(list);
        return pageResultDTO;
    }


    @Override
    public String formatSqlToSrc(String sql) throws Exception {
        SQLForm form = new SQLForm();
        form.setLinebreakCase(true);
        form.setCase(false, false);
        form.setLowerCase(false);
        form.setGraphLevel(false);
        form.setSuppressSpace(true);
        form.setQuoteCharacter("'");
        form.setSuppressEmptyLine(true);
        form.setFormatLanguage("SQL");
        form.setBracketSpaces("noSpacesAroundBracket");
        form.setCommaSpaces("oneSpaceAfterComma");
        form.setEqualSpaces("oneSpaceAroundEqual");
        form.setSmallSQLWidth(80);
        form.setPageWidth(500);
        form.setAndOrIndention(true);
        form.setInitialIndentation(0);
        form.setIndention(4, true);
        form.setAlignmentEqual(false);
        form.setAlignmentComma(false);
        form.setAlignmentComment(false);
        form.setAlignmentAs(false);
        form.setAlignmentOperator(false);
        String formatSql = form.formatSQLAsString(sql);
        //特殊处理!=符号
        formatSql = formatSql.replace("! =", " != ");
//        formatSql = formatSql.replaceAll("\\s{1,},"," ,");
        return formatSql;
    }

    @Override
    public void validScriptName(String name) throws Exception {
        String pattern = "^[0-9a-zA-z\\-_]+$";
        boolean isMatch = Pattern.matches(pattern, name);
        if (!isMatch) {
            throw new RuntimeException("脚本名称只能是数字,字母,下划线,中划线");
        }
    }

    @Override
    public String dos2unix(String content) throws Exception {
        if (StringUtils.isNotBlank(content)) {
            content = content.replaceAll("\r\n", "\n");
            content = content.replaceAll("\r", "\n");
        }
        return content;
    }


    @Override
    public void insert(DataDevScriptFile dataDevScriptFile) {
        dataDevScriptFileDao.insertScriptFile(dataDevScriptFile);
    }

    @Override
    public void insertFiles(List<DataDevScriptFile> dataDevScriptFiles) {
        Integer size = dataDevScriptFiles.size();
        if (size > 0) {
            Integer page = size / PER_INSERT_SIZE + (size % PER_INSERT_SIZE == 0 ? 0 : 1);
            for (int index = 0; index < page; index++) {
                List<DataDevScriptFile> insertList = dataDevScriptFiles.subList(index * PER_INSERT_SIZE, Math.min(size, (index + 1) * PER_INSERT_SIZE));
                dataDevScriptFileDao.insertScriptFiles(insertList);
            }
        }

    }

    @Override
    public List<DataDevScriptFile> getScriptsByGitProjectIdAndDirPath(Long gitProjectId, String gitProjectDirPath) {
        return dataDevScriptFileDao.getScriptDirFile(gitProjectId, gitProjectDirPath);
    }

    @Override
    public List<DataDevScriptFile> getScriptsByGitProjectIdAndDirId(Long gitProjectId, Long dirId) {
        return dataDevScriptFileDao.getScriptDirFileByProjectIdAndDirId(gitProjectId, dirId);
    }

    @Override
    public DataDevScriptFile getScriptByGitProjectIdAndFilePath(Long gitProjectId, String filePath) {
        return getScriptByGitProjectIdAndFilePath(gitProjectId, filePath, null);
    }

    @Override
    public DataDevScriptFile getScriptByGitProjectIdAndFilePathIgnoreDelete(Long gitProjectId, String filePath, String version) {
        DataDevScriptFile file = new DataDevScriptFile();
        DataDevScriptFileHis fileHis = dataDevScriptFileHisDao.findByPathAndVersion(gitProjectId, filePath, version);
        if (fileHis == null) {
            return null;
        }
        file.setId(fileHis.getFileId());
        file.setGitProjectId(gitProjectId);
        file.setGitProjectFilePath(filePath);
        file.setVersion(fileHis.getVersion());
        file.setRelationDependencyId(fileHis.getRelationDependencyId());
        return file;
    }

    @Override
    public void updateDataDevScriptFile(Long gitProjectId, String filePath, DataDevScriptFile params) {
        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, filePath, params);
    }

    @Override
    public DataDevScriptFile getScriptByGitProjectIdAndFilePath(Long gitProjectId, String filePath, String version) {
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, filePath);
        if (dataDevScriptFile != null && StringUtils.isNotBlank(version) && !version.equals(dataDevScriptFile.getVersion())) {
            DataDevScriptFileHis his = dataDevScriptFileHisDao.findByVersion(dataDevScriptFile.getId(), version);
            if (his != null) {
                dataDevScriptFile.setVersion(his.getVersion());
                dataDevScriptFile.setName(his.getName());
                dataDevScriptFile.setSize(his.getSize());
                dataDevScriptFile.setFileMd5(his.getFileMd5());
                dataDevScriptFile.setDescription(his.getCommitMessage());
                dataDevScriptFile.setRelationDependencyId(his.getRelationDependencyId());
            } else {
                return null;
            }
        }
        return dataDevScriptFile;
    }

    /**
     * 获取 ScriptContent
     * <p>
     * 原因：最开始有可能是从GIT上拉取的文件，所以需要获取Git上面的数据到Hbase
     * 检查whereIsNew字段如果是GIT 同步到 hbase && 版本 + 1
     * 其他的情况都是从hbase获取
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @Override
    public String getScriptContent(Long gitProjectId, String gitProjectFilePath, String version, String erp) throws Exception {
        return new String(getScriptBytes(gitProjectId, gitProjectFilePath, version, erp), "utf-8");
    }

    @Override
    public String getScriptContentFromHbase(Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        return new String(getScriptBytesFromHbase(gitProjectId, gitProjectFilePath, version), "utf-8");
    }

    @Override
    public String getScriptContentFromHbase(Long fileId, String version) throws Exception {
        DataDevScriptFile file = new DataDevScriptFile();
        file.setId(fileId);
        file.setVersion(version);
        return new String(hbaseScript.getScriptBytes(file), "utf-8");
    }

    /**
     * 1.获取row文件的 btye
     * version为空取最新version
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getScriptBytes(Long gitProjectId, String gitProjectFilePath, String version, String erp) throws Exception {
        return getScriptBytes(gitProjectId, gitProjectFilePath, version, erp, false);
    }

    @Override
    public byte[] getScriptBytes(Long gitProjectId, String gitProjectFilePath, String version, String erp, boolean cache) throws Exception {
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
        if (dataDevScriptFile == null) {
            if (StringUtils.isBlank(version)) {
                throw new FileNotFoundException(gitProjectId, gitProjectFilePath);
            }
            DataDevScriptFileHis his = dataDevScriptFileHisDao.findByPathAndVersion(gitProjectId, gitProjectFilePath, version);
            if (his == null) {
                throw new FileNotFoundException(gitProjectId, gitProjectFilePath, version);
            }
            dataDevScriptFile = new DataDevScriptFile();
            dataDevScriptFile.setGitProjectId(gitProjectId);
            dataDevScriptFile.setGitProjectFilePath(gitProjectFilePath);
            dataDevScriptFile.setVersion(version);
            dataDevScriptFile.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.BOTH.tocode());
            dataDevScriptFile.setId(his.getFileId());
        } else if (StringUtils.isNotBlank(version)) {
            dataDevScriptFile.setVersion(version);
        }
        String tmpPath = DATA_DEV_TMP_DIR_PATH + dataDevScriptFile.getId() + TMP_SEPARATOR + dataDevScriptFile.getVersion();
        String tmpDonePath = tmpPath + TMP_SEPARATOR + TMP_FILE_WRITEDONE_STSUTS;
        String tmpWritingPath = tmpPath + TMP_SEPARATOR + TMP_FILE_WRITING_STSUTS;
        File doneFile = new File(tmpDonePath);
        boolean fromHbase = true;
        byte[] bytes = null;
        tmpLockMap.putIfAbsent(tmpPath, new ReentrantReadWriteLock());
        ReentrantReadWriteLock lock = tmpLockMap.get(tmpPath);
        if (lock == null) {
            tmpLockMap.putIfAbsent(tmpPath, new ReentrantReadWriteLock());
            lock = tmpLockMap.get(tmpPath);
        }
        if (doneFile.exists() && cache) {
            try {
                lock.readLock().lock();
                bytes = ZipUtil.File2byte(doneFile);
                fromHbase = false;
                logger.error("==================文件:" + tmpPath + "来自缓存");
            } catch (Exception e) {
                logger.error("===============取文件内容失败：" + e.getMessage());
                bytes = null;
            } finally {
                lock.readLock().unlock();
            }
        }
        if (fromHbase) {
            DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
            if (dataDevScriptFile != null && dataDevScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode())) {
                bytes = copyGitFileToHbase(dataDevScriptFile, dataDevGitProject, erp);
            } else if (dataDevScriptFile != null && dataDevScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.NOCONTENT.tocode())) {
                bytes = "".getBytes();
            } else {

                bytes = hbaseScript.getScriptBytes(dataDevScriptFile);
            }
            if (cache) {
                try {
                    lock.writeLock().lock();
                    ZipUtil.byte2File(bytes, new File(tmpWritingPath));
                    ZipUtil.renameFile(tmpWritingPath, tmpDonePath);
                } catch (Exception e) {
                    logger.error("================写文件报错：" + e.getMessage());
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
        return bytes;
    }

    @Override
    public ZtreeNode packZip(Long dependencyId, String filePath, String erp, String comment) throws Exception {
        List<DataDevDependencyDetail> dependencyDetails = devDependencyService.getDetails(dependencyId);
        DataDevDependency dataDevDependency = devDependencyService.getById(dependencyId);
        Long gitProjectId = dataDevDependency.getGitProjectId();
        String gitProjectFilePath = dataDevDependency.getGitProjectFilePath();
        if (dataDevDependency == null || dependencyDetails.size() == 0) {
            throw new RuntimeException("文件依赖为空！");
        }
        Long time1 = System.currentTimeMillis();
        byte[] bytes = devDependencyService.packZip(dependencyDetails, erp, null);
        Long time2 = System.currentTimeMillis();
        ZtreeNode newNode = null;
        DataDevScriptFile targetFile = getScriptByGitProjectIdAndFilePath(gitProjectId, filePath);
        if (targetFile == null) {
            newNode = createNewFile(gitProjectId, filePath, DataDevScriptTypeEnum.Zip.toCode(), erp, 0, bytes, comment, gitProjectFilePath, null, null, dependencyId, null);
            while (newNode.getChildren() != null && newNode.getChildren().size() > 0) {
                newNode = newNode.getChildren().get(0);
            }
        } else {
            HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = tryUpdateFile(gitProjectId, filePath, erp, bytes, null, null, true, comment, null, dependencyId);
            newNode = new ZtreeNode();
            newNode.setParChl(1);
            newNode.setGitProjectId(gitProjectId);
            newNode.setPath(filePath);
            newNode.setVersion(holdDoubleValue.b.getVersion());
        }
        Long time3 = System.currentTimeMillis();
        logger.error("=============================================打包时间：" + (time2 - time1));
        logger.error("=============================================生成文件时间：" + (time3 - time2));

        return newNode;
    }

    /**
     * 1.获取row文件的 btye
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getScriptBytesFromHbase(Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
        dataDevScriptFile.setVersion(version);
        if (dataDevScriptFile == null) {
            throw new RuntimeException("脚本不存在");
        }
        return hbaseScript.getScriptBytes(dataDevScriptFile);
    }

    /**
     * copy git 文件内容到Hbase
     *
     * @param dataDevScriptFile
     * @param dataDevGitProject
     * @return
     * @throws Exception
     */
    @Override
    public byte[] copyGitFileToHbase(DataDevScriptFile dataDevScriptFile, DataDevGitProject dataDevGitProject, String erp) throws Exception {
        Long gitProjectId = dataDevGitProject.getGitProjectId();
        boolean isBinary = !DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());

        if (dataDevScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode()) || (dataDevScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.NOCONTENT.tocode()))) {
            JDGitFiles jdGitFiles = new JDGitFiles();
            jdGitFiles.setGitProjectId(gitProjectId);
            jdGitFiles.setBranch(dataDevGitProject.getBranch());
            jdGitFiles.setFilePath(dataDevScriptFile.getGitProjectFilePath());
            jdGitFiles.setBinary(isBinary);
            jdGitFiles.setLastCommitId(dataDevScriptFile.getGitVersion());
            jdGitFiles.setVersion(dataDevScriptFile.getVersion());
            jdGitFiles.loadFileContent();
            byte[] bytes = null;
            if (isBinary) {
                bytes = jdGitFiles.getBytes();
            } else {
                bytes = jdGitFiles.getContent().getBytes("utf-8");
            }
            tryUpdateFile(dataDevScriptFile.getGitProjectId(), dataDevScriptFile.getGitProjectFilePath(), erp, bytes, String.valueOf(1000), jdGitFiles.getLastCommitId(), true);
            return bytes;
        }
        return null;
    }


    /**
     * 尝试update
     * <p>
     * 1.如果提交的版本version，和数据库的一致。那么直接覆盖 , 同时计算MD5的值，如果有变化或者是不可编辑脚本，那么版本加1
     * 2.如果提交的版本version，和数据库的不一致，那么提示需要前端做合并,同时传递内容给前端
     */
    @Override
    public HoldDoubleValue<Boolean, JDGitFiles> tryUpdateFile(Long gitProjectId, String gitProjectFilePath, String erp, byte[] bytes, String version, String gitVersion, boolean isDiscover, String description, String startShellPath, Long relationDependencyId) throws Exception {
        DataDevScriptFile dataBaseScriptFile = getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if (dataBaseScriptFile.getVersion() == null || StringUtils.isBlank(version) || version.equals(dataBaseScriptFile.getVersion())) {
            isDiscover = true;
        }
        bytes = bytes != null ? bytes : new byte[0];
        DataDevScriptFile updateParams = new DataDevScriptFile();
        updateParams.setMender(erp);
        JDGitFiles ressult = new JDGitFiles();
        ressult.setVersion(dataBaseScriptFile.getVersion());
        ressult.setFileMd5(dataBaseScriptFile.getFileMd5());
        updateParams.setGitVersion(gitVersion);
        updateParams.setDescription(description);
        updateParams.setStartShellPath(startShellPath);
        updateParams.setRelationDependencyId(relationDependencyId);
        if (isDiscover) {
            String newMd5 = MD5Util.getMD5(bytes);
            String oldMd5 = dataBaseScriptFile.getFileMd5();
            if (dataBaseScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode())
                    || StringUtils.isBlank(dataBaseScriptFile.getVersion())
                    || !DataDevScriptTypeEnum.canEdit(dataBaseScriptFile.getType())
                    || StringUtils.isEmpty(oldMd5)
                    || !oldMd5.equals(newMd5)) {
                Integer newVersion = 1000;
                if (StringUtils.isNotEmpty(dataBaseScriptFile.getVersion()) && !DataDevScriptFileWhereIsNewEnum.GIT.tocode().equals(dataBaseScriptFile.getWhereIsNew())
                ) {
                    newVersion = Integer.parseInt(dataBaseScriptFile.getVersion()) + 1;
                }
                ressult.setVersion(String.valueOf(newVersion));
                ressult.setFileMd5(newMd5);
                //同步hbase
                dataBaseScriptFile.setVersion(String.valueOf(newVersion));
                dataBaseScriptFile.setBytes(bytes);
                hbaseScript.upScriptToHbase(dataBaseScriptFile);

                //更新scriptFile表
                updateParams.setVersion(String.valueOf(newVersion));
                updateParams.setFileMd5(newMd5);
                updateParams.setSize((long) bytes.length);

                updateParams.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.HBASE.tocode());


                //添加历史
                DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
                dataDevScriptFileHis.setVersion(String.valueOf(newVersion));
                dataDevScriptFileHis.setCreator(erp);
                dataDevScriptFileHis.setScriptOperatorType(DataDevHisTypeEnum.MOIDFY.tocode());
                dataDevScriptFileHis.setType(dataBaseScriptFile.getType());
                dataDevScriptFileHis.setFileMd5(newMd5);
                dataDevScriptFileHis.setName(Base64Util.splitFilePath(gitProjectFilePath).b);
                dataDevScriptFileHis.setSize((long) bytes.length);
                dataDevScriptFileHis.setGitProjectId(gitProjectId);
                dataDevScriptFileHis.setGitProjectFilePath(gitProjectFilePath);
                dataDevScriptFileHis.setGitVersion(gitVersion);
                dataDevScriptFileHis.setFileId(dataBaseScriptFile.getId());
                dataDevScriptFileHis.setRelationDependencyId(relationDependencyId);
                dataDevScriptFileHisDao.insertHis(dataDevScriptFileHis);
            }
            dataDevScriptFileDao.updateGitScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            return new HoldDoubleValue<Boolean, JDGitFiles>(isDiscover, ressult);
        }
        ressult.setContent(hbaseScript.getScriptContent(dataBaseScriptFile));
        return new HoldDoubleValue<Boolean, JDGitFiles>(isDiscover, ressult);

    }

    public HoldDoubleValue<Boolean, JDGitFiles> tryUpdateFile(Long gitProjectId, String gitProjectFilePath, String erp, byte[] bytes, String version, String gitVersion, boolean isDiscover) throws Exception {
        return tryUpdateFile(gitProjectId, gitProjectFilePath, erp, bytes, version, gitVersion, isDiscover, null, null, null);
    }

    /**
     * 新建 调用改方法
     * 1.有byte 操作hbase,scriptHistory
     * 2.无byte说明只是新建立文件,不操作hbase ,scriptHistory
     */
    public ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath) throws Exception {
        return createNewFile(gitProjectId, gitProjectFilePath, scriptType, erp, isShow, bytes, description, startShellPath, null, null, null, null);
    }

    public ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath, String tmpDirPath) throws Exception {
        return createNewFile(gitProjectId, gitProjectFilePath, scriptType, erp, isShow, bytes, description, startShellPath, tmpDirPath, null, null, null);
    }


    @Override
    public ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath, String tmpDirPath, String args, Long relationDependencyId, Long scriptUploadId) throws Exception {
        if (isShow == 1) {
            //临时文件 , 重新命名
            String suffix = DataDevScriptTypeEnum.suffixOf(scriptType);
            String fileName = TEMP_NOW_TIME_FORMATE.format(new Date()) + suffix;
            /**
             * zhangrui156_20180601222222.py
             * zhangrui156_20180601222222.py
             * zhangrui156_20180601222222.py
             */
            gitProjectFilePath = erp + "_" + fileName;
            gitProjectFilePath = Base64Util.getFullPath(tmpDirPath, gitProjectFilePath);
        }
        int countFile = dataDevScriptFileDao.countScriptFile(gitProjectId, gitProjectFilePath);
        if (countFile > 0) {
            throw new RuntimeException(gitProjectFilePath + "文件名重复");
        }
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setGitProjectId(gitProjectId);
        jdGitFiles.setErp(erp);
        jdGitFiles.setFilePath(gitProjectFilePath);
        jdGitFiles.setSize((long) (bytes != null ? bytes.length : 0));
        jdGitFiles.setIsShow(isShow);
        jdGitFiles.setDescription(description);
        jdGitFiles.setStartShellPath(startShellPath);
        jdGitFiles.setArgs(args);
        jdGitFiles.setScriptUploadId(scriptUploadId);
        if (bytes != null && bytes.length > 0) {
            jdGitFiles.setFileMd5(MD5Util.getMD5(bytes));
        } else {
            jdGitFiles.setFileMd5(null);
        }

        //createFileAndDir
        ZtreeNode result = dataDevScriptDirService.createFileOrDirInDataBase(jdGitFiles, false);

        DataDevScriptFile dataDevScriptFile = getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);

        DataDevScriptFile updateParams = new DataDevScriptFile();
        updateParams.setFileMd5(jdGitFiles.getFileMd5());
        updateParams.setVersion(String.valueOf(1000L));
        updateParams.setGitProjectId(gitProjectId);
        updateParams.setGitProjectFilePath(gitProjectFilePath);
        updateParams.setSize((long) (bytes != null ? bytes.length : 0));
        updateParams.setId(dataDevScriptFile.getId());
        updateParams.setBytes(bytes);
        updateParams.setRelationDependencyId(relationDependencyId);
        updateParams.setArgs(args);
        //saveToHbase
        if (bytes != null && bytes.length > 0) {
            hbaseScript.upScriptToHbase(updateParams);
        }

        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, gitProjectFilePath, updateParams);
        //添加历史
        DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
        dataDevScriptFileHis.setVersion(String.valueOf(1000L));
        dataDevScriptFileHis.setCreator(erp);
        dataDevScriptFileHis.setGitProjectId(gitProjectId);
        dataDevScriptFileHis.setGitProjectFilePath(gitProjectFilePath);
        dataDevScriptFileHis.setScriptOperatorType(DataDevHisTypeEnum.ADD.tocode());
        dataDevScriptFileHis.setType(scriptType);
        dataDevScriptFileHis.setFileMd5(jdGitFiles.getFileMd5());
        dataDevScriptFileHis.setName(Base64Util.splitFilePath(gitProjectFilePath).b);
        dataDevScriptFileHis.setSize((long) (bytes != null ? bytes.length : 0));
        dataDevScriptFileHis.setGitProjectId(gitProjectId);
        dataDevScriptFileHis.setGitProjectFilePath(gitProjectFilePath);
        dataDevScriptFileHis.setFileId(dataDevScriptFile.getId());
        dataDevScriptFileHis.setRelationDependencyId(relationDependencyId);
        dataDevScriptFileHisDao.insertHis(dataDevScriptFileHis);
        return result;
    }


    /**
     * 在数据库里面添加
     *
     * @param jdGitFiles
     * @param dataDevScriptFile
     * @return
     * @throws Exception
     */
 /*   private ZtreeNode createScriptFile(JDGitFiles jdGitFiles, DataDevScriptFile dataDevScriptFile) throws Exception {
        jdGitFiles.setBytes(dataDevScriptFile.getBytes());
        JDGitFiles result = jdGitFiles.addFile();
        return dataDevScriptDirService.createFileOrDirInDataBase(result, false);
    }*/


    /**
     * 修改为Hbase版本过后的 删除文件
     * 删除脚本中心的数据
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @throws Exception
     */
    @Override
    public void deleteScriptFile(Long gitProjectId, String gitProjectFilePath, String erp) throws Exception {
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);

        if (dataDevScriptFile != null) {
            DataDevScriptFilePublish notFail = dataDevScriptPublishService.findLastNotFail(gitProjectId, gitProjectFilePath, null);
            if (notFail != null) {
                importScriptManager.deleteBuffaloScript(dataDevScriptFile, null, erp);
            }
            dataDevScriptPublishService.deletePublish(null, gitProjectId, gitProjectFilePath);
            if (StringUtils.isNotBlank(dataDevScriptFile.getGitVersion())) {
                //删除Git上面文件
                DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
                pushDeleteGit(dataDevGitProject, gitProjectFilePath, erp);
            } else {
                dataDevScriptFileDao.realDeleteSingleScriptFile(gitProjectId, gitProjectFilePath);
            }
        }
    }

    @Override
    public boolean canMoveScriptFile(Long gitProjectId, String gitProjectFilePath) throws Exception {
        List<DataDevScriptFilePublish> scriptPush = dataDevScriptPublishService.getPushList(gitProjectId, gitProjectFilePath);
        if (scriptPush != null && scriptPush.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * @param gitProjectId
     * @param gitProjectFilePath app/a.java
     * @param newFileName        test2
     * @param erp
     * @throws Exception
     */
    @Override
    public DataDevScriptFile renameScriptFile(Long gitProjectId, String gitProjectFilePath, String newFileName, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("Project " + gitProjectId + "不存在");
        }
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
        if (dataDevScriptFile == null) {
            throw new RuntimeException(gitProjectFilePath + "不存在");
        }
        String dirStr = Base64Util.splitFilePath(gitProjectFilePath).a;
        String moveToGitProjectFilePath = StringUtils.isNotBlank(dirStr) ? (dirStr + "/" + newFileName) : newFileName;
        DataDevScriptFile res = move(dataDevGitProject, dataDevScriptFile, moveToGitProjectFilePath, erp);
        return res;
    }


    /**
     * @param gitProjectId
     * @param gitProjectFilePath app/javaFile/a.java
     * @param newDirPath         app/javaFileDir
     * @param erp
     * @return
     * @throws Exception
     */
    @Override
    public ZtreeNode moveScriptFile(Long gitProjectId, String gitProjectFilePath, String newDirPath, String newName, String description, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("Project " + gitProjectId + "不存在");
        }

        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
        if (dataDevScriptFile == null) {
            throw new RuntimeException(gitProjectFilePath + "不存在");
        }
        dataDevScriptFile.setDescription(description);
        newDirPath = Base64Util.formatName(newDirPath);
        newName = Base64Util.formatName(newName);
        String moveToGitProjectFilePath = StringUtils.isNotBlank(newDirPath) ? (newDirPath + "/" + newName) : newName;
        List<DataDevScriptFilePublish> publishList = dataDevScriptPublishService.getDistinctPushList(gitProjectId, gitProjectFilePath);
        for (DataDevScriptFilePublish publish : publishList) {
            if (publish.getBuffaloScriptId() != null) {
                importScriptManager.callBackScript(publish.getBuffaloScriptId(), publish.getBuffaloScriptVersion(), gitProjectId, moveToGitProjectFilePath, erp, publish.getVersion());
            }
        }
        DataDevScriptFile res = move(dataDevGitProject, dataDevScriptFile, moveToGitProjectFilePath, erp);
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setGitProjectId(gitProjectId);
        jdGitFiles.setFilePath(moveToGitProjectFilePath);
        jdGitFiles.setErp(erp);
        jdGitFiles.setVersion(res.getVersion());
        jdGitFiles.setLastCommitId(res.getGitVersion());
        jdGitFiles.setVersion(res.getVersion());
        jdGitFiles.setSize(dataDevScriptFile.getSize());
        return dataDevScriptDirService.createFileOrDirInDataBase(jdGitFiles, false);
    }

    /**
     * 移动文件
     * <p>
     * 在本地数据库创建文件夹、文件
     *
     * @param dataDevGitProject
     * @param dataDevScriptFile
     * @param moveToGitProjectFilePath sdk/java/test.java
     * @param erp
     * @throws Exception
     */
    private DataDevScriptFile move(DataDevGitProject dataDevGitProject, DataDevScriptFile dataDevScriptFile, String moveToGitProjectFilePath, String erp) throws Exception {
        Long gitProjectId = dataDevGitProject.getGitProjectId();
        String gitProjectFilePath = dataDevScriptFile.getGitProjectFilePath();
        String fileName = Base64Util.splitFilePath(moveToGitProjectFilePath).b;

        if (dataDevScriptFileDao.countScriptFile(gitProjectId, moveToGitProjectFilePath) > 0) {
            throw new RuntimeException(moveToGitProjectFilePath + "文件重复");
        }
        DataDevScriptFile upateParams = new DataDevScriptFile();

        //操作Git
        if (StringUtils.isNotBlank(dataDevScriptFile.getGitVersion())) {
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setGitProjectId(gitProjectId);
                jdGitFiles.setBranch(dataDevGitProject.getBranch());
                jdGitFiles.setFilePath(gitProjectFilePath);
                jdGitFiles.setErp(erp);
                jdGitFiles.setBinary(DataDevScriptTypeEnum.canRun(dataDevScriptFile.getType()));
                String gitVersion = jdGitFiles.move(moveToGitProjectFilePath);
                upateParams.setGitVersion(gitVersion);
            } catch (Exception e) {
                try {
                    DataDevScriptFile oldFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
                    boolean isBinary = !DataDevScriptTypeEnum.canEdit(oldFile.getType());
                    JDGitFiles jdGitFiles = new JDGitFiles();
                    jdGitFiles.setFilePath(moveToGitProjectFilePath);
                    jdGitFiles.setBinary(isBinary);
                    jdGitFiles.setErp(erp);
                    jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
                    jdGitFiles.setBranch(dataDevGitProject.getBranch());
                    jdGitFiles.setName(fileName);
                    jdGitFiles.setBytes(hbaseScript.getScriptBytes(oldFile));
                    JDGitFiles result = jdGitFiles.addFile(null);
                } catch (Exception ee) {
                    logger.error("============move22222", e);
                }

            }
        }
        //更新数据库记录
        upateParams.setGitProjectId(gitProjectId);
        upateParams.setGitProjectFilePath(moveToGitProjectFilePath);
        upateParams.setName(fileName);
        upateParams.setDescription(dataDevScriptFile.getDescription());
        upateParams.setGitProjectDirPath(Base64Util.splitFilePath(moveToGitProjectFilePath).a);
        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.getFileNameScriptType(moveToGitProjectFilePath);
        upateParams.setType(typeEnum.toCode());
        DataDevScriptFilePublish updatePublish = new DataDevScriptFilePublish();
        updatePublish.setGitProjectFilePath(moveToGitProjectFilePath);
        publishDao.updateDataDevPublish(gitProjectId, gitProjectFilePath, updatePublish);

        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, gitProjectFilePath, upateParams);
        //更新运行记录
        dataDevScriptRunDetailService.updateDataDevScriptRunDetailPath(gitProjectId, gitProjectFilePath, moveToGitProjectFilePath);

        //更新操作记录
        dataDevScriptFileHisDao.updateDataDevScriptFileHisPath(gitProjectId, gitProjectFilePath, moveToGitProjectFilePath);
        return upateParams;
    }

    /**
     * version (页面传递过来的), bytes,mender,scriptType,gitProjectId , gitScriptFilePath , filemd5 required
     * 如果是不可编辑的文件直接覆盖
     * <p>
     * isDirectCover : true 对外接口上传的脚本都是覆盖
     * <p>
     * 1.覆盖直接 || md5 值不一样 添加版本号
     * 修改为Hbase版本(提交到Hbase)
     *
     * @param dataDevScriptFile
     * @param isDirectCover     (是否直接增加一个版本)
     * @return
     */
/*    @Override
    @Transactional
    public HoldDoubleValue<Boolean, JDGitFiles> updateScriptFileContent(DataDevScriptFile dataDevScriptFile, Long gitProjectId, Boolean isDirectCover) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        DataDevScriptFile dataBaseScriptFile = getScriptFile(gitProjectId, dataDevScriptFile.getGitProjectFilePath());

        String gitProjectFilePath = dataDevScriptFile.getGitProjectFilePath();
        Integer scriptType = dataDevScriptFile.getType();
        boolean isBinary = !DataDevScriptTypeEnum.canEdit(scriptType);


        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setBranch(dataDevGitProject.getBranch());
        jdGitFiles.setGitProjectId(gitProjectId);
        jdGitFiles.setFilePath(gitProjectFilePath);
        jdGitFiles.setName(dataDevScriptFile.getName());
        jdGitFiles.setBinary(isBinary);
        jdGitFiles.setErp(dataDevScriptFile.getMender());

        // isBinary 二进制（系统不识别的文件内容，直接覆盖） || 或者明确指定
        isDirectCover = isDirectCover || isBinary;

        if (!isDirectCover) {
            *//**
     *   检查是否可以直接提交(version 一致)
     *   获取最近的一次提交的Commit，如果不一致，那么提示页面做对比
     *   或者获取最新的提交结果比较
     *//*
            String version = dataDevScriptFile.getVersion();
            String dataBaseVersion = dataBaseScriptFile.getVersion();
            if (StringUtils.isBlank(dataBaseVersion) || "0".equalsIgnoreCase(dataBaseVersion) || version.equalsIgnoreCase(dataBaseVersion)) {
                isDirectCover = true;
            }
        }
        if (isDirectCover) {
            *//**
     * 1.保存Hbase history , 计算MD5的值是否可以新添加一个版本
     *//*
            String lastCommitId = addScriptHbaseVersion(dataDevScriptFile, dataBaseScriptFile);
            jdGitFiles.setLastCommitId(lastCommitId);
            if (isBinary) {
                jdGitFiles.setBytes(dataDevScriptFile.getBytes());
            } else {
                jdGitFiles.setContent(new String(dataDevScriptFile.getBytes(), "utf-8"));
            }
            return new HoldDoubleValue<Boolean, JDGitFiles>(isDirectCover, jdGitFiles);
        } else {
            *//**
     * 页面需要比较 返回version
     * 页面需要比较 返回待比较的Content
     *//*
            String lastCommitId = dataBaseScriptFile.getVersion();
            String content = hbaseScript.getScriptContent(dataBaseScriptFile);
            jdGitFiles.setLastCommitId(lastCommitId);
            jdGitFiles.setContent(content);
            return new HoldDoubleValue<Boolean, JDGitFiles>(isDirectCover, jdGitFiles);
        }
    }*/

    /**
     * 计算newVersion
     *
     * @param dataDevScriptFile
     * @param dataBaseDataDevScriptFile
     * @return
     */
/*    private HoldDoubleValue<String, String> caculateNewVersion(DataDevScriptFile dataDevScriptFile, DataDevScriptFile dataBaseDataDevScriptFile) {
        boolean isBinary = !DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());
        int newVersion = 0;
        String newMd5 = null;
        if (StringUtils.isEmpty(dataBaseDataDevScriptFile.getVersion()) || "0".equalsIgnoreCase(dataBaseDataDevScriptFile.getVersion())) {
            newVersion = 1000;
        } else {
            newVersion = Integer.parseInt(dataBaseDataDevScriptFile.getVersion());
        }
        *//**
     * 1.二进制文件直接新增
     * 2.其他文件计算MD5 的值是否有变化
     *//*
        if (isBinary) {
            newVersion++;
        } else {
            //计算MD5的值是否有变化
            newMd5 = MD5Util.getMD5(dataDevScriptFile.getBytes());
            if (StringUtils.isNotEmpty(dataBaseDataDevScriptFile.getFileMd5()) && !dataBaseDataDevScriptFile.getFileMd5().equalsIgnoreCase(newMd5)) {
                newVersion++;
            }
        }
        return new HoldDoubleValue<String, String>(String.valueOf(newVersion), newMd5);
    }*/

    /**
     * 返回最新的版本号
     *
     * @return
     * @throws Exception
     */
/*    private String addScriptHbaseVersion(DataDevScriptFile dataDevScriptFile, DataDevScriptFile dataBaseDataDevScriptFile) throws Exception {
        HoldDoubleValue<String, String> newVersionAndMd5 = caculateNewVersion(dataDevScriptFile, dataBaseDataDevScriptFile);
        if (!dataBaseDataDevScriptFile.getVersion().equalsIgnoreCase(newVersionAndMd5.a)) {
            dataDevScriptFile.setVersion(newVersionAndMd5.a);
            hbaseScript.upScriptToHbase(dataDevScriptFile);
            //更新script_file 表 ， 添加script_file_history
            DataDevScriptFile updateParams = new DataDevScriptFile();
            updateParams.setVersion(newVersionAndMd5.a);
            updateParams.setSize((long) dataDevScriptFile.getBytes().length);
            updateParams.setGitVersion(dataDevScriptFile.getGitVersion());
            updateParams.setFileMd5(newVersionAndMd5.b);
            updateParams.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.HBASE.tocode());
            updateParams.setIsBigFile(SplitFileUtils.isBigFile((long) dataDevScriptFile.getBytes().length));
            dataDevScriptFileDao.updateGitScriptFile(dataDevScriptFile.getGitProjectId(), dataDevScriptFile.getGitProjectFilePath(), updateParams);

            //临时文件没有操作历史记录
            if (!dataDevScriptFile.getIsShow().equals(1)) {
                DataDevScriptFileHis insertHistory = new DataDevScriptFileHis();
                insertHistory.setGitProjectId(dataDevScriptFile.getGitProjectId());
                insertHistory.setGitProjectFilePath(dataDevScriptFile.getGitProjectFilePath());
                insertHistory.setFileId(dataDevScriptFile.getId());
                insertHistory.setDirId(dataDevScriptFile.getDirId());
                insertHistory.setName(dataDevScriptFile.getName());
                insertHistory.setScriptOperatorType(DataDevHisTypeEnum.DELETE.tocode());
                insertHistory.setType(dataDevScriptFile.getType());
                insertHistory.setGitVersion(dataDevScriptFile.getGitVersion());
                insertHistory.setSize((long) dataDevScriptFile.getBytes().length);
                insertHistory.setVersion(newVersionAndMd5.a);
                insertHistory.setCreator(dataDevScriptFile.getMender() != null ? dataDevScriptFile.getMender() : dataDevScriptFile.getCreator());
                insertHistory.setFileMd5(newVersionAndMd5.b);
                dataDevScriptFileHisDao.insertHis(insertHistory);
            }

            return newVersionAndMd5.a;
        }
        return dataBaseDataDevScriptFile.getVersion();
    }*/

    /**
     * 获取一个Git的版本
     *
     * @param gitProjectId
     * @param gitProjecFilePath
     * @return
     * @throws Exception
     */
    @Override
    public JDGitFiles getGitContent(Long gitProjectId, String gitProjecFilePath, String gitVersion) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("项目不存在!");
        }
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setFilePath(gitProjecFilePath);
        jdGitFiles.setGitProjectId(gitProjectId);
        jdGitFiles.setBranch(gitVersion);
        jdGitFiles.setFilePath(gitProjecFilePath);
        jdGitFiles.loadFileContent();
        return jdGitFiles;
    }

    /**
     * 刷新文件
     * <p>
     * 如果Git上已经已经删除 || Git 最新版本不一致提示merge
     * <p>
     * setNewGitVersion 为 空字符串 说明Git已经删除  本地不在做处理
     * <p>
     * 有新的GitVersion 然后在做处理
     * <p>
     * 如果是pull 非可编辑文件，那么直接覆盖
     *
     * @param gitProjectId
     * @param gitProjecFilePath
     * @param erp
     * @throws Exception
     */
    @Override
    public DataDevScriptFile pullFile(Long gitProjectId, String gitProjecFilePath, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("项目不存在!");
        }
        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjecFilePath);

        if (!DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType())) {
           /* try{
                //git 文件不存在会报错
                DataDevScriptFileHis dataDevScriptFileHis = queryLastCommit(dataDevGitProject, gitProjecFilePath);
                if (dataDevScriptFileHis != null) {
                    //更新数据库中 GitVersion字段
                    //不可编辑的文件 pull不处理
                    DataDevScriptFile updateParams = new DataDevScriptFile();
                    updateParams.setGitVersion(dataDevScriptFileHis.getGitVersion());
                    dataDevScriptFileDao.updateGitScriptFile(gitProjectId, gitProjecFilePath, updateParams);
                }
                return dataDevScriptFile;
            }catch (Exception e){
                logger.error("pullFile",e);
            }
            dataDevScriptFile.setLastGitVersion(dataDevScriptFile.getGitVersion());
            dataDevScriptFile.setLastGitVersionMd5(dataDevScriptFile.getFileMd5());*/

        }
        dataDevScriptFile.setNewGitVersion(dataDevScriptFile.getLastGitVersion() == null ? dataDevScriptFile.getGitVersion() : dataDevScriptFile.getLastGitVersion());
        return dataDevScriptFile;
    }

    /**
     * 拉取文件夹
     * <p>
     * 1.获取文件列表 以及最近的一次Commit
     * 2.新文件直接插入
     * 3.如果文件无法和数据库中的GitVersion版本一直 提示merge ,本地如果没有改过，直接当成是最新的 whereIsNew Git
     * 4.如果本地已经删除，直接更新回来
     * 5.以Git拉取的文件为主
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @throws Exception
     */
    @Override
    public HoldDoubleValue<List<DataDevScriptFile>, List<ZtreeNode>> pullDir(Long gitProjectId, String gitProjectDirPath, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("项目不存在!");
        }
        /**
         *  Git 文件
         */
        List<JDGitFiles> jdGitFiles = getJdGitFileTree(gitProjectDirPath, dataDevGitProject, erp);
        /**
         * 数据库文件
         */
        Map<String, DataDevScriptFile> dataBaseDataDevSriptFileMap = getMapDataBaseScriptFile(gitProjectId, gitProjectDirPath);

        //本地不存在的数据 ： Git里面存在 数据库没有
        List<JDGitFiles> needInsert = new ArrayList<JDGitFiles>();
        //相同的文件，需要查询Commit，然后merge
        List<DataDevScriptFile> maryMerge = new ArrayList<DataDevScriptFile>();

        for (JDGitFiles gitFile : jdGitFiles) {


            DataDevScriptFile dataBaseDataDevScriptFile = dataBaseDataDevSriptFileMap.get(gitFile.getFilePath());
            if (dataBaseDataDevScriptFile != null) {
                if (dataDevScriptDirService.isBelongTarget(dataBaseDataDevScriptFile.getGitProjectFilePath())) {
                    continue;
                }
                if (dataBaseDataDevScriptFile.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode())) {
                    // continue;
                }
                gitFile.setLastCommitId(dataBaseDataDevScriptFile.getLastGitVersion());
                //本地删除了，但是GIT上还存在
                if (dataBaseDataDevScriptFile.getDeleted().equals(1)) {
                    needInsert.add(gitFile);
                    continue;
                }
                //本地没有修改过 这个目前不充分
                if (StringUtils.isBlank(dataBaseDataDevScriptFile.getGitVersion())) {
                    needInsert.add(gitFile);
                    continue;
                }
                //如果是不可编辑的文件 pull 不处理
                if (!DataDevScriptTypeEnum.canEdit(DataDevScriptTypeEnum.getFileNameScriptType(gitFile.getName()).toCode())) {
                    continue;
                }
                if (!dataBaseDataDevScriptFile.getGitVersion().equals(dataBaseDataDevScriptFile.getLastGitVersion())) {
                    //处理 GitVersion不一致
                    dataBaseDataDevScriptFile.setNewGitVersion(dataBaseDataDevScriptFile.getLastGitVersion());
                    maryMerge.add(dataBaseDataDevScriptFile);
                }
            } else {
                needInsert.add(gitFile);
            }
        }
        List<ZtreeNode> ztreeNodeList = handPullNeedInsert(needInsert);
        return new HoldDoubleValue<List<DataDevScriptFile>, List<ZtreeNode>>(maryMerge, ztreeNodeList);
    }

    /**
     * 插入DataDevScriptFile
     * <p>
     * 2018-12-20 获取Git内容计算md5
     *
     * @param needInsert
     */
    private List<ZtreeNode> handPullNeedInsert(List<JDGitFiles> needInsert) throws Exception {
        List<ZtreeNode> ztreeNodesList = new ArrayList<ZtreeNode>();
        if (needInsert != null && needInsert.size() > 0) {
            for (JDGitFiles jdGitFile : needInsert) {
                try {
                    jdGitFile.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.GIT.tocode());
                    jdGitFile.loadFileContent();
                    ZtreeNode ztreeNode = dataDevScriptDirService.createFileOrDirInDataBase(jdGitFile, false);
                    String gitStatus = DataDevScriptGitStatusEnum.getGitStatus(jdGitFile.getLastCommitId(), jdGitFile.getLastCommitId(), jdGitFile.getFileMd5(), jdGitFile.getFileMd5(), 0).toCode();
                    ztreeNode.setGitStatus(gitStatus);

                    ztreeNodesList.add(ztreeNode);
                } catch (Exception e) {
                    logger.error("handPullNeedInsert", e);
                }

            }
        }
        return ztreeNodesList;
    }

    /**
     * 获取最近一条提交历史
     *
     * @param dataDevGitProject
     * @param gitScriptFilePath
     * @return
     * @throws Exception
     */
    private DataDevScriptFileHis queryLastCommit(DataDevGitProject dataDevGitProject, String gitScriptFilePath) throws Exception {
        JDGitCommits jdGitCommits = new JDGitCommits();
        jdGitCommits.setBranch(dataDevGitProject.getBranch());
        jdGitCommits.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitCommits.setPath(gitScriptFilePath);
        jdGitCommits.setProjectPath(dataDevGitProject.getGitProjectPath());
        jdGitCommits.setPage(1);
        jdGitCommits.setPageSize(1);
        return jdGitCommits.queryLastCommit();
    }

    /**
     * 返回 某个目录下面的DataDevScriptFile
     *
     * @param gitProjectId
     * @return
     */
    private Map<String, DataDevScriptFile> getMapDataBaseScriptFile(Long gitProjectId, String gitProjectDirPath) {
        Map<String, DataDevScriptFile> source = new HashMap<String, DataDevScriptFile>();
        List<DataDevScriptFile> allDataBaseDataDevScriptFile = dataDevScriptFileDao.queryDirAll(gitProjectId, gitProjectDirPath);
        for (DataDevScriptFile temp : allDataBaseDataDevScriptFile) {
            temp.setTypeStr(DataDevScriptTypeEnum.enumValueOf(temp.getType()).toName());
            source.put(temp.getGitProjectFilePath(), temp);
        }
        return source;
    }

    /**
     * 获取Git里面的文件
     *
     * @param gitProjectDir
     * @param dataDevGitProject
     * @return
     * @throws Exception
     */
    private List<JDGitFiles> getJdGitFileTree(String gitProjectDir, DataDevGitProject dataDevGitProject, String erp) throws Exception {

        List<JDGitFiles> result = new ArrayList<JDGitFiles>();

        Long gitProjectId = dataDevGitProject.getGitProjectId();

        JDGitRepositories jdGitRepositories = new JDGitRepositories();
        jdGitRepositories.setFilePath(gitProjectDir);
        jdGitRepositories.setGitProjectId(gitProjectId);
        jdGitRepositories.setBranch(dataDevGitProject.getBranch());
        jdGitRepositories.setRecursive(true);

        List<JDGitRepositories> allGitRepositories = jdGitRepositories.treeAll();
        for (JDGitRepositories temp : allGitRepositories) {
            if (temp.getType().equals("blob")) {
                JDGitFiles jdGitFile = new JDGitFiles();
                jdGitFile.setName(temp.getName());
                jdGitFile.setGitProjectId(gitProjectId);
                jdGitFile.setFilePath(temp.getPath());
                jdGitFile.setBinary(!DataDevScriptTypeEnum.getFileNameScriptType(temp.getName()).getCanEdit());
                jdGitFile.setErp(erp);
                result.add(jdGitFile);
            }
        }
        return result;
    }


    /**
     * push 文件夹里面的数据
     * <p>
     * 同步本地文件夹的数据到GIT
     * 1.本地删除-Git直接删除
     * 2.本地新建-直接push
     * 3.其他情况提示pull
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @param erp
     */
    @Override
    public List<DataDevScriptFile> pushDir(Long gitProjectId, String gitProjectDirPath, String commitMessage, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("项目不存在!");
        }
        List<DataDevScriptFile> allDataBaseDataDevScriptFile = dataDevScriptFileDao.queryDirAll(gitProjectId, gitProjectDirPath);

        return handPush(dataDevGitProject, erp, allDataBaseDataDevScriptFile, commitMessage, gitProjectDirPath, true);

    }

    /**
     * push file 处理和push Dir 一样
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @return
     * @throws Exception
     */
    @Override
    public DataDevScriptFile pushFile(Long gitProjectId, String gitProjectFilePath, String commitMessage, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("项目不存在!");
        }

        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFileIgnoreDeleted(gitProjectId, gitProjectFilePath);
        List<DataDevScriptFile> result = handPush(dataDevGitProject, erp, Arrays.asList(dataDevScriptFile), commitMessage, gitProjectFilePath, false);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }


    /**
     * @param dataDevGitProject
     * @param erp
     * @param allDataBaseDataDevScriptFile
     * @return
     * @throws Exception
     */
    private List<DataDevScriptFile> handPush(DataDevGitProject dataDevGitProject, String erp, List<DataDevScriptFile> allDataBaseDataDevScriptFile, String commitMessage, String commitPath, boolean isDir) throws Exception {

        /**
         * 2018-12-11 发现Git PUSH 接口对于大于10M的也可以push 上去但是Push的结果不对
         * 1。如果是DirPush那么直接忽略大于10M的文件
         * 2。如果是FilePush那么给出错误信息
         */
        // boolean isDir = allDataBaseDataDevScriptFile.size() > 1;
        List<DataDevScriptFile> fixDataDevScriptFileList = new ArrayList<DataDevScriptFile>();
        for (DataDevScriptFile dataDevScriptFile : allDataBaseDataDevScriptFile) {
            if (dataDevScriptFile.getSize() != null && dataDevScriptFile.getSize() >= MAX_FILE_SIZE) {
                /*if (!isDir) {
                    throw new RuntimeException("脚本文件大于10M，无法Push到Git");
                }*/
                continue;
            }
            dataDevScriptFile.setTypeStr(DataDevScriptTypeEnum.enumValueOf(dataDevScriptFile.getType()).toName());
            fixDataDevScriptFileList.add(dataDevScriptFile);
        }
        List<DataDevScriptFile> maryMerge = new ArrayList<DataDevScriptFile>();
        List<DataDevScriptFile> createDataDevScriptFile = new ArrayList<DataDevScriptFile>();
        List<DataDevScriptFile> updateDataDevScriptFile = new ArrayList<DataDevScriptFile>();
        List<DataDevScriptFile> deleteDataDevScriptFile = new ArrayList<DataDevScriptFile>();

        /**
         * 1。如果本地已经删除 && 已经同步Git 删除GIT
         * 2。无GitVersion 说明需要同步GIt create
         * 3。GitVersion和getLastGitVersion一样  && md5 不一样update
         * 4。GitVersion和getLastGitVersion一样  && md5 一样  什么都不处理
         * 5。GitVersion和getLastGitVersion不一样 需要merge
         */
        for (DataDevScriptFile dataBaseTemp : fixDataDevScriptFileList) {
            if (dataDevScriptDirService.isBelongTarget(dataBaseTemp.getGitProjectFilePath())) {
                continue;
            }
            if (dataBaseTemp.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode())) {
                continue;
            }
            //删除
            if (dataBaseTemp.getDeleted().equals(1) && StringUtils.isNotBlank(dataBaseTemp.getGitVersion())) {
                pushDeleteGit(dataDevGitProject, dataBaseTemp.getGitProjectFilePath(), erp);
                // deleteDataDevScriptFile.add(dataBaseTemp);
                continue;
            }
            if (StringUtils.isBlank(dataBaseTemp.getGitVersion()) || dataBaseTemp.getGitDeleted() == 1) {
                //新建
                createDataDevScriptFile.add(dataBaseTemp);
                continue;
            }

            if (StringUtils.isBlank(dataBaseTemp.getLastGitVersion())) {
                updateDataDevScriptFile.add(dataBaseTemp);
                continue;
            }
            //不可编辑文件直接更新
            if (!DataDevScriptTypeEnum.canEdit(dataBaseTemp.getType())) {
                updateDataDevScriptFile.add(dataBaseTemp);
                continue;
            }
            if (!dataBaseTemp.getGitVersion().equals(dataBaseTemp.getLastGitVersion())) {
                dataBaseTemp.setNewGitVersion(dataBaseTemp.getLastGitVersion());
                maryMerge.add(dataBaseTemp);
                continue;
            }
            if (dataBaseTemp.getGitVersion().equals(dataBaseTemp.getLastGitVersion())) {
                if (dataBaseTemp.getLastGitVersionMd5() != null && dataBaseTemp.getFileMd5() != null && !dataBaseTemp.getFileMd5().equals(dataBaseTemp.getLastGitVersionMd5())) {
                    updateDataDevScriptFile.add(dataBaseTemp);
                    continue;
                }
            }
        }
        //先处理冲突
        if (maryMerge.size() > 0) {
            return maryMerge;
        }
        batchPush(dataDevGitProject, createDataDevScriptFile, updateDataDevScriptFile, deleteDataDevScriptFile, commitMessage, erp, commitPath, isDir);
        return null;
    }

    /**
     * 直接push到Git
     *
     * @param createDataDevScriptFile
     */
    private void handNeePush(DataDevGitProject dataDevGitProject, List<DataDevScriptFile> createDataDevScriptFile, List<DataDevScriptFile> updateDataDevScriptFile, List<DataDevScriptFile> maryMerge, String commitMessage, String erp, String commitPath) throws Exception {
        for (DataDevScriptFile temp : createDataDevScriptFile) {
            try {
                JDGitFiles jdGitFiles = createOrUpdateJdGitFile(dataDevGitProject, temp, "create", commitMessage, erp);
            } catch (Exception e) {
                logger.error("==========================e:" + e.getMessage());
                JDGitFiles jdGitFile = createOrUpdateJdGitFile(dataDevGitProject, temp, "update", commitMessage, erp);
            }
        }
        for (DataDevScriptFile temp : updateDataDevScriptFile) {

            try {
                JDGitFiles jdGitFile = createOrUpdateJdGitFile(dataDevGitProject, temp, "update", commitMessage, erp);
            } catch (Exception e) {
                logger.error("============================e:" + e.getMessage());
                JDGitFiles jdGitFiles = createOrUpdateJdGitFile(dataDevGitProject, temp, "create", commitMessage, erp);
                maryMerge.remove(temp);
            }

        }

    }

    /**
     * 1。通过 commitPath 查询出这个git目录下所有的文件，如果是单个文件当前文件
     * * 计算出 createDataDevScriptFile 确定是create，如果git上已经存在，那么改成update。
     * * 计算出 updateDataDevScriptFile 确定是update，如果git上不存在，那么改成create。
     * * 计算出 deleteDataDevScriptFile 确定是delete，如果git上存在，那么删除。
     *
     * @param dataDevGitProject
     * @param createDataDevScriptFile
     * @param updateDataDevScriptFile
     * @param deleteDataDevScriptFile
     * @param commitMessage
     * @param erp
     * @param commitPath
     * @throws Exception
     */
    private void batchPush(DataDevGitProject dataDevGitProject, List<DataDevScriptFile> createDataDevScriptFile, List<DataDevScriptFile> updateDataDevScriptFile, List<DataDevScriptFile> deleteDataDevScriptFile, String commitMessage, String erp, String commitPath, boolean isDir) throws Exception {
        Long gitProjectId = dataDevGitProject.getGitProjectId();

        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitFiles.setBranch(dataDevGitProject.getBranch());
        jdGitFiles.setCommitMessage(commitMessage);
        jdGitFiles.setErp(erp);

        JSONArray actions = new JSONArray();

        caculateBatchPush(dataDevGitProject, createDataDevScriptFile, updateDataDevScriptFile, deleteDataDevScriptFile, commitPath, isDir);

        //添加Insert
        for (DataDevScriptFile insert : createDataDevScriptFile) {
            logger.error("===insert=" + insert.getGitProjectFilePath());
            boolean isBinary = !DataDevScriptTypeEnum.canEdit(insert.getType());
            JSONObject insertAction = new JSONObject();
            byte[] bytes = getScriptBytes(gitProjectId, insert.getGitProjectFilePath(), null, erp);
            insertAction.put("action", "create");
            insertAction.put("file_path", insert.getGitProjectFilePath());
            if (isBinary) {
                insertAction.put("content", Base64Util.StringToBase64(bytes));
                insertAction.put("encoding", "base64");
            } else {
                insertAction.put("content", new String(bytes, "utf-8"));
            }
            actions.add(insertAction);
        }
        //update
        for (DataDevScriptFile update : updateDataDevScriptFile) {
            logger.error("===update=" + update.getGitProjectFilePath());

            boolean isBinary = !DataDevScriptTypeEnum.canEdit(update.getType());
            JSONObject updateAction = new JSONObject();
            byte[] bytes = getScriptBytes(gitProjectId, update.getGitProjectFilePath(), null, erp);
            updateAction.put("action", "update");
            updateAction.put("file_path", update.getGitProjectFilePath());
            if (isBinary) {
                updateAction.put("content", Base64Util.StringToBase64(bytes));
                updateAction.put("encoding", "base64");
            } else {
                updateAction.put("content", new String(bytes, "utf-8"));
            }
            actions.add(updateAction);
        }
        //remvoe
        for (DataDevScriptFile delete : deleteDataDevScriptFile) {
            logger.error("===update=" + delete.getGitProjectFilePath());
            JSONObject deleteAction = new JSONObject();
            deleteAction.put("action", "delete");
            deleteAction.put("file_path", delete.getGitProjectFilePath());
            actions.add(deleteAction);
        }

        JDGitFiles result = jdGitFiles.doCommit(actions, commitMessage);

        //更新lastCommit，lastMd5
        List<DataDevScriptFile> totalUpdate = new ArrayList<DataDevScriptFile>();
        totalUpdate.addAll(createDataDevScriptFile);
        totalUpdate.addAll(updateDataDevScriptFile);
        for (DataDevScriptFile needUpdate : totalUpdate) {
            DataDevScriptFile updateParams = new DataDevScriptFile();
            updateParams.setGitVersion(result.getLastCommitId());
            updateParams.setLastGitVersion(result.getLastCommitId());
            updateParams.setLastGitVersionMd5(needUpdate.getFileMd5());
            updateParams.setGitDeleted(0);
            dataDevScriptFileDao.updateGitScriptFile(gitProjectId, needUpdate.getGitProjectFilePath(), updateParams);
        }

        for (DataDevScriptFile needDelete : deleteDataDevScriptFile) {
            dataDevScriptFileDao.realDeleteSingleScriptFile(dataDevGitProject.getGitProjectId(), needDelete.getGitProjectFilePath());
        }


       /* DataDevGitHis dataDevGitHis = new DataDevGitHis();
        dataDevGitHis.setComment(commitMessage);
        dataDevGitHis.setCommitId(result.getLastCommitId());
        dataDevGitHis.setCreator(erp);
        dataDevGitHis.setSubmitErp(erp);
        dataDevGitHis.setGitProjectId(gitProjectId);
        dataDevGitHis.setCommitPath(commitPath);
        dataDevGitHisDao.insertGitHis(dataDevGitHis);
*/
    }

    /**
     * 1。通过 commitPath 查询出这个git目录下所有的文件，如果是单个文件当前文件
     * * 计算出 createDataDevScriptFile 确定是create，如果git上已经存在，那么改成update。
     * * 计算出 updateDataDevScriptFile 确定是update，如果git上不存在，那么改成create。
     * * 计算出 deleteDataDevScriptFile 确定是delete，如果git上存在，那么删除。
     *
     * @param dataDevGitProject
     * @param createDataDevScriptFile
     * @param updateDataDevScriptFile
     * @param deleteDataDevScriptFile
     * @param commitPath
     * @param isDir
     * @throws Exception
     */
    private void caculateBatchPush(DataDevGitProject dataDevGitProject, List<DataDevScriptFile> createDataDevScriptFile, List<DataDevScriptFile> updateDataDevScriptFile, List<DataDevScriptFile> deleteDataDevScriptFile, String commitPath, boolean isDir) throws Exception {

        Set<String> tree = getTreeSet(commitPath, isDir, dataDevGitProject);

        Iterator<DataDevScriptFile> createIt = createDataDevScriptFile.iterator();
        while (createIt.hasNext()) {
            DataDevScriptFile create = createIt.next();
            String gitProjectFilePath = create.getGitProjectFilePath();
            boolean hasExists = tree.contains(gitProjectFilePath);
            if (hasExists) {
                updateDataDevScriptFile.add(create);
                createIt.remove();
            }
        }


        Iterator<DataDevScriptFile> updateIt = updateDataDevScriptFile.iterator();
        while (updateIt.hasNext()) {
            DataDevScriptFile update = updateIt.next();
            String gitProjectFilePath = update.getGitProjectFilePath();
            boolean hasExists = tree.contains(gitProjectFilePath);
            if (!hasExists) {
                createDataDevScriptFile.add(update);
                updateIt.remove();
            }
        }

        Iterator<DataDevScriptFile> deleteIt = deleteDataDevScriptFile.iterator();
        while (deleteIt.hasNext()) {
            DataDevScriptFile delete = deleteIt.next();
            String gitProjectFilePath = delete.getGitProjectFilePath();
            boolean hasExists = tree.contains(gitProjectFilePath);
            if (!hasExists) {
                deleteIt.remove();
            }
        }

    }

    /**
     * 获取tree
     *
     * @param commitPath
     * @param dataDevGitProject
     * @return
     * @throws Exception
     */
    private Set<String> getTreeSet(String commitPath, boolean isDir, DataDevGitProject dataDevGitProject) throws Exception {
        Set<String> treeSet = new HashSet<String>();
        if (isDir) {
            //获取目录下的tree
            JDGitRepositories jdGitRepositories = new JDGitRepositories();
            jdGitRepositories.setBranch(dataDevGitProject.getBranch());
            jdGitRepositories.setGitProjectId(dataDevGitProject.getGitProjectId());
            jdGitRepositories.setFilePath(commitPath);
            jdGitRepositories.setRecursive(true);
            List<JDGitRepositories> repositories = jdGitRepositories.treeAll();
            if (repositories != null && repositories.size() > 0) {
                for (JDGitRepositories repositorie : repositories) {
                    treeSet.add(repositorie.getPath());
                }
            }
        } else {
            //检查当前文件是否存在
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setBranch(dataDevGitProject.getBranch());
                jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
                jdGitFiles.setFilePath(commitPath);
                jdGitFiles.loadFileContent();
                treeSet.add(commitPath);
            } catch (GitFileNotFoundException e) {
                logger.error("getTreeSet", e);
            } catch (Exception e) {
                //coding 到时候不是返回404 ，返回500
                logger.error("getTreeSet", e);
            }

        }
        return treeSet;
    }

    private JDGitFiles createOrUpdateJdGitFile(DataDevGitProject dataDevGitProject, DataDevScriptFile temp, String createOrUpdate, String commitMessage, String erp) throws Exception {
        Long gitProjectId = dataDevGitProject.getGitProjectId();
        String branch = dataDevGitProject.getBranch();
        boolean isBinary = !DataDevScriptTypeEnum.canEdit(temp.getType());
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setFilePath(temp.getGitProjectFilePath());
        jdGitFiles.setBinary(isBinary);
        jdGitFiles.setErp(erp);
        jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitFiles.setBranch(branch);
        jdGitFiles.setName(temp.getName());
        jdGitFiles.setBytes(getScriptBytes(gitProjectId, temp.getGitProjectFilePath(), null, erp));
        JDGitFiles result = null;
        if (createOrUpdate.equals("create")) {
            result = jdGitFiles.addFile(commitMessage);
        } else {
            result = jdGitFiles.updateFile(commitMessage);
        }
        //更新数据库中 GitVersion字段 && 提交到Git都需要更新lastmd5
        DataDevScriptFile updateParams = new DataDevScriptFile();

        updateParams.setGitVersion(result.getLastCommitId());
        updateParams.setLastGitVersion(result.getLastCommitId());
        updateParams.setLastGitVersionMd5(temp.getFileMd5());
        updateParams.setGitDeleted(0);
        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, temp.getGitProjectFilePath(), updateParams);
        return result;
    }

    @Override
    public void createGitFile(DataDevGitProject dataDevGitProject, String fileName, String gitProjectFilePath, String erp, Integer scriptType, byte[] bytes) throws Exception {
        boolean isBinary = !DataDevScriptTypeEnum.canEdit(scriptType);

        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setFilePath(gitProjectFilePath);
        jdGitFiles.setBinary(isBinary);
        jdGitFiles.setErp(erp);
        jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitFiles.setBranch(dataDevGitProject.getBranch());
        jdGitFiles.setName(fileName);
        jdGitFiles.setBytes(bytes);
        JDGitFiles result = null;
        try {
            result = jdGitFiles.addFile("调度导入更新脚本");
        } catch (Exception e) {
            result = jdGitFiles.updateFile("调度导入更新脚本.");
        }
        if (result != null) {
            DataDevScriptFile updateParams = new DataDevScriptFile();
            updateParams.setGitVersion(result.getLastCommitId());
            updateParams.setLastGitVersion(result.getLastCommitId());
            updateParams.setLastGitVersionMd5(MD5Util.getMD5(bytes));
            dataDevScriptFileDao.updateGitScriptFile(dataDevGitProject.getGitProjectId(), gitProjectFilePath, updateParams);
        }
    }

    /**
     * 删除Git文件
     *
     * @param gitProjecFilePath
     * @param erp
     * @throws Exception
     */
    private void pushDeleteGit(DataDevGitProject dataDevGitProject, String gitProjecFilePath, String erp) throws Exception {
        try {
            JDGitFiles jdGitFiles = new JDGitFiles();
            jdGitFiles.setFilePath(gitProjecFilePath);
            jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
            jdGitFiles.setBranch(dataDevGitProject.getBranch());
            jdGitFiles.setErp(erp);
            jdGitFiles.deleteFile();
        } catch (Exception e) {
            logger.error("pushDeleteGit", e);

        }
        //在删除数据库文件
        dataDevScriptFileDao.realDeleteSingleScriptFile(dataDevGitProject.getGitProjectId(), gitProjecFilePath);
    }

    @Override
    public void deleteTmpFile() throws Exception {
        File file = new File(DATA_DEV_TMP_DIR_PATH);
        File[] listFiles = file.listFiles();
        for (File tmpFile : listFiles) {
            if (tmpFile.isFile() && tmpFile.lastModified() < (new Date().getTime() - TMP_FILE_LIVE_TIME)) {
                Matcher matcher = FILE_STATUS_PATTERN.matcher(tmpFile.getName());
                if (matcher.find()) {
                    String tmpName = matcher.group(1);
                    ReentrantReadWriteLock lock = tmpLockMap.get(tmpName);
                    if (lock != null) {
                        try {
                            lock.writeLock().lock();
                            if (tmpFile.lastModified() < (new Date().getTime() - TMP_FILE_LIVE_TIME)) {
                                tmpFile.delete();
                            }
                        } catch (Exception e) {
                            logger.error("===============删除文件报错：" + e.getMessage());
                        } finally {
                            tmpLockMap.remove(tmpName);
                            lock.writeLock().unlock();
                        }
                    }
                }
            }
        }

    }

    @Override
    public Integer countScriptFile(Long gitProjectId, String gitProjectFilePath) throws Exception {
        return dataDevScriptFileDao.countScriptFile(gitProjectId, gitProjectFilePath);
    }

    @Override
    public String getNoVersion() throws Exception {
        return NO_VERSION;
    }

    @Override
    public List<DataDevScriptFile> getDependencyFiles(Long dependencyId) throws Exception {
        return dataDevScriptFileDao.getDependencyFiles(dependencyId);
    }

    @Override
    public Page<DataDevScriptFile> findByScriptUpLoadId(Long scriptUpLoadId, Pageable pageable) {
        int total = dataDevScriptFileDao.findCount(scriptUpLoadId);
        List<DataDevScriptFile> list = dataDevScriptFileDao.findByScriptUpLoadId(scriptUpLoadId, pageable.getOffset(), pageable.getPageSize());
        double size = 0;
        DecimalFormat df = new DecimalFormat("#.00");
        int B = 1024;
        int MB = 1024 * B;
        for (DataDevScriptFile dataDevScriptFile : list) {
            size = dataDevScriptFile.getSize();
            if (size < B) {
                dataDevScriptFile.setSizeShow(size + " B");
            } else if (size < MB) {
                size /= 1024;
                dataDevScriptFile.setSizeShow(df.format(size) + " KB");
            } else {
                size /= MB;
                dataDevScriptFile.setSizeShow(df.format(size) + " MB");
            }
        }
        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public List<DataDevScriptFile> getNoLastGitVersionMd5(Long gitProjectId) {
        return dataDevScriptFileDao.getNoLastGitVersionMd5(gitProjectId);
    }

    @Override
    public Integer initScriptType(Integer limit) throws Exception {
        limit = limit != null && limit > 0 ? limit : 1000;
        DataDevScriptFile updateFile = new DataDevScriptFile();
        List<DataDevScriptFile> scriptFiles = dataDevScriptFileDao.getScriptsByType(DataDevScriptTypeEnum.Other.toCode(), limit);
        if (scriptFiles.size() > 0) {
            for (DataDevScriptFile file : scriptFiles) {
                updateFile.setType(DataDevScriptTypeEnum.getFileNameScriptType(file.getName()).toCode());
                dataDevScriptFileDao.updateGitScriptFile(file.getGitProjectId(), file.getGitProjectFilePath(), updateFile);
            }
        }
        return scriptFiles.size();

    }

    @Override
    public void initScriptHisType() throws Exception {
        dataDevScriptFileDao.initScriptHisType();
    }

    @Override
    public void tmpFileUpdateHisAndDetail(Long scriptFileId, String gitProjectFilePath, String scriptFileName, Long gitProjectId, String oldGirProjectFilePath) throws Exception {

        logger.error("===scriptFileId:" + scriptFileId);
        logger.error("===gitProjectFilePath:" + gitProjectFilePath);
        logger.error("===scriptFileName:" + scriptFileName);
        logger.error("===gitProjectId:" + gitProjectId);
        logger.error("===oldGirProjectFilePath:" + oldGirProjectFilePath);

        DataDevScriptFile dataDevScriptFile = new DataDevScriptFile();

        dataDevScriptFile.setGitProjectFilePath(gitProjectFilePath);
        dataDevScriptFile.setName(scriptFileName);
        dataDevScriptFile.setIsShow(0);
        String dirPath = gitProjectFilePath.contains("/") ? gitProjectFilePath.substring(0, gitProjectFilePath.lastIndexOf("/")) : "";
        dataDevScriptFile.setGitProjectDirPath(dirPath);
        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, oldGirProjectFilePath, dataDevScriptFile);
        dataDevScriptFileHisDao.updateScriptFileHis(scriptFileId, gitProjectFilePath, scriptFileName);
        dataDevScriptRunDetailDao.updateRunDetailPath(gitProjectId, oldGirProjectFilePath, gitProjectFilePath);
    }

    /**
     * 项目启动的时候删除临时文件(7天以前)
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            File file = new File(DATA_DEV_TMP_DIR_PATH);
            if (file != null && file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File temp : files) {
                        if (temp.lastModified() < (System.currentTimeMillis() - TMP_FILE_LIVE_TIME)) {
                            FileUtils.deleteQuietly(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("inin project ====delete temp file", e);
        }

    }
}
