# tcp-relay-android

A naive Android app for two-way TCP relay using only **incoming** connections, used in the paper [An Empirical Study of Android Test Generation Tools in Industrial Cases](https://dl.acm.org/citation.cfm?id=3240465) for enabling Ella agent (on the app side) to access the coverage log server (on the computer side) using only `adb forward` (as opposed to `adb reverse`). See [here](https://github.com/ms1995/ella-customized) for more details on the Ella version we used in the paper.

### How to build and use?

- Make sure you have the standard Android building environment (including necessary settings such as environment variables). Also install Android SDK 28 and Android build tools 28.0.3 (or alternatively change such version names in `./build.gradle` to whatever you have).
- Run `./gradlew assembleDebug`, find the built APK in `./build/outputs/apk/debug/`, and install it on the target device.
- Start an ADB Shell session and run `am startservice -n edu.illinois.cs.ase.reportrelay/.RelayService -a start --ei pA YOUR_PORTA_NUMBER_HERE --ei pB YOUR_PORTB_NUMBER_HERE` (replace with the port numbers you want the app to listen on). Now the app should be ready to accept connections. You can check out LogCat to see the log messages from the app.
- To stop, execute `am force-stop edu.illinois.cs.ase.reportrelay`.

### Known issues?

- It's possible for the app to be killed by the system after a while. While a more elegant solution is to re-structure the app using sticky services, in the experiments I simply re-ran the starting command every minute to make sure the app is alive. You are very welcome to submit your enhancements to this naive app!

### License?

Just do whatever you want as long as it's for non-commercial purposes. It would be appreciated if you can cite our paper, though. :-)
