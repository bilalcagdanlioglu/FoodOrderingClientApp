package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name;
    public ImageView food_image, quick_cart;

    private ItemClickListener itemClickListener;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FavoritesViewHolder(@NonNull View itemView) {
        super( itemView );
        food_name = itemView.findViewById( R.id.food_name );
        food_image = itemView.findViewById( R.id.food_image );
        quick_cart = itemView.findViewById( R.id.btn_quick_cart );

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById( R.id.view_foreground );

        itemView.setOnClickListener( this );
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick( view,getAdapterPosition(),false );
    }
}
