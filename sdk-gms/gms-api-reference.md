# GMS SdkApi Guidance Document

> Android SDK API for motherboard control on devices running Android 10 and above.

## Overview

GMS SdkApi provides a comprehensive set of APIs for controlling and interacting with industrial/commercial Android motherboards. The SDK enables applications to manage hardware peripherals, display settings, system controls, networking, and more through a unified interface.

**Current Version:** V1.20260410  
**Update Date:** 2026-05-10  
**Status:** Officially Released

## Repository Contents

| File / Directory | Description |
|---|---|
| `GMS SdkApi Guidance document_V1.20260510.docx` | Full API reference documentation |
| `SdkApiJar_V1.260410.0350.aar` | Android AAR library for integration |
| `Application Signing Guidance (Use your own Google Play or enterprise key)( )/` | Platform signing files (`platform.pk8`, `platform.x509.pem`) for system-level permissions |

## Target Audience

- Software Development Engineers
- Technical Support Engineers
- Sales Engineers

## Quick Start

### 1. Import the AAR Library

Copy `SdkApiJar.aar` to your project's `app/libs` directory, then add the dependency in `build.gradle`:

```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.aar'])
}
```

### 2. Initialize the SDK

```java
// Bind the SDK service
SdkApi.newInstance(this);

// Wait ~2 seconds for the service to connect, then verify
boolean connected = SdkApi.getInstance().getServiceConnectionStatus();

// Get the SdkApi instance
SdkApi api = SdkApi.getInstance();
```

### 3. Call API Methods

```java
// Example: Get the motherboard serial number
String sn = SdkApi.getInstance().DeviceInfo().getSerialNumber();

// Example: Set screen brightness to 80%
SdkApi.getInstance().Display().setBrightness(ApiTool.Screen.ALL, 80);

// Example: Reboot the device
SdkApi.getInstance().SystemCtrl().deviceReboot();
```

### 4. Unbind the SDK (on destroy)

```java
SdkApi.getInstance().unBindApiSdkService();
```

## API Modules

| Module | Class | Description |
|---|---|---|
| Device Info | `ApiDeviceInfo` | Hardware information, system version, memory, storage, MAC address, product info |
| Display | `ApiDisplay` | Screen orientation, on/off, brightness, resolution |
| System Control | `ApiSystemCtrl` | Reboot, screenshot, status/navigation bar, standby, app install/uninstall, factory reset, touch control |
| Timer | `ApiTimerOnoff` | Scheduled power on/off |
| Watchdog | `ApiWatchdog` | Watchdog timer management |
| System Time | `ApiSystemTime` | System time, RTC time, NTP auto-calibration |
| Hardware | `ApiHardware` | GPIO, serial port, USB port control |
| Other | `ApiOther` | SIM ICCID, IMEI, shell commands, eMMC lifespan |
| CommonEvent | `ApiCommonEvent` | WiFi, Bluetooth, Ethernet, AP, OTA update, boot animation |

## API Reference

All API calls follow the pattern:

```
SdkApi.getInstance().<Module>().<Method>()
```

### Device Info

```java
// Get AAR library version
String version = SdkApi.getInstance().DeviceInfo().getApiVersion();

// Get motherboard system version
String fw = SdkApi.getInstance().DeviceInfo().getFirmwareVersion();

// Get chip name (e.g., "rk3576")
String chip = SdkApi.getInstance().DeviceInfo().getChipName();

// Get serial number
String sn = SdkApi.getInstance().DeviceInfo().getSerialNumber();

// Get memory / storage size
String ddr = SdkApi.getInstance().DeviceInfo().getDDRSize();
String emmc = SdkApi.getInstance().DeviceInfo().getEMMCSize();

// Get available memory / storage
String ddrAvail = SdkApi.getInstance().DeviceInfo().getDDRAvailableSize();
String emmcAvail = SdkApi.getInstance().DeviceInfo().getEMMCAvailableSize();

// Get Ethernet MAC address
String mac = SdkApi.getInstance().DeviceInfo().getEthMac();

// Set / get screen density (range: 100-480, default: 160)
SdkApi.getInstance().DeviceInfo().setLcdDensity(240);
int density = SdkApi.getInstance().DeviceInfo().getLcdDensity();

// Get system property
String prop = SdkApi.getInstance().DeviceInfo().getProperty("persist.sys.timezone", "Asia/Shanghai");

// Set / get product info (1=Model, 2=Device, 3=Brand, 4=Name, 5=Manufacturer)
SdkApi.getInstance().DeviceInfo().setProductInfo(1, "MyModel");
String model = SdkApi.getInstance().DeviceInfo().getProductInfo(1);
```

