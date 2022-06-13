package com.example.ecommerceservidorjava.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaDespesaAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaDespesaBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoEnviarBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoStatusBinding;
import com.example.ecommerceservidorjava.model.Despesa;

import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaDespesaActivity extends AppCompatActivity implements ListaDespesaAdapter.OnClickLister, ListaDespesaAdapter.OnLongClickLister {
    ActivityListaDespesaBinding binding;
    ListaDespesaAdapter despesaAdapter;
    private final List<Despesa> despesaList = new ArrayList<>();
    List<Despesa> filtroList = new ArrayList<>();
    SPM spm = new SPM(this);
    private AlertDialog dialog;
    private Despesa despesa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaDespesaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        recuperaOrcamento();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroDespesaActivity.class);
            startActivity(intent);
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

    private void configSearchView() {
        EditText edtSerachView = binding.searchView.findViewById(R.id.search_src_text);
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
                if (newText.isEmpty()) {
                    binding.textVazio.setVisibility(View.GONE);
                    ocultaTeclado();
                    filtroList.clear();
                    configRvProdutos(despesaList);
                }
                return false;
            }
        });

        binding.searchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {

            binding.textVazio.setVisibility(View.GONE);
            edtSerachView.setText("");
            edtSerachView.clearFocus();
            ocultaTeclado();
            filtroList.clear();
            configRvProdutos(despesaList);
        });

    }

    private void recuperarIntent() {
        despesa = (Despesa) getIntent().getSerializableExtra("orcamento");

    }


    private void filtraProdutoNome(String pesquisa) {


        for (Despesa despesa : despesaList) {
            if (despesa.getDescricao().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(despesa);
            }
        }


        configRvProdutos(filtroList);


        if (filtroList.isEmpty()) {
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum produto encontrado.");
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Despesa> despesaList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        despesaAdapter = new ListaDespesaAdapter(R.layout.item_lista_usuario, despesaList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(despesaAdapter);
    }

    private void recuperaOrcamento() {
        SPM spm = new SPM(getApplicationContext());
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("despesas");
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // orcamentoList.clear();
                if (snapshot.exists()) {

                    binding.progressBar2.setVisibility(View.GONE);
                    monitorarLista();

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

    private void monitorarLista() {
        despesaList.clear();
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("despesas").orderByChild("data");
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    Despesa despesa = snapshot.getValue(Despesa.class);
                    despesaList.add(despesa);
                    binding.progressBar2.setVisibility(View.GONE);

                    configRvProdutos(despesaList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Despesa despesa = snapshot.getValue(Despesa.class);

                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.set(i, despesa);
                    }
                }

                despesaAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(despesa.getId())) {
                            filtroList.set(i, despesa);
                        }
                    }

                    despesaAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Despesa despesa = snapshot.getValue(Despesa.class);

                for (int i = 0; i < despesaList.size(); i++) {
                    if (despesaList.get(i).getId().equals(despesa.getId())) {
                        despesaList.remove(i);
                    }
                }

                despesaAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(despesa.getId())) {
                            filtroList.remove(i);
                        }
                    }

                    despesaAdapter.notifyDataSetChanged();
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

    private void showDialog(Despesa despesa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogClienteOpcoesBinding dialogBinding = DialogClienteOpcoesBinding
                .inflate(LayoutInflater.from(this));


       // Glide.with(getApplicationContext())
                //.load(despesa.getIdCliente().getUrlImagem())
              //  .into(dialogBinding.imagemProduto);


        dialogBinding.btnEndereco.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaEnderecoActivity.class);
            intent.putExtra("clienteSelecionado", despesa);
            startActivity(intent);
            dialog.dismiss();
            dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
            intent.putExtra("clienteSelecionado", despesa);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
            showDialogDelete(despesa);


        });

        dialogBinding.txtNomeProduto.setText(despesa.getDescricao());


        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void showDialogDelete(Despesa despesa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaDespesaActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaDespesaActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + despesa.getDescricao() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            despesaAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            despesaList.remove(despesa);

            if (despesaList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(despesa);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    @SuppressLint("NotifyDataSetChanged")
    public void excluir(Despesa despesa) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho).child("orcamentos").child(despesa.getId());
        databaseReference.removeValue();


        binding.progressBar2.setVisibility(View.GONE);


    }

    private void alterarStatus(Despesa despesa, int position, String status) {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("orcamentos")
                .child(despesa.getId()).child("status");
        databaseReference.setValue(status).addOnSuccessListener(unused -> {
            if (filtroList.size() > 0) {
                filtroList.get(position).setStatus(status);
            } else {
                despesaList.get(position).setStatus(status);
            }


            despesaAdapter.notifyItemChanged(position);
        });

    }







    BroadcastReceiver onCompleteVisualizar = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {


        }
    };



    private void showDialog(Despesa despesa, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoOrcamentoBinding dialogBinding = DialogOpcaoOrcamentoBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llEnviar.setOnClickListener(view -> {
            dialog.dismiss();
            showDialogEnviar();

        });

        dialogBinding.llEditar.setOnClickListener(view -> {


        });

        dialogBinding.llClonar.setOnClickListener(view -> {
            dialog.dismiss();

        });

        dialogBinding.llStatus.setOnClickListener(view -> {

            dialog.dismiss();
            showDialogStatus(despesa, position);

        });

        dialogBinding.llPdf.setOnClickListener(view -> {

            dialog.dismiss();

        });

        dialogBinding.llRecibo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReciboActivity.class);
            intent.putExtra("orcamentoSelecionado", despesa);
            startActivity(intent);
            dialog.dismiss();

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogStatus(Despesa despesa, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoStatusBinding dialogBinding = DialogOpcaoStatusBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llAnalise.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(despesa, position, "Em analise");
        });

        dialogBinding.llAprovado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(despesa, position, "Aprovado");
        });

        dialogBinding.llRecusado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(despesa, position, "Recusado");

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogEnviar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoEnviarBinding dialogBinding = DialogOpcaoEnviarBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llWhatsapp.setOnClickListener(view -> {

            dialog.dismiss();
        });

        dialogBinding.llEmail.setOnClickListener(view -> {

            dialog.dismiss();
        });

        dialogBinding.llImprimir.setOnClickListener(view -> {
            dialog.dismiss();


        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }


    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private void listaVazia() {

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("produtos");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<Despesa> arrayList = new ArrayList<Despesa>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Despesa produto = ds.getValue(Despesa.class);

                        arrayList.add(produto);

                    }

                    if (arrayList.size() > 0) {


                    } else {

                    }
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("id", "geral");
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(Despesa despesa, int position) {
        despesa = despesa;

        showDialog(despesa, position);


    }


    @Override
    public void onLongClick(Despesa despesa) {
        showDialogDelete(despesa);
    }
}