package com.example.ecommerceservidorjava.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaCategoriaAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaCategoriaBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogFormCategoriaBinding;
import com.example.ecommerceservidorjava.model.Categoria;
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
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaCategoriaActivity extends AppCompatActivity implements ListaCategoriaAdapter.OnClickLister, ListaCategoriaAdapter.OnLongClickLister {
    private ListaCategoriaAdapter listaCategoriaAdapter;
    private ActivityListaCategoriaBinding binding;
    private final List<Categoria> categoriaList = new ArrayList<>();
    private List<Categoria> filtroList = new ArrayList<>();
    private DialogFormCategoriaBinding categoriaBinding;
    private AlertDialog dialog;
    private Categoria categoria;
    String tipo = "";
    private Uri resultUri;
    private SPM spm = new SPM(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaCategoriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        configRvProdutos(filtroList);
        recuperaProdutos();
        //configRv();
        binding.floatingActionButton.setOnClickListener(view -> {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            categoria = new Categoria();
            categoria.setId(databaseReference.push().getKey());
            showDialog(false);
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.floatingActionButton.getVisibility() == View.VISIBLE) {
                    binding.floatingActionButton.hide();
                } else if (dy < 0 && binding.floatingActionButton.getVisibility() != View.VISIBLE) {
                    binding.floatingActionButton.show();
                }
            }
        });
    }

    //---------------------------------------------------- RECUPERAR OBJETO -----------------------------------------------------------------
    private void recuperarIntent() {
        tipo = getIntent().getStringExtra("tipo");

        if (tipo != null) {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            categoria = new Categoria();
            categoria.setId(databaseReference.push().getKey());
            showDialog(false);
        }
    }

    private void configSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String pesquisa) {
                ocultaTeclado();
                filtroList.clear();
                filtraProdutoNome(pesquisa);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.searchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {
            EditText edtSerachView = binding.searchView.findViewById(R.id.search_src_text);
            binding.textVazio.setVisibility(View.GONE);
            edtSerachView.setText("");
            edtSerachView.clearFocus();
            ocultaTeclado();
            filtroList.clear();
            configRvProdutos(categoriaList);
        });

    }


    private void filtraProdutoNome(String pesquisa) {


        for (Categoria categoria : categoriaList) {
            if (Util.removerAcentos(categoria.getNome()).contains(Util.removerAcentos(pesquisa))) {
                filtroList.add(categoria);
            }
        }


        configRvProdutos(filtroList);

        if(filtroList.isEmpty()){
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum categoria encontrada.");
        }else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Categoria> categoriaList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        listaCategoriaAdapter = new ListaCategoriaAdapter( categoriaList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(listaCategoriaAdapter);
        binding.recycler.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {

            }

            @Override
            public void onSwipedRight(int position) {
                showDialogDelete(categoriaList.get(position));
            }
        });
    }


    private void recuperaProdutos() {
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
                        binding.progressBar2.setVisibility(View.GONE);
                        binding.textVazio.setVisibility(View.GONE);
                    }
                    configRvProdutos(categoriaList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
                listaCategoriaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //---------------------------------------------------- DIALOGO DE DELETAR -----------------------------------------------------------------
    private void showDialogDelete(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaCategoriaActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaCategoriaActivity.this));

        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            listaCategoriaAdapter.notifyDataSetChanged();
        });

        deleteBinding.textTitulo.setText("Deseja remover esta categoria ?");

        deleteBinding.btnSim.setOnClickListener(v -> {
            categoriaList.remove(categoria);

            if (categoriaList.isEmpty()) {
                binding.textVazio.setText("Nenhuma categoria cadastrada.");
            } else {
                binding.textVazio.setText("");
            }

            deletarCategoria(categoria);

            listaCategoriaAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    private void deletarCategoria(Categoria categoria) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("categorias").child(categoria.getId());
        databaseReference.removeValue();

        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("categorias").child(categoria.getId());
        storageReferencere.delete();

    }

    //---------------------------------------------------- DIALO DE ADICINAR -----------------------------------------------------------------
    private void showDialog(boolean editar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListaCategoriaActivity.this, R.style.CustomAlertDialog);

        categoriaBinding = DialogFormCategoriaBinding.inflate(LayoutInflater.from(ListaCategoriaActivity.this));

        if (editar) {
            categoriaBinding.edtCategoria.setText(categoria.getNome());
            categoriaBinding.cbTodos.setChecked(categoria.isTodas());
            Glide.with(getApplicationContext())
                    .load(categoria.getUrlImagem())
                    .into(categoriaBinding.imagemCategoria);
        }

        categoriaBinding.btnFechar.setOnClickListener(v -> dialog.dismiss());

        categoriaBinding.btnSalvar.setOnClickListener(v -> {


            String nomeCategoria = categoriaBinding.edtCategoria.getText().toString().trim();
            if (!nomeCategoria.isEmpty()) {

                if (categoria == null) categoria = new Categoria();

                categoria.setNome(nomeCategoria);
                categoria.setTodas(categoriaBinding.cbTodos.isChecked());
                ocultaTeclado();
                categoriaBinding.progressBar.setVisibility(View.VISIBLE);
                if (resultUri != null) {
                    salvarImagemDados(categoria);
                } else if (resultUri == null & categoria.getUrlImagem() != null) {

                    salvarDados(categoria);
                    Toast.makeText(getApplicationContext(), "salvar dados", Toast.LENGTH_SHORT).show();
                } else {
                    categoriaBinding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Escolha uma imagem para a categoria.", Toast.LENGTH_SHORT).show();
                }

            } else {
                categoriaBinding.edtCategoria.setError("Informação obrigatória.");
            }


        });

        categoriaBinding.imagemCategoria.setOnClickListener(v -> {
            chamarImagens();
        });

        builder.setView(categoriaBinding.getRoot());
        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.

    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS-----------------------------------------------------------------
    public void salvarImagemDados(Categoria categoria) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("categorias").child(categoria.getId());
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("categorias").child(categoria.getId() + ".png");


        Glide.with(this).asBitmap().load(resultUri).apply(new RequestOptions().override(1024, 768))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                        resource.compress(Bitmap.CompressFormat.PNG, 70, bytes);

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toByteArray());

                        try {
                            bytes.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        UploadTask uploadTask = storageReferencere.putStream(inputStream);

                        uploadTask.continueWithTask(task -> storageReferencere.getDownloadUrl()).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                categoria.setUrlImagem(uri.toString());
                                databaseReference.setValue(categoria).addOnCompleteListener(task1 -> {


                                    if (task1.isSuccessful()) {

                                        categoriaBinding.imagemCategoria.setImageURI(resultUri);
                                        resultUri = null;
                                        dialog.dismiss();
                                        if (tipo != null){
                                            finish();
                                        }


                                    } else {

                                        storageReferencere.delete(); //apaga a imagem previamente salva no banco
                                        Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

                                    }
                                });


                            } else {

                                Toast.makeText(getApplicationContext(), "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }
                            categoriaBinding.progressBar.setVisibility(View.INVISIBLE);
                        });

                        return false;
                    }
                }).submit();

    }

    private void salvarDados(Categoria categoria) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("categorias").child(categoria.getId());
        databaseReference.setValue(categoria).addOnCompleteListener(task1 -> {


            if (task1.isSuccessful()) {

                categoriaBinding.imagemCategoria.setImageURI(resultUri);
                dialog.dismiss();


            } else {

                Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //---------------------------------------------------- RECORTE DE IMAGEM -----------------------------------------------------------------
    private void chamarImagens() {
        CropImage.activity() // chama intenção de busca a imagem
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                categoriaBinding.imagemCategoria.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    //---------------------------------------------------- OCULTAR TECLADO -----------------------------------------------------------------
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //---------------------------------------------------- INTERFACES DE CLICKS -----------------------------------------------------------------
    public void onClick(Categoria categoria) {
        this.categoria = categoria;
        showDialog(true);


    }

    @Override
    public void onLongClick(Categoria categoria) {

    }


}