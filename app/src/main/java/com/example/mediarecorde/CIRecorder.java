package com.example.mediarecorde;

import android.app.Application;
import android.content.Context;

/**
 * Created by steveyang on 10/19/15.
 */
public class CIRecorder extends Application {

    static Context context = null;

    public void onCreate(){

        super.onCreate();

        context = getApplicationContext();

        ServiceUploadFile.startUploadFile();
    }

    public static Context getContext(){
        return context;
    }
}
