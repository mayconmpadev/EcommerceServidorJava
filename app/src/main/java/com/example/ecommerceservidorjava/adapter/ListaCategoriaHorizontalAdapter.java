package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemCategoriaHorizontalBinding;

import com.example.ecommerceservidorjava.model.Categoria;

import java.util.List;


public class ListaCategoriaHorizontalAdapter extends RecyclerView.Adapter<ListaCategoriaHorizontalAdapter.MyViewHolder> {

    private final int layout;
    private final List<Categoria> categoriaList;
    private final Context context;
    private final boolean favorito;
    private final OnClickLister onClick;
    private final boolean background;
    private int row_index = 0;


    public ListaCategoriaHorizontalAdapter(int layout, List<Categoria> categoriaList, Context context, boolean favorito, OnClickLister onClick, boolean background) {
        this.layout = layout;
        this.categoriaList = categoriaList;
        this.context = context;
        this.favorito = favorito;
        this.onClick = onClick;
        this.background = background;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemCategoriaHorizontalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Categoria categoria = categoriaList.get(position);
        if (background) {

            holder.itemView.setOnClickListener(v -> {
                onClick.onClick(categoria);

                row_index = holder.getAdapterPosition();
                notifyDataSetChanged();
            });

            if(row_index == holder.getAdapterPosition()){
                holder.itemView.setBackgroundResource(R.drawable.bg_categoria_home);
                holder.binding.nomeCategoria.setTextColor(Color.parseColor("#FFFFFF"));
                holder.binding.imagemCategoria.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
            }else {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                holder.binding.nomeCategoria.setTextColor(Color.parseColor("#808080"));
               holder.binding.imagemCategoria.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
            }

        } else {
            holder.itemView.setOnClickListener(v -> onClick.onClick(categoria));
        }
        holder.binding.nomeCategoria.setText(categoria.getNome().substring(0, 1).toUpperCase().concat(categoria.getNome().substring(1)));
        Glide.with(context).load(categoria.getUrlImagem()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imagemCategoria);

    }

    @Override
    public int getItemCount() {
        return categoriaList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemCategoriaHorizontalBinding binding;


        public MyViewHolder(ItemCategoriaHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Categoria categoria);

        void onLongClick(Categoria categoria);

    }


}
