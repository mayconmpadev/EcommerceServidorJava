package com.example.ecommerceservidorjava.util;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Jeremy on 06/01/2018.
 */

public class Timestamp {

    /**
     * Obtêm o Timestamp em segundos desde 1/1/1970
     * @return timestamp em segundos
     */
    public static long getUnixTimestamp() {
        // create a calendar
        Calendar cal = Calendar.getInstance();
        long timestamp = cal.getTimeInMillis()/1000;
        return  timestamp;
    }

    /**
     * Obtêm a data e hora no formato especificado a partir do timestamp em ms
     * @param timestamp DateTimeHandler em milisegundos
     * @return Retorna uma string contendo a data e hora no formato desejado
     */
    public static String getFormatedDateTime(long timestamp, String format) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        return DateFormat.format(format, cal).toString();
    }

    public static String convert(String data){
        // data = data.replace("/", "");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,Integer.parseInt(data.substring(6,10)));
        calendar.set(Calendar.MONTH, Integer.parseInt(data.substring(3,5)) -1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(0,2)));
        long date_ship_millis = calendar.getTimeInMillis()/1000;
        return String.valueOf(date_ship_millis);
    }
}
