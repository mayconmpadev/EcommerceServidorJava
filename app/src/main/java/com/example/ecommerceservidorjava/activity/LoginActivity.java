package com.example.ecommerceservidorjava.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityLoginBinding;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configClicks();

    }

    public void validaDados() {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {

                ocultaTeclado();


                login(email, senha);

            } else {
                binding.edtSenha.requestFocus();
                binding.edtSenha.setError("Informe uma senha.");
            }
        } else {
            binding.edtEmail.requestFocus();
            binding.edtEmail.setError("Informe seu email.");
        }
    }

    private void login(String email, String senha) {
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getAuth().signInWithEmailAndPassword(
                email, senha
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (FirebaseHelper.getAuth().getCurrentUser().isEmailVerified()) {
                    SPM spm = new SPM(getApplicationContext());
                    spm.setPreferencia("PREFERENCIAS","USUARIO", email);
                    spm.setPreferencia("PREFERENCIAS","SENHA", senha);
                    recuperaUsuario(task.getResult().getUser().getUid());
                } else {
                    // FirebaseHelper.getAuth().signOut();
                    binding.progressBar.setVisibility(View.GONE);
                    String msg = "Um email de confirmação foi enviado para *" + email + "* clique no link " +
                            "de confirmação do email para validar seu cadastro. Caso nao tenha recebido clique em reenviar";

                    dialogRenvioEmailConfirmacao("Reenvio", msg);

                }

            } else {

                Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }

        });
    }

    private void recuperaUsuario(String id) {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("empresas")
                .child(id);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // Usuário
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                }

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dialogRenvioEmailConfirmacao(final String sTitulo, String menssagem) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_padrao_ok_cancelar);
        //instancia os objetos que estão no layout customdialog.xml
        final TextView titulo = dialog.findViewById(R.id.dialog_padrao_text_titulo);
        final TextView msg = dialog.findViewById(R.id.dialog_padrao_text_msg);
        final Button cancelar = dialog.findViewById(R.id.dialog_padrao_btn_esquerda);
        final Button ok = dialog.findViewById(R.id.dialog_padrao_btn_direita);
        final LinearLayout layout = dialog.findViewById(R.id.root);
        ok.setText("Reenviar");
        cancelar.setText("sair");
        layout.setVisibility(View.VISIBLE);


        Util.textoNegrito(menssagem, msg, null);
        titulo.setText(sTitulo);


        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHelper.getAuth().signOut();
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "reenviar", Toast.LENGTH_SHORT).show();
                verificarEmail();
                dialog.dismiss();
            }
        });

        //exibe na tela o dialog
        dialog.show();

    }

    private void verificarEmail() {
        try {
            binding.progressBar.setVisibility(View.GONE);
            final FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();


            if (user != null) {
                user.updateEmail(binding.edtEmail.getText().toString());
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Um e-mail de verificação foi eniado " +
                                    "para o endereço " + user.getEmail(), Toast.LENGTH_LONG).show();

                            FirebaseHelper.getAuth().signOut();
                        } else {
                            Toast.makeText(LoginActivity.this, "Verifique se o e-mail cadastrado está correto!", Toast.LENGTH_LONG).show();
                            FirebaseHelper.getAuth().signOut();
                        }

                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void configClicks() {


        binding.include.ibVoltar.setOnClickListener(view -> finish());

        binding.btnLogin.setOnClickListener(view -> validaDados());

        binding.btnRecuperaSenha.setOnClickListener(view ->
                startActivity(new Intent(this, RecuperarSenhaActivity.class)));

        binding.btnCadastro.setOnClickListener(view -> {
            Intent intent = new Intent(this, CadastroUsuarioActivity.class);
            resultLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String email = result.getData().getStringExtra("email");
                    binding.edtEmail.setText(email);

                }
            }
    );

    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.edtEmail.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

}