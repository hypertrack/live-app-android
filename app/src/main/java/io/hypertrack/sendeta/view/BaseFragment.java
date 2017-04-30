package io.hypertrack.sendeta.view;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 02/09/16.
 */
public class BaseFragment extends Fragment {

    private FrameLayout loadingLayout;
    private CardView loadingLayoutMessageContainer;
    private TextView loadingLayoutMessage;

    public void initLoader(View rootView) {
        loadingLayout = (FrameLayout) rootView.findViewById(R.id.loading_container);
        loadingLayoutMessage = (TextView) rootView.findViewById(R.id.loading_message);
        loadingLayoutMessageContainer = (CardView) rootView.findViewById(R.id.loading_message_container);
    }

    /**
     * used to display a loader within content section
     * [IMPORTANT] include "layout_loading" inside main data container of the view
     *
     * @param isEnabled true to enable/show loader, false to disable/hide
     */
    public void displayLoader(boolean isEnabled, String message) {

        //check if layouts available or not
        if (loadingLayout != null && loadingLayoutMessage != null && loadingLayoutMessageContainer != null) {
            loadingLayoutMessageContainer.setVisibility(View.GONE);

            if (isEnabled) {

                //if any message to be shown
                if (message != null && message.trim().length() > 0) {
                    loadingLayoutMessage.setText(message);
                    loadingLayoutMessageContainer.setVisibility(View.VISIBLE);
                }
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * used to display a loader within content section
     * [IMPORTANT] include "layout_loading" inside main data container of the view
     *
     * @param isEnabled true to enable/show loader, false to disable/hide
     */
    public void displayLoader(boolean isEnabled) {
        displayLoader(isEnabled, null);
    }
}