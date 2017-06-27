package io.hypertrack.sendeta.store;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.io.IOException;

import io.hypertrack.sendeta.model.PlacelineData;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class PlacelineManager {

    private static final String TAG = PlacelineManager.class.getSimpleName();
    private static PlacelineManager placelineManager;
    private Context mContext;

    private PlacelineManager(Context mContext) {
        this.mContext = mContext;
    }

    public static PlacelineManager getPlacelineManager(Context context) {
        if (placelineManager == null) {
            placelineManager = new PlacelineManager(context);
        }

        return placelineManager;
    }

    public void getPlacelineData(String userId, String date, final HyperTrackCallback callback){
        HyperTrackService getPlacelineService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
        Call<PlacelineData> call = getPlacelineService.getUserPlaceline(userId, date);
        call.enqueue(new Callback<PlacelineData>() {
            @Override
            public void onResponse(Call<PlacelineData> call, Response<PlacelineData> response) {
                PlacelineData placelineData = response.body();
               //Log.d(TAG, "onResponse: "+response.body());
                if(placelineData !=null ){

                    if(callback != null) {
                        callback.onSuccess(new SuccessResponse(placelineData));
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
            public void onFailure(Call<PlacelineData> call, Throwable t) {
                if(callback != null){
                    callback.onError(new ErrorResponse());
                }
            }
        });
    }

}
