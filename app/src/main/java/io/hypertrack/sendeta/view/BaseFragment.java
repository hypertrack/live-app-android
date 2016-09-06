package io.hypertrack.sendeta.view;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 02/09/16.
 */
public class BaseFragment extends Fragment {

    private FrameLayout loadingLayout;
    private CardView loadingLayoutMessageContainer;
    private TextView loadingLayoutMessage;

    public LinearLayout retryContainer;
    public TextView retryLayoutMessage;
    public Button retryLayoutButton;

    public void initRetryAndLoader(View rootView) {
        loadingLayout = (FrameLayout) rootView.findViewById(R.id.loading_container);
        loadingLayoutMessage = (TextView) rootView.findViewById(R.id.loading_message);
        loadingLayoutMessageContainer = (CardView) rootView.findViewById(R.id.loading_message_container);
        retryContainer = (LinearLayout) rootView.findViewById(R.id.retry_container);
        retryLayoutMessage = (TextView) rootView.findViewById(R.id.retry_message);
        retryLayoutButton = (Button) rootView.findViewById(R.id.retry_action);
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

    /**
     * Show retry layout within a container, width and height will be adjusted according to parent
     * [IMPORTANT not to use if display area is very small as scrolling is not supported
     *
     * @param message  message to be shown
     * @param listener action to be performed on retry button click
     * @return true or false depending on that view was successfully inflated or not
     */
    public boolean showRetryLayout(String message, View.OnClickListener listener) {

        if (retryContainer != null) {
            retryLayoutMessage.setText(message);
            retryLayoutButton.setOnClickListener(listener);
            retryContainer.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    /**
     * Hide retry layout, default is hidden
     *
     * @return true or false depending on that view was successfully inflated or not
     */
    public boolean hideRetryLayout() {

        if (retryContainer != null) {
            retryContainer.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}