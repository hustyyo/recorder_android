package com.example.mediarecorde;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by steveyang on 7/25/15.
 */
public class BaseAsyncTask extends AsyncTask<String, Void, String > {


    private void deleteRecord(String playFileName) {

        File file = new File(playFileName);
        if (file.exists()) {
            file.delete();
        } else {
        }
    }

    public static String firmwareoutgoingfolder = "/config/"; //driver list, pre-op check question, send msg to outgoing table
    public static String firmwarefolderprifix = "FW_";
    public static String firmwareport = "211";
    public static String firmwareserver = "fms.fleetiq360.com";
    //public static String firmwareinternerserver = "localhost";

    public static String firmwareuploadfolder = "/voice/";  // actually firmware
    public static int firmwareintport = 211;
    public static String firmwareuser = "firmware";
    public static String firmwarepass = "Sdh79HfkLq6";
    public static String firmwareinternerserver = "pandora.collectiveintelligence.com.au";

    protected boolean sendFile(String path) {

        File file = new File(path);
        if(!file.exists()){
            deleteRecord(path);
            return false;
        }

        boolean result = Ftp.upload(firmwareinternerserver,firmwareintport,
                firmwareuploadfolder,firmwareuser,firmwarepass,path);

        MainActivity.setUploadResult(result);

        if (result) {
            //Delete file.
            deleteRecord(path);
        }
        else{
            ServiceUploadFile.startUploadFile();
        }
        return result;
    }

    @Override
    protected String doInBackground(String... params) {
        String path = params[0];
        boolean result = sendFile(path);
        return path;
    }

    PostTaskInterface postTask = null;

    public void setPostTask(PostTaskInterface d) {
        this.postTask = d;
    }

    @Override
    protected void onPostExecute(String file) {
        if(postTask != null){
            postTask.onPostExecute(file);
        }
    }
}
