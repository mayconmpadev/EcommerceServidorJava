package com.example.ecommerceservidorjava.activity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroDespesaBinding;
import com.example.ecommerceservidorjava.databinding.DialogDespesaCategoriaBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.model.Parcela;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DatabaseReference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class CadastroDespesaActivity extends AppCompatActivity {
    private ActivityCadastroDespesaBinding binding;
    private DialogDespesaCategoriaBinding categoriaBinding;
    private Despesa despesa = new Despesa();
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private AlertDialog dialog;
    private boolean editar = false;
    private Despesa despesaSelecionado;
    long timestap;
    private SPM spm = new SPM(this);
    private boolean bPagas = false;
    private boolean bLucro = true;
    private boolean bValor = true;
    private ArrayList<Parcela> listPaecela = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroDespesaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        clicks();


        binding.spinnerParcelas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (bLucro) {

                    porcentagem1(binding.spinnerParcelas.getSelectedItem().toString().replace("x", ""));
                    parcelasPagas(Integer.parseInt(binding.spinnerParcelas.getSelectedItem().toString().replace("x", "")), bPagas);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        binding.editValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (bValor) {
                    bLucro = false;


                    porcentagem(binding.spinnerParcelas.getSelectedItem().toString().replace("x", ""));
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.editparcela.setEnabled(false);


    }

    private void porcentagem(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editValor.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string);
        a = a.divide(divisor);
        BigDecimal c = a.divide(b, 2, RoundingMode.HALF_UP);
        binding.editparcela.setText(NumberFormat.getCurrencyInstance().format(c));
        bLucro = true;

    }

    private void porcentagem1(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editValor.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string).add(new BigDecimal("0"));
        a = a.divide(divisor);
        BigDecimal c = a.divide(b, 2, RoundingMode.HALF_UP);
        binding.editparcela.setText(NumberFormat.getCurrencyInstance().format(c));

    }

    private void recuperarIntent() {
        despesaSelecionado = (Despesa) getIntent().getSerializableExtra("despesaSelecionado");
        if (despesaSelecionado != null) {
            binding.btnSalvar.setText("Salvar");
            editar = true;
            bPagas = true;
            binding.editDescricao.setText(despesaSelecionado.getDescricao());
            binding.editInstituicao.setText(despesaSelecionado.getInstituicao());
            binding.editValor.setText(despesaSelecionado.getValor());
            binding.spinnerParcelas.setSelection(despesaSelecionado.getQtd_parcelas() - 1);
            binding.editparcela.setText(despesaSelecionado.getValor_parcela());
            binding.editData.setText(Timestamp.getFormatedDateTime(Long.parseLong(despesaSelecionado.getData()), "dd/MM/yyyy"));

            binding.btnCategorias.setText(despesaSelecionado.getCategoria());
            timestap = Long.parseLong(despesaSelecionado.getData());

            String[] arrayPagamento = getResources().getStringArray(R.array.forma_pagamento);


            for (int i = 0; i < arrayPagamento.length; i++) {
                if (arrayPagamento[i].equals(despesaSelecionado.getTipoPagamento())) {
                    binding.spinnerFPagamento.setSelection(i);
                    break;
                }
            }

            String[] arrayStatus = getResources().getStringArray(R.array.status_despesa);


            for (int i = 0; i < arrayStatus.length; i++) {
                if (arrayStatus[i].equals(despesaSelecionado.getStatus())) {
                    binding.spinnerStatus.setSelection(i);
                    break;
                }
            }

        } else {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            bLucro = false;
            despesa = new Despesa();
            despesa.setId(databaseReference.push().getKey());
            Calendar calendar = Calendar.getInstance();
            long data = calendar.getTimeInMillis();
            binding.editData.setText(Timestamp.getFormatedDateTime(data / 1000, "dd/MM/yyyy"));
            timestap = data / 1000;


        }

    }

    private void parcelasPagas(int qtd, boolean b) {
        String[] pagas = new String[qtd];
        for (int i = 0; i < qtd; i++) {
            pagas[i] = String.valueOf(i + 1);
        }


        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_item, pagas);

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerParcelasPagas.setAdapter(adaptador);
        if (b) {
            binding.spinnerParcelasPagas.setSelection(despesaSelecionado.getParcela_paga() - 1);
        }
        bPagas = false;

    }


    public void salvarDados() {

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("despesas").child(despesa.getId());

        databaseReference.setValue(despesa).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "erro de foto", Toast.LENGTH_SHORT).show();
            }

        });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void validaDados() {

        String descricao = binding.editDescricao.getText().toString();
        String instituicao = binding.editInstituicao.getText().toString();

        String valor = binding.editValor.getText().toString();
        String tipoPagamento = binding.spinnerFPagamento.getSelectedItem().toString();
        String qtdParcelas = binding.spinnerParcelas.getSelectedItem().toString().replace("x", "");
        int proximaParcela = 0;
        if (binding.spinnerParcelasPagas.getSelectedItem() != null) {
            proximaParcela = Integer.parseInt(binding.spinnerParcelasPagas.getSelectedItem().toString());
        }


        String status = binding.spinnerStatus.getSelectedItem().toString();
        String parcela = binding.editparcela.getText().toString();
        String categoria = binding.btnCategorias.getText().toString();
        String data = binding.editData.getText().toString();
        Toast.makeText(getApplicationContext(), valor, Toast.LENGTH_SHORT).show();
        if (descricao.isEmpty()) {
            binding.editDescricao.setError("preencha o campo");
            binding.editDescricao.requestFocus();
        } else if (valor.replaceAll("[^0-9]", "").equals("000")) {
            binding.editValor.setError("preencha o campo");
            binding.editValor.requestFocus();
        } else if (categoria.equals("Nenhuma categoria selecionada")) {
            binding.btnCategorias.setError("preencha o campo");
            binding.btnCategorias.requestFocus();
        } else {

            if (editar) {
                despesa.setId(despesaSelecionado.getId());
            }


            binding.progressBar.setVisibility(View.VISIBLE);
            despesa.setDescricao(descricao);
            despesa.setInstituicao(instituicao);
            despesa.setValor(valor);
            despesa.setQtd_parcelas(Integer.parseInt(qtdParcelas));

            for (int i = 0; i < despesa.getQtd_parcelas(); i++) {
                Parcela parcela1 = new Parcela();
                parcela1.setQtd(i);
                parcela1.setData(Timestamp.convertPoximoMes(this, data, i));
                parcela1.setStatus(false);
                listPaecela.add(parcela1);
            }
            despesa.setParcelas(listPaecela);
            despesa.setStatus(status);
            despesa.setTipoPagamento(tipoPagamento);
            despesa.setValor_parcela(parcela);
            despesa.setData(String.valueOf(timestap));
            despesa.setCategoria(categoria);
            despesa.setParcela_paga(proximaParcela);
            salvarDados();

        }
    }

    public void showDialogCategorias(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog2);

        categoriaBinding = DialogDespesaCategoriaBinding
                .inflate(LayoutInflater.from(this));


        categoriaBinding.layoutAjudantes.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textAjudantes.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutAlimentacao.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textAlimentacao.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutCombustivel.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textCombustivel.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutFerramentas.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textFerramentas.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutHospedagem.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textHospedagem.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutImpostos.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textImpostos.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutLazer.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textLazer.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutMercadoria.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textMercadoria.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutContas.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textContas.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutTransporte.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textTransporte.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutViagem.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textViagem.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutConsumo.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textConsumo.getText());
            dialog.dismiss();
        });

        categoriaBinding.layoutOutros.setOnClickListener(v -> {
            binding.btnCategorias.setText(categoriaBinding.textOutros.getText());
            dialog.dismiss();
        });


        builder.setView(categoriaBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    private void setDateTimeField() {

        final Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Calendar newDate = Calendar.getInstance();
                //newDate.set(year, monthOfYear, dayOfMonth);
                // culto.setText(dateFormatter.format(newDate.getTime()));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long date_ship_millis = calendar.getTimeInMillis();
                timestap = date_ship_millis / 1000;
                binding.editData.setText(Timestamp.getFormatedDateTime(timestap, "dd/MM/yyyy"));


            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }


    private void tipoParcela(int position) {

        if (position <= 2) {
            binding.spinnerParcelas.setSelection(0);
            binding.spinnerParcelas.setEnabled(false);
            binding.spinnerParcelasPagas.setSelection(0);
            binding.spinnerParcelasPagas.setEnabled(false);
            binding.spinnerStatus.setSelection(2);
            binding.spinnerStatus.setEnabled(false);
        } else {
            binding.spinnerParcelas.setEnabled(true);
            binding.spinnerParcelasPagas.setEnabled(true);
            binding.spinnerStatus.setSelection(0);
            binding.spinnerStatus.setEnabled(false);
        }

    }

    public void clicks() {

        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        if (editar) {
            binding.include.textTitulo.setText("Editar");
        } else {
            binding.include.textTitulo.setText("Novo");
        }


        binding.btnSalvar.setOnClickListener(view -> validaDados());

        binding.editData.setOnClickListener(view -> {

            setDateTimeField();
            fromDatePickerDialog.show();
        });
        binding.btnCategorias.setOnClickListener(view -> showDialogCategorias(binding.btnCategorias));
        binding.spinnerFPagamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                tipoParcela(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

}