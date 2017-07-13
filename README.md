<<<<<<< HEAD
# Live location sharing with HyperTrack
[![Build Status](https://travis-ci.org/hypertrack/hypertrack-live-android.svg?branch=master)](https://travis-ci.org/hypertrack/hypertrack-live-android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger) [![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-HyperTrack%20Live-brightgreen.svg?style=flat)](https://android-arsenal.com/details/3/5754) [![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/licenses/MIT) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

If you can track your Uber turn-by-turn with an accurate ETA, why not track friends, colleagues, buyers, and sellers similarly! [Facebook Messenger](https://newsroom.fb.com/news/2017/03/introducing-live-location-in-messenger/) and [Google Maps](https://blog.google/products/maps/share-your-trips-and-real-time-location-google-maps/) recently added live location sharing to their apps. Now it’s your turn.
 
This open source repo uses [HyperTrack](https://www.hypertrack.com) to enable location sharing and real-time tracking of your friends and family. Scrape parts of this app to add live location sharing into your own app, or fork the repo and modify it to your own need to build your own brand new app.

![Live Tracking Demo](https://raw.githubusercontent.com/hypertrack/hypertrack-live-android/master/live_location_sharing.gif)

## Example App
If you want to experience the app or share live location amongst your friends & family, get HyperTrack Live from the Play Store.

<a href='https://play.google.com/store/apps/details?id=io.hypertrack.sendeta&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="150"/></a>

## Usage
### To build live location sharing within your own app

Follow [this step-by-step tutorial](https://www.hypertrack.com/tutorials/live-location-sharing-android-messaging-app) that will walk you through how you can embed this code in your app.

### To use this as your own app

1. Clone the project.
    
2. Get your HyperTrack API keys [here](https://dashboard.hypertrack.com/signup), and add the publishable key to [key.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/key.properties) file.
    
3. Get the [Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup) and add it to [api-keys.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/res/values/api-keys.xml).
    
4. To release the app on the Play Store, you will have to change the app's package name.
   - Change the package name in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L4) file.
   - Refactor the name of your package with right click → Refactor → Rename in the tree view, then Android Studio will display a window, select "Rename package" option.
   - Change the application id in the [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L60) file. Once done, clean and rebuild the project.
        
5. The HyperTrack SDK requires FCM/GCM for a battery efficient real-time tracking experience. Refer to the [FCM Integration guide](https://docs.hypertrack.com/sdks/android/guides/gcm-integration.html).
   - Uncomment `HyperTrackLiveFCMListenerService` service tag in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L198) file.
   - Uncomment the [HyperTrackLiveFCMListenerService](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/service/HyperTrackLiveFCMListenerService.java#L32) file.
   - After setting up your account on the [Firebase console](https://console.firebase.google.com), you will need to add the [google-services.json](https://support.google.com/firebase/answer/7015592) file to your app.
   - Uncomment `apply google-services plugin` in [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L124) file.
  
## Documentation
For detailed documentation of the APIs, customizations and what all you can build using HyperTrack, please visit the official [docs](https://docs.hypertrack.com/).

## Contribute
Feel free to clone, use, and contribute back via [pull requests](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [issues tracker](https://github.com/hypertrack/hypertrack-live-android/issues) to raise bug reports and feature requests.

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
* [Compact Calendar View](https://github.com/SundeepK/CompactCalendarView)
* [Android Ripple Background](https://github.com/skyfishjy/android-ripple-background)
* [Scrolling Image View](https://github.com/Q42/AndroidScrollingImageView)
* [RecylcerView Snap](https://github.com/rubensousa/RecyclerViewSnap)
* [Leak Canary](https://github.com/square/leakcanary)
* [Branch](https://branch.io/)