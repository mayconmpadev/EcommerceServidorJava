package com.example.ecommerceservidorjava.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.FragmentInicioBinding;

import java.util.ArrayList;
import java.util.List;


public class InicioFragment extends Fragment {

List<TextView> mes = new ArrayList<>();
    FragmentInicioBinding binding;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mes.add(binding.texthoje);
        mes.add(binding.textJaneiro);
        mes.add(binding.textFevereiro);
        mes.add(binding.textMarco);
        mes.add(binding.textAbril);
        mes.add(binding.textMaio);
        mes.add(binding.textJunho);
        mes.add(binding.textJunho);
        mes.add(binding.textAgosto);
        mes.add(binding.textSetembro);
        mes.add(binding.textOutubro);
        mes.add(binding.textNovembro);
        mes.add(binding.textDezembro);
        configClicks();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void configClicks() {
        for (int i = 0; i < mes.size(); i++) {
            clickMes(mes.get(i));
        }
    }


    private void clickMes(TextView textView) {
        textView.setOnClickListener(view -> {
            for (int i = 0; i < mes.size(); i++) {
                if (mes.get(i).getText().toString().equals(textView.getText().toString())){
                    mes.get(i).setBackgroundResource(R.color.color_laranja);
                    mes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.branco));
                }else {
                    mes.get(i).setBackgroundResource(R.color.branco);
                    mes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.preto));
                }
            }

        });

    }

}