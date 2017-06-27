package io.hypertrack.sendeta.network.retrofit;

import java.util.List;

import io.hypertrack.sendeta.model.ETAResponse;
import io.hypertrack.sendeta.model.PlacelineData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by piyush on 22/10/16.
 */
public interface HyperTrackService {
    @GET("/api/v1/eta/")
    Call<List<ETAResponse>> getTaskETA(@Query("origin") String origin, @Query("destination") String destination, @Query("vehicle_type") String vehicleType);

    @GET("users/{id}/timeline/")
    Call<PlacelineData> getUserPlaceline(@Path("id") String id, @Query("date") String date);
}
