package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.R;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{
    public TextView txt_cart_name,txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(@NonNull View itemView) {
        super( itemView );
        txt_cart_name = itemView.findViewById( R.id.cart_item_name );
        txt_price = itemView.findViewById( R.id.cart_item_price );
        btn_quantity = (ElegantNumberButton) itemView.findViewById( R.id.btn_quantity );
        cart_image = itemView.findViewById( R.id.cart_image );
        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById( R.id.view_foreground );

        itemView.setOnCreateContextMenuListener( this );
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "İşlem seçiniz" );
        contextMenu.add( 0,0, getAdapterPosition(), Common.DELETE );
    }
}