### Display Control

Screen IDs: `ApiTool.Screen.ALL` (0), `ApiTool.Screen.LVDS` (1), `ApiTool.Screen.eDP` (3), `ApiTool.Screen.MIPI` (4), `ApiTool.Screen.HDMI` (6)

```java
// Set screen orientation (0/90/180/270)
SdkApi.getInstance().Display().setScreenRotate(ApiTool.Screen.ALL, 90);

// Set screen on/off
SdkApi.getInstance().Display().setScreenOnoff(ApiTool.Screen.ALL, true);

// Set brightness (0-100)
SdkApi.getInstance().Display().setBrightness(ApiTool.Screen.ALL, 80);

// Get screen resolution (async)
SdkApi.getInstance().Display().getResolution(ApiTool.Screen.ALL,
    new ResultCallback<ResultInfo<String>>() {
        @Override
        public void onMsg(String val) {
            // val format: "1920x1080@60"
        }
    });
```

### System Control

```java
// Reboot device
SdkApi.getInstance().SystemCtrl().deviceReboot();

// Screenshot (PNG format, main screen only)
SdkApi.getInstance().SystemCtrl().shotScreen("/mnt/sdcard/1.png", 0,
    new ResultCallback<ResultInfo<Boolean>>() {
        @Override
        public void onMsg(Boolean val) { }
    });

// Hide/show status bar and navigation bar
SdkApi.getInstance().SystemCtrl().setStatusBarDisplay2(false);  // status bar
SdkApi.getInstance().SystemCtrl().setNavigationBar(false);       // navigation bar

// Standby modes
SdkApi.getInstance().SystemCtrl().setDeviceShallowOnoff(true);   // shallow standby
SdkApi.getInstance().SystemCtrl().setDeviceDeepStandby();         // deep standby
SdkApi.getInstance().SystemCtrl().setDeviceDeepStandby(60);       // deep standby, wake in 60 min

// Silent install/uninstall
SdkApi.getInstance().SystemCtrl().silentInstallApk("/mnt/sdcard/app.apk", false,
    new ResultCallback<ResultInfo<Boolean>>() { ... });
SdkApi.getInstance().SystemCtrl().silentUninstallApk("com.example.app",
    new ResultCallback<ResultInfo<Boolean>>() { ... });

// Factory reset (0 = reset all, including local files)
SdkApi.getInstance().SystemCtrl().recoverySystem(0);

// Touch control (0=all, 1=main, 2=secondary)
SdkApi.getInstance().SystemCtrl().setTouchState(1, "1000", "1001", 1);  // enable
int state = SdkApi.getInstance().SystemCtrl().getTouchState(1, "1000", "1001");
```

### Timer (Scheduled Power On/Off)

```java
// Enable timer
SdkApi.getInstance().TimerSwitch().setTimerSwitchOnoff(true);

// Set scheduled on/off time (format: "HH:mm")
SdkApi.getInstance().TimerSwitch().setTimerOnTime("09:00");
SdkApi.getInstance().TimerSwitch().setTimerOffTime("22:00");

// Get scheduled times
String onTime = SdkApi.getInstance().TimerSwitch().getTimerOnTime();
String offTime = SdkApi.getInstance().TimerSwitch().getTimerOffTime();
```

### Watchdog

```java
// Set watchdog to APP mode (default: system)
SdkApi.getInstance().Watchdog().setWatchdogOnoff(true);

// Set timeout (30s - 5min, default: 3min)
SdkApi.getInstance().Watchdog().setWatchdogTimeout(60);

// Feed the watchdog
SdkApi.getInstance().Watchdog().feedWatchdog();
```

### System Time

```java
// Set system time (milliseconds since epoch)
SdkApi.getInstance().SystemTime().setSystemTime(System.currentTimeMillis());

// Get RTC time
String rtc = SdkApi.getInstance().SystemTime().getRtcTime();
// format: "yyyy/MM/dd hh:mm:ss"

// Enable/disable NTP auto-calibration
SdkApi.getInstance().SystemTime().setAutoTimeOnoff(true);
```

### Hardware (GPIO / Serial / USB)

