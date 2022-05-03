package com.example.ecommerceservidorjava.util;

import static com.itextpdf.text.Rectangle.NO_BORDER;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Orcamento;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
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

public class GerarPDF extends AppCompatActivity {

    private Orcamento orcamento;
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    Context context;
    private SPM spm;
    private String subTotal = "";


    Document document = new Document();
    File myFile;

    public GerarPDF(Orcamento orcamento, Context context) {
        this.orcamento = orcamento;
        this.context = context;
        spm = new SPM(context);
        recuperarPerfil();
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
                    finalizar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String total(String tipo, int valor) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal desconto = new BigDecimal("0");

        for (int i = 0; i < orcamento.getItens().size(); i++) {
            if (orcamento.getItens().get(i).getQtd() != 0) {
                BigDecimal preco = Util.convertMoneEmBigDecimal(orcamento.getItens().get(i).getPreco());
                preco = preco.divide(new BigDecimal("100"));

                total = total.add(new BigDecimal(orcamento.getItens().get(i).getQtd()).multiply(preco));

            }

        }
        if (!tipo.equals("credito")) {
            desconto = total.multiply(new BigDecimal(valor).divide(new BigDecimal(100)));

        }
        total = total.subtract(desconto);
        return NumberFormat.getCurrencyInstance().format(total);
    }


    private void finalizar() {

        try {
            File perfil = new File(context.getExternalFilesDir(null)
                    + File.separator
                    + "ecommercempa/foto perfil"
                    + File.separator);

            File file = new File(perfil + File.separator + "perfil" + ".png");
            if (file.exists()) {
                createPdf(orcamento, perfilEmpresa);
            } else {

                if (perfilEmpresa == null) {
                    setLocalBitmapUri("perfil");
                } else {

                    baixarFoto();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            Toast.makeText(context, " erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public Uri setLocalBitmapUri(String uid) {
        // Extract Bitmap from ImageView drawable
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_login_off);

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(null) + File.separator + "ecommercempa/foto perfil" + File.separator);

            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file + File.separator + uid + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.close();
            bmpUri = Uri.fromFile(file);

            try {
                createPdf(orcamento, perfilEmpresa);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void baixarFoto() {


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(perfilEmpresa.getUrlImagem());
        File pdfFolder = new File(context.getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/foto perfil"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        final File myFile2 = new File(pdfFolder + File.separator + "perfil" + ".png");

        storageReference.getFile(myFile2).addOnSuccessListener(taskSnapshot -> {

            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", myFile2);
            try {
                createPdf(orcamento, perfilEmpresa);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }

        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(getApplicationContext(), "erro: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    private void createPdf(Orcamento orcamento, PerfilEmpresa perfilEmpresa) throws IOException, DocumentException {

        // LINE SEPARATOR
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

        File pdfFolder = new File(context.getExternalFilesDir(null)
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
        //String url;
        // String url = "/data/data/" + this.getPackageName() + "/mpasistema/foto perfil/" + "perfil" + ".png";
        File baseDir = context.getExternalFilesDir(null);
        String url = baseDir.getAbsolutePath() + File.separator + "ecommercempa/foto perfil/" + "perfil" + ".png";
        Image img = Image.getInstance(url);
        img.scaleAbsoluteWidth(100f);
        img.scaleAbsoluteHeight(100f);
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
        Paragraph p7 = new Paragraph("Rua: " + perfilEmpresa.getEndereco().getLogradouro() + " - " + "N.: " + perfilEmpresa.getEndereco().getNumero(), paragraphFont2);
        Paragraph p8 = new Paragraph(perfilEmpresa.getEndereco().getBairro() + " - " + perfilEmpresa.getEndereco().getLocalidade() + " - " + perfilEmpresa.getEndereco().getUf(), paragraphFont2);
        Paragraph p9 = new Paragraph("CEP: " + perfilEmpresa.getEndereco().getCep(), paragraphFont2);
        Paragraph pDez = new Paragraph(perfilEmpresa.getNome(), paragraphFont2);

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
        Paragraph p19 = new Paragraph(10, orcamento.getIdUsuario().getNome(), paragraphFont);

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


        // document.add(new Paragraph("Para:  " + venda.nomeCliente + "\n" + "Tel:  " + venda.telefone1Cliente, paragraphFont));

        // document.add(new Paragraph("Tel:  " + venda.telefone2Cliente, paragraphFont));
        // Chunk glue2 = new Chunk(new VerticalPositionMark());
        // Chunk glue2 = new Chunk(data());
        // glue2.setFont(paragraphFont);

        // Paragraph p2 = new Paragraph("vendedor(a): " + venda.nomeVendedor + "\n", paragraphFont2);
        // p2.add(new Chunk(glue2));
        //border.setActive(true);
        // document.add(p2);
        // document.add(new Chunk(lineSeparator));

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
        p1.add(orcamento.getSubTotal());
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
        Parametro.bPdf = true;


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
//l
        for (int i = 0; i < orcamento.getItens().size(); i++) {

            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getCodigo(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getNome(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(orcamento.getItens().get(i).getQtd()), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(orcamento.getItens().get(i).getPreco(), paragraphFont2)));
            table.addCell(new PdfPCell(new Phrase(NumberFormat.getCurrencyInstance().format(somatoriaDosProdutosIguais(orcamento.getItens().get(i).getPreco(), String.valueOf(orcamento.getItens().get(i).getQtd()))), paragraphFont2)));
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