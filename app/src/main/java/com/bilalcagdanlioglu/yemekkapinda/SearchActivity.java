package com.bilalcagdanlioglu.yemekkapinda;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.Model.Favorites;
import com.bilalcagdanlioglu.yemekkapinda.Model.Food;
import com.bilalcagdanlioglu.yemekkapinda.Model.Order;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    List<String> suggestList = new ArrayList<String>();
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodList;

    Database localDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_search );

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");
        localDB = new Database( this );

        recyclerView = findViewById( R.id.recycler_search );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        materialSearchBar = findViewById( R.id.searchbar );
        materialSearchBar.setHint( "Yemek aramak için tıklayın" );
        loadSuggest();

        materialSearchBar.setCardViewElevation( 10 );
        materialSearchBar.addTextChangeListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<String>();
                for(String search:suggestList){
                    if(search.toLowerCase().contains( materialSearchBar.getText().toLowerCase() )){
                        suggest.add( search );
                    }
                }
                materialSearchBar.setLastSuggestions( suggest );
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        } );
        materialSearchBar.setOnSearchActionListener( new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){
                    recyclerView.setAdapter( adapter );
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch( text );
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        } );

        loadAllFoods();
        
    }

    private void loadAllFoods() {

        Query searchByName = foodList;
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery( searchByName , Food.class ).build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {
                foodViewHolder.food_name.setText( food.getName() );
                Picasso.with( getBaseContext() ).load( food.getImage() )
                        .into( foodViewHolder.food_image );

                //Quick cart event


                foodViewHolder.quick_cart.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isExists = new Database( getBaseContext() ).checkFoodExists( adapter.getRef( i ).getKey(), Common.currentUser.getPhone() );
                        if(!isExists){
                            new Database( getBaseContext() ).addToCart( new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef( i ).getKey(),
                                    food.getName(),
                                    "1",
                                    food.getPrice(),
                                    food.getDiscount(),
                                    food.getImage()
                            ) );

                        }
                        else{
                            new Database(getBaseContext()).increaseCart( Common.currentUser.getPhone(),adapter.getRef( i ).getKey() );
                        }
                        Toast.makeText( SearchActivity.this, "Yemekler sepete eklendi..", Toast.LENGTH_SHORT ).show();

                    }
                });


                //Add favorites
                if(localDB.isFavorite( adapter.getRef( i ).getKey() ,Common.currentUser.getPhone() ))
                    foodViewHolder.fav_image.setImageResource( R.drawable.ic_baseline_favorite_24 );

                foodViewHolder.fav_image.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef( i ).getKey());
                        favorites.setFoodName( food.getName() );
                        favorites.setFoodDescription( food.getDescription() );
                        favorites.setFoodDiscount( food.getDiscount() );
                        favorites.setFoodImage( food.getImage() );
                        favorites.setFoodMenuId( food.getMenuId() );
                        favorites.setUserPhone( Common.currentUser.getPhone() );
                        favorites.setFoodPrice( food.getPrice() );

                        if(!localDB.isFavorite( adapter.getRef( i ).getKey(),Common.currentUser.getPhone() ))
                        {
                            localDB.addToFavorites( favorites );
                            foodViewHolder.fav_image.setImageResource( R.drawable.ic_baseline_favorite_24 );
                            Toast.makeText( SearchActivity.this, ""+food.getName()+" favorilere eklendi.", Toast.LENGTH_SHORT ).show();
                        }
                        else
                        {
                            localDB.removeFromFavorites( adapter.getRef( i ).getKey(),Common.currentUser.getPhone() );
                            foodViewHolder.fav_image.setImageResource( R.drawable.ic_baseline_favorite_border_24 );
                            Toast.makeText( SearchActivity.this, ""+food.getName()+" favorilerden çıkarıldı.", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );

                final Food local = food;
                foodViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(SearchActivity.this,FoodDetail.class);
                        foodDetail.putExtra( "FoodId", adapter.getRef( position ).getKey());
                        startActivity( foodDetail );
                    }
                } );
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.food_item,parent,false );
                return new FoodViewHolder( itemView );
            }
        };
        adapter.startListening();
        recyclerView.setAdapter( adapter);

    }

    private void startSearch(CharSequence text) {
        Query searchByName = foodList.orderByChild( "name" ).equalTo( text.toString() );
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery( searchByName , Food.class ).build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {
                foodViewHolder.food_name.setText( food.getName() );
                Picasso.with( getBaseContext() ).load( food.getImage() )
                        .into( foodViewHolder.food_image );
                final Food local = food;
                foodViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(SearchActivity.this,FoodDetail.class);
                        foodDetail.putExtra( "FoodId", searchAdapter.getRef( position ).getKey());
                        startActivity( foodDetail );
                    }
                } );
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.food_item,parent,false );
                return new FoodViewHolder( itemView );
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter( searchAdapter );
    }

    private void loadSuggest() {
        foodList.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot postSnapshot:snapshot.getChildren()){
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add( item.getName() );
                        }
                        materialSearchBar.setLastSuggestions( suggestList );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }

    @Override
    protected void onStop() {
        if(adapter != null){ adapter.stopListening(); }
        if(searchAdapter != null) { searchAdapter.stopListening(); }
        super.onStop();
    }
}