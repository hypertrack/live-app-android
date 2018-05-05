
# Build live location sharing within your app
Following this tutorial to build live location share within your app. 

<p align="center">
<kbd>
<img src="http://res.cloudinary.com/hypertrack/image/upload/v1524554407/HyperTrack_Live_Android.gif" alt="Live Location Sharing" width="300">
</kbd>
</p>

The tutorial is divided into three sections.
1. In the first section, we will do a basic setup of Hypertrack SDK. 
2. In the second section, we will select a destination and start a Live Location trip to that place. 
3. In the last section, we will get your friend to join the trip started by you. 

Let's get started 😊. Strap yourself in and get ready for an exciting ride 🚀.

- [Basic setup](#basic-setup)
  - [Get API keys](#step-1-get-api-keys)
  - [Install SDK](#Step-2-Install-SDK)
  - [Enable communication](#Step-3-Enable-communication)
  - [Set permissions](#Step-4-Set-permissions)
  - [Create HyperTrack user](#Step-5-Create-HyperTrack-user)
  - [Crashlytics Setup (optional)](#step-6-crashlytics-setup-optional)
- [Start a trip](#start-a-trip)
  - [Add destination](#step-1-add-destination)
  - [Create and track action](#step-2-create-and-track-action)
  - [Share your trip](#step-3-share-your-trip)
- [Track or join an ongoing trip](#track-or-join-an-ongoing-trip)
  - [Track ongoing trip](#step-1-track-ongoing-trip)
  - [Join ongoing trip](#step-2-join-ongoing-trip)

  
## Basic Setup
### Step 1. Get API keys
Get your HyperTrack API keys [here](https://www.hypertrack.com/signup?utm_source=github&utm_campaign=ht_live_android).

### Step 2. Install SDK
1. Import our SDK into your app
```java
repositories {
    maven { url 'http://hypertrack-android-sdk.s3-website-us-west-2.amazonaws.com/' }
}

dependencies {
    implementation('com.hypertrack:android:0.7.7@aar') {
        transitive = true;
    }
}
```

2. Initialize the SDK with your [publishable keys](https://dashboard.hypertrack.com/settings)
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HyperTrack.initialize(this, YOUR_PUBLISHABLE_KEY_HERE);
    }
}
```

### Step 3. Enable communication
1. Enable bidirectional communication between server and SDK using FCM notifications
```java
public class MyFirebaseMessagingService extends HyperTrackFirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            String sdkNotification = remoteMessage.getData().get(Constants.HT_SDK_NOTIFICATION_KEY);
            if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {
                /**
                 * HyperTrack notifications are received here
                 * Dont handle these notifications. This might end up in a crash
                 */
                return;
            }
        }
        // Handle your notifications here.
    }
}
```

```java
 <service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
2. Head over to [FCM Console](https://console.firebase.google.com/project/), select your project, and then visit Settings > Cloud Messaging to get your FCM server key. Copy the key and it to your [HyperTrack dashboard settings](https://dashboard.hypertrack.com/settings).

### Step 4. Set permissions
Ask user permission to access location
```java
private void checkForLocationSettings() {
     // Check for Location permission
    if (!HyperTrack.checkLocationPermission(this)) {
        HyperTrack.requestPermissions(this);
        return;
    }
    // Check for Location settings
    if (!HyperTrack.checkLocationServices(this)) {
        HyperTrack.requestLocationServices(this);
    }
    // Location Permissions and Settings have been enabled
    // Proceed with your app logic here i.e User Login in this case
    attemptUserLogin();
}

@Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
 
 @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, R.string.enable_location_settings,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

```

### Step 5. Create HyperTrack user
Create a HyperTrack user to identify the mobile device. You can do so when your user logs-in into your app.

```java
UserParams userParams = new UserParams()
    .setName(name)
    .setPhone(phoneNumber)
    .setPhoto(encodedImage)
    .setLookupId(phoneNumber);
                
HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse successResponse) {
        // Handle success on getOrCreate user
        Toast.makeText(this, "Yay! User is created successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle error on getOrCreate user
        Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

### Step 6. Crashlytics Setup (Optional)
You can **optionally** enable the crashlytics crash reporting tool. 
1. Get your Crashlytics key from the **Add Your API Key** section [here](https://fabric.io/kits/android/crashlytics/install).
2. Paste the key to [fabric.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/fabric.properties). Create a new fabric.properties file, if it doesn't exist already.

## Start a trip
You are now all set with the basic setup. Are you ready to rock and roll?

### Step 1. Add destination
The first thing that you need to do is to add a destination. For this, we will need a location picker. HyperTrack SDK has a location picker within it. Once the user selects a location with the help of our inbuilt location picker, the SDK gives a callback to the app with the selected location so that the app can start a trip.

For  starter project, check ```Home.java``` embedding the HyperTrackMapFragment view in  ```content_home.xml``` view. Initialize the HyperTrackMapFragment inside ```oncreate``` method of ```Home``` activity and set your implementation of [HyperTrackMapAdapter](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/view/HomeMapAdapter.java), [MapFragmentCallback](https://github.com/hypertrack/hypertrack-live-android/blob/6c801e65a628769cd160ef7b0b4f77fd68df7818/app/src/main/java/io/hypertrack/sendeta/view/Home.java#L137-L213) for HyperTrackMapFragment.

```java
MapFragmentCallback callback = new MapFragmentCallback(){
    @Override
    public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
        // Handle onMapReadyCallback API here
    }

    @Override
    public void onExpectedPlaceSelected(Place expectedPlace) {
        // Check if expected place was selected
        if (expectedPlace != null) {
            // Handle selected expectedPlace here
        }
    }
    ...
};
```

```java
htMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.htMapfragment);
HomeMapAdapter adapter = new HomeMapAdapter(this);
htMapFragment.setHTMapAdapter(adapter);
htMapFragment.setMapFragmentCallback(callback);
```

Implement this method in the MapFragmentCallback as specified above. You will get a callback when a user selects an expected place using SDK's Place Selector View.
```java
@Override
public void onExpectedPlaceSelected(Place expectedPlace) {
    // Check if expected place was selected
    if (expectedPlace != null) {
        // Handle selected expectedPlace here
    }
}
```

### Step 2. Create and track action
When the user selects a destination, you will get a callback in the ```onExpectedPlaceSelected``` function of your ```MapFragmentCallback``` instance. This is the right time to start a trip. For starting a trip, you need to create a session. This can be achieved by creating a 'visit' [action](https://docs.hypertrack.com/api/entities/action.html).  

You will need two things to create an action. 
1. ```expectedPlace``` - This is the destination for the visit. You have it after you select the destination.
2. ```collectionId``` - A ```collectionId``` is an identifier created by you for the Live Location trip. A ```collectionId``` is what needs to be shared with the friend, so they can join your trip and share their location. We chose it to be the UUID. You can use your own internal identifiers. 

For starter project, go to [Home.java](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/view/Home.java#L194-L199) and add the following code when you get a callback of place selection ```onExpectedPlaceSelected```.
```java
ActionParams actionParams = new ActionParamsBuilder()
                .setCollectionId(collectionId != null ? collectionId : UUID.randomUUID().toString())
                .setType(Action.ACTION_TYPE_VISIT)
                .setExpectedPlace(expectedPlace)
                .build();

// Call createAction to create an action
HyperTrack.createAction(actionParams, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse response) {
        if (response.getResponseObject() != null) {
            Action action = (Action) response.getResponseObject();
            
            // Handle createAction API success here
            Toast.makeText(this, "Live location shared", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle createAction API error here
        Toast.makeText(this, "Live location not shared", Toast.LENGTH_SHORT).show();
    }
});
```

Also, allow user to stop sharing his location by handling `stopSharing` method in [HomePresenter.java](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/presenter/HomePresenter.java) file so the action gets completed when the user taps stop sharing.

```java
HyperTrack.completeAction(actionID);
```

### Step 3. Share your trip
As described earlier, a ```collectionId``` is an identifier which identifies a Live Location trip. When you want to share your trip, your trip's ```collectionId``` needs to be shared.

You can share your ```collectionId``` to the other person in different ways. 

1. You can use the Android's ShareCard Intent to share it through messaging apps.
2. You can use your backend to send the ```collectionId```. 

For starter project, let us keep it simple and use Android's ShareCard to do the job for us.
```java
Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
sharingIntent.setType("text/plain");
sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
startActivityForResult(Intent.createChooser(sharingIntent, "Share via"),
        Constants.SHARE_REQUEST_CODE);
```

## Track or join an ongoing trip
You now have a user who has started a Live Location session. Once their friend receives a ```collectionId``` (either through your own backend or through a messaging app), she can use it to track the user and optionally join the trip by adding the few lines of code as described in the following steps.

### Step 1. Track ongoing trip
To track the user, use the following function. Although the tracking has started in the SDK, visualizing it requires you to embed HyperTrack's map fragment in your activity containing hypertrack map view. 
```java
HyperTrack.trackActionByCollectionId(collectionId, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse response) {
        // Handle trackActionByCollectionId API success here
    }
 
    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle trackActionByCollectionId API error here
    });
```
 
For starter project, you have to enter the ```collectionId``` in the text field that you have received from the user who has started the session. Add the following code in the click of track button ```onTrackSharedLinkButtonClick()```.
 
```java
ActionParams actionParams = new ActionParamsBuilder()
                .setCollectionId(collectionId != null ? collectionId : UUID.randomUUID().toString())
                .setType(Action.ACTION_TYPE_VISIT)
                .setExpectedPlace(expectedPlace)
                .build();

// Call createAction to create an action
HyperTrack.createAction(actionParams, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse response) {
        if (response.getResponseObject() != null) {
            Action action = (Action) response.getResponseObject();
            
            // Handle createAction API success here
            Toast.makeText(this, "Live location shared", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle createAction API error here
        Toast.makeText(this, "Live location not shared", Toast.LENGTH_SHORT).show();
    }
});
```
 
Now to see the result, go to the other device and set up the user. After that click on 'Track a Live Location trip' and paste/enter the ```collectionId``` which you received from the user. 
 
### Step 2. Join ongoing trip
In this step, we will see how the friend can share her Live Location and join the trip. To join the trip, an action with the same collectionId needs to be created. This step is similar to Step 6. But this time it is a collectionId of an existing trip and NOT a new one in Step 6.

For starter project, add this code to `onShareLiveLocationBack()` when the user taps the 'Share Live Location' button. It creates an action for your friends' Live Location session.
```java
ActionParams actionParams = new ActionParamsBuilder()
                .setCollectionId(collectionId != null ? collectionId : UUID.randomUUID().toString())
                .setType(Action.ACTION_TYPE_VISIT)
                .setExpectedPlace(expectedPlace)
                .build();

// Call assignAction to start the tracking action
HyperTrack.createAction(actionParams, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse response) {
        if (response.getResponseObject() != null) {
            Action action = (Action) response.getResponseObject();
            
            // Handle createAction API success here
            Toast.makeText(this, "Live location shared", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle createAction API error here
        Toast.makeText(this, "Live location not shared", Toast.LENGTH_SHORT).show();
    }
});
```

We hope you’ve enjoyed yourself on your epic quest to build a Live Location Sharing feature. If you have any problems or suggestions for the tutorial, do not hesitate to buzz 🐝 us on [Slack](http://slack.hypertrack.com) or email us at help@hypertrack.com.
