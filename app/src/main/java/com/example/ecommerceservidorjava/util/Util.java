package com.example.ecommerceservidorjava.util;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;

/**
 * Created by mac on 09/05/2018.
 */

public class Util {


    public static boolean verificarInternet(Context context) {


        ConnectivityManager conexao = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo informacao = conexao.getActiveNetworkInfo();


        if (informacao != null && informacao.isConnected()) {

            return true;

        } else {

            return false;
        }


    }

    public static void textoNegrito(final String texto, final TextView textView, EditText editText) {

        final SpannableString text = new SpannableString(texto.toString().replace("+", "").replace("/", "").replace("*", ""));
        int a = 0;
        int b = texto.length();
        int c = 0;
        int d = 0;
        int e = texto.length();
        int f = 0;
        for (int i = 0; i < texto.length(); i++) {

            if ("+".equals(texto.substring(i, i + 1)) || "/".equals(texto.substring(i,i + 1)) || "*".equals(texto.substring(i,i + 1))) {
                if (b == texto.length()) {
                    b = a;
                } else {
                    c = a - 1;
                    if ("+".equals(texto.substring(i, i +1))){
                        text.setSpan(new UnderlineSpan(), b, c, 0);
                        text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), b, c, 0);
                    }else if("/".equals(texto.substring(i, i +1))){
                        text.setSpan(new UnderlineSpan(), b, c, 0);
                    }else if("*".equals(texto.substring(i, i +1))){
                        text.setSpan(new ForegroundColorSpan(Color.BLACK), b, c, 0);
                        text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), b, c, 0);
                    }

                    a--;
                    b = texto.length();
                    c = 0;
                    a--;
                }
            }
            a++;

        }

        // text.setSpan(new ForegroundColorSpan(Color.RED), negrito.get(i).comeco, negrito.get(i).fim - 1, 0);
        // text.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), negrito.get(i).comeco - 1, negrito.get(i).comeco, 0);
        // text.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), negrito.get(i).fim - 1, negrito.get(i).fim, 0);
        // text.setSpan(new ForegroundColorSpan(Color.RED), 5, 9, 0);
        if (textView == null) {
            editText.setText(text, EditText.BufferType.SPANNABLE);
        } else {
            textView.setText(text, EditText.BufferType.SPANNABLE);
        }

    }


    public static void vibrar(Context context, int milisegundos) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(milisegundos);
        }
    }

    public static Activity activity(Context context) {
        while (!(context instanceof Activity)) {
            if (!(context instanceof ContextWrapper)) {
                context = null;
            }
            ContextWrapper contextWrapper = (ContextWrapper) context;
            if (contextWrapper == null) {
                return null;
            }
            context = contextWrapper.getBaseContext();
            if (context == null) {
                return null;
            }
        }
        return (Activity) context;
    }


    public static java.math.BigDecimal convertMoneEmBigDecimal(String monetaria) {
        java.math.BigDecimal bigDecimal;
        String vazia = "";
        try {
            vazia = monetaria.replace("R$", "");
            vazia = vazia.replace("\u00A0", "");
            vazia = vazia.replace(",", "");
            vazia = vazia.replace(".", "");

            bigDecimal = new java.math.BigDecimal(vazia);
        } catch (Exception e) {
            bigDecimal = new BigDecimal("0.00");
        }
        return bigDecimal;
    }

}
