package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaUsuarioBinding;
import com.example.ecommerceservidorjava.model.Usuario;

import java.util.List;


public class ListaUsuarioAdapter extends RecyclerView.Adapter<ListaUsuarioAdapter.MyViewHolder> {

    private final int layout;
    private final List<Usuario> usuarioList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaUsuarioAdapter(int layout, List<Usuario> usuarioList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.usuarioList = usuarioList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemListaUsuarioBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);

        holder.binding.textNome.setText(usuario.getNome().substring(0, 1).toUpperCase().concat(usuario.getNome().substring(1)));
        holder.binding.textEmeil.setText(usuario.getEmail());
        holder.binding.textPerfil.setText(usuario.getPerfil());
        Glide.with(context).load(usuario.getUrlImagem()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imgFoto);
        if (usuario.isStatus()) {
            holder.binding.textStatus.setText("Ativo");
        } else {
            holder.binding.textStatus.setText("Bloqueado");
        }


        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(usuario));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(usuario);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaUsuarioBinding binding;


        public MyViewHolder(ItemListaUsuarioBinding binding) {
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
