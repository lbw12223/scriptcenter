package com.jd.bdp.datadev.component;


/**
 *
 * @param <T>
 */
public interface ConvertErp2UserName<T> {
    /**
     * 获取需要改变的erp字段
     * @return
     */
    String getErp(T t);

    /**
     * 设置erp对应的中文名
     * @param userNames
     * @return
     */
    void setErpUserName(T t, String userNames);
}
