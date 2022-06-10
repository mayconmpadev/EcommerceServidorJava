package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroDespesaBinding;
import com.example.ecommerceservidorjava.databinding.DialogFormProdutoCategoriaBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

public class CadastroDespesaActivity extends AppCompatActivity {
    private ActivityCadastroDespesaBinding binding;
    private Despesa despesa = new Despesa();
    private AlertDialog dialog;
    private boolean editar = false;
    private Despesa despesaSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroDespesaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clicks();
    }


    public void salvarDados() {

        produto.setUrlImagem0(caminhoImagens.get(0));
        if (caminhoImagens.size() > 1 ){
            produto.setUrlImagem1(caminhoImagens.get(1));
        }
        if (caminhoImagens.size() > 2 ){
            produto.setUrlImagem2(caminhoImagens.get(2));
        }


        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos").child(produto.getId());

        databaseReference.setValue(produto).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                finish();
            }else {
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

        if (descricao.isEmpty()) {
            binding.editDescricao.setError("preencha o campo");
            binding.editDescricao.requestFocus();
        } else if (valor.equals("R$ 0,00")) {
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
            despesa.setData(data);
            despesa.setCategoria(categoria);

        }
    }

    public void showDialogCategorias(View view) {
        bCategoria = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog2);

        categoriaBinding = DialogFormProdutoCategoriaBinding
                .inflate(LayoutInflater.from(this));

        categoriaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        categoriaBinding.btnSalvar.setOnClickListener(v -> {
            categoriasSelecionadas();
            dialog.dismiss();
        });

        if (categoriaList.isEmpty()) {
            categoriaBinding.textInfo.setText("");
            Intent intent = new Intent(CadastroProdutoActivity.this, ListaCategoriaActivity.class);
            intent.putExtra("tipo", "cadastro");
            startActivity(intent);
        } else {
            categoriaBinding.textInfo.setText("");
        }
        categoriaBinding.progressBar.setVisibility(View.GONE);

        configRv();

        builder.setView(categoriaBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    public void clicks() {
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        if (editar) {
            binding.include.textTitulo.setText("Editar");
        } else {
            binding.include.textTitulo.setText("Novo");
        }


        binding.btnSalvar.setOnClickListener(view -> validaDados());
        binding.btnCategorias.setOnClickListener(view -> showDialogCategorias(binding.btnCategorias));
    }
}