package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.ecommerceservidorjava.databinding.DialogFormProdutoCategoriaBinding;
import com.example.ecommerceservidorjava.model.Categoria;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
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
import java.util.ArrayList;
import java.util.List;


public class CadastroProdutoActivity extends AppCompatActivity implements CategoriaDialogAdapter.OnClick {

    private DialogFormProdutoCategoriaBinding categoriaBinding;

    private final List<String> idsCategoriasSelecionadas = new ArrayList<>();
    private final List<String> caminhoImagens = new ArrayList<>();
    private final List<String> categoriaSelecionadaList = new ArrayList<>();
    private final List<Categoria> categoriaList = new ArrayList<>();
    private final List<Uri> resultUri = new ArrayList<>();
    private int imagemSelecionada;
    private Uri imagemUri0, imagemUri1, imagemUri2;
    private Produto produto;
    private Produto produtoSelecionado;
    private AlertDialog dialog;
    private boolean editar = false;
    private SPM spm = new SPM(this);
    String imagem = "";
    int i = 3;
    private ActivityCadastroProdutoBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clicks();
        recuperarIntent();
        recuperaCategotia();

    }

    private void recuperarIntent() {
        produtoSelecionado = (Produto) getIntent().getSerializableExtra("produtoSelecionado");
        if (produtoSelecionado != null) {
            binding.btnSalvar.setText("Salvar");
            editar = true;
            binding.editNome.setText(produtoSelecionado.getNome());
            binding.editDescricao.setText(produtoSelecionado.getDescricao());
            binding.editNome.setText(produtoSelecionado.getNome());
            binding.editNome.setText(produtoSelecionado.getNome());
            binding.editPrecoCusto.setText(produtoSelecionado.getPrecoCusto());
            binding.editPrecoVenda.setText(produtoSelecionado.getPrecoVenda());
            binding.imageFake0.setVisibility(View.GONE);
            binding.imageFake1.setVisibility(View.GONE);
            binding.imageFake2.setVisibility(View.GONE);
            Glide.with(this).load(produtoSelecionado.getUrlImagem0()).into(binding.imagemProduto0);
            Glide.with(this).load(produtoSelecionado.getUrlImagem1()).into(binding.imagemProduto1);
            Glide.with(this).load(produtoSelecionado.getUrlImagem2()).into(binding.imagemProduto2);

            String sStatus = produtoSelecionado.getStatus();

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
        }
    }

    private void configRv() {
        categoriaBinding.rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        categoriaBinding.rvCategorias.setHasFixedSize(true);
        CategoriaDialogAdapter categoriaDialogAdapter = new CategoriaDialogAdapter(idsCategoriasSelecionadas, categoriaList, this, this);
        categoriaBinding.rvCategorias.setAdapter(categoriaDialogAdapter);
    }

    public void showDialogCategorias(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog2);

        categoriaBinding = DialogFormProdutoCategoriaBinding
                .inflate(LayoutInflater.from(this));

        categoriaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        categoriaBinding.btnSalvar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        if (categoriaList.isEmpty()) {
            categoriaBinding.textInfo.setText("Nenhuma categoria cadastrada.");
        } else {
            categoriaBinding.textInfo.setText("");
        }
        categoriaBinding.progressBar.setVisibility(View.GONE);

        configRv();

        builder.setView(categoriaBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }


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
                        // binding.progressBar2.setVisibility(View.GONE);
                        // binding.textVazio.setVisibility(View.GONE);
                    }
                } else {
                    // binding.progressBar2.setVisibility(View.GONE);
                    //  binding.textVazio.setVisibility(View.VISIBLE);
                }
                //listaCategoriaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        if (imagemUri0 == null) {
            Toast.makeText(getApplicationContext(), "Selecione todas as imagens", Toast.LENGTH_SHORT).show();
        } else if (imagemUri1 == null) {
            Toast.makeText(getApplicationContext(), "Selecione todas as imagens", Toast.LENGTH_SHORT).show();
        } else if (imagemUri2 == null) {
            Toast.makeText(getApplicationContext(), "Selecione todas as imagens", Toast.LENGTH_SHORT).show();
        } else if (nome.isEmpty()) {
            binding.editNome.setError("preencha o campo");
            binding.editNome.requestFocus();
        } else if (descricao.isEmpty()) {
            binding.editDescricao.setError("preencha o campo");
            binding.editDescricao.requestFocus();
        } else if (precoCusto.equals("R$ 0,00")) {
            binding.editPrecoCusto.setError("preencha o campo");
            binding.editPrecoCusto.requestFocus();
        } else if (precoVenda.contains("R$ 0,00")) {
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
        } else if (categoriaList.size() != 0) {
            binding.btnCategorias.setError("preencha o campo");
            binding.btnCategorias.requestFocus();
        } else {

            if (editar) {
                produto.setId(produtoSelecionado.getId());
            }
            categoriaSelecionadaList.add("teste");
            resultUri.add(imagemUri0);
            resultUri.add(imagemUri1);
            resultUri.add(imagemUri2);
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPrecoCusto(precoCusto);
            produto.setPrecoVenda(precoVenda);
            produto.setLucro(lucro);
            produto.setDesconto(desconto);
            produto.setCodigo(codigo);
            produto.setStatus(binding.spinnerStatus.getSelectedItem().toString());
            produto.setUnidade(binding.spinnerUnidade.getSelectedItem().toString());
            produto.setObservacao(observação);
            produto.setIdsCategorias(categoriaSelecionadaList);
            salvarDadosImagem(produto, 0);
            salvarDadosImagem(produto, 1);
            salvarDadosImagem(produto, 2);

        }
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvarDadosImagem(Produto produto, int index) {
        binding.progressBar3.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("produtos").child(produto.getId()).child("imagem" + index);


        Glide.with(this).asBitmap().load(resultUri.get(index)).apply(new RequestOptions().override(1024, 768))
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
                                caminhoImagens.add(uri.toString());
                                if (caminhoImagens.size() == 3) {
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
        produto.setUrlImagem1(caminhoImagens.get(1));
        produto.setUrlImagem2(caminhoImagens.get(2));
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos").child(produto.getId());

        databaseReference.setValue(produto).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                binding.progressBar3.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), ListaProdutoActivity.class);
                startActivity(intent);
                finish();
            }

        });

    }

    public void clicks() {
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
                    imagemUri0 = result.getUri();
                    binding.imagemProduto0.setImageURI(imagemUri0);
                    binding.imageFake0.setVisibility(View.GONE);
                } else if (imagemSelecionada == 1) {
                    imagemUri1 = result.getUri();
                    binding.imagemProduto1.setImageURI(imagemUri1);
                    binding.imageFake1.setVisibility(View.GONE);
                } else {
                    imagemUri2 = result.getUri();
                    binding.imagemProduto2.setImageURI(imagemUri2);
                    binding.imageFake2.setVisibility(View.GONE);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}