package com.example.ecommerceservidorjava.activity;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityAndroidPdfviewerBinding;
import com.example.ecommerceservidorjava.databinding.ActivityBoletoBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity that opens the first page of a PDF document using {@link PdfRenderer}
 * and displays it in an {@link ImageView}.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidPDFViewerVendas extends AppCompatActivity {
    ActivityAndroidPdfviewerBinding binding;
    String caminho ;
    String arquivo;
    private static final String FILE_NAME = "sample_cache.pdf";
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mPdfPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAndroidPdfviewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recuperaraIntent();
        try {
            openPdfWithAndroidSDK(binding.pdfViewAndroid, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.fb.setOnClickListener(v -> {

        });
    }

    public void recuperaraIntent() {
        caminho = getIntent().getStringExtra("caminho");
        arquivo = getIntent().getStringExtra("arquivo");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPdfPage != null) {
            mPdfPage.close();
        }
        if (mPdfRenderer != null) {
            mPdfRenderer.close();
        }
    }

    /**
     * Render a given page in the PDF document into an ImageView.
     *
     * @param imageView  used to display the PDF
     * @param pageNumber page of the PDF to view (index starting at 0)
     */
    void openPdfWithAndroidSDK(SubsamplingScaleImageView imageView, int pageNumber) throws IOException {
        // Copia sample.pdf da pasta de recursos brutos para o cache local, para que o PdfRenderer possa lidar com isso

        File pdfFolder = new File(context.getExternalFilesDir(null)
                + File.separator
                + "ecommercempa/" + caminho
                + File.separator);
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }


        File myFile = new File(pdfFolder + File.separator + arquivo + ".pdf");
        // File fileCopy = new File(getCacheDir(), FILE_NAME);
        //copyToLocalCache(fileCopy, R.raw.sample);


        // We will get a page from the PDF file by calling PdfRenderer.openPage
        ParcelFileDescriptor fileDescriptor =
                ParcelFileDescriptor.open(myFile,
                        ParcelFileDescriptor.MODE_READ_ONLY);
        mPdfRenderer = new PdfRenderer(fileDescriptor);
        mPdfPage = mPdfRenderer.openPage(pageNumber);

        // Create a new bitmap and render the page contents on to it
        int height = 2000;
        int width = height * mPdfPage.getWidth() / mPdfPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,

                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFFFFFFF);
        mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageView.resetScaleAndCenter();
        // Set the bitmap in the ImageView so we can view it
        imageView.setImage(ImageSource.cachedBitmap(bitmap));
    }


    /**
     * Copies the resource PDF file locally so that {@link PdfRenderer} can handle the file
     *
     * @param outputFile  location of copied file
     * @param pdfResource pdf resource file
     */
    void copyToLocalCache(File outputFile, @RawRes int pdfResource) throws IOException {
        if (!outputFile.exists()) {
            InputStream input = getResources().openRawResource(pdfResource);
            FileOutputStream output;
            output = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int size;
            // Just copy the entire contents of the file
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            input.close();
            output.close();
        }
    }
}
