package com.example.appco;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home extends AppCompatActivity {

    private Button signInButton;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signInButton = findViewById(R.id.buttonSignInInitial);
        signUpButton = findViewById(R.id.buttonSignUpInitial);

        signInButton.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, Login.class));
        });

        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, Register.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, get their role from Firestore
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    if (role != null) {
                        Log.d("InitialActivity", "User role: " + role);
                        switch (role) {
                            case "locataire":
                                startActivity(new Intent(Home.this, LocataireDashboard.class));
                                finish(); // Prevent going back to InitialActivity
                                break;
                            case "proprietaire":
                                startActivity(new Intent(Home.this, ProprietaireDashboard.class));
                                finish();
                                break;
                            case "admin":
                                startActivity(new Intent(Home.this, AdminDashboard.class));
                                finish();
                                break;
                            default:
                                // Handle unexpected role (maybe log out or show an error)
                                Log.w("InitialActivity", "Unknown user role: " + role);
                                Toast.makeText(Home.this, "Erreur: Rôle utilisateur inconnu.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Optionally sign out the user
                                break;
                        }
                    } else {
                        Log.w("InitialActivity", "User role is null in Firestore.");
                        // Optionally handle this case (e.g., navigate to a profile setup page)
                    }
                } else {
                    Log.d("InitialActivity", "User document not found in Firestore.");
                    // Optionally handle this case (e.g., navigate to a profile setup page)
                }
            }).addOnFailureListener(e -> {
                Log.e("InitialActivity", "Error fetching user role: ", e);
                Toast.makeText(Home.this, "Erreur de connexion.", Toast.LENGTH_SHORT).show();
            });
        }
        // If currentUser is null, the buttons will be visible by default
    }
}