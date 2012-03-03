package com.tinyhydra.botd;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Copyright © 2012 tinyhydra.com
 */
public class Utils {
    // Get date for use with voting. This mechanism matches the server's, but obviously grabs the phone's date
    // not the server's date. Potential for problems is low, but -
    // TODO: get server date instead of phone date.
    public static long GetDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    // not implemented. this section will help determine the screen size so we can download
    // the right assets for the current device
    // TODO: implement resource dowloader service and 'loading' dialog.
    public static void GetResources(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                break;
            case DisplayMetrics.DENSITY_HIGH:
                break;
        }
    }

    public static void PostToastMessageToHandler(Handler handler, String message, int toastLength) {
        Message msg = new Message();
        msg.arg1 = Const.CODE_SHOWTOAST;
        msg.arg2 = toastLength;
        Bundle bundle = new Bundle();
        bundle.putString(Const.MessageToastString, message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
