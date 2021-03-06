package com.bilalcagdanlioglu.yemekkapinda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class Login extends AppCompatActivity {
    Button btnLogin;
    EditText edtPhone , edtPassword;
    TextView txtForgotPwd;
    FirebaseDatabase database;
    DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_login );

        edtPhone = findViewById( R.id.edtPhone );
        edtPassword = findViewById( R.id.edtPassword );
        btnLogin = findViewById( R.id.btnLogin );
        txtForgotPwd = findViewById( R.id.txtForgotPwd );

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPwdDialog();
            }
        } );

        btnLogin.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet( getBaseContext() )) {

                    ProgressDialog dialog = new ProgressDialog( Login.this );
                    dialog.setMessage( "L??tfen Bekleyiniz.." );
                    dialog.show();
                    table_user.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child( edtPhone.getText().toString() ).exists()) {
                                dialog.dismiss();
                                User user = snapshot.child( edtPhone.getText().toString() ).getValue( User.class );
                                user.setPhone( edtPhone.getText().toString() );
                                if (user.getPassword().equals( edtPassword.getText().toString() )) {
                                    Intent homeIntent = new Intent( Login.this, Home.class );
                                    Common.currentUser = user;
                                    startActivity( homeIntent );
                                    finish();

                                    table_user.removeEventListener( this );
                                } else {
                                    Toast.makeText( Login.this, "Hatal?? ??ifre!", Toast.LENGTH_SHORT ).show();
                                }
                            } else {
                                dialog.dismiss();
                                Toast.makeText( Login.this, "Kullan??c?? mevcut de??il", Toast.LENGTH_SHORT ).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    } );
                }
                else{
                    Toast.makeText( Login.this, "L??tfen internet ba??lant??n??z?? kontrol ediniz!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "??ifremi unuttum" );
        builder.setMessage( "G??venlik kodunu giriniz" );

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate( R.layout.forgot_password_layout,null );

        builder.setView( forgot_view );
        builder.setIcon( R.drawable.ic_baseline_security_24 );

        EditText edtPhone = forgot_view.findViewById( R.id.edtPhone );
        EditText edtSecureCode = forgot_view.findViewById( R.id.edtSecureCode );

        builder.setPositiveButton( "EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                table_user.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.child( edtPhone.getText().toString() )
                                .getValue(User.class);
                        if(user.getSecureCode().equals( edtSecureCode.getText().toString() ))
                        {
                            Toast.makeText( Login.this, "??ifreniz: "+user.getPassword(), Toast.LENGTH_LONG ).show();
                        }
                        else
                        {
                            Toast.makeText( Login.this, "Hatal?? G??venlik Kodu !!", Toast.LENGTH_LONG ).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
            }
        } );

        builder.setNegativeButton( "HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        } );

        builder.show();
    }
}