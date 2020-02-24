package com.hypertrack.live.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.hypertrack.live.R;

public class WelcomeFragment extends Fragment {

    private TextView contentText;
    private Button startButton;

    private String pubKey;

    public static Fragment newInstance(String pubKey) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.PUBLISHABLE_KEY, pubKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pubKey = bundle.getString(MainActivity.PUBLISHABLE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentText = view.findViewById(R.id.contentText);
        startButton = view.findViewById(R.id.startButton);
        updateUI();
    }

    private void updateUI() {
        if (!TextUtils.isEmpty(pubKey)) {
            contentText.setText(R.string.we_need_your_permission);
            startButton.setText(R.string.allow_access);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT < 29) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION},
                                MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
            });
        } else {
            contentText.setText(R.string.welcome_steps);
            startButton.setText(R.string.i_m_ready);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), VerificationActivity.class);
                    startActivityForResult(intent, MainActivity.VERIFICATION_REQUEST);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.VERIFICATION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                pubKey = data.getStringExtra(MainActivity.PUBLISHABLE_KEY);
                updateUI();
            }
        }
    }
}
