package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.SpringContextUtil;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.jdgit.JDGitMembers;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.jdgit.JDGitUser;
import com.jd.bdp.datadev.service.DataDevGitProjectMemberService;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ChangeDataDevErpController {

    private static final Logger logger = Logger.getLogger(ChangeDataDevErpController.class);

    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao ;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService ;

    @Autowired
    private DataDevGitProjectMemberService dataDevGitProjectMemberService ;



}
