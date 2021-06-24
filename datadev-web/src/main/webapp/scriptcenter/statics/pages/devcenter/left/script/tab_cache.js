/**
 * tab缓存Js
 * @type {TabCacheClass}
 * @dep 依赖工具类 JmdUtil
 * @dep 依赖工具类 FrameBus
 * @dep 依赖配置文件 BdpFrameBusConfig frameBus的配置文件
 * @dep 依赖配置文件 BdpLocalStorageConfig localstorage配置文件
 */
var TabCacheClass = /** @class */ (function () {
    function TabCacheClass(cfg) {
        this.config = cfg;
        this.state = {};
        this.isInstance();
    }

    // 校验是否实例
    TabCacheClass.prototype.isInstance = function () {
        if (!(this instanceof TabCacheClass)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };

    /**
     * 加入缓存
     * @param param tab实例
     */
    TabCacheClass.addCache = function (param) {
        var cacheTabs = TabCacheClass.getCache();
        var tabs = cacheTabs || [];
        var tab = tabs.find(item => {
            return item.key === param.key
        });
        if (!tab) {
            tabs.push(param);
            TabCacheClass.setCache(tabs);
        }
    }

    /**
     * 写入缓存
     * @param params tabs实例
     */
    TabCacheClass.setCache = function (params) {
        JmdUtil.LsUtil.setItem(BdpLocalStorageConfig.SCRIPT_CENTER_TABS, params, top.window)
    }

    // 获取现有缓存
    TabCacheClass.getCache = function () {
        return JmdUtil.LsUtil.getItem(BdpLocalStorageConfig.SCRIPT_CENTER_TABS, top.window);
    }

    /**
     * 更新缓存
     * @param param tab实例
     */
    TabCacheClass.updateCache = function (param) {
        var cacheTabs = TabCacheClass.getCache();
        var tabs = cacheTabs || [];
        if (JmdUtil.ValidateUtil.isEmptyList(tabs)) {
            return;
        }
        for (var i = 0; i < tabs.length; i++) {
            if (tabs[i].key === param.key) {
                for (var k in param) {
                    tabs[i][k] = param[k]

                }
                break;
            }
        }
        TabCacheClass.setCache(tabs);
    }

    TabCacheClass.updateCacheByKey = function (key, param) {
        var cacheTabs = TabCacheClass.getCache();
        var tabs = cacheTabs || [];
        if (JmdUtil.ValidateUtil.isEmptyList(tabs)) {
            return;
        }
        for (var i = 0; i < tabs.length; i++) {
            if (tabs[i].key === key) {
                tabs[i] = param ;
                break;
            }
        }
        TabCacheClass.setCache(tabs);
    }

    /**
     * 删除一个tab
     * @param key tab实例的唯一索引
     */
    TabCacheClass.removeCache = function (key) {
        if (!key) {
            throw new Error("请传入唯一的key")
        }
        var cacheTabs = TabCacheClass.getCache();
        var tabs = cacheTabs || [];
        var oldLen = tabs.length;
        if (JmdUtil.ValidateUtil.isEmptyList(tabs)) {
            return;
        }
        for (var i = 0; i < tabs.length; i++) {
            if (tabs[i].key === key) {
                tabs.splice(i, 1);
                i--;
                break;
            }
        }
        if (oldLen === tabs.length) {
            return;
        }
        TabCacheClass.setCache(tabs);
    }

    // 清空缓存
    TabCacheClass.clearCache = function () {
        JmdUtil.LsUtil.removeItem(BdpLocalStorageConfig.SCRIPT_CENTER_TABS, top.window)
    }

    /**
     * 从缓存打开tabs
     * @param cb 回调函数（入参为本地缓存的tabs）
     */
    TabCacheClass.openCacheTabs = function (cb) {
        var cacheTabs = TabCacheClass.getCache();
        if (JmdUtil.ValidateUtil.notEmptyList(cacheTabs)) {
            cb(cacheTabs)
        }
    }

    // 绑定frameBus事件
    TabCacheClass.bindFrameEvent = function () {
        var frameBus = new FrameBus();
        TabCacheClass.bindLogoutFrameEvent(frameBus);
        TabCacheClass.bindCloseTabFrameEvent(frameBus);
        TabCacheClass.bindUpdateTabFrameEvent(frameBus);
        TabCacheClass.bindAddTabFrameEvent(frameBus);
    }

    // 退出登录清空缓存
    TabCacheClass.bindLogoutFrameEvent = function (frameBus) {
        frameBus.on(BdpFrameBusConfig.BDP_LOGIN_OUT, function () {
            TabCacheClass.clearCache()
        })
    }
    // 关闭tab删除tab项
    TabCacheClass.bindCloseTabFrameEvent = function (frameBus) {
        frameBus.on(BdpFrameBusConfig.BDP_SCRIPT_CENTER_TAB_CLOSE, function (key) {
            TabCacheClass.removeCache(key)
        })
    }
    // 修改tabs项
    TabCacheClass.bindUpdateTabFrameEvent = function (frameBus) {
        frameBus.on(BdpFrameBusConfig.BDP_SCRIPT_CENTER_TAB_UPDATE, function (param) {
            TabCacheClass.updateCache(param)
        })
    }
    // 添加tab项
    TabCacheClass.bindAddTabFrameEvent = function (frameBus) {
        frameBus.on(BdpFrameBusConfig.BDP_SCRIPT_CENTER_TAB_ADD, function (param) {
            TabCacheClass.addCache(param)
        })
    }

    return TabCacheClass;
}())
