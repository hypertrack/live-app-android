package com.hypertrack.live.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.hypertrack.live.App;
import com.hypertrack.live.CognitoClient;
import com.hypertrack.live.LaunchActivity;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.utils.HTTextWatcher;

public class SignInFragment extends Fragment implements CognitoClient.Callback {

    private static final String TAG = App.TAG + "SignInFragment";

    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private View passwordClear;
    private TextView incorrect;

    private LoaderDecorator loader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(R.string.sign_in_to_your_account);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loader = new LoaderDecorator(getContext());

        emailAddressEditText = view.findViewById(R.id.email_address);
        passwordEditText = view.findViewById(R.id.password);
        passwordClear = view.findViewById(R.id.password_clear);
        incorrect = view.findViewById(R.id.incorrect);

        passwordEditText.addTextChangedListener(new HTTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                passwordClear.setVisibility(TextUtils.isEmpty(editable) ? View.INVISIBLE : View.VISIBLE);
            }
        });
        passwordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordEditText.setText("");
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incorrect.setText("");

                String email = emailAddressEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    startSignIn(email, password);
                }
            }
        });

        view.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    onBackPressed();
                }
            }
        });
    }

    public void startSignIn(String email, String password) {
        loader.start();
        CognitoClient.getInstance(getContext()).signIn(email, password, SignInFragment.this);
    }

    @Override
    public void onSuccess(CognitoClient mobileClient) {
        if (getActivity() != null) {
            loader.stop();
            ((LaunchActivity)getActivity()).onLoginCompleted();
        }
    }

    @Override
    public void onError(String message, Exception e) {
        if (getActivity() != null) {
            loader.stop();

            if (e instanceof UserNotConfirmedException) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
            } else if (e instanceof UserNotFoundException) {
                incorrect.setText(R.string.user_does_not_exist);
            } else if (e instanceof NotAuthorizedException) {
                incorrect.setText(R.string.incorrect_username_or_pass);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onBackPressed() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
