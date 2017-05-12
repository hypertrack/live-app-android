# Live location sharing with HyperTrack
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger) [![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com)

If your users can track their Uber coming to them turn-by-turn with an accurate ETA, why not track friends, colleagues, buyers, and sellers similarly! [Facebook Messenger](https://newsroom.fb.com/news/2017/03/introducing-live-location-in-messenger/) and [Google Maps](https://blog.google/products/maps/share-your-trips-and-real-time-location-google-maps/) recently added functionality for live location sharing and Whatsapp is likely to follow soon. Now it’s your turn.
 
This open source repo uses [HyperTrack](https://www.hypertrack.com) to enable location sharing and real-time tracking of your friends and family. Scrape parts of this app to add live location sharing into your own app, or fork the repo and modify it to your own need to build your own brand new app.

![Live Tracking Demo](https://raw.githubusercontent.com/hypertrack/hypertrack-live-android/master/live_location_sharing.gif)

## Example App
If you want to experience the app or want to share live location amongst your friends & family, get it from Play Store.

<a href='https://play.google.com/store/apps/details?id=io.hypertrack.sendeta&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="150"/></a>

## Usage
### To use this as your own app

1. Clone the project.
    
2. Get your HyperTrack API keys [here](https://dashboard.hypertrack.com/signup), and add the publishable key to [gradle.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/gradle.properties) file. Refer to the [Authentication](https://docs.hypertrack.com/gettingstarted/authentication.html) docs to learn more about keys.
    
3. Get [Google Map API Key](https://developers.google.com/maps/documentation/android-api/signup) and add them to the AndroidManifest.xml.
    
4. For the app to be released on playstore, you will have to change the app's package name.
   - Change the package name in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L4) file.
   - Refactor the name of your package with right click -> refactor -> rename in the tree view, then Android studio will display a window, select "rename package" option.
   - Change manually the application Id in the [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L26) file. Once done, clean and rebuild the project.
        
5. HyperTrack SDKs require FCM integration for enhanced real-time tracking experiences. Refer to our [FCM Integration guide](https://docs.hypertrack.com/sdks/android/guides/gcm-integration.html) for detailed info.
   - Uncomment `HyperTrackLiveFCMListenerService` service tag in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L160) file.
   - Uncomment [HyperTrackLiveFCMListenerService](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/service/HyperTrackLiveFCMListenerService.java#L35) file.
   - Once you have either an existing account on [Google Developer](https://console.developers.google.com/) or a new account [Firebase console](https://console.firebase.google.com), you will need to add [google-services.json](https://support.google.com/firebase/answer/7015592) file for your app to remove the below compilation failure.
   - Uncomment `apply google-services plugin` in [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L75) file.
   
### To build live location sharing within your own app

Follow [this step-by-step tutorial](https://www.hypertrack.com/tutorials/live-location-sharing-android-messaging-app) that walks through how you can embed this code in your app.
  
## Documentation
For detailed documentation of the APIs, customizations and what all you can build using HyperTrack, please visit the official [HyperTrack Docs](https://docs.hypertrack.com/).

## Contribute
Feel free to clone, use, and contribute back via [pull request](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [Issues Tracker](https://github.com/hypertrack/hypertrack-live-android/issues) to raise bug reports and feature requests.

We are excited to see what live location feature you build in your app using this project. Do ping us at help@hypertrack.io once you build one, and we would love to feature your app on our blog!

## Support
Join our [Slack community](http://slack.hypertrack.com) for instant responses, or interact with our growing [community](https://community.hypertrack.com). You can also email us at help@hypertrack.com.

## Dependencies
* [Google v7 appcompat library](https://developer.android.com/topic/libraries/support-library/packages.html#v7-appcompat)
* [Google Design Support Library](https://developer.android.com/topic/libraries/support-library/packages.html#design)
* [Google libphonenumber library](https://github.com/googlei18n/libphonenumber/)
* [Square Retrofit](https://github.com/square/retrofit)
* [Square Picasso](https://github.com/square/picasso)
* [tajchert WaitingDots](https://github.com/tajchert/WaitingDots)