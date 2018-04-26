package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hypertrack.lib.models.User;
import com.hypertrack.lib.tracking.MapProvider.MapFragmentView;
import com.hypertrack.lib.tracking.basemvp.BaseView;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

public class StartView extends BaseView {

    private TextView mUserName, mSummaryTitle;
    private ImageButton mSettingButton;

    public static StartView newInstance() {
        StartView startView = new StartView();
        Bundle bundle = new Bundle();
        bundle.putInt("type", MapFragmentView.Type.CUSTOM);
        startView.setArguments(bundle);
        return startView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        mParent = (RelativeLayout) ((LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.start_view_fragment, null);
        setType(MapFragmentView.Type.CUSTOM);
        initView();
        return view;
    }

    private void initView() {
        mUserName = mParent.findViewById(R.id.user_name);
        mSummaryTitle = mParent.findViewById(R.id.summary_title);
        mSettingButton = mParent.findViewById(R.id.setting);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Profile.class));
            }
        });
        addBottomView(mParent);
        setCTAButtonTitle("SHARE YOUR LOCATION");
        User user = SharedPreferenceManager.getHyperTrackLiveUser(getContext());
        if (user != null)
            mUserName.setText(String.format("Howdy %s!", user.getName()));
        mSummaryTitle.setText(R.string.summary_title);
        showCTAButton();
    }
}
