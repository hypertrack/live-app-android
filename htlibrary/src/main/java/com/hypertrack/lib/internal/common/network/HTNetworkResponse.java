package com.hypertrack.lib.internal.common.network;

import com.android.volley.Cache;
import com.android.volley.VolleyError;

/**
 * Created by piyush on 19/09/16.
 */
public class HTNetworkResponse<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        public void onErrorResponse(VolleyError error, Exception exception);
    }

    /** Returns a successful response containing the parsed result. */
    public static <T> HTNetworkResponse<T> success(T result, Cache.Entry cacheEntry) {
        return new HTNetworkResponse<T>(result, cacheEntry);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> HTNetworkResponse<T> error(VolleyError error) {
        return new HTNetworkResponse<T>(error);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** Cache metadata for this response, or null in the case of error. */
    public final Cache.Entry cacheEntry;

    /** Detailed error information if <code>errorCode != OK</code>. */
    public final VolleyError error;

    /** True if this response was a soft-expired one and a second one MAY be coming. */
    public boolean intermediate = false;

    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess() {
        return error == null;
    }

    private HTNetworkResponse(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }

    private HTNetworkResponse(VolleyError error) {
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
    }
}
