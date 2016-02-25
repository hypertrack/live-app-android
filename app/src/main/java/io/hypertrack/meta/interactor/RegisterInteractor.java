package io.hypertrack.meta.interactor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.Verify;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class RegisterInteractor {

    public void registerPhoneNumber(final OnRegisterListener onRegisterListener, String number) {

        String url = HTConstants.API_ENDPOINT + "/api/v1/users/";

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

                        if (!TextUtils.isEmpty(response.getFirstName())) {
                            spm.setFirstName(response.getFirstName());
                        }

                        if (!TextUtils.isEmpty(response.getLastName())) {
                            spm.setLastName(response.getLastName());
                        }

                        if (!TextUtils.isEmpty(response.getPhoto())) {
                            spm.setUserPhoto(response.getPhoto());
                        }

                        onRegisterListener.OnSuccess();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "Inside OnError");
                        onRegisterListener.OnError();
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(request);

    }
}
