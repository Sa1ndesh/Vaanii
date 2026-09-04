# ProGuard rules for Vani-Kanoon
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
