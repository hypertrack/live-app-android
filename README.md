# Live Location Sharing
[![Build Status](https://travis-ci.org/hypertrack/hypertrack-live-android.svg?branch=master)](https://travis-ci.org/hypertrack/hypertrack-live-android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger) [![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-HyperTrack%20Live-brightgreen.svg?style=flat)](https://android-arsenal.com/details/3/5754) [![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/licenses/MIT) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Billions of trips happen on the planet every day. These trips lead to people meeting each other at home, work or some place else. Friends, family and colleagues use their phones to check where the other has reached, often coordinating when and where to meet. Whether you are a messaging app or a marketplace with messaging capability, your users are likely messaging each other about this. It’s time to solve their problem better. 

<p align="center">
<kbd>
<img src="http://res.cloudinary.com/hypertrack/image/upload/v1524554407/HyperTrack_Live_Android.gif" alt="Live Location Sharing" width="300">
</kbd>
</p>

If your users can track their Uber coming to them turn-by-turn with an accurate ETA, why not track friends, colleagues, buyers and sellers similarly! Facebook Messenger and Google Maps recently added functionality for live location sharing and Whatsapp is likely to follow soon. Now it’s your turn. 

Use this **open source repo** of the [Hypertrack Live](https://play.google.com/store/apps/details?id=io.hypertrack.sendeta&hl=en) app to build live location sharing experience within your app within a few hours. HyperTrack Live app helps you share your Live Location with friends and family through your favorite messaging app when you are on the way to meet up. HyperTrack Live uses [HyperTrack](https://www.hypertrack.com/) APIs and SDKs. 

In case you are using iOS, refer to our open source iOS [repository](https://github.com/hypertrack/hypertrack-live-ios).

- [Clone the repo](#clone-the-repo)
- [Build live location sharing within your app](#build-within-your-app)
- [Release to playstore](#release-to-playstore)
- [Dependencies](#dependencies)
- [Documentation](#documentation)
- [Contribute](#contribute)
- [Support](#support)

## Clone the repo

1. Clone this repository
```bash
$ git clone https://github.com/hypertrack/hypertrack-live-android.git
```

2. [Signup](https://www.hypertrack.com/signup?utm_source=github&utm_campaign=ht_live_android) to get your [HyperTrack API keys](https://dashboard.hypertrack.com/settings). Add the **publishable key** to [release key.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/release/java/io/hypertrack/sendeta/key.properties) and [debug key.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/debug/java/io/hypertrack/sendeta/key.properties) file.
```java
HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
```

3. Get the [Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup) and add it to [api-keys.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/res/values/api-keys.xml).

## Build within your app
[Follow this step-by-step tutorial](https://github.com/hypertrack/hypertrack-live-android/blob/master/TUTORIAL.md) to build live location sharing within your own app.

## Release to Playstore
Following these steps to release the app on the Play Store.

1. Change the package name in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L4) file.

2. Refactor the name of your package. Right click → Refactor → Rename in the tree view. Android Studio will display a window. Select "Rename package" option.

3. Change the application id in [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L102) file. Once done, clean and rebuild the project.
   - Add `release key store file` in app level folder.
   - Create a `keystore.properties` file in root or project level folder with key-values pair.
    ```properties
        storeFile=<File path of keystore file>
        storePassword=<Key Store Password>
        keyAlias=<Key Alias>
        keyPassword=<Key Password>
   ```

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
* [Crashlytics](https://fabric.io/kits/android/crashlytics)

## Documentation
For detailed documentation of the HyperTrack APIs and SDKs, customizations and what all you can build using HyperTrack, please visit the official [docs](https://www.hypertrack.com/docs).

## Contribute
Feel free to clone, use, and contribute back via [pull requests](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [issues tracker](https://github.com/hypertrack/hypertrack-live-android/issues) to raise bug reports and feature requests.

We are excited to see what live location feature you build in your app using this project. Do ping us at help@hypertrack.io once you build one, and we would love to feature your app on our blog!

## Support
Join our [Slack community](http://slack.hypertrack.com) for instant responses. You can also email us at help@hypertrack.com.
