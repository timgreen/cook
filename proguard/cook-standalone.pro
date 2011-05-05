-injars cook-unprocessed.jar
-injars ../lib/scala-library.jar(!META-INF/MANIFEST.MF,!library.properties)
-injars ../lib/ant.jar(!META-INF/**,!images/**,!org/apache/tools/ant/version.txt)
-injars ../lib/ant-launcher.jar(!META-INF/MANIFEST.MF)

-outjars cook-standalone.jar

-libraryjars <java.home>/lib/rt.jar


# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

-overloadaggressively

# Put all obfuscated classes into the nameless root package.

-repackageclasses ''

# Allow classes and class members to be made public.

-allowaccessmodification

# Keep sourcefile & linenumber
-keepparameternames
-keepattributes Exceptions,SourceFile,LineNumberTable

# The entry point: cook.app.Main and its main method.

-keep public class cook.app.Main {
    public static void main(java.lang.String[]);
}

#
-dontwarn **$$anonfun$*
-dontwarn scala.collection.immutable.RedBlack$Empty
-dontwarn scala.tools.**,plugintemplate.**

# enumeration
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# MODULE$
-keepclassmembers class * {
    ** MODULE$;
}
