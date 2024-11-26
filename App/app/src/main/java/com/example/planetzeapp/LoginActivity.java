package com.example.planetzeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

//import java.util.HashMap;
//import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;

    private TextView forgotPasswordText;
    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                //if string is valid email
                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    if(!pass.isEmpty()){
                        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener
                                (new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            Toast.makeText(LoginActivity.this, "Login Success!",
                                                    Toast.LENGTH_SHORT).show();
                                            checkSetupQsAndNavigate();
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(LoginActivity.this, "Please verify your email before logging in.",
                                                    Toast.LENGTH_SHORT).show();

                                            if (user != null) {
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(LoginActivity.this,
                                                                            "Please verify your email.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(LoginActivity.this,
                                                                            "Failed to send verification email: "
                                                                                    + task.getException().getMessage(),
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Login Fail.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        loginPassword.setError("Password can't be empty.");
                    }
                }
                else if(email.isEmpty()){
                    loginEmail.setError("Email can't be empty.");
                }
                else{
                    loginEmail.setError("Email address invalid.");
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPassActivity.class));
            }
        });
    }
    private void checkSetupQsAndNavigate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            DatabaseReference finalQRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(uid)
                    .child("annual_answers").child("consumption").child("recycling_frequency");

            finalQRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String recyclingFrequency = snapshot.getValue(String.class);
                        if (recyclingFrequency != null &&
                                (recyclingFrequency.equalsIgnoreCase("never") ||
                                        recyclingFrequency.equalsIgnoreCase("occasionally") ||
                                        recyclingFrequency.equalsIgnoreCase("frequently") ||
                                        recyclingFrequency.equalsIgnoreCase("always"))) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            Log.d("Setup", "Navigating to EcoGaugeActivity");
                            finish();
                        } else {
                            startActivity(new Intent(LoginActivity.this, SignupQuestionsActivity.class));
                            Log.d("Setup", "Navigating to SignupQuestionsActivity");
                            finish();
                        }
                    } else {
                        startActivity(new Intent(LoginActivity.this, SignupQuestionsActivity.class));
                        Log.d("Setup", "Navigating to SignupQuestionsActivity");
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Error accessing data: ",
                            Toast.LENGTH_SHORT).show();
                    Log.e("Setup", "Database error: " + error.getMessage());
                }
            });
        }
    }
}