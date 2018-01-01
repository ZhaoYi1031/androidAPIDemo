Analyze a android API demo.

From [https://github.com/googlesamples/android-PermissionRequest](https://github.com/googlesamples/android-PermissionRequest)

modify configuration: [Issue](https://stackoverflow.com/questions/44546849/unsupported-method-baseconfig-getapplicationidsuffix)

* Open build.gradle and change the gradle version to the recommended version: `classpath 'com.android.tools.build:gradle:1.3.0'` to
`classpath 'com.android.tools.build:gradle:2.3.2'`
* Hit 'Try Again'
* In the messages box it'll say 'Fix Gradle Wrapper and re-import project' Click that, since the minimum gradle version is 3.3
* A new error will popup and say The SDK Build Tools revision (23.0.1) is too low for project ':app'. Minimum required is 25.0.0 - Hit Update Build Tools version and sync project
* A window may popup that says Android Gradle Plugin Update recommended, just update from there.




