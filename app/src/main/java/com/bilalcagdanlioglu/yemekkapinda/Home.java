package com.bilalcagdanlioglu.yemekkapinda;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andremion.counterfab.CounterFab;
import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapinda.Model.Banner;
import com.bilalcagdanlioglu.yemekkapinda.Model.Category;
import com.bilalcagdanlioglu.yemekkapinda.Model.Token;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.MenuViewHolder;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class Home extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    CounterFab fab;

    SwipeRefreshLayout swipeRefreshLayout;

    HashMap<String,String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );

        Toolbar toolbar = findViewById( R.id.toolbar );
        toolbar.setTitle( "Menü" );
        setSupportActionBar( toolbar );

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById( R.id.swipe_layout );
        swipeRefreshLayout.setColorSchemeResources( R.color.primaryColor,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet( getBaseContext() ))
                    loadMenu();
                else
                {
                    Toast.makeText( getBaseContext(), "Lütfen internet bağlantınızı kontrol ediniz!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );
        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet( getBaseContext() ))
                    loadMenu();
                else
                {
                    Toast.makeText( getBaseContext(), "Lütfen internet bağlantınızı kontrol ediniz!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );


        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery( category , Category.class).build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i, @NonNull Category category) {
                menuViewHolder.txtMenuName.setText( category.getName() );
                Picasso.with( getBaseContext() ).load( category.getImage() )
                        .into( menuViewHolder.imageView );
                final Category clickItem = category;
                menuViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        foodList.putExtra( "CategoryId",adapter.getRef( position ).getKey() );
                        startActivity( foodList );
                    }
                } );
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.menu_item , parent, false);
                return new MenuViewHolder( itemView );
            }
        };

        fab = (CounterFab) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity( cartIntent );
            }
        } );

        fab.setCount( new Database(this).getCountCart(Common.currentUser.getPhone()) );

        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        View headerView = navigationView.getHeaderView( 0 );
        txtFullName = headerView.findViewById( R.id.txtFullName );
        txtFullName.setText( Common.currentUser.getName() );

        recycler_menu = findViewById( R.id.recycler_menu );
        //layoutManager = new LinearLayoutManager( this );
        //recycler_menu.setLayoutManager( layoutManager );
        recycler_menu.setLayoutManager( new GridLayoutManager( this,2 ) );

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation( recycler_menu.getContext() ,
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation( controller );

        updateToken( FirebaseInstanceId.getInstance().getToken() );

        setupSlider();
    }

    private void setupSlider() {
        mSlider = findViewById( R.id.slider );
        image_list = new HashMap<>();
        DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot : snapshot.getChildren())
                {
                    Banner banner = postSnapShot.getValue(Banner.class);
                    image_list.put( banner.getName()+"@@@"+banner.getId(),banner.getImage() );
                }
                for(String key:image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    TextSliderView textSliderView = new TextSliderView( getBaseContext() );
                    textSliderView
                            .description(nameOfFood)
                            .image( image_list.get( key ) )
                            .setScaleType( BaseSliderView.ScaleType.Fit )
                            .setOnSliderClickListener( new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this , FoodDetail.class );
                                    intent.putExtras( textSliderView.getBundle() );
                                    startActivity( intent );
                                }
                            } );
                    textSliderView.bundle( new Bundle() );
                    textSliderView.getBundle().putString( "FoodId" , idOfFood);
                    mSlider.addSlider( textSliderView );

                    banners.removeEventListener( this );

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        } );
        mSlider.setPresetTransformer( SliderLayout.Transformer.Background2Foreground );
        mSlider.setPresetIndicator( SliderLayout.PresetIndicators.Center_Bottom );
        mSlider.setCustomAnimation( new DescriptionAnimation() );
        mSlider.setDuration( 4000 );
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token( token,false );
        tokens.child( Common.currentUser.getPhone() ).setValue( data );
    }

    private void loadMenu() {


        adapter.startListening();
        recycler_menu.setAdapter( adapter );
        swipeRefreshLayout.setRefreshing( false );

        //animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }
    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount( new Database(this).getCountCart(Common.currentUser.getPhone()) );
        if(adapter != null)
            adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        if(drawer.isDrawerOpen( GravityCompat.START )){
            drawer.closeDrawer( GravityCompat.START );
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main,menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_search){
            startActivity(new Intent(Home.this,SearchActivity.class));
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id == R.id.nav_menu){

        }
        else if(id == R.id.nav_favorites){
            Intent favorites = new Intent(Home.this,FavoritesActivity.class);
            startActivity( favorites );
        }
        else if(id == R.id.nav_cart){
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity( cartIntent );
        }
        else if(id == R.id.nav_order){
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity( orderIntent );
        }
        else if(id == R.id.nav_change_pwd){
            showChangePasswordDialog();
        }
        else if(id == R.id.nav_home_address){
            showHomeAddressDialog();
        }
        else if(id == R.id.nav_log_out){

            Intent login = new Intent(Home.this,Login.class);
            login.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( login );
        }
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    private void showHomeAddressDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "Ev Adresi Bilgileri" );
        alertDialog.setMessage( "Lütfen adresi giriniz" );

        LayoutInflater inflater = LayoutInflater.from( this );
        View layout_home = inflater.inflate( R.layout.home_address_layout,null );

        EditText edtHomeAddress = layout_home.findViewById( R.id.edtHomeAddress );
        alertDialog.setView( layout_home );

        alertDialog.setPositiveButton( "Güncelle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.dismiss();

               Common.currentUser.setHomeAddress( edtHomeAddress.getText().toString() );

               FirebaseDatabase.getInstance().getReference("User")
               .child( Common.currentUser.getPhone() )
                       .setValue( Common.currentUser )
                       .addOnCompleteListener( new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               Toast.makeText( Home.this, "Ev adresiniz güncellendi", Toast.LENGTH_SHORT ).show();
                           }
                       } );
            }
        } );
        alertDialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "Şifre Değişikliği" );
        alertDialog.setMessage( "Bilgileri giriniz." );

        LayoutInflater inflater = LayoutInflater.from( this );
        View layout_pwd = inflater.inflate( R.layout.change_password_layout,null );

        EditText edtPassword = layout_pwd.findViewById( R.id.edtPassword );
        EditText edtNewPassword = layout_pwd.findViewById( R.id.edtNewPassword );
        EditText edtNewPassword2 = layout_pwd.findViewById( R.id.edtNewPassword2 );

        alertDialog.setView( layout_pwd );

        alertDialog.setPositiveButton( "Değiştir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(edtPassword.getText().toString().equals( Common.currentUser.getPassword() ))
                {
                    if(edtNewPassword.getText().toString().equals( edtNewPassword2.getText().toString() ))
                    {
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put( "password",edtNewPassword.getText().toString() );

                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child( Common.currentUser.getPhone() )
                                .updateChildren( passwordUpdate )
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText( Home.this, "Şifre güncellendi", Toast.LENGTH_SHORT ).show();
                                    }
                                } )
                                .addOnFailureListener( new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText( Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                                    }
                                } );
                    }
                    else
                    {
                        Toast.makeText( Home.this, "Yeni şifre eşleşmedi", Toast.LENGTH_SHORT ).show();
                    }
                }
                else
                    {
                        Toast.makeText( Home.this, "Yeni şifre giriniz!!", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

        alertDialog.setNegativeButton( "İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        } );

        alertDialog.show();
    }
}