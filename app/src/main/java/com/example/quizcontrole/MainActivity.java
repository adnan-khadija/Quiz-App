package com.example.quizcontrole;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {

    private EditText etMail, etPassword;
    private Button bLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private ImageView imageView;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        bLogin = findViewById(R.id.bLogin);
        tvRegister = findViewById(R.id.tvRegister);
        imageView = findViewById(R.id.imageView);
        storage = FirebaseStorage.getInstance();

        // Charger l'image depuis Firebase Storage et l'afficher dans l'ImageView
        StorageReference storageRef = storage.getReference().child("login/logo.png");
        storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Picasso.get().load(downloadUri).into(imageView);
                } else {
                    // Gérer l'erreur de chargement de l'image
                    Toast.makeText(MainActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupérer les informations saisies
                String email = etMail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Vérifier si les champs requis sont remplis
                if (TextUtils.isEmpty(email)) {
                    etMail.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Password is required");
                    return;
                }

                // Authentifier l'utilisateur avec email et mot de passe
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Connexion réussie, afficher un message à l'utilisateur et/ou rediriger
                                    Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                    // Vous pouvez ajouter ici une redirection vers une autre activité si nécessaire
                                    Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                                    startActivity(intent);

                                } else {
                                    // En cas d'échec de connexion, afficher un message à l'utilisateur
                                    Toast.makeText(MainActivity.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Gérer le clic sur le lien de l'enregistrement
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rediriger vers l'activité d'inscription
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
    }
}
