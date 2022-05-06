package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroClienteBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Validacao;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.Mask;
import com.santalu.maskara.MaskChangedListener;
import com.santalu.maskara.MaskStyle;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CadastroClienteActivity extends AppCompatActivity {
    private ActivityCadastroClienteBinding binding;
    boolean senha = true;
    boolean confirmaSenha = true;
    private SPM spm = new SPM(this);
    private Uri resultUri;
    private Cliente clienteSelecionado;
    private Cliente cliente;
    private boolean editar = false;
    private boolean mascara = true;
    private boolean mascara2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configClicks();

    }
    //---------------------------------------------------- RECUPERAR OBJETO -----------------------------------------------------------------
    private void recuperarIntent() {
        clienteSelecionado = (Cliente) getIntent().getSerializableExtra("clienteSelecionado");
        if (clienteSelecionado != null) {
            binding.btnCriarConta.setText("Editar conta");
            editar = true;
            binding.linearCamposNaoEditados.setVisibility(View.GONE);
            binding.edtSenha.setText("123456");
            binding.edtConfirmaSenha.setText("123456");
            binding.edtNome.setText(clienteSelecionado.getNome());
            binding.edtEmail.setText(clienteSelecionado.getEmail());
            binding.edtTelefone1.setText(clienteSelecionado.getTelefone1());
            binding.edtTelefone2.setText(clienteSelecionado.getTelefone2());
            binding.edtDocumento.setText(clienteSelecionado.getDocumento());
            binding.edtObservacao.setText(clienteSelecionado.getObservacao());
            binding.checkbox.setChecked(clienteSelecionado.isStatus());
            binding.imageFake.setVisibility(View.GONE);
            Glide.with(this).load(clienteSelecionado.getUrlImagem()).into(binding.imagemFoto);

            String sPerfil = clienteSelecionado.getPerfil();
            String[] arrayPerfil = getResources().getStringArray(R.array.perfil_cliente);
            for (int i = 0; i < arrayPerfil.length; i++) {
                if (arrayPerfil[i].equals(sPerfil)) {
                    binding.spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    //---------------------------------------------------- VALIDAR DADOS -----------------------------------------------------------------
    public void validaDadosSalvar() {
        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String telefone = binding.edtTelefone1.getText().toString();
        String telefone2 = binding.edtTelefone2.getText().toString();
        String documento = binding.edtDocumento.getUnMasked();
        String senha = binding.edtSenha.getText().toString().trim();
        String confirmaSenha = binding.edtConfirmaSenha.getText().toString().trim();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (telefone.length() == 15) {
                        if (!senha.isEmpty()) {
                            if (!confirmaSenha.isEmpty()) {
                                if (!confirmaSenha.isEmpty()) {
                                    if (verificarDocumento(documento)) {

                                        binding.progressBar.setVisibility(View.VISIBLE);

                                        cliente = new Cliente();
                                        cliente.setNome(nome);
                                        cliente.setEmail(email);
                                        cliente.setTelefone1(telefone);
                                        cliente.setTelefone2(telefone2);
                                        cliente.setSenha(senha);
                                        cliente.setDocumento(binding.edtDocumento.getMasked());
                                        cliente.setObservacao(binding.edtObservacao.getText().toString());
                                        cliente.setPerfil(binding.spinner.getSelectedItem().toString());
                                        cliente.setStatus(binding.checkbox.isChecked());


                                        if (editar) {
                                            cliente.setUrlImagem(clienteSelecionado.getUrlImagem());
                                            cliente.setId(clienteSelecionado.getId());
                                            if (resultUri != null) {
                                                editarDadosImagem(cliente);
                                            } else {
                                                editarDados(cliente);
                                            }
                                        } else {
                                            criarConta(cliente);
                                        }


                                    } else {
                                        binding.edtDocumento.requestFocus();
                                        binding.edtDocumento.setError("documento invalido");
                                    }
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
                        binding.edtTelefone1.requestFocus();
                        binding.edtTelefone1.setError("Fomato do telefone inválido.");
                    }
                } else {
                    binding.edtTelefone1.requestFocus();
                    binding.edtTelefone1.setError("Informe um número de telefone.");
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


    //---------------------------------------------------- CRIAR CONTA -----------------------------------------------------------------
    private void criarConta(Cliente cliente) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(
                cliente.getEmail(), cliente.getSenha()
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();

                cliente.setId(id);
                verificarEmail(cliente);


            } else {
                Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.INVISIBLE);
            }

        });
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvarDadosImagem(Cliente cliente) {
        if (resultUri == null){
         resultUri = Uri.parse("android.resource://com.example.ecommerceservidorjava/drawable/user_123");
        }
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("clientes").child(cliente.getId());

        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("clientes").child(cliente.getId());

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
                        uploadTask.continueWithTask(task -> storageReferencere.getDownloadUrl()).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                cliente.setUrlImagem(uri.toString());
                                databaseReference.setValue(cliente).addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {
                                        binding.imageFake.setVisibility(View.GONE);
                                        binding.imagemFoto.setImageURI(resultUri);
                                        FirebaseHelper.getAuth().signOut();
                                        finishAffinity();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        storageReferencere.delete(); //apaga a imagem previamente salva no banco
                                        Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {

                                Toast.makeText(getApplicationContext(), "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        });

                        return false;
                    }
                }).submit();


    }
    //---------------------------------------------------- EDITAR DADOS -----------------------------------------------------------------
    private void editarDados(Cliente cliente) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("clientes").child(cliente.getId());
        databaseReference.setValue(cliente).addOnCompleteListener(task1 -> {


            if (task1.isSuccessful()) {

                binding.imagemFoto.setImageURI(resultUri);
                finish();


            } else {

                Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void editarDadosImagem(Cliente cliente) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("clientes").child(cliente.getId());

        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("clientes").child(cliente.getId());

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
                        uploadTask.continueWithTask(task -> storageReferencere.getDownloadUrl()).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                cliente.setUrlImagem(uri.toString());
                                databaseReference.setValue(cliente).addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {
                                        binding.imagemFoto.setImageURI(resultUri);
                                        finish();
                                    } else {
                                        storageReferencere.delete(); //apaga a imagem previamente salva no banco
                                        Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {

                                Toast.makeText(getApplicationContext(), "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        });

                        return false;
                    }
                }).submit();


    }


    //---------------------------------------------------- ENVIA UM EMAIL PARA O EMAIL CADASTRADO -----------------------------------------------------------------
    private void verificarEmail(Cliente cliente) {
        try {

            final FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (FirebaseHelper.getAuth().getCurrentUser() != null) {
                            salvarDadosImagem(cliente);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Verifique se o email cadastrado esta correto!", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    //---------------------------------------------------- CLIQUES -----------------------------------------------------------------
    private void configClicks() {
        binding.imgSenha.setTag("imagem");// tag para saber quem é o imageview
        binding.imgConfirmaSenha.setTag("imagem2");
        binding.cardFoto.setOnClickListener(view -> chamarImagens());
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        if (editar) {
            binding.include.textTitulo.setText("Editar");
        } else {
            binding.include.textTitulo.setText("Novo");
        }

        binding.btnCriarConta.setOnClickListener(view -> validaDadosSalvar());
        binding.imgSenha.setOnClickListener(view -> mostrarSenha(senha, binding.imgSenha, binding.edtSenha));
        binding.imgConfirmaSenha.setOnClickListener(view -> mostrarSenha(confirmaSenha, binding.imgConfirmaSenha, binding.edtConfirmaSenha));
        binding.edtDocumento.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.edtDocumento.getUnMasked().length() == 11 & mascara) {
                    if (mascara2) {
                        mascara = false;
                        mascaraCnpj();
                    } else {
                        mascara2 = true;
                    }

                } else if (binding.edtDocumento.getUnMasked().length() == 10 & !mascara) {
                    mascara = true;
                    mascara2 = false;
                    mascaraCpf();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void mascaraCnpj() {

        Mask mask = new Mask("__.___.___/____-__", '_', MaskStyle.COMPLETABLE);
        MaskChangedListener listener = new MaskChangedListener(mask);
        binding.edtDocumento.addTextChangedListener(listener);
    }

    private void mascaraCpf() {

        Mask mask = new Mask("___.___.___-__", '_', MaskStyle.COMPLETABLE);
        MaskChangedListener listener = new MaskChangedListener(mask);
        binding.edtDocumento.addTextChangedListener(listener);
    }

    private boolean verificarDocumento(String documento) {
        boolean resultado = true;
        if (!documento.isEmpty()) {
            if (Validacao.validarCpf(documento) || Validacao.validarCnpj(documento)) {
                resultado = true;
            } else {
                resultado = false;
            }
        }
        return resultado;
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