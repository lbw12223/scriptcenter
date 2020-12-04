package com.jd.bdp.datadev.web.jsch.jschTest;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jd.bdp.datadev.web.jsch.Shell;

import javax.swing.*;

/**
 * Created by zhangrui25 on 2018/10/29.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        JSch jsch = new JSch();

        String userName = "zhangrui";
        String password = "zhangrui1561";
        String host = "192.168.144.103";
        Integer port = 22;

        String user = userName;//host.substring(0, host.indexOf('@'));
       // host = host.substring(host.indexOf('@') + 1);

        Session session = jsch.getSession(user, host, 22);

       // String passwd = JOptionPane.showInputDialog("Enter password");
        session.setPassword(password);

        UserInfo ui = new Shell.MyUserInfo() {
            public void showMessage(String message) {
                System.out.println(message);
                JOptionPane.showMessageDialog(null, message);
            }
            public boolean promptYesNo(String message) {
                return true;
            }

        };

        session.setUserInfo(ui);

        // It must not be recommended, but if you want to skip host-key check,
        // invoke following,
        // session.setConfig("StrictHostKeyChecking", "no");

        //session.connect();
        session.connect(30000);   // making a connection with timeout.

        Channel channel = session.openChannel("shell");

        // Enable agent-forwarding.
        //((ChannelShell)channel).setAgentForwarding(true);

        channel.setInputStream(System.in);
      /*
      // a hack for MS-DOS prompt on Windows.
      channel.setInputStream(new FilterInputStream(System.in){
          public int read(byte[] b, int off, int len)throws IOException{
            return in.read(b, off, (len>1024?1024:len));
          }
        });
       */

        channel.setOutputStream(System.out);

      /*
      // Choose the pty-type "vt102".
      ((ChannelShell)channel).setPtyType("vt102");
      */

      /*
      // Set environment variable "LANG" as "ja_JP.eucJP".
      ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
      */

        //channel.connect();
        channel.connect(3 * 1000);
    }

}
