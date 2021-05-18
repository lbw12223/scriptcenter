package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SplitFileUtils;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.dao.DataDevScriptDirDao;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.*;
import com.jd.bdp.datadev.jdgit.Base64Util;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.jdgit.JDGitRepositories;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptDirService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataDevScriptDirServiceImpl implements DataDevScriptDirService {

    private static final String REDIS_LOCK_DIR_KEY = "datadev.lock.dir.key_%s";

    @Autowired
    private DataDevScriptDirDao dataDevScriptDirDao;

    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao;

    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    private static final Logger logger = Logger.getLogger(DataDevScriptDirServiceImpl.class);

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private UrmUtil urmUtil;

    @Autowired
    private LockUtil lockUtil;


    public static final String TARGET_DIR_NAME = "BDP.TARGET";


    @Override
    public void getRecursiveDir(Set<String> set, List<DataDevScriptDir> list, Long gitProjectId, String gitProjectDirPath) throws Exception {
        if (set.contains(gitProjectDirPath)) {
            return;
        }
        DataDevScriptDir dir = dataDevScriptDirDao.getDataDevScriptDirBy(gitProjectId, gitProjectDirPath);
        if (dir != null) {
            set.add(dir.getGitProjectDirPath());
            list.add(dir);
            if (StringUtils.isNotBlank(dir.getGitParentProjectDirPath())) {
                getRecursiveDir(set, list, gitProjectId, dir.getGitParentProjectDirPath());
            }
        }
    }


    @Override
    public Long insertDir(DataDevScriptDir dir) throws Exception {
        return dataDevScriptDirDao.insertDir(dir);
    }

    @Override
    public DataDevScriptDir getDataDevScriptDir(Long gitProjectId, String path) {
        return dataDevScriptDirDao.getDataDevScriptDirBy(gitProjectId, path);
    }


    @Override
    public List<DataDevScriptDir> getDataDevScriptDirByPid(Long gitProjectId, Long pId) {
        return dataDevScriptDirDao.getDirsByGitProjectId(gitProjectId, pId);
    }

    @Override
    public List<ZtreeNode> getAllDataDevScriptDir(Long gitProjectId, boolean isTargetSelect) {
        List<DataDevScriptDir> list = dataDevScriptDirDao.getDirsByProjectId(gitProjectId);
        String targetDirName = TARGET_DIR_NAME + "/";
        List<ZtreeNode> ztreeNodeList = new ArrayList<ZtreeNode>();
        for (DataDevScriptDir dir : list) {
            if (!isTargetSelect && (dir.getName().equals(TARGET_DIR_NAME) || dir.getName().startsWith(targetDirName))) {
                continue;
            }
            ZtreeNode ztreeNode = new ZtreeNode(dir);
            ztreeNodeList.add(ztreeNode);
        }
        return ztreeNodeList;
    }

    /**
     * 非select时只获取一层
     *
     * @param gitProjectId   项目id
     * @param queryDirPath   查找的目录
     * @param openDirs       保持打开状态的目录
     * @param selectFilePath 选择的脚本
     * @param selectDirPath  选择的目录
     * @param range          0全部文件  1只获取目录
     * @param isRootSelect   是否选择根目录文件,不重复选择根目录文件，因为一开始就加载了，只加载缺的文件
     * @param targetRange    0：不选择target目录及子文件目录  1：所有文件  2：只选择target目录及子文件目录
     * @return
     * @throws Exception
     */
    @Override
    public List<ZtreeNode> getDirFiles(Long gitProjectId, String queryDirPath, String[] openDirs, String[] selectFilePath, String selectDirPath, Integer range, boolean isRootSelect, Integer targetRange) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (!dataDevGitProject.getFinishProjectTreeFlag().equals(DataDevGitInitFlag.INIT_FINISH.tocode())) {
            throw new RuntimeException(dataDevGitProject.getGitProjectName() + "正在初始化...");
        }
        String parentDirPath = "";
        if (!StringUtils.isBlank(queryDirPath)) {
            DataDevScriptDir parentDir = getDataDevScriptDir(gitProjectId, queryDirPath);
            if (parentDir != null) {
                parentDirPath = parentDir.getGitProjectDirPath();
            }
        } else {
            //如果是空标明是根目录，需要判断是否有target目录
            createTargetDir(gitProjectId);
        }
        Set<String> openDirSet = openDirs != null ? new HashSet<String>(Arrays.asList(openDirs)) : new HashSet<String>();
        Set<String> selectFileSet = selectFilePath != null ? new HashSet<String>(Arrays.asList(selectFilePath)) : new HashSet<String>();
        List<ZtreeNode> ztreeNodeList = getDirZtreeNode(gitProjectId, parentDirPath, openDirSet);
        if (selectFilePath != null && selectFilePath.length > 0 || StringUtils.isNotBlank(selectDirPath) || openDirs != null && openDirs.length > 0) {
            List<String> dirList = Base64Util.getSelectDirs(parentDirPath, selectFilePath, selectDirPath);
            String queryPathWithSep = Base64Util.formatName(parentDirPath) + "/";
            for (String dir : openDirSet) {
                if (StringUtils.isNotBlank(dir) && !dirList.contains(dir) && (StringUtils.isBlank(queryDirPath) || (StringUtils.isNotBlank(dir) && dir.startsWith(queryPathWithSep)))) {
                    dirList.add(dir);
                }
            }
            for (String dirStr : dirList) {
                List<ZtreeNode> tmpList = getDirZtreeNode(gitProjectId, dirStr, openDirSet);
                ztreeNodeList.addAll(tmpList);
            }
        }
        if (range == 1 || !isRootSelect || targetRange != 1) {
            List<ZtreeNode> copy = new ArrayList<ZtreeNode>();
            for (ZtreeNode ztreeNode : ztreeNodeList) {
                if (range == 1 && ztreeNode.getParChl() == 1) {
                    //选择目录，过滤掉文件
                    continue;
                }
                if (!isRootSelect && StringUtils.isBlank(ztreeNode.getParentPath()) && !selectFileSet.contains(ztreeNode.getPath())) {
                    //排除重复选择根目录文件
                    continue;
                }
                if (targetRange == 0 && (ztreeNode.getPath().startsWith(Base64Util.formatName(TARGET_DIR_NAME) + "/") || ztreeNode.isDir() && ztreeNode.getPath().equals(TARGET_DIR_NAME))) {
                    //当选择不显示target目录时，排除target目录及子目录
                    continue;
                }
                if (targetRange == 2 && !(ztreeNode.getPath().startsWith(Base64Util.formatName(TARGET_DIR_NAME) + "/") || ztreeNode.isDir() && ztreeNode.getPath().equals(TARGET_DIR_NAME))) {
                    //当选择只显示target目录时，排除非target目录
                    continue;
                }
                copy.add(ztreeNode);
            }
            if (ztreeNodeList != null) {
                ztreeNodeList.clear();
            }
            ztreeNodeList = copy;
        }
        checkSelect(ztreeNodeList, selectFileSet);
        return ztreeNodeList;
    }

    private void checkSelect(List<ZtreeNode> ztreeNodes, Collection<String> selectFilePaths) {
        Set<String> set = new HashSet<>();
        for (String selectFile : selectFilePaths) {
            set.addAll(Base64Util.getDirsOfFile("", selectFile));
        }
        set.addAll(selectFilePaths);
        for (ZtreeNode ztreeNode : ztreeNodes) {
            if (set.contains(ztreeNode.getPath())) {
                ztreeNode.setSelected(1);
            }
        }
    }

    public List<ZtreeNode> getAllDirsAndFiles(Long gitProjectId, String rootPath) {
        return getAllDirsAndFiles(gitProjectId, rootPath, 0);
    }

    @Override
    public List<ZtreeNode> getAllDirsAndFiles(Long gitProjectId, String rootPath, Integer mode) {
        List<ZtreeNode> list = new ArrayList<>();
        if (rootPath == null) {
            rootPath = "";
        }
        rootPath = StringUtils.isNotBlank(rootPath.trim()) ? (Base64Util.formatName(rootPath.trim()) + "/") : "";
        List<DataDevScriptDir> dirs = dataDevScriptDirDao.getAllDataDevScriptDirsByRoot(gitProjectId, rootPath);
        if (dirs != null && dirs.size() > 0) {
            for (DataDevScriptDir dir : dirs) {
                ZtreeNode ztreeNode = new ZtreeNode(dir);
                list.add(ztreeNode);
            }
        }
        if (mode != 1) {
            List<DataDevScriptFile> dataDevScriptFiles = dataDevScriptFileDao.getScriptDirFileRecur(gitProjectId, rootPath);
            if (dataDevScriptFiles != null && dataDevScriptFiles.size() > 0) {
                for (DataDevScriptFile file : dataDevScriptFiles) {
                    ZtreeNode ztreeNode = new ZtreeNode(file);
                    list.add(ztreeNode);
                }
            }
        }
        return list;
    }

    /**
     * 获取 & 转换 dir下面的文件 和 文件夹
     *
     * @param gitProjectId
     * @param parentDirPath
     * @return
     */
    private List<ZtreeNode> getDirZtreeNode(Long gitProjectId, String parentDirPath, Set<String> openDirSet) {
        List<ZtreeNode> ztreeNodes = new ArrayList<ZtreeNode>();
        List<DataDevScriptDir> dirs = dataDevScriptDirDao.findSubDirsByGitProjectId(parentDirPath, gitProjectId);
        ZtreeNode targetDir = null;
        if (dirs != null && dirs.size() > 0) {
            for (DataDevScriptDir dir : dirs) {
                ZtreeNode ztreeDirNode = new ZtreeNode();
                ztreeDirNode.setId(dir.getId());
                ztreeDirNode.setParentPath(parentDirPath);
                ztreeDirNode.setParChl(0);
                ztreeDirNode.setName(dir.getName());
                ztreeDirNode.setPath(dir.getGitProjectDirPath());
                ztreeDirNode.setTargetDir(TARGET_DIR_NAME.equals(ztreeDirNode.getPath()));
                if (openDirSet.contains(dir.getGitProjectDirPath())) {
                    ztreeDirNode.setOpenStatus(1);
                }
                ztreeDirNode.setGitProjectId(gitProjectId);
                if (TARGET_DIR_NAME.equals(ztreeDirNode.getPath())) {
                    targetDir = ztreeDirNode;
                } else {
                    ztreeNodes.add(ztreeDirNode);
                }
            }
        }
        if (targetDir != null) {
            ztreeNodes.add(0, targetDir);
        }
        List<DataDevScriptFile> scriptsByGitProjectIdAndDirPath = dataDevScriptFileService.getScriptsByGitProjectIdAndDirPath(gitProjectId, parentDirPath);
        List<DataDevScriptFile> files = scriptsByGitProjectIdAndDirPath;
        if (files != null && files.size() > 0) {
            for (DataDevScriptFile file : files) {
                ZtreeNode ztreeFileNode = new ZtreeNode();
                ztreeFileNode.setId(file.getId());
                ztreeFileNode.setParentPath(parentDirPath);
                ztreeFileNode.setParChl(1);
                ztreeFileNode.setName(file.getName());
                ztreeFileNode.setPath(file.getGitProjectFilePath());
                ztreeFileNode.setType(file.getType());
                ztreeFileNode.setGitProjectId(gitProjectId);
                ztreeFileNode.setRunType((file.getDependencyId() != null && file.getDependencyId() > 0) ? DataDevRunTypeEnum.DependencyRun.tocode() : DataDevRunTypeEnum.SingeRun.tocode());
                if (file.getGitProjectFilePath().startsWith(TARGET_DIR_NAME + "/")) {
                    ztreeFileNode.setGitStatus(DataDevScriptGitStatusEnum.NON.toCode());
                } else {
                    ztreeFileNode.setGitStatus(DataDevScriptGitStatusEnum.getGitStatus(file.getGitVersion(), file.getLastGitVersion(), file.getFileMd5(), file.getLastGitVersionMd5(), file.getDeleted()).toCode());
                }
                ztreeNodes.add(ztreeFileNode);
            }
        }
        return ztreeNodes;
    }

    /**
     * load  file tree
     * <p>
     * 1。改成 递归获取 同时插入数据库中保证 file_id dir_id 的关系正确
     * 2。Git 获取到的数据结构始终是 所有的dir在前面（并且按照dir的父子关系父在前） 文件在最后
     *
     * <p>
     * 这个方法只是在GIT项目第一次获取的时候使用
     *
     * @param dataDevGitProject
     */
    @Override
    public void loadGitFileTree(DataDevGitProject dataDevGitProject, String gitParentDirPath, Long parentDirId, String queryDirPath) throws Exception {
        JDGitRepositories jdGitRepositories = new JDGitRepositories();
        jdGitRepositories.setBranch(dataDevGitProject.getBranch());
        jdGitRepositories.setRecursive(true);
        jdGitRepositories.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitRepositories.setFilePath(queryDirPath);
        List<JDGitRepositories> trees = jdGitRepositories.treeAll();
        List<DataDevScriptFile> inserts = new ArrayList<>();

        /**
         * parent dirs
         */
        Map<String, Long> parentDirs = new HashMap<String, Long>();
        parentDirs.put(gitParentDirPath, parentDirId);
        if (trees != null && trees.size() > 0) {
            for (JDGitRepositories repositories : trees) {
                if (repositories.getType().equals("blob")) {
                    //file
                    HoldDoubleValue<String, String> parentAndFilePath = Base64Util.splitFilePath(repositories.getPath());
                    Long pid = getParentDir(parentAndFilePath.a, parentDirs);
                    DataDevScriptFile dataDevScriptFile = new DataDevScriptFile();
                    Long gitProjectId = repositories.getGitProjectId();
                    String gitProjectFilePath = repositories.getPath();

                    dataDevScriptFile.setName(repositories.getName());
                    dataDevScriptFile.setGitProjectId(gitProjectId);
                    dataDevScriptFile.setGitProjectFilePath(gitProjectFilePath);
                    dataDevScriptFile.setGitProjectDirPath(parentAndFilePath.a);
                    dataDevScriptFile.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.GIT.tocode());
                    dataDevScriptFile.setDirId(pid);
                    dataDevScriptFile.setType(DataDevScriptTypeEnum.getFileNameScriptType(repositories.getName()).toCode());
                    dataDevScriptFile.setGitVersion("");
                    dataDevScriptFile.setFileMd5("");
                    dataDevScriptFile.setVersion("");
                    dataDevScriptFile.setIsShow(0);
                    dataDevScriptFile.setIsBigFile(0);
                    dataDevScriptFile.setCreator(GitHttpUtil.getPrivetUser());
                    dataDevScriptFile.setMender(GitHttpUtil.getPrivetUser());


                    DataDevScriptFile dataBase = dataDevScriptFileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
                    if (dataBase != null) {
                        dataDevScriptFileDao.updateGitScriptFile(gitProjectId, gitProjectFilePath, dataDevScriptFile);
                    } else {
                        //获取md5 version 2018-12-20
                        JDGitFiles jdGitFiles = new JDGitFiles();
                        jdGitFiles.setBinary(!DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType()));
                        jdGitFiles.setGitProjectId(gitProjectId);
                        jdGitFiles.setFilePath(gitProjectFilePath);
                        jdGitFiles.loadFileContent();
                        if (StringUtils.isNotBlank(jdGitFiles.getLastCommitId())) {
                            dataDevScriptFile.setLastGitVersion(jdGitFiles.getLastCommitId());
                            dataDevScriptFile.setLastGitVersionMd5(jdGitFiles.getFileMd5());
                        }
                        dataDevScriptFile.setGitVersion(jdGitFiles.getLastCommitId());
                        dataDevScriptFile.setFileMd5(jdGitFiles.getFileMd5());
                        dataDevScriptFile.setBytes(jdGitFiles.getBytes() != null ? jdGitFiles.getBytes() : jdGitFiles.getContent().getBytes("utf-8"));
                        inserts.add(dataDevScriptFile);
                    }
                } else {
                    //dir
                    HoldDoubleValue<String, String> parentAndDirPath = Base64Util.splitDirPath(repositories.getPath());
                    Long pid = getParentDir(parentAndDirPath.a, parentDirs);
                    DataDevScriptDir dataDevScriptDir = new DataDevScriptDir();
                    dataDevScriptDir.setpId(pid);
                    dataDevScriptDir.setName(repositories.getName());
                    dataDevScriptDir.setGitParentProjectDirPath(parentAndDirPath.a);
                    dataDevScriptDir.setGitProjectDirPath(repositories.getPath());
                    dataDevScriptDir.setGitProjectId(repositories.getGitProjectId());
                    DataDevScriptDir dataBaseDir = dataDevScriptDirDao.getDataDevScriptDirBy(repositories.getGitProjectId(), repositories.getPath());
                    if (dataBaseDir == null) {
                        insertDir(dataDevScriptDir);
                    } else {
                        dataDevScriptDir = dataBaseDir;
                    }
                    parentDirs.put(repositories.getPath(), dataDevScriptDir.getId());
                }

            }
            if (inserts.size() > 0) {
                dataDevScriptFileService.insertFiles(inserts);
                for (DataDevScriptFile dataDevScriptFile : inserts) {
                   //先不保存文件
                   //dataDevScriptFileService.tryUpdateFile(dataDevGitProject.getGitProjectId(), dataDevScriptFile.getGitProjectFilePath(), dataDevScriptFile.getCreator(), dataDevScriptFile.getBytes(), dataDevScriptFile.getVersion(), dataDevScriptFile.getLastGitVersion(), true);
                }
            }
        }
    }

    private Long getParentDir(String path, Map<String, Long> parentDirs) {
        Long result = parentDirs.get(path);
        return result != null ? result : 0;
    }


    /**
     * 在数据库里面创建dir
     *
     * @param gitProjectId
     * @param dirPath
     * @param erp
     * @return
     * @throws Exception
     */
    @Override
    public ZtreeNode createScriptDir(Long gitProjectId, String dirPath, String erp) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        if (dataDevGitProject == null) {
            throw new RuntimeException("" + gitProjectId + "不存在");
        }
        JDGitFiles result = new JDGitFiles();
        result.setFilePath(dirPath);
        result.setGitProjectId(gitProjectId);
        result.setErp(erp);
        //在数据库里面创建文件夹或者文件
        return createFileOrDirInDataBase(result, true);
    }

    /**
     * AddDir ， addFile 根据Post到Git后，在database里面创建对应的scriptDir，scriptFile
     */
    @Override
    public ZtreeNode createFileOrDirInDataBase(JDGitFiles jdGitFiles, boolean isCreateDir) {


        List<ZtreeNode> subs = new ArrayList<ZtreeNode>();
        String filePath = jdGitFiles.getFilePath();
        Long gitProjectId = jdGitFiles.getGitProjectId();
        String erp = jdGitFiles.getErp();

        String[] dirs = filePath.split("\\/+");
        String baseDir = "";
        Long parentDirId = 0l;
        for (int index = 0, length = dirs.length; index < length; index++) {
            String dir = dirs[index];
            if (StringUtils.isNotBlank(dir)) {
                String dirPath = baseDir + "/" + dir;
                if (index == (length - 1) && !isCreateDir) {
                    ZtreeNode subNode = createScriptFile(jdGitFiles, parentDirId);
                    subs.add(subNode);
                } else {
                    if (StringUtils.isBlank(baseDir)) {
                        dirPath = dir;
                    }
                    DataDevScriptDir dataDevScriptDir = createDataBaseDir(gitProjectId, dirPath, erp, parentDirId);
                    subs.add(covertDataDevScriptDir(dataDevScriptDir));
                    //重新设置parentId ，baseDir
                    parentDirId = dataDevScriptDir.getId();
                    baseDir = dataDevScriptDir.getGitProjectDirPath();
                }

            }
        }
        return mergeZtreeNode(subs);
    }

    /**
     * 分解path
     */
    @Override
    public ZtreeNode createFilePathZtreeNode(Long gitProjectId, String filePath, String version) {
        filePath = Base64Util.formatName(filePath);
        List<ZtreeNode> subs = new ArrayList<ZtreeNode>();
        String[] dirs = filePath.split("\\/+");
        String baseDir = "";
        for (int index = 0, length = dirs.length; index < length; index++) {
            String dir = dirs[index];
            if (StringUtils.isNotBlank(dir)) {
                String dirPath = baseDir + "/" + dir;
                if (index == (length - 1)) {
                    ZtreeNode subNode = new ZtreeNode();
                    subNode.setGitProjectId(gitProjectId);
                    subNode.setPath(filePath);
                    subNode.setVersion(version);
                    subNode.setParentPath(baseDir);
                    subs.add(subNode);
                } else {
                    if (StringUtils.isBlank(baseDir)) {
                        dirPath = dir;
                    }
                    ZtreeNode subNode = new ZtreeNode();
                    subNode.setGitProjectId(gitProjectId);
                    subNode.setPath(dirPath);
                    subNode.setParentPath(baseDir);
                    subs.add(subNode);
                    //重新设置parentId ，baseDir
                    baseDir = dirPath;
                }

            }
        }
        return mergeZtreeNode(subs);
    }

    /**
     * 组成tree结构
     *
     * @param subs
     * @return
     */
    private ZtreeNode mergeZtreeNode(List<ZtreeNode> subs) {
        if (subs != null && subs.size() > 0) {
            ZtreeNode root = subs.get(0);
            for (int index = 1; index < subs.size(); index++) {
                ZtreeNode sub = subs.get(index);
                root.setChildren(Arrays.asList(sub));
                root = sub;
            }
            return subs.get(0);
        }
        return new ZtreeNode();
    }

    /**
     * 转换 dir to ztreeNode
     *
     * @param dataDevScriptDir
     * @return
     */
    private ZtreeNode covertDataDevScriptDir(DataDevScriptDir dataDevScriptDir) {
        ZtreeNode dirNode = new ZtreeNode();
        dirNode.setpId(dataDevScriptDir.getpId());
        dirNode.setParentPath(dataDevScriptDir.getGitParentProjectDirPath());
        dirNode.setParChl(0);
        dirNode.setPath(dataDevScriptDir.getGitProjectDirPath());
        dirNode.setName(dataDevScriptDir.getName());
        dirNode.setId(dataDevScriptDir.getId());
        dirNode.setGitProjectId(dataDevScriptDir.getGitProjectId());
        return dirNode;
    }

    /**
     * 在目录下面创建文件
     */
    private ZtreeNode createScriptFile(JDGitFiles jdGitFiles, Long parentDirId) {
        ZtreeNode fileNode = new ZtreeNode();

        String filePath = jdGitFiles.getFilePath();
        Long gitProjectId = jdGitFiles.getGitProjectId();
        String erp = jdGitFiles.getErp();
        jdGitFiles.setVersion("1000");

        DataDevScriptFile dataDevScriptFile = dataDevScriptFileDao.getSingleScriptFileIgnoreDeleted(gitProjectId, filePath);
        if (dataDevScriptFile == null) {
            HoldDoubleValue<String, String> parentAndFile = Base64Util.splitFilePath(filePath);
            dataDevScriptFile = new DataDevScriptFile();
            dataDevScriptFile.setDirId(parentDirId);
            dataDevScriptFile.setMender(erp);
            dataDevScriptFile.setCreator(erp);
            dataDevScriptFile.setGitProjectId(gitProjectId);
            dataDevScriptFile.setGitVersion(jdGitFiles.getLastCommitId());
            dataDevScriptFile.setVersion(jdGitFiles.getVersion());
            dataDevScriptFile.setName(parentAndFile.b);
            dataDevScriptFile.setGitProjectDirPath(parentAndFile.a);
            dataDevScriptFile.setGitProjectFilePath(filePath);
            dataDevScriptFile.setIsBigFile(SplitFileUtils.isBigFile(jdGitFiles.getSize()));
            dataDevScriptFile.setDescription(jdGitFiles.getDescription());
            dataDevScriptFile.setStartShellPath(jdGitFiles.getStartShellPath());
            dataDevScriptFile.setScriptUpLoadId(jdGitFiles.getScriptUploadId() != null ? jdGitFiles.getScriptUploadId() : 0);
            dataDevScriptFile.setArgs(jdGitFiles.getArgs());
            dataDevScriptFile.setFileMd5(jdGitFiles.getFileMd5());

            if (StringUtils.isNotBlank(jdGitFiles.getLastCommitId())) {
                dataDevScriptFile.setLastGitVersionMd5(jdGitFiles.getFileMd5());
                dataDevScriptFile.setLastGitVersion(jdGitFiles.getLastCommitId());
            }
            if (jdGitFiles.getSize() == null || jdGitFiles.getSize() <= 0L) {
                dataDevScriptFile.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.NOCONTENT.tocode());
            } else {
                dataDevScriptFile.setWhereIsNew(DataDevScriptFileWhereIsNewEnum.HBASE.tocode());
            }
            if (jdGitFiles.getWhereIsNew() != null) {
                dataDevScriptFile.setWhereIsNew(jdGitFiles.getWhereIsNew());
            }
            dataDevScriptFile.setType(DataDevScriptTypeEnum.getFileNameScriptType(parentAndFile.b).toCode());
            dataDevScriptFile.setSize(jdGitFiles.getSize());
            dataDevScriptFile.setIsShow(jdGitFiles.getIsShow());

            dataDevScriptFileDao.insertScriptFile(dataDevScriptFile);
        } else {
            DataDevScriptFile updateDataDev = new DataDevScriptFile();
            updateDataDev.setDirId(parentDirId);
            updateDataDev.setDeleted(0);
            updateDataDev.setMender(jdGitFiles.getErp());
            updateDataDev.setGitVersion(jdGitFiles.getLastCommitId());
            updateDataDev.setLastGitVersion(jdGitFiles.getLastCommitId());
            updateDataDev.setLastGitVersionMd5(jdGitFiles.getFileMd5());
            updateDataDev.setDescription(jdGitFiles.getDescription());
            updateDataDev.setWhereIsNew(jdGitFiles.getWhereIsNew());
            dataDevScriptFileDao.updateGitScriptFile(gitProjectId, filePath, updateDataDev);
        }
        fileNode.setGitStatus(DataDevScriptGitStatusEnum.getGitStatus(jdGitFiles.getLastCommitId(), jdGitFiles.getLastCommitId(), jdGitFiles.getFileMd5(), jdGitFiles.getFileMd5(), null).toCode());
        fileNode.setpId(parentDirId);
        fileNode.setParentPath(dataDevScriptFile.getGitProjectDirPath());
        fileNode.setParChl(1);
        fileNode.setPath(dataDevScriptFile.getGitProjectFilePath());
        fileNode.setName(dataDevScriptFile.getName());
        fileNode.setType(dataDevScriptFile.getType());
        fileNode.setId(dataDevScriptFile.getId());
        fileNode.setGitProjectId(dataDevScriptFile.getGitProjectId());
        fileNode.setVersion(dataDevScriptFile.getVersion());
        fileNode.setMd5(jdGitFiles.getFileMd5());
        return fileNode;

    }


    /**
     * @param gitProjectId
     * @param dirPath
     * @param erp
     * @param parentDirId
     * @return
     */
    private DataDevScriptDir createDataBaseDir(Long gitProjectId, String dirPath, String erp, Long parentDirId) {
        boolean hasLock = false;
        String requestId = UUID.randomUUID().toString();
        try {
            DataDevScriptDir dataDevScriptDir = getDataDevScriptDir(gitProjectId, dirPath);
            if (dataDevScriptDir != null) {
                return dataDevScriptDir;
            }

            hasLock = lockUtil.tryLock(String.format(REDIS_LOCK_DIR_KEY, dirPath), requestId, 10);
            DataDevScriptDir temp = getDataDevScriptDir(gitProjectId, dirPath);
            if (temp != null) {
                return temp;
            }
            //检查dirPath是否存在，不存在插入数据

            HoldDoubleValue<String, String> splitPaths = Base64Util.splitDirPath(dirPath);
            DataDevScriptDir inesrtDataDevScriptDir = new DataDevScriptDir();
            inesrtDataDevScriptDir.setGitProjectId(gitProjectId);
            inesrtDataDevScriptDir.setName(splitPaths.b);
            inesrtDataDevScriptDir.setGitProjectDirPath(dirPath);
            inesrtDataDevScriptDir.setGitParentProjectDirPath(splitPaths.a);
            inesrtDataDevScriptDir.setCreator(erp);
            inesrtDataDevScriptDir.setpId(parentDirId);
            inesrtDataDevScriptDir.setMender(erp);
            dataDevScriptDirDao.insertDir(inesrtDataDevScriptDir);
            return inesrtDataDevScriptDir;

        } catch (Exception e) {
            throw e;
        } finally {
            if (hasLock) {
                lockUtil.unLock(String.format(REDIS_LOCK_DIR_KEY, dirPath), requestId);
            }
        }

    }


    /**
     * 删除Git Dir Path
     * <p>
     * 1. 查询scriptFile表中，在当前文件夹下面的所有的文件（包括子文件夹）-> 删除Git - > 删除数据库（scriptDir & scriptFile ）
     * <p>
     * 2. 查询Git当前文件夹下所有的文件(递归)-> 删除Git - > 删除数据库（scriptDir & scriptFile ）更彻底
     * <p>
     * 目前选择的第一种方法
     *
     * @param gitProjectId
     * @param gitProjectDirPath (eg. app app/file2)
     * @param erp
     * @throws Exception
     */
    @Override
    public void deleteDir(Long gitProjectId, String gitProjectDirPath, String erp) throws Exception {
        gitProjectDirPath = Base64Util.formatName(gitProjectDirPath);
        String gitProjectDirPathWithSep = gitProjectDirPath + "/";
        List<DataDevScriptFile> dataDevScriptFiles = dataDevScriptFileDao.getScriptDirFileRecur(gitProjectId, gitProjectDirPathWithSep);
        if (dataDevScriptFiles != null && dataDevScriptFiles.size() > 0) {
            throw new RuntimeException("当前目录或子目录存在文件，不能删除!");
        }
        dataDevScriptDirDao.deleteScriptDirRecursion(gitProjectId, gitProjectDirPath, gitProjectDirPathWithSep);
    }

    private List<String> getRemoteGitFiles(DataDevGitProject dataDevGitProject, String gitProjectDirPath) throws Exception {
        List<String> files = new ArrayList<String>();
        JDGitRepositories jdGitRepositories = new JDGitRepositories();
        jdGitRepositories.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitRepositories.setFilePath(gitProjectDirPath);
        jdGitRepositories.setRecursive(true);
        jdGitRepositories.setBranch(dataDevGitProject.getBranch());
        List<JDGitRepositories> repositories = jdGitRepositories.treeAll();
        for (JDGitRepositories temp : repositories) {
            if (temp.getType().equals("blob")) {
                String filePath = temp.getPath();
                files.add(filePath);
            }
        }
        return files;
    }

    @Override
    public void createTargetDir(Long gitProjectId) throws Exception {
        DataDevScriptDir dir = dataDevScriptDirDao.getDataDevScriptDirBy(gitProjectId, TARGET_DIR_NAME);
        if (dir == null) {
            DataDevScriptDir insertDataDevScriptDir = new DataDevScriptDir();
            insertDataDevScriptDir.setGitProjectId(gitProjectId);
            insertDataDevScriptDir.setName(TARGET_DIR_NAME);
            insertDataDevScriptDir.setGitProjectDirPath(TARGET_DIR_NAME);
            insertDataDevScriptDir.setGitParentProjectDirPath("");
            insertDataDevScriptDir.setCreator(urmUtil.getBdpManager());
            insertDataDevScriptDir.setMender(urmUtil.getBdpManager());
            dataDevScriptDirDao.insertDir(insertDataDevScriptDir);
        }
    }

    @Override
    public List<ZtreeNode> getTargetDirs(Long gitProjectId) throws Exception {
        List<ZtreeNode> list = getAllDirsAndFiles(gitProjectId, TARGET_DIR_NAME, 1);
        ZtreeNode ztreeNode = new ZtreeNode();
        ztreeNode.setPath(TARGET_DIR_NAME);
        ztreeNode.setGitProjectId(gitProjectId);
        ztreeNode.setParentPath("");
        ztreeNode.setParChl(0);
        ztreeNode.setName(TARGET_DIR_NAME);
        list.add(ztreeNode);
        return list;
    }

    @Override
    public JSONObject getPushNum(Long gitProjectId, String gitProjectDirPath, String gitProjectFilePath) throws Exception {
        Integer pushNum = 0;
        Boolean hasMerge = false;
        List<DataDevScriptFile> mergeList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(gitProjectFilePath)) {
            DataDevScriptFile file = dataDevScriptFileDao.getSingleScriptFile(gitProjectId, gitProjectFilePath);
            if (file == null) {
                throw new RuntimeException("文件不存在");
            }

            if (!isBelongTarget(gitProjectFilePath) && (file.getWhereIsNew() == null || !file.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode()))) {
                file.setTypeStr(DataDevScriptTypeEnum.enumValueOf(file.getType()).toName());
                if ((file.getGitDeleted() == null || file.getGitDeleted() != 1) && DataDevScriptTypeEnum.canEdit(file.getType()) && StringUtils.isNotBlank(file.getGitVersion()) && StringUtils.isNotBlank(file.getLastGitVersion()) && !file.getGitVersion().equals(file.getLastGitVersion())) {
                    hasMerge = true;
                    file.setNewGitVersion(file.getLastGitVersion());
                    mergeList.add(file);
                }
                DataDevScriptGitStatusEnum gitStatusEnum = DataDevScriptGitStatusEnum.getGitStatus(file.getGitVersion(), file.getLastGitVersion(), file.getFileMd5(), file.getLastGitVersionMd5(), file.getGitDeleted());
                if (DataDevScriptGitStatusEnum.canPush(gitStatusEnum)) {
                    pushNum++;
                }
            }

        } else {
            if (StringUtils.isNotBlank(gitProjectDirPath)) {
                DataDevScriptDir dir = dataDevScriptDirDao.getDataDevScriptDirBy(gitProjectId, gitProjectDirPath);
                if (dir == null) {
                    throw new RuntimeException("目录不存在");
                }
            }

            String rootPath = StringUtils.isNotBlank(gitProjectDirPath.trim()) ? (Base64Util.formatName(gitProjectDirPath.trim()) + "/") : "";
            List<DataDevScriptFile> dataDevScriptFiles = dataDevScriptFileDao.getScriptDirFileRecur(gitProjectId, rootPath);
            if (dataDevScriptFiles != null && dataDevScriptFiles.size() > 0) {
                for (DataDevScriptFile file : dataDevScriptFiles) {
                    if (isBelongTarget(file.getGitProjectFilePath())) {
                        continue;
                    }
                    if (file.getWhereIsNew() != null && file.getWhereIsNew().equals(DataDevScriptFileWhereIsNewEnum.GIT.tocode())) {
                        continue;
                    }
                    file.setTypeStr(DataDevScriptTypeEnum.enumValueOf(file.getType()).toName());
                    if (file.getGitDeleted() != 1 && DataDevScriptTypeEnum.canEdit(file.getType()) && StringUtils.isNotBlank(file.getGitVersion()) && StringUtils.isNotBlank(file.getLastGitVersion()) && !file.getGitVersion().equals(file.getLastGitVersion())) {
                        hasMerge = true;
                        file.setNewGitVersion(file.getLastGitVersion());
                        mergeList.add(file);
                    }
                    DataDevScriptGitStatusEnum gitStatusEnum = DataDevScriptGitStatusEnum.getGitStatus(file.getGitVersion(), file.getLastGitVersion(), file.getFileMd5(), file.getLastGitVersionMd5(), file.getGitDeleted());
                    if (DataDevScriptGitStatusEnum.canPush(gitStatusEnum)) {
                        pushNum++;
                    }
                }
            }
        }
        jsonObject.put("pushNum", pushNum);
        jsonObject.put("gitMerge", hasMerge);
        jsonObject.put("mergeList", mergeList);
        return jsonObject;
    }

    @Override
    public boolean isBelongTarget(String path) throws Exception {
        return StringUtils.isNotBlank(path) && (path.equals(TARGET_DIR_NAME) || path.startsWith(TARGET_DIR_NAME + "/"));
    }
}
