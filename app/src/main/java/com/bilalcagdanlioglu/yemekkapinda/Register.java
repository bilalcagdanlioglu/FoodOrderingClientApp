package com.bilalcagdanlioglu.yemekkapinda;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bilalcagdanlioglu.yemekkapinda.Common.Common;
import com.bilalcagdanlioglu.yemekkapinda.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Register extends AppCompatActivity {

    Button btnRegister;
    EditText edtPhone,edtName, edtPassword,edtSecureCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_register );

        btnRegister= findViewById( R.id.btnRegister );
        edtName= findViewById( R.id.edtName );
        edtPhone= findViewById( R.id.edtPhone );
        edtPassword= findViewById( R.id.edtPassword );
        edtSecureCode= findViewById( R.id.edtSecureCode );

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference table_user = database.getReference("User");

        btnRegister.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (Common.isConnectedToInternet( getBaseContext() )) {

                        ProgressDialog dialog = new ProgressDialog( Register.this );
                        dialog.setMessage( "Lütfen Bekleyiniz.." );
                        dialog.show();

                        table_user.addValueEventListener( new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child( edtPhone.getText().toString() ).exists()) {
                                    dialog.dismiss();
                                    Toast.makeText( Register.this, "Telefon numarası daha önce kullanılmış", Toast.LENGTH_SHORT ).show();
                                } else {
                                    dialog.dismiss();
                                    User user = new User( edtName.getText().toString(),
                                            edtPassword.getText().toString(),
                                            edtSecureCode.getText().toString());
                                    table_user.child( edtPhone.getText().toString() ).setValue( user );
                                    Toast.makeText( Register.this, "Kayıt Başarılı", Toast.LENGTH_SHORT ).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        } );
                    }
                    else
                    {
                        Toast.makeText( Register.this, "Lütfen internet bağlantınızı kontrol ediniz!", Toast.LENGTH_SHORT ).show();
                        return;
                    }
            }
        } );
    }
}