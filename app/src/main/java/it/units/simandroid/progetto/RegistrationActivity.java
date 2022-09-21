package it.units.simandroid.progetto;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userEmail;
    private EditText userEmailConfirm;
    private EditText userPassword;
    private EditText userPasswordConfirm;
    private Button registrationButton;
    private FirebaseAuth firebaseInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userEmail = (EditText) findViewById(R.id.registrationEmail);
        userEmailConfirm = (EditText) findViewById(R.id.registrationEmailConfirm);
        userPassword = (EditText) findViewById(R.id.registrationPassword);
        userPasswordConfirm = (EditText) findViewById(R.id.registrationPasswordConfirm);
        registrationButton = (Button) findViewById(R.id.registrationButton);

        firebaseInstance = FirebaseAuth.getInstance();

        registrationButton.setOnClickListener(view -> {
            if (!validateForm()) {
                return;
            }
            firebaseInstance.createUserWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d("AUTH", "create user success");
                            // updateUI(user)
                        } else {
                            Log.w("AUTH", "create user failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication falied", Toast.LENGTH_SHORT).show();
                            // updateUI(null)
                        }
                    });
        });
    }

    private boolean validateForm() {
        boolean isFormValid = true;
        String email = userEmail.getText().toString();
        String confirmEmail = userEmailConfirm.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userPasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(email)) {
            userEmail.setError(getString(R.string.email_required));
            isFormValid = false;
        } else {
            userEmail.setError(null);
        }
        if (!email.equals(confirmEmail)) {
            userEmail.setError(getString(R.string.email_mismatch));
            isFormValid = false;
        } else {
            userEmail.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            userPassword.setError(getString(R.string.password_required));
            isFormValid = false;
        } else {
            userPassword.setError(null);
        }
        if (!password.equals(confirmPassword)) {
            userPassword.setError(getString(R.string.password_mismatch));
            isFormValid = false;
        } else {
            userPassword.setError(null);
        }
        return isFormValid;
    }
}