package com.hypertrack.live.models;


import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.hypertrack.backend.models.GeofenceLocation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlaceModel implements Parcelable {
    private static final CharacterStyle STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);

    public String placeId = "";
    public String primaryText = "";
    public String secondaryText = "";

    public LatLng latLng;
    public String address = "";

    public boolean isRecent = false;

    public static PlaceModel from(AutocompletePrediction autocompletePrediction) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.placeId = autocompletePrediction.getPlaceId();
        placeModel.primaryText = autocompletePrediction.getPrimaryText(STYLE_NORMAL).toString();
        placeModel.secondaryText = autocompletePrediction.getSecondaryText(STYLE_NORMAL).toString();
        return placeModel;
    }

    public static List<PlaceModel> from(Collection<AutocompletePrediction> collection) {
        List<PlaceModel> placeModelList = new ArrayList<>();
        for (AutocompletePrediction item : collection) {
            placeModelList.add(from(item));
        }
        return placeModelList;
    }


    @NotNull
    public static String getAddressFromGeocoder(LatLng latLng, Context context) {
        if (!Geocoder.isPresent()) return "";
        List<Address> results;
        try {
            results = new Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException ignored) {
            return "";
        }
        if (results == null || results.isEmpty()) return "";

        String thoroughfare = results.get(0).getThoroughfare();
        return thoroughfare != null ? thoroughfare : "";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        return obj instanceof PlaceModel && placeId.equals(((PlaceModel) obj).placeId);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + placeId.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placeId);
        dest.writeString(this.primaryText);
        dest.writeString(this.secondaryText);
        dest.writeParcelable(this.latLng, flags);
        dest.writeString(this.address);
        dest.writeByte(this.isRecent ? (byte) 1 : (byte) 0);
    }

    public void populateAddressFromGeocoder(Context context) {
        address = getAddressFromGeocoder(latLng, context);
    }

    public PlaceModel() {
    }

    protected PlaceModel(Parcel in) {
        this.placeId = in.readString();
        this.primaryText = in.readString();
        this.secondaryText = in.readString();
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
        this.address = in.readString();
        this.isRecent = in.readByte() != 0;
    }

    public static final Parcelable.Creator<PlaceModel> CREATOR = new Parcelable.Creator<PlaceModel>() {
        @Override
        public PlaceModel createFromParcel(Parcel source) {
            return new PlaceModel(source);
        }

        @Override
        public PlaceModel[] newArray(int size) {
            return new PlaceModel[size];
        }
    };
}
