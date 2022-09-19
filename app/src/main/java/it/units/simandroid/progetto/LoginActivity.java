package it.units.simandroid.progetto;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseInstance = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseInstance.getCurrentUser();
        if (currentUser != null) {
            // reload
        }
    }
}