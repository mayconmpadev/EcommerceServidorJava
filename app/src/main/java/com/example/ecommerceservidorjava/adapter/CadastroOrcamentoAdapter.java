package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemProdutoOrcamentoBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Produto;

import java.util.List;


public class CadastroOrcamentoAdapter extends RecyclerView.Adapter<CadastroOrcamentoAdapter.MyViewHolder> {


    private final List<Produto> produtoList;
    private final List<ItemVenda> itemVendaList;
    private final Context context;
    private final OnClickLister onClickLister;


    public CadastroOrcamentoAdapter(List<Produto> produtoList, List<ItemVenda> itemVendaList, Context context, OnClickLister onClickLister) {

        this.produtoList = produtoList;
        this.itemVendaList = itemVendaList;
        this.context = context;

        this.onClickLister = onClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemProdutoOrcamentoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Produto produto = produtoList.get(position);
        if (itemVendaList.get(position).getQtd() > 0) {
            holder.binding.btnComprar.setVisibility(View.GONE);
            holder.binding.lytQuantidade.setVisibility(View.VISIBLE);
            holder.binding.textQuantidade.setText(String.valueOf(itemVendaList.get(position).getQtd()));
            holder.binding.lytPlusMinus.setVisibility(View.VISIBLE);


        }else {
            holder.binding.btnComprar.setVisibility(View.VISIBLE);
            holder.binding.lytQuantidade.setVisibility(View.GONE);
            holder.binding.lytPlusMinus.setVisibility(View.GONE);
        }

        holder.binding.textNome.setText(produto.getNome().substring(0, 1).toUpperCase().concat(produto.getNome().substring(1)));
        if (produto.getDesconto().equals("0")) {
            holder.binding.textValorAntigo.setVisibility(View.INVISIBLE);
        }
        holder.binding.textValorNovo.setText(produto.getPrecoVenda());

        Glide.with(context).load(produto.getUrlImagem0()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imagemProduto);
        holder.binding.lytParent.setOnClickListener(v -> onClickLister.onClick(position, produto, "detalhe"));
        holder.binding.btnComprar.setOnClickListener(v -> onClickLister.onClick(position, produto, "mais"));
        holder.binding.btnMais.setOnClickListener(v -> onClickLister.onClick(position, produto, "mais"));
        holder.binding.btnMenos.setOnClickListener(v -> onClickLister.onClick(position, produto, "menos"));

    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemProdutoOrcamentoBinding binding;


        public MyViewHolder(ItemProdutoOrcamentoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(int position, Produto usuario, String operacao);

    }

}
