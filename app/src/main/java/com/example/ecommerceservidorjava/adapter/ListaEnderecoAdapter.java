package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerceservidorjava.databinding.ItemListaClienteBinding;
import com.example.ecommerceservidorjava.model.Endereco;


import java.util.List;


public class ListaEnderecoAdapter extends RecyclerView.Adapter<ListaEnderecoAdapter.MyViewHolder> {

    private final int layout;
    private final List<Endereco> enderecoList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaEnderecoAdapter(int layout, List<Endereco> enderecoList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.enderecoList = enderecoList;
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
        Endereco endereco = enderecoList.get(position);

        holder.binding.textNome.setText(endereco.getNomeEndereco().substring(0, 1).toUpperCase().concat(endereco.getNomeEndereco().substring(1)));
        holder.binding.textEmeil.setText(endereco.getLocalidade());




        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(endereco));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(endereco);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return enderecoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaClienteBinding binding;


        public MyViewHolder(ItemListaClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Endereco usuario);

        void onLongClick(Endereco usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(Endereco usuario);

    }

}