```java
// GPIO read/write
int level = SdkApi.getInstance().Hardware().getGpioInput(ApiTool.GpioPin.GPIO0, ApiTool.GpioPin.PB3);
SdkApi.getInstance().Hardware().setGpioOutput(ApiTool.GpioPin.GPIO0, ApiTool.GpioPin.PB3, true);

// Serial port (8N1 mode)
int fd = SdkApi.getInstance().Hardware().openSerial("/dev/ttyS0", 115200);
byte[] buf = new byte[256];
int len = SdkApi.getInstance().Hardware().readSerial(fd, buf, 256);
SdkApi.getInstance().Hardware().writeSerial(fd, buf, len);
SdkApi.getInstance().Hardware().closeSerial(fd);

// USB port power control
SdkApi.getInstance().Hardware().setUsbPortPowerOnoff(port, true);
int portNum = SdkApi.getInstance().Hardware().getUsbPortNumber("1000", "1001");
```

### Other

```java
// Get SIM card ICCID (requires 4G module)
String iccid = SdkApi.getInstance().Other().getSimICCID();

// Get IMEI
String imei = SdkApi.getInstance().Other().getModemIMEI();

// Execute shell command
SdkApi.getInstance().Other().execShellCommand("input keyevent 26");

// Read eMMC lifespan
String life = SdkApi.getInstance().Other().getEmmcLeftTime();
```

### CommonEvent (WiFi / Bluetooth / Ethernet / OTA)

```java
// Register listener
SdkApi.getInstance().CommonEvent().registerCommonListener(
    ApiTool.CommonAppId.ID_APP_CUSTOM, new ISdkCommonListener() {
        @Override
        public void CommonListenerNotify(int actionId, String sVal, int[] iArray, int intArrayLen) {
            // Handle callback
        }
    });

// Toggle WiFi
int[] data = new int[]{0};
SdkApi.getInstance().CommonEvent().setCommonEvent(
    ApiTool.CommonAppId.ID_APP_CUSTOM, ApiTool.CommonEvent.SWITCH_WIFI, "on", data, data.length);

// Toggle Bluetooth
SdkApi.getInstance().CommonEvent().setCommonEvent(
    ApiTool.CommonAppId.ID_APP_CUSTOM, ApiTool.CommonEvent.SWITCH_BLUETOOTH, "on", data, data.length);

// Toggle Ethernet
SdkApi.getInstance().CommonEvent().setCommonEvent(
    ApiTool.CommonAppId.ID_APP_CUSTOM, ApiTool.CommonEvent.SWITCH_ETH, "on", data, data.length);

// OTA update (default path: /mnt/sdcard/update.zip)
SdkApi.getInstance().CommonEvent().setCommonEvent(
    ApiTool.CommonAppId.ID_APP_CUSTOM, ApiTool.CommonEvent.UPDATE_SYSTEM_OTA, "/mnt/sdcard/update.zip", data, data.length);
```

## CommonEvent Action IDs

| Action | Code | Description |
|---|---|---|
| `EVENT_GET_LT_VERSION` | `0x1003` | Get LongXun chip version |
| `SWITCH_WIFI` | `0x9000` | WiFi on/off |
| `GET_WIFI_MAC` | `0x9001` | Get WiFi MAC address |
| `SWITCH_BLUETOOTH` | `0x9010` | Bluetooth on/off |
| `GET_BLUETOOTH_MAC` | `0x9011` | Get Bluetooth MAC address |
| `SWITCH_ETH` | `0x9020` | Ethernet on/off |
| `SWITCH_AP` | `0x9030` | AP hotspot on/off |
| `GET_ETH_STATUS` | `0x9040` | Get Ethernet status |
| `SET_ETH_IP` | `0x9050` | Set Ethernet IP (static/DHCP) |
| `GET_ETH_INFO` | `0x9060` | Get network information |
| `UPDATE_SYSTEM_OTA` | `0x9091` | OTA system update |
| `UPDATE_SYSTEM_ANIMATION` | `0x9096` | Update boot animation/logo/wallpaper |

## Application Signing Guidance (Use your own Google Play or enterprise key)s

The `Application Signing Guidance (Use your own Google Play or enterprise key)( )/` directory contains platform signing credentials required for system-level API access:

- `platform.pk8` - Platform private key
- `platform.x509.pem` - Platform X.509 certificate

These files are needed when signing your APK with system platform privileges.

## Notes

- After calling `newInstance()`, wait approximately 2 seconds for the service to connect before making API calls.
- Screen orientation changes require a system restart to take effect.
- The timer function activates 2 minutes after device power-on.
- Silent install from USB drive: be aware that unplugging the drive after installation may cause the app to crash. Consider using `recoverySystem` for USB-based deployments.
- Logo/animation update supports zip and mp4 (H.264/H.265) formats, max 60MB, max 4K resolution.
