package com.example.ecommerceservidorjava.activity;

import static com.itextpdf.text.Rectangle.NO_BORDER;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

import com.example.ecommerceservidorjava.databinding.ActivityCheckoutOrcamentoBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.model.Usuario;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

public class CheckoutOrcamentoActivity extends AppCompatActivity {
    private ActivityCheckoutOrcamentoBinding binding;
    private Orcamento orcamento;
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    private Usuario usuario;
    private SPM spm = new SPM(this);
    private ArrayList<ItemVenda> itemVendaList;
    private ArrayList<Endereco> enderecoList = new ArrayList<>();
    private int quantidade;
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
        binding = ActivityCheckoutOrcamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.includeSheet.btnContinue.setText("finalizar");
        binding.include.textTitulo.setText("Checkout");
        recuperarUsuario();
        recuperarPerfil();
        recuperarIntent();
        recuperarConfiguracao();
        configClicks();

    }

    private void recuperarPerfil(){
        binding.progressBar.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));


        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("perfil_empresa");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    perfilEmpresa = snapshot.getValue(PerfilEmpresa.class);
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(perfilEmpresa.getUrlImagem())
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                   // binding.imagemFoto.setImageBitmap(resource);
                                    setLocalBitmapUri(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                    binding.progressBar.setVisibility(View.GONE);
                }else{
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                if (snapshot.exists()){
                    usuario = snapshot.getValue(Usuario.class);
                }else {
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
        if (clienteSelecionado != null){
            clienteSelecionado.setNome(binding.edtNome.getText().toString());
            clienteSelecionado.setTelefone1(binding.edtTelefone.getText().toString());
            orcamento = new Orcamento();
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            orcamento.setId(databaseReference.push().getKey());
            orcamento.setIdCliente(clienteSelecionado);
            if (enderecoList.size() > 0){
                orcamento.setIdEndereco(enderecoList.get(end));
            }
            orcamento.setIdUsuario(usuario);
            orcamento.setData(String.valueOf(Timestamp.getUnixTimestamp()));
            orcamento.setItens(itemVendaList);
            orcamento.setStatus("Em analise");
            orcamento.setDesconto(String.valueOf(desconto));
            orcamento.setTipoPagamento(pagamento);
            orcamento.setTotal(binding.includeSheet.tvTotalCart.getText().toString());
            orcamento.setSubTotal(binding.includeSheet.tvTotalCart.getText().toString());

            SPM spm = new SPM(getApplicationContext());
            DatabaseReference produtoRef = FirebaseHelper.getDatabaseReference()
                    .child("empresas")
                    .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                    .child("orcamentos").child(orcamento.getId());
            produtoRef.setValue(orcamento).addOnSuccessListener(unused -> {
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(), ListaOrcamentoActivity.class);
                intent.putExtra("orcamento", orcamento);
                startActivity(intent);

            });
          /* try {


                createPdf(orcamento, perfilEmpresa);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                Toast.makeText(this, " erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }*/
        }else {
            Toast.makeText(getApplicationContext(), "Selecione um Cliente", Toast.LENGTH_SHORT).show();
        }


    }

    public Uri setLocalBitmapUri(Bitmap bitmap) {

        Uri bmpUri = null;
        try {
            File file = new File(this.getExternalFilesDir(null) + File.separator + "ecommercempa/foto perfil" + File.separator);

            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file + File.separator + "perfil" + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.close();
            bmpUri = Uri.fromFile(file);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }




    private void createPdf(Orcamento orcamento, PerfilEmpresa perfilEmpresa) throws IOException, DocumentException {

        // LINE SEPARATOR
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

        File pdfFolder = new File(this.getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/orcamentos"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }


        myFile = new File(pdfFolder + File.separator + "orcamento" + ".pdf");


        OutputStream output = new FileOutputStream(myFile);

        PdfWriter pdfWriter = PdfWriter.getInstance(document, output);

        document.open();
        // ParagraphBorder border = new ParagraphBorder();
        //pdfWriter.setPageEvent(border);
        // Titulo

        Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLDITALIC);
        Font chapterFont2 = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
        Font paragraphFont4 = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        Font paragraphFont2 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        Font paragraphFont3 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
        Font paragraphRodaPe = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
        Font paragraphRodaPe2 = FontFactory.getFont(FontFactory.HELVETICA, 4, Font.NORMAL);

        PdfPTable table = new PdfPTable(3);

        int headerwidths[] = {30, 50, 20};
        table.setWidthPercentage(100);
        table.setWidths(headerwidths);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        File baseDir = this.getExternalFilesDir(null);
        String url = baseDir.getAbsolutePath() + File.separator + "ecommercempa/foto perfil/" + "perfil" + ".png";
        Image img = Image.getInstance(url);
        img.scaleAbsoluteWidth(80);
        img.scaleAbsoluteHeight(80f);
        img.setAlignment(Element.ALIGN_CENTER);
        PdfPCell c1 = new PdfPCell();
        c1.addElement(img);
        PdfPCell c2 = new PdfPCell();
        PdfPCell c3 = new PdfPCell();

        c1.setBorder(NO_BORDER);
        c2.setBorder(NO_BORDER);
        c3.setBorder(NO_BORDER);

        Paragraph p5 = new Paragraph("Tel.: " + perfilEmpresa.getTelefone1(), paragraphFont2);
        Paragraph p6 = new Paragraph("WhatsApp.: " + perfilEmpresa.getTelefone2(), paragraphFont2);
        Paragraph p7 = new Paragraph("Rua: " + perfilEmpresa.getEndereco().getLogradouro() + " - " + "N.: " + perfilEmpresa.getEndereco().getNumero(),paragraphFont2);
        Paragraph p8 = new Paragraph(perfilEmpresa.getEndereco().getBairro() + " - " + perfilEmpresa.getEndereco().getLocalidade() + " - " + perfilEmpresa.getEndereco().getUf(),paragraphFont2);
        Paragraph p9 = new Paragraph("CEP: " + perfilEmpresa.getEndereco().getCep(),paragraphFont2);
        Paragraph pDez = new Paragraph( perfilEmpresa.getNome(),paragraphFont2);

        Paragraph p10 = new Paragraph(10, "Criado em", paragraphFont3);
        Paragraph p11 = new Paragraph(10, Timestamp.getFormatedDateTime(Long.parseLong(orcamento.getData()), "dd/MM/yy"), paragraphFont4);
        Paragraph p12 = new Paragraph(25, "Valido por", paragraphFont3);
        Paragraph p13 = new Paragraph(10, "30 dias", paragraphFont4);
        Paragraph p14 = new Paragraph(23, "Orçamento id:", paragraphFont3);
        Paragraph p15 = new Paragraph(10, orcamento.getData(), paragraphFont4);

        p5.setAlignment(Element.ALIGN_CENTER);
        p6.setAlignment(Element.ALIGN_CENTER);
        p7.setAlignment(Element.ALIGN_CENTER);
        p8.setAlignment(Element.ALIGN_CENTER);
        p9.setAlignment(Element.ALIGN_CENTER);
        pDez.setAlignment(Element.ALIGN_CENTER);

        p10.setAlignment(Element.ALIGN_CENTER);
        p11.setAlignment(Element.ALIGN_CENTER);
        p12.setAlignment(Element.ALIGN_CENTER);
        p13.setAlignment(Element.ALIGN_CENTER);
        p14.setAlignment(Element.ALIGN_CENTER);
        p15.setAlignment(Element.ALIGN_CENTER);

        c2.addElement(p5);
        c2.addElement(p6);
        c2.addElement(p7);
        c2.addElement(p8);
        c2.addElement(p9);
        c2.addElement(pDez);
        c3.addElement(p10);
        c3.addElement(p11);
        c3.addElement(p12);
        c3.addElement(p13);
        c3.addElement(p14);
        c3.addElement(p15);

        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);


        PdfPTable table2 = new PdfPTable(2);
        int headerwidths2[] = {70, 30};
        table2.setWidthPercentage(100);
        table2.setWidths(headerwidths2);
        table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.getDefaultCell().setBorder(NO_BORDER);
        PdfPCell c4 = new PdfPCell();
        PdfPCell c5 = new PdfPCell();
        c1.addElement(img);
        Paragraph p16 = new Paragraph(10, "Cliente:", paragraphFont2);
        Paragraph p17 = new Paragraph(10, orcamento.getIdCliente().getNome(), paragraphFont);
        Paragraph p18 = new Paragraph(10, "Vendedor:", paragraphFont2);
        Paragraph p19 = new Paragraph(10, usuario.getNome(), paragraphFont);

        c4.addElement(p16);
        c4.addElement(p17);
        c5.addElement(p18);
        c5.addElement(p19);

        PdfPTable table3 = new PdfPTable(3);
        int headerwidths3[] = {30, 30, 40};
        table3.setWidthPercentage(100);
        table3.setWidths(headerwidths3);
        table3.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.getDefaultCell().setBorder(NO_BORDER);
        PdfPCell c6 = new PdfPCell();
        PdfPCell c7 = new PdfPCell();
        PdfPCell c8 = new PdfPCell();
        c1.addElement(img);
        Paragraph p20 = new Paragraph(10, "Telefone 1:", paragraphFont2);
        Paragraph p21 = new Paragraph(10, orcamento.getIdCliente().getTelefone1(), paragraphFont);
        Paragraph p22 = new Paragraph(10, "Telefone 2:", paragraphFont2);
        Paragraph p23 = new Paragraph(10, orcamento.getIdCliente().getTelefone2(), paragraphFont);
        Paragraph p24 = new Paragraph(10, "Email:", paragraphFont2);
        Paragraph p25 = new Paragraph(10, orcamento.getIdCliente().getEmail(), paragraphFont);

        c6.addElement(p20);
        c6.addElement(p21);
        c7.addElement(p22);
        c7.addElement(p23);
        c8.addElement(p24);
        c8.addElement(p25);


        Chunk chunk = new Chunk("Orçamento", chapterFont2);
        // chunk.setBackground(BaseColor.BLACK);
        Paragraph titulo = new Paragraph(chunk);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(table);
        document.add(titulo);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        table2.addCell(c4);
        table2.addCell(c5);
        table3.addCell(c6);
        table3.addCell(c7);
        table3.addCell(c8);
        document.add(table2);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        document.add(table3);
        document.add(new Paragraph("\n", paragraphRodaPe2));

        PdfContentByte canvas = pdfWriter.getDirectContent();
        Rectangle rect = new Rectangle(36, 710, 445, 805);
        rect.setBorder(Rectangle.BOX);
        rect.setBorderWidth(1);
        canvas.rectangle(rect);

        PdfContentByte canvas2 = pdfWriter.getDirectContent();
        Rectangle rect2 = new Rectangle(450, 776, 559, 805);
        rect2.setBorder(Rectangle.BOX);
        rect2.setBorderWidth(1);
        canvas2.rectangle(rect2);

        PdfContentByte canvas3 = pdfWriter.getDirectContent();
        Rectangle rect3 = new Rectangle(450, 743, 559, 771);
        rect3.setBorder(Rectangle.BOX);
        rect3.setBorderWidth(1);
        canvas3.rectangle(rect3);

        PdfContentByte canvas4 = pdfWriter.getDirectContent();
        Rectangle rect4 = new Rectangle(450, 710, 559, 738);
        rect4.setBorder(Rectangle.BOX);
        rect4.setBorderWidth(1);
        canvas4.rectangle(rect4);


        createTable(orcamento);


        Chunk glue1 = new Chunk(new VerticalPositionMark());
        Paragraph p1 = new Paragraph("Subtotal", paragraphFont3);
        p1.add(new Chunk(glue1));
        p1.add(orcamento.getTotal());
        document.add(p1);

        if (Integer.parseInt(orcamento.getDesconto()) > 0) {

            Chunk glue2 = new Chunk(new VerticalPositionMark());
            Paragraph p2 = new Paragraph("Desconto aplicado nos produtos", paragraphFont3);
            p2.add(new Chunk(glue2));
            p2.add(orcamento.getDesconto() + "%");
            document.add(p2);
        }

        Chunk glue = new Chunk(new VerticalPositionMark());
        Paragraph p = new Paragraph("Total", paragraphFont);
        p.add(new Chunk(glue));
        p.add(orcamento.getTotal());
        document.add(p);

        document.add(new Chunk(lineSeparator));

        Chunk glue3 = new Chunk();
        glue3.setFont(paragraphFont2);
        Paragraph p3 = new Paragraph("Forma de pagamento: ", paragraphFont3);
        p3.add(new Chunk(glue3));
        p3.add(orcamento.getTipoPagamento());
        document.add(p3);

        document.add(new Paragraph("\n", paragraphRodaPe));

        Paragraph rodape2 = new Paragraph(perfilEmpresa.getNome(), paragraphRodaPe);
        rodape2.setAlignment(Element.ALIGN_CENTER);
        document.add(rodape2);

        Paragraph rodape3 = new Paragraph(configuracao.getRodape(), paragraphRodaPe);
        rodape3.setAlignment(Element.ALIGN_CENTER);
        document.add(rodape3);

        document.close();

    }

    private void createTable(Orcamento orcamento)
            throws DocumentException {

        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE);
        Font paragraphFont2 = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
        PdfPTable table = new PdfPTable(5);

        int headerwidths[] = {15, 60, 10, 20, 25};
        table.setWidthPercentage(100);
        table.setWidths(headerwidths);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell c1 = new PdfPCell(new Phrase("CÓDIGO", paragraphFont));
        c1.setBorder(0);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(0);
        (c1).setBackgroundColor(BaseColor.BLACK);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("DESCRIÇÃO", paragraphFont));
        c1.setBorder(0);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(0);
        (c1).setBackgroundColor(BaseColor.BLACK);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("QTD", paragraphFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(0);
        (c1).setBackgroundColor(BaseColor.BLACK);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("PREÇO UN.", paragraphFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(0);
        (c1).setBackgroundColor(BaseColor.BLACK);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("PREÇO TOTAL.", paragraphFont));
        c1.setBorder(0);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(0);
        (c1).setBackgroundColor(BaseColor.BLACK);
        table.addCell(c1);
        table.setHeaderRows(1);

        for (int i = 0; i < orcamento.getItens().size(); i++) {

            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getCodigo(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getNome(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(orcamento.getItens().get(i).getQtd()), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getPreco_venda(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(NumberFormat.getCurrencyInstance().format(somatoriaDosProdutosIguais(orcamento.getItens().get(i).getPreco_venda(), String.valueOf(orcamento.getItens().get(i).getQtd()))), paragraphFont2)));
        }

        document.add(table);
    }
    private BigDecimal somatoriaDosProdutosIguais(String sPreco, String sQtd) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal preco = Util.convertMoneEmBigDecimal(sPreco);
        preco = preco.divide(new BigDecimal("100"));
        BigDecimal qtd = Util.convertMoneEmBigDecimal(sQtd);
        total = total.add(preco.multiply(qtd));
        return total;
    }
}