/**
 * FrameBusEvent 用于Iframe之间通信的事件绑定类
 * @type {FrameBusBindClass}
 * @dep 依赖工具类 FrameBus
 * @dep 依赖配置变量 top.window.BdpBaseConfig
 */
var FrameBusBindClass = /** @class */ (function () {
    function FrameBusBindClass(cfg) {
        this.config = cfg;
        this.state = {};
        this.isInstance();
    }

    // 校验是否实例
    FrameBusBindClass.prototype.isInstance = function () {
        if (!(this instanceof FrameBusBindClass)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };

    // 基础配置文件
    FrameBusBindClass.baseConfig = top.window.BdpBaseConfig && top.window.BdpBaseConfig.frameBusConfig && top.window.BdpBaseConfig.frameBusConfig.BDP_QIANKUN_CONTEXTMENU_OPEN ? top.window.BdpBaseConfig.frameBusConfig : null

    // framBus实例
    FrameBusBindClass.frameBus = new FrameBus();
    /**
     * 绑定鼠标右键打开事件
     * @param param tab实例
     */
    FrameBusBindClass.emitContextOpen = function (option) {
        const eventKey = FrameBusBindClass.baseConfig ? FrameBusBindClass.baseConfig.BDP_QIANKUN_CONTEXTMENU_OPEN : null;
        if (!eventKey) {
            return
        }
        FrameBusBindClass.frameBus.emit(eventKey, option)
    }

    // 绑定右键菜单的左键点击事件
    FrameBusBindClass.bindContextClick = function (cb) {
        const eventKey = FrameBusBindClass.baseConfig ? FrameBusBindClass.baseConfig.BDP_QIANKUN_CONTEXTMENU_CLICK : null;
        if (!eventKey) {
            return
        }
        FrameBusBindClass.frameBus.on(eventKey, function (data) {
            console.log(data);
            cb(data)
        })
    }
    return FrameBusBindClass;
}())
