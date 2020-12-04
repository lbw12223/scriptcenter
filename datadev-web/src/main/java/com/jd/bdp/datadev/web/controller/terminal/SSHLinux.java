package com.jd.bdp.datadev.web.controller.terminal;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SSHLinux {

    private String hostname = "11.11.11.15";
    private int port = 22;
    private String username = "root";
    private String password = "123456";

    private Connection conn;
    private Session sess;
    private BufferedReader stdout;
    private BufferedReader stderr;
    private PrintWriter pw;

    public SSHLinux() {
        //使用默认值
        long t1 = System.currentTimeMillis();
        initSession();
        long t2 = System.currentTimeMillis();
        System.out.println("远程登陆linux，连接耗时：" + (t2 - t1) / 1000.0 + "s");
    }

    public SSHLinux(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;

        long t1 = System.currentTimeMillis();
        initSession();
        long t2 = System.currentTimeMillis();
        System.out.println("远程登陆linux，连接耗时：" + (t2 - t1) / 1000.0 + "s");
    }

    //初始化连接并建立虚拟终端
    public void initSession() {
        try {
            conn = new Connection(hostname, port);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");

            sess = conn.openSession();
            sess.requestDumbPTY();//建立虚拟终端
            sess.startShell();//打开一个Shell


            stdout = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout()), Charset.forName("utf-8")));
            stderr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr()), Charset.forName("utf-8")));
            pw = new PrintWriter(sess.getStdin(), true);// 准备输入命令

        } catch (IOException e) {
            System.out.println("------建立ssh linux连接错误------");
        }
    }


    public void close() {
        try {
            stdout.close();
            stderr.close();
            pw.close();
            sess.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("------关闭ssh连接错误------");
        }

    }

    public List<String> execute(String strcmd) {

        System.out.println("在execute()中接收到命令:" + strcmd);
        List<String> rstList = new ArrayList<String>();
        try {
            if (strcmd.equals("exit")) {
                pw.println(strcmd);// 输入待执行命令
                close();
                rstList.add("用户退出，关闭连接!!!");
                return rstList;
            }

            pw.println(strcmd);// 输入待执行命令

            while (true) {
                String line = stdout.readLine();
                if (line == null || line.trim().endsWith("#"))
                    break;

                System.out.println(line);
                rstList.add(line);
            }

        } catch (IOException e) {
            System.out.println("------连接已经关闭或命令出错------");
        }

        return rstList;
    }


    public static void main(String[] args) {
        SSHLinux ssh = new SSHLinux("vm1.zhangrui.com", 22, "root", "zhangrui156");
        List<String> list = ssh.execute("tail -f /tmp/test");
        System.out.println("list----: " + list);

        //ssh.execute("exit");
    }
}


