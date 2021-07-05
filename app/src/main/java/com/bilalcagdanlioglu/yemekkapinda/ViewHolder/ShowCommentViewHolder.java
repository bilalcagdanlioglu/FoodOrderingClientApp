package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.R;

import org.jetbrains.annotations.NotNull;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserPhone , txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull @NotNull View itemView) {
        super( itemView );

        txtComment = itemView.findViewById( R.id.txtComment );
        txtUserPhone = itemView.findViewById( R.id.txtUserPhone );
        ratingBar = itemView.findViewById( R.id.ratingBar );


    }
}
