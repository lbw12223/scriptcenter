package com.jd.bdp.datadev.component;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.jd.jim.cli.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LockUtil {

    @Autowired
    private Cluster jimClient;


    /**
     * @param key
     * @param requestId
     * @param seconds
     * @return
     * @throws RuntimeException
     */
    public boolean tryLock(String key, String requestId, Integer seconds) throws RuntimeException {
        try {
            for (int index = 0; index < seconds + 1; index++) {
                String assignStr = jimClient.get(key);
                if (StringUtils.isBlank(assignStr)) {
                    boolean isOk = jimClient.set(key, requestId, seconds, TimeUnit.SECONDS, false);
                    if (isOk) {
                        return true;
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * @param key
     * @param requestId
     * @param seconds
     * @return
     * @throws RuntimeException
     */
    public boolean tryNotWaitLock(String key, String requestId, Integer seconds) throws RuntimeException {
        try {
            String assignStr = jimClient.get(key);
            if (StringUtils.isBlank(assignStr)) {
                boolean isOk = jimClient.set(key, requestId, seconds, TimeUnit.SECONDS, false);
                if (isOk) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * @param requestId
     */
    public void unLock(String key, String requestId) {
        if (jimClient.get(key).equals(requestId)) {
            jimClient.del(key);
        }
    }


}
