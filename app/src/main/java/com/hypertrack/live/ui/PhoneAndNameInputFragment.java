package com.hypertrack.live.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hypertrack.live.R;
import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.sdk.HyperTrack;

public class PhoneAndNameInputFragment extends Fragment {

    private TextView nameText;
    private TextView phoneText;

    private HyperTrack hyperTrack;

    public static Fragment newInstance(String pubKey) {
        PhoneAndNameInputFragment fragment = new PhoneAndNameInputFragment();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.PUBLISHABLE_KEY, pubKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_name_and_phone_input, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String pubKey = bundle.getString(MainActivity.PUBLISHABLE_KEY);
            hyperTrack = HyperTrack.getInstance(view.getContext(), pubKey);
        }

        nameText = view.findViewById(R.id.name);
        phoneText = view.findViewById(R.id.phone);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hyperTrack != null
                        && !nameText.getText().toString().isEmpty() && !phoneText.getText().toString().isEmpty()) {
                    String userName = nameText.getText().toString();
                    String userPhone = phoneText.getText().toString();

                    SharedHelper sharedHelper = SharedHelper.getInstance(getActivity());
                    sharedHelper.sharedPreferences().edit()
                            .putString(SharedHelper.USER_NAME_KEY, userName)
                            .putString(SharedHelper.USER_PHONE_KEY, userPhone)
                            .apply();

                    hyperTrack.setDeviceName(getString(R.string.phone_number_user_name, userPhone, userName))
                            .setDeviceMetadata(sharedHelper.getDeviceMetadata());

                    ((MainActivity)getActivity()).onStateUpdate();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.all_fields_required), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
