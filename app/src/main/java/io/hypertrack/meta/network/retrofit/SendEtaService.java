package io.hypertrack.meta.network.retrofit;

import java.util.Map;
import java.util.Objects;

import butterknife.Bind;
import io.hypertrack.meta.model.OnboardingUser;
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
public interface SendETAService {

    @PATCH("/api/v1/users/{id}/")
    Call<User> updateUserName(@Path("id") int id, @Body Map<String, String> user);

    @Multipart
    @POST("/api/v1/users/{id}/add_photo/")
    Call<Map<String, Object>> updateUserProfilePic(@Path("id") int id, @PartMap Map<String, RequestBody> params);

    @POST("/api/v1/users/")
    Call<OnboardingUser> getUser(@Body Map<String, String> phoneNumber);

    @POST("/api/v1/users/{id}/verify_phone_number/")
    Call<Map<String, Object>> verifyUser(@Path("id") int id, @Body Map<String, String> verificationCode);

    @POST("api/v1/users/{id}/resend_verification_code/")
    Call<Map<String, Object>> resendCode(@Path("id") int id);
}
