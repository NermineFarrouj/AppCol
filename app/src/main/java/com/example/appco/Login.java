package com.example.appco;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, registerRedirectButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Vérifier si l'utilisateur est déjà connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            Log.d("DEBUG", "Role: " + role);

                            if ("Locataire".equals(role)) {
                                startActivity(new Intent(Login.this, LocataireDashboard.class));
                            } else if ("Propriétaire".equals(role)) {
                                startActivity(new Intent(Login.this, ProprietaireDashboard.class));
                            } else if ("admin".equals(role)) {
                                startActivity(new Intent(Login.this, AdminDashboard.class));
                            } else {
                                Toast.makeText(this, "Rôle inconnu : " + role, Toast.LENGTH_SHORT).show();
                            }

                            finish(); // ✅ Terminer Login pour empêcher le retour avec le bouton "back"
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la vérification du rôle", Toast.LENGTH_SHORT).show());
        }

        // ⚙️ Sinon on affiche normalement l'écran de connexion
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextEmailLogin);
        passwordInput = findViewById(R.id.editTextPasswordLogin);
        loginButton = findViewById(R.id.buttonLogin);
        registerRedirectButton = findViewById(R.id.buttonGoToRegister);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                db.collection("users").document(user.getUid())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String role = documentSnapshot.getString("role");
                                                Toast.makeText(Login.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                                                if ("Locataire".equals(role)) {
                                                    startActivity(new Intent(Login.this, LocataireDashboard.class));
                                                } else if ("Propriétaire".equals(role)) {
                                                    startActivity(new Intent(Login.this, ProprietaireDashboard.class));
                                                } else {
                                                    Toast.makeText(this, "Rôle inconnu : " + role, Toast.LENGTH_SHORT).show();
                                                }

                                                finish();
                                            } else {
                                                Toast.makeText(Login.this, "Profil utilisateur introuvable", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(Login.this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show());
                            }
                        } else {
                            Toast.makeText(Login.this, "Échec de connexion : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        registerRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
            finish();
        });
    }
}
