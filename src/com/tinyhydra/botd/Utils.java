package com.tinyhydra.botd;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Copyright © 2012 tinyhydra.com
 */
public class Utils {
    // Get date for use with voting. This mechanism matches the server's, but obviously grabs the phone's date
    // not the server's date. Potential for problems is low, but -
    // TODO: get server date instead of phone date.
    public static long GetDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Calendar ca = Calendar.getInstance();
        String date = ca.get(Calendar.YEAR) + "/" + (ca.get(Calendar.MONTH) + 1) + "/" + ca.get(Calendar.DAY_OF_MONTH);
        java.util.Date newDate = null;
        try {
            newDate = sdf.parse(date);
        } catch (ParseException pex) {
            pex.printStackTrace();
        }

        return newDate.getTime();
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
}
