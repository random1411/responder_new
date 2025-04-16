package com.rescuereach.responder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.MenuItem;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button btnSetActive, btnSetInactive;
    private TextView roleTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String role;
    private NavigationDrawerHandler navigationDrawerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get role from intent extras
        role = getIntent().getStringExtra("role");

        // Setup DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Setup ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );

        // Initialize Navigation Drawer Handler
        navigationDrawerHandler = new NavigationDrawerHandler(this, drawerLayout, navigationView, toggle, auth);

        // Initialize views
        roleTextView = findViewById(R.id.roleTextView);
        btnSetActive = findViewById(R.id.btnSetActive);
        btnSetInactive = findViewById(R.id.btnSetInactive);

        // Display role in the UI
        roleTextView.setText("Role: " + role);

        // Setup button click listeners
        btnSetActive.setOnClickListener(v -> updateUserStatus("active"));
        btnSetInactive.setOnClickListener(v -> updateUserStatus("inactive"));

        fetchResponderInfo();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return navigationDrawerHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void fetchResponderInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference responderDoc = db.collection("responders").document(uid);

            responderDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String status = task.getResult().getString("status");
                    Log.d("RESPONDER_INFO", "Role: " + role + ", Status: " + status);
                    Toast.makeText(this, "Role: " + role + ", Status: " + status, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to fetch responder info", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserStatus(String status) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference responderDoc = db.collection("responders").document(uid);

            responderDoc.update("status", status)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onBackPressed() {
        navigationDrawerHandler.handleBackPress();
    }
}