package com.example.appco;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profil extends AppCompatActivity {

    private static final String TAG = "ConsulterProfilAct";
    private ImageView imageViewPhotoProfilConsultation;
    private TextView textViewNomCompletConsultation;
    private TextView textViewEmailConsultation;
    private TextView textViewTelephoneConsultation;
    private TextView textViewDescriptionPersonnelleConsultation;
    private Button boutonModifierProfil;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String proprietaireId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Récupérer l'ID de l'utilisateur connecté
        if (mAuth.getCurrentUser() != null) {
            proprietaireId = mAuth.getCurrentUser().getUid();
        } else {
            // Gérer le cas où l'utilisateur n'est pas connecté
            Log.e(TAG, "Utilisateur non connecté.");
            // Rediriger vers l'écran de connexion ou afficher un message d'erreur
            finish();
            return;
        }

        // Liaison des éléments de la mise en page
        imageViewPhotoProfilConsultation = findViewById(R.id.imageViewPhotoProfilConsultation);
        textViewNomCompletConsultation = findViewById(R.id.textViewNomCompletConsultation);
        textViewEmailConsultation = findViewById(R.id.textViewEmailConsultation);
        textViewTelephoneConsultation = findViewById(R.id.textViewTelephoneConsultation);
        textViewDescriptionPersonnelleConsultation = findViewById(R.id.textViewDescriptionPersonnelleConsultation);
        boutonModifierProfil = findViewById(R.id.boutonModifierProfil);

        // Affichage des informations du profil
        chargerInformationsProfil(proprietaireId);

        // Listener pour le bouton "Modifier"
        boutonModifierProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Démarrer l'activité ModifierProfilActivity en passant l'ID de l'utilisateur
                Intent intent = new Intent(Profil.this, EditProfil.class);
                intent.putExtra("proprietaireId", proprietaireId);
                startActivity(intent);
            }
        });
    }

    private void chargerInformationsProfil(String proprietaireId) {
        DocumentReference docRef = db.collection("proprietaires").document(proprietaireId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nom = documentSnapshot.getString("nom");
                String email = documentSnapshot.getString("email");
                String telephone = documentSnapshot.getString("telephone");
                String description = documentSnapshot.getString("description");
                String imageUrl = documentSnapshot.getString("imageUrl");

                textViewNomCompletConsultation.setText(nom != null ? nom : "");
                textViewEmailConsultation.setText(email != null ? email : "");
                textViewTelephoneConsultation.setText(telephone != null ? telephone : "");
                textViewDescriptionPersonnelleConsultation.setText(description != null ? description : "");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(Profil.this)
                            .load(imageUrl)
                            // Placeholder en cas de chargement
                            .error(android.R.drawable.ic_menu_report_image) // Image en cas d'erreur
                            .into(imageViewPhotoProfilConsultation);
                } else {
                    imageViewPhotoProfilConsultation.setImageResource(R.drawable.baseline_person_24); // Image par défaut
                }
            } else {
                Log.d(TAG, "Document du propriétaire non trouvé.");
                // Gérer le cas où les données n'existent pas
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Erreur lors du chargement du profil.", e));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lorsque l'activité revient au premier plan, rechargez les informations du profil
        // pour afficher les éventuelles modifications.
        if (proprietaireId != null) {
            chargerInformationsProfil(proprietaireId);
        }
    }
}