package it.units.simandroid.progetto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

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
            Intent goToRegistrationForm = new Intent(this, RegistrationActivity.class);
            startActivity(goToRegistrationForm);
        });
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