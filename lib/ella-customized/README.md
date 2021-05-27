# ella-customized

A customized version of Ella used in the paper [An Empirical Study of Android Test Generation Tools in Industrial Cases](https://dl.acm.org/citation.cfm?id=3240465). See [here](https://github.com/saswatanand/ella) for the original version by Saswat Anand. Some implementations are still ad-hoc and improvements are welcomed.

Basically, I added support for [MultiDex](https://developer.android.com/studio/build/multidex), fixed numerous bugs in the original implementation, and simplified some code. The usages are mostly the same with several exceptions stated below. Note that I only preserved the cumulative method coverage recorder in this version.

### Extra tips for `ella.settings`

* Modify `ella.x.aapath` to point it to the directory containing `apkanalyzer` (available in recent versions of Android SDK tools).
* It's recommended to use ADB (instead of network) as the communication channel between Ella runtime and server. To do this, keep using `127.0.0.1` in `ella.server.ip`, and set up `adb reverse tcp:23745 tcp:23745` on the computer before running the instrumented app.
  - Note that `adb reverse` is not available on Android 4.4, but `adb forward` and one relay app accepting two incoming connections (see [here](https://github.com/ms1995/tcp-relay-android)) can be used. Essentially, you need to run the relay app on the device, let the Ella agent connect to one port listened by the app, use `adb forward` to forward the other port listened by the app to some port on the computer, and use another TCP relay program (e.g., socat, by running `socat TCP:localhost:23745 TCP:localhost:PORT_BY_ADB_FORWARD`) to finally connect this port to Ella server.
* The generated keystore seemed to have some issues. Thus I (shamelessly) took the keystore from SwiftHand. BTW, thanks, Wontae :-)

### Extra steps for Multidex support

*The following steps are required only once before instrumenting the first app with multiple DEX files.*

* Make sure the directory containing `dx` (available in Android build tools) is in `$PATH` (or alternatively `gen-ella-wrappers.py`).
* Modify `MAX_COUNT` in `gen-ella-wrappers.py` to be the maximum number of additional DEX files you want to instrument. The default value is 20.
* Run `gen-ella-wrappers.py` to generate wrapper classes for additional DEX files, which would be used subsequently by instrumentation.

### Known issue(s)

#### `com.android.dex.DexIndexOverflowException: method ID not in [0, 0xffff]: 65536`

Some DEX file contains exactly 65536 methods, and there’s no extra space to put Ella’s runtime stub (Dalvik requires each DEX to contain up to 65536 methods, and that’s why MultiDex exists.) Such APKs cannot be processed by Ella at the moment given that they require moving methods across DEX files.

### License?

For my parts, just do whatever you want as long as it's for non-commercial purposes. It would be appreciated if you can cite our paper, though. :-)

## ELLA: A Tool for Binary Instrumentation of Android Apps


Ella is a tool to instrument Android APK's for various purposes. Out of the box, it instruments
apps to record which methods gets executed. It can also record time-stamped trace of executed
methods, values of arguments passed at call-sites, values of formal parameters of methods, etc.

Several tools exist that can instrument APK's to some
degree. But they usually do not work very reliably because they
translate Dalvik bytecodes to another form such as Java bytecode or
internal representations of other tools, and this translation is quite
challenging.  Thus, Ella's approach is to instrument at the Dalvik
bytecode level. It does so by builiding atop the DexLib2 library (a part
of the [Smali](https://github.com/JesusFreke/smali) project).

## Pre-requisite
1. Unix-like operating system. Minor tweaks to scripts and build files may be needed to run ella on Windows.
2. Android SDK
3. Java SDK
4. Apache Ant

## Before building ella
1. Rename `ella.settings.template` file to `ella.settings`, and if needed, set values of different environment variables of ella.
2. If the instrumented app will be executed on an emulator **and** the ella server will be run on the host machine of the emulator, then do nothing. Otherwise, set:
```
ella.use.emulator.host.loopback=false
```
If `ella.use.emulator.host.loopback` is set to `false` **and** the ella server will be running on a machine that is different from the machine on which the instrumentor is run, then set `ella.server.ip` to the IP address of the machine on which the ella server will be run. For example,
```
ella.server.ip=1.2.3.4
```
Otherwise, do nothing.

## Build ella
Execute the following command inside ella's installation directory.
```
ant 
```

## Instrument the app
Execute the following command.
```
ella.sh i <path-to-apk>
```

`<path-to-apk>` is the Apk that you want to instrument. This command would produce the instrumented apk named `instrumented.apk` inside a subdirectory inside `<ella-home>/ella-out` directory, where `<ella-home>` represents the installation directory of ella. The name of the subdirectory is derived from `<path-to-apk>`.

## Start ella server
Before executing any instrumented app, ella server must be up and running. To start the ella server, execute the following command.
```
ella.sh s
```
Whenever needed, the ella server can be shutdown by executing the following command
```
ella.sh k
```
## Execute the instrumented app 
1. Install the `instrumented.apk` on the emulator or device. You may have to uninstall it first if the app is already installed.
2. Execute the instrumented app. The instrumented app will send coverage data periodically to the ella server.
3. To end recording and uploading coverage data, either simply kill the app **or** execute the following command on computer connected to the device/emulator. `e` stands for "end" in end recording coverage data.
```
ella.sh e
```

## Coverage data

The coverage data are stored inside a subdirectory of `<ella-home>/ella-out` directory, where `<ella-home>` represents the installation directory of ella. The name of the subdirectory is derived from `<path-to-apk>`. Currently, coverage data are stored in files `coverage.dat` and `covids`. `covids` contain the list of method signatures; index of a method is its identifier. `coverage.dat` contains the list of method identifiers that were executed.
