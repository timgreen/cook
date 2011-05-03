-injars cook-unprocessed.jar
-outjars cook-processed.jar

-libraryjars <java.home>/lib/rt.jar
-libraryjars ../lib/ant.jar
-libraryjars ../lib/Mouse-1.3.jar
-libraryjars ../lib/scala-library.jar


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
