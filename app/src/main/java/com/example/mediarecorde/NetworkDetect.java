package com.example.mediarecorde;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by steveyang on 9/07/2015.
 */
public class NetworkDetect {

    static public boolean isNetworkConnected () {
        ConnectivityManager connMgr = (ConnectivityManager) CIRecorder.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
