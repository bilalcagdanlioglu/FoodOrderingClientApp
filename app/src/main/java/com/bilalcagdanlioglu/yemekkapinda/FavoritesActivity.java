package com.bilalcagdanlioglu.yemekkapinda;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Helper.RecyclerItemTouchHelper;
import com.bilalcagdanlioglu.yemekkapinda.Interface.RecyclerItemTouchHelperListener;
import com.bilalcagdanlioglu.yemekkapinda.Model.Favorites;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.FavoritesAdapter;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.FavoritesViewHolder;
import com.google.android.material.snackbar.Snackbar;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FavoritesAdapter adapter;
    RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_favorites );

        rootLayout = findViewById( R.id.root_layout );

        recyclerView = findViewById( R.id.recycler_fav );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper( 0,ItemTouchHelper.LEFT,this );
        new ItemTouchHelper( itemTouchHelperCallback ).attachToRecyclerView( recyclerView );
        
        loadFavorites();
    }

    private void loadFavorites() {
        adapter = new FavoritesAdapter( this, new Database( this ).getAllFavorites( Common.currentUser.getPhone() ) );
        recyclerView.setAdapter( adapter );
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getItem( position ).getFoodName();

            Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getItem( viewHolder.getAdapterPosition() );
            int deleteIndex  = viewHolder.getAdapterPosition();

            adapter.removeItem( viewHolder.getAdapterPosition());
            new Database( getBaseContext() ).removeFromFavorites( deleteItem.getFoodId(), Common.currentUser.getPhone() );

            Snackbar snackbar = Snackbar.make( rootLayout,name+" silindi!",Snackbar.LENGTH_LONG );
            snackbar.setAction( "Geri Al", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem( deleteItem,deleteIndex );
                    new Database( getBaseContext() ).addToFavorites( deleteItem );
                }
            } );
            snackbar.setActionTextColor( Color.YELLOW );
            snackbar.show();
        }
    }
}