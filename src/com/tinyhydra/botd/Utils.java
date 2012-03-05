package com.tinyhydra.botd;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Brew of the day
 * Copyright (C) 2012  tinyhydra.com
 * *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Utils {
    // Get date for use with voting. This mechanism matches the server's, but obviously grabs the phone's date
    // not the server's date. Potential for problems is low, but -
    // TODO: get server date instead of phone date.
    public static long GetDate() {
        SimpleDateFormat date_format_gmt = new SimpleDateFormat("yyyy-MM-dd");
        date_format_gmt.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        long returnDate = 0;
        try {
            returnDate = date_format_gmt.parse(date_format_gmt.format(Calendar.getInstance().getTime())).getTime();
        } catch (ParseException pex) {
            pex.printStackTrace();
        }
        return returnDate;
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
