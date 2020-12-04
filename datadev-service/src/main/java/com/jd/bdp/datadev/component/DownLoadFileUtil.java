package com.jd.bdp.datadev.component;

import com.jd.bdp.datadev.domain.DataDevDependencyDetail;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DownLoadFileUtil {
    public static final Integer TASK_NUM_PER_THREAD = 10;
    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
    private static final Logger logger = Logger.getLogger(DownLoadFileUtil.class);

    private static DataDevScriptFileService fileService;

    @Autowired
    private void setFileService(DataDevScriptFileService fileService) {
        DownLoadFileUtil.fileService = fileService;
    }

    public static void downFile(final BlockingQueue<DataDevDependencyDetail> queue, final State state, final String erp) throws Exception {
        final Map<String, byte[]> fileMap = new ConcurrentHashMap<>();
        final AtomicInteger total =state.getTotal();
        state.setFileMap(fileMap);
        Integer threadNum = state.getThreadNum();
        for(int i=0;i<threadNum;i++){
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!state.isFinish() || queue.size() != 0 ) {
                            final DataDevDependencyDetail detail = queue.poll(1, TimeUnit.SECONDS);
                            if (detail != null) {
                                try {
                                    byte[] bytes = fileService.getScriptBytes(detail.getGitProjectId(), detail.getDependencyGitProjectFilePath(), detail.getDependencyVersion(), erp,true);
                                    fileMap.put(detail.getDependencyGitProjectFilePath(), bytes);
                                    state.addFileSize(bytes.length);
                                } catch (Exception e) {
                                    logger.error("下载文件出错:" + e.getMessage());
                                    state.setException(e.getMessage());
                                }finally {
                                    total.getAndDecrement();
                                }
                            }
                        }
                    } catch (Exception e) {
                        state.setException(e.getMessage());
                    }
                }
            });
        }


    }


    public static class State {
        private Integer state = 0;
        private Map<String, byte[]> fileMap = new ConcurrentHashMap<>();
        private String exception = "";
        //需要下载的数量
        private AtomicInteger total = new AtomicInteger(0);
        private Integer threadNum;
        private AtomicInteger fileSize = new AtomicInteger(0);

        public Integer getFileSize() {
            return fileSize.get();
        }
        public void addFileSize(Integer size){
            fileSize.addAndGet(size);
        }
        public void setTotal(AtomicInteger total) {
            this.total = total;
        }

        public Map<String, byte[]> getFileMap() {
            return fileMap;
        }

        public void setFileMap(Map<String, byte[]> fileMap) {
            this.fileMap = fileMap;
        }

        public String getException() {
            return exception;
        }

        public void setException(String exception) {
            this.exception = exception;
        }

        public AtomicInteger getTotal() {
            return total;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }

        public void setFinish() {
            this.state = 1;
        }

        public boolean isFinish() {
            return this.state == 1;
        }

        public Map<String, byte[]> getResult() throws Exception {
            while (total.get() > 0 && StringUtils.isBlank(exception)) {
                Thread.sleep(1000);
            }
            if (StringUtils.isNotBlank(exception)) {
                throw new RuntimeException(exception);
            }
            return fileMap;
        }

        public Integer getThreadNum() {
            return threadNum!=null?threadNum:0;
        }

        public void setThreadNum(Integer threadNum) {
            this.threadNum = threadNum;
        }
    }
}
