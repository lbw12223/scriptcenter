package com.jd.bdp.datadev.web.websocket;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.UrmStaticUtil;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.netty.common.DebugCmd;
import com.jd.bdp.datadev.netty.common.DebugMessage;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.datadev.service.DataDevScriptService;
import com.jd.bdp.datadev.web.component.WebSpringContextUtil;
import com.jd.bdp.datadev.web.nettyclient.DebugClient;
import com.jd.bdp.datadev.web.nettyclient.DebugSession;
import com.jd.bdp.datadev.web.nettyclient.DebugSessionManager;
import com.jd.common.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/10/27.
 */
public class WebSocketRunDeBugHander extends TextWebSocketHandler {
    private static final Log logger = LogFactory.getLog(WebSocketRunDeBugHander.class);



    /**
     * Ø
     * private static final Log logger = LogFactory.getLog(WebSocketRunDeBugHander.class);
     * <p>
     * /**
     * 建立连接时候处理
     * <p>
     * 1.建立netty 连接
     * 2.连接建立完成过后立即给页面端发送一条消息 neetyReady
     * 3.页面收到 neetyReady 发送开始执行指令
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        StandardWebSocketSession standardWebSocketSession = (StandardWebSocketSession) session;
        standardWebSocketSession.getAttributes();
        Long runDetailId = Long.parseLong(standardWebSocketSession.getNativeSession().getRequestParameterMap().get("runDetailId").get(0));
        Integer pythonVersion = Integer.parseInt(standardWebSocketSession.getNativeSession().getRequestParameterMap().get("pythonVersion").get(0));
        DataDevScriptRunDetail dataDevScriptRunDetail = WebSpringContextUtil.getBean(DataDevScriptRunDetailService.class).findById(runDetailId);
        DebugSession debugSession = DebugSessionManager.attachmentSession(session);

        String ip = dataDevScriptRunDetail.getIp();
        DebugClient debugClient = new DebugClient(ip, 45535);
        //设置Session 中的DebugClient
        debugSession.setDebugClient(debugClient);
        debugSession.setRunDetailId(runDetailId);
        debugSession.setPythonVersion(pythonVersion);
        debugClient.init();
        debugClient.connect();
        //启动成功过后 发送一条信息给Web端 已经连接好
        DebugMessage debugMessage = new DebugMessage();
        debugMessage.setDebugCmd(DebugCmd.NettyReady);
        logger.error("nettyStart=====get channal= =======" + DebugSessionManager.attachmentSession(session).getChannelId());
        debugSession.flushPageDebugResult(debugMessage);

    }

    /**
     * 处理页面的请求
     * <p>
     * 将页面的请求转换为DebugMessage
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //换成 线程池
        DebugSession debugSession = DebugSessionManager.attachmentSession(session);

        try {
            String cmd = message.getPayload();
            Integer colonIndex = cmd.indexOf(":");
            String cmdStr = colonIndex != -1 ? cmd.substring(0, colonIndex) : cmd;
            String param = colonIndex != -1 ? cmd.substring(colonIndex + 1) : "";
            DebugCmd debugCmd = DebugCmd.valueOf(cmdStr);
            if (debugCmd.equals(DebugCmd.Start)) {
                DebugMessage debugMessage = new DebugMessage();
                debugMessage.setDebugCmd(DebugCmd.Start);
                DataDevScriptRunDetailService runDetailService = WebSpringContextUtil.getBean(DataDevScriptRunDetailService.class);
                DataDevScriptRunDetail dataDevScriptRunDetail = runDetailService.findById(debugSession.getRunDetailId());
                if(StringUtils.isNotBlank(dataDevScriptRunDetail.getRunClusterCode())){
                    dataDevScriptRunDetail.setClusterCode(dataDevScriptRunDetail.getRunClusterCode());
                }
                if(StringUtils.isNotBlank(dataDevScriptRunDetail.getRunMarketLinuxUser())){
                    dataDevScriptRunDetail.setMarketLinuxUser(dataDevScriptRunDetail.getRunMarketLinuxUser());
                }
                dataDevScriptRunDetail.setDebugFlag(1);
                dataDevScriptRunDetail.setStopErp(dataDevScriptRunDetail.getOperator());
                String userToken = UrmStaticUtil.UserTokenByErp(null,dataDevScriptRunDetail.getOperator());
                dataDevScriptRunDetail.setUserToken(userToken);
                dataDevScriptRunDetail.setPythonVersion(debugSession.getPythonVersion());
                Map<Long,String> contentMap = runDetailService.getRunContentMap();
                if(contentMap.containsKey(dataDevScriptRunDetail.getId())){
                    dataDevScriptRunDetail.setContent(contentMap.get(dataDevScriptRunDetail.getId()));
                    dataDevScriptRunDetail.setRunByContent(true);
                }else {
                    dataDevScriptRunDetail.setRunByContent(false);
                }
                //打包依赖方式，获取真正的zip文件信息
                if(dataDevScriptRunDetail.getDependencyId()!=null){
                    DataDevScriptFileService scriptFileService = WebSpringContextUtil.getBean(DataDevScriptFileService.class);
                    DataDevScriptFile tmpFile = scriptFileService.findTmpByRelationDependencyId(dataDevScriptRunDetail.getDependencyId());
                    if(tmpFile == null){
                        throw new RuntimeException("找不到指定依赖文件");
                    }
                    dataDevScriptRunDetail.setVersion(tmpFile.getVersion());
                    dataDevScriptRunDetail.setId(tmpFile.getId());
                }
                debugMessage.setData(JSONObject.parseObject(JSONObject.toJSONString(dataDevScriptRunDetail)));
                debugMessage.setParam(param);
                debugSession.sendDebugCmd(debugMessage);
            } else {
                if(debugCmd.equals(DebugCmd.Stop)){
                    DebugMessage ret = new DebugMessage();
                    Long detailId = debugSession.getRunDetailId();
                    DataDevScriptRunDetail runDetail =new DataDevScriptRunDetail();
                    runDetail.setId(detailId);
                    runDetail.setDebugFlag(1);
                    DataDevScriptService dataDevScriptService = WebSpringContextUtil.getBean(DataDevScriptService.class);
                    if(dataDevScriptService != null ){
                        dataDevScriptService.stopScript(runDetail);
                    }
                    ret.setDebugCmd(DebugCmd.Close);
                    debugSession.flushPageDebugResult(ret);
                }else {
                    DebugMessage debugMessage = new DebugMessage();
                    debugMessage.setDebugCmd(debugCmd);
                    debugMessage.setParam(param);
                    debugSession.sendDebugCmd(debugMessage);
                }

            }
        } catch (Exception e) {
            logger.error("fdsfsdfsdf+"+e);
            debugSession.flushPageDebugErrorResult(e.getMessage());
        }

    }

    /**
     * 关闭session
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        DebugSession debugSession = DebugSessionManager.attachmentSession(session);
        Long detailId = debugSession.getRunDetailId();
        DataDevScriptRunDetail runDetail =new DataDevScriptRunDetail();
        runDetail.setId(detailId);
        runDetail.setDebugFlag(1);
        DataDevScriptService dataDevScriptService = WebSpringContextUtil.getBean(DataDevScriptService.class);
        if(dataDevScriptService != null ){
            dataDevScriptService.stopScript(runDetail);
        }
        debugSession.releaseSession();

       /* DebugMessage debugMessage = new DebugMessage();
        debugMessage.setDebugCmd(DebugCmd.Stop);
        debugSession.sendDebugCmd(debugMessage);*/
    }




    /**
     * 需验证
     *
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        DebugSession debugSession = DebugSessionManager.attachmentSession(session);
        String message = StringUtils.isNotBlank(exception.getMessage()) ? exception.getMessage() : "请求失败！";
        debugSession.flushPageDebugErrorResult(message);
    }
}
