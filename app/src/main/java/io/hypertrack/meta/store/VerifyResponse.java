package io.hypertrack.meta.store;

import io.hypertrack.meta.model.OnboardingUser;

/**
 * Created by ulhas on 25/06/16.
 */
public class VerifyResponse {
    private String token;
    private OnboardingUser user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public OnboardingUser getUser() {
        return user;
    }

    public void setUser(OnboardingUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "VerifyResponse{" +
                "token='" + token + '\'' +
                ", user=" + user +
                '}';
    }
}
