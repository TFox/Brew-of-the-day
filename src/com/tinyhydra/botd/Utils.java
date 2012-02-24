package com.tinyhydra.botd;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Copyright © 2012 mercapps.com
 */
public class Utils {
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
