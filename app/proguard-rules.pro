# Room Database
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# BCrypt Password Hashing
-keep class at.favre.lib.crypto.bcrypt.** { *; }
-keep class at.favre.lib.bytes.** { *; }

# Firebase (Firestore, Auth, Messaging)
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}
-keep class com.google.firebase.** { *; }

# Moshi & Retrofit
-keepattributes Signature
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Kotlin Coroutines
-keepclassmembers class * extends kotlinx.coroutines.internal.MainDispatcherFactory {
    public <init>();
}
