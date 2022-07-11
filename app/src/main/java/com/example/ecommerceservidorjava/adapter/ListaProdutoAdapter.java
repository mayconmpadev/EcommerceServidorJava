package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaUsuarioBinding;
import com.example.ecommerceservidorjava.databinding.ItemProdutoAdapterBinding;
import com.example.ecommerceservidorjava.model.Produto;
import com.google.firebase.database.collection.LLRBNode;


import java.util.List;


public class ListaProdutoAdapter extends RecyclerView.Adapter<ListaProdutoAdapter.MyViewHolder> {

    private final int layout;
    private final List<Produto> produtoList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaProdutoAdapter(int layout, List<Produto> produtoList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.produtoList = produtoList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemProdutoAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Produto produto = produtoList.get(position);

        holder.binding.txtNomeProduto.setText(produto.getNome().substring(0, 1).toUpperCase().concat(produto.getNome().substring(1)));
        if (produto.getDesconto().equals("0")){
            holder.binding.txtDescontoProduto.setVisibility(View.INVISIBLE);
        }else{
            holder.binding.txtDescontoProduto.setVisibility(View.VISIBLE);
            holder.binding.txtDescontoProduto.setText("-"+ produto.getDesconto()+ "%");
        }
        if (produto.getStatus().equals("rascunho")){
            holder.binding.cardView.setForeground(ContextCompat.getDrawable(context,R.color.color_fundo));
        }
        if (Integer.parseInt(produto.getQuantidadeMinima()) > Integer.parseInt(produto.getQuantidadeEtoque())){
            holder.binding.cardView.setForeground(ContextCompat.getDrawable(context,R.color.color_fundo));
        }else {
            holder.binding.cardView.setForeground(ContextCompat.getDrawable(context,R.color.transparente));
        }
        holder.binding.txtValorProduto.setText(produto.getPrecoVenda());
        Glide.with(context).load(produto.getUrlImagem0()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imagemProduto);



        holder.binding.cardView.setOnClickListener(v -> onClickLister.onClick(produto));

    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemProdutoAdapterBinding binding;


        public MyViewHolder(ItemProdutoAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Produto usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(Produto usuario);

    }

}
