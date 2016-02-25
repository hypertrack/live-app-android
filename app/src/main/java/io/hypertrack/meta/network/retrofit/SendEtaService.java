package io.hypertrack.meta.network.retrofit;

import java.util.Map;

import io.hypertrack.meta.model.User;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

/**
 * Created by suhas on 25/01/16.
 */
public interface SendEtaService {

    @PATCH("/api/v1/users/{id}/")
    Call<User> updateUserName(@Path("id") String id,@Body User user);

    @Multipart
    @POST("/api/v1/users/{id}/add_photo/")
    Call<User> updateUserProfilePic(@Path("id") String id, @PartMap Map<String, RequestBody> params);
    //Call<User> updateUserProfilePic(@Path("id") String id, @Part("photo\"; filename=\"profilePic.jpg\" ")RequestBody file);
}
