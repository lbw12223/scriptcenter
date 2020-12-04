package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevScriptFileHis;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.jsf.gd.util.StringUtils;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/5/17.
 */
public class JDGitCommits extends GitConvertToDataDevDomain<DataDevScriptFileHis, JDGitCommits> implements JSONObjectCovertToGitDomain<JDGitCommits> {

    private static Pattern CREATOR_PATTERN = Pattern.compile("\\[(.*)\\].*");
    private static DateFormat UTC_FORMATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  //yyyy-MM-dd'T'HH:mm:ss.SSSZ
    private static DateFormat UTC_FORMATE_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        UTC_FORMATE_Z.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Long gitProjectId;
    private String projectPath;  //projectFullPath
    private String branch;    //branchName
    private String path;       //filePath*/
    private Date since;      // YYYY-MM-DDTHH:MM:SSZ
    private Date until;      // YYYY-MM-DDTHH:MM:SSZ
    private Integer page;
    private Integer pageSize;

    //2018-06-04T19:04:14.000+08:00
    private String shaId;
    private String shaShortId;
    private String committerName;
    private String committerEmail;
    private Date committedDate;
    private String message;
    private String beforeShaId;
    private boolean isCommitByBDPIDE = false;


    private static final Pattern PATTERN_TIME = Pattern.compile("^(\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}:).*");
    private static final Pattern PATTERN_ERP = Pattern.compile("^\\[(\\w+)\\].*?");


    public static String utcString2YYYYMMDDHHMMSS(String oldDateStr) {
        return YYYYMMDDHHMMSS.format(utcString2Date(oldDateStr));
    }

