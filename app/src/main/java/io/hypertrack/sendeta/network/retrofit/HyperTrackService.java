package io.hypertrack.sendeta.network.retrofit;


import java.util.List;

import io.hypertrack.sendeta.model.CreateTaskDTO;
import io.hypertrack.sendeta.model.Task;
import io.hypertrack.sendeta.model.TaskETAResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by piyush on 22/10/16.
 */
public interface HyperTrackService {

    @POST("tasks/")
    Call<Task> createTask(@Body CreateTaskDTO createTaskDTO);

    @GET("/api/v1/eta/")
    Call<List<TaskETAResponse>> getTaskETA(@Query("origin") String origin, @Query("destination") String destination, @Query("vehicle_type") String vehicleType);
}
