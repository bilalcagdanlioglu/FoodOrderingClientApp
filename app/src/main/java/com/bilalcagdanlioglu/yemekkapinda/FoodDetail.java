package com.bilalcagdanlioglu.yemekkapinda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.andremion.counterfab.CounterFab;
import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Database.Database;
import com.bilalcagdanlioglu.yemekkapinda.Model.Food;
import com.bilalcagdanlioglu.yemekkapinda.Model.Order;
import com.bilalcagdanlioglu.yemekkapinda.Model.Rating;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    CounterFab btnCart;
    FloatingActionButton btnRating;
    ElegantNumberButton numberButton;
    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference refFood;
    DatabaseReference ratingTbl;
    Food currentFood;
    RatingBar ratingBar;
    Button btnShowComment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );


        setContentView( R.layout.activity_food_detail );

        database = FirebaseDatabase.getInstance();
        refFood = database.getReference( "Food" );
        ratingTbl = database.getReference("Rating");

        numberButton = (ElegantNumberButton) findViewById( R.id.number_button );

        btnCart = (CounterFab) findViewById( R.id.btnCart );
        btnRating = findViewById( R.id.btn_rating );
        ratingBar = (RatingBar) findViewById( R.id.ratingBar );

        btnShowComment = findViewById( R.id.btnShowComment );
        btnShowComment.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FoodDetail.this, ShowComment.class);
                intent.putExtra( Common.INTENT_FOOD_ID,foodId );
                startActivity( intent);
            }
        } );

        btnRating.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        } );

        btnCart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database( getBaseContext() ).addToCart( new Order(
                        Common.currentUser.getPhone(),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ) );
                Toast.makeText( FoodDetail.this, "Yemekler sepete eklendi..", Toast.LENGTH_SHORT ).show();
            }
        } );

        btnCart.setCount( new Database(this).getCountCart(Common.currentUser.getPhone()) );

        food_name = findViewById( R.id.food_name );
        food_price = findViewById( R.id.food_price );
        food_description = findViewById( R.id.food_description );
        food_image = findViewById( R.id.img_food );
        collapsingToolbarLayout = findViewById( R.id.collapsing );
        collapsingToolbarLayout.setExpandedTitleTextAppearance( R.style.ExpandedAppbar );
        collapsingToolbarLayout.setCollapsedTitleTextAppearance( R.style.CollapsedAppbar );


        if (getIntent() != null)
            foodId = getIntent().getStringExtra( "FoodId" );
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet( getBaseContext() )) {
                getDetailFood( foodId );
                getRatingFood(foodId);
            } else {
                Toast.makeText( FoodDetail.this, "Lütfen internet bağlantınızı kontrol ediniz!", Toast.LENGTH_SHORT ).show();
                return;
            }
        }
    }

    private void getRatingFood(String foodId) {
        com.google.firebase.database.Query foodRating = ratingTbl.orderByChild( "foodId" ).equalTo( foodId );

        foodRating.addValueEventListener( new ValueEventListener() {
            int count=0, sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot:snapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt( item.getRateValue() );
                    count++;
                }
                if(count !=0 )
                {
                    float average = sum /count;
                    ratingBar.setRating( average );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        } );
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Gönder")
                .setNegativeButtonText("İptal")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("Yemeklerimizi yorumlayınız")
                .setDescription("Lütfen Yıldız ile puanlama yapınız")
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.black)
                .setHint("Lütfen yorum yazınız")
                .setHintTextColor(R.color.white)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimary)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }


    private void getDetailFood(String foodId) {
        refFood.child( foodId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentFood = snapshot.getValue( Food.class );

                Picasso.with( getBaseContext() ).load( currentFood.getImage() )
                        .into( food_image );

                collapsingToolbarLayout.setTitle( currentFood.getName() );
                food_price.setText( currentFood.getPrice() );
                food_name.setText( currentFood.getName() );
                food_description.setText( currentFood.getDescription() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf( value ),
                comments);

        ratingTbl.push()
                .setValue( rating )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText( FoodDetail.this, "Teşekkürler..", Toast.LENGTH_SHORT ).show();
                    }
                } );

        /*
        ratingTbl.child( Common.currentUser.getPhone() ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child( Common.currentUser.getPhone() ).exists())
                {
                    ratingTbl.child( Common.currentUser.getPhone() ).removeValue();
                    ratingTbl.child( Common.currentUser.getPhone() ).setValue( rating );
                }
                else
                {
                    ratingTbl.child( Common.currentUser.getPhone() ).setValue( rating );
                }
                Toast.makeText( FoodDetail.this, "Teşekkürler..", Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
        */
    }
}