package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.LoginFragment.AUTH_TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

public class RegistrationFragment extends Fragment {

    private EditText userEmail;
    private EditText userEmailConfirm;
    private EditText userPassword;
    private EditText userPasswordConfirm;
    private Button registrationButton;
    private FirebaseAuth firebaseInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        userEmail = view.findViewById(R.id.registration_email);
        userEmailConfirm = view.findViewById(R.id.registration_email_confirm);
        userPassword = view.findViewById(R.id.registration_password);
        userPasswordConfirm = view.findViewById(R.id.registration_password_confirm);
        registrationButton = view.findViewById(R.id.registration_button);

        firebaseInstance = FirebaseAuth.getInstance();

        registrationButton.setOnClickListener(registrationButtonView -> {
            if (!validateForm()) {
                return;
            }
            firebaseInstance.createUserWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "User created successfully");
                            NavHostFragment.findNavController(this)
                                    .navigate(RegistrationFragmentDirections.actionRegistrationFragmentToTripsFragment());
                        } else {
                            Log.w(AUTH_TAG, "Failed to create new user", task.getException());
                            Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            // updateUI(null)
                        }
                    });
        });

        return view;
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