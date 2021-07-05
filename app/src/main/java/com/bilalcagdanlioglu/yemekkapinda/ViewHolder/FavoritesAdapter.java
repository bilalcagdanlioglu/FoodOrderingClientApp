package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.FoodDetail;
import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.Model.Favorites;
import com.bilalcagdanlioglu.yemekkapinda.Model.Order;
import com.bilalcagdanlioglu.yemekkapinda.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter  extends RecyclerView.Adapter<FavoritesViewHolder>{

    private Context context;
    private List<Favorites> favoritesList;

    public FavoritesAdapter(Context context, List<Favorites> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from( context )
                .inflate( R.layout.favorites_item, parent,false );
        return new FavoritesViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder viewHolder, int position) {
        viewHolder.food_name.setText( favoritesList.get( position ).getFoodName() );
        Picasso.with( context ).load( favoritesList.get( position ).getFoodImage() )
                .into( viewHolder.food_image );

        //Quick cart event


        viewHolder.quick_cart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isExists = new Database( context ).checkFoodExists( favoritesList.get( position ).getFoodId(), Common.currentUser.getPhone() );
                if(!isExists){
                    new Database( context ).addToCart( new Order(
                            Common.currentUser.getPhone(),
                            favoritesList.get( position ).getFoodId(),
                            favoritesList.get( position ).getFoodName(),
                            "1",
                            favoritesList.get( position ).getFoodPrice(),
                            favoritesList.get( position ).getFoodDiscount(),
                            favoritesList.get( position ).getFoodImage()
                    ) );

                }
                else{
                    new Database(context).increaseCart( Common.currentUser.getPhone(),favoritesList.get( position ).getFoodId() );
                }
                Toast.makeText( context, "Yemekler sepete eklendi..", Toast.LENGTH_SHORT ).show();

            }
        });

        final Favorites local = favoritesList.get( position );
        viewHolder.setItemClickListener( new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent foodDetail = new Intent(context, FoodDetail.class);
                foodDetail.putExtra( "FoodId", favoritesList.get( position ).getFoodId());
                context.startActivity( foodDetail );
            }
        } );
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public void removeItem(int position)
    {
        favoritesList.remove( position );
        notifyItemRemoved( position );
    }

    public void restoreItem(Favorites item,int position)
    {
        favoritesList.add( position,item );
        notifyItemInserted( position );
    }

    public Favorites getItem(int position){
        return favoritesList.get(position);
    }
}
