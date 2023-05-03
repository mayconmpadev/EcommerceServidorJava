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
import com.example.ecommerceservidorjava.adapter.ListaOrdemServicoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaOrdemServicoBinding;
import com.example.ecommerceservidorjava.databinding.DialogClienteOpcoesBinding;
import com.example.ecommerceservidorjava.databinding.DialogDeleteBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoEnviarBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoOrdemServicoBinding;
import com.example.ecommerceservidorjava.databinding.DialogOpcaoStatusOrdemServicoBinding;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.GerarPDFOSFinalizada;
import com.example.ecommerceservidorjava.util.GerarPDFOrdenServico;
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

public class ListaOrdemServicoActivity extends AppCompatActivity implements ListaOrdemServicoAdapter.OnClickLister, ListaOrdemServicoAdapter.OnLongClickLister {
    ActivityListaOrdemServicoBinding binding;
    ListaOrdemServicoAdapter ordemServicoAdapter;
    private final List<OrdemServico> ordemServicoList = new ArrayList<>();
    List<OrdemServico> filtroList = new ArrayList<>();
    SPM spm = new SPM(this);
    private AlertDialog dialog;
    private OrdemServico ordemServico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaOrdemServicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configSearchView();
        recuperaOrcamento();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroOrdemServicoActivity.class);
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
                    configRvProdutos(ordemServicoList);
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
            configRvProdutos(ordemServicoList);
        });

    }

    private void recuperarIntent() {
        ordemServico = (OrdemServico) getIntent().getSerializableExtra("venda");
        if (ordemServico != null) {
            enviarPDFWhatsapp();
        }
    }


    private void filtraProdutoNome(String pesquisa) {


        for (OrdemServico ordemServico : ordemServicoList) {
            if (ordemServico.getIdCliente().getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(ordemServico);
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

    private void configRvProdutos(List<OrdemServico> vendaList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        ordemServicoAdapter = new ListaOrdemServicoAdapter(R.layout.item_lista_usuario, vendaList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(ordemServicoAdapter);
    }

    private void recuperaOrcamento() {
        SPM spm = new SPM(getApplicationContext());
        DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos");
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
        ordemServicoList.clear();
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos").orderByChild("data");
        produtoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    OrdemServico cliente = snapshot.getValue(OrdemServico.class);
                    ordemServicoList.add(cliente);
                    binding.progressBar2.setVisibility(View.GONE);

                    configRvProdutos(ordemServicoList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OrdemServico orcamento = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(orcamento.getId())) {
                        ordemServicoList.set(i, orcamento);
                    }
                }

                ordemServicoAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(orcamento.getId())) {
                            filtroList.set(i, orcamento);
                        }
                    }

                    ordemServicoAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                OrdemServico ordemServico = snapshot.getValue(OrdemServico.class);

                for (int i = 0; i < ordemServicoList.size(); i++) {
                    if (ordemServicoList.get(i).getId().equals(ordemServico.getId())) {
                        ordemServicoList.remove(i);
                    }
                }

                ordemServicoAdapter.notifyDataSetChanged();
                if (!filtroList.isEmpty()) {
                    for (int i = 0; i < filtroList.size(); i++) {
                        if (filtroList.get(i).getId().equals(ordemServico.getId())) {
                            filtroList.remove(i);
                        }
                    }

                    ordemServicoAdapter.notifyDataSetChanged();
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

    private void showDialog(OrdemServico ordemServico) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogClienteOpcoesBinding dialogBinding = DialogClienteOpcoesBinding
                .inflate(LayoutInflater.from(this));


        Glide.with(getApplicationContext())
                .load(ordemServico.getIdCliente().getUrlImagem())
                .into(dialogBinding.imagemProduto);


        dialogBinding.btnEndereco.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaEnderecoActivity.class);
            intent.putExtra("clienteSelecionado", ordemServico);
            startActivity(intent);
            dialog.dismiss();
            dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
        });

        dialogBinding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
            intent.putExtra("clienteSelecionado", ordemServico);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogBinding.btnRemover.setOnClickListener(v -> {

            dialog.dismiss();
            showDialogDelete(ordemServico);


        });

        dialogBinding.txtNomeProduto.setText(ordemServico.getIdCliente().getNome());


        builder.setView(dialogBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void showDialogDelete(OrdemServico ordemServico) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ListaOrdemServicoActivity.this, R.style.CustomAlertDialog2);

        DialogDeleteBinding deleteBinding = DialogDeleteBinding
                .inflate(LayoutInflater.from(ListaOrdemServicoActivity.this));
        deleteBinding.textTitulo.setText("Deseja remover o produto " + ordemServico.getIdCliente().getNome() + "?");
        deleteBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
            ordemServicoAdapter.notifyDataSetChanged();
        });

        deleteBinding.btnSim.setOnClickListener(v -> {
            ordemServicoList.remove(ordemServico);

            if (ordemServicoList.isEmpty()) {
                binding.textVazio.setText("Sua lista esta vazia.");
            } else {
                binding.textVazio.setText("");
            }
            dialog.dismiss();
            excluir(ordemServico);

        });

        builder.setView(deleteBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
    }

    @SuppressLint("NotifyDataSetChanged")
    public void excluir(OrdemServico ordemServico) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho).child("ordens_servicos").child(ordemServico.getId());
        databaseReference.removeValue();


        binding.progressBar2.setVisibility(View.GONE);


    }

    private void alterarStatus(OrdemServico ordemServico, int position, String status) {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos")
                .child(ordemServico.getId()).child("status");
        databaseReference.setValue(status).addOnSuccessListener(unused -> {
            if (filtroList.size() > 0) {
                filtroList.get(position).setStatus(status);
            } else {
                ordemServicoList.get(position).setStatus(status);
            }


            ordemServicoAdapter.notifyItemChanged(position);
        });

    }

    private void entregue(OrdemServico ordemServico, int position, boolean status) {
        SPM spm = new SPM(getApplicationContext());
        String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("ordens_servicos")
                .child(ordemServico.getId()).child("entregue");
        databaseReference.setValue(status).addOnSuccessListener(unused -> {
            if (filtroList.size() > 0) {
                filtroList.get(position).setEntregue(status);
            } else {
                ordemServicoList.get(position).setEntregue(status);
            }


            ordemServicoAdapter.notifyItemChanged(position);
        });

    }

    //---------------------------------------------------- ENVIAR PDF WHATSAPP-----------------------------------------------------------------
    private void enviarPDFWhatsapp() {
        binding.progressBar2.setVisibility(View.VISIBLE);

        File pdfFolder = new File(getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/ordemServico"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "ordemServico" + ".pdf");
        String telefone = "55" + ordemServico.getIdCliente().getTelefone1().replaceAll("\\D", "");
        Intent sendIntent = new Intent("android.intent.action.SEND");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile);
        if(isAppInstalled("com.whatsapp")) {
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

    private void enviarPDFEmeail() {
        try {
            File pdfFolder = new File(getExternalFilesDir(null)
                    + File.separator
                    + "ecommercempa/ordemServico"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "ordemServico" + ".pdf");
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
            email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ordemServico.getIdCliente().getEmail()});

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
                + "ecommercempa/ordemServico"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File myFile = new File(pdfFolder + File.separator + "ordemServico" + ".pdf");
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
                    + "ecommercempa/ordens_servicos"
                    + File.separator);
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }
            File myFile = new File(pdfFolder + File.separator + "ordemServico" + ".pdf");
            String telefone = "55" + ordemServico.getIdCliente().getTelefone1().replaceAll("\\D", "");
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
                        + "mpasistema/ordens_servicos"
                        + File.separator);
                if (!pdfFolder.exists()) {
                    pdfFolder.mkdirs();
                }
                File myFile = new File(pdfFolder + File.separator + "venda" + ".pdf");
                Intent email = new Intent(Intent.ACTION_SENDTO);
                email.setType("message/rfc822");
                email.putExtra(Intent.EXTRA_SUBJECT, "Orcamento em anexo");
                email.putExtra(Intent.EXTRA_TEXT, "obrigado pela preferencia");
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ordemServico.getIdCliente().getEmail()});

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

    private void showDialog(OrdemServico ordemServico, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoOrdemServicoBinding dialogBinding = DialogOpcaoOrdemServicoBinding
                .inflate(LayoutInflater.from(this));


        if (ordemServico.getStatus().equals("Em analise")) {
            dialogBinding.llStatus.setVisibility(View.GONE);
        } else {
            dialogBinding.llStatus.setVisibility(View.VISIBLE);
        }

        dialogBinding.llEnviar.setOnClickListener(view -> {
            dialog.dismiss();
            showDialogEnviar();

        });

        dialogBinding.llEditar.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroOrdemServicoActivity.class);
            intent.putExtra("ordemServiçoSelecionada", ordemServico);
            startActivity(intent);
            dialog.dismiss();

        });

        dialogBinding.llEntregue.setOnClickListener(view -> {
            if (ordemServico.isEntregue()){
                entregue(ordemServico, position, false);
            }else {
                entregue(ordemServico, position, true);
            }

            dialog.dismiss();


        });
        dialogBinding.llStatus.setOnClickListener(view -> {

            dialog.dismiss();
            showDialogStatus(ordemServico, position);

        });

        dialogBinding.llOrcar.setOnClickListener(view -> {

            dialog.dismiss();
            Intent intent = new Intent(this, OrcarActivity.class);
            intent.putExtra("ordemServiçoSelecionada", ordemServico);
            startActivity(intent);

        });

        dialogBinding.llPdf.setOnClickListener(view -> {
            dialog.dismiss();
            if (ordemServico.getStatus().equals("Em analise")){
                GerarPDFOrdenServico gerarPDFOrcamento = new GerarPDFOrdenServico(ordemServico, this);
                exibirPDF();
            }else {
                GerarPDFOSFinalizada gerarPDFOSFinalizada = new GerarPDFOSFinalizada(ordemServico, this);
                exibirPDF();
            }


        });

        dialogBinding.llRecibo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReciboOrcamentoActivity.class);
            intent.putExtra("ordemServicoSelecionado", ordemServico);
            startActivity(intent);
            dialog.dismiss();

        });

        dialogBinding.llDeletar.setOnClickListener(view -> {
            exibirPDF();
            dialog.dismiss();

        });


        builder.setView(dialogBinding.getRoot());
        dialog = builder.create();
        dialog.show();
    }

    private void showDialogStatus(OrdemServico ordemServico, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        DialogOpcaoStatusOrdemServicoBinding dialogBinding = DialogOpcaoStatusOrdemServicoBinding
                .inflate(LayoutInflater.from(this));


        dialogBinding.llAnalise.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(ordemServico, position, "Em analise");
        });

        dialogBinding.llAnalise.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(ordemServico, position, "Em analise");
        });

        dialogBinding.llAprovado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(ordemServico, position, "Aprovado");
        });

        dialogBinding.llRecusado.setOnClickListener(view -> {
            dialog.dismiss();
            alterarStatus(ordemServico, position, "Recusado");

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
            if (ordemServico != null) {
                if(isAppInstalled("com.whatsapp") || isAppInstalled("com.whatsapp.w4b")) {
                    enviarPDFWhatsapp();
                } else {
                    Toast.makeText(this, "Instale o whatsapp!!", Toast.LENGTH_SHORT).show();
                }

            }
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

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
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
                    ArrayList<OrdemServico> arrayList = new ArrayList<OrdemServico>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        OrdemServico ordemServico = ds.getValue(OrdemServico.class);

                        arrayList.add(ordemServico);

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
    public void onClick(OrdemServico ordemServico, int position) {
        this.ordemServico = ordemServico;

        showDialog(ordemServico, position);
        //GerarPDFVendas gerarPDFOrcamento = new GerarPDFVendas(this.ordemServico, this);
        // while (Parametro.bPdf){
        // Toast.makeText(getApplicationContext(), "teste", Toast.LENGTH_SHORT).show();
        // break;
        //   }


    }


    @Override
    public void onLongClick(OrdemServico ordemServico) {
        // showDialogDelete(ordemServico);

    }
}