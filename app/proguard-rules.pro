# R8 keeps Compose and Kotlin metadata intact via the AGP-supplied rules.
# These cover the reflective and annotation-driven corners of this app.

# Keep line numbers so a release crash report is still readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# DataStore reads the generated Preferences class by name.
-keep class androidx.datastore.*.** { *; }

# Compose tooling metadata is not needed at runtime and is large.
-dontwarn org.jetbrains.annotations.**
