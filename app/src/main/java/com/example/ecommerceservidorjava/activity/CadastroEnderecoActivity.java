package com.example.ecommerceservidorjava.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.databinding.ActivityCadastroEnderecoBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DatabaseReference;

public class CadastroEnderecoActivity extends AppCompatActivity {
    private ActivityCadastroEnderecoBinding binding;
    private SPM spm = new SPM(this);
    private Endereco enderecoSelecionado;
    private Endereco endereco;
    private Cliente clienteSelecionado;
    private boolean editar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroEnderecoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configClicks();

    }

    private void recuperarIntent() {
        enderecoSelecionado = (Endereco) getIntent().getSerializableExtra("enderecoSelecionado");
        clienteSelecionado = (Cliente) getIntent().getSerializableExtra("clienteSelecionado");
        if (enderecoSelecionado != null) {
            binding.btnCriarConta.setText("Editar conta");
            editar = true;

            binding.edtCep.setText(enderecoSelecionado.getCep());
            binding.edtNome.setText(enderecoSelecionado.getNomeEndereco());
            binding.edtBairro.setText(enderecoSelecionado.getBairro());
            binding.edtUf.setText(enderecoSelecionado.getUf());
            binding.edtMunicipio.setText(enderecoSelecionado.getLocalidade());
            binding.edtNumero.setText(enderecoSelecionado.getNumero());
            binding.edtLogradouro.setText(enderecoSelecionado.getLogradouro());
            endereco.setId(enderecoSelecionado.getId());
        }else {
            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference();
            endereco = new Endereco();
            endereco.setId(databaseReference.push().getKey());

        }

        if (clienteSelecionado != null){
            editar = false;
        }
    }

    public void validaDadosSalvar() {
        String nome = binding.edtNome.getText().toString().trim();
        String cep = binding.edtCep.getUnMasked();
        String uf = binding.edtUf.getText().toString();
        String numero = binding.edtNumero.getText().toString();
        String bairro = binding.edtBairro.getText().toString().trim();
        String municipio = binding.edtMunicipio.getText().toString().trim();
        String logradouro = binding.edtLogradouro.getText().toString().trim();
        String observacao = binding.edtObservacao.getText().toString().trim();


        if (!nome.isEmpty()) {
            if (!cep.isEmpty() & cep.length() == 8) {
                if (!uf.isEmpty() & uf.length() == 2) {
                    if (!bairro.isEmpty()) {
                        if (!municipio.isEmpty()) {
                            if (!logradouro.isEmpty()) {

                                binding.progressBar.setVisibility(View.VISIBLE);
                                endereco.setNomeEndereco(nome);
                                endereco.setCep(cep);
                                endereco.setUf(uf);
                                endereco.setNumero(numero);
                                endereco.setBairro(bairro);
                                endereco.setLocalidade(municipio);
                                endereco.setLogradouro(logradouro);
                                endereco.setObservacao(observacao);



                                salvarDados(endereco);
                            } else {
                                binding.edtLogradouro.requestFocus();
                                binding.edtLogradouro.setError("Senha não confere.");
                            }
                        } else {
                            binding.edtMunicipio.requestFocus();
                            binding.edtMunicipio.setError("Confirme sua senha.");
                        }
                    } else {
                        binding.edtBairro.requestFocus();
                        binding.edtBairro.setError("Informe uma senha.");
                    }
                } else {
                    binding.edtUf.requestFocus();
                    binding.edtUf.setError("Fomato do telefone inválido.");
                }
            } else {
                binding.edtCep.requestFocus();
                binding.edtCep.setError("CEP nao confere.");
            }
        } else {
            binding.edtNome.requestFocus();
            binding.edtNome.setError("Informe seu email.");
        }
    }

    private void salvarDados(Endereco endereco) {
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("enderecos").child(clienteSelecionado.getId()).child(endereco.getId());
        databaseReference.setValue(endereco).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {

                finish();

            } else {
                Toast.makeText(getApplicationContext(), "Erro ao cadastrar", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //---------------------------------------------------- CLIQUES -----------------------------------------------------------------
    private void configClicks() {
        binding.btnCep.setOnClickListener(view -> validaDadosSalvar());
        binding.btnCriarConta.setOnClickListener(view -> validaDadosSalvar());
    }


}