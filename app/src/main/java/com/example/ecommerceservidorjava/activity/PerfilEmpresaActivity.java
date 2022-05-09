package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.example.ecommerceservidorjava.api.CEPService;
import com.example.ecommerceservidorjava.databinding.ActivityPerfilEmpresaBinding;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.PerfilEmpresa;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Validacao;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilEmpresaActivity extends AppCompatActivity {
    ActivityPerfilEmpresaBinding binding;
    private PerfilEmpresa perfilEmpresa;
    private Endereco endereco;
    private SPM spm = new SPM(this);
    private boolean editar = false;
    private boolean mascara = true;
    private boolean mascara2 = false;
    private boolean digitacao = false;
    private Uri resultUri;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilEmpresaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.include.textTitulo.setText("Perfil Empresa");

        iniciaRetrofit();
        recuperarPerfil();
        configClicks();

    }

    private void iniciaRetrofit() {
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void recuperarPerfil() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));


        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("perfil_empresa");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    perfilEmpresa = snapshot.getValue(PerfilEmpresa.class);
                    binding.imageFake.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(perfilEmpresa.getUrlImagem()).into(binding.imagemFoto);
                    binding.edtNome.setText(perfilEmpresa.getNome());
                    binding.edtEmail.setText(perfilEmpresa.getEmail());
                    binding.edtTelefone1.setText(perfilEmpresa.getTelefone1());
                    binding.edtTelefone2.setText(perfilEmpresa.getTelefone2());
                    binding.edtDocumento.setText(perfilEmpresa.getDocumento());
                    tipodocumento();
                    binding.edtDocumento.setText(perfilEmpresa.getDocumento());
                    binding.edtCep.setText(perfilEmpresa.getEndereco().getCep());
                    binding.edtUf.setText(perfilEmpresa.getEndereco().getUf());
                    binding.edtNumero.setText(perfilEmpresa.getEndereco().getNumero());
                    binding.edtBairro.setText(perfilEmpresa.getEndereco().getBairro());
                    binding.edtMunicipio.setText(perfilEmpresa.getEndereco().getLocalidade());
                    binding.edtLogradouro.setText(perfilEmpresa.getEndereco().getLogradouro());
                    binding.edtObservacao.setText(perfilEmpresa.getEndereco().getComplemento());

                } else {
                    resultUri = Uri.parse("android.resource://com.example.ecommerceservidorjava/drawable/user_123");
                    binding.progressBar.setVisibility(View.GONE);
                    perfilEmpresa = new PerfilEmpresa();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void tipodocumento() {

        if (perfilEmpresa.getDocumento().length() == 18) {
            mascaraCnpj();

        } else {
            mascaraCpf();
        }
    }

    //---------------------------------------------------- VALIDAR DADOS -----------------------------------------------------------------
    public void validaDadosSalvar() {
        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String telefone = binding.edtTelefone1.getText().toString();
        String telefone2 = binding.edtTelefone2.getText().toString();
        String documento = binding.edtDocumento.getMasked();
        String cep = binding.edtCep.getText().toString();
        String estado = binding.edtUf.getText().toString();
        String numero = binding.edtNumero.getText().toString();
        String bairro = binding.edtBairro.getText().toString();
        String municipio = binding.edtMunicipio.getText().toString();
        String logradouro = binding.edtLogradouro.getText().toString();
        String observacao = binding.edtObservacao.getText().toString();


        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (telefone.length() == 15) {
                        if (!cep.isEmpty()) {
                            if (cep.length() == 9) {
                                if (!estado.isEmpty()) {
                                    if (!bairro.isEmpty()) {
                                        if (!municipio.isEmpty()) {
                                            if (!logradouro.isEmpty()) {
                                                if (verificarDocumento(documento)) {

                                                    binding.progressBar.setVisibility(View.VISIBLE);


                                                    perfilEmpresa.setId("perfil_empresa");
                                                    perfilEmpresa.setNome(nome);
                                                    perfilEmpresa.setEmail(email);
                                                    perfilEmpresa.setTelefone1(telefone);
                                                    perfilEmpresa.setTelefone2(telefone2);
                                                    perfilEmpresa.setDocumento(binding.edtDocumento.getMasked());

                                                    endereco = new Endereco();
                                                    endereco.setCep(binding.edtCep.getMasked());
                                                    endereco.setUf(binding.edtUf.getText().toString());
                                                    endereco.setNumero(binding.edtNumero.getText().toString());
                                                    endereco.setBairro(binding.edtBairro.getText().toString());
                                                    endereco.setLocalidade(binding.edtMunicipio.getText().toString());
                                                    endereco.setLogradouro(binding.edtLogradouro.getText().toString());
                                                    endereco.setComplemento(binding.edtObservacao.getText().toString());
                                                    perfilEmpresa.setEndereco(endereco);
                                                    if (resultUri != null) {
                                                        salvarDadosImagem(perfilEmpresa);
                                                    } else {
                                                        editarDados(perfilEmpresa);
                                                    }


                                                } else {
                                                    binding.edtDocumento.requestFocus();
                                                    binding.edtDocumento.setError("documento invalido");
                                                }


                                            } else {
                                                binding.edtUf.requestFocus();
                                                binding.edtUf.setError("UF não pode ser vazio.");
                                            }
                                        } else {
                                            binding.edtCep.requestFocus();
                                            binding.edtCep.setError("CEP invalido");
                                        }
                                    } else {
                                        binding.edtCep.requestFocus();
                                        binding.edtCep.setError("CEP não pode ser vazio");
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
            }
        }
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvarDadosImagem(PerfilEmpresa objeto) {

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("perfil_empresa").child(objeto.getId());

        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("perfil_empresa");

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
                                objeto.setUrlImagem(uri.toString());
                                databaseReference.setValue(objeto).addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {
                                        binding.imageFake.setVisibility(View.GONE);
                                        binding.imagemFoto.setImageURI(resultUri);
                                        Toast.makeText(getApplicationContext(), "Salvo com sucesso", Toast.LENGTH_SHORT).show();
                                    } else {
                                        storageReferencere.delete(); //apaga a imagem previamente salva no banco
                                        Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {

                                Toast.makeText(getApplicationContext(), "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        });

                        return false;
                    }
                }).submit();


    }

    //---------------------------------------------------- EDITAR DADOS -----------------------------------------------------------------
    private void editarDados(PerfilEmpresa perfilEmpresa) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("perfil_empresa");
        databaseReference.setValue(perfilEmpresa).addOnCompleteListener(task1 -> {


            if (task1.isSuccessful()) {

                binding.imagemFoto.setImageURI(resultUri);
                finish();


            } else {

                Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

            }
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void buscarCEP() {
        String cep = binding.edtCep.getText().toString().replace("-", "").replaceAll("_", "");
        Toast.makeText(getApplicationContext(), cep, Toast.LENGTH_SHORT).show();

        if (cep.length() == 8) {

            //ocultaTeclado();

            binding.progressBar.setVisibility(View.VISIBLE);

            CEPService cepService = retrofit.create(CEPService.class);
            Call<Endereco> call = cepService.recuperarCEP(cep);

            call.enqueue(new Callback<Endereco>() {
                @Override
                public void onResponse(@NonNull Call<Endereco> call, @NonNull Response<Endereco> response) {
                    if (response.isSuccessful()) {


                        Endereco endereco = response.body();

                        if (endereco != null) {
                            if (endereco.getLocalidade() != null) {

                                binding.edtBairro.setText(endereco.getBairro());
                                binding.edtMunicipio.setText(endereco.getLocalidade());
                                binding.edtUf.setText(endereco.getUf());
                                binding.edtLogradouro.setText(endereco.getLogradouro());

                            } else {
                                Toast.makeText(getBaseContext(), "Não foi possível recuperar o endereço.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "Não foi possível recuperar o endereço.", Toast.LENGTH_SHORT).show();
                        }

                        binding.progressBar.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure(@NonNull Call<Endereco> call, @NonNull Throwable t) {
                    Toast.makeText(getBaseContext(), "Não foi possível recuperar o endereço.", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                }
            });

        } else {
            Toast.makeText(this, "Formato do CEP inválido.", Toast.LENGTH_SHORT).show();
        }
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

    private void configClicks() {
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        binding.cardFoto.setOnClickListener(view -> chamarImagens());
        binding.btnCriarConta.setOnClickListener(view -> validaDadosSalvar());
        binding.btnCep.setOnClickListener(view -> buscarCEP());
        binding.edtDocumento.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    digitacao = true;
                }
            }
        });
        binding.edtDocumento.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (digitacao)
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