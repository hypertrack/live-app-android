package io.hypertrack.sendeta.view;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.lib.transmitter.model.HTShift;
import io.hypertrack.lib.transmitter.model.callback.HTShiftStatusCallback;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.FetchDriverIDForUserResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.ShiftManager;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.ShiftManagerListener;
import io.hypertrack.sendeta.util.LocationUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 12/09/16.
 */
public class ShiftsScreen extends BaseActivity {

    private final String TAG = ShiftsScreen.class.getSimpleName();

    private ShiftManager shiftManager;
    private String hyperTrackDriverID;

    private TextView driverIDTextView, shiftIDTextView;
    private Button shiftBtn;
    private LinearLayout shiftBtnLoader;

    private Call<FetchDriverIDForUserResponse> getDriverIDForUserCall;

    private View.OnClickListener shiftBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Check if Location Permission has been granted & Location has been enabled
            if (PermissionUtils.checkForPermission(ShiftsScreen.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    && LocationUtils.isLocationEnabled(ShiftsScreen.this)) {
                if (!ShiftManager.getSharedManager(ShiftsScreen.this).isShiftActive()) {

                    // Start the Shift
                    startShift();
                } else {

                    // Reset Current State
                    endShift();
                }
            } else {
                Toast.makeText(ShiftsScreen.this, R.string.invalid_current_location, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shift_screen);

        initToolbar("Shift Tracking");

        // Initialize UI
        driverIDTextView = (TextView) findViewById(R.id.shift_screen_driver_id);
        driverIDTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(driverIDTextView.getText());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Driver ID", driverIDTextView.getText());
                    clipboard.setPrimaryClip(clip);
                }

                Toast.makeText(ShiftsScreen.this, "Copied Driver ID", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        shiftIDTextView = (TextView) findViewById(R.id.shift_screen_shift_id);
        shiftIDTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (TextUtils.isEmpty(shiftIDTextView.getText()))
                    return true;

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(shiftIDTextView.getText());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Shift ID", shiftIDTextView.getText());
                    clipboard.setPrimaryClip(clip);
                }

                Toast.makeText(ShiftsScreen.this, "Copied Shift ID", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        shiftBtn = (Button) findViewById(R.id.shift_screen_shift_btn);
        shiftBtnLoader = (LinearLayout) findViewById(R.id.shift_screen_shift_btn_loader);
        if (shiftBtn != null) {
            shiftBtn.setOnClickListener(shiftBtnOnClickListener);
        }

        shiftManager = ShiftManager.getSharedManager(this);

        hyperTrackDriverID = SharedPreferenceManager.getShiftDriverID(this);
        if (TextUtils.isEmpty(hyperTrackDriverID)) {
            fetchShiftDriverID();

        } else {
            driverIDTextView.setText(hyperTrackDriverID);
        }

        restoreShiftStateIfNeeded();
    }

    private void restoreShiftStateIfNeeded() {
        //Check if there is any existing task to be restored
        if (shiftManager.shouldRestoreState()) {

            Log.v(TAG, "Shift is active");
            HTLog.i(TAG, "Shift restored successfully.");

            onShiftStart();

        } else {
            HTLog.e(TAG, "No shift to restore.");
        }
    }

    private void startShift() {
        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if DriverID is not available
        if (TextUtils.isEmpty(hyperTrackDriverID)) {
            Toast.makeText(ShiftsScreen.this, "Start shift failed because Driver ID is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        shiftBtnLoader.setVisibility(View.VISIBLE);

        shiftManager.startShift(hyperTrackDriverID, new HTShiftStatusCallback() {
            @Override
            public void onSuccess(HTShift shift) {
                Toast.makeText(ShiftsScreen.this, "Shift started successfully", Toast.LENGTH_SHORT).show();
                onShiftStart();

                shiftBtnLoader.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ShiftsScreen.this, "Shift start failed with error: " + e, Toast.LENGTH_SHORT).show();

                shiftBtnLoader.setVisibility(View.GONE);
            }
        });
    }

    private void endShift() {
        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
            return;
        }

        shiftBtnLoader.setVisibility(View.VISIBLE);

        shiftManager.endShift(new HTShiftStatusCallback() {
            @Override
            public void onSuccess(HTShift shift) {
                Toast.makeText(ShiftsScreen.this, "Shift ended successfully", Toast.LENGTH_SHORT).show();
                onEndShift();

                shiftBtnLoader.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ShiftsScreen.this, "Shift end failed with error: " + e, Toast.LENGTH_SHORT).show();

                shiftBtnLoader.setVisibility(View.GONE);
            }
        });
    }

    private void fetchShiftDriverID() {
        shiftBtnLoader.setVisibility(View.VISIBLE);

        // Initialize UserStore
        UserStore.sharedStore.initializeUser();
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            Toast.makeText(ShiftsScreen.this, "User Data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        getDriverIDForUserCall = sendETAService.getDriverIDForUser(user.getId());
        getDriverIDForUserCall.enqueue(new Callback<FetchDriverIDForUserResponse>() {
            @Override
            public void onResponse(Call<FetchDriverIDForUserResponse> call, Response<FetchDriverIDForUserResponse> response) {
                if (response.isSuccessful()) {

                    FetchDriverIDForUserResponse fetchDriverIDForUserResponse = response.body();
                    if (fetchDriverIDForUserResponse != null) {

                        if (!TextUtils.isEmpty(fetchDriverIDForUserResponse.getHypertrackDriverID())) {
                            hyperTrackDriverID = fetchDriverIDForUserResponse.getHypertrackDriverID();
                            driverIDTextView.setText(hyperTrackDriverID);
                            SharedPreferenceManager.setShiftDriverID(ShiftsScreen.this, hyperTrackDriverID);

                            shiftBtnLoader.setVisibility(View.GONE);
                            return;
                        }
                    }
                }

                Toast.makeText(ShiftsScreen.this, "Fetch Driver ID failed", Toast.LENGTH_SHORT).show();
                shiftBtnLoader.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<FetchDriverIDForUserResponse> call, Throwable t) {
                shiftBtnLoader.setVisibility(View.GONE);

                ErrorData errorData = new ErrorData();
                try {
                    errorData = NetworkUtils.processFailure(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showErrorMessage(errorData);
            }

            private void showErrorMessage(ErrorData errorData) {
                if (ShiftsScreen.this.isFinishing())
                    return;

                if (ErrorCodes.NO_INTERNET.equalsIgnoreCase(errorData.getCode()) ||
                        ErrorCodes.REQUEST_TIMED_OUT.equalsIgnoreCase(errorData.getCode())) {
                    Toast.makeText(ShiftsScreen.this, R.string.network_issue, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ShiftsScreen.this, R.string.generic_error_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onShiftStart() {

        // Set ShiftCompletedListener
        shiftManager.setShiftCompletedListener(new ShiftManagerListener() {
            @Override
            public void OnCallback() {
                Toast.makeText(ShiftsScreen.this, "Shift ended successfully", Toast.LENGTH_SHORT).show();
                onEndShift();
            }
        });

        shiftBtn.setText("End Shift");

        HTShift shift = shiftManager.getHyperTrackShift();

        if (shift != null) {
            driverIDTextView.setText(shift.getDriverID());
            shiftIDTextView.setText(shift.getId());
        }
    }

    private void onEndShift() {
        shiftIDTextView.setText("Shift has not started yet");
        shiftBtn.setText("Start Shift");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (getDriverIDForUserCall != null) {
            getDriverIDForUserCall.cancel();
        }
    }
}
