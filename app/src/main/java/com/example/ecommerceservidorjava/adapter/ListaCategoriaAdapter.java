package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemCategoriaVerticalBinding;
import com.example.ecommerceservidorjava.databinding.ItemListaUsuarioBinding;
import com.example.ecommerceservidorjava.model.Usuario;

import java.util.List;


public class ListaCategoriaAdapter extends RecyclerView.Adapter<ListaCategoriaAdapter.MyViewHolder> {

    private final int layout;
    private final List<Usuario> categoriaList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaCategoriaAdapter(int layout, List<Usuario> categoriaList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
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
        Usuario usuario = categoriaList.get(position);

        holder.binding.nomeCategoria.setText(usuario.getNome().substring(0, 1).toUpperCase().concat(usuario.getNome().substring(1)));
        Glide.with(context).load(usuario.getFoto()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imageView);



        holder.binding.getRoot().setOnClickListener(v -> onClickLister.onClick(usuario));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(usuario);
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
        void onClick(Usuario usuario);

        void onLongClick(Usuario usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(Usuario usuario);

    }

}
