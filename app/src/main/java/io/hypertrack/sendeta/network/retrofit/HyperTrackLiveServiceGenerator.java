/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.network.retrofit;

import android.content.Context;

import com.hypertrack.lib.internal.common.util.Utils;

import java.io.IOException;
import java.util.TimeZone;

import io.hypertrack.sendeta.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by piyush on 22/10/16.
 */
public class HyperTrackLiveServiceGenerator {

    public static <S> S createService(final Class<S> serviceClass, final Context context) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Token " + BuildConfig.HYPERTRACK_PK)
                        .header("User-Agent", "hypertrack-live-android")
                        .header("timezone", TimeZone.getDefault().getID())
                        .header("Device-id", Utils.getDeviceId(context))
                        .header("app-id", context.getPackageName())
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.HYPERTRACK_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(serviceClass);
    }
}
