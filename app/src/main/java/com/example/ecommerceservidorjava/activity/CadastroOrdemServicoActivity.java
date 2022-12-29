package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroOrdemServicoBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CadastroOrdemServicoActivity extends AppCompatActivity {

    private ActivityCadastroOrdemServicoBinding binding;
    private final List<String> caminhoImagens = new ArrayList<>();
    private final List<String> codigoList = new ArrayList<>();
    private final List<OrdemServico> ordemServicoList = new ArrayList<>();
    private int imagemSelecionada;
    private Uri imagemUri_0, imagemUri_1, imagemUri_2;
    private OrdemServico ordemServico = new OrdemServico();
    private OrdemServico ordemServicoSelecionado;
    private boolean editar = false;
    private SPM spm = new SPM(this);
    private Cliente clienteSelecionado;

    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 2) {
                    clienteSelecionado = (Cliente) result.getData().getSerializableExtra("cliente");
                    Toast.makeText(getApplicationContext(), clienteSelecionado.getNome(), Toast.LENGTH_SHORT).show();
                    binding.edtCliente.setText(clienteSelecionado.getNome());
                    binding.editTelefone.setText(clienteSelecionado.getTelefone1());

                } else {
                    Toast.makeText(getApplicationContext(), "falso", Toast.LENGTH_SHORT).show();
                }
            }

    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroOrdemServicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        clicks();
        gerarCodigo();

    }

    private void recuperarIntent() {
        ordemServicoSelecionado = (OrdemServico) getIntent().getSerializableExtra("ordemServiçoSelecionada");
        if (ordemServicoSelecionado != null) {
            binding.btnSalvar.setText("Salvar");
            editar = true;
            binding.edtCliente.setText(ordemServicoSelecionado.getIdCliente().getNome());
            binding.editTelefone.setText(ordemServicoSelecionado.getTelefone());
            binding.editNumeroOs.setText(ordemServicoSelecionado.getNumeroOs());
            binding.editEquipamento.setText(ordemServicoSelecionado.getEquipamento());
            binding.editMarca.setText(ordemServicoSelecionado.getMarca());
            binding.editModelo.setText(ordemServicoSelecionado.getModelo());
            binding.editDefeitoRelatado.setText(ordemServicoSelecionado.getDefeitoRelatado());
            binding.editObservacao.setText(ordemServicoSelecionado.getObservacao());
            binding.checkGarantia.setChecked(ordemServicoSelecionado.isGarantia());
            binding.editNumeroOs.setEnabled(false);
            clienteSelecionado = ordemServicoSelecionado.getIdCliente();

            if (ordemServicoSelecionado.getUrlImagem0() != null) {
                binding.imageFake0.setVisibility(View.GONE);
                Glide.with(this).load(ordemServicoSelecionado.getUrlImagem0()).into(binding.imagemProduto0);
                caminhoImagens.add(ordemServicoSelecionado.getUrlImagem0());
                binding.cardViewImage1.setVisibility(View.VISIBLE);
            }

            if (ordemServicoSelecionado.getUrlImagem1() != null) {
                binding.imageFake1.setVisibility(View.GONE);
                Glide.with(this).load(ordemServicoSelecionado.getUrlImagem1()).into(binding.imagemProduto1);
                caminhoImagens.add(ordemServicoSelecionado.getUrlImagem1());
                binding.cardViewImage2.setVisibility(View.VISIBLE);
            }
            if (ordemServicoSelecionado.getUrlImagem2() != null) {
                binding.imageFake2.setVisibility(View.GONE);
                Glide.with(this).load(ordemServicoSelecionado.getUrlImagem2()).into(binding.imagemProduto2);
                caminhoImagens.add(ordemServicoSelecionado.getUrlImagem2());
            }

        } else {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            ordemServico = new OrdemServico();
            ordemServico.setId(databaseReference.push().getKey());
        }
    }


    public void validaDados() {
        String cliente = binding.edtCliente.getText().toString();
        String telefone = binding.editTelefone.getText().toString();
        String numeroOs = binding.editNumeroOs.getText().toString();
        String equipamento = binding.editEquipamento.getText().toString();
        String marca = binding.editMarca.getText().toString();
        String modelo = binding.editModelo.getText().toString();
        String defeitoRelatado = binding.editDefeitoRelatado.getText().toString();
        String observação = binding.editObservacao.getText().toString();


        if (imagemUri_0 == null & !editar) {
            Toast.makeText(getApplicationContext(), "Selecione todas as imagens", Toast.LENGTH_SHORT).show();
        } else if (cliente.isEmpty()) {
            binding.edtCliente.setError("preencha o campo");
            binding.edtCliente.requestFocus();
        } else if (equipamento.isEmpty()) {
            binding.editEquipamento.setError("preencha o campo");
            binding.editEquipamento.requestFocus();
        } else if (modelo.isEmpty()) {
            binding.editModelo.setError("preencha o campo");
            binding.editModelo.requestFocus();
        } else if (defeitoRelatado.isEmpty()) {
            binding.editDefeitoRelatado.setError("preencha o campo");
            binding.editDefeitoRelatado.requestFocus();
        } else {

            if (editar) {
                ordemServico.setId(ordemServicoSelecionado.getId());
            }


            binding.progressBar3.setVisibility(View.VISIBLE);
            clienteSelecionado.setNome(cliente);
            ordemServico.setIdCliente(clienteSelecionado);
            ordemServico.setTelefone(telefone);
            ordemServico.setNumeroOs(numeroOs);
            ordemServico.setEquipamento(equipamento);
            ordemServico.setMarca(marca);
            ordemServico.setModelo(modelo);
            ordemServico.setDefeitoRelatado(defeitoRelatado);
            ordemServico.setObservacao(observação);
            ordemServico.setGarantia(binding.checkGarantia.isChecked());
            ordemServico.setStatus("Em analise");
            ordemServico.setDataEntrada(String.valueOf(Timestamp.getUnixTimestamp()));
            if (imagemUri_0 != null) {
                caminhoImagens.set(0, "");
                salvarDadosImagem(ordemServico, imagemUri_0, 0);
            }
            if (imagemUri_1 != null) {
                caminhoImagens.set(1, "");
                salvarDadosImagem(ordemServico, imagemUri_1, 1);
            }
            if (imagemUri_2 != null) {
                caminhoImagens.set(2, "");
                salvarDadosImagem(ordemServico, imagemUri_2, 2);
            }
            if (imagemUri_0 == null) {
                salvarDados();
            }
        }
    }

    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvarDadosImagem(OrdemServico ordemServico, Uri sUri, int index) {
        binding.progressBar3.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        StorageReference storageReferencere = FirebaseHelper.getStorageReference().child("empresas")
                .child(caminho).child("imagens").child("ordens_servicos").child(ordemServico.getId()).child("imagem" + index);


        Glide.with(this).asBitmap().load(sUri).apply(new RequestOptions().override(1024, 768))
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
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        UploadTask uploadTask = storageReferencere.putStream(inputStream);
                        uploadTask.continueWithTask(task -> storageReferencere.getDownloadUrl()).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();

                                caminhoImagens.set(index, uri.toString());

                                if (!caminhoImagens.contains("")) {
                                    salvarDados();
                                }


                            } else {

                                Toast.makeText(getApplicationContext(), "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                            }

                        });

                        return false;
                    }
                }).submit();

    }

    public void salvarDados() {

        ordemServico.setUrlImagem0(caminhoImagens.get(0));
        if (caminhoImagens.size() > 1) {
            ordemServico.setUrlImagem1(caminhoImagens.get(1));
        }
        if (caminhoImagens.size() > 2) {
            ordemServico.setUrlImagem2(caminhoImagens.get(2));
        }


        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("ordens_servicos").child(ordemServico.getId());

        databaseReference.setValue(ordemServico).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "erro de foto", Toast.LENGTH_SHORT).show();
            }

        });

    }


    public void gerarCodigo() {
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""))).child("ordens_servicos").orderByChild("numeroOs");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ordemServicoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        OrdemServico ordemServico = ds.getValue(OrdemServico.class);
                        codigoList.add(ordemServico.getNumeroOs());
                    }

                    if (!editar) {
                        binding.editNumeroOs.setText(String.format("%05d", Integer.parseInt(Collections.max(codigoList)) + 1));
                    }


                } else {
                    binding.editNumeroOs.setText(String.format("%05d", 1));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void clicks() {

        binding.fbtPesquisa.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaClienteActivity.class);
            intent.putExtra("checkout", true);
            resultLauncher.launch(intent);
        });
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        if (editar) {
            binding.include.textTitulo.setText("Editar");
        } else {
            binding.include.textTitulo.setText("Novo");
        }

        binding.imagemProduto0.setOnClickListener(view -> {
            imagemSelecionada = 0;
            chamarImagens();
        });
        binding.imagemProduto1.setOnClickListener(view -> {
            imagemSelecionada = 1;
            chamarImagens();
        });
        binding.imagemProduto2.setOnClickListener(view -> {
            imagemSelecionada = 2;
            chamarImagens();
        });

        binding.btnSalvar.setOnClickListener(view -> validaDados());
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
                if (imagemSelecionada == 0) {
                    imagemUri_0 = result.getUri();
                    binding.imagemProduto0.setImageURI(imagemUri_0);
                    binding.imageFake0.setVisibility(View.GONE);
                    binding.cardViewImage1.setVisibility(View.VISIBLE);
                    if (caminhoImagens.size() == 0) {
                        caminhoImagens.add("");
                    }

                } else if (imagemSelecionada == 1) {
                    imagemUri_1 = result.getUri();
                    binding.imagemProduto1.setImageURI(imagemUri_1);
                    binding.imageFake1.setVisibility(View.GONE);
                    binding.cardViewImage2.setVisibility(View.VISIBLE);
                    if (caminhoImagens.size() == 1) {
                        caminhoImagens.add("");
                    }
                } else {
                    imagemUri_2 = result.getUri();
                    binding.imagemProduto2.setImageURI(imagemUri_2);
                    binding.imageFake2.setVisibility(View.GONE);
                    if (caminhoImagens.size() == 2) {
                        caminhoImagens.add("");
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == 2) {
                Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT).show();
            }
        } else {

            clienteSelecionado = (Cliente) data.getSerializableExtra("cliente");
            Toast.makeText(getApplicationContext(), clienteSelecionado.getNome(), Toast.LENGTH_SHORT).show();
            binding.edtCliente.setText(clienteSelecionado.getNome());
            binding.editTelefone.setText(clienteSelecionado.getTelefone1());


        }
    }

}