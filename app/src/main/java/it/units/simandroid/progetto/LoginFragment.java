package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    public static final String AUTH_TAG = "AUTH";

    private FirebaseAuth firebaseInstance;
    private Button loginButton;
    private Button registrationButton;
    private EditText userEmail;
    private EditText userPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseInstance = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginButton = view.findViewById(R.id.login_button);
        registrationButton = view.findViewById(R.id.sign_up_button);
        userEmail = view.findViewById(R.id.login_username_text);
        userPassword = view.findViewById(R.id.login_password_text);

        registrationButton.setOnClickListener(registrationButtonView -> {
            NavHostFragment.findNavController(this)
                    .navigate(LoginFragmentDirections.actionLoginFragmentToRegistrationFragment());
        });
        loginButton.setOnClickListener(loginButtonView -> {
            firebaseInstance.signInWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "Sign-in successful");
                        } else {
                            Log.w(AUTH_TAG, "Sign-in failed", task.getException());
                            Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}