package com.rescuereach.responder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private AutoCompleteTextView stateDropdown;
    private CheckBox rememberMeCheckbox;
    private Button loginButton, forgotPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        stateDropdown = findViewById(R.id.dropdown_state);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up dropdown menu for selecting states
        setupStateDropdown();

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordButton.setOnClickListener(v -> openEmailApp());
    }

    private void setupStateDropdown() {
        // Set up State dropdown
        String[] stateOptions = getResources().getStringArray(R.array.indian_states);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, stateOptions);
        stateDropdown.setAdapter(stateAdapter);
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String selectedState = stateDropdown.getText().toString().trim();
                    if (!selectedState.isEmpty()) {
                        updateStateInFirebase(user.getUid(), selectedState);
                    } else {
                        Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStateInFirebase(String uid, String selectedState) {
        DocumentReference responderDocRef = db.collection("responders").document(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("state", selectedState);

        responderDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "State updated successfully", Toast.LENGTH_SHORT).show();
                    checkResponderRole(uid); // Proceed to check the role after updating the state
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update state: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkResponderRole(String uid) {
        DocumentReference responderDocRef = db.collection("responders").document(uid);
        responderDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                if (role != null) {
                    navigateToDashboard(role);
                } else {
                    Toast.makeText(this, "Access Denied: Role not found for responder", Toast.LENGTH_SHORT).show();
                    auth.signOut();
                }
            } else {
                Toast.makeText(this, "Access Denied: Responder not found", Toast.LENGTH_SHORT).show();
                auth.signOut();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private void openEmailApp() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps are shown
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin_rescuereach@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Forgot Password Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I forgot my password. Please assist.");

        try {
            // Show a chooser dialog to select the email app
            startActivity(Intent.createChooser(emailIntent, "Choose an email app"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email apps available", Toast.LENGTH_SHORT).show();
        }
    }
}