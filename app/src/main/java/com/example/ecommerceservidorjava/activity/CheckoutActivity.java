package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCheckoutBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.Usuario;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private Orcamento orcamento;
    private Usuario usuario;
    private ArrayList<ItemVenda> itemVendaList;
    private ArrayList<Endereco> enderecoList = new ArrayList<>();
    private int quantidade;
    private int end = 0;
    private String pagamento = "credito";
    private int desconto = 0;
    private Cliente clienteSelecionado;
    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    clienteSelecionado = (Cliente) result.getData().getSerializableExtra("cliente");

                    binding.edtNome.setText(clienteSelecionado.getNome());
                    binding.edtTelefone.setText(clienteSelecionado.getTelefone1());
                    recuperaEndereco(0);
                }
            }

    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.includeSheet.btnContinue.setText("finalizar");
        binding.include.textTitulo.setText("Checkout");
        recuperarUsuario();
        recuperarIntent();
        configClicks();

    }

    private void recuperaEndereco(final int iEndereco) {
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
                        binding.progressBar.setVisibility(View.GONE);


                    }

                    binding.edtLogradouro.setText(enderecoList.get(iEndereco).getLogradouro());
                    binding.edtNumero.setText(enderecoList.get(iEndereco).getNumero());
                    binding.edtBairro.setText(enderecoList.get(iEndereco).getBairro());
                    binding.edtMunicipio.setText(enderecoList.get(iEndereco).getLocalidade());
                    binding.edtEstado.setText(enderecoList.get(iEndereco).getUf());
                    binding.edtComplemento.setText(enderecoList.get(iEndereco).getComplemento());

                } else {
                    binding.progressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarUsuario() {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("usuarios").child(user);
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void trocarEndereco() {
        end++;
        if (end == enderecoList.size()) {
            end = 0;
        }
        recuperaEndereco(end);
    }

    private void formaDePagamento(View view) {
        Util.vibrar(this, 25);
        switch (view.getId()) {
            case R.id.ib_dinheiro: {
                dinheiro();
                break;
            }
            case R.id.ib_debito: {
                debito();
                break;
            }
            case R.id.ib_credito: {
                credito();
                break;
            }
        }
    }

    private void dinheiro() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda_2);
        binding.ibDebito.setBackgroundResource(R.drawable.borda);
        binding.ibCredito.setBackgroundResource(R.drawable.borda);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.preto));
        //binding.textDinheiro.setTypeface(Typeface.DEFAULT_BOLD);
        binding.textDebito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.grey_40));
        pagamento = "dinheiro";
        binding.includeSheet.tvTotalCart.setText(total(pagamento, 5));
        desconto = 5;
    }

    private void debito() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda);
        binding.ibDebito.setBackgroundResource(R.drawable.borda_2);
        binding.ibCredito.setBackgroundResource(R.drawable.borda);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textDebito.setTextColor(getResources().getColor(R.color.preto));
        binding.textCredito.setTextColor(getResources().getColor(R.color.grey_40));
        pagamento = "debito";
        binding.includeSheet.tvTotalCart.setText(total(pagamento, 3));
        desconto = 3;
    }

    private void credito() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda);
        binding.ibDebito.setBackgroundResource(R.drawable.borda);
        binding.ibCredito.setBackgroundResource(R.drawable.borda_2);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textDebito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.preto));
        pagamento = "credito";
        binding.includeSheet.tvTotalCart.setText(total(pagamento, 0));
        desconto = 0;
    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");
        binding.includeSheet.tvTotalCart.setText(total("credito", 0));
        for (int i = 0; i < itemVendaList.size(); i++) {
            quantidade = quantidade + itemVendaList.get(i).getQtd();

        }
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));

    }

    private String total(String tipo, int valor) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal desconto = new BigDecimal("0");

        for (int i = 0; i < itemVendaList.size(); i++) {
            if (itemVendaList.get(i).getQtd() != 0) {
                BigDecimal preco = Util.convertMoneEmBigDecimal(itemVendaList.get(i).getPreco());
                preco = preco.divide(new BigDecimal("100"));

                total = total.add(new BigDecimal(itemVendaList.get(i).getQtd()).multiply(preco));
            }

        }
        if (!tipo.equals("credito")) {
            desconto = total.multiply(new BigDecimal(valor).divide(new BigDecimal(100)));

        }
        total = total.subtract(desconto);
        return NumberFormat.getCurrencyInstance().format(total);
    }

    private void configClicks() {
        binding.fbtPesquisa.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaClienteActivity.class);
            intent.putExtra("checkout", true);
            resultLauncher.launch(intent);
        });
        binding.imageEndereco.setOnClickListener(view -> trocarEndereco());

        binding.ibDinheiro.setOnClickListener(view -> formaDePagamento(view));
        binding.ibDebito.setOnClickListener(view -> formaDePagamento(view));
        binding.ibCredito.setOnClickListener(view -> formaDePagamento(view));
        binding.includeSheet.btnContinue.setOnClickListener(view -> finalizar());
    }

    private void finalizar() {
        orcamento = new Orcamento();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
        orcamento.setId(databaseReference.push().getKey());
        orcamento.setIdCliente(clienteSelecionado);
        orcamento.setIdEndereco(enderecoList.get(end));
        orcamento.setIdUsuario(usuario);
        orcamento.setData(String.valueOf(Timestamp.getUnixTimestamp()));
        orcamento.setItens(itemVendaList);
        orcamento.setStatus("Em analise");
        orcamento.setDesconto(String.valueOf(desconto));
        orcamento.setTotal(binding.includeSheet.tvTotalCart.getText().toString());

        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("orcamentos").child(user).child(orcamento.getId());
        produtoRef.setValue(orcamento).addOnSuccessListener(unused -> {
            Intent intent = new Intent(getApplicationContext(), ListaOrcamentoActivity.class);
            startActivity(intent);
            finish();
        });


    }
}