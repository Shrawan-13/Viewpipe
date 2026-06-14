# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Retrofit and OkHttp
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn java.beans.**
-dontwarn org.mozilla.javascript.**

# Keep NewPipe Extractor classes so they are not broken by minification
-keep class org.schabi.newpipe.** { *; }

# Keep app models and user package
-keep class com.example.** { *; }

