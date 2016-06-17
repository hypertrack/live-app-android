package io.hypertrack.meta.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnRegisterCallback;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class RegisterInteractor {

    public void registerPhoneNumber(String number, final OnRegisterCallback onRegisterCallback) {

        String url = Constants.API_ENDPOINT + "/api/v1/users/";

        User user = new User(number);
        Gson gson = new Gson();
        String jsonObjectBody = gson.toJson(user);

        Log.d("Response", "Request Body - " + jsonObjectBody);

        HTCustomPostRequest<User> request = new HTCustomPostRequest<User>(
                1,
                url,
                jsonObjectBody,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {

                        Log.d("Response", "ID :" + response.getId()
                            + " First Name :" + response.getFirstName()
                            + " Last name :" + response.getLastName());

                        SharedPreferenceManager spm = new SharedPreferenceManager(MetaApplication.getInstance());

                        if (response.getId() != null) {
                            spm.setUserId(response.getId());
                        }


                        if (!TextUtils.isEmpty(response.getFirstName())) {
                            spm.setFirstName(response.getFirstName());
                        }

                        if (!TextUtils.isEmpty(response.getLastName())) {
                            spm.setLastName(response.getLastName());
                        }

                        if (!TextUtils.isEmpty(response.getPhoto())) {
                            spm.setUserPhoto(response.getPhoto());
                        }

                        if (onRegisterCallback != null) {
                            onRegisterCallback.OnSuccess();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "Inside OnError");

                        if (onRegisterCallback != null) {
                            onRegisterCallback.OnError();
                        }
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(request);

    }
}
