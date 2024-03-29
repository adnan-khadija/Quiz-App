package com.example.quizcontrole;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etMail, etPassword, etPassword1;
    private Button bRegister;
    private FirebaseAuth mAuth;
    private ImageView imageView;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        etName = findViewById(R.id.etName);
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        etPassword1 = findViewById(R.id.etPassword1);
        bRegister = findViewById(R.id.bRegister);
        imageView = findViewById(R.id.imageView);

        // Charger l'image depuis Firebase Storage et l'afficher dans l'ImageView
        StorageReference storageRef = storage.getReference().child("login/logoo.jpg");
        storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Picasso.get().load(downloadUri).into(imageView);
                } else {
                    // Gérer l'erreur de chargement de l'image
                    Toast.makeText(SignUpActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupérer les informations saisies
                String name = etName.getText().toString().trim();
                String email = etMail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etPassword1.getText().toString().trim();

                // Vérifier si les champs requis sont remplis
                if (TextUtils.isEmpty(email)) {
                    etMail.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Password is required");
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    etPassword1.setError("Confirm password is required");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    etPassword1.setError("Passwords do not match");
                    return;
                }

                // Créer un nouveau compte utilisateur avec email et mot de passe
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Enregistrement réussi, afficher un message à l'utilisateur et/ou rediriger
                                    Toast.makeText(SignUpActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();

                                    // Rediriger vers l'activité de connexion
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish(); // Optionnel : fermer l'activité d'inscription pour empêcher l'utilisateur de revenir en arrière
                                } else {
                                    // En cas d'échec de l'enregistrement, afficher un message à l'utilisateur
                                    Toast.makeText(SignUpActivity.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });
    }
}
