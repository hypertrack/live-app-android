package com.hypertrack.live.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.hypertrack.live.App;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;

public class ConfirmFragment extends Fragment implements HTMobileClient.Callback {

    private static final String TAG = App.TAG + "ConfirmFragment";

    private LoaderDecorator loader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loader = new LoaderDecorator(getContext());

        view.findViewById(R.id.verified).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.start();
                HTMobileClient.getInstance(getContext()).confirmSignIn(ConfirmFragment.this);
            }
        });

        view.findViewById(R.id.resend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.start();
                HTMobileClient.getInstance(getContext()).resendSignUp(new HTMobileClient.Callback() {
                    @Override
                    public void onSuccess(HTMobileClient mobileClient) {
                        if (getActivity() != null) {
                            loader.stop();
                        }
                    }

                    @Override
                    public void onError(String message, Exception e) {
                        if (getActivity() != null) {
                            loader.stop();
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onSuccess(HTMobileClient mobileClient) {
        if (getActivity() != null) {
            loader.stop();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onError(String message, Exception e) {
        if (getActivity() != null) {
            loader.stop();
            if (e instanceof UserNotConfirmedException) {
                Toast.makeText(getActivity(), getString(R.string.user_is_not_confirmed), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
