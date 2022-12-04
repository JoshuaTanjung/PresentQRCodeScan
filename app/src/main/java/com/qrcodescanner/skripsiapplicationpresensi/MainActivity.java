package com.qrcodescanner.skripsiapplicationpresensi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://researchproject2-ecd18-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText id = findViewById(R.id.et_username);
        final EditText Password = findViewById(R.id.et_password);
        final Button loginBtn = findViewById(R.id.btn_login);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String IdTxt = id.getText().toString();
                final String PasswordTxt = Password.getText().toString();

                if(IdTxt.isEmpty() || PasswordTxt.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter your email or password", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // check if email is exist in firebase database
                            if(snapshot.hasChild(IdTxt)){

                                // mobile is exist in firebase database
                                // now get password of students firebase data and match if with user entered password

                                final String getPassword = snapshot.child(IdTxt).child("password").getValue(String.class);
                                final String getName = snapshot.child(IdTxt).child("name").getValue(String.class);
                                final String getResidence = snapshot.child(IdTxt).child("residence").getValue(String.class);


                                if(getPassword.equals(PasswordTxt)){
                                    Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                                    // Open Layout2
                                    Intent menu_scan = new Intent(MainActivity.this, layout2.class);
                                    menu_scan.putExtra("id-key", IdTxt);
                                    menu_scan.putExtra("name-key", getName);
                                    menu_scan.putExtra("residence-key", getResidence);
                                    startActivity(menu_scan);
                                    finish();

                                } else {
                                    Toast.makeText(MainActivity.this, "Wrong Email/Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong Email/Password", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }
}