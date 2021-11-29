package com.example.automatedtourguidesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private FirebaseAuth auth;
    private Intent intent;
    private ProgressBar progressBar;
    private EditText editTextpassword, editTextemail;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseUser user;
    private ValueEventListener a;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        progressBar = findViewById(R.id.login_progress);
        progressBar.bringToFront();
        editTextpassword = findViewById(R.id.password_edit_text);
        editTextemail = findViewById(R.id.email_edit_text);

        //sign in
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);

        //intent to register activity
        findViewById(R.id.register).setOnClickListener(this);

        //intent to reset Password activity
        findViewById(R.id.reset_password).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
//                finish();
                break;
            case R.id.email_sign_in_button:

                String email = editTextemail.getText().toString().trim();
                String password = editTextpassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Email address cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                } else if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);

                //create user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                } else {
                                    user=auth.getCurrentUser();
                                    a = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            //check if the user is user or coach
                                            if (dataSnapshot.exists()) {
                                                if(dataSnapshot.child("User_Information").child("type").getValue(String.class).equals("user")) {
                                                    progressBar.setVisibility(View.GONE);
                                                    intent = new Intent(LoginActivity.this, HomeActivity.class);

                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                                                }
                                            }else{
                                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                            }
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    };

                                    ref.child(user.getUid()).addListenerForSingleValueEvent(a);

//                                    progressBar.setVisibility(View.GONE);
//                                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
//                                    startActivity(intent);
////                                    finish();
                                }
                            }
                        });
                break;
            case R.id.reset_password:
                intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
//                finish();
                break;
        }

    }
}
