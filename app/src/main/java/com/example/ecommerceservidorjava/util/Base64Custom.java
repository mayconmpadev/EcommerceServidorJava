package com.example.ecommerceservidorjava.util;

import android.util.Base64;

/**
 * Created by maycon on 25/01/2018.
 */

public class Base64Custom {
    public static String codificarBase64(String texto){
       return Base64.encodeToString(texto.getBytes(),Base64.DEFAULT).replaceAll("(\\n|\\r)", "");
    }

    public static String decodificarBase64(String textoCodificado){
       return new String(Base64.decode(textoCodificado, Base64.DEFAULT));
    }
}
