-injars cook-unprocessed.jar
-outjars cook-processed.jar

-libraryjars <java.home>/lib/rt.jar
-libraryjars ../lib/ant.jar
-libraryjars ../lib/Mouse-1.3.jar
-libraryjars ../lib/scala-library.jar
-libraryjars ../lib/commons-cli-1.2.jar


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

# enumeration
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Scala
-dontwarn **$$anonfun$*
-dontwarn scala.collection.immutable.RedBlack$Empty
-dontwarn scala.tools.**,plugintemplate.**
-keepclassmembers class * {
    ** MODULE$;
}
-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
    long eventCount;
    int  workerCounts;
    int  runControl;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
}
-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
    int base;
    int sp;
    int runState;
}
-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
    int status;
}
-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
}
