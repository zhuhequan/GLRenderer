#不混淆GLRendererNotProguard注解 {@
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep @com.android.common.renderer.GLRendererNotProguard class * {*;}
-keepclasseswithmembers class * {
    @com.android.common.renderer.GLRendererNotProguard <methods>;
}
# @}