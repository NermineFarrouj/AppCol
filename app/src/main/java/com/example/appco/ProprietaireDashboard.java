package com.example.appco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ProprietaireDashboard extends AppCompatActivity {

    Button btnAnnonces, btnAvisLocataires, btnDonnerAvis, btnMessages, btnPaiements, btnProfil,btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prop);

        btnAnnonces = findViewById(R.id.btnAnnonces);
        btnAvisLocataires = findViewById(R.id.btnAvisLocataires);
        btnDonnerAvis = findViewById(R.id.btnDonnerAvis);
        btnMessages = findViewById(R.id.btnMessages);
        btnPaiements = findViewById(R.id.btnPaiements);
        btnProfil = findViewById(R.id.btnProfil);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProprietaireDashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // termine l'activité actuelle
        });


        btnAnnonces.setOnClickListener(v -> {
            // rediriger vers l’activité d’annonces du propriétaire
            startActivity(new Intent(this, Annonces.class));
        });




        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, Messagerie.class));
        });

        btnPaiements.setOnClickListener(v -> {
            startActivity(new Intent(this, Payment.class));
        });

        btnProfil.setOnClickListener(v -> {
            startActivity(new Intent(this, Profil.class));
        });
    }
}
