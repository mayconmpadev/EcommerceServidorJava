package com.example.ecommerceservidorjava.util;

import static com.itextpdf.text.Element.ALIGN_BOTTOM;
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
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.model.Venda;
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

public class GerarPDFOSFinalizada extends AppCompatActivity {

    private OrdemServico ordemServico;
    private PerfilEmpresa perfilEmpresa;
    private Configuracao configuracao;
    Context context;
    private SPM spm;
    private String subTotal = "";


    Document document = new Document();
    File myFile;

    public GerarPDFOSFinalizada(OrdemServico ordemServico, Context context) {
        this.ordemServico = ordemServico;
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

        for (int i = 0; i < ordemServico.getItens().size(); i++) {
            if (ordemServico.getItens().get(i).getQtd() != 0) {
                BigDecimal preco = Util.convertMoneEmBigDecimal(ordemServico.getItens().get(i).getPreco_venda());
                preco = preco.divide(new BigDecimal("100"));

                total = total.add(new BigDecimal(ordemServico.getItens().get(i).getQtd()).multiply(preco));

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
                createPdf(ordemServico, perfilEmpresa);
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
                createPdf(ordemServico, perfilEmpresa);
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
                createPdf(ordemServico, perfilEmpresa);
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


    private void createPdf(OrdemServico ordemServico, PerfilEmpresa perfilEmpresa) throws IOException, DocumentException {

        // LINE SEPARATOR
        LineSeparator lineSeparator = new LineSeparator(0.5f, 100, null, Element.ALIGN_UNDEFINED, -2);
        lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

        File pdfFolder = new File(context.getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/ordemServicos"
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }


        myFile = new File(pdfFolder + File.separator + "ordemServico" + ".pdf");


        OutputStream output = new FileOutputStream(myFile);

        PdfWriter pdfWriter = PdfWriter.getInstance(document, output);

        document.open();
        // ParagraphBorder border = new ParagraphBorder();
        //pdfWriter.setPageEvent(border);
        // Titulo

        Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLDITALIC);
        Font chapterFont1 = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        Font chapterFont2 = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
        Font paragraphFont4 = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        Font paragraphFont2 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        Font paragraphFont3 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
        Font paragraphRodaPe = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
        Font paragraphRodaPe2 = FontFactory.getFont(FontFactory.HELVETICA, 4, Font.NORMAL);





        Paragraph p50 = new Paragraph(new Chunk(lineSeparator));
//_________________________________________________________________________TABELA 1_________________________________________________________________________________
        Chunk glue2 = new Chunk(new VerticalPositionMark());
        Paragraph g = new Paragraph(perfilEmpresa.getNome(), chapterFont1);
        Chunk o = new Chunk(perfilEmpresa.getTelefone1(), paragraphFont);

        g.add(new Chunk(glue2));
        g.add(o);

        Paragraph p1_tabela1 = g;
        Paragraph p2_tabela1 = new Paragraph("Rua: " + perfilEmpresa.getEndereco().getLogradouro() + " - "
                + perfilEmpresa.getEndereco().getBairro() + " - " + perfilEmpresa.getEndereco().getLocalidade() + " - "
                + perfilEmpresa.getEndereco().getUf() + " - N.: " + perfilEmpresa.getEndereco().getNumero(), paragraphFont2);

        p1_tabela1.setAlignment(Element.ALIGN_LEFT);
        p2_tabela1.setAlignment(Element.ALIGN_LEFT);

        PdfPTable table1 = new PdfPTable(2);
        int headerwidths[] = {15, 65};
        table1.setWidthPercentage(100);
        table1.setWidths(headerwidths);
        table1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell tabela1_c1 = new PdfPCell();
        PdfPCell tabela1_c2 = new PdfPCell();
        tabela1_c1.setBorder(NO_BORDER);
        tabela1_c2.setBorder(NO_BORDER);

        File baseDir = context.getExternalFilesDir(null);
        String url = baseDir.getAbsolutePath() + File.separator + "ecommercempa/foto perfil/" + "perfil" + ".png";
        Image img = Image.getInstance(url);
        img.scaleAbsoluteWidth(65f);
        img.scaleAbsoluteHeight(65f);
        img.setAlignment(Element.ALIGN_CENTER);

        tabela1_c1.addElement(img);
        tabela1_c1.setUseAscender(true);
//_________________________________________________________________________TABELA 2_________________________________________________________________________________

        Paragraph p1_tabela2 = new Paragraph("COMPROVANTE DE RETIRADA - OS Nº " + ordemServico.getNumeroOs(), paragraphFont4);
        Paragraph p2_tabela2 = new Paragraph("Hora: " + Timestamp.getFormatedDateTime(Long.parseLong(ordemServico.getDataEntrada()), "HH:mm"), paragraphFont3);
        Paragraph p3_tabela2 = new Paragraph("Data: " + Timestamp.getFormatedDateTime(Long.parseLong(ordemServico.getDataEntrada()), "dd/MM/yy"), paragraphFont3);

        p1_tabela2.setAlignment(Element.ALIGN_TOP);
        p2_tabela2.setAlignment(Element.ALIGN_RIGHT);
        p3_tabela2.setAlignment(Element.ALIGN_RIGHT);

        PdfPTable table2 = new PdfPTable(3);
        int headerwidths2[] = {60, 20, 20};
        table2.setWidthPercentage(100);
        table2.setWidths(headerwidths2);
       // table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        // String url = "/data/data/" + this.getPackageName() + "/mpasistema/foto perfil/" + "perfil" + ".png";
        PdfPCell table2_c1 = new PdfPCell();
        PdfPCell table2_c2 = new PdfPCell();
        PdfPCell table2_c3 = new PdfPCell();
        table2_c1.setBorder(NO_BORDER);
        table2_c2.setBorder(NO_BORDER);
        table2_c3.setBorder(NO_BORDER);

        table2_c1.setVerticalAlignment(ALIGN_BOTTOM);
        table2_c2.setVerticalAlignment(ALIGN_BOTTOM);
        table2_c3.setVerticalAlignment(ALIGN_BOTTOM);

       // Paragraph p11 = new Paragraph(10, Timestamp.getFormatedDateTime(Long.parseLong(ordemServico.getDataEntrada()), "dd/MM/yy"), paragraphFont4);

        tabela1_c2.addElement(p1_tabela1);
        tabela1_c2.addElement(p2_tabela1);

        table2_c1.addElement(p1_tabela2);
        table2_c2.addElement(p2_tabela2);
        table2_c3.addElement(p3_tabela2);

        tabela1_c2.addElement(p50);
        tabela1_c2.addElement(table2);

        table2.addCell(table2_c1);
        table2.addCell(table2_c2);
        table2.addCell(table2_c3);
        table1.addCell(tabela1_c1);
        table1.addCell(tabela1_c2);




        Chunk glue = new Chunk(new VerticalPositionMark());
        Paragraph p = new Paragraph("Cliente: " + ordemServico.getIdCliente().getNome(), paragraphFont);
        p.add(new Chunk(glue));
        p.add("Telefone: " + ordemServico.getTelefone());

        Chunk glue1 = new Chunk(new VerticalPositionMark());
        Paragraph p1 = new Paragraph("Acessório: " + ordemServico.getObservacao(), paragraphFont);
        p1.add(new Chunk(glue1));
        if (ordemServico.isGarantia()){
            p1.add("Equipamento na garantia   x ");
        }else {
            p1.add("Equipamento na garantia     ");
        }


//_________________________________________________________________________TABELA 3__________________________________________________________________________________

        Paragraph p1_tabela3 = new Paragraph("Eqipamento: " + ordemServico.getEquipamento(), paragraphFont);
        Paragraph p2_tabela3 = new Paragraph("Marca: " + ordemServico.getMarca(), paragraphFont);
        Paragraph p3_tabela3 = new Paragraph("Modelo: " + ordemServico.getModelo(), paragraphFont);


        p1_tabela3.setAlignment(Element.ALIGN_LEFT);
        p2_tabela3.setAlignment(Element.ALIGN_CENTER);
        p3_tabela3.setAlignment(Element.ALIGN_RIGHT);

        PdfPTable table3 = new PdfPTable(3);
        int headerwidths3[] = {35, 30, 45};
        table3.setWidthPercentage(100);
        table3.setWidths(headerwidths3);

        // table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        // String url = "/data/data/" + this.getPackageName() + "/mpasistema/foto perfil/" + "perfil" + ".png";
        PdfPCell table3_c1 = new PdfPCell();
        PdfPCell table3_c2 = new PdfPCell();
        PdfPCell table3_c3 = new PdfPCell();
        table3_c1.setBorder(NO_BORDER);
        table3_c2.setBorder(NO_BORDER);
        table3_c3.setBorder(NO_BORDER);

        table3_c1.addElement(p1_tabela3);
        //table3_c1.setUseAscender(true);
        table3_c2.addElement(p2_tabela3);
        //table3_c2.setUseAscender(true);
        table3_c3.addElement(p3_tabela3);
       // table3_c3.setUseAscender(true);

        table3.addCell(table3_c1);
        table3.addCell(table3_c2);
        table3.addCell(table3_c3);

        //_________________________________________________________________________TABELA 4__________________________________________________________________________________

        Paragraph p1_tabela4 = new Paragraph(new Chunk(lineSeparator));
        Paragraph p2_tabela4 = new Paragraph("", paragraphFont);
        Paragraph p3_tabela4 = new Paragraph(new Chunk(lineSeparator));
        Paragraph p4_tabela4 = new Paragraph("Assi. Cliente", paragraphFont);
        Paragraph p5_tabela4 = new Paragraph("Assi. Técnico", paragraphFont);


        p1_tabela4.setAlignment(Element.ALIGN_CENTER);
        p2_tabela4.setAlignment(Element.ALIGN_CENTER);
        p3_tabela4.setAlignment(Element.ALIGN_RIGHT);
        p4_tabela4.setAlignment(Element.ALIGN_CENTER);
        p5_tabela4.setAlignment(Element.ALIGN_CENTER);

        PdfPTable table4 = new PdfPTable(3);
        int headerwidths4[] = {30, 30, 30};
        table4.setWidthPercentage(100);
        table4.setWidths(headerwidths4);

        // table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        // String url = "/data/data/" + this.getPackageName() + "/mpasistema/foto perfil/" + "perfil" + ".png";
        PdfPCell table4_c1 = new PdfPCell();
        PdfPCell table4_c2 = new PdfPCell();
        PdfPCell table4_c3 = new PdfPCell();
        table4_c1.setBorder(NO_BORDER);
        table4_c2.setBorder(NO_BORDER);
        table4_c3.setBorder(NO_BORDER);

        table4_c1.addElement(p1_tabela4);
        table4_c1.addElement(p4_tabela4);
        //table3_c1.setUseAscender(true);
        table4_c2.addElement(p2_tabela4);
        //table3_c2.setUseAscender(true);
        table4_c3.addElement(p3_tabela4);
        table4_c3.addElement(p5_tabela4);
        // table3_c3.setUseAscender(true);

        table4.addCell(table4_c1);
        table4.addCell(table4_c2);
        table4.addCell(table4_c3);


        document.add(table1);
        document.add(lineSeparator);

        document.add(p);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        document.add(lineSeparator);

        document.add(table3);
        document.add(p1);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        document.add(lineSeparator);

        PdfContentByte canvas = pdfWriter.getDirectContent();
        canvas.moveTo(36, 36);
        canvas.lineTo(559, 36);
        canvas.stroke();



        Paragraph pp = new Paragraph("Problema encontrado:", paragraphFont4);
        Paragraph problema = new Paragraph(ordemServico.getDefeitoEncontrado() + "\n", paragraphFont);
        Paragraph ppp = new Paragraph("Condições de Garantia:", paragraphFont4);
        Paragraph condicao = new Paragraph("1 - A Empresa da garantia de 90 dias para mão de obra e peças usadas no conserto, contados a partir da entrega" + "\n", paragraphFont);

        Chunk glue3 = new Chunk(new VerticalPositionMark());
        Paragraph p3 = new Paragraph("Assi. Cliente " , paragraphFont);
        p3.add(new Chunk(glue3));
        p3.add("Assi. Técnico ");
        document.add(pp);
        document.add(problema);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        document.add(lineSeparator);
        createTable(ordemServico);

        Chunk glue4 = new Chunk(new VerticalPositionMark());
        Paragraph p4 = new Paragraph("Mão de obra", paragraphFont3);
        p4.add(new Chunk(glue4));
        p4.add(ordemServico.getValorMaoDeObra());
        document.add(p4);

        Chunk glue5 = new Chunk(new VerticalPositionMark());
        Paragraph p5 = new Paragraph("Total", paragraphFont3);
        p5.add(new Chunk(glue5));
        p5.add(ordemServico.getTotal());
        document.add(p5);

        document.add(ppp);
        document.add(condicao);
        document.add(new Paragraph("\n", paragraphRodaPe2));
        document.add(new Paragraph("\n", paragraphRodaPe2));

        document.add(table4);



        PdfContentByte canvas2 = pdfWriter.getDirectContent();
        Rectangle rect2 = new Rectangle(549, 675, 559, 685);
        rect2.setBorder(Rectangle.BOX);
        rect2.setBorderWidth(1);
        canvas2.rectangle(rect2);

       // PdfContentByte canvas3 = pdfWriter.getDirectContent();
      //  Rectangle rect3 = new Rectangle(549, 675, 559, 685);
       // rect3.setBorder(Rectangle.BOX);
       // rect3.setBorderWidth(1);
        //canvas3.rectangle(rect3);


        document.close();
        Parametro.bPdf = true;


    }

    private void createTable(OrdemServico ordemServico)
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
        if (ordemServico.getItens() != null){
            for (int i = 0; i < ordemServico.getItens().size(); i++) {

                table.addCell(new PdfPCell(new Phrase(ordemServico.getItens().get(i).getCodigo(), paragraphFont2)));
                table.addCell(new PdfPCell(new Phrase(ordemServico.getItens().get(i).getNome(), paragraphFont2)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(ordemServico.getItens().get(i).getQtd()), paragraphFont2)));
                table.addCell(new PdfPCell(new Phrase(ordemServico.getItens().get(i).getPreco_venda(), paragraphFont2)));
                table.addCell(new PdfPCell(new Phrase(NumberFormat.getCurrencyInstance().format(somatoriaDosProdutosIguais(ordemServico.getItens().get(i).getPreco_venda(), String.valueOf(ordemServico.getItens().get(i).getQtd()))), paragraphFont2)));
            }
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