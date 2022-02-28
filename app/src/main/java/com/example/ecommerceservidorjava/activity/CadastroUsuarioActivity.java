package com.example.ecommerceservidorjava.activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroUsuarioBinding;
import com.example.ecommerceservidorjava.model.Usuario;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Util;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CadastroUsuarioActivity extends AppCompatActivity {
    private ActivityCadastroUsuarioBinding binding;
    boolean senha = true;
    boolean confirmaSenha = true;
    SPM spm = new SPM(this);
    Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configClicks();
    }

    public void validaDados() {
        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String telefone = binding.edtTelefone.getMasked();
        String senha = binding.edtSenha.getText().toString().trim();
        String confirmaSenha = binding.edtConfirmaSenha.getText().toString().trim();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (telefone.length() == 15) {
                        if (!senha.isEmpty()) {
                            if (!confirmaSenha.isEmpty()) {
                                if (senha.equals(confirmaSenha)) {

                                    binding.progressBar.setVisibility(View.VISIBLE);

                                    Usuario usuario = new Usuario();
                                    usuario.setNome(nome);
                                    usuario.setEmail(email);
                                    usuario.setTelefone(telefone);
                                    usuario.setSenha(senha);
                                    usuario.setPerfil(binding.spinner.getSelectedItem().toString());
                                    usuario.setStatus(binding.checkbox.isChecked());

                                    criarConta(usuario);

                                } else {
                                    binding.edtConfirmaSenha.requestFocus();
                                    binding.edtConfirmaSenha.setError("Senha não confere.");
                                }
                            } else {
                                binding.edtConfirmaSenha.requestFocus();
                                binding.edtConfirmaSenha.setError("Confirme sua senha.");
                            }
                        } else {
                            binding.edtSenha.requestFocus();
                            binding.edtSenha.setError("Informe uma senha.");
                        }
                    } else {
                        binding.edtTelefone.requestFocus();
                        binding.edtTelefone.setError("Fomato do telefone inválido.");
                    }
                } else {
                    binding.edtTelefone.requestFocus();
                    binding.edtTelefone.setError("Informe um número de telefone.");
                }
            } else {
                binding.edtEmail.requestFocus();
                binding.edtEmail.setError("Informe seu email.");
            }
        } else {
            binding.edtNome.requestFocus();
            binding.edtNome.setError("Informe seu nome.");
        }
    }
    //---------------------------------------------------- SALVAR DADOS E IMAGEM -----------------------------------------------------------------
    private void criarConta(Usuario usuario) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();

                usuario.setId(id);
                verificarEmail(usuario);


            } else {
                Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.INVISIBLE);
            }

        });
    }

    //---------------------------------------------------- SALVAR -----------------------------------------------------------------
    public void salvar(Usuario usuario) {
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("imagens").child("usuarios").child(usuario.getId());

        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("usuarios").child(usuario.getId());

        Glide.with(this).asBitmap().load(resultUri).apply(new RequestOptions().override(1024, 768))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                        resource.compress(Bitmap.CompressFormat.JPEG, 70, bytes);

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toByteArray());

                        try {
                            bytes.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        UploadTask uploadTask = storageReferencere.putStream(inputStream);

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {// teste

                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                return storageReferencere.getDownloadUrl();
                            }


                        }).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                usuario.setFoto(uri.toString());
                                databaseReference.setValue(usuario).addOnCompleteListener(task1 -> {


                                    if (task1.isSuccessful()) {

                                        binding.imagemFoto.setImageURI(resultUri);
                                        FirebaseHelper.getAuth().signOut();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();


                                    } else {

                                        storageReferencere.delete(); //apaga a imagem previamente salva no banco
                                        Toast.makeText(CadastroUsuarioActivity.this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

                                    }
                                });


                            } else {

                                Toast.makeText(CadastroUsuarioActivity.this, "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        });

                        return false;
                    }
                }).submit();


    }
    //---------------------------------------------------- ENVIA UM EMAIL PARA O EMAIL CADASTRADO -----------------------------------------------------------------
    private void verificarEmail(Usuario usuario) {
        try {

            final FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();

            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                     //   dialogSenhaPadrao("Confirmção", "Um email de confirmação foi enviado para *" + binding.edtEmail.getText().toString() + "*"
                          //      + ", clique no link para validar o cadastro", usuario);
                        if (FirebaseHelper.getAuth().getCurrentUser() != null) {
                            salvar(usuario);


                        }

                    } else {
                        Toast.makeText(CadastroUsuarioActivity.this, "Verifique se o email cadastrado esta correto!", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.INVISIBLE);
                    }

                });
            }

        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void dialogSenhaPadrao(final String sTitulo, String menssagem, Usuario usuario) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_padrao);
        dialog.setCanceledOnTouchOutside(false);
        //instancia os objetos que estão no layout customdialog.xml
        final TextView titulo = dialog.findViewById(R.id.dialog_padrao_text_titulo);
        final TextView msg = dialog.findViewById(R.id.dialog_padrao_text_msg);
        final Button ok = dialog.findViewById(R.id.dialog_padrao_btn_direita);
        ok.setText("OK");
        Util.textoNegrito(menssagem, msg, null);
        titulo.setText(sTitulo);

        ok.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra("email", usuario.getEmail());
            setResult(RESULT_OK, intent);
            finish();
        });

        //exibe na tela o dialog
        dialog.show();

    }
    //---------------------------------------------------- CLIQUES -----------------------------------------------------------------
    private void configClicks() {
        binding.imgSenha.setTag("imagem");// tag para saber quem é o imageview
        binding.imgConfirmaSenha.setTag("imagem2");
        binding.cardFoto.setOnClickListener(view -> chamarImagens());
        binding.include.ibVoltar.setOnClickListener(view -> finish());
        binding.btnLogin.setOnClickListener(view -> finish());
        binding.btnCriarConta.setOnClickListener(view -> validaDados());
        binding.imgSenha.setOnClickListener(view -> mostrarSenha(senha, binding.imgSenha, binding.edtSenha));
        binding.imgConfirmaSenha.setOnClickListener(view -> mostrarSenha(confirmaSenha, binding.imgConfirmaSenha, binding.edtConfirmaSenha));
    }
    //---------------------------------------------------- MOSTRAR SENHA -----------------------------------------------------------------
    private void mostrarSenha(boolean status, ImageView imageView, EditText editText) {
        if (status) {
            imageView.setImageResource(R.drawable.ic_action_visivel);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            imageView.setImageResource(R.drawable.ic_action_nao_visivel);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        if (imageView.getTag().equals("imagem")) { //alterar o boolean especifico conforme o imageview
            senha = !senha;
        } else {
            confirmaSenha = !confirmaSenha;
        }
    }
    //---------------------------------------------------- RECORTE DE IMAGEM -----------------------------------------------------------------
    private void chamarImagens() {
        CropImage.activity() // chama intenção de busca a imagem
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                binding.imagemFoto.setImageURI(resultUri);
                binding.imageFake.setVisibility(View.GONE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}