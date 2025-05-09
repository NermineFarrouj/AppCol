package com.example.appco;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfil extends Activity {

    private EditText edtNom, edtEmail, edtTel, edtDescription;
    private ImageView imgProfil;
    private Button btnEnregistrer, btnAnnuler, btnChangerImage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String userId = "user123"; // Replace this with actual user ID logic
    private String currentImageUrl;
    private Uri currentImageUri = null; // Will store new image URI if taken

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil);

        edtNom = findViewById(R.id.edtNom);
        edtEmail = findViewById(R.id.edtEmail);
        edtTel = findViewById(R.id.edtTel);
        edtDescription = findViewById(R.id.edtDescription);
        imgProfil = findViewById(R.id.imgProfil);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);
        btnAnnuler = findViewById(R.id.btnAnnuler);
        btnChangerImage = findViewById(R.id.btnChangerImage);

        loadUserProfile();

        btnChangerImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cameraOnly()
                    .start(101);
        });

        btnEnregistrer.setOnClickListener(v -> saveProfile());
        btnAnnuler.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtNom.setText(documentSnapshot.getString("nom"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtTel.setText(documentSnapshot.getString("telephone"));
                edtDescription.setText(documentSnapshot.getString("description"));
                currentImageUrl = documentSnapshot.getString("photoUrl");

                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(currentImageUrl)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_person)
                            .into(imgProfil);
                }
            }
        });
    }

    private void saveProfile() {
        String nom = edtNom.getText().toString();
        String email = edtEmail.getText().toString();
        String tel = edtTel.getText().toString();
        String desc = edtDescription.getText().toString();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("nom", nom);
        profileData.put("email", email);
        profileData.put("telephone", tel);
        profileData.put("description", desc);

        if (currentImageUri != null) {
            uploadImageAndSave(profileData);
        } else {
            updateProfileInFirestore(profileData);
        }
    }

    private void uploadImageAndSave(Map<String, Object> data) {
        StorageReference imgRef = storage.getReference().child("profile_pics/" + userId + ".jpg");
        imgRef.putFile(currentImageUri)
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            data.put("photoUrl", uri.toString());
                            updateProfileInFirestore(data);
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur de téléversement de l'image", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateProfileInFirestore(Map<String, Object> data) {
        db.collection("users").document(userId).update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            currentImageUri = data.getData();
            Glide.with(this)
                    .load(currentImageUri)
                    .transform(new CircleCrop())
                    .into(imgProfil);
        }
    }
}
