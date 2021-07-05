package com.bilalcagdanlioglu.yemekkapinda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Model.Request;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_order_status );

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById( R.id.listOrders );
        recyclerView.setHasFixedSize( true );
        layoutManager= new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        if(getIntent().getExtras() == null)
            loadOrders( Common.currentUser.getPhone());
        else
            loadOrders( getIntent().getStringExtra( "userPhone" ) );
    }
    private void loadOrders(String phone)
    {
        Query gerOrderByUser = requests.orderByChild( "phone" )
                .equalTo( phone );
        FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery( gerOrderByUser, Request.class).build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, int position, @NonNull Request request) {
                orderViewHolder.txtOrderId.setText( adapter.getRef( position).getKey() );
                orderViewHolder.txtOrderStatus.setText( Common.convertCodeToStatus(request.getStatus()) );
                orderViewHolder.txtOrderAddress.setText( request.getAddress() );
                orderViewHolder.txtOrderPhone.setText( request.getPhone() );
                orderViewHolder.btn_delete.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(adapter.getItem( position ).getStatus().equals( "0" ))
                        {
                            deleteOrder(adapter.getRef( position ).getKey());
                        }
                        else{
                            Toast.makeText( OrderStatus.this, "Sipariş iptal edilemez!", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.order_layout,parent,false );
                return new OrderViewHolder( itemView );
            }
        };
        adapter.startListening();
        recyclerView.setAdapter( adapter );
    }

    private void deleteOrder(String key) {
        requests.child( key )
                .removeValue().addOnSuccessListener( new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText( OrderStatus.this, new StringBuilder("Sipariş ")
                        .append( key )
                        .append( " silinmiştir." ).toString(), Toast.LENGTH_SHORT ).show();
            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText( OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}