package com.bilalcagdanlioglu.yemekkapinda;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Common.Config;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Helper.RecyclerItemTouchHelper;
import com.bilalcagdanlioglu.yemekkapinda.Interface.RecyclerItemTouchHelperListener;
import com.bilalcagdanlioglu.yemekkapinda.Model.MyResponse;
import com.bilalcagdanlioglu.yemekkapinda.Model.Notification;
import com.bilalcagdanlioglu.yemekkapinda.Model.Order;
import com.bilalcagdanlioglu.yemekkapinda.Model.Request;
import com.bilalcagdanlioglu.yemekkapinda.Model.Sender;
import com.bilalcagdanlioglu.yemekkapinda.Model.Token;
import com.bilalcagdanlioglu.yemekkapinda.Remote.APIService;
import com.bilalcagdanlioglu.yemekkapinda.Remote.IGoogleService;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.CartAdapter;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.CartViewHolder;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private static final int PAYPAL_REQUEST_CODE = 9999;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    Button btnPlace;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    Place shippingAddress;

    static PayPalConfiguration config = new PayPalConfiguration()
            .environment( PayPalConfiguration.ENVIRONMENT_SANDBOX )
            .clientId( Config.PAYPAL_CLIENT_ID );
    String address, comment;

    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static final int LOCATION_REQUEST_CODE=9999;

    IGoogleService mGoogleMapService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_cart );

        mGoogleMapService = Common.getGoogleMapAPI();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);
        }else {

            fetchLastLocation();
            createLocationRequest();

        }

        Intent intent = new Intent(this, PayPalService.class );
        intent.putExtra( PayPalService.EXTRA_PAYPAL_CONFIGURATION,config );
        startService( intent );

        mService = Common.getFCMService();

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        rootLayout = findViewById( R.id.rootLayout );
        recyclerView = findViewById( R.id.listCart );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        //Swipe to delete cart
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper( 0,ItemTouchHelper.LEFT,this );
        new ItemTouchHelper( itemTouchHelperCallback ).attachToRecyclerView( recyclerView );


        txtTotalPrice = findViewById( R.id.total );
        btnPlace = findViewById( R.id.btnPlaceOrder );
        btnPlace.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cart.size()>0 )
                {
                    showAlertDialog();
                }
                else
                {
                    Toast.makeText( Cart.this, "Sepetiniz boş.", Toast.LENGTH_SHORT ).show();
                }


            }
        } );
        loadListFood();

    }



    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_REQUEST_CODE);
        }
        else {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        Toast.makeText(Cart.this, mLastLocation.getLatitude() + " " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        Log.e("location: ",mLastLocation.getLatitude()+"/"+mLastLocation.getLongitude());
                    } else {
                        Toast.makeText(Cart.this, "Make sure you've enabled GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    createLocationRequest();
                    fetchLastLocation();
                }
                else {
                    Toast.makeText(Cart.this,"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Cart.this );
        alertDialog.setTitle( "Son işlemler.." );
        alertDialog.setMessage( "Lütfen adres giriniz: " );

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate( R.layout.order_address_comment ,null);

        //final EditText edtAddress = order_address_comment.findViewById( R.id.edtAddress );
        PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment);
        edtAddress.getView().findViewById( R.id.place_autocomplete_search_button ).setVisibility( View.GONE );
        ((EditText) edtAddress.getView().findViewById( R.id.place_autocomplete_search_input ) )
                .setHint( "Adres giriniz" );
        ((EditText) edtAddress.getView().findViewById( R.id.place_autocomplete_search_input ) )
                .setTextSize( 14 );
        edtAddress.setOnPlaceSelectedListener( new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e( "ERROR",status.getStatusMessage() );
            }
        } );


        final EditText edtComment = order_address_comment.findViewById( R.id.edtComment );

        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById( R.id.rdiShipToAddress );
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById( R.id.rdiHomeAddress );

        final RadioButton rdiCOD = (RadioButton) order_address_comment.findViewById( R.id.rdiCOD );
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById( R.id.rdiPaypal );

        rdiHomeAddress.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    if(!TextUtils.isEmpty( Common.currentUser.getHomeAddress()))
                    {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById( R.id.place_autocomplete_search_input ))
                                .setText( address );
                    }
                    else
                    {
                        Toast.makeText( Cart.this, "Lütfen ev adres bilgisini güncelleyiniz", Toast.LENGTH_SHORT ).show();
                    }

                }
            }
        } );


        rdiShipToAddress.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) // b == true
                {
                    mGoogleMapService.getAddressName( String.format( "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue( new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject( response.body().toString() );

                                        JSONArray resultsArray = jsonObject.getJSONArray( "results" );

                                        JSONObject firstObject = resultsArray.getJSONObject( 0 );

                                        address = firstObject.getString( "formatted_address" );
                                        ((EditText) edtAddress.getView().findViewById( R.id.place_autocomplete_search_input ))
                                                .setText( address );


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText( Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT ).show();
                                }
                            } );
                }
            }
        } );

        alertDialog.setView( order_address_comment );
        alertDialog.setIcon( R.drawable.ic_baseline_shopping_cart_24 );



        alertDialog.setPositiveButton( "EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

/*
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        cart
                );

                String order_number = String.valueOf( System.currentTimeMillis() );
                requests.child( order_number ).setValue( request );
      */

                if(!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()){
                    if(shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                }
                else if(rdiHomeAddress.isChecked()){
                    address = Common.currentUser.getHomeAddress();
                }
                else {
                    Toast.makeText( Cart.this, "Lütfen adres bilgisini seçiniz", Toast.LENGTH_SHORT ).show();

                    getFragmentManager().beginTransaction()
                            .remove( getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment ) )
                            .commit();

                    return;
                }

                if (TextUtils.isEmpty( address ))
                {
                    Toast.makeText( Cart.this, "Lütfen adres bilgisini seçiniz", Toast.LENGTH_SHORT ).show();

                    getFragmentManager().beginTransaction()
                            .remove( getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment ) )
                            .commit();

                    return;
                }

                comment = edtComment.getText().toString();

                //Check payment
                if(!rdiCOD.isChecked() && !rdiPaypal.isChecked()){
                    Toast.makeText( Cart.this, "Lütfen ödeme yöntemi seçiniz", Toast.LENGTH_SHORT ).show();

                    getFragmentManager().beginTransaction()
                            .remove( getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment ) )
                            .commit();

                    return;
                }
                else if(rdiPaypal.isChecked()){
                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace( "$","" )
                            .replace( ",","" );

                    float amoun = Float.parseFloat( formatAmount );
                    PayPalPayment payPalPayment = new PayPalPayment( new BigDecimal( formatAmount ),
                            "USD",
                            "Yemek Kapında ",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class );
                    intent.putExtra( PayPalService.EXTRA_PAYPAL_CONFIGURATION,config );
                    intent.putExtra( PaymentActivity.EXTRA_PAYMENT,payPalPayment );
                    startActivityForResult( intent , PAYPAL_REQUEST_CODE);
                }
                else if(rdiCOD.isChecked()){
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            comment,
                            "COD",
                            "Unpaid",
                            String.format( "%s,%s",mLastLocation.getLatitude(), mLastLocation.getLongitude() ),
                            cart
                    );

                    String order_number = String.valueOf( System.currentTimeMillis());
                    requests.child( order_number )
                            .setValue( request );
                    new Database( getBaseContext() ).cleanCart(Common.currentUser.getPhone());

                    //  sendNotificationOrder(order_number);

                    Toast.makeText( Cart.this, "Teşekkürler sipariş alındı", Toast.LENGTH_SHORT ).show();
                    finish();
                }

                getFragmentManager().beginTransaction()
                        .remove( getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment ) )
                        .commit();

            }
        } );
        alertDialog.setNegativeButton( "HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                getFragmentManager().beginTransaction()
                        .remove( getFragmentManager().findFragmentById( R.id.place_autocomplete_fragment ) )
                        .commit();
            }
        } );
        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (requestCode == PAYPAL_REQUEST_CODE)
          {
              if (resultCode == RESULT_OK)
              {
                  PaymentConfirmation confirmation = data.getParcelableExtra( PaymentActivity.EXTRA_RESULT_CONFIRMATION );
                  if(confirmation !=null)
                  {
                      try {
                          String paymentDetail = confirmation.toJSONObject().toString(4);
                          JSONObject jsonObject = new JSONObject(paymentDetail);

                              Request request = new Request(
                          Common.currentUser.getPhone(),
                          Common.currentUser.getName(),
                          address,
                          txtTotalPrice.getText().toString(),
                          "0",
                          comment,
                          "Paypal",
                          jsonObject.getJSONObject( "response" ).getString( "state" ),
                          String.format( "%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude ),
                          cart
                  );

                  String order_number = String.valueOf( System.currentTimeMillis());
                          requests.child( order_number )
                          .setValue( request );
                  new Database( getBaseContext() ).cleanCart(Common.currentUser.getPhone());

                //  sendNotificationOrder(order_number);

                          Toast.makeText( this, "Teşekkürler sipariş alındı", Toast.LENGTH_SHORT ).show();
                          finish();

                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
              }
              else if (resultCode == Activity.RESULT_CANCELED)
              {
                  Toast.makeText( this, "Ödeme iptal", Toast.LENGTH_SHORT ).show();
              }
              else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
              {
                  Toast.makeText( this, "Geçersiz ödeme ", Toast.LENGTH_SHORT ).show();
              }
          }
      }

    private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild( "isServerToken" ).equalTo( true);
        data.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren()){
                    Token serverToken = postSnapShot.getValue(Token.class);

                    Notification notification = new Notification( "Yemek Kapında","Yeni Sipariş "+order_number );
                    Sender content = new Sender(serverToken.getToken(),notification);
                    mService.sendNotification( content )
                            .enqueue( new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success == 1)
                                        {
                                            Toast.makeText( Cart.this, "Teşekkürler, Sipariş Alındı", Toast.LENGTH_SHORT ).show();
                                            finish();
                                        }
                                        else
                                        {
                                            Toast.makeText( Cart.this, "Hata Oluştu!!", Toast.LENGTH_SHORT ).show();
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            } );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void loadListFood() {
        cart = new Database( this ).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter( cart,this );
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter( adapter );
        int total=0;
        for(Order order:cart)
            total+=(Integer.parseInt( order.getPrice() ))*(Integer.parseInt( order.getQuantity() ));
            Locale locale = new Locale( "en","US" );
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText( fmt.format( total ) );
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals( Common.DELETE )){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        cart.remove( position );
        new Database(this).cleanCart(Common.currentUser.getPhone());
        for(Order item:cart)
        {
            new Database( this ).addToCart( item );
        }
        loadListFood();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder){
            String name =((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem( viewHolder.getAdapterPosition() );
            int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem( deleteIndex );
            new Database(getBaseContext()).removeFromCart( deleteItem.getProductId(),Common.currentUser.getPhone() );

            int total=0;
            List<Order> orders = new Database( getBaseContext() ).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total+=(Integer.parseInt( item.getPrice() ))*(Integer.parseInt( item.getQuantity() ));
            Locale locale = new Locale( "en","US" );
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText( fmt.format( total ) );

            Snackbar snackbar = Snackbar.make( rootLayout,name+" silindi!",Snackbar.LENGTH_LONG );
            snackbar.setAction( "Geri Al", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem( deleteItem,deleteIndex );
                    new Database( getBaseContext() ).addToCart( deleteItem );

                    int total=0;
                    List<Order> orders = new Database( getBaseContext() ).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total+=(Integer.parseInt( item.getPrice() ))*(Integer.parseInt( item.getQuantity() ));
                    Locale locale = new Locale( "en","US" );
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText( fmt.format( total ) );
                }
            } );
            snackbar.setActionTextColor( Color.YELLOW );
            snackbar.show();
        }
    }
}