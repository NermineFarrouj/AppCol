package com.example.appco;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton, loginRedirectButton;
    private Spinner spinnerRole;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.editTextEmailRegister);
        passwordInput = findViewById(R.id.editTextPasswordRegister);
        confirmPasswordInput = findViewById(R.id.editTextConfirmPasswordRegister);
        registerButton = findViewById(R.id.buttonRegister);
        loginRedirectButton = findViewById(R.id.buttonGoToLogin);
        spinnerRole = findViewById(R.id.spinnerRole);

        registerButton.setOnClickListener(v -> {
            Log.d("RegisterActivity", "Register button clicked!"); // Added log

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            String selectedRole = spinnerRole.getSelectedItem().toString();

            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email requis");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Format d'email invalide");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Mot de passe requis");
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Mot de passe trop court (min 6 caractères)");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Les mots de passe ne correspondent pas");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // Créer le document Firestore
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", email);
                                userMap.put("role", selectedRole);

                                db.collection("users").document(uid)
                                        .set(userMap)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(Register.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Register.this, Login.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("RegisterActivity", "Firestore Error: " + e.getMessage());
                                            Toast.makeText(Register.this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            String errorMessage = task.getException().getMessage();
                            Log.e("RegisterActivity", "Authentication Error: " + errorMessage);
                            if (errorMessage != null) {
                                if (errorMessage.contains("email-already-in-use")) {
                                    emailInput.setError("Cet email est déjà utilisé");
                                } else if (errorMessage.contains("weak-password")) {
                                    passwordInput.setError("Mot de passe trop faible");
                                } else {
                                    Toast.makeText(Register.this, "Inscription échouée : " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(Register.this, "Erreur d'inscription inconnue", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        loginRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}