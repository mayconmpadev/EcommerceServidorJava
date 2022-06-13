package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaOrcamentoBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.util.Timestamp;

import java.util.List;


public class ListaDespesaAdapter extends RecyclerView.Adapter<ListaDespesaAdapter.MyViewHolder> {

    private final int layout;
    private final List<Despesa> clienteList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaDespesaAdapter(int layout, List<Despesa> clienteList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
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
                ItemListaOrcamentoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Despesa despesa = clienteList.get(position);

        holder.binding.textNome.setText(despesa.getDescricao().substring(0, 1).toUpperCase().concat(despesa.getDescricao().substring(1)));
        holder.binding.textEmeil.setText(despesa.getQtd_parcelas() + "x " + despesa.getValor_parcela());
        holder.binding.textTotal.setText(despesa.getValor());
        //   if (despesa.getStatus().equals("Em analise")){
        //     holder.binding.viewStatus.setBackgroundResource(R.color.ouro);
        //}else if(despesa.getStatus().equals("Aprovado")){
        //     holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        //   }else {
        //    holder.binding.viewStatus.setBackgroundResource(R.color.red);
        //  }

        holder.binding.textData.setText(Timestamp.getFormatedDateTime(Long.parseLong(despesa.getData()), "dd/MM/yyyy - HH:mm"));


        //Glide.with(context).load(despesa.getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);
        holder.binding.imgFoto.setImageResource(R.drawable.ic_mercadoria);


        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(despesa, position));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(despesa);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return clienteList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaOrcamentoBinding binding;


        public MyViewHolder(ItemListaOrcamentoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Despesa despesa, int position);


    }

    public interface OnLongClickLister {

        void onLongClick(Despesa despesa);

    }

}