    //2018-12-17T07:16:14Z
    public static Date utcString2Date(String oldDateStr) {
        try {
            //此格式只有  jdk 1.7才支持  yyyy-MM-dd'T'HH:mm:ss.SSSXXX
            try {
                Date date = UTC_FORMATE.parse(oldDateStr);
                SimpleDateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
                Date newDate = df1.parse(date.toString());
                return newDate;

            } catch (Exception e) {
                try {
                    Date date = UTC_FORMATE_Z.parse(oldDateStr);
                    SimpleDateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
                    Date newDate = df1.parse(date.toString());
                    return newDate;
                } catch (Exception e1) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = formatter.parse(oldDateStr);
                    return date;
                }

            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static String date2UtcString(Date date) {
        return UTC_FORMATE.format(date);
    }


    /**
     * 处理提交历史中的message信息
     * 可能获取到提交人信息
     *
     * @return
     */
    public static HoldDoubleValue<String, String> handMessage(String message) {
        return handMessage2(message);
       /* if (message.endsWith("fdsfs")) {
            int j = 1 + 1;
        }

        String committerName = null;
        boolean isIdeBdpCommit = message.indexOf(":[") == 19;
        if (isIdeBdpCommit) {
            Matcher matcher = IDE_COMMIT_MESSSGE_PATTERN.matcher(message);
            if (matcher.matches()) {
                committerName = matcher.group(1);
                message = matcher.group(2);
            } else {
                message = message.substring(message.indexOf("]") + 1);
            }
        }
        //[zhangrui156] 删除 datadev-test-1.0-SNAPSHOT.jar 文件
        if (!isIdeBdpCommit) {
            Matcher matcher = IDE_COMMIT_MESSAGE_MODIFY.matcher(message);
            if (matcher.matches()) {
                isIdeBdpCommit = true;
                committerName = matcher.group(1);
                message = "修改 " + matcher.group(2);
            }
        }
        if (!isIdeBdpCommit) {
            Matcher matcher = IDE_COMMIT_MESSAGE_ADD.matcher(message);
            if (matcher.matches()) {
                isIdeBdpCommit = true;
                committerName = matcher.group(1);
                message = "添加 " + matcher.group(2);
            }
        }

        if (!isIdeBdpCommit) {
            Matcher matcher = IDE_COMMIT_MESSAGE_DELETED.matcher(message);
            if (matcher.matches()) {
                isIdeBdpCommit = true;
                committerName = matcher.group(1);
                message = "删除 " + matcher.group(2);
            }
        }

        Matcher matcher = IDE_COMMIT_MESSAGE_1.matcher(message);
        if (matcher.matches()) {
            message = matcher.group(1);
        }
        int indexOfFlag = message.indexOf("提交信息：");
        if (indexOfFlag > -1 && (message.startsWith("添加")) || message.startsWith("删除") || message.startsWith("修改")) {
            String fixMessage = message.substring(indexOfFlag + 5);
            if (StringUtils.isNotBlank(fixMessage)) {
                message = fixMessage;
            }
        }
        return new HoldDoubleValue<String, String>(committerName, message != null ? message.trim() : "");*/
    }

    public static void main(String[] args) throws Exception {

        if (true) {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = formatter.parse("2019-12-19T18:40:50+08:00");

            System.out.println(date);


            return;

        }
        JDGitCommits jdGitCommits = new JDGitCommits();
        jdGitCommits.setBranch("bdp_ide_branch");
        jdGitCommits.setGitProjectId(23759L);
        //List<JDGitCommits> allJDGitCommits = jdGitCommits.allCommits();

        //System.out.println(allJDGitCommits);

       /* String message = "2018-12-24 19:16:24: 提交信息：mkmm";
        Pattern pattern = Pattern.compile("\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}:\\s提交信息：(.*)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            System.out.println(matcher.group(1));
        }
*/
        String message = "2019-01-09 11:14:23:[zhangrui156] 提交信息：sss";
        // message = "2018-12-24 18:53:16: 提交信息：他天天";
        //  message = "修改 Debug-测试脚本/dependency_class/HiveTaskTest.py  提交信息：被调函数";
        // message = "2018-12-25 14:26:41:[zhanglei847] 添加 2322/nettyServer/test.pull 文件 提交信息：fdsfs";
        // message = "2019-01-08 18:53:05:[zhangrui156] 提交信息：jj";
        // message = "2018-12-29 12:07:30:[wangxiaoli76] 添加 testpull/wxl_test_20181229_112642.py 文件 提交信息：调度导入更新脚本";
        // message = "[zhangrui156] 删除 中文目录/132213.py 文件";
        message = "2018-12-28 15:07:44:[wangxiaoli76] 添加 debug_test/wxl_test_20181228_150617.py 文件 提交信息：";
        handMessage2(message);


        String UTC = "2017-11-09T23:16:03.562Z";

        UTC = "2018-12-17T07:16:14Z";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date UtcDate = null;
        try {
            UtcDate = sdf.parse(UTC);
        } catch (Exception e) {
            return;
        }
        System.out.println(UtcDate);

        SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(TimeZone.getDefault());
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(UtcDate.getTime());
        System.out.println(localTime);

    }

    private static HoldDoubleValue<String, String> handMessage2(String message) {
        String fixMessage = message;
        String erp = null;
        Matcher timeMatcher = PATTERN_TIME.matcher(fixMessage);
        if (timeMatcher.matches()) {
            fixMessage = fixMessage.replace(timeMatcher.group(1), "").trim();
        }

        Matcher erpMatcher = PATTERN_ERP.matcher(fixMessage);

        if (erpMatcher.matches()) {
            erp = erpMatcher.group(1);
            fixMessage = fixMessage.replace(erpMatcher.group(1), "").substring(2).trim();
        }

        int indexOfFlag = fixMessage.indexOf("提交信息：");

        if (indexOfFlag > -1) {
            fixMessage = fixMessage.substring(indexOfFlag + 5).trim();
        }
        fixMessage = StringUtils.isBlank(fixMessage) ? message : fixMessage;
        return new HoldDoubleValue<String, String>(erp, fixMessage);
    }


    @Override
    public JDGitCommits covertGitDomain(JSONObject jsonObject) {
        String message = jsonObject.getString("message");
        String commitDate = jsonObject.getString("committed_date");

        String committerName = jsonObject.getString("committer_name");      //ide提交的为邮箱账号
        String authorEmail = jsonObject.getString("committer_email");          //ide提交邮箱为密码
        if (authorEmail.indexOf("@jd.com") == -1) { //ide提交
            authorEmail = committerName + "@jd.com";
        }
        String beforeCommitId = "";
        JSONArray parentIds = jsonObject.getJSONArray("parent_ids");
        if (parentIds != null) {
            for (int index = 0; index < parentIds.size(); index++) {
                beforeCommitId += "," + parentIds.getString(index);
            }
            beforeCommitId = beforeCommitId.length() > 0 ? beforeCommitId.substring(1) : "";
        }
        //通过数据开发平台提交的Commit全部为虚拟账号提交
        HoldDoubleValue<String, String> commitNameAndMessage = handMessage(message);
        message = commitNameAndMessage.b;


        JDGitCommits jdGitCommits = new JDGitCommits();
        if (commitNameAndMessage.a != null) {
            committerName = commitNameAndMessage.a;
            jdGitCommits.setCommitByBDPIDE(true);
        }
        jdGitCommits.setMessage(message);
        jdGitCommits.setShaId(jsonObject.getString("id"));
        jdGitCommits.setBranch(branch);
        jdGitCommits.setBeforeShaId(beforeCommitId);
        jdGitCommits.setCommitterName(committerName);
        jdGitCommits.setCommitterEmail(authorEmail);
        jdGitCommits.setGitProjectId(gitProjectId);
        jdGitCommits.setCommittedDate(utcString2Date(commitDate));
        return jdGitCommits;
    }

    @Override
    DataDevScriptFileHis convertDataDevDomain(JDGitCommits jdGitCommits) {
        DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
        dataDevScriptFileHis.setGitProjectId(jdGitCommits.getGitProjectId());
        dataDevScriptFileHis.setCreator(jdGitCommits.getCommitterName());
        dataDevScriptFileHis.setCreated(jdGitCommits.getCommittedDate());
        dataDevScriptFileHis.setGitVersion(StringUtils.isNotBlank(jdGitCommits.getShaId()) ? jdGitCommits.getShaId() : "");
        dataDevScriptFileHis.setCommitMessage(jdGitCommits.getMessage());
        dataDevScriptFileHis.setGitProjectFilePath(jdGitCommits.getPath());
        return dataDevScriptFileHis;
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 获取最近的一次提交记录
     *
     * @return
     * @throws Exception
     */
    public DataDevScriptFileHis queryLastCommit() throws Exception {
        try {
            if (isContainChinese(path)) {
                //如果报错重新获取一下文件的内容里面的lastCommitId
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setBranch(branch);
                jdGitFiles.setFilePath(path);
                jdGitFiles.setGitProjectId(gitProjectId);
                jdGitFiles.loadFileContent();

                DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
                dataDevScriptFileHis.setGitProjectId(gitProjectId);
                dataDevScriptFileHis.setGitVersion(getLastCommitIdByPage(gitProjectId));
                dataDevScriptFileHis.setGitProjectFilePath(path);
                return dataDevScriptFileHis;
            } else {
                HoldDoubleValue<Integer, List<JDGitCommits>> result = doQueryCommits();
                if (result != null && result.b != null && result.b.size() > 0) {
                    return convertDataDevDomain(result.b.get(0));
                }
            }

        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    private String getLastCommitIdByPage(Long gitProjectId) throws Exception {
        String lastCommitId = null;
        String url = "http://git.jd.com/" + projectPath + "/commits/" + branch + "/" + URLEncoder.encode(path, "utf-8");
        byte[] contentBytes = GitHttpUtil.createClient(gitProjectId).getBytes(url);
        String pageContent = new String(contentBytes);
        Pattern p = Pattern.compile("data-clipboard-text=\"([\\w]*)\"");
        Matcher m = p.matcher(pageContent);
        if (m.find()) {
            lastCommitId = (m.group(1));
        }
        return lastCommitId;
    }


    /**
     * 通过访问一个网页去查询lastGitCommit
     *
     * @return
     * @throws Exception
     */

    public HoldDoubleValue<Integer, List<JDGitCommits>> doQueryCommits() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtils.isNotBlank(branch)) {
            params.put("ref_name", branch);
        }
        if (StringUtils.isNotBlank(path)) {
            params.put("path", URLEncoder.encode(path, "utf-8"));
        }
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(pageSize));
        if (until != null) {
            Date realUntil = new Date(until.getTime() - 60 * 1000 * 480);
            params.put("until", date2UtcString(realUntil));
        }
        if (since != null) {
            Date realSince = new Date(since.getTime() - 60 * 1000 * 480);
            params.put("since", date2UtcString(realSince));
        }
        HoldDoubleValue<Integer, List<JDGitCommits>> totalAndList = GitHttpUtil.createClient(gitProjectId).getOnePage("projects/" + gitProjectId + "/repository/commits", params, this);
        return totalAndList;
    }

    /**
     * git commits
     * https://gitlab.msu.edu/help/api/commits.md#list-repository-commits
     */

    public PageResultDTO pageCommits() throws Exception {
        HoldDoubleValue<Integer, List<JDGitCommits>> totalAndList = doQueryCommits();
        List<DataDevScriptFileHis> gitCommitsList = covertDataDevDomainIterable(totalAndList.b);
        PageResultDTO pageResultDTO = new PageResultDTO();
        pageResultDTO.setCode(0);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setLimit(pageSize);
        pageResultDTO.setRows(gitCommitsList);
        pageResultDTO.setRecords(Long.parseLong(totalAndList.a + ""));
        return pageResultDTO;

    }


    /**
     * 获取当前Git项目所有的提交历史
     * <p>
     * https://gitlab.msu.edu/help/api/commits.md#list-repository-commits
     *
     * @return
     * @throws Exception
     */
    public List<JDGitCommits> allCommits() throws Exception {
        String url = "projects/" + gitProjectId + "/repository/commits";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ref_name", branch);
        List<JDGitCommits> allCommits = GitHttpUtil.createClient(gitProjectId).pageAll(url, params, this);
        return allCommits;
    }

    /**
     * 1.获取某个commit的diff
     * 2。curl -i  --header "PRIVATE-TOKEN: j9e-suFgk6p7di-rgKFg"
     * "http://git.jd.com/api/v4/projects/23759/repository/commits/63107cfe58049fe8db00c22d7ae3ce6d0cc8dcb3/diff"
     * 3。http://git.jd.com/help/api/commits.md#get-the-diff-of-a-commit
     */
    public JSONArray commitDiff() throws Exception {
        String url = "projects/" + gitProjectId + "/repository/commits/" + shaId + "/diff";
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doGet(url, null);
        if (gitHttpResponse.getResponseCode().equals(200)) {
            return JSONArray.parseArray(gitHttpResponse.getResponseMessage());
        }
        return null;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getShaId() {
        return shaId;
    }

    public void setShaId(String shaId) {
        this.shaId = shaId;
    }

    public String getShaShortId() {
        return shaShortId;
    }

    public void setShaShortId(String shaShortId) {
        this.shaShortId = shaShortId;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public Date getCommittedDate() {
        return committedDate;
    }

    public void setCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }


    public String getBeforeShaId() {
        return beforeShaId;
    }

    public void setBeforeShaId(String beforeShaId) {
        this.beforeShaId = beforeShaId;
    }

    public boolean isCommitByBDPIDE() {
        return isCommitByBDPIDE;
    }

    public void setCommitByBDPIDE(boolean commitByBDPIDE) {
        isCommitByBDPIDE = commitByBDPIDE;
    }

    @Override
    public String toString() {
        return "JDGitCommits{" +
                "gitProjectId=" + gitProjectId +
                ", projectPath='" + projectPath + '\'' +
                ", branch='" + branch + '\'' +
                ", path='" + path + '\'' +
                ", since='" + since + '\'' +
                ", until='" + until + '\'' +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", shaId='" + shaId + '\'' +
                ", shaShortId='" + shaShortId + '\'' +
                ", committerName='" + committerName + '\'' +
                ", committerEmail='" + committerEmail + '\'' +
                ", committedDate='" + committedDate + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
