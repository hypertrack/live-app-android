package io.hypertrack.sendeta.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.internal.transmitter.models.UserActivity;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.ServiceNotificationParamsBuilder;

import java.util.ArrayList;
import java.util.UUID;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.ActionManager;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Placeline extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Placeline.class.getSimpleName();
    PlacelineFragment placelineFragment;
    FloatingActionButton floatingActionButton;
    private DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeline);
        initUI();

        // TODO: 17/09/17 Remove this and use proper way for this
        // Start Tracking, if not already tracked
        if (!HyperTrack.isTracking())
            startHyperTrackTracking();

        setHyperTrackCallbackForActivityUpdates();
    }

    private void initUI() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (HyperTrack.isTracking()) {
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
        }
        navigationView.setNavigationItemSelectedListener(this);
        placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Placeline.this, Home.class);
                intent.putExtra("class_from", Placeline.class.getSimpleName());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        if (item.getItemId() == R.id.edit_profile)
            startActivity(new Intent(this, Profile.class));
        else if (item.getItemId() == R.id.edit_activity) {
            startActivity(new Intent(this, FeedbackPlaceline.class));
        } else if (item.getItemId() == R.id.start_tracking_toggle) {
            startHyperTrackTracking();
        }
        return true;
    }

    private void startHyperTrackTracking() {
        if (!HyperTrack.isTracking()) {
            HyperTrack.startTracking();
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
            Toast.makeText(this, "Tracking started successfully.", Toast.LENGTH_SHORT).show();

        } else {
            HyperTrack.stopTracking();
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.start_tracking);
            Toast.makeText(this, "Tracking stopped successfully.", Toast.LENGTH_SHORT).show();
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Method to set callback for HyperTrackEvents to update notification with relevant information.
     * Note: Show share tracking url message on Stop_Ended/Trip_Started event and reset it in other cases.
     */
    private void setHyperTrackCallbackForActivityUpdates() {
        HyperTrack.setCallback(new HyperTrackEventCallback() {
            @Override
            public void onEvent(@NonNull final HyperTrackEvent event) {
                switch (event.getEventType()) {
                    case HyperTrackEvent.EventType.STOP_ENDED_EVENT:

                        //Check if user has shared his tracking link
                        if (ActionManager.getSharedManager(Placeline.this).isActionLive()) {
                            return;
                        }

                        Placeline.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ServiceNotificationParamsBuilder builder = new ServiceNotificationParamsBuilder();
                                ArrayList<String> action = new ArrayList<>();
                                action.add("Set Destination Address");
                                ServiceNotificationParams notificationParams = builder
                                        .setSmallIcon(R.drawable.ic_ht_service_notification_small)
                                        .setSmallIconBGColor(ContextCompat.getColor(Placeline.this, R.color.colorAccent))
                                        .setContentTitle(getString(R.string.notification_share_tracking_link))
                                        .setContextText(getString(R.string.notification_set_destination))
                                        .setContentIntentActivityClass(SplashScreen.class)
                                        .setContentIntentExtras(action)
                                        .build();
                                HyperTrack.setServiceNotificationParams(notificationParams);
                            }
                        });
                        break;
                    case HyperTrackEvent.EventType.TRACKING_STOPPED_EVENT:
                    case HyperTrackEvent.EventType.ACTION_ASSIGNED_EVENT:
                    case HyperTrackEvent.EventType.ACTION_COMPLETED_EVENT:
                    case HyperTrackEvent.EventType.STOP_STARTED_EVENT:
                        HyperTrack.clearServiceNotificationParams();
                        break;
                    case HyperTrackEvent.EventType.ACTIVITY_CHANGED_EVENT:

                        showActivityChangedNotification(Placeline.this, getActivityChangedMessage((UserActivity) event.getData()));
                        break;
                }
            }

            @Override
            public void onError(@NonNull final ErrorResponse errorResponse) {
                // do nothing
            }
        });
    }

    private String getActivityChangedMessage(UserActivity activity) {
        return activity.getActivityString();
    }

    private void showActivityChangedNotification(Context context, String message) {
        try {
            HTLog.i(TAG, "ACTIVITY_CHANGED_EVENT received, activity = " + message);

            // Gets an instance of notification builder object
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            // Sets the parameters required for standard notification
            String title = "You are " + message;
            builder.setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_ht_service_notification_small)
                    .setColor(ContextCompat.getColor(Placeline.this, R.color.colorAccent))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setGroup("activities")
                    .setAutoCancel(true);

            // Send the Notification with the specified parameters
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(UUID.randomUUID().hashCode() /* ID of notification */, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.exception(TAG, e);
        }
    }
}