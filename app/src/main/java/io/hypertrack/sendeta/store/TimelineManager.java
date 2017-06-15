package io.hypertrack.sendeta.store;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.io.IOException;

import io.hypertrack.sendeta.model.UserTimelineData;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class TimelineManager {

    private static final String TAG = TimelineManager.class.getSimpleName();
    private static TimelineManager timelineManager;
    private Context mContext;

    private TimelineManager(Context mContext) {
        this.mContext = mContext;
    }

    public static TimelineManager getTimelineManager(Context context) {
        if (timelineManager == null) {
            timelineManager = new TimelineManager(context);
        }

        return timelineManager;
    }

    public void getTimelineData(String userId, String date, final HyperTrackCallback callback){
        HyperTrackService getTimelineService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
        Call<UserTimelineData> call = getTimelineService.getUsetTimeline(userId, date);
        call.enqueue(new Callback<UserTimelineData>() {
            @Override
            public void onResponse(Call<UserTimelineData> call, Response<UserTimelineData> response) {
                UserTimelineData userTimelineData = response.body();
                Log.d(TAG, "onResponse: "+response.body());
                if(userTimelineData !=null ){

                    if(callback != null) {
                        callback.onSuccess(new SuccessResponse(userTimelineData));
                    }

                }else {
                    if(callback != null){
                        try {
                            callback.onError(new ErrorResponse(response.code(),response.errorBody().string()));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "Error occured. Pls try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserTimelineData> call, Throwable t) {
                if(callback != null){
                    callback.onError(new ErrorResponse());
                }
            }
        });
    }

}
