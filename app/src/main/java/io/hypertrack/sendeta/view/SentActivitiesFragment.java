package io.hypertrack.sendeta.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 29/08/16.
 */
public class SentActivitiesFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_received_activities, container, false);

        return rootView;
    }
}
