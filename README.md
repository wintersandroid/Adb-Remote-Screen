# Adb-Remote-Screen
Simple tool to control your device through ADB. Useful to retrieve data for broken devices.
Supports touches, swipes and keys input.

[**DOWNLOAD**](https://github.com/wintersandroid/Adb-Remote-Screen/releases/download/2.1/AdbRemoteScreen-2.1.jar)

![alt tag](https://raw.githubusercontent.com/MajeurAndroid/Adb-Remote-Screen/master/web_demo.png)

### How to use
AdbRemoteScreen needs ADB to work. You must provide an adb binary path in the local.properties file. (Or as an argument if you are running jars from command line)

###### From UI:

Right click on jar file > open with > Oracle Java X Runtime or OpenJDK X Runtime.

###### In command line :
```shell
cd path/to/jar/file
java -jar AdbRemoteScreen-2.1.jar
#or
java -jar AdbRemoteScreen.jar /path/to/adb/binary
```

### New features in 2.1
- Works in Windows
- Defaults for config file if not found.
- Screenshots are now retrieved directly from device to computer's RAM through adb shell stream
- Support for multiple connected devices
- Added keys input support (all keys supported by Android are available)
- Hot-Plug support (you can now unplug or plug a new device while running ARS, it will automatically detect changes)
- Fixed bugs

### TODOs
- Add directory dialog to let user choose adb file through UI
- Better handle device changes
- 
### Note

### License

Copyright Majeur 2015-2016

Licensed under Apache License 2.0
