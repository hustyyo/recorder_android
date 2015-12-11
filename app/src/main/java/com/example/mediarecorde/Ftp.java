package com.example.mediarecorde;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by steveyang on 10/19/15.
 */
public class Ftp {


    static public boolean upload(String host, int port, String folder, String userName, String password, String path){

        FTPClient ftpClient = null;

        try
        {
            ftpClient = new FTPClient();
            ftpClient.connect(host, port);

            if (ftpClient.login(userName, password))
            {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                String name = path.substring(path.lastIndexOf("/") + 1);

                FileInputStream in = new FileInputStream(new File(path));

                boolean result = ftpClient.storeFile(folder + name, in);
                in.close();
                int code = ftpClient.getReplyCode();
                boolean succeed = (result && 226==code);
                if (succeed) {

                    ftpClient.sendSiteCommand("chmod " + "755 " + folder + name);

                    Log.v("upload result", "succeeded");
                }

                ftpClient.logout();
                ftpClient.disconnect();

                return succeed;
            }else{
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
