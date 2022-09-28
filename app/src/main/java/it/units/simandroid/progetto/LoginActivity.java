package it.units.simandroid.progetto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String AUTH_TAG = "AUTH";

    private FirebaseAuth firebaseInstance;
    private Button loginButton;
    private Button registrationButton;
    private EditText userEmail;
    private EditText userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseInstance = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        registrationButton = (Button) findViewById(R.id.signUpButton);
        userEmail = (EditText) findViewById(R.id.loginUsernameText);
        userPassword = (EditText) findViewById(R.id.loginPasswordText);

        registrationButton.setOnClickListener(view -> {
            Intent goToRegistrationForm = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(goToRegistrationForm);
        });
        loginButton.setOnClickListener(view -> {
            firebaseInstance.signInWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "Sign-in successful");
                        } else {
                            Log.w(AUTH_TAG, "Sign-in failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseInstance.getCurrentUser();
        if (currentUser != null) {
            Intent goToMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(goToMainActivity);
        }
    }
}