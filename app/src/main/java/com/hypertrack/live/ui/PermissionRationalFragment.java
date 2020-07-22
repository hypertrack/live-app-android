package com.hypertrack.live.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hypertrack.live.PermissionsManager;
import com.hypertrack.live.R;

public class PermissionRationalFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView contentText = view.findViewById(R.id.contentText);
        Button startButton = view.findViewById(R.id.startButton);
        contentText.setText(R.string.we_need_your_permission);
        startButton.setText(R.string.allow_access);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                PermissionsManager.requestPermissions(getActivity(), MainActivity.PERMISSIONS_REQUEST);
            }
        });
    }

}
