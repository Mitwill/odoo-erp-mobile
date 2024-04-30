package com.mitwill.erp.addons.saleorder.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by st29 on 12/02/18.
 */

public class DateUtils {

    private static final String TAG = DateUtils.class.getSimpleName();

    /*
    *  Returns the number of days since date with the string in the format "dd-MM-yyyy"
    *  String string = "01-01-2016";
    * */
    public static long numberOfDaysSinceDate( String dateString ) {
        DateFormat format = new SimpleDateFormat( "dd-MM-yyyy", Locale.ENGLISH );
        Date date = null;
        Date currentDate = new Date();
        long numberOfDays = 1000;
        try {
            date = format.parse( dateString );
            long diffInMiliseconds = currentDate.getTime()-date.getTime();
            numberOfDays = diffInMiliseconds / ( 24 * 60 * 60 * 1000 );
        } catch ( ParseException e ) {
            e.printStackTrace();
        }
        Log.d( TAG, "numberOfDaysSinceDate: "+numberOfDays );

        return numberOfDays;
    }

    public static String GMTToLocalDateString( String dateStringInGMT ) {
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        try {
            Date date = format.parse( dateStringInGMT );
            format.setTimeZone( TimeZone.getDefault() );
            format.applyPattern("MMM dd yyyy, hh:mm:ss a");
            return format.format( date );
        } catch ( ParseException e ) {
            e.printStackTrace();
        }
        return dateStringInGMT;
    }
}
