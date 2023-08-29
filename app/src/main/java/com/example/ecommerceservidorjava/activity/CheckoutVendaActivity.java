package com.example.ecommerceservidorjava.activity;

import static com.itextpdf.text.Rectangle.NO_BORDER;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCheckoutVendaBinding;
import com.example.ecommerceservidorjava.model.Boleto;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.model.Usuario;
import com.example.ecommerceservidorjava.model.Venda;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CheckoutVendaActivity extends AppCompatActivity {
    private ActivityCheckoutVendaBinding binding;
    private Venda venda;
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    private Usuario usuario;
    private SPM spm = new SPM(this);
    private ArrayList<ItemVenda> itemVendaList;
    private ArrayList<Endereco> enderecoList = new ArrayList<>();
    private int quantidade;
    private int qtdEstoque;
    private int end = 0;
    private String pagamento = "credito";
    private String subTotal = "";
    private int desconto = 0;
    private Cliente clienteSelecionado;

    Document document = new Document();
    File myFile;


    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 2) {
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
        binding = ActivityCheckoutVendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.includeSheet.btnContinue.setText("finalizar");
        binding.include.textTitulo.setText("Checkout");
        recuperarUsuario();
        recuperarIntent();
        recuperarConfiguracao();
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
                if (snapshot.exists()) {
                    usuario = snapshot.getValue(Usuario.class);
                } else {
                    Toast.makeText(getApplicationContext(), "usuario não exixte", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void recuperarConfiguracao() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("configuracao");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    configuracao = snapshot.getValue(Configuracao.class);

                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
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

            case R.id.ib_boleto: {
                boleto();
                break;
            }
        }
    }

    private void dinheiro() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda_2);
        binding.ibDebito.setBackgroundResource(R.drawable.borda);
        binding.ibCredito.setBackgroundResource(R.drawable.borda);
        binding.ibBoleto.setBackgroundResource(R.drawable.borda);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.preto));
        binding.textDebito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textBoleto.setTextColor(getResources().getColor(R.color.grey_40));
        pagamento = "dinheiro";
        desconto = configuracao.getDesconto_dinheiro();
        binding.includeSheet.tvTotalCart.setText(total(pagamento, binding.swDesconto.isChecked() ? desconto : 0));

    }

    private void debito() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda);
        binding.ibDebito.setBackgroundResource(R.drawable.borda_2);
        binding.ibCredito.setBackgroundResource(R.drawable.borda);
        binding.ibBoleto.setBackgroundResource(R.drawable.borda);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textDebito.setTextColor(getResources().getColor(R.color.preto));
        binding.textBoleto.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.grey_40));
        pagamento = "debito";
        desconto = configuracao.getDesconto_debito();
        binding.includeSheet.tvTotalCart.setText(total(pagamento, binding.swDesconto.isChecked() ? desconto : 0));

    }

    private void credito() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda);
        binding.ibDebito.setBackgroundResource(R.drawable.borda);
        binding.ibBoleto.setBackgroundResource(R.drawable.borda);
        binding.ibCredito.setBackgroundResource(R.drawable.borda_2);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textDebito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textBoleto.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.preto));
        pagamento = "credito";
        binding.includeSheet.tvTotalCart.setText(total(pagamento, 0));
        desconto = 0;
    }

    private void boleto() {
        binding.ibDinheiro.setBackgroundResource(R.drawable.borda);
        binding.ibDebito.setBackgroundResource(R.drawable.borda);
        binding.ibCredito.setBackgroundResource(R.drawable.borda);
        binding.ibBoleto.setBackgroundResource(R.drawable.borda_2);
        binding.textDinheiro.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textDebito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textCredito.setTextColor(getResources().getColor(R.color.grey_40));
        binding.textBoleto.setTextColor(getResources().getColor(R.color.preto));
        pagamento = "boleto";
        desconto = configuracao.getAcrecimo_boleto();
        binding.includeSheet.tvTotalCart.setText(total(pagamento, binding.swDesconto.isChecked() ? desconto : 0));
    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");
        binding.includeSheet.tvTotalCart.setText(total("credito", 0));
        subTotal = total("credito", 0);
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
                BigDecimal preco = Util.convertMoneEmBigDecimal(itemVendaList.get(i).getPreco_venda());
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
        binding.ibBoleto.setOnClickListener(view -> formaDePagamento(view));
        binding.includeSheet.btnContinue.setOnClickListener(view -> finalizar());
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());

        binding.swDesconto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.includeSheet.tvTotalCart.setText(total(pagamento, desconto));
            } else {
                binding.includeSheet.tvTotalCart.setText(total(pagamento, 0));
            }

        });
    }

    private void finalizar() {
        BigDecimal bDesconto = new BigDecimal("0");
        for (int i = 0; i < itemVendaList.size(); i++) {
            BigDecimal preco = Util.convertMoneEmBigDecimal(itemVendaList.get(i).getPreco_venda());
            preco = preco.divide(new BigDecimal("100"));
            bDesconto = preco.multiply(new BigDecimal(String.valueOf(desconto)).divide(new BigDecimal(100)));
            preco = preco.subtract(bDesconto);
            itemVendaList.get(i).setPreco_venda(NumberFormat.getCurrencyInstance().format(preco));
        }
        if (clienteSelecionado != null) {
            clienteSelecionado.setNome(binding.edtNome.getText().toString());
            clienteSelecionado.setTelefone1(binding.edtTelefone.getText().toString());
            venda = new Venda();
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            venda.setId(databaseReference.push().getKey());
            venda.setIdCliente(clienteSelecionado);
            if (enderecoList.size() > 0) {
                venda.setIdEndereco(enderecoList.get(end));
            }
            venda.setIdUsuario(usuario);
            venda.setData(String.valueOf(Timestamp.getUnixTimestamp()));
            venda.setDataBoleto(String.valueOf(Timestamp.getUnixTimestamp()));
            venda.setItens(itemVendaList);
            venda.setTipoPagamento(pagamento);
            venda.setStatus("Finalizada");
            venda.setDesconto(String.valueOf(desconto));
            venda.setTotal(binding.includeSheet.tvTotalCart.getText().toString());
            venda.setSubTotal(binding.includeSheet.tvTotalCart.getText().toString());
            if (pagamento.equals("boleto")) {
                venda.setBoletoPago(false);
            } else {
                venda.setBoletoPago(true);
            }
            SPM spm = new SPM(getApplicationContext());
            DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                    .child("empresas")
                    .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                    .child("vendas").child(venda.getId());
            produtoRef.setValue(venda).addOnSuccessListener(unused -> {

                baixaEstoque();


            });

        } else {
            Toast.makeText(getApplicationContext(), "Selecione um Cliente", Toast.LENGTH_SHORT).show();
        }


    }



    private void baixaEstoque() {
        for (int i = 0; i < venda.getItens().size(); i++) {
            String id = venda.getItens().get(i).getIdProduto();
            int a = i;

            DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                    .child("empresas")
                    .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                    .child("produtos").child(id).child("quantidadeEtoque");

            produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String qtd = (String) snapshot.getValue().toString();
                    qtdEstoque = Integer.parseInt(qtd);
                    qtdEstoque = qtdEstoque - venda.getItens().get(a).getQtd();
                    //Toast.makeText(getApplicationContext(), String.valueOf(qtdEstoque), Toast.LENGTH_SHORT).show();
                    produtoRef.setValue(String.valueOf(qtdEstoque)).addOnSuccessListener(unused -> {
                        if (a == venda.getItens().size() - 1) {
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), ListaVendaActivity.class);
                            intent.putExtra("venda", venda);
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    }

}

