package com.example.ecommerceservidorjava.activity;

import android.app.Activity;
import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaCategoriaHorizontalAdapter;
import com.example.ecommerceservidorjava.adapter.ListaProdutoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaProdutoBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogLojaProdutoBinding;
import com.example.ecommerceservidorjava.model.Categoria;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListaProdutoActivity extends AppCompatActivity implements ListaProdutoAdapter.OnClickLister, ListaProdutoAdapter.OnLongClickLister, ListaCategoriaHorizontalAdapter.OnClickLister {
    ActivityListaProdutoBinding binding;
    ListaProdutoAdapter produtoAdapter;

    private final List<Produto> produtoList = new ArrayList<>();
    private final List<Categoria> categoriaList = new ArrayList<>();
    private List<Produto> filtroProdutoCategoriaList = new ArrayList<>();
    List<Produto> filtroProdutoNomeList = new ArrayList<>();
    private AlertDialog dialog;
    private SPM spm = new SPM(this);
    private Categoria categoriaSelecionada;
    private String pesquisa = "";
    int qtdImagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configSearchView();
        recuperaProdutos();
        recuperaCategotia();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroProdutoActivity.class);
            startActivity(intent);
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.floatingActionButton.getVisibility() == View.VISIBLE) {
                    binding.floatingActionButton.hide();
                } else if (dy < 0 && binding.floatingActionButton.getVisibility() != View.VISIBLE) {
                    binding.floatingActionButton.show();
                }
            }
        });

    }

    private void configSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String texto) {
                ocultaTeclado();
                pesquisa = texto;
                filtraProdutoNome(texto);
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
            pesquisa = "";
            edtSerachView.clearFocus();
            ocultaTeclado();

            configRvProdutos(filtroProdutoCategoriaList);
        });

    }


    private void filtraProdutoNome(String pesquisa) {

        filtroProdutoNomeList.clear();
        for (Produto produto : filtroProdutoCategoriaList) {
            if (produto.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroProdutoNomeList.add(produto);
            }
        }


        configRvProdutos(filtroProdutoNomeList);

        if (filtroProdutoNomeList.isEmpty()) {
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum produto encontrado.");
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Produto> usuarioList) {
        binding.recycler.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        binding.recycler.setHasFixedSize(true);
        produtoAdapter = new ListaProdutoAdapter(R.layout.item_produto_adapter, usuarioList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(produtoAdapter);
    }

    private void configRvCategoria() {
        binding.rvCategorias.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategorias.setHasFixedSize(true);
        ListaCategoriaHorizontalAdapter listaCategoriaAdapter = new ListaCategoriaHorizontalAdapter(R.layout.item_lista_usuario, categoriaList, getApplicationContext(), true, this, true);
        binding.rvCategorias.setAdapter(listaCategoriaAdapter);
    }


    private void recuperaCategotia() {
        SPM spm = new SPM(getApplicationContext());
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(caminho).child("categorias").orderByChild("todas");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                categoriaList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Categoria categoria = ds.getValue(Categoria.class);
                        categoriaList.add(categoria);

                        if (categoria.isTodas() && categoriaSelecionada == null) {
                            categoriaSelecionada = categoria;
                        }
                    }
                    Collections.reverse(categoriaList);
                    configRvCategoria();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void recuperaProdutos() {
        SPM spm = new SPM(getApplicationContext());
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("produtos");
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //produtoList.clear();
                if (snapshot.exists()) {
                    binding.progressBar2.setVisibility(View.GONE);
                    monitorarLista();
                } else {
                    monitorarLista();
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void monitorarLista() {
        produtoList.clear();
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("produtos").orderByChild("data");
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildAdded", Toast.LENGTH_SHORT).show();
                if (snapshot.exists()) {
                    Produto produto = snapshot.getValue(Produto.class);
                    produtoList.add(produto);
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.GONE);

                    configRvProdutos(produtoList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildChanged", Toast.LENGTH_SHORT).show();
                Produto produto = snapshot.getValue(Produto.class);

                for (int i = 0; i < produtoList.size(); i++) {
                    if (produtoList.get(i).getId().equals(produto.getId())) {
                        produtoList.set(i, produto);
                    }
                }

                produtoAdapter.notifyDataSetChanged();
                if (!filtroProdutoNomeList.isEmpty()) {
                    for (int i = 0; i < filtroProdutoNomeList.size(); i++) {
                        if (filtroProdutoNomeList.get(i).getId().equals(produto.getId())) {
                            filtroProdutoNomeList.set(i, produto);
                        }
                    }

                    produtoAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Toast.makeText(getApplicationContext(), "onChildRemoved", Toast.LENGTH_SHORT).show();
                Produto produto = snapshot.getValue(Produto.class);

                if (produtoList.size() == 0){
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < produtoList.size(); i++) {
                    if (produtoList.get(i).getId().equals(produto.getId())) {
                        produtoList.remove(i);




                    }
                }
                listVazia();
                produtoAdapter.notifyDataSetChanged();
                if (!filtroProdutoNomeList.isEmpty()) {
                    for (int i = 0; i < filtroProdutoNomeList.size(); i++) {
                        if (filtroProdutoNomeList.get(i).getId().equals(produto.getId())) {
                            filtroProdutoNomeList.remove(i);


                        }
                    }
                    listVazia();
                    produtoAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void filtraProdutoCategoria() {
        if (!categoriaSelecionada.isTodas()) {
            filtroProdutoCategoriaList.clear();
            for (Produto produto : produtoList) {
                if (produto.getIdsCategorias().contains(categoriaSelecionada.getId())) {
                    if (!filtroProdutoCategoriaList.contains(produto)) {
                        filtroProdutoCategoriaList.add(produto);
                    }
                }
            }
            if (!pesquisa.equals("")) {
                filtraProdutoNome(pesquisa);
            } else {
                configRvProdutos(filtroProdutoCategoriaList);
            }


        } else {
            filtroProdutoCategoriaList = new ArrayList<>(produtoList);
            if (!pesquisa.equals("")) {
                filtraProdutoNome(pesquisa);
            } else {
                configRvProdutos(filtroProdutoCategoriaList);
            }

        }
    }


    private void showDialogDelete(Produto produto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaProdutoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaProdutoActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + produto.getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            produtoAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {

            dialog.dismiss();
            excluir(produto);


        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }


    public void excluir(Produto produto) {

        qtdImagem = 0;
        if (produto.getUrlImagem0() != null) qtdImagem++;
        if (produto.getUrlImagem1() != null) qtdImagem++;
        if (produto.getUrlImagem2() != null) qtdImagem++;
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos").child(produto.getId());


        for (int i = 0; i < qtdImagem; i++) {
            StorageReference storageReference = FirebaseHelper.getStorageReference()
                    .child("empresas")
                    .child(caminho)
                    .child("imagens")
                    .child("produtos")
                    .child(produto.getId())
                    .child("imagem" + i);
            int finalI = i;
            storageReference.delete().addOnSuccessListener(unused -> {
                if (finalI == qtdImagem - 1) {
                    binding.progressBar2.setVisibility(View.GONE);
                    databaseReference.removeValue().addOnSuccessListener(unused1 -> {
                        produtoAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Excluido com sucesso!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

    }

    public void alterarStatus(Produto produto, boolean tipo) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos").child(produto.getId());
        if (tipo) {
            databaseReference.child("status").setValue("rascunho");
        } else {
            databaseReference.child("status").setValue("em estoque");
        }


    }
    //---------------------------------------------------- DIALOGO DE DELETAR -----------------------------------------------------------------

    private void showDialog(Produto produto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogLojaProdutoBinding dialogBinding = DialogLojaProdutoBinding
                .inflate(LayoutInflater.from(this));
        if (produto.getStatus().equals("rascunho")) {
            dialogBinding.cbRascunho.setChecked(true);
        } else {
            dialogBinding.cbRascunho.setChecked(false);
        }

        Glide.with(getApplicationContext())
                .load(produto.getUrlImagem0())
                .into(dialogBinding.imagemProduto);
        dialogBinding.cbRascunho.setOnCheckedChangeListener((check, b) -> {
            //  produto.setRascunho(check.isChecked());
            // produto.salvar(false);
        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroProdutoActivity.class);
            intent.putExtra("produtoSelecionado", produto);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
            showDialogDelete(produto);
            listVazia();

        });

        dialogBinding.txtNomeProduto.setText(produto.getNome());

        dialogBinding.btnFechar.setOnClickListener(v -> {
            if (dialogBinding.cbRascunho.isChecked()) {
                alterarStatus(produto, true);
                dialog.dismiss();
            } else {
                alterarStatus(produto, false);
                dialog.dismiss();
            }
        });

        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.

    }

    private void listVazia() {
        if (produtoList.size() == 0) {
            binding.textVazio.setVisibility(View.VISIBLE);
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }


    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public void onClick(Produto produto) {
        showDialog(produto);


    }

    @Override
    public void onLongClick(Produto produto) {

    }


    @Override
    public void onClick(Categoria categoria) {
        this.categoriaSelecionada = categoria;
        filtraProdutoCategoria();
    }

    @Override
    public void onLongClick(Categoria categoria) {

    }
}
//TODO: atualizar lista com estoque baixo.