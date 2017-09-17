# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/Cellar/android-sdk/24.3.3/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes *Annotation*

-keep public class com.google.** {*;}
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keep class com.facebook.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit2.** { *; }
-keep class io.hypertrack.sendeta.model.** { *; }
-keep class io.hypertrack.sendeta.network.** { *; }
-keep class io.hypertrack.sendeta.store.** { *; }
-keep class maps.** { *; }
-keep class com.hypertrack.lib.** { *; }

-keep interface com.squareup.okhttp.** { *; }

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface

-dontwarn com.squareup.okhttp.**
-dontwarn retrofit2.**
-dontwarn okio.**
-dontwarn com.google.android.gms.**
-dontwarn com.hypertrack.lib.**

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keepattributes Signature
-keepattributes Exceptions

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes *Annotation*

## For Retrofit
-keepattributes Signature

## Other
-dontwarn com.google.common.**
-dontwarn io.branch.**