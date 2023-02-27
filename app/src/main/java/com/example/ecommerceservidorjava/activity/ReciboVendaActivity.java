package com.example.ecommerceservidorjava.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;

import com.example.ecommerceservidorjava.databinding.ActivityReciboVendaBinding;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ReciboVendaActivity extends AppCompatActivity {
    private ActivityReciboVendaBinding binding;
    private Venda vendaSelecionada;
    private SPM spm = new SPM(this);
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    private String recibo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReciboVendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarPerfil();
        binding.imaWhatsapp.setOnClickListener(view -> enviarPDFWhatsapp());

    }


    private void recuperarPerfil() {

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));


        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("perfil_empresa");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    perfilEmpresa = snapshot.getValue(PerfilEmpresa.class);
                    recuperarConfiguracao();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    criarRecibo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void criarRecibo() {
        vendaSelecionada = (Venda) getIntent().getSerializableExtra("vendaSelecionado");
        String produtos = "";
        for (int i = 0; i < vendaSelecionada.getItens().size(); i++) {
            produtos = produtos + vendaSelecionada.getItens().get(i).getQtd() + " x " +
                    vendaSelecionada.getItens().get(i).getNome() + "    " +
                    vendaSelecionada.getItens().get(i).getPreco_venda() + "\n";

        }
        String divisao = "--------------------------------------------------------------------";

        recibo = "*VENDA*" + "\n" +
                "*" + perfilEmpresa.getNome() + "*" + "\n" +
                "CNPJ: " + perfilEmpresa.getDocumento() + "\n" +
                perfilEmpresa.getEndereco().getLogradouro() + "\n" +
                perfilEmpresa.getEndereco().getBairro() + "\n" +
                perfilEmpresa.getEndereco().getLocalidade() + "\n" +
                divisao + "\n" +
                "Cliente: " + vendaSelecionada.getIdCliente().getNome() + "\n" +
                "Telefone: " + vendaSelecionada.getIdCliente().getTelefone1() + "\n" +
                divisao + "\n" +
                produtos +
                divisao + "\n" +
                "Subtotal:   " + vendaSelecionada.getSubTotal() + "\n" +
                "Desconto:   " + vendaSelecionada.getDesconto() + "%" + "\n" +
                "Total:   " + vendaSelecionada.getTotal() + "\n"
        ;
        binding.editRecibo.setText(recibo);

    }

    //---------------------------------------------------- ENVIAR PDF WHATSAPP-----------------------------------------------------------------
    private void enviarPDFWhatsapp() {
        //binding.progressBar2.setVisibility(View.VISIBLE);


        String telefone = "55" + vendaSelecionada.getIdCliente().getTelefone1().replaceAll("\\D", "");
        Intent sendIntent = new Intent("android.intent.action.SEND");

        sendIntent.setPackage(
                "com.whatsapp");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, recibo);
        //sendIntent.putExtra(Intent.EXTRA_STREAM, recibo);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(telefone) + "@s.whatsapp.net");
        startActivity(sendIntent);
        // binding.progressBar2.setVisibility(View.GONE);


    }
}