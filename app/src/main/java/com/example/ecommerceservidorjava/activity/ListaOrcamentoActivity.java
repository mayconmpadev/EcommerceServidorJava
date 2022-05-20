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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaOrcamentoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoStatusBinding;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.GerarPDF;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaOrcamentoActivity extends AppCompatActivity implements ListaOrcamentoAdapter.OnClickLister, ListaOrcamentoAdapter.OnLongClickLister {
    ActivityListaOrcamentoBinding binding;
    ListaOrcamentoAdapter orcamentoAdapter;
    private final List<Orcamento> orcamentoList = new ArrayList<>();
    List<Orcamento> filtroList = new ArrayList<>();
    SPM spm = new SPM(this);
    private AlertDialog dialog;
    private Orcamento orcamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaOrcamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        recuperaOrcamento();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroOrcamentoActivity.class);
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
                return false;
            }
        });

        binding.searchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {
            EditText edtSerachView = binding.searchView.findViewById(R.id.search_src_text);
            binding.textVazio.setVisibility(View.GONE);
            edtSerachView.setText("");
            edtSerachView.clearFocus();
            ocultaTeclado();
            filtroList.clear();
            configRvProdutos(orcamentoList);
        });

    }

    private void recuperarIntent() {
        orcamento = (Orcamento) getIntent().getSerializableExtra("orcamento");
        if (orcamento != null) {
            enviarPDFWhatsapp();
        }
    }


    private void filtraProdutoNome(String pesquisa) {


        for (Orcamento orcamento : orcamentoList) {
            if (orcamento.getIdCliente().getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(orcamento);
            }
        }


        configRvProdutos(filtroList);


        configRvProdutos(filtroList);

        if (filtroList.isEmpty()) {
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum produto encontrado.");
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Orcamento> orcamentoList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        orcamentoAdapter = new ListaOrcamentoAdapter(R.layout.item_lista_usuario, orcamentoList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(orcamentoAdapter);
    }


    private void recuperaOrcamento() {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""))).child("orcamentos").child(user).orderByChild("nome");
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                orcamentoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Orcamento cliente = ds.getValue(Orcamento.class);
                        orcamentoList.add(cliente);
                        binding.progressBar2.setVisibility(View.GONE);
                    }
                    configRvProdutos(orcamentoList);
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

    private void showDialog(Orcamento cliente) {
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

    private void showDialogDelete(Orcamento cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaOrcamentoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaOrcamentoActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + cliente.getIdCliente().getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            orcamentoAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            orcamentoList.remove(cliente);

            if (orcamentoList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(cliente);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    @SuppressLint("NotifyDataSetChanged")
    public void excluir(Orcamento cliente) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho);


        StorageReference storageReference = FirebaseHelper.getStorageReference()
                .child("empresas")
                .child(caminho)
                .child("imagens")
                .child("clientes")
                .child(cliente.getId());

        storageReference.delete().addOnSuccessListener(unused -> {

            binding.progressBar2.setVisibility(View.GONE);
            databaseReference.child("enderecos").child(cliente.getId()).removeValue().addOnSuccessListener(unused1 -> {

                databaseReference.child("clientes").child(cliente.getId()).removeValue().addOnSuccessListener(unused2 -> {
                    orcamentoAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Excluido com sucesso!", Toast.LENGTH_SHORT).show();
                });
            });

        });

    }

    private void alterarStatus(Orcamento orcamento,int position, String status) {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("orcamentos")
                .child(user)
                .child(orcamento.getId()).child("status");
        databaseReference.setValue(status).addOnSuccessListener(unused -> {
            orcamentoList.get(position).setStatus(status);
            orcamentoAdapter.notifyItemChanged(position);
        });

    }

    //---------------------------------------------------- ENVIAR PDF WHATSAPP-----------------------------------------------------------------
    private void enviarPDFWhatsapp() {
        binding.progressBar2.setVisibility(View.VISIBLE);

        File pdfFolder = new File(getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/orcamentos"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
        String telefone = "55" + orcamento.getIdCliente().getTelefone1().replaceAll("\\D", "");
        Intent sendIntent = new Intent("android.intent.action.SEND");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
        sendIntent.setPackage(
                "com.whatsapp");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("application/pdf");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "sample text you want to send along with the image");
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(telefone) + "@s.whatsapp.net");

        startActivity(sendIntent);
        binding.progressBar2.setVisibility(View.GONE);


    }

    private void enviarPDFEmeail() {
        try {
            File pdfFolder = new File(getExternalFilesDir(null)
                    + File.separator
                    + "ecommercempa/orcamentos"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
            email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{orcamento.getIdCliente().getEmail()});

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
                + "ecommercempa/orcamentos"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
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
                    + "ecommercempa/orcamentos"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
            String telefone = "55" + orcamento.getIdCliente().getTelefone1().replaceAll("\\D", "");
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
                        + "mpasistema/orcamentos"
                        + File.separator);
                if (!pdfFolder.exists()) {
                    pdfFolder.mkdirs();
                }
                File myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");
                Intent email = new Intent(Intent.ACTION_SENDTO);
                email.setType("message/rfc822");
                email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
                email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{orcamento.getIdCliente().getEmail()});

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

    private void showDialog(Orcamento cliente, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoOrcamentoBinding dialogBinding = DialogOpcaoOrcamentoBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llEnviar.setOnClickListener(view -> {
            enviarPDFWhatsapp();
            dialog.dismiss();

        });

        dialogBinding.llEditar.setOnClickListener(view -> {
            enviarPDFEmeail();
            dialog.dismiss();

        });

        dialogBinding.llClonar.setOnClickListener(view -> {
            dialog.dismiss();

        });

        dialogBinding.llStatus.setOnClickListener(view -> {

            dialog.dismiss();
            showDialogStatus(orcamento, position);

        });

        dialogBinding.llPdf.setOnClickListener(view -> {
            exibirPDF();
            dialog.dismiss();

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogStatus(Orcamento orcamento, int  position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoStatusBinding dialogBinding = DialogOpcaoStatusBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llAnalise.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(orcamento, position, "Em analise");
        });

        dialogBinding.llAprovado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(orcamento, position,"Aprovado");
        });

        dialogBinding.llRecusado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(orcamento, position,"Recusado");

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
    public void onClick(Orcamento usuario, int position) {
        orcamento = usuario;

        showDialog(usuario, position);
        GerarPDF gerarPDF = new GerarPDF(orcamento, this);
        // while (Parametro.bPdf){
        // Toast.makeText(getApplicationContext(), "teste", Toast.LENGTH_SHORT).show();
        // break;
        //   }


    }


    @Override
    public void onLongClick(Orcamento usuario) {

    }
}