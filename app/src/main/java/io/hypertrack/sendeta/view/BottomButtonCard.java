package io.hypertrack.sendeta.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.consumer.utils.AnimationUtils;
import com.hypertrack.lib.internal.consumer.view.RippleView;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman on 28/07/17.
 */

public class BottomButtonCard extends RelativeLayout {
    private final Context mContext;
    RelativeLayout bottomCardLayout, trackingURLLayout;
    TextView titleText, descriptionText, actionButtonText, trackingURL;
    ImageView actionLoader;
    RippleView closeButton, actionButton;
    Button trackingURLCopyButton;
    ButtonListener buttonListener;
    ActionType type;

    public enum ActionType {
        START_TRACKING,
        CONFIRM_LOCATION,
        SHARE_TRACKING_URL,
        SHARE_BACK_LOCATION
    }

    public BottomButtonCard(Context context) {
        this(context, null);
    }

    public BottomButtonCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomButtonCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.bottom_buttom_card_view, this, true);
        initiateView();
        type = ActionType.START_TRACKING;
    }

    public void setButtonClickListener(ButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    private void initiateView() {
        bottomCardLayout = (RelativeLayout) findViewById(R.id.bottom_card_layout);
        titleText = (TextView) findViewById(R.id.title_text);
        descriptionText = (TextView) findViewById(R.id.description_text);
        actionButton = (RippleView) findViewById(R.id.action_button);
        actionButtonText = (TextView) findViewById(R.id.action_button_text);
        actionLoader = (ImageView) findViewById(R.id.action_loader);
        closeButton = (RippleView) findViewById(R.id.close_button);
        trackingURLLayout = (RelativeLayout) findViewById(R.id.tracking_url_layout);
        trackingURL = (TextView) findViewById(R.id.tracking_url);
        trackingURLCopyButton = (Button) findViewById(R.id.tracking_url_copy_button);

        closeButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (buttonListener != null) {
                    buttonListener.OnCloseButtonClick();
                }
            }
        });

        actionButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (buttonListener != null) {
                    buttonListener.OnActionButtonClick();
                }
            }
        });

        trackingURLCopyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonListener != null) {
                    buttonListener.OnCopyButtonClick();
                    trackingURLCopyButton.setEnabled(false);
                    trackingURLCopyButton.setText("Copied");
                }
            }
        });
    }

    public void setActionType(ActionType type) {
        this.type = type;
    }

    public ActionType getActionType() {
        return type;
    }

    public void setTitleText(String title) {
        this.titleText.setText(title);
        titleText.setVisibility(VISIBLE);
    }

    public void setDescriptionText(String description) {
        if (HTTextUtils.isEmpty(description)) {
            descriptionText.setText("");
            descriptionText.setVisibility(GONE);
        } else {
            descriptionText.setText(description);
            descriptionText.setVisibility(VISIBLE);
        }
    }

    public void setErrorText(String text) {
        descriptionText.setError(text);
    }

    public void showCloseButton() {
        closeButton.setVisibility(VISIBLE);
    }

    public void hideCloseButton() {
        closeButton.setVisibility(GONE);
    }

    public void setActionButtonText(String actionText) {
        actionButtonText.setVisibility(VISIBLE);
        actionButtonText.setText(actionText);
    }

    public void showBottomCardLayout() {
        hideProgress();
        actionButtonText.setVisibility(VISIBLE);
        AnimationUtils.expand(this, AnimationUtils.DURATION_DEFAULT_VALUE_ANIMATION);
    }

    public void hideBottomCardLayout() {
        hideProgress();
        AnimationUtils.collapse(this, AnimationUtils.DURATION_DEFAULT_VALUE_ANIMATION, trackingURLLayout);
    }

    public void startProgress() {
        actionButtonText.setVisibility(GONE);
        actionLoader.setVisibility(View.VISIBLE);
        Animation rotationAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(),
                R.anim.rotate);
        rotationAnim.setFillAfter(true);
        actionLoader.startAnimation(rotationAnim);
    }

    public void hideActionButton() {
        AnimationUtils.collapse(actionButton);
    }

    public void showActionButton() {
        AnimationUtils.expand(actionButton);
    }

    public void hideProgress() {
        actionLoader.setVisibility(View.GONE);
        actionLoader.clearAnimation();
    }

    public void showTrackingURLLayout() {
        AnimationUtils.expand(trackingURLLayout);
    }

    public void hideTrackingURLLayout() {
        trackingURLLayout.setVisibility(GONE);
    }

    public boolean isActionTypeConfirmLocation() {
        return type == ActionType.CONFIRM_LOCATION;
    }

    public boolean isActionTypeStartTracking() {
        return type == ActionType.START_TRACKING;
    }

    public boolean isActionTypeShareTrackingLink() {
        return type == ActionType.SHARE_TRACKING_URL;
    }

    public boolean isActionTypeShareBackLocation() {
        return type == ActionType.SHARE_BACK_LOCATION;
    }

    public void hideTitle() {
        titleText.setVisibility(GONE);
    }

    public void showTitle() {
        titleText.setVisibility(VISIBLE);
    }

    public void setTrackingURL(String URL) {
        trackingURL.setText(URL);
    }

    public interface ButtonListener {
        void OnCloseButtonClick();

        void OnActionButtonClick();

        void OnCopyButtonClick();
    }
}
