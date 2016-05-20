package io.hypertrack.meta.interactor;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.model.Verification;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class VerificationInteractor {

    public void validateVerificationCode(String verificationCode, int userId, final OnVerificationCallback onVerificationCallback) {

        String url = HTConstants.API_ENDPOINT + "/api/v1/users/"+ userId + "/verify_phone_number/";

        Verification verification = new Verification(verificationCode);
        Gson gson = new Gson();
        String jsonObjectBody = gson.toJson(verification);

        HTCustomPostRequest<User> request = new HTCustomPostRequest<User>(
                1,
                url,
                jsonObjectBody,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {

                        Log.d("response", response.toString());

                        HTConstants.setPublishableApiKey(response.getToken());

                        SharedPreferenceManager spm = new SharedPreferenceManager(MetaApplication.getInstance());
                        spm.setUserAuthToken(response.getToken());

                        if (onVerificationCallback != null) {
                            onVerificationCallback.OnSuccess();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "Inside OnError");

                        if (onVerificationCallback != null) {
                            onVerificationCallback.OnError();
                        }
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(request);
    }
}
