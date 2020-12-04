package com.jd.bdp.datadev.netty.common;

/**
 * Created by zhangrui25 on 2018/11/9.
 */
public enum DebugCmd {
    //websocket cmd

    Ready,       //weksocket 建立好链接，可以发送运行信息过来


    //debug cmd
    NettyReady,
    Start,
    StepNext,
    StepInto,
    Finish,
    Stop,
    ShowParam,     //查看参数
    ShowFullParam,     //查看参数
    AddBreakpoint,
    CancelBreakPoint,
    DisableBreakPoint,
    EnableBreakPoint,
    Continue,
    StepOut,
    ReStart,
    Close,
    Exception,      //发生异常
    //返回给web端的处理
    ShowCodeLine,   //显示当前运行的多少行
    LogMsg,        //日志信息              正常日志
    DoNothing;

}
