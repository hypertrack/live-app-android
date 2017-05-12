# HyperTrack Live
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger)
[![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com)

Share your live location with family and friends and let them share their location back with you.

![Live Tracking Demo](https://raw.githubusercontent.com/hypertrack/hypertrack-live-android/master/live_location_sharing.gif)

### Live location
Let your friends and family track you on a map when you are on the way. No more phone calls asking where you are.

### Accurate ETA
Send your real-time ETA to your friends and family based on mode of transport and trip progress.

### Easy to share
Use your favorite messaging app to send ETA with a web link for live tracking.

### Private and secure
Your location and trips are completely private and secure. They are shared only with people you chose and only for the duration of the trip.

<a href='https://play.google.com/store/apps/details?id=io.hypertrack.sendeta&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

## Usage
1. Clone the project.
2. Get your HyperTrack API keys [here](https://dashboard.hypertrack.com/signup), and add the publishable key to [gradle.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/gradle.properties) file. To get to know more about API keys, refer to the [Authentication](https://docs.hypertrack.com/gettingstarted/authentication.html) section in our documentation.
3. Get [Google Map API Key](https://developers.google.com/maps/documentation/android-api/signup) and add them to the AndroidManifest.xml.
4. HyperTrack SDKs require FCM/GCM integration for enhanced realtime tracking experiences. Refer to our [FCM Integration guide](https://docs.hypertrack.com/sdks/android/guides/gcm-integration.html) for detailed info. Once you have either an existing account on [Google Developer](https://console.developers.google.com/) or a new account [Firebase console](https://console.firebase.google.com), you will need to add [google-services.json](https://support.google.com/firebase/answer/7015592) file for your app to remove the below compilation failure. 
    
    > Error:Execution failed for task ':app:processDebugGoogleServices'.
    > File google-services.json is missing. The Google Services Plugin cannot function without it.
    > Searched Location:
    > /Users/piyush/StudioProjects/hypertrack-live-android/app/src/debug/google-services.json
    > /Users/piyush/StudioProjects/hypertrack-live-android/app/google-services.json
    
**Check out the [Live Location Sharing tutorial](https://www.hypertrack.com/tutorials/live-location-sharing-android-messaging-app) and the [Service Visit Tracking tutorial](https://www.hypertrack.com/tutorials/service-visit-tracking-android) for a few suggestions of how to use this source code in your business.**
  
## Documentation
For detailed documentation of the methods and customizations, please visit the official [HyperTrack Docs](https://docs.hypertrack.com/).

## Contribute
Feel free to clone, use, and contribute back via [pull request](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [Issues Tracker](https://github.com/hypertrack/example-android/issues) to raise bug reports and feature requests.

## Support
Join our [Slack community](http://slack.hypertrack.com) for instant responses, or interact with our growing [Community](https://community.hypertrack.com). You can also email us at help@hypertrack.com.

## Dependencies
* [Google v7 appcompat library](https://developer.android.com/topic/libraries/support-library/packages.html#v7-appcompat)
* [Google Design Support Library](https://developer.android.com/topic/libraries/support-library/packages.html#design)
* [Google libphonenumber library](https://github.com/googlei18n/libphonenumber/)
* [Square Retrofit](https://github.com/square/retrofit)
* [Square Picasso](https://github.com/square/picasso)
* [tajchert WaitingDots](https://github.com/tajchert/WaitingDots)