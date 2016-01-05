# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/x7c1/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn scala.beans.**
-dontwarn scala.concurrent.**
-dontwarn scala.sys.process.**

-dontwarn x7c1.wheat.macros.**

# to use `format` method of String class
-keepclassmembers class * {
    java.lang.String toString();
}

# to use scalaz
-dontwarn scala.util.parsing.**
-dontwarn scala.xml.**
-dontwarn javax.swing.**

# needless?
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
    # Note: the configuration refers to the unknown class 'com.google.vending.licensing.ILicensingService'
    # Note: the configuration refers to the unknown class 'com.android.vending.licensing.ILicensingService'
-dontnote scala.Enumeration$$anonfun$scala$Enumeration$$isValDef$1$1
    # Note: scala.Enumeration$$anonfun$scala$Enumeration$$isValDef$1$1 calls 'Field.getType'
