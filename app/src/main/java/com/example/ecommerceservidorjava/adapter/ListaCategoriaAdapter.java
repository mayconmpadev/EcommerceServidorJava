package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemCategoriaVerticalBinding;
import com.example.ecommerceservidorjava.model.Categoria;

import java.util.List;


public class ListaCategoriaAdapter extends RecyclerView.Adapter<ListaCategoriaAdapter.MyViewHolder> {

    private final int layout;
    private final List<Categoria> categoriaList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaCategoriaAdapter(int layout, List<Categoria> categoriaList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.categoriaList = categoriaList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemCategoriaVerticalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Categoria categoria = categoriaList.get(position);

        holder.binding.nomeCategoria.setText(categoria.getNome().substring(0, 1).toUpperCase().concat(categoria.getNome().substring(1)));
        Glide.with(context).load(categoria.getUrlImagem()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imagemCategoria);


        holder.binding.getRoot().setOnClickListener(v -> onClickLister.onClick(categoria));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(categoria);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categoriaList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemCategoriaVerticalBinding binding;


        public MyViewHolder(ItemCategoriaVerticalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Categoria categoria);

        void onLongClick(Categoria categoria);

    }

    public interface OnLongClickLister {

        void onLongClick(Categoria categoria);

    }

}
