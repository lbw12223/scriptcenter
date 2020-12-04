package com.jd.bdp.datadev.argsParse;

import java.util.HashMap;
import java.util.Random;

public class ThreadLocalTest {

    private static ThreadLocal<HashMap<String, Long>> seqNum = new ThreadLocal<HashMap<String, Long>>() {
        public HashMap<String, Long> initialValue() {
            HashMap<String, Long> params = new HashMap<String, Long>();
            params.put("number", 5L + new Random().nextInt(100));
            return params;
        }
    };

    public static void setValue() {
        HashMap<String, Long> params = seqNum.get();
        params.put("number", 0L);
    }

    // ②获取下一个序列值
    public Long getNextNum() {
        Long number = seqNum.get().get("number");
        seqNum.get().put("number", number + 1);
        return seqNum.get().get("number");
    }


    public static void main(String[] args) throws Exception {
        ThreadLocalTest sn = new ThreadLocalTest();
        // ③ 3个线程共享sn，各自产生序列号
        Thread.sleep(1000);
        TestClient t1 = new TestClient(sn);
        Thread.sleep(1000);
        TestClient t2 = new TestClient(sn);
        Thread.sleep(1000);
        TestClient t3 = new TestClient(sn);
        t1.start();
        t2.start();
        t3.start();
    }

    private static class TestClient extends Thread {
        private ThreadLocalTest sn;

        public TestClient(ThreadLocalTest sn) {
            this.sn = sn;
            setValue();
        }

        public void run() {
            for (int i = 0; i < 3; i++) {
                // ④每个线程打出3个序列值
                System.out.println("thread[" + Thread.currentThread().getName() + "] --> sn["
                        + sn.getNextNum() + "]");
            }
        }
    }


}
