var H_C_K_T = "H_C_K_T"; // tabs
var H_C_K_A_L = "H_C_K_A_L"; //homeCookieActiveLi
var H_C_K_A_G = "H_C_K_A_G";
var H_C_K_A_G_2 = "H_C_K_A_G_2";
var H_C_K_A_G_P = "H_C_K_A_G_P";
var D_C = "D_C";

var HOME_COOKIE = {

    coverToCookieArray: function (homeIndexCookie) {
        try {
            if ($.trim(homeIndexCookie).length > 0) {
                return JSON.parse(homeIndexCookie);
            }
        } catch (e) {
        }
        return [];
    },
    covertToCookieStr: function (homeIndexCookie) {
        if (homeIndexCookie && homeIndexCookie.length > 0) {
            return JSON.stringify(homeIndexCookie);
        }
        return "";
    },
    addTabCookie: function (openGitProjectId, key, params, isTmp, dirPath) {

        var homeIndexCookieTabs = $.cookie(H_C_K_T);
        homeIndexCookieTabs = HOME_COOKIE.coverToCookieArray(homeIndexCookieTabs);

        console.log("add cookie before :" + homeIndexCookieTabs.length);
        for (var index = 0; index < homeIndexCookieTabs.length; index++) {
            var tabs = homeIndexCookieTabs[index];
            if (tabs.key === key) {
                return;
            }
        }
        homeIndexCookieTabs.push({
            openGitProjectId: openGitProjectId,
            key: key,
            params: params,
            isTmp: isTmp || false,
            dirPath: dirPath || ""
        })
        console.log("after cookie before :" + homeIndexCookieTabs.length);

        $.cookie(H_C_K_T, HOME_COOKIE.covertToCookieStr(homeIndexCookieTabs), {expires: 7});
    },
    removeTabCookie: function (key) {
        var homeIndexCookieTabs = $.cookie(H_C_K_T);
        homeIndexCookieTabs = HOME_COOKIE.coverToCookieArray(homeIndexCookieTabs);


        if (homeIndexCookieTabs && homeIndexCookieTabs.length > 0) {
            for (var index = 0; index < homeIndexCookieTabs.length; index++) {
                var tabs = homeIndexCookieTabs[index];
                if (tabs.key == key) {
                    homeIndexCookieTabs.splice(index, 1)
                }
            }
        }
        $.cookie(H_C_K_T, HOME_COOKIE.covertToCookieStr(homeIndexCookieTabs), {expires: 7});
    },
    getTabCookie: function (key) {
        try {
            var homeIndexCookieTabs = $.cookie(H_C_K_T);
            homeIndexCookieTabs = HOME_COOKIE.coverToCookieArray(homeIndexCookieTabs);
            if (homeIndexCookieTabs && homeIndexCookieTabs.length > 0) {
                for (var index = 0; index < homeIndexCookieTabs.length; index++) {
                    var tabs = homeIndexCookieTabs[index];
                    if (tabs.key == key) {
                        return tabs;
                    }
                }
            }
        } catch (e) {

        }
        return null;
    },
    changeName: function (key, newKey, params) {
        var homeIndexCookieTabs = $.cookie(H_C_K_T);
        homeIndexCookieTabs = HOME_COOKIE.coverToCookieArray(homeIndexCookieTabs);
        if (homeIndexCookieTabs && homeIndexCookieTabs.length > 0) {
            for (var index = 0; index < homeIndexCookieTabs.length; index++) {
                var tabs = homeIndexCookieTabs[index];
                if (tabs.key == key) {
                    tabs.params = params;
                    tabs.key = newKey;
                    tabs.isTmp = false;
                    break;
                }
            }
        }
        $.cookie(H_C_K_T, HOME_COOKIE.covertToCookieStr(homeIndexCookieTabs), {expires: 7});
        var activeKey = this.getActiveLiCookie();
        if (!activeKey || activeKey == key) {
            this.setActiveLiCookie(newKey);
        }
    },
    setActiveLiCookie: function (key) {
        $.cookie(H_C_K_A_L, key, {expires: 7});
    },
    getActiveLiCookie: function () {
        return $.cookie(H_C_K_A_L);
    },
    getLiCookie: function () {
        return $.cookie(H_C_K_T);
    },
    setActiveGitCookie: function (erp, gitProjectid) {
        if (erp && gitProjectid) {
            $.cookie(H_C_K_A_G + "_" + erp, gitProjectid, {expires: 7})
        }
    },
    getActiveGitCookie: function (erp) {
        return $.cookie(H_C_K_A_G + "_" + erp);
    },
    changeColorCookie: function () {
       var dc = HOME_COOKIE.getColorCookie() ;
       if(dc == "white"){
           $.cookie(D_C, "black", {expires: 365})
       }else{
           $.cookie(D_C, "white", {expires: 365})
       }
    },
    getColorCookie: function () {
        var dc = $.cookie(D_C)
        if(dc && dc === "white"){
            return "white"
        }
        return "black";
    },

    setActiveGit2Cookie: function (erp, gitProjectid) {
        if (erp && gitProjectid) {
            $.cookie(H_C_K_A_G_2 + "_" + erp, gitProjectid, {expires: 7})
        }
    },
    getActiveGit2Cookie: function (erp) {
        return $.cookie(H_C_K_A_G_2 + "_" + erp);
    },
    setActiveGitPathCookie: function (erp, treeNodePath) {
        if (erp && treeNodePath) {
            $.cookie(H_C_K_A_G_P + "_" + erp, treeNodePath, {expires: 7})
        }
    },
    getActiveGitPathCookie: function (erp) {
        return $.cookie(H_C_K_A_G_P + "_" + erp);
    },
    rmActiveGitPathCookie: function (erp) {
        if ($.cookie(H_C_K_A_G_P + "_" + erp) !== undefined) {
            $.cookie(H_C_K_A_G_P + "_" + erp, '', { expires: -1 });
        }
    }
}
