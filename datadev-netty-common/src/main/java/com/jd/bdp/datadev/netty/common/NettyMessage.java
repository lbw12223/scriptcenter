package com.jd.bdp.datadev.netty.common;

/**
 * Created by zhangrui25 on 2018/11/12.
 * <p>
 * --header--bodylength--body
 * <p>
 * 定义Netty消息格式
 */
public class NettyMessage<T extends JavaModelToByte> {

    public static Integer CONTANTS_HEADER = 45545;

    private Integer header = CONTANTS_HEADER;
    private Integer length;
    private byte[] datas;
    private T t;

    public NettyMessage(T t)throws Exception {
        this.t = t;
        setDatas(t.covertBytes());
    }

    public NettyMessage(byte[] datas) {
        this.datas = datas;
        this.length = datas.length;
    }


    public Integer getHeader() {
        return header;
    }

    public Integer getLength() {
        return length;
    }

    public byte[] getDatas() {
        return datas;
    }

    private void setDatas(byte[] datas) {
        this.datas = datas;
        this.length = this.datas.length;
    }



}
