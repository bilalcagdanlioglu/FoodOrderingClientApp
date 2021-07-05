package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone,txtOrderAddress;
    private ItemClickListener itemClickListener;

    public ImageView btn_delete;

    public OrderViewHolder(@NonNull View itemView) {
        super( itemView );
        txtOrderPhone = itemView.findViewById( R.id.order_phone );
        txtOrderId = itemView.findViewById( R.id.order_id );
        txtOrderStatus = itemView.findViewById( R.id.order_status );
        txtOrderAddress = itemView.findViewById( R.id.order_address );
        btn_delete = itemView.findViewById( R.id.btn_delete);
        itemView.setOnClickListener( this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick( view,getAdapterPosition(),false );
    }
}
