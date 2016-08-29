package io.hypertrack.sendeta.network.retrofit;

import java.util.List;
import java.util.Map;

import io.hypertrack.sendeta.model.GCMAddDeviceDTO;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.MembershipDTO;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.PlaceDTO;
import io.hypertrack.sendeta.model.TaskDTO;
import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.model.Trip;
import io.hypertrack.sendeta.model.TripETAResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.VerifyResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by suhas on 25/01/16.
 */
public interface SendETAService {

    @POST("/api/v1/users/{id}/edit/")
    Call<User> updateUserName(@Path("id") int id, @Body Map<String, String> user);

    @Multipart
    @POST("/api/v1/users/{id}/add_photo/")
    Call<Map<String, Object>> updateUserProfilePic(@Path("id") int id, @PartMap Map<String, RequestBody> params);

    @POST("/api/v1/login/")
    Call<Map<String, Object>> getUser(@Body Map<String, String> phoneNumber);

    @POST("/api/v1/verify_code/")
    Call<VerifyResponse> verifyUser(@Body Map<String, String> verificationParams);

    @POST("/api/v1/resend_code/")
    Call<Map<String, Object>> resendCode(@Body Map<String, String> phoneNumber);

    @GET("/api/v1/eta/")
    Call<List<TripETAResponse>> getETA(@Query("origin") String origin, @Query("destination") String destination);

    @GET("/api/v1/eta/")
    Call<List<TaskETAResponse>> getTaskETA(@Query("origin") String origin, @Query("destination") String destination, @Query("vehicle_type") String vehicleType);

    @POST("/api/v1/trips/")
    Call<Trip> addTrip(@Body Map<String, String> tripDetails);

    @POST("/api/v1/users/{id}/start_task/")
    Call<Map<String, Object>> startTask(@Path("id") int id, @Body TaskDTO taskDTO);

    @POST("/api/v1/places/")
    Call<MetaPlace> addPlace(@Body PlaceDTO place);

    @POST("/api/v1/places/{id}/edit/")
    Call<MetaPlace> editPlace(@Path("id") int id, @Body PlaceDTO place);

    @DELETE("/api/v1/places/{id}/")
    Call<MetaPlace> deletePlace(@Path("id") int id);

    @GET("/api/v1/places/")
    Call<List<MetaPlace>> getPlaces();

    @GET("/api/v1/users/{id}/task/")
    Call<Map <String, Object>> createTask(@Path("id") int id, @Query("destination_id") String destinationID);

    // Membership Calls
    @GET("/api/v1/users/{id}/")
    Call<User> getUserData(@Path("id") int id);

    @GET("/api/v1/users/{id}/membership/")
    Call<Membership> getMembershipForAccountId(@Path("id") int id, @Query("account_id") int accountId);

    @POST("/api/v1/users/{id}/accept_membership/")
    Call<Membership> acceptMembership(@Path("id") int id, @Body MembershipDTO membership);

    @POST("/api/v1/users/{id}/reject_membership/")
    Call<Membership> rejectMembership(@Path("id") int id, @Body MembershipDTO membership);

    @POST("/api/v1/users/{id}/delete_membership/")
    Call<ResponseBody> deleteMembership(@Path("id") int id, @Body MembershipDTO membership);

    //Add GCM Token Call
    @POST("/api/v1/users/{id}/add_device/")
    Call<ResponseBody> addGCMToken(@Path("id") int id, @Body GCMAddDeviceDTO gcmAddDeviceDTO);
}
