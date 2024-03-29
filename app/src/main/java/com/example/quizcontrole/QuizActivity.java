package com.example.quizcontrole;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    private TextView questionTextView;
    private ImageView questionImageView;
    private RadioGroup radioGroupOptions;
    private Button nextButton;
    private ImageView imageView;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    List<DocumentSnapshot> quizDocuments = new ArrayList<>();
    int currentQuestionIndex = 0;
    int totalScore = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.textViewQuestion);
        questionImageView = findViewById(R.id.imageView);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        nextButton = findViewById(R.id.bNext);

        db = FirebaseFirestore.getInstance();

        // récupérer la question depuis Firestore
        loadQuizDocuments();
        // Configuration du gestionnaire d'événements du bouton "Next"
        Button bNext = findViewById(R.id.bNext);
        bNext.setOnClickListener(v -> {
            if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
                // Aucune option sélectionnée, afficher un message à l'utilisateur
                Toast.makeText(QuizActivity.this, "Veuillez sélectionner une réponse.", Toast.LENGTH_SHORT).show();
            } else {
                // Une option a été sélectionnée, passer à la prochaine question
                currentQuestionIndex++;
                displayQuestion();
            }
        });

    }


    private void loadQuizDocuments() {
        db.collection("quizes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        quizDocuments.add(documentSnapshot);
                    }
                    // Une fois tous les documents chargés, affichez la première question
                    displayQuestion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement des documents de quiz", e);
                });
    }

    private void displayQuestion() {
        if (currentQuestionIndex < quizDocuments.size()) {
            DocumentSnapshot currentDocument = quizDocuments.get(currentQuestionIndex);

            String question = currentDocument.getString("question");
            String imageUrl = currentDocument.getString("imag_url");
            List<String> options = (List<String>) currentDocument.get("options");
            String correctAnswer = currentDocument.getString("answer"); // Réponse correcte

            // Afficher la question
            TextView textViewQuestion = findViewById(R.id.textViewQuestion);
            textViewQuestion.setText(question);

            // Afficher l'image
            ImageView imageView = findViewById(R.id.imageView);
            Picasso.get().load(imageUrl).into(imageView);

            // Ajouter les options radio
            RadioGroup radioGroupOptions = findViewById(R.id.radioGroupOptions);
            radioGroupOptions.removeAllViews(); // Supprimer les vues précédentes
            for (int i = 0; i < options.size(); i++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(options.get(i));
                radioGroupOptions.addView(radioButton);
            }

            // Gérer la réponse de l'utilisateur
            radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedRadioButton = findViewById(checkedId);
                String selectedAnswer = selectedRadioButton.getText().toString();
                if (selectedAnswer.equals(correctAnswer)) {
                    // La réponse est correcte, incrémentez le score
                    totalScore++;
                }
            });
            Button bNext = findViewById(R.id.bNext);
            bNext.setOnClickListener(null); // Retirer le gestionnaire d'événements existant
            bNext.setOnClickListener(v -> {
                if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
                    // Aucune option sélectionnée, afficher un message à l'utilisateur
                    Toast.makeText(QuizActivity.this, "Veuillez sélectionner une réponse.", Toast.LENGTH_SHORT).show();
                } else {
                    // Une option a été sélectionnée, passer à la prochaine question
                    currentQuestionIndex++;
                    displayQuestion();
                }
            });
        } else {
            // Toutes les questions ont été répondues, affichez le score final
            displayFinalScore();
        }
    }


    // Méthode pour afficher le score final
    private void displayFinalScore() {
        setContentView(R.layout.activity_score);

        int percentageScore = (int) ((float) totalScore / quizDocuments.size() * 100); // Calcul du score en pourcentage
        TextView textViewFinalScore = findViewById(R.id.textViewFinalScore);
        textViewFinalScore.setText("Votre score final est : " + percentageScore + "%");

        // Configurer le bouton "Restart Quiz"
        Button buttonRestart = findViewById(R.id.buttonRestart);
        buttonRestart.setOnClickListener(v -> {
            // Redémarrer le quiz en réinitialisant les variables nécessaires
            currentQuestionIndex = 0;
            totalScore = 0;
            loadQuizDocuments(); // Recharger les documents de quiz pour recommencer
        });

        // Configurer le bouton "Exit Quiz"
        Button buttonExit = findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(v -> {
            finishAffinity(); // Terminer toutes les activités de l'application
            System.exit(0); // Terminer le processus de l'application
        });
        imageView = findViewById(R.id.imageView);
        storage = FirebaseStorage.getInstance();

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
                    Toast.makeText(QuizActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


}
