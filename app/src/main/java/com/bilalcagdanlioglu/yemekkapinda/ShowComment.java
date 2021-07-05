package com.bilalcagdanlioglu.yemekkapinda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Model.Rating;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

public class ShowComment extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference ratingTbl;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;
    String foodId="";


    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_comment );

        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");
        recyclerView = findViewById( R.id.recyclerComment );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        swipeRefreshLayout = findViewById( R.id.swipe_layout );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               if(getIntent() != null){
                   foodId = getIntent().getStringExtra( Common.INTENT_FOOD_ID );
               }
               if (!foodId.isEmpty() && foodId != null){
                   Query query = ratingTbl.orderByChild( "foodId" ).equalTo( foodId );

                   FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                           .setQuery( query, Rating.class )
                           .build();

                   adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                       @Override
                       protected void onBindViewHolder(@NonNull @NotNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull @NotNull Rating rating) {
                           showCommentViewHolder.ratingBar.setRating( Float.parseFloat( rating.getRateValue() ) );
                           showCommentViewHolder.txtComment.setText( rating.getComment());
                           showCommentViewHolder.txtUserPhone.setText( rating.getUserPhone() );
                       }

                       @NonNull
                       @NotNull
                       @Override
                       public ShowCommentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                           View view = LayoutInflater.from( parent.getContext() )
                                   .inflate( R.layout.show_comment_layout,parent,false );
                           return new ShowCommentViewHolder( view );
                       }
                   };

                   loadComment(foodId);
               }
            }
        } );
        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing( true );

                if(getIntent() != null){
                    foodId = getIntent().getStringExtra( Common.INTENT_FOOD_ID );
                }
                if (!foodId.isEmpty() && foodId != null) {
                    Query query = ratingTbl.orderByChild( "foodId" ).equalTo( foodId );

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery( query, Rating.class )
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>( options ) {
                        @Override
                        protected void onBindViewHolder(@NonNull @NotNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull @NotNull Rating rating) {
                            showCommentViewHolder.ratingBar.setRating( Float.parseFloat( rating.getRateValue() ) );
                            showCommentViewHolder.txtComment.setText( rating.getComment() );
                            showCommentViewHolder.txtUserPhone.setText( rating.getUserPhone() );
                        }

                        @NonNull
                        @NotNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from( parent.getContext() )
                                    .inflate( R.layout.show_comment_layout, parent, false );
                            return new ShowCommentViewHolder( view );
                        }
                    };

                    loadComment( foodId );
                }
            }
        } );

    }

    private void loadComment(String foodId) {
        adapter.startListening();
        recyclerView.setAdapter( adapter);
        swipeRefreshLayout.setRefreshing( false );

    }
}