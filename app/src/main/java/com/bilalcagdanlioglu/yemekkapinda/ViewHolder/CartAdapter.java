package com.bilalcagdanlioglu.yemekkapinda.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Cart;
import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Model.Order;
import com.bilalcagdanlioglu.yemekkapinda.R;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listdata = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listdata, Cart cart) {
        this.listdata = listdata;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from( cart );
        View itemView = inflater.inflate( R.layout.cart_layout,parent,false );
        return new CartViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
     /*   TextDrawable drawable = TextDrawable.builder()
                .buildRound( ""+listdata.get( position ).getQuantity(), Color.RED );
        holder.img_cart_count.setImageDrawable( drawable );
      */
        Picasso.with( cart.getBaseContext() )
                .load( listdata.get( position ).getImage() )
                .resize( 70,70 )
                .centerCrop()
                .into( holder.cart_image );

        holder.btn_quantity.setNumber( listdata.get( position ).getQuantity() );
        holder.btn_quantity.setOnValueChangeListener( new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listdata.get( position );
                order.setQuantity( String.valueOf( newValue ) );
                new Database(cart).updateCart(order);

                int total=0;
                List<Order> orders = new Database( cart ).getCarts(Common.currentUser.getPhone());
                for(Order item:orders)
                    total+=(Integer.parseInt( order.getPrice() ))*(Integer.parseInt( item.getQuantity() ));
                    Locale locale = new Locale( "en","US" );
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    cart.txtTotalPrice.setText( fmt.format( total ) );
            }
        } );

        Locale locale = new Locale( "en","US" );
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt( listdata.get( position ).getPrice() ))*(Integer.parseInt( listdata.get( position ).getQuantity() )) ;
        holder.txt_price.setText( fmt.format( price ) );
        holder.txt_cart_name.setText( listdata.get( position ).getProductName() );
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public Order getItem(int position){
        return listdata.get( position );
    }

    public void removeItem(int position)
    {
        listdata.remove( position );
        notifyItemRemoved( position );
    }

    public void restoreItem(Order item,int position)
    {
        listdata.add( position,item );
        notifyItemInserted( position );
    }
}
