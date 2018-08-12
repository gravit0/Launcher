-libraryjars '<java.home>/lib/rt.jar'
-libraryjars '<java.home>/lib/jce.jar'
-libraryjars '<java.home>/lib/ext/jfxrt.jar'
-libraryjars '<java.home>/lib/ext/nashorn.jar'

-printmapping 'mapping.pro'
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute Source

-dontnote
-dontwarn
-dontshrink
-dontoptimize
-ignorewarnings
-target 8
-forceprocessing

-obfuscationdictionary 'dictionary.pro'
-classobfuscationdictionary 'dictionary.pro'
-overloadaggressively
-repackageclasses 'launcher'
-keepattributes SourceFile,LineNumberTable,*Annotation*
-renamesourcefileattribute SourceFile
-adaptresourcefilecontents META-INF/MANIFEST.MF

-keeppackagenames com.eclipsesource.json.**,com.mojang.**

-keep class com.eclipsesource.json.**,com.mojang.** {
    <fields>;
    <methods>;
}

-keepclassmembers @launcher.LauncherAPI class ** {
    <fields>;
    <methods>;
}

-keepclassmembers class ** {
    @launcher.LauncherAPI
    <fields>;
    @launcher.LauncherAPI
    <methods>;
}

-keepclassmembers public class ** {
    public static void main(java.lang.String[]);
}

-keepclassmembers enum ** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
