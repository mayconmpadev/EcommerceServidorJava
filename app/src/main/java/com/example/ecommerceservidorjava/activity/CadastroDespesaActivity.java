package com.example.ecommerceservidorjava.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroDespesaBinding;
import com.example.ecommerceservidorjava.databinding.DialogDespesaCategoriaBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.model.Produto;
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
    private boolean bVenda = true;
    private boolean bLucro = true;
    private boolean bValor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroDespesaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        clicks();
        Calendar calendar = Calendar.getInstance();
        long data = calendar.getTimeInMillis();
        binding.editData.setText(Timestamp.getFormatedDateTime(data / 1000, "dd/MM/yyyy"));
        despesa.setData(String.valueOf(data));

        binding.spinnerParcelas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (bLucro) {
                    bVenda = false;
                    porcentagem1(binding.spinnerParcelas.getSelectedItem().toString().replace("x", ""));
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
                    bVenda = false;

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
        bVenda = true;
    }

    private void porcentagem1(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editValor.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string).add(new BigDecimal("0"));
        a = a.divide(divisor);
        BigDecimal c = a.divide(b, 2, RoundingMode.HALF_UP);
        binding.editparcela.setText(NumberFormat.getCurrencyInstance().format(c));
        bVenda = true;
    }

    private void recuperarIntent() {
        despesaSelecionado = (Despesa) getIntent().getSerializableExtra("despesaSelecionado");
        if (despesaSelecionado != null) {
            binding.btnSalvar.setText("Salvar");
            editar = true;

            binding.editDescricao.setText(despesaSelecionado.getDescricao());
            binding.editInstituicao.setText(despesaSelecionado.getInstituicao());
            binding.editValor.setText(despesaSelecionado.getValor());
            binding.spinnerParcelas.setSelection(despesaSelecionado.getQtd_parcelas() + 1);
            binding.editparcela.setText(despesaSelecionado.getValor_parcela());
            binding.editData.setText(Timestamp.getFormatedDateTime(Long.parseLong(despesaSelecionado.getData()) / 1000, "dd/MM/yyyy"));
            binding.btnCategorias.setText(despesaSelecionado.getCategoria());


            String[] arrayUnidade = getResources().getStringArray(R.array.forma_pagamento);


            for (int i = 0; i < arrayUnidade.length; i++) {
                if (arrayUnidade[i].equals(despesaSelecionado.getTipoPagamento())) {
                    binding.spinnerFPagamento.setSelection(i);
                    break;
                }
            }

        } else {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            despesa = new Despesa();
            despesa.setId(databaseReference.push().getKey());



        }
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


    public void validaDados() {

        String descricao = binding.editDescricao.getText().toString();

        String valor = binding.editValor.getText().toString();
        String qtdParcelas = binding.spinnerParcelas.getSelectedItem().toString().replace("x", "");
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
            despesa.setValor(valor);
            despesa.setQtd_parcelas(Integer.parseInt(qtdParcelas));
            despesa.setValor_parcela(parcela);
            despesa.setData(String.valueOf(timestap));
            despesa.setCategoria(categoria);
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
        } else {
            binding.spinnerParcelas.setEnabled(true);
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