package com.example.ecommerceservidorjava.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaBoletoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaBoletoBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoEnviarBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoPagamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoStatusVendasBinding;
import com.example.ecommerceservidorjava.model.Boleto;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaBoletoActivity extends AppCompatActivity implements ListaBoletoAdapter.OnClickLister, ListaBoletoAdapter.OnLongClickLister {
    ActivityListaBoletoBinding binding;
    ListaBoletoAdapter boletoAdapter;
    private final List<Boleto> boletoList = new ArrayList<>();
    List<Boleto> filtroList = new ArrayList<>();
    SPM spm = new SPM(this);
    private AlertDialog dialog;
    private Boleto boleto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaBoletoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        recuperaVendas();
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
                    configRvProdutos(boletoList);
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
            configRvProdutos(boletoList);
        });

    }

    private void recuperarIntent() {
        boleto = (Boleto) getIntent().getSerializableExtra("venda");
        if (boleto != null) {
            enviarPDFWhatsapp();
        }
    }


    private void filtraProdutoNome(String pesquisa) {


        for (Boleto boleto : boletoList) {
            if (boleto.getIdVenda().getIdCliente().getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(boleto);
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

    private void filtraProdutoPagamento(String pesquisa) {
        filtroList.clear();

        for (Boleto boleto : boletoList) {
            if (boleto.getIdVenda().getTipoPagamento().equals(pesquisa)) {
                filtroList.add(boleto);
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

    private void configRvProdutos(List<Boleto> vendaList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        boletoAdapter = new ListaBoletoAdapter(R.layout.item_lista_usuario, vendaList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(boletoAdapter);
    }

    private void recuperaVendas() {
        SPM spm = new SPM(getApplicationContext());
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("boletos");
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
        boletoList.clear();
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("boletos").orderByChild("data");
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    Boleto boleto = snapshot.getValue(Boleto.class);
                    boletoList.add(boleto);
                    binding.progressBar2.setVisibility(View.GONE);

                    configRvProdutos(boletoList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Boleto boleto = snapshot.getValue(Boleto.class);

                for (int i = 0; i < boletoList.size(); i++) {
                    if (boletoList.get(i).getId().equals(boleto.getId())) {
                        boletoList.set(i, boleto);
                    }
                }

                boletoAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(boleto.getId())) {
                            filtroList.set(i, boleto);
                        }
                    }

                    boletoAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Boleto orcamento = snapshot.getValue(Boleto.class);

                for (int i = 0; i < boletoList.size(); i++) {
                    if (boletoList.get(i).getId().equals(orcamento.getId())) {
                        boletoList.remove(i);
                    }
                }

                boletoAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(orcamento.getId())) {
                            filtroList.remove(i);
                        }
                    }

                    boletoAdapter.notifyDataSetChanged();
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

    private void showDialog(Boleto boleto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogClienteOpcoesBinding dialogBinding = DialogClienteOpcoesBinding
                .inflate(LayoutInflater.from(this));


        Glide.with(getApplicationContext())
                .load(boleto.getIdVenda().getIdCliente().getUrlImagem())
                .into(dialogBinding.imagemProduto);


        dialogBinding.btnEndereco.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaEnderecoActivity.class);
            intent.putExtra("clienteSelecionado", boleto);
            startActivity(intent);
            dialog.dismiss();
            dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
            intent.putExtra("clienteSelecionado", boleto);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
            showDialogDelete(boleto);


        });

        dialogBinding.txtNomeProduto.setText(boleto.getIdVenda().getIdCliente().getNome());


        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void showDialogDelete(Boleto boleto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaBoletoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaBoletoActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + boleto.getIdVenda().getIdCliente().getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            boletoAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            boletoList.remove(boleto);

            if (boletoList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(boleto);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    @SuppressLint("NotifyDataSetChanged")
    public void excluir(Boleto boleto) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho).child("vendas").child(boleto.getId());
        databaseReference.removeValue();


        binding.progressBar2.setVisibility(View.GONE);


    }

    private void alterarStatus(Boleto venda, int position, String status) {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("vendas")
                .child(venda.getId()).child("status");
        databaseReference.setValue(status).addOnSuccessListener(unused -> {
            if (filtroList.size() > 0) {
                filtroList.get(position).setStatus(status);
            } else {
                boletoList.get(position).setStatus(status);
            }


            boletoAdapter.notifyItemChanged(position);
        });

    }

    //---------------------------------------------------- ENVIAR PDF WHATSAPP-----------------------------------------------------------------
    private void enviarPDFWhatsapp() {
        binding.progressBar2.setVisibility(View.VISIBLE);

        File pdfFolder = new File(getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/vendas"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "venda" + ".pdf");
        String telefone = "55" + boleto.getIdVenda().getIdCliente().getTelefone1().replaceAll("\\D", "");
        Intent sendIntent = new Intent("android.intent.action.SEND");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
        boolean tipowhts = whatsappIntelado("com.whatsapp");
        if (tipowhts) {
            sendIntent.setPackage("com.whatsapp");
        } else {
            sendIntent.setPackage("com.whatsapp.w4b");
        }

        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("application/pdf");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "sample text you want to send along with the image");
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(telefone) + "@s.whatsapp.net");

        startActivity(sendIntent);
        binding.progressBar2.setVisibility(View.GONE);


    }

    private boolean whatsappIntelado(String s) {
        PackageManager pm = getPackageManager();
        boolean app_intalado = false;
        try {
            pm.getPackageInfo(s, PackageManager.GET_ACTIVITIES);
            app_intalado = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_intalado = false;
        }
        return app_intalado;
    }

    private void enviarPDFEmeail() {
        try {
            File pdfFolder = new File(getExternalFilesDir(null)
                    + File.separator
                    + "ecommercempa/vendas"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "venda" + ".pdf");
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
            email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{boleto.getIdVenda().getIdCliente().getEmail()});

            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setData(Uri.parse("mailto:")); // only email apps should handle this


            List<ResolveInfo> resInfoList = getApplication().getPackageManager().queryIntentActivities(email, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getApplication().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            email.addFlags(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
            email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            email.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


            email.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(email);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        binding.progressBar2.setVisibility(View.GONE);
    }

    private void exibirPDF() {

        File pdfFolder = new File(getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/vendas"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "venda" + ".pdf");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
        binding.progressBar2.setVisibility(View.GONE);
    }

    BroadcastReceiver onCompleteVisualizar = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {


        }
    };

    BroadcastReceiver onCompleteWhatsApp = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            File pdfFolder = new File(getExternalFilesDir(null)
                    + File.separator
                    + "ecommercempa/vendas"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
            String telefone = "55" + boleto.getIdVenda().getIdCliente().getTelefone1().replaceAll("\\D", "");
            Intent sendIntent = new Intent("android.intent.action.SEND");
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker"));
            sendIntent.setType("application/pdf");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "sample text you want to send along with the image");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(telefone) + "@s.whatsapp.net");

            startActivity(sendIntent);
            binding.progressBar2.setVisibility(View.GONE);
        }
    };

    BroadcastReceiver onCompleteEmail = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            try {

                File pdfFolder = new File(getExternalFilesDir(null)
                        + File.separator
                        + "mpasistema/vendas"
                        + File.separator);
                if (!pdfFolder.exists()) {
                    pdfFolder.mkdirs();
                }
                File myFile = new File(pdfFolder + File.separator + "venda" + ".pdf");
                Intent email = new Intent(Intent.ACTION_SENDTO);
                email.setType("message/rfc822");
                email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
                email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{boleto.getIdVenda().getIdCliente().getEmail()});

                Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
                email.putExtra(Intent.EXTRA_STREAM, uri);
                email.setData(Uri.parse("mailto:")); // only email apps should handle this


                List<ResolveInfo> resInfoList = getApplication().getPackageManager().queryIntentActivities(email, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getApplication().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                email.addFlags(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
                email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                email.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


                email.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(email);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            binding.progressBar2.setVisibility(View.GONE);

        }
    };

    private void showDialog(Boleto boleto, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoOrcamentoBinding dialogBinding = DialogOpcaoOrcamentoBinding
                .inflate(LayoutInflater.from(this));

        if (!boleto.getIdVenda().getTipoPagamento().equals("boleto")) {
            dialogBinding.llEditar.setVisibility(View.GONE);
        }
        dialogBinding.llEnviar.setOnClickListener(view -> {
            dialog.dismiss();
            showDialogEnviar();

        });

        dialogBinding.llEditar.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BoletoActivity.class);
            intent.putExtra("boletoSelecionado", boleto);
            startActivity(intent);

        });

        dialogBinding.llClonar.setOnClickListener(view -> {
            dialog.dismiss();

        });

        dialogBinding.llStatus.setOnClickListener(view -> {

            dialog.dismiss();
            showDialogStatus(boleto, position);

        });

        dialogBinding.llPdf.setOnClickListener(view -> {
            exibirPDF();
            dialog.dismiss();

        });

        dialogBinding.llRecibo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReciboVendaActivity.class);
            intent.putExtra("vendaSelecionado", boleto);
            startActivity(intent);
            dialog.dismiss();

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogStatus(Boleto boleto, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoStatusVendasBinding dialogBinding = DialogOpcaoStatusVendasBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llRetirada.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(boleto, position, "Aguardando retirada");
        });

        dialogBinding.llFinalizada.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(boleto, position, "Finalizada");
        });

        dialogBinding.llCancelada.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(boleto, position, "Cancelada");

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogPagamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoPagamentoBinding dialogBinding = DialogOpcaoPagamentoBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llDinheiro.setOnClickListener(view -> {
            dialog.dismiss();
            filtraProdutoPagamento("dinheiro");
        });

        dialogBinding.llDebito.setOnClickListener(view -> {
            dialog.dismiss();
            filtraProdutoPagamento("debito");
        });

        dialogBinding.llCredito.setOnClickListener(view -> {
            dialog.dismiss();
            filtraProdutoPagamento("credito");

        });

        dialogBinding.llBoleto.setOnClickListener(view -> {
            dialog.dismiss();
            filtraProdutoPagamento("boleto");

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
            enviarPDFWhatsapp();
            dialog.dismiss();
        });

        dialogBinding.llEmail.setOnClickListener(view -> {
            enviarPDFEmeail();
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
                    ArrayList<Produto> arrayList = new ArrayList<Produto>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Produto produto = ds.getValue(Produto.class);

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
    public void onClick(Boleto boleto, int position) {
        this.boleto = boleto;

        showDialog(boleto, position);
        //GerarPDFVendas gerarPDFOrcamento = new GerarPDFVendas(this.boleto, this);



    }


    @Override
    public void onLongClick(Boleto boleto) {
        showDialogDelete(boleto);
    }
}