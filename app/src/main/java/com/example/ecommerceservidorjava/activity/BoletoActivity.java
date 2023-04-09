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

import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DatabaseReference;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BoletoActivity extends AppCompatActivity {
    Venda boletoSelecionado;
    ActivityBoletoBinding binding;
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private long timestap;
    private SPM spm = new SPM(this);

    //ghfghfghf
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
            if (somarParcelas()) {
                salvar();
            }

        });
    }

    private void recuperarIntent() {

        boletoSelecionado = (Venda) getIntent().getSerializableExtra("vendaSelecionado");
        if (boletoSelecionado != null) {
            binding.btnSalvar.setText("Salvar");

            binding.editCliente.setText(boletoSelecionado.getIdCliente().getNome());
            binding.editID.setText(boletoSelecionado.getId());
            binding.editValor.setText(boletoSelecionado.getTotal());
            if (boletoSelecionado.getParcela1() != null) {
                if (!boletoSelecionado.getParcela1().replaceAll("[^0-9]", "").equals("000")) {
                    binding.editParcela1.setText(boletoSelecionado.getParcela1());
                    int pL = binding.llParcela1.getPaddingLeft();
                    int pT = binding.llParcela1.getPaddingTop();
                    int pR = binding.llParcela1.getPaddingRight();
                    int pB = binding.llParcela1.getPaddingBottom();
                    binding.llParcela1.setBackground(getDrawable(R.drawable.borda));
                    binding.llParcela1.setPadding(pL, pT, pR, pB);
                }

            }
            if (boletoSelecionado.getParcela2() != null) {
                if (!boletoSelecionado.getParcela2().replaceAll("[^0-9]", "").equals("000")) {
                    binding.editParcela2.setText(boletoSelecionado.getParcela2());
                    binding.llParcela2.setBackgroundResource(R.drawable.borda);
                }

            }
            if (boletoSelecionado.getParcela3() != null) {
                if (!boletoSelecionado.getParcela3().replaceAll("[^0-9]", "").equals("000")) {
                    binding.editParcela3.setText(boletoSelecionado.getParcela3());
                    binding.llParcela3.setBackgroundResource(R.drawable.borda);
                }

            }


            binding.editData.setText(Timestamp.getFormatedDateTime(Long.parseLong(boletoSelecionado.getData()), "dd/MM/yyyy"));

            // binding.btnCategorias.setText(boletoSelecionado.getCategoria());
            timestap = Long.parseLong(boletoSelecionado.getData());
            somarParcelas();
        }
    }

    private void setDateTimeField() {

        final Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
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


        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    private void salvar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        boletoSelecionado.setParcela1(binding.editParcela1.getText().toString());
        boletoSelecionado.setParcela2(binding.editParcela2.getText().toString());
        boletoSelecionado.setParcela3(binding.editParcela3.getText().toString());
        boletoSelecionado.setData(String.valueOf(timestap));

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("vendas").child(boletoSelecionado.getId());

        databaseReference.setValue(boletoSelecionado).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                binding.progressBar.setVisibility(View.GONE);
                finish();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "erro de foto", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private boolean somarParcelas() {
        boolean a = false;
        BigDecimal parcela1 = new BigDecimal("0");
        BigDecimal parcela2 = new BigDecimal("0");
        BigDecimal parcela3 = new BigDecimal("0");
        BigDecimal dividir = new BigDecimal("100");
        BigDecimal total;

        parcela1 = Util.convertMoneEmBigDecimal(binding.editParcela1.getText().toString());
        parcela2 = Util.convertMoneEmBigDecimal(binding.editParcela2.getText().toString());
        parcela3 = Util.convertMoneEmBigDecimal(binding.editParcela3.getText().toString());

        total = parcela1.add(parcela2.add(parcela3));
        int resultado = total.compareTo(Util.convertMoneEmBigDecimal(boletoSelecionado.getTotal()));
        Toast.makeText(this, total.toString(), Toast.LENGTH_SHORT).show();
        total = total.divide(dividir);

        binding.textValorPago.setText(NumberFormat.getCurrencyInstance().format(total));


        if (resultado > 0) {
            if (parcela1.compareTo(BigDecimal.ZERO) == 0) {
                Toast.makeText(this, "  A primeira parcela não pode ser R$ 0,00 ", Toast.LENGTH_SHORT).show();
            } else if (parcela1.compareTo(BigDecimal.ZERO) != 0 & parcela2.compareTo(BigDecimal.ZERO) == 0 & parcela3.compareTo(BigDecimal.ZERO) != 0){
                Toast.makeText(this, "  A Segunda parcela não pode ser R$ 0,00 ", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "  O valor pago é maior !", Toast.LENGTH_SHORT).show();
                boletoSelecionado.setBoletoPago(true);
                a = true;
            }

        }
        if (resultado < 0 & parcela1.compareTo(BigDecimal.ZERO) != 0 & parcela2.compareTo(BigDecimal.ZERO) != 0 & parcela3.compareTo(BigDecimal.ZERO) != 0) {
            Toast.makeText(this, "  O valor pago é menor 1", Toast.LENGTH_SHORT).show();
            boletoSelecionado.setBoletoPago(false);
            a = false;
        } else if (resultado < 0 & parcela1.compareTo(BigDecimal.ZERO) != 0 & parcela2.compareTo(BigDecimal.ZERO) != 0 & parcela3.compareTo(BigDecimal.ZERO) == 0) {
            Toast.makeText(this, "  O valor pago é menor 2", Toast.LENGTH_SHORT).show();
            boletoSelecionado.setBoletoPago(false);
            a = true;
        }else if (resultado < 0 & parcela1.compareTo(BigDecimal.ZERO) != 0 & parcela2.compareTo(BigDecimal.ZERO) == 0 & parcela3.compareTo(BigDecimal.ZERO) == 0){
            Toast.makeText(this, "  O valor pago é menor 3", Toast.LENGTH_SHORT).show();
            boletoSelecionado.setBoletoPago(false);
            a = true;
        }

        if (resultado == 0) {
            Toast.makeText(this, "  O valor pago esta correto ", Toast.LENGTH_SHORT).show();
            boletoSelecionado.setBoletoPago(true);
            a = true;
        }

        return a;
    }
}