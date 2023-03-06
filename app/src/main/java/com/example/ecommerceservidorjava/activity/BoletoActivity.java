package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityBoletoBinding;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroDespesaBinding;
import com.example.ecommerceservidorjava.model.Boleto;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BoletoActivity extends AppCompatActivity {
    Boleto boletoSelecionado;
    ActivityBoletoBinding binding;
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private long timestap;
    private SPM spm = new SPM(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoletoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        binding.editData.setOnClickListener(view -> {
            setDateTimeField();
            fromDatePickerDialog.show();
        });

        binding.btnSalvar.setOnClickListener(view -> {
            salvar();
        });
    }

    private void recuperarIntent() {

        boletoSelecionado = (Boleto) getIntent().getSerializableExtra("boletoSelecionado");
        if (boletoSelecionado != null) {
            binding.btnSalvar.setText("Salvar");

            binding.editCliente.setText(boletoSelecionado.getIdVenda().getIdCliente().getNome());
            binding.editID.setText(boletoSelecionado.getId());
            binding.editValor.setText(boletoSelecionado.getIdVenda().getTotal());
            if (boletoSelecionado.getParcela1() != null) {
                if (!boletoSelecionado.getParcela1().replaceAll("[^0-9]", "").equals("000")){
                    binding.editParcela1.setText(boletoSelecionado.getParcela1());
                    binding.llParcela1.setBackgroundResource(R.drawable.borda);
                }

            }
            if (boletoSelecionado.getParcela2() != null) {
                if (!boletoSelecionado.getParcela2().replaceAll("[^0-9]", "").equals("000")){
                    binding.editParcela2.setText(boletoSelecionado.getParcela2());
                    binding.llParcela2.setBackgroundResource(R.drawable.borda);
                }

            }
            if (boletoSelecionado.getParcela3() != null) {
                if (!boletoSelecionado.getParcela3().replaceAll("[^0-9]", "").equals("000")){
                    binding.editParcela3.setText(boletoSelecionado.getParcela3());
                    binding.llParcela3.setBackgroundResource(R.drawable.borda);
                }

            }


            binding.editData.setText(Timestamp.getFormatedDateTime(Long.parseLong(boletoSelecionado.getData()), "dd/MM/yyyy"));

            // binding.btnCategorias.setText(boletoSelecionado.getCategoria());
            timestap = Long.parseLong(boletoSelecionado.getData());
        }
    }

    private void setDateTimeField() {

        final Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Calendar newDate = Calendar.getInstance();
                //newDate.set(year, monthOfYear, dayOfMonth);
                // culto.setText(dateFormatter.format(newDate.getTime()));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long date_ship_millis = calendar.getTimeInMillis();
                timestap = date_ship_millis / 1000;
                binding.editData.setText(Timestamp.getFormatedDateTime(timestap, "dd/MM/yyyy"));


            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    private void salvar() {
        boletoSelecionado.setParcela1(binding.editParcela1.getText().toString());
        boletoSelecionado.setParcela2(binding.editParcela2.getText().toString());
        boletoSelecionado.setParcela3(binding.editParcela3.getText().toString());
        boletoSelecionado.setData(String.valueOf(timestap));

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("boletos").child(boletoSelecionado.getId());

        databaseReference.setValue(boletoSelecionado).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "erro de foto", Toast.LENGTH_SHORT).show();
            }

        });
    }
}