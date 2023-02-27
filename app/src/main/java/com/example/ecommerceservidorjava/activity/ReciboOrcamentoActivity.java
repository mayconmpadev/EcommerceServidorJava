package com.example.ecommerceservidorjava.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

import com.example.ecommerceservidorjava.databinding.ActivityReciboOrcamentoBinding;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ReciboOrcamentoActivity extends AppCompatActivity {
    private ActivityReciboOrcamentoBinding binding;
    private Orcamento orcamentoselecionado;
    private Venda vendaSelecionada;
    private OrdemServico ordemServicoSelecionada;
    private SPM spm = new SPM(this);
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    private String recibo;
    private String telefone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReciboOrcamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarPerfil();
        binding.imaWhatsapp.setOnClickListener(view -> {

            if (isAppInstalled("com.whatsapp") || isAppInstalled("com.whatsapp.w4b")) {
                enviarPDFWhatsapp();
            } else {
                Toast.makeText(this, "Instale o whatsapp!!", Toast.LENGTH_SHORT).show();
            }
        });

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
                    tipoRecibo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void criarReciboOrcamento() {

        String produtos = "";
        for (int i = 0; i < orcamentoselecionado.getItens().size(); i++) {
            produtos = produtos + orcamentoselecionado.getItens().get(i).getQtd() + " x " +
                    orcamentoselecionado.getItens().get(i).getNome() + "    " +
                    orcamentoselecionado.getItens().get(i).getPreco_venda() + "\n";

        }
        String divisao = "--------------------------------------------------------------------";


        recibo = "*ORÇAMENTO*" + "\n" +
                "*" + perfilEmpresa.getNome() + "*" + "\n" +
                "CNPJ: " + perfilEmpresa.getDocumento() + "\n" +
                perfilEmpresa.getEndereco().getLogradouro() + "\n" +
                perfilEmpresa.getEndereco().getBairro() + "\n" +
                perfilEmpresa.getEndereco().getLocalidade() + "\n" +
                divisao + "\n" +
                "Cliente: " + orcamentoselecionado.getIdCliente().getNome() + "\n" +
                "Telefone: " + orcamentoselecionado.getIdCliente().getTelefone1() + "\n" +
                "Data: " + Timestamp.getFormatedDateTime(Long.parseLong(orcamentoselecionado.getData()), "dd/MM/yy") + "   "
                + Timestamp.getFormatedDateTime(Long.parseLong(orcamentoselecionado.getData()), "HH:mm") +

                divisao + "\n" +
                produtos +
                divisao + "\n" +
                "Subtotal:   " + orcamentoselecionado.getSubTotal() + "\n" +
                "Desconto:   " + orcamentoselecionado.getDesconto() + "%" + "\n" +
                "Total:   " + orcamentoselecionado.getTotal() + "\n"
        ;
        binding.editRecibo.setText(recibo);

    }

    private void criarReciboVendas() {

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
                "Data: " + Timestamp.getFormatedDateTime(Long.parseLong(vendaSelecionada.getData()), "dd/MM/yy") + "   "
                + Timestamp.getFormatedDateTime(Long.parseLong(vendaSelecionada.getData()), "HH:mm") +

                divisao + "\n" +
                produtos +
                divisao + "\n" +
                "Subtotal:   " + vendaSelecionada.getSubTotal() + "\n" +
                "Desconto:   " + vendaSelecionada.getDesconto() + "%" + "\n" +
                "Total:   " + vendaSelecionada.getTotal() + "\n"
        ;
        binding.editRecibo.setText(recibo);

    }

    private void criarReciboOrdemEntrada() {

        String divisao = "----------------------------------------------------------------";
        String garantia = "";
        if (ordemServicoSelecionada.isGarantia()) {
            garantia = "sim";
        } else {
            garantia = "não";
        }
        recibo = "*ORDEM DE SERVIÇO:  " + ordemServicoSelecionada.getNumeroOs() +  "*" + "\n" +
                "*" + perfilEmpresa.getNome() + "*" + "\n" +
                "CNPJ: " + perfilEmpresa.getDocumento() + "\n" +
                perfilEmpresa.getEndereco().getLogradouro() + "\n" +
                perfilEmpresa.getEndereco().getBairro() + "\n" +
                perfilEmpresa.getEndereco().getLocalidade() + "\n" +
                divisao + "\n" +
                "Cliente: " + ordemServicoSelecionada.getIdCliente().getNome() + "\n" +
                "Telefone: " + ordemServicoSelecionada.getIdCliente().getTelefone1() + "\n" +
                "Data: " + Timestamp.getFormatedDateTime(Long.parseLong(ordemServicoSelecionada.getDataEntrada()), "dd/MM/yy") +
                " " + Timestamp.getFormatedDateTime(Long.parseLong(ordemServicoSelecionada.getDataEntrada()), "HH:mm") + "\n" +
                divisao + "\n" +
                "Equipamento: " + ordemServicoSelecionada.getEquipamento() + "\n" +
                "Marca: " + ordemServicoSelecionada.getMarca() + "  Modelo: " + ordemServicoSelecionada.getModelo() + "\n" +
                divisao + "\n" +
                "Garantia: " + garantia + "\n";
        binding.editRecibo.setText(recibo);

    }

    private void tipoRecibo() {
        orcamentoselecionado = (Orcamento) getIntent().getSerializableExtra("orcamentoSelecionado");
        vendaSelecionada = (Venda) getIntent().getSerializableExtra("vendaSelecionado");
        ordemServicoSelecionada = (OrdemServico) getIntent().getSerializableExtra("ordemServicoSelecionado");

        if (orcamentoselecionado != null) {
            criarReciboOrcamento();
            telefone = "55" + orcamentoselecionado.getIdCliente().getTelefone1().replaceAll("\\D", "");
        } else if (vendaSelecionada != null) {
            criarReciboVendas();
            telefone = "55" + vendaSelecionada.getIdCliente().getTelefone1().replaceAll("\\D", "");
        } else {
            criarReciboOrdemEntrada();
            telefone = "55" + ordemServicoSelecionada.getIdCliente().getTelefone1().replaceAll("\\D", "");
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    //---------------------------------------------------- ENVIAR PDF WHATSAPP-----------------------------------------------------------------
    private void enviarPDFWhatsapp() {

        Intent sendIntent = new Intent("android.intent.action.SEND");
        if (isAppInstalled("com.whatsapp")) {
            sendIntent.setPackage("com.whatsapp");
        } else {
            sendIntent.setPackage("com.whatsapp.w4b");
        }
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, recibo);
        //sendIntent.putExtra(Intent.EXTRA_STREAM, recibo);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(telefone) + "@s.whatsapp.net");
        startActivity(sendIntent);

    }
}