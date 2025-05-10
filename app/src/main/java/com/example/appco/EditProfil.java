package com.example.appco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfil extends Activity {

    private EditText edtNom, edtEmail, edtTel, edtDescription;
    private ImageView imgProfil;
    private Button btnEnregistrer, btnAnnuler, btnChangerImage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;
    private Bitmap selectedBitmap = null; // For saving

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
        userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

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
        // Load text data from Firestore
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtNom.setText(documentSnapshot.getString("nom"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtTel.setText(documentSnapshot.getString("telephone"));
                edtDescription.setText(documentSnapshot.getString("description"));
            }
        });

        // Load image from Realtime Database
        FirebaseDatabase.getInstance().getReference()
                .child("userImages")
                .child(userId)
                .child("profileImage")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    String base64Image = dataSnapshot.getValue(String.class);
                    if (base64Image != null && !base64Image.isEmpty()) {
                        Log.d("IMAGE_LOAD", "Base64 récupérée : " + base64Image.substring(0, 30));
                        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        Glide.with(this)
                                .load(decodedBitmap)
                                .transform(new CircleCrop())
                                .into(imgProfil);
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

        // Save to Firestore (text fields only)
        db.collection("users").document(userId).update(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show();
                });

        // Save Base64 image to Realtime DB (if new image was selected)
        if (selectedBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            FirebaseDatabase.getInstance().getReference()
                    .child("userImages")
                    .child(userId)
                    .child("profileImage")
                    .setValue(base64Image)
                    .addOnSuccessListener(aVoid -> Log.d("IMAGE_SAVE", "Image enregistrée avec succès"))
                    .addOnFailureListener(e -> Log.e("IMAGE_SAVE", "Erreur d’enregistrement image", e));

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                Glide.with(this)
                        .load(selectedBitmap)
                        .transform(new CircleCrop())
                        .into(imgProfil);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur de lecture d'image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("IMAGE_PICKER", "Échec de sélection d'image");
        }
    }
}
