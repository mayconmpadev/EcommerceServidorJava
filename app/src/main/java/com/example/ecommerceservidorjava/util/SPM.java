package com.example.ecommerceservidorjava.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jeremy on 23/12/2017.
 */

public class SPM {
    private Context context;

    /**
     * Construtor da classe que gerencia o arquivo sharedpreferences
     * @param mContext Contexto a ser utilizado
     */
    public SPM(Context mContext){
        this.context = mContext;
    }

    /**
     * Obtêm o contador global de clic dos botões e incrementa o valor atual
     * @param SharedPreferencesFile Nome do arquivo a ser utilizado
     * @return Contador global do tipo long
     */
    public String getPreferencia(String SharedPreferencesFile, String chave, String valorNaoEncontrado) {
        /*Inicia uma instância do arquivo*/
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPreferencesFile, MODE_PRIVATE);
        /*Obtêm o contador salvo*/
        String preferencia = sharedPreferences.getString(chave, valorNaoEncontrado);
        /*Salva o contador incrementado*/
        //setGlobalClickCount(count+1, SharedPreferencesFile);
        /*Retorna o contador*/
        return preferencia;
    }


    public void setPreferencia(String SharedPreferencesFile, String chave, String valor) {
        /*Inicia uma instância do arquivo*/
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPreferencesFile, MODE_PRIVATE);
        /*Inicia uma instância do editor*/
        SharedPreferences.Editor editor = sharedPreferences.edit();
        /*Insere a chave e valor no arquivo*/
        editor.putString(chave, valor);
        /*Aplica a modificação*/
        editor.apply();
    }
}
