package io.hypertrack.sendeta.network.retrofit;

import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.sendeta.model.CreateTaskDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by piyush on 22/10/16.
 */
public interface HyperTrackService {

    @POST("tasks/")
    Call<HTTask> createTask(@Body CreateTaskDTO createTaskDTO);
}
