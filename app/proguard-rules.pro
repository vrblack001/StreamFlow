# ═══════════════════════════════════════════════
# StreamFlow — ProGuard Rules
# ═══════════════════════════════════════════════

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Retrofit interfaces and Gson models
-keep interface dev.streamflow.android.data.api.** { *; }
-keep class dev.streamflow.android.data.model.** { *; }
-keepclassmembers class dev.streamflow.android.data.model.** { *; }

# Gson: keep classes used in serialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room DAOs
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao class * { *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# NewPipeExtractor
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-keep class com.grack.nanojson.** { *; }
-dontwarn com.grack.nanojson.**

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep R8 from stripping Compose runtime internals
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# General Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
