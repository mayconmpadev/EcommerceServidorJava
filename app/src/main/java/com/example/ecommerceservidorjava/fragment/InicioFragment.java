package com.example.ecommerceservidorjava.fragment;

import static android.widget.Toast.makeText;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.FragmentInicioBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class InicioFragment extends Fragment {
    private final List<Venda> vendaList = new ArrayList<>();
    private final List<Produto> produtos = new ArrayList<>();
    private final List<Orcamento> orcamentoList = new ArrayList<>();
    private final List<Despesa> despesaList = new ArrayList<>();
    private final List<OrdemServico> ordemServicoList = new ArrayList<>();
    BigDecimal receita = new BigDecimal("0");
    BigDecimal despesa = new BigDecimal("0");
    ArrayList<TextView> mes = new ArrayList<>();
    FragmentInicioBinding binding;
    String data;
    int produtosEstoqueBaixo = 0;
    int mesAtual = 0;
    Handler mHandler = new Handler();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mes.clear();
        mes.add(binding.texthoje);
        mes.add(binding.textJaneiro);
        mes.add(binding.textFevereiro);
        mes.add(binding.textMarco);
        mes.add(binding.textAbril);
        mes.add(binding.textMaio);
        mes.add(binding.textJunho);
        mes.add(binding.textJulho);
        mes.add(binding.textAgosto);
        mes.add(binding.textSetembro);
        mes.add(binding.textOutubro);
        mes.add(binding.textNovembro);
        mes.add(binding.textDezembro);
        data = String.valueOf(Timestamp.getUnixTimestamp());
        data = Timestamp.getFormatedDateTime(Long.parseLong(data), "dd/MM/yyyy");
        binding.textAno.setText(Timestamp.getFormatedDateTime(Timestamp.getUnixTimestamp(), "yyyy"));
       mesAtual =Integer.parseInt (Timestamp.getFormatedDateTime(Timestamp.getUnixTimestamp(), "MM"));

        configClicks();
        produtosEmFalta();
        recuperarDespesaMes(mesAtual,Integer.parseInt(binding.textAno.getText().toString()));
        recuperarOrcamentoMes(mesAtual,Integer.parseInt(binding.textAno.getText().toString()));
        recuperarVendasMes(mesAtual,Integer.parseInt(binding.textAno.getText().toString()));
        recuperarOrdemMes(mesAtual,Integer.parseInt(binding.textAno.getText().toString()));
        mes.get(mesAtual).setBackgroundResource(R.color.color_laranja);
        mes.get(mesAtual).setTextColor(ContextCompat.getColor(getContext(), R.color.branco));


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentInicioBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }



    public void focusOnView(final HorizontalScrollView scroll, final View view) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int vLeft = view.getLeft();
                int vRight = view.getRight();
                int sWidth = scroll.getWidth();

              //  scroll.smoothScrollTo(((vLeft + vRight - sWidth) / 2), 0); para centralizar
                scroll.smoothScrollTo(vLeft , 0);
            }
        });
    }

    private void configClicks() {


        for (int i = 0; i < mes.size(); i++) {
            clickMes(mes.get(i));
        }

        binding.ibAnoProximo.setOnClickListener(v -> {
            int anoAtual = Integer.parseInt(binding.textAno.getText().toString());
            binding.textAno.setText(String.valueOf(anoAtual + 1));
            recuperarVendasMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarOrcamentoMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarDespesaMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarOrdemMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarQtdOrdemMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
        });

        binding.ibAnoAnterior.setOnClickListener(v -> {
            int anoAtual = Integer.parseInt(binding.textAno.getText().toString());
            binding.textAno.setText(String.valueOf(anoAtual - 1));
            recuperarVendasMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarOrcamentoMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarDespesaMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarOrdemMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
            recuperarQtdOrdemMes(mesAtual, Integer.parseInt(binding.textAno.getText().toString()));
        });
    }

    private void clickMes(TextView textView) {

        textView.setOnClickListener(view -> {
            for (int i = 0; i < mes.size(); i++) {
                if (mes.get(i).getText().toString().equals(textView.getText().toString())) {
                    mes.get(i).setBackgroundResource(R.color.color_laranja);
                    mes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.branco));
                    if (i != 0) {
                        mesAtual = i;
                        recuperarVendasMes(i, Integer.parseInt(binding.textAno.getText().toString()));
                        recuperarOrcamentoMes(i, Integer.parseInt(binding.textAno.getText().toString()));
                        recuperarDespesaMes(i, Integer.parseInt(binding.textAno.getText().toString()));
                        recuperarOrdemMes(i, Integer.parseInt(binding.textAno.getText().toString()));
                        recuperarQtdOrdemMes(i, Integer.parseInt(binding.textAno.getText().toString()));

                    } else {
                        recuperarVendasDia(data);
                        recuperarOrcamentoDia(data);
                        recuperarDespesaDia(data);
                        recuperarOrdemDia(data);
                        recuperarQtdOrdemDia(data);
                    }


                } else {
                    mes.get(i).setBackgroundResource(R.color.branco);
                    mes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.preto));
                }
            }

        });

    }

    private void recuperarVendasDia(String data) {
        binding.progressBar.setVisibility(View.VISIBLE);
        vendaList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("vendas").orderByChild("data").startAt(Timestamp.convertInicio(data)).endAt(Timestamp.convertFim(data));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textReceita.setText("R$ 0,00");
                    totalVendas();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Venda venda = snapshot.getValue(Venda.class);
                vendaList.add(venda);

                totalVendas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.set(i, venda);
                    }
                }
                totalVendas();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.remove(i);

                    }
                }
                totalVendas();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void recuperarVendasMes(int mes, int ano) {
        vendaList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("vendas").orderByChild("data").startAt(Timestamp.convertMesInicio(mes, ano)).endAt(Timestamp.convertMesFim(mes, ano));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                  totalVendas();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Venda venda = snapshot.getValue(Venda.class);
                vendaList.add(venda);
                totalVendas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.set(i, venda);
                    }
                }
                totalVendas();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.remove(i);

                    }
                }
                totalVendas();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void recuperarOrcamentoDia(String data) {

        orcamentoList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("orcamentos").orderByChild("data").startAt(Timestamp.convertInicio(data)).endAt(Timestamp.convertFim(data));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textAnalise.setText("0");
                    binding.textAprovado.setText("0");
                    binding.textRecusado.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);
                orcamentoList.add(orcamento);
                totalOrcamentos();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);

                for (int i = 0; i < orcamentoList.size(); i++) {
                    if (orcamentoList.get(i).getId().equals(orcamento.getId())) {
                        orcamentoList.set(i, orcamento);
                    }
                }
                totalOrcamentos();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);

                for (int i = 0; i < orcamentoList.size(); i++) {
                    if (orcamentoList.get(i).getId().equals(orcamento.getId())) {
                        orcamentoList.remove(i);

                    }
                }
                totalOrcamentos();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void recuperarOrcamentoMes(int mes, int ano) {
        orcamentoList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("orcamentos").orderByChild("data").startAt(Timestamp.convertMesInicio(mes, ano)).endAt(Timestamp.convertMesFim(mes, ano));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textAnalise.setText("0");
                    binding.textAprovado.setText("0");
                    binding.textRecusado.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);
                orcamentoList.add(orcamento);
                totalOrcamentos();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);

                for (int i = 0; i < orcamentoList.size(); i++) {
                    if (orcamentoList.get(i).getId().equals(orcamento.getId())) {
                        orcamentoList.set(i, orcamento);
                    }
                }
                totalOrcamentos();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Orcamento orcamento = snapshot.getValue(Orcamento.class);

                for (int i = 0; i < orcamentoList.size(); i++) {
                    if (orcamentoList.get(i).getId().equals(orcamento.getId())) {
                        orcamentoList.remove(i);

                    }
                }
                totalOrcamentos();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void recuperarOrdemMes(int mes, int ano) {
        ordemServicoList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos").orderByChild("dataEntrada").startAt(Timestamp.convertMesInicio(mes, ano)).endAt(Timestamp.convertMesFim(mes, ano));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    totalVendas();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);
                ordemServicoList.add(ordemServico);
                totalVendas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.set(i, ordemServico);
                    }
                }
                totalVendas();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.remove(i);

                    }
                }
                totalVendas();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void recuperarOrdemDia(String data) {
        binding.progressBar.setVisibility(View.VISIBLE);
        vendaList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos").orderByChild("dataEntrada").startAt(Timestamp.convertInicio(data)).endAt(Timestamp.convertFim(data));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textReceita.setText("R$ 0,00");
                    totalVendas();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);
                ordemServicoList.add(ordemServico);

                totalVendas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.set(i, ordemServico);
                    }
                }
                totalVendas();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.remove(i);

                    }
                }
                totalVendas();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void recuperarDespesaDia(String data) {
        binding.texthoje.setBackgroundResource(R.color.color_laranja);
        binding.texthoje.setTextColor(ContextCompat.getColor(getContext(), R.color.branco));
        despesaList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("despesas").orderByChild("data").startAt(Timestamp.convertInicio(data)).endAt(Timestamp.convertFim(data));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textDespesa.setText("R$ 0,00");
                    despesa = Util.convertMoneEmBigDecimal("R$ 0,00");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Despesa despesa = snapshot.getValue(Despesa.class);
                despesaList.add(despesa);
                totalDespesas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Despesa despesa = snapshot.getValue(Despesa.class);

                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.set(i, despesa);
                    }
                }
                totalDespesas();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Despesa despesa = snapshot.getValue(Despesa.class);

                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.remove(i);

                    }
                }
                totalDespesas();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recuperarDespesaMes(int mes, int ano) {
        despesaList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("despesas");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textDespesa.setText("R$ 0,00");
                    despesa = Util.convertMoneEmBigDecimal("R$ 0,00");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Despesa despesa = snapshot.getValue(Despesa.class);
                if (snapshot.exists()){
                    for (int i = 0; i < despesa.getParcelas().size(); i++) {
                        if (Timestamp.getFormatedDateTime(Long.parseLong(despesa.getParcelas().get(i).getData()), "MMyyyy").equals(String.format("%02d", mes) + ano)) {
                            despesaList.add(despesa);
                        }
                    }
                }

                totalDespesas();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Despesa despesa = snapshot.getValue(Despesa.class);
                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.set(i, despesa);
                    }
                }
                totalDespesas();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Despesa despesa = snapshot.getValue(Despesa.class);

                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.remove(i);

                    }
                }
                totalDespesas();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recuperarQtdOrdemDia(String data) {
        binding.texthoje.setBackgroundResource(R.color.color_laranja);
        binding.texthoje.setTextColor(ContextCompat.getColor(getContext(), R.color.branco));
        ordemServicoList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos").orderByChild("dataEntrada").startAt(Timestamp.convertInicio(data)).endAt(Timestamp.convertFim(data));
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);
                ordemServicoList.add(ordemServico);
                binding.progressBar.setVisibility(View.GONE);

                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.set(i, ordemServico);
                    }
                }
                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.remove(i);

                    }
                }
                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recuperarQtdOrdemMes(int mes, int ano) {
        ordemServicoList.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos").orderByChild("dataEntrada").startAt(Timestamp.convertMesInicio(mes, ano)).endAt(Timestamp.convertMesFim(mes, ano));

        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);
                ordemServicoList.add(ordemServico);
                binding.progressBar.setVisibility(View.GONE);

                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.set(i, ordemServico);
                    }
                }
                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.remove(i);

                    }
                }
                binding.textQtdServico.setText(String.valueOf(ordemServicoList.size()));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void totalVendas() {


        BigDecimal totalVendas = new BigDecimal("0");
        for (int i = 0; i < vendaList.size(); i++) {
            BigDecimal preco = Util.convertMoneEmBigDecimal(vendaList.get(i).getTotal());
            preco = preco.divide(new BigDecimal("100"));
            totalVendas = totalVendas.add(preco);
        }
        BigDecimal totalOdens = new BigDecimal("0");
        for (int i = 0; i < ordemServicoList.size(); i++) {
            if (ordemServicoList.get(i).isEntregue()){
                BigDecimal preco = Util.convertMoneEmBigDecimal(ordemServicoList.get(i).getTotal());
                preco = preco.divide(new BigDecimal("100"));
                totalOdens = totalOdens.add(preco);
            }

        }
        receita = totalVendas.add(totalOdens);
        binding.textQtdVendas.setText(String.valueOf(vendaList.size()));
        binding.textReceita.setText(NumberFormat.getCurrencyInstance().format(receita));
        lucro();
    }



    private void totalOrcamentos() {
        int emAnalise = 0, aprovado = 0, recusado = 0;


        for (int i = 0; i < orcamentoList.size(); i++) {
            if (orcamentoList.get(i).getStatus().equals("Em analise")) {
                emAnalise++;
            } else if (orcamentoList.get(i).getStatus().equals("Aprovado")) {
                aprovado++;
            } else {
                recusado++;
            }
        }

        binding.textAnalise.setText(String.valueOf(emAnalise));
        binding.textRecusado.setText(String.valueOf(recusado));
        binding.textAprovado.setText(String.valueOf(aprovado));
    }

    private void totalDespesas() {

        BigDecimal total = new BigDecimal("0");
        for (int i = 0; i < despesaList.size(); i++) {
            BigDecimal preco = Util.convertMoneEmBigDecimal(despesaList.get(i).getValor_parcela());
            preco = preco.divide(new BigDecimal("100"));
            total = total.add(preco);
        }
        despesa = total;
        binding.textDespesa.setText(NumberFormat.getCurrencyInstance().format(total));
        lucro();
    }

    private void lucro() {
        BigDecimal lucro;
        lucro = receita.subtract(despesa);
        binding.textLucro.setText(NumberFormat.getCurrencyInstance().format(lucro));
    }

    private void produtosEmFalta() {
        binding.progressBar.setVisibility(View.VISIBLE);
        produtos.clear();
        SPM spm = new SPM(getContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("produtos");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textReceita.setText("R$ 0,00");
                    
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Produto produto = snapshot.getValue(Produto.class);
                if (Integer.parseInt(produto.getQuantidadeMinima()) > Integer.parseInt(produto.getQuantidadeEtoque())){
                    produtos.add(produto);
                }
                focusOnView(binding.horizontalScrollView, mes.get(mesAtual));
                binding.textProdutosBaixo.setText(String.valueOf(produtos.size()));

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Produto produto = snapshot.getValue(Produto.class);
                for (int i = 0; i < produtos.size(); i++) {
                    if (Integer.parseInt(produtos.get(i).getQuantidadeMinima()) >= Integer.parseInt(produtos.get(i).getQuantidadeEtoque())){
                        produtosEstoqueBaixo++;
                    }
                }




            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.remove(i);

                    }
                }
                totalVendas();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                makeText(getContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeText(getContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

}