package it.units.simandroid.progetto.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.fragments.directions.LoginFragmentDirections;

public class LoginFragment extends Fragment {

    public static final String AUTH_TAG = "AUTH";

    private FirebaseAuth authentication;
    private MaterialButton loginButton;
    private MaterialButton registrationButton;
    private TextInputEditText userEmail;
    private TextInputEditText userPassword;
    private TextInputLayout userEmailLayout;
    private TextInputLayout userPasswordLayout;
    private LinearProgressIndicator progressIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);

        loginButton = fragmentView.findViewById(R.id.login_button);
        registrationButton = fragmentView.findViewById(R.id.sign_up_button);
        userEmail = fragmentView.findViewById(R.id.login_username_text);
        userEmailLayout = fragmentView.findViewById(R.id.login_username_layout);
        userPassword = fragmentView.findViewById(R.id.login_password_text);
        userPasswordLayout = fragmentView.findViewById(R.id.login_password_layout);
        progressIndicator = requireActivity().findViewById(R.id.progress_indicator);

        registrationButton.setOnClickListener(registrationButtonView ->
                NavHostFragment.findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()));
        loginButton.setOnClickListener(new View.OnClickListener() {
            private final AtomicBoolean alreadySentOnce = new AtomicBoolean(false);

            @Override
            public void onClick(View loginButtonView) {
                hideKeyboard();
                if (!LoginFragment.this.inputValidation()) {
                    return;
                }
                progressIndicator.show();
                if (!alreadySentOnce.get()) {
                    alreadySentOnce.set(true);
                    // always non-null on user input, checked with inputValidation
                    authentication.signInWithEmailAndPassword(Objects.requireNonNull(userEmail.getText()).toString(), Objects.requireNonNull(userPassword.getText()).toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(AUTH_TAG, "Sign-in successful");
                                    NavHostFragment.findNavController(LoginFragment.this).navigate(LoginFragmentDirections.actionLoginFragmentToTripsFragment());
                                } else {
                                    Log.w(AUTH_TAG, "Sign-in failed", task.getException());
                                    Snackbar.make(LoginFragment.this.requireView(), Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
                                    alreadySentOnce.set(false);
                                }
                                progressIndicator.hide();
                            });
                } else {
                    Log.d(AUTH_TAG, "Sign-in request already sent once without failing, waiting for response");
                }
            }
        });

        return fragmentView;
    }

    private void hideKeyboard() {
        userEmailLayout.setEnabled(false);
        userEmailLayout.setEnabled(true);
        userPasswordLayout.setEnabled(false);
        userPasswordLayout.setEnabled(true);
    }

    private boolean inputValidation() {
        boolean isInputValid = true;
        Editable email = userEmail.getText();
        Editable password = userPassword.getText();

        if (TextUtils.isEmpty(email)) {
            userEmailLayout.setError(getString(R.string.field_required));
            isInputValid = false;
        } else {
            userEmailLayout.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            userPasswordLayout.setError(getString(R.string.field_required));
            isInputValid = false;
        } else {
            userPasswordLayout.setError(null);
        }
        return isInputValid;
    }
}