package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.CategoriaDialogAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroProdutoBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogFormProdutoCategoriaBinding;
import com.example.ecommerceservidorjava.model.Categoria;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CadastroProdutoActivity extends AppCompatActivity implements CategoriaDialogAdapter.OnClick {

    private DialogFormProdutoCategoriaBinding categoriaBinding;
    private ActivityCadastroProdutoBinding binding;
    private final List<String> idsCategoriasSelecionadas = new ArrayList<>();
    private final List<String> caminhoImagens = new ArrayList<>();
    private final List<String> codigoList = new ArrayList<>();
    private final List<String> categoriaSelecionadaList = new ArrayList<>();
    private final List<Categoria> categoriaList = new ArrayList<>();
    private final List<Produto> produtoList = new ArrayList<>();
    private int imagemSelecionada;
    private Uri imagemUri_0, imagemUri_1, imagemUri_2;
    private Produto produto = new Produto();
    private Produto produtoSelecionado;
    private Configuracao configuracao;
    private AlertDialog dialog;
    private boolean editar = false;
    private SPM spm = new SPM(this);

    private boolean bVenda = true;
    private boolean bLucro = true;
    private boolean bCategoria = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarConfiguracao();
        clicks();
        recuperaCategotia();
        gerarCodigo();

        binding.editLucro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (bLucro) {
                    bVenda = false;
                    porcentagem1(binding.editLucro.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.editPrecoCusto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bLucro = false;
                bVenda = false;

                porcentagem(binding.editLucro.getText().toString());


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.editPrecoVenda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (bVenda) {
                    bLucro = false;
                    porcentagem2(binding.editPrecoVenda.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void porcentagem(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editPrecoCusto.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string);
        a = a.divide(divisor);
        BigDecimal c = a.multiply(b.divide(new BigDecimal("100")));
        c = c.add(a);
        binding.editPrecoVenda.setText(NumberFormat.getCurrencyInstance().format(c));
        bLucro = true;
        bVenda = true;
    }

    private void porcentagem1(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editPrecoCusto.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string).add(new BigDecimal("0"));
        a = a.divide(divisor);
        BigDecimal c = a.multiply(b.divide(new BigDecimal("100")));
        c = c.add(a);
        binding.editPrecoVenda.setText(NumberFormat.getCurrencyInstance().format(c));
        bVenda = true;
    }

    private void porcentagem2(String string) {
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal a = Util.convertMoneEmBigDecimal(binding.editPrecoCusto.getText().toString());
        BigDecimal b = Util.convertMoneEmBigDecimal(string).add(new BigDecimal("0"));
        BigDecimal d = b.subtract(a);
        BigDecimal c = d;
        c = c.divide(a, 2, RoundingMode.HALF_UP);
        c = c.multiply(new BigDecimal("100"));
        binding.editLucro.setText(String.valueOf(c.intValue()));
        bLucro = true;
    }

    private void recuperarIntent() {
        produtoSelecionado = (Produto) getIntent().getSerializableExtra("produtoSelecionado");
        if (produtoSelecionado != null) {
            binding.btnSalvar.setText("Salvar");
            editar = true;
            binding.editNome.setText(produtoSelecionado.getNome());
            binding.editDescricao.setText(produtoSelecionado.getDescricao());
            binding.editCodigo.setText(produtoSelecionado.getCodigo());
            binding.editPrecoCusto.setText(produtoSelecionado.getPrecoCusto());
            binding.editPrecoVenda.setText(produtoSelecionado.getPrecoVenda());
            binding.editLucro.setText(produtoSelecionado.getLucro());
            binding.editDesconto.setText(produtoSelecionado.getDesconto());
            binding.editQuantidadeEstoque.setText(produtoSelecionado.getQuantidadeEtoque());
            binding.editQuantidadeMinima.setText(produtoSelecionado.getQuantidadeMinima());
            binding.editObservacao.setText(produtoSelecionado.getObservacao());

            recuperarCategotia(produtoSelecionado.getIdsCategorias());

            if (produtoSelecionado.getUrlImagem0() != null){
                binding.imageFake0.setVisibility(View.GONE);
                Glide.with(this).load(produtoSelecionado.getUrlImagem0()).into(binding.imagemProduto0);
                caminhoImagens.add(produtoSelecionado.getUrlImagem0());
                binding.cardViewImage1.setVisibility(View.VISIBLE);
            }

            if (produtoSelecionado.getUrlImagem1() != null){
                binding.imageFake1.setVisibility(View.GONE);
                Glide.with(this).load(produtoSelecionado.getUrlImagem1()).into(binding.imagemProduto1);
                caminhoImagens.add(produtoSelecionado.getUrlImagem1());
                binding.cardViewImage2.setVisibility(View.VISIBLE);
            }
            if (produtoSelecionado.getUrlImagem2() != null){
                binding.imageFake2.setVisibility(View.GONE);
                Glide.with(this).load(produtoSelecionado.getUrlImagem2()).into(binding.imagemProduto2);
                caminhoImagens.add(produtoSelecionado.getUrlImagem2());
            }


            String sStatus = produtoSelecionado.getStatus();

            for (int i = 0; i < produtoSelecionado.getIdsCategorias().size(); i++) {

                idsCategoriasSelecionadas.add(produtoSelecionado.getIdsCategorias().get(i));

            }
            String[] arrayStatus = getResources().getStringArray(R.array.perfil_usuario);
            for (int i = 0; i < arrayStatus.length; i++) {
                if (arrayStatus[i].equals(sStatus)) {
                    binding.spinnerStatus.setSelection(i);
                    break;
                }
            }

            String sUnidade = produtoSelecionado.getUnidade();
            String[] arrayUnidade = getResources().getStringArray(R.array.unidades);
            for (int i = 0; i < arrayUnidade.length; i++) {
                if (arrayUnidade[i].equals(sUnidade)) {
                    binding.spinnerUnidade.setSelection(i);
                    break;
                }
            }
        } else {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            produto = new Produto();
            produto.setId(databaseReference.push().getKey());
            if (configuracao!= null){
                binding.editLucro.setText(String.valueOf(configuracao.getLucro()));
            }


        }
    }

    private void configRv() {
        categoriaBinding.rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        categoriaBinding.rvCategorias.setHasFixedSize(true);
        CategoriaDialogAdapter categoriaDialogAdapter = new CategoriaDialogAdapter(idsCategoriasSelecionadas, categoriaList, this, this);
        categoriaBinding.rvCategorias.setAdapter(categoriaDialogAdapter);
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

    //---------------------------------------------------- DIALOGO DE DELETAR -----------------------------------------------------------------


    private void recuperaCategotia() {
        SPM spm = new SPM(getApplicationContext());
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(caminho).child("categorias").orderByChild("nome");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                categoriaList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Categoria categoria = ds.getValue(Categoria.class);
                        categoriaList.add(categoria);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categoriasSelecionadas() {
        StringBuilder categorias = new StringBuilder();
        for (int i = 0; i < categoriaSelecionadaList.size(); i++) {
            if (i != categoriaSelecionadaList.size() - 1) {
                categorias.append(categoriaSelecionadaList.get(i)).append(", ");
            } else {
                categorias.append(categoriaSelecionadaList.get(i));
            }
        }

        if (!categoriaSelecionadaList.isEmpty()) {
            binding.btnCategorias.setText(categorias);
            binding.btnCategorias.setError(null);
        } else {
            binding.btnCategorias.setText("Nenhuma categoria selecionada");
        }
    }

    public void validaDados() {
        String nome = binding.editNome.getText().toString();
        String descricao = binding.editDescricao.getText().toString();
        String codigo = binding.editCodigo.getText().toString();
        String observação = binding.editObservacao.getText().toString();
        String precoCusto = binding.editPrecoCusto.getText().toString();
        String precoVenda = binding.editPrecoVenda.getText().toString();
        String lucro = binding.editLucro.getText().toString();
        String quantidadeEstoque = binding.editQuantidadeEstoque.getText().toString();
        String quantidadeMinima = binding.editQuantidadeMinima.getText().toString();

        String desconto = binding.editDesconto.getText().toString();
        if (desconto.isEmpty()) {
            desconto = "0";
        }

        if (imagemUri_0 == null & !editar) {
            Toast.makeText(getApplicationContext(), "Selecione todas as imagens", Toast.LENGTH_SHORT).show();
        }  else if (nome.isEmpty()) {
            binding.editNome.setError("preencha o campo");
            binding.editNome.requestFocus();
        } else if (descricao.isEmpty()) {
            binding.editDescricao.setError("preencha o campo");
            binding.editDescricao.requestFocus();
        } else if (precoCusto.replaceAll("[^0-9]", "").equals("000")) {
            binding.editPrecoCusto.setError("preencha o campo");
            binding.editPrecoCusto.requestFocus();
        } else if (precoVenda.replaceAll("[^0-9]", "").equals("000")) {
            binding.editPrecoVenda.setError("preencha o campo");
            binding.editPrecoVenda.requestFocus();
        } else if (codigo.isEmpty()) {
            binding.editCodigo.setError("preencha o campo");
            binding.editCodigo.requestFocus();
        } else if (quantidadeEstoque.isEmpty()) {
            binding.editQuantidadeEstoque.setError("preencha o campo");
            binding.editQuantidadeEstoque.requestFocus();
        } else if (quantidadeMinima.isEmpty()) {
            binding.editQuantidadeMinima.setError("preencha o campo");
            binding.editQuantidadeMinima.requestFocus();
        } else if (categoriaSelecionadaList.size() == 0) {
            binding.btnCategorias.setError("preencha o campo");
            binding.btnCategorias.requestFocus();
        } else {

            if (editar) {
                produto.setId(produtoSelecionado.getId());
            }


            binding.progressBar3.setVisibility(View.VISIBLE);
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPrecoCusto(precoCusto);
            produto.setPrecoVenda(precoVenda);
            produto.setLucro(lucro);
            produto.setDesconto(desconto);
            produto.setCodigo(codigo);
            produto.setQuantidadeEtoque(quantidadeEstoque);
            produto.setQuantidadeMinima(quantidadeMinima);
            produto.setStatus(binding.spinnerStatus.getSelectedItem().toString());
            produto.setUnidade(binding.spinnerUnidade.getSelectedItem().toString());
            produto.setObservacao(observação);
            produto.setIdsCategorias(idsCategoriasSelecionadas);
            if (imagemUri_0 != null) {
                caminhoImagens.set(0, "");
                salvarDadosImagem(produto, imagemUri_0, 0);
            }
            if (imagemUri_1 != null) {
                caminhoImagens.set(1, "");
                salvarDadosImagem(produto, imagemUri_1, 1);
            }
            if (imagemUri_2 != null) {
                caminhoImagens.set(2, "");
                salvarDadosImagem(produto, imagemUri_2, 2);
            }
            if (imagemUri_0 == null ) {
                salvarDados();
            }
        }
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvarDadosImagem(Produto produto, Uri sUri, int index) {
        binding.progressBar3.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("produtos").child(produto.getId()).child("imagem" + index);


        Glide.with(this).asBitmap().load(sUri).apply(new RequestOptions().override(1024, 768))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toByteArray());

                        try {
                            bytes.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        UploadTask uploadTask = storageReferencere.putStream(inputStream);
                        uploadTask.continueWithTask(task -> storageReferencere.getDownloadUrl()).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();

                                caminhoImagens.set(index, uri.toString());

                                if (!caminhoImagens.contains("")) {
                                    salvarDados();
                                }


                            } else {

                                Toast.makeText(CadastroProdutoActivity.this, "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }

                        });

                        return false;
                    }
                }).submit();

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


    public void recuperarCategotia(List<String> list) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        for (int i = 0; i < list.size(); i++) {
            DatabaseReference database = FirebaseHelper.getDatabaseReference()
                    .child("empresas").child(caminho).child("categorias").child(list.get(i)).child("nome");
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String categoria = snapshot.getValue().toString();
                        categoriaSelecionadaList.add(categoria);
                        categoriasSelecionadas();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }


    public void gerarCodigo() {
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""))).child("produtos").orderByChild("codigo");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                produtoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Produto produto = ds.getValue(Produto.class);
                        codigoList.add(produto.getCodigo());
                    }

                    if (!editar) {
                        binding.editCodigo.setText(String.format("%05d", Integer.parseInt(Collections.max(codigoList)) + 1));
                    }


                } else {
                    binding.editCodigo.setText(String.format("%05d", 1));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void clicks() {
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        if (editar) {
            binding.include.textTitulo.setText("Editar");
        } else {
            binding.include.textTitulo.setText("Novo");
        }

        binding.imagemProduto0.setOnClickListener(view -> {
            imagemSelecionada = 0;
            chamarImagens();
        });
        binding.imagemProduto1.setOnClickListener(view -> {
            imagemSelecionada = 1;
            chamarImagens();
        });
        binding.imagemProduto2.setOnClickListener(view -> {
            imagemSelecionada = 2;
            chamarImagens();
        });

        binding.btnSalvar.setOnClickListener(view -> validaDados());
        binding.btnCategorias.setOnClickListener(view -> showDialogCategorias(binding.btnCategorias));
    }

    private void recuperarConfiguracao() {

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));


        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("configuracao");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    configuracao = snapshot.getValue(Configuracao.class);
                    recuperarIntent();

                }else {
                    recuperarIntent();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onClickListener(Categoria categoria) {
        if (!idsCategoriasSelecionadas.contains(categoria.getId())) { // Adc
            idsCategoriasSelecionadas.add(categoria.getId());
            categoriaSelecionadaList.add(categoria.getNome());
        } else { // Del
            idsCategoriasSelecionadas.remove(categoria.getId());
            categoriaSelecionadaList.remove(categoria.getNome());
        }


    }

    //---------------------------------------------------- RECORTE DE IMAGEM -----------------------------------------------------------------
    private void chamarImagens() {
        bCategoria = false;
        CropImage.activity() // chama intenção de busca a imagem
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (imagemSelecionada == 0) {
                    imagemUri_0 = result.getUri();
                    binding.imagemProduto0.setImageURI(imagemUri_0);
                    binding.imageFake0.setVisibility(View.GONE);
                    binding.cardViewImage1.setVisibility(View.VISIBLE);
                    if (caminhoImagens.size() == 0){
                        caminhoImagens.add("");
                    }

                } else if (imagemSelecionada == 1) {
                    imagemUri_1 = result.getUri();
                    binding.imagemProduto1.setImageURI(imagemUri_1);
                    binding.imageFake1.setVisibility(View.GONE);
                    binding.cardViewImage2.setVisibility(View.VISIBLE);
                    if (caminhoImagens.size() == 1){
                        caminhoImagens.add("");
                    }
                } else {
                    imagemUri_2 = result.getUri();
                    binding.imagemProduto2.setImageURI(imagemUri_2);
                    binding.imageFake2.setVisibility(View.GONE);
                    if (caminhoImagens.size() == 2){
                        caminhoImagens.add("");
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (bCategoria) {
            configRv();
        }

    }
}