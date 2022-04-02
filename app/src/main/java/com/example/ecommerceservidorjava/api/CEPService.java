package com.example.ecommerceservidorjava.api;

import com.example.ecommerceservidorjava.model.Endereco;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CEPService {

    @GET("{cep}/json/")
    Call<Endereco> recuperarCEP(@Path("cep") String cep);

}
