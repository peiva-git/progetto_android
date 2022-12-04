package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.fragments.LoginFragment.AUTH_TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.fragments.directions.RegistrationFragmentDirections;
import it.units.simandroid.progetto.viewmodels.UsersViewModel;

public class RegistrationFragment extends Fragment {

    private TextInputEditText userEmail;
    private TextInputEditText userEmailConfirm;
    private TextInputEditText userPassword;
    private TextInputEditText userPasswordConfirm;
    private MaterialButton registrationButton;
    private FirebaseAuth authentication;
    private TextInputEditText userName;
    private TextInputEditText userSurname;
    private MaterialButton cancelButton;
    private TextInputLayout userNameLayout;
    private TextInputLayout userSurnameLayout;
    private TextInputLayout userEmailLayout;
    private TextInputLayout userEmailConfirmLayout;
    private TextInputLayout userPasswordLayout;
    private TextInputLayout userPasswordConfirmLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_registration, container, false);

        userName = fragmentView.findViewById(R.id.user_name);
        userNameLayout = fragmentView.findViewById(R.id.user_name_layout);
        userSurname = fragmentView.findViewById(R.id.user_surname);
        userSurnameLayout = fragmentView.findViewById(R.id.user_surname_layout);
        userEmail = fragmentView.findViewById(R.id.registration_email);
        userEmailLayout = fragmentView.findViewById(R.id.registration_email_layout);
        userEmailConfirm = fragmentView.findViewById(R.id.registration_email_confirm);
        userEmailConfirmLayout = fragmentView.findViewById(R.id.registration_email_confirm_layout);
        userPassword = fragmentView.findViewById(R.id.registration_password);
        userPasswordLayout = fragmentView.findViewById(R.id.registration_password_layout);
        userPasswordConfirm = fragmentView.findViewById(R.id.registration_password_confirm);
        userPasswordConfirmLayout = fragmentView.findViewById(R.id.registration_password_confirm_layout);
        registrationButton = fragmentView.findViewById(R.id.registration_button);
        cancelButton = fragmentView.findViewById(R.id.cancel_registration_button);

        registrationButton.setOnClickListener(registrationButtonView -> {
            if (!validateForm()) {
                return;
            }
            authentication.createUserWithEmailAndPassword(Objects.requireNonNull(userEmail.getText()).toString(), userPassword.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "User " + authentication.getUid() + " created successfully");
                            // always non-null, checked with formValidation and user has signed-in
                            User newUser = new User(
                                    userEmail.getText().toString(),
                                    userName.getText().toString(),
                                    userSurname.getText().toString(),
                                    authentication.getUid());
                            UsersViewModel viewModel = new ViewModelProvider(RegistrationFragment.this).get(UsersViewModel.class);
                            viewModel.setUser(newUser)
                                    .addOnSuccessListener(newUserTask -> Log.d(AUTH_TAG, "User " + newUser.getId() + " added to the database"))
                                    .addOnFailureListener(exception -> Log.w(AUTH_TAG, "Unable to add user " + newUser.getId() + " to the database", exception));
                            NavHostFragment.findNavController(this)
                                    .navigate(RegistrationFragmentDirections.actionRegistrationFragmentToTripsFragment());
                        } else {
                            Log.w(AUTH_TAG, "Failed to create new user", task.getException());
                            // always non-null, checked if task has failed
                            Snackbar.make(requireView(), task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                            // updateUI(null)
                        }
                    });
        });
        cancelButton.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return fragmentView;
    }

    private boolean validateForm() {
        boolean isFormValid = true;
        Editable name = userName.getText();
        Editable surname = userSurname.getText();
        Editable email = userEmail.getText();
        Editable confirmEmail = userEmailConfirm.getText();
        Editable password = userPassword.getText();
        Editable confirmPassword = userPasswordConfirm.getText();

        if (TextUtils.isEmpty(name)) {
            userNameLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            userNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(surname)) {
            userSurnameLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            userSurnameLayout.setError(null);
        }
        if (TextUtils.isEmpty(email)) {
            userEmailLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            userEmailLayout.setError(null);
            if (!TextUtils.equals(email, confirmEmail)) {
                userEmailLayout.setError(getString(R.string.email_mismatch));
                userEmailConfirmLayout.setError(getString(R.string.email_mismatch));
                isFormValid = false;
            } else {
                userEmailLayout.setError(null);
                userEmailConfirmLayout.setError(null);
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    userEmailLayout.setError(getString(R.string.email_bad_format));
                    isFormValid = false;
                } else {
                    userEmailLayout.setError(null);
                }
            }
        }
        if (TextUtils.isEmpty(password)) {
            userPasswordLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            userPasswordLayout.setError(null);
            if (!TextUtils.equals(password, confirmPassword)) {
                userPasswordLayout.setError(getString(R.string.password_mismatch));
                userPasswordConfirmLayout.setError(getString(R.string.password_mismatch));
                isFormValid = false;
            } else {
                userPasswordLayout.setError(null);
                userPasswordConfirmLayout.setError(null);
                if (password.length() < 6) {
                    userPasswordLayout.setError(getString(R.string.password_weak));
                    isFormValid = false;
                } else {
                    userPasswordLayout.setError(null);
                }
            }
        }
        return isFormValid;
    }
}