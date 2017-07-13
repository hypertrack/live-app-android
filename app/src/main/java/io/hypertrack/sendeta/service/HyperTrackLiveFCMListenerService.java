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
package io.hypertrack.sendeta.service;

/**
 * Created by piyush on 27/07/16.
 */

/**
 * - Uncomment `HyperTrackLiveFCMListenerService` service tag in the <a href="https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L161">AndroidManifest.xml</a> file.
 * - Uncomment <a href="https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/service/HyperTrackLiveFCMListenerService.java#L36">HyperTrackLiveFCMListenerService.java</a> file.
 * - Once you have either an existing account on <a href="https://console.developers.google.com/">Google Developer</a> or a new account <a href="https://console.firebase.google.com">Firebase console</a>, you will need to add <a href="https://support.google.com/firebase/answer/7015592">google-services.json</a> file for your app to remove the below compilation failure.
 * - Uncomment `apply google-services plugin` in <a href="https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L75">build.gradle</a> file.
 */
//public class HyperTrackLiveFCMListenerService extends HyperTrackFirebaseMessagingService {

// No need to implement/override any method for HyperTrack SDK's Gcm/Fcm integration
// In case onMessageReceived method is overridden, super.onMessageReceived() method should be called.
//}
