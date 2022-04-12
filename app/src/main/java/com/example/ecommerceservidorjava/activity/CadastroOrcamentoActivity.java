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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.CadastroOrcamentoAdapter;
import com.example.ecommerceservidorjava.adapter.ListaCategoriaHorizontalAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogLojaProdutoBinding;
import com.example.ecommerceservidorjava.model.Categoria;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class CadastroOrcamentoActivity extends AppCompatActivity implements CadastroOrcamentoAdapter.OnClickLister, ListaCategoriaHorizontalAdapter.OnClickLister {
    ActivityCadastroOrcamentoBinding binding;
    CadastroOrcamentoAdapter produtoAdapter;

    private final List<Produto> produtoList = new ArrayList<>();
    private final List<Orcamento> orcamentoList = new ArrayList<>();
    private final List<ItemVenda> itemVendaList = new ArrayList<>();

    private final List<Categoria> categoriaList = new ArrayList<>();

    private final List<Produto> filtroProdutoCategoriaList = new ArrayList<>();
    private List<Produto> filtroProdutoNomeList = new ArrayList<>();
    private List<ItemVenda> filtroItemVendaList = new ArrayList<>();
    private List<ItemVenda> filtroItemVendaCategotia = new ArrayList<>();

    private AlertDialog dialog;
    private SPM spm = new SPM(this);
    private Categoria categoriaSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroOrcamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configSearchView();
        recuperaProdutos("CFTV");
        recuperaCategotia();

    }

    private void configSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String pesquisa) {
                ocultaTeclado();

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
            filtroProdutoNomeList.clear();
            configRvProdutos(produtoList, itemVendaList);
        });

    }


    private void filtraProdutoNome(String pesquisa) {


        for (Produto produto : produtoList) {
            if (produto.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroProdutoNomeList.add(produto);

            }
        }

        for (ItemVenda itemVenda : itemVendaList) {
            if (itemVenda.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroItemVendaList.add(itemVenda);

            }
        }


        configRvProdutos(filtroProdutoNomeList, filtroItemVendaList);

        if (filtroProdutoNomeList.isEmpty()) {
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum produto encontrado.");
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Produto> produtoList, List<ItemVenda> itemVendaList) {
        binding.recycler.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        binding.recycler.setHasFixedSize(true);
        produtoAdapter = new CadastroOrcamentoAdapter(produtoList, itemVendaList, getApplicationContext(), this);
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


    private void recuperaProdutos(String nome) {
        SPM spm = new SPM(getApplicationContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("produtos").orderByChild("nome");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                produtoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Produto produto = ds.getValue(Produto.class);

                        produtoList.add(produto);
                        ItemVenda itemVenda = new ItemVenda();
                        itemVenda.setIdProduto(produto.getId());
                        itemVenda.setIdsCategorias(produto.getIdsCategorias());
                        itemVenda.setCodigo(produto.getCodigo());
                        itemVenda.setNome(produto.getNome());
                        itemVenda.setDescricao(produto.getDescricao());
                        itemVenda.setFoto(produto.getUrlImagem0());
                        itemVendaList.add(itemVenda);


                        binding.progressBar2.setVisibility(View.GONE);
                        binding.textVazio.setVisibility(View.GONE);
                    }
                    configRvProdutos(produtoList, itemVendaList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filtraProdutoCategoria() {
        if (!categoriaSelecionada.isTodas()) {
            filtroProdutoCategoriaList.clear();
            filtroItemVendaCategotia.clear();
            for (Produto produto : produtoList) {
                if (produto.getIdsCategorias().contains(categoriaSelecionada.getId())) {
                    if (!filtroProdutoCategoriaList.contains(produto)) {
                        filtroProdutoCategoriaList.add(produto);
                    }
                }
            }

            for (ItemVenda itemVenda : itemVendaList) {
                if (itemVenda.getIdsCategorias().contains(categoriaSelecionada.getId())) {
                    if (!filtroItemVendaCategotia.contains(itemVenda)) {
                        filtroItemVendaCategotia.add(itemVenda);
                    }
                }
            }

            configRvProdutos(filtroProdutoCategoriaList, filtroItemVendaCategotia);
        } else {
            filtroProdutoCategoriaList.clear();
            filtroItemVendaCategotia.clear();
            configRvProdutos(produtoList, itemVendaList);
        }
    }


    private void showDialogDelete(Produto produto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                CadastroOrcamentoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(CadastroOrcamentoActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + produto.getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            produtoAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            produtoList.remove(produto);

            if (produtoList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(produto);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }


    public void excluir(Produto produto) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos").child(produto.getId());


        for (int i = 0; i < 3; i++) {
            StorageReference storageReference = FirebaseHelper.getStorageReference()
                    .child("empresas")
                    .child(caminho)
                    .child("imagens")
                    .child("produtos")
                    .child(produto.getId())
                    .child("imagem" + i);
            int finalI = i;
            storageReference.delete().addOnSuccessListener(unused -> {
                if (finalI == 2) {
                    binding.progressBar2.setVisibility(View.GONE);
                    databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            produtoAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Excluido com sucesso!", Toast.LENGTH_SHORT).show();
                        }
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
        if (produtoList.isEmpty()) {
            binding.textVazio.setText("Nenhum produto cadastrado.");
        } else {
            binding.textVazio.setText("");
        }
    }

    private void somar(int position, ItemVenda itemVenda) {

        itemVenda.setQtd(itemVenda.getQtd() + 1);

        produtoAdapter.notifyItemChanged(position);
    }

    private void subtrair(int position, ItemVenda itemVenda) {
        itemVenda.setQtd(itemVenda.getQtd() - 1);

        produtoAdapter.notifyItemChanged(position);
    }


    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public void onClick(Categoria categoria) {
        this.categoriaSelecionada = categoria;
        filtraProdutoCategoria();
    }

    @Override
    public void onLongClick(Categoria categoria) {

    }

    @Override
    public void onClick(int position, ItemVenda itemVenda, String operacao) {

        if (operacao.equals("mais")) {
            somar(position, itemVenda);
        }
        if (operacao.equals("menos")) {
            subtrair(position, itemVenda);
        }


    }
}