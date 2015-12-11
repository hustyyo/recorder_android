package com.example.mediarecorde;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by steveyang on 7/24/15.
 */
public class ServiceUploadFile extends IntentService {

    public ServiceUploadFile(){
        super("ServiceUploadFile");
    }

    private ArrayList<String> list = new ArrayList<String>();


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



    private void initList() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/collectiveintelligence/";

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD Card Error！", Toast.LENGTH_LONG).show();
        } else {

            File file = new File(path);
            File files[] = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {

                    String name = files[i].getName();

                    if (name.indexOf(".") >= 0) {


                        String fileStr = name.substring(files[i].getName().indexOf("."));

                        if (fileStr.toLowerCase().equals(".mp3")
                                || fileStr.toLowerCase().equals(".amr")
                                || fileStr.toLowerCase().equals(".mp4"))

                            list.add(path+"/"+name);
                    }
                }
            }
        }
    }

    private void deleteRecord(String playFileName) {

        File file = new File(playFileName);
        if (file.exists()) {
            file.delete();
        } else {
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        initList();

        while(list.size() > 0) {
            while(!NetworkDetect.isNetworkConnected()){
                SystemClock.sleep(5000);
            }

            for(String path : list){

                File file = new File(path);
                if(!file.exists()){
                    deleteRecord(path);
                    continue;
                }

                boolean result = Ftp.upload(firmwareinternerserver,firmwareintport,
                        firmwareuploadfolder,firmwareuser,firmwarepass,path);
                if(result){
                    //Delete file.
                    deleteRecord(path);
                    String name = path.substring(path.lastIndexOf("/")+1);
                    MainActivity.showFileUploaded(name);
                }
                else{

                }

//                SystemClock.sleep(200);
            }
            SystemClock.sleep(10*1000);

            initList();
        }
    }

    static public void startUploadFile(){
        Intent serviceIntent = new Intent(CIRecorder.getContext(), ServiceUploadFile.class);
        CIRecorder.getContext().startService(serviceIntent);
    }
}
