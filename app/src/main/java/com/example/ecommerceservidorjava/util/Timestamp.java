package com.example.ecommerceservidorjava.util;

import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Jeremy on 06/01/2018.
 */

public class Timestamp {

    /**
     * Obtêm o Timestamp em segundos desde 1/1/1970
     *
     * @return timestamp em segundos
     */
    public static long getUnixTimestamp() {
        // create a calendar
        Calendar cal = Calendar.getInstance();
        long timestamp = cal.getTimeInMillis() / 1000;
        return timestamp;
    }

    /**
     * Obtêm a data e hora no formato especificado a partir do timestamp em ms
     *
     * @param timestamp DateTimeHandler em milisegundos
     * @return Retorna uma string contendo a data e hora no formato desejado
     */
    public static String getFormatedDateTime(long timestamp, String format) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        return DateFormat.format(format, cal).toString();
    }

    public static String convert(String data) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(data.substring(6, 10)));
        calendar.set(Calendar.MONTH, Integer.parseInt(data.substring(3, 5)) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(0, 2)));
        long date_ship_millis = calendar.getTimeInMillis() / 1000;
        return String.valueOf(date_ship_millis);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertPoximoMes(Context context, String data, int mes) {
        LocalDateTime localDate = LocalDateTime.now();

        localDate.withYear(Integer.parseInt(data.substring(6, 10)));

        localDate = localDate.withMonth(Integer.parseInt(data.substring(3, 5)));
        localDate = localDate.plusMonths(mes);
        Toast.makeText(context, String.valueOf(localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()), Toast.LENGTH_SHORT).show();
if (Integer.parseInt(data.substring(0, 2)) > localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()){
    localDate = localDate.withDayOfMonth(localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth());
}else {
    localDate = localDate.withDayOfMonth(Integer.parseInt(data.substring(0, 2)));
}


        ZonedDateTime zdt = ZonedDateTime.of(localDate, ZoneId.systemDefault());
        long date_ship_millis = zdt.toInstant().toEpochMilli() / 1000;
        return String.valueOf(date_ship_millis);
    }

    public static String convertInicio(String data) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(data.substring(6, 10)));
        calendar.set(Calendar.MONTH, Integer.parseInt(data.substring(3, 5)) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(0, 2)));
        calendar.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        calendar.set(Calendar.MINUTE, 0);                 // set minute in hour
        calendar.set(Calendar.SECOND, 0);                 // set second in minute
        calendar.set(Calendar.MILLISECOND, 0);            // set millis in second
        long date_ship_millis = calendar.getTimeInMillis() / 1000;
        return String.valueOf(date_ship_millis);
    }

    public static String convertFim(String data) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(data.substring(6, 10)));
        calendar.set(Calendar.MONTH, Integer.parseInt(data.substring(3, 5)) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(0, 2)));
        calendar.set(Calendar.HOUR_OF_DAY, 23);            // set hour to midnight
        calendar.set(Calendar.MINUTE, 59);                 // set minute in hour
        calendar.set(Calendar.SECOND, 59);                 // set second in minute
        calendar.set(Calendar.MILLISECOND, 0);            // set millis in second
        long date_ship_millis = calendar.getTimeInMillis() / 1000;
        return String.valueOf(date_ship_millis);
    }

    public static String convertMesInicio(int mes, int ano) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(mes, 0);
        calendar.set(Calendar.YEAR, ano);
        calendar.set(Calendar.MONTH, mes - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        calendar.set(Calendar.MINUTE, 0);                 // set minute in hour
        calendar.set(Calendar.SECOND, 0);                 // set second in minute
        calendar.set(Calendar.MILLISECOND, 0);            // set millis in second
        long date_ship_millis = calendar.getTimeInMillis() / 1000;
        return String.valueOf(date_ship_millis);
    }

    public static String convertMesFim(int mes, int ano) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(mes, 0);
        calendar.set(Calendar.YEAR, ano);
        calendar.set(Calendar.MONTH, mes - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);            // set hour to midnight
        calendar.set(Calendar.MINUTE, 59);                 // set minute in hour
        calendar.set(Calendar.SECOND, 59);                 // set second in minute
        calendar.set(Calendar.MILLISECOND, 0);            // set millis in second
        long date_ship_millis = calendar.getTimeInMillis() / 1000;
        return String.valueOf(date_ship_millis);
    }
}
