package com.example.appco;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.content.Intent;
import android.view.View;

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
        boutonModifierProfil.setOnClickListener(v -> {
            Intent intent = new Intent(Profil.this, EditProfil.class);
            intent.putExtra("proprietaireId", proprietaireId);
            startActivity(intent);
        });
    }

    private void chargerInformationsProfil(String proprietaireId) {
        // Charger les données depuis Firestore (texte)
        db.collection("users").document(proprietaireId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nom = documentSnapshot.getString("nom");
                        String email = documentSnapshot.getString("email");
                        String telephone = documentSnapshot.getString("telephone");
                        String description = documentSnapshot.getString("description");

                        textViewNomCompletConsultation.setText(nom != null ? nom : "");
                        textViewEmailConsultation.setText(email != null ? email : "");
                        textViewTelephoneConsultation.setText(telephone != null ? telephone : "");
                        textViewDescriptionPersonnelleConsultation.setText(description != null ? description : "");
                    } else {
                        Log.d(TAG, "Document du propriétaire non trouvé.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors du chargement des informations", e));

        // Charger l'image depuis Realtime Database
        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference().child("userImages").child(proprietaireId).child("profileImage");
        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String base64Image = dataSnapshot.getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    Glide.with(Profil.this)
                            .load(decodedBitmap)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imageViewPhotoProfilConsultation);
                } else {
                    imageViewPhotoProfilConsultation.setImageResource(R.drawable.baseline_person_24); // Image par défaut
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Erreur lors de la récupération de l'image", databaseError.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proprietaireId != null) {
            chargerInformationsProfil(proprietaireId);
        }
    }
}
