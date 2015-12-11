package com.example.mediarecorde;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

/**
 * Created by steveyang on 8/6/15.
 */
public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
    }

    @Override
    public void setMessage(CharSequence message) {
        SpannableString ss = createProgressDialogMessage(message.toString());
        super.setMessage(ss);
    }

    public static SpannableString createProgressDialogMessage(String msg){
        SpannableString ss = new SpannableString(msg);
        ss.setSpan(new RelativeSizeSpan(1.28f), 0, ss.length(), 0);
        return ss;
    }
}
