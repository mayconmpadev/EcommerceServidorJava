package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaClienteBinding;
import com.example.ecommerceservidorjava.model.Cliente;

import java.util.List;


public class ListaClienteAdapter extends RecyclerView.Adapter<ListaClienteAdapter.MyViewHolder> {

    private final int layout;
    private final List<Cliente> clienteList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaClienteAdapter(int layout, List<Cliente> clienteList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.clienteList = clienteList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemListaClienteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);

        holder.binding.textNome.setText(cliente.getNome().substring(0, 1).toUpperCase().concat(cliente.getNome().substring(1)));
        holder.binding.textEmeil.setText(cliente.getEmail());
        if (cliente.getPerfil().equals("bronze")){
            holder.binding.textPerfil.setCompoundDrawableTintList(ColorStateList.valueOf( ContextCompat.getColor(context, R.color.broze)));
        }else if (cliente.getPerfil().equals("prata")){
            holder.binding.textPerfil.setCompoundDrawableTintList(ColorStateList.valueOf( ContextCompat.getColor(context, R.color.prata)));
        }else {
            holder.binding.textPerfil.setCompoundDrawableTintList(ColorStateList.valueOf( ContextCompat.getColor(context, R.color.ouro)));
        }

        Glide.with(context).load(cliente.getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);
        if (cliente.isStatus()) {
            holder.binding.textStatus.setText("Ativo");
        } else {
            holder.binding.textStatus.setText("Bloqueado");
        }


        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(cliente));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(cliente);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return clienteList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaClienteBinding binding;


        public MyViewHolder(ItemListaClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Cliente usuario);

        void onLongClick(Cliente usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(Cliente usuario);

    }

}
