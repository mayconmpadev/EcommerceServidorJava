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
import com.example.ecommerceservidorjava.adapter.ListaVendaAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaVendaBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoEnviarBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoStatusBinding;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.GerarPDFVendas;
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

public class ListaVendaActivity extends AppCompatActivity implements ListaVendaAdapter.OnClickLister, ListaVendaAdapter.OnLongClickLister {
    ActivityListaVendaBinding binding;
    ListaVendaAdapter vendaAdapter;
    private final List<Venda> vendaList = new ArrayList<>();
    List<Venda> filtroList = new ArrayList<>();
    SPM spm = new SPM(this);
    private AlertDialog dialog;
    private Venda venda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaVendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        recuperaVendas();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroVendaActivity.class);
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
                    configRvProdutos(vendaList);
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
            configRvProdutos(vendaList);
        });

    }

    private void recuperarIntent() {
        venda = (Venda) getIntent().getSerializableExtra("venda");
        if (venda != null) {
            enviarPDFWhatsapp();
        }
    }


    private void filtraProdutoNome(String pesquisa) {


        for (Venda venda : vendaList) {
            if (venda.getIdCliente().getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(this.venda);
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

    private void configRvProdutos(List<Venda> vendaList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        vendaAdapter = new ListaVendaAdapter(R.layout.item_lista_usuario, vendaList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(vendaAdapter);
    }

    private void recuperaVendas() {
        SPM spm = new SPM(getApplicationContext());
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("vendas");
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
        vendaList.clear();
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("vendas").orderByChild("data");
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    Venda venda = snapshot.getValue(Venda.class);
                    vendaList.add(venda);
                    binding.progressBar2.setVisibility(View.GONE);

                    configRvProdutos(vendaList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Venda venda = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(venda.getId())) {
                        vendaList.set(i, venda);
                    }
                }

                vendaAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(venda.getId())) {
                            filtroList.set(i, venda);
                        }
                    }

                    vendaAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Venda orcamento = snapshot.getValue(Venda.class);

                for (int i = 0; i < vendaList.size(); i++) {
                    if (vendaList.get(i).getId().equals(orcamento.getId())) {
                        vendaList.remove(i);
                    }
                }

                vendaAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(orcamento.getId())) {
                            filtroList.remove(i);
                        }
                    }

                    vendaAdapter.notifyDataSetChanged();
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

    private void showDialog(Venda cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogClienteOpcoesBinding dialogBinding = DialogClienteOpcoesBinding
                .inflate(LayoutInflater.from(this));


        Glide.with(getApplicationContext())
                .load(cliente.getIdCliente().getUrlImagem())
                .into(dialogBinding.imagemProduto);


        dialogBinding.btnEndereco.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaEnderecoActivity.class);
            intent.putExtra("clienteSelecionado", cliente);
            startActivity(intent);
            dialog.dismiss();
            dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
            intent.putExtra("clienteSelecionado", cliente);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
            showDialogDelete(cliente);


        });

        dialogBinding.txtNomeProduto.setText(cliente.getIdCliente().getNome());


        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void showDialogDelete(Venda venda) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaVendaActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaVendaActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + venda.getIdCliente().getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            vendaAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            vendaList.remove(venda);

            if (vendaList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(venda);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    @SuppressLint("NotifyDataSetChanged")
    public void excluir(Venda venda) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho).child("vendas").child(venda.getId());
        databaseReference.removeValue();


        binding.progressBar2.setVisibility(View.GONE);


    }

    private void alterarStatus(Venda venda, int position, String status) {
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
                vendaList.get(position).setStatus(status);
            }


            vendaAdapter.notifyItemChanged(position);
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
        String telefone = "55" + venda.getIdCliente().getTelefone1().replaceAll("\\D", "");
        Intent sendIntent = new Intent("android.intent.action.SEND");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
        boolean tipowhts = whatsappIntelado("com.whatsapp");
        if (tipowhts){
            sendIntent.setPackage("com.whatsapp");
        }else {
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
    private boolean whatsappIntelado(String s){
        PackageManager pm = getPackageManager();
        boolean app_intalado = false;
        try{
            pm.getPackageInfo(s, PackageManager.GET_ACTIVITIES);
            app_intalado = true;
        }catch (PackageManager.NameNotFoundException e){
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
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{venda.getIdCliente().getEmail()});

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
            String telefone = "55" + venda.getIdCliente().getTelefone1().replaceAll("\\D", "");
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
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{venda.getIdCliente().getEmail()});

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

    private void showDialog(Venda cliente, int position) {
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
            showDialogStatus(venda, position);

        });

        dialogBinding.llPdf.setOnClickListener(view -> {
            exibirPDF();
            dialog.dismiss();

        });

        dialogBinding.llRecibo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReciboVendaActivity.class);
            intent.putExtra("vendaSelecionado", venda);
            startActivity(intent);
            dialog.dismiss();

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogStatus(Venda venda, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoStatusBinding dialogBinding = DialogOpcaoStatusBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llAnalise.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(venda, position, "Em analise");
        });

        dialogBinding.llAprovado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(venda, position, "Aprovado");
        });

        dialogBinding.llRecusado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(venda, position, "Recusado");

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
    public void onClick(Venda usuario, int position) {
        venda = usuario;

        showDialog(usuario, position);
        GerarPDFVendas gerarPDFOrcamento = new GerarPDFVendas(venda, this);
        // while (Parametro.bPdf){
        // Toast.makeText(getApplicationContext(), "teste", Toast.LENGTH_SHORT).show();
        // break;
        //   }


    }


    @Override
    public void onLongClick(Venda venda) {
        showDialogDelete(venda);
    }
}