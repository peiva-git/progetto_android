package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.fragments.LoginFragment.AUTH_TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.fragments.directions.RegistrationFragmentDirections;

public class RegistrationFragment extends Fragment {

    private EditText userEmail;
    private EditText userEmailConfirm;
    private EditText userPassword;
    private EditText userPasswordConfirm;
    private Button registrationButton;
    private FirebaseAuth authentication;
    private EditText userName;
    private EditText userSurname;
    private FirebaseDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance(DB_URL);
        authentication = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        userName = view.findViewById(R.id.user_name);
        userSurname = view.findViewById(R.id.user_surname);
        userEmail = view.findViewById(R.id.registration_email);
        userEmailConfirm = view.findViewById(R.id.registration_email_confirm);
        userPassword = view.findViewById(R.id.registration_password);
        userPasswordConfirm = view.findViewById(R.id.registration_password_confirm);
        registrationButton = view.findViewById(R.id.registration_button);

        registrationButton.setOnClickListener(registrationButtonView -> {
            if (!validateForm()) {
                return;
            }
            authentication.createUserWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "User created successfully");
                            User newUser = new User(
                                    userEmail.getText().toString(),
                                    userName.getText().toString(),
                                    userSurname.getText().toString(),
                                    authentication.getUid());
                            database.getReference("users").child(newUser.getId()).setValue(newUser);
                            NavHostFragment.findNavController(this)
                                    .navigate(RegistrationFragmentDirections.actionRegistrationFragmentToTripsFragment());
                        } else {
                            Log.w(AUTH_TAG, "Failed to create new user", task.getException());
                            Snackbar.make(requireView(), R.string.new_user_failed, Snackbar.LENGTH_SHORT).show();
                            // updateUI(null)
                        }
                    });
        });

        return view;
    }

    private boolean validateForm() {
        boolean isFormValid = true;
        String name = userName.getText().toString();
        String surname = userSurname.getText().toString();
        String email = userEmail.getText().toString();
        String confirmEmail = userEmailConfirm.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userPasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(name)) {
            userName.setError(getString(R.string.email_required));
            isFormValid = false;
        } else {
            userName.setError(null);
        }
        if (TextUtils.isEmpty(surname)) {
            userSurname.setError(getString(R.string.email_required));
            isFormValid = false;
        } else {
            userSurname.setError(null);
        }
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