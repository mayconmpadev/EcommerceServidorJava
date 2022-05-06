package com.example.ecommerceservidorjava.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaEnderecoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaEnderecoBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaEnderecoActivity extends AppCompatActivity implements ListaEnderecoAdapter.OnClickLister, ListaEnderecoAdapter.OnLongClickLister {
    ActivityListaEnderecoBinding binding;
    private final List<Endereco> enderecoList = new ArrayList<>();
    ListaEnderecoAdapter enderecoAdapter;
    private AlertDialog dialog;
    private Cliente clienteSelecionado;
private SPM spm = new SPM(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaEnderecoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        recuperaEndereco();
        configClicks();
    }

    //---------------------------------------------------- RECUPERAR OBJETO -----------------------------------------------------------------
    private void recuperarIntent() {
        clienteSelecionado = (Cliente) getIntent().getSerializableExtra("clienteSelecionado");
    }

    private void configRvProdutos(List<Endereco> enderecoList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
       enderecoAdapter = new ListaEnderecoAdapter(R.layout.item_lista_usuario, enderecoList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(enderecoAdapter);
    }

    private void recuperaEndereco() {
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("enderecos").child(clienteSelecionado.getId());
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                enderecoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Endereco endereco = ds.getValue(Endereco.class);
                        enderecoList.add(endereco);
                        binding.progressBar2.setVisibility(View.GONE);
                    }
                    configRvProdutos(enderecoList);
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

    private void showDialog(Endereco endereco) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogClienteOpcoesBinding dialogBinding = DialogClienteOpcoesBinding
                .inflate(LayoutInflater.from(this));

        dialogBinding.btnEndereco.setOnClickListener(view -> {

        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroEnderecoActivity.class);
            intent.putExtra("enderecoSelecionado", endereco);
            intent.putExtra("clienteSelecionado", clienteSelecionado);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
           showDialogDelete(endereco);

        });

        dialogBinding.txtNomeProduto.setText(endereco.getNomeEndereco());
        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }
    //---------------------------------------------------- DIALOGO DE DELETAR -----------------------------------------------------------------
    private void showDialogDelete(Endereco endereco) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaEnderecoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaEnderecoActivity.this));

        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
           // listaCategoriaAdapter.notifyDataSetChanged();
        });

        deleteBinding.textTitulo.setText("Deseja remover esta categoria ?");

        deleteBinding.btnSim.setOnClickListener(v -> {
            enderecoList.remove(endereco);

            if (enderecoList.isEmpty()) {
                binding.textVazio.setText("Nenhuma categoria cadastrada.");
            } else {
                binding.textVazio.setText("");
            }

            excluir(endereco);

            enderecoAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }


    private void excluir(Endereco endereco) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("enderecos").child(clienteSelecionado.getId())
                .child(endereco.getId());
        databaseReference.removeValue().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {

                binding.progressBar2.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Produto removido com sucesso!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Erro ao remover", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void configClicks(){
        binding.include.textTitulo.setText("Endereços");
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());

        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroEnderecoActivity.class);
            intent.putExtra("clienteSelecionado", clienteSelecionado);
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

    public void onClick(Endereco endereco) {
        showDialog(endereco);
    }

    @Override
    public void onLongClick(Endereco cliente) {

    }


}