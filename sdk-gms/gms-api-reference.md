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


---

# SC1 / SK1 / SK1-15 GMS SdkApi Detailed Reference

GMS SdkApi Guidance document

| Current Version | V1.20260410 |
| :--- | :--- |
| Update Date | 2026-05-10 |
| Confidentiality Level | [] Internal[√] Public |
| Status | [] Draft [√] Officially released[] Under revision |

### Overview

APIs supported by motherboards running Android 10 and above, along with their corresponding descriptions.

### Target Audience

This document is mainly intended for the following engineers:

Software Development Engineers

Technical Support Engineers

Sales Engineers

Catalogue

### API Detailed Description

#### API Import Method

Import aar package into Android Studio

Copy the araar package [SdkApiJar.aar] to the project directory /app/libs.

Add references to build.gradle files in the app.

Register SdkApi.

Use the functions of the SdkApi-related class interface.

### API Registration Flowchart

#### API Module Classification

| module | Class Name | meaning |
| :--- | :--- | :--- |
| facility information | ApiDeviceInfo | Device Basic Info Interface |
| display control | ApiDisplay | Display relevant control interfaces |
| System control | ApiSystemCtrl | System control interface |
| Schedule | ApiTimerOnoff | Timer interface |
| Dog Settings | ApiWatchdog | Dog survival-related interface |
| Time Settings | ApiSystemTime | System time-related interfaces |
| hardware control | ApiHardware | Hardware control interface |
| Other Settings | ApiOther | Other related interfaces |
| Scale set up | ApiScale | Touchpoint chip control interface |
| General Settings | ApiCommonEvent | General Function Interface |

1. Initialize API

### 1.1 Bind SdkApi

Interface name:

void newInstance(Context context)

Parameter :

--context: context

Returned value :

-- None

Code example:

SdkApi.newInstance(this);

Remarks :

-- None

### 1.2 Obtain the SdkApi instance

Interface name:

SdkApi getInstance()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | class | SdkApi living example |

Code example:

SdkApi api = SdkApi.getInstance();

Remarks :

-- None

### 1.3 Get the SDK service connection status

Interface name:

boolean  getServiceConnectionStatus()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: Connection successful<br>false: Connection failed |

Code example:

boolean ret = SdkApi.getInstance().getServiceConnectionStatus();

Remarks :

After the--newInstance function completes, wait approximately 2 seconds for the service to connect successfully.

### 1.4 Unbind SDK API

Interface name:

void  unBindApiSdkService()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | void |   |

Code example:

SdkApi.getInstance().unBindApiSdkService();

Remarks :

--After unbinding, you need to rebind the SDK API

### Facility information API

--Interface call method: SdkApi.getInstance(). DeviceInfo(). Interface

### 2.1 Get the API aar library version

Interface name:

String getApiVersion()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | aar library version |

Code example:

```java
String version =  SdkApi.getInstance().DeviceInfo().getApiVersion();
Version: YYMMdd.hhmm
Instance : 250813.0759
```

Remarks :

-- None

### 2.2 Get API SDK service version

Interface name:

String getApiSdkServiceVersion()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | SDK service version |

Code example:

```java
String version =  SdkApi.getInstance().DeviceInfo().getApiSdkServiceVersion();
Version: V2.YYMMdd.hhmm ss
Example: V2.20 250813.0759 00
```

Remarks :

-- None

### 2.3 Get the motherboard system version

Interface name:

String getFirmwareVersion()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | System version |

Code example:

```java
String version =  SdkApi.getInstance().DeviceInfo().getFirmwareVersion();
Example: EC3576_RK3576_Android14_A_20250813.143037
```

Remarks :

-- None

### 2.4 Obtain the mainboards main chip name

Interface name:

String getChipName()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Main chip name |

Code example:

```java
String chip = SdkApi.getInstance().DeviceInfo().getChipName()
Instance : rk3576
```

Remarks :

-- None

### 2.5 Get the motherboard serial number

Interface name:

String getSerialNumber()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Mainboard unique serial number |

Code example:

```java
String sn = SdkApi.getInstance().DeviceInfo().getSerialNumber()
Instance : 12345678
```

Remarks :

--The data length is usually 8 or 21 bits

### 2.6 Get motherboard memory size

Interface name:

String getDDRSize()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Total memory size |

Code example:

```java
String ddrSize = SdkApi.getInstance().DeviceInfo().getDDRSize()
Instance : 4GB
```

Remarks :

-- None

### 2.7 Get motherboard storage size

Interface name:

String getEMMCSize()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Total storage size |

Code example:

```java
String ddrSize = SdkApi.getInstance().DeviceInfo().getEMMCSize()
Instance : 32GB
```

Remarks :

-- None

### 2.8 Set system screen density

Interface name:

void setLcdDensity(int density)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| syntony | int | Screen density<br>Value range: 100-480<br>Default: 160 |

Returned value :

-- None

Code example:

SdkApi.getInstance().DeviceInfo().setLcdDensity ( density);

Remarks :

Some systems need to restart to take effect

### 2.9 Get system screen density

Interface name:

int getLcdDensity()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | Screen density |

Code example:

int density = SdkApi.getInstance().DeviceInfo().getLcdDensity()

Remarks :

-- None

### 2.10 Get the system prop attribute

Interface name:

String getProperty(String key, String default)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| key | String | Prop key name |
| default | String | Prop key default value |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| parameter values | String | parameter value corresponding to the system prop attribute |

Code example:

```java
String prop = SdkApi.getInstance().DeviceInfo().getProperty(
" persist.sys.timezone" , "Asia/Shanghai" )
```

Remarks :

-- None

### 2.11 Get the motherboards Ethernet MAC address

Interface name:

String getEthMac()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Ethernet MAC address, format: "0C63FC112233" |

Code example:

String mac =  SdkApi.getInstance().DeviceInfo().getEthMac()

Remarks :

When a board has multiple MAC addresses, the results are separated by @. For example: 0C63FC112233@0C63FC445566

--> Previous Mac: Ethernet 0

----> The latter Mac: Ethernet 1

### 2.12 Get Motherboard Available Memory Size

Interface name:

String getDDRAvailableSize()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Total Motherboard Memory Size |

Code example:

```java
String ddrSize = SdkApi.getInstance().DeviceInfo().getDDRAvailableSize()
Example: 2.6GB
```

Remarks :

-- None

### 2.13 Get Motherboard Available Storage Size

Interface name:

String getEMMCAvailableSize()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Total Motherboard Storage Size |

Code example:

```java
String ddrSize = SdkApi.getInstance().DeviceInfo().getEMMCAvailableSize()
Example: 32.0GB
```

Remarks :

-- None

### 2.14 Set Device Product Information

Method Name:
void setProductInfo(int type, String data)

Parameters:

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| type | int | Product value ID<br>-- 1: Model [product.model]<br>-- 2: Device name [product.device]<br>-- 3: Brand name [product.brand]<br>-- 4: Product name [product.name]<br>-- 5: Manufacturer name [product.manufacturer] |
| data | String | New content |

Return Value:
-- None

Code Example:
SdkApi.getInstance().DeviceInfo().setProductInfo(1, "1234")

Remarks :

-- None

### 2.15 Get Device Product Information

Method Name:
String getProductInfo(int type)

Parameters:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| type | int | Product value ID<br>-- 1: Model [product.model]<br>-- 2: Device name [product.device]<br>-- 3: Brand name [product.brand]<br>-- 4: Product name [product.name]<br>-- 5: Manufacturer name [product.manufacturer] |

Return Value:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| Return Value | String | Node value |

```java
Code Example:
String value = SdkApi.getInstance().DeviceInfo().getProductInfo(1)
Example: 1234
```

Remarks :

-- None

### Show relevant API

--Interface call method: SdkApi.getInstance(). Display(). Interface

--screen display interface ID includes

| ID | Define in.aar | Interface value | Explain |
| :--- | :--- | :--- | :--- |
| ALL | ApiTool.Screen.ALL | 0 | Set up all display interfaces on the motherboard |
| LVDS | ApiTool.Screen.LVDS | 1 | LVDS interface defined in motherboard specifications |
| eDP | ApiTool.Screen.eDP | 3 | The eDP interface defined in the motherboard specifications |
| MIPI | ApiTool.Screen.MIPI | 4 | The MIPI interface defined in the motherboard specifications |
| HDMI | ApiTool.Screen.HDMI | 6 | HDMI interface defined in motherboard specifications |

### 3.1 Set screen orientation

Interface name:

boolean setScreenRotate(int screen, int rotate)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| rotate | int | Direction: 0/90/180/270 |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

boolean ret = SdkApi.getInstance().Display().setScreenRotate(
ApiTool.Screen.ALL, 90)

Remarks :

Restart the system to take effect

### 3.2 Get screen orientation

Interface name:

void getScreenRotate(int screen, ResultCallback<ResultInfo<Integer>> cb )

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | int | Error code: 0/90/180/270 |

Code example:

```java
SdkApi.getInstance().Display().getScreenRotate(ApiTool.Screen.ALL,
    new ResultCallback<ResultInfo<Integer>>() {
        @Override
        public void onMsg(Integer val) {
        }
})
```

Remarks :

If the motherboard has dual outputs, setting screen to all returns the current main screen status.

### 3.3 Set screen switch

Interface name:

boolean setScreenOnoff(int screen, boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| onoff | boolean | true:  open<br>false:  close |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

boolean ret = SdkApi.getInstance().Display().setScreenOnoff(
ApiTool.Screen.ALL, true)

Remarks :

-- None

### 3.4 Get screen switch

Interface name:

void getScreenOnoff(int screen, ResultCallback<ResultInfo<Boolean>> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | String | true:  open<br>false:  close |

Code example:

```java
SdkApi.getInstance().Display().getScreenOnoff(ApiTool.Screen.ALL,
    new ResultCallback<ResultInfo<Boolean>>() {
        @Override
        public void onMsg(Boolean val) {
        }
})
```

Remarks :

If the motherboard has dual outputs, setting screen to all returns the current main screen status.

### 3.5 Set screen brightness

Interface name:

boolean setBrightness(int screen, int brightness)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| brightness | int | screen intensity<br>Range: 0-100 |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

boolean ret = SdkApi.getInstance().Display().setBrightness(
ApiTool.Screen.ALL, 100)

Remarks :

-- None

### 3.6 Get screen brightness

Interface name:

void getBrightness(int screen, ResultCallback<ResultInfo<Integer>> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | int | Screen brightness: 0-100 |

Code example:

```java
SdkApi.getInstance().Display().getBrightness(ApiTool.Screen.ALL,
    new ResultCallback<ResultInfo<Integer>>() {
    @Override
        public void onMsg(Integer val) {
        }
})
```

Remarks :

If the motherboard has dual outputs, setting screen to all returns the current main screen status.

### 3.7 Get screen resolution

Interface name:

void getResolution(int screen, ResultCallback<ResultInfo<String>> cb )

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| screen | int | Display interface ID |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | String | Return value: resolution<br>Format: "1920x1080@60" |

Code example:

```java
SdkApi.getInstance().Display().getResolution(ApiTool.Screen.ALL,
    new ResultCallback<ResultInfo<String>>() {
        @Override
        public void onMsg(String val) {
        }
})
```

Remarks :

If the motherboard has dual outputs, setting screen to all returns the current main screen status.

### System Control API

--Interface call method: SdkApi.getInstance(). SystemCtrl(). Interface

### 4.1 Restart the device

Interface name:

void deviceReboot()

Parameter :

-- None

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemCtrl().deviceReboot()

Remarks :

-- None

### 4.2 Screenshot

Interface name:

void shotScreen(String path, int screen, ResultCallback<Boolean> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| path | String | Screenshot file path and name |
| screen | int | Primary and secondary screens<br>0: Home screen<br>1: Secondary screen<br>Default value: 0 |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | boolean | syntony val:<br>true:  success<br>false:  fail |

Code example:

```java
SdkApi.getInstance().SystemCtrl().shotScreen("/mnt/sdcard/1.png", 0 , new ResultCallback<ResultInfo<Boolean>>() {
        @Override
        public void onMsg( Boolean val) {
        }
})
```

Remarks :

--The screenshot file format must be PNG. Currently, only main screen captures are supported.

### 4.3 Hide or show the navigation bar and status bar

Interface name:

void setStatusBarDisplay(boolean display)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| display | boolean | true: Navigation bar display<br>false: Hide navigation bar |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemCtrl().setStatusBarDisplay(false)

Remarks :

--Old interface, unified control of navigation bar and status bar

This interface is being phased out

### 4.4 Access the navigation bar and status bar

Interface name:

boolean getStatusBarDisplay()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: Navigation bar display<br>false: Hide navigation bar |

Code example:

SdkApi.getInstance().SystemCtrl().getStatusBarDisplay()

Remarks :

--Old interface, unified navigation bar and status bar

This interface is being phased out

### 4.5 Hide or show the status bar

Interface name:

void setStatusBarDisplay2(boolean display)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| display | boolean | true: Status bar display<br>false: hide status bar |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemCtrl().setStatusBarDisplay2(false)

Remarks :

-- None

### 4.6 Get status bar status

Interface name:

boolean getStatusBarDisplay2()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: Status bar display<br>false: hide status bar |

Code example:

boolean ret = SdkApi.getInstance().SystemCtrl().getStatusBarDisplay2()

Remarks :

-- None

### 4.7 Hide or show the navigation bar

Interface name:

void setNavigationBar(boolean display)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| display | boolean | true: Navigation bar display<br>false: Hide navigation bar |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemCtrl().setNavigationBar(false)

Remarks :

-- None

### 4.8 Get navigation bar status

Interface name:

boolean getNavigationBar()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: Navigation bar display<br>false: Hide navigation bar |

Code example:

boolean ret =
SdkApi.getInstance().SystemCtrl().getNavigationBar()

Remarks :

-- None

### 4.9 General standby mode

Interface name:

void setDeviceShallowOnoff(boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| onoff | boolean | true:  starting up<br>false:  shut down |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemCtrl().setDeviceShallowOnoff( true )

Remarks :

-- None

### 4.10 Get device standby status

Interface name:

boolean getDeviceShallowOnoff()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  starting up<br>false:  shut down |

Code example:

boolean ret =  SdkApi.getInstance().SystemCtrl().getDeviceShallowOnoff()

Remarks :

-- None

### 4.11 Low-power standby mode

Interface name:

void setDeviceDeepStandby()

Parameter :

-- None

Returned value :

-- None

Code example:

SdkApi.getInstance().setDeviceDeepStandby()

Remarks :

--After executing this interface, the device will remain in standby mode until the next startup trigger condition is met.

--> Cut off power

--> Remote or button to turn on

### 4.12 Low-power timed standby for devices

Interface name:

void setDeviceDeepStandby(int time)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| time | int | Time to next startup<br>Unit: minutes<br>Range: 1-10080 |

Returned value :

-- None

Code example:

```java
//Turn on in 1 hour
SdkApi.getInstance().SystemCtrl().setDeviceDeepStandby(1*60)
```

Remarks :

-- None

### 4.13 Silent App installation

Interface name:

void silentInstallApk(String path, boolean isAuto, ResultCallback<ResultInfo<Boolean>> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| path | String | Absolute path for the app to install |
| isAuto | boolean | true: Start the app automatically after installation<br>false: Install the app without launching it |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | boolean | syntony val:<br>true:  success<br>false:  fail |

Code example:

```java
SdkApi.getInstance().SystemCtrl().silentInstallApk("/mnt/sdcard/1.apk", false, new ResultCallback<ResultInfo<Boolean>>() {
    @Override
    public void onMsg(Boolean val) {
    }
})
```

Remarks :

Before using this interface, you need to consider the actual business scenario, because

When the installation file is stored on a USB drive, unplugging it after app installation may cause the app to crash. For this scenario, we recommend using the 4.16 interface.

### 4.14 Silent App Installation

Interface name:

void silentInstallApkForPackage(String path, String packageName, boolean isAuto, ResultCallback<ResultInfo<Boolean>> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| path | String | Absolute path for the app to install |
| packageName | String | The app package name to install cannot be empty |
| isAuto | boolean | true: Start the app automatically after installation<br>false: Install the app without launching it |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | boolean | syntony val:<br>true:  success<br>false:  fail |

Code example:

```java
SdkApi.getInstance().SystemCtrl().silentInstallApkForPackage("/mnt/sdcard/1.apk", "com.xx.xx", false, new ResultCallback<ResultInfo<Boolean>>() {
    @Override
    public void onMsg(Boolean val) {
    }
})
```

Remarks :

-- None

### 4.15 Silent uninstallation of the app

Interface name:

void silentUninstallApk(String packageName, ResultCallback<ResultInfo<Boolean>> cb)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| packageName | String | App package name to uninstall |
| cb | class | ResultCallback class |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Return parameter val | boolean | syntony val:<br>true:  success<br>false:  fail |

Code example:

```java
SdkApi.getInstance().SystemCtrl().silentUninstallApk(
"com.suntek.sample.app",
new ResultCallback<ResultInfo<Boolean>>() {
    @Override
    public void onMsg(Boolean val) {
    }
})
```

Remarks :

-- None

### 4.16 Restore factory settings

Interface name:

int recoverySystem(int Type)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Type | int | 0: Restore all system parameters, including locally stored files |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | 0: Success<br>-1: Failed |

Code example:

```java
int ret =
SdkApi.getInstance().SystemCtrl().recoverySystem(0)
```

Remarks :

-- None

### 4.17 Set Touch Function State

Method Name:
boolean setTouchState(int screenType, String pid, String vid, int state)

Parameters:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| screenType | int | 0: All touchscreens<br>1: Main screen touch panel<br>2: Secondary screen touch panel |
| pid | String | Touch panel device PID (decimal) |
| vid | String | Touch panel device VID (decimal) |
| state | int | 0: Disable touch function<br>1: Enable touch function |

Return Value:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| Return Value | boolean | true: Success<br>false: Failure |

Code Example:

```java
boolean ret =
SdkApi.getInstance().SystemCtrl().setTouchState(1, "1000", "1001", 1);
```

Remarks:
-- screenType:

When set to 0, enable or disable all touch panels.

When set to 1 or 2, locate the touch panel by PID and VID, then enable or disable it.

### 4.18 Get Touch Function State

Method Name:
int getTouchState(int screenType, String pid, String vid)

Parameters:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| screenType | int | 0: All touchscreens<br>1: Main screen touch panel<br>2: Secondary screen touch panel |
| pid | String | Touch panel device PID (decimal) |
| vid | String | Touch panel device VID (decimal) |

Return Value:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| Return Value | int | -1: Touch panel not found<br>0: Touch function disabled<br>1: Touch function enabled |

Code Example:

```java
int ret =
SdkApi.getInstance().SystemCtrl().getTouchState(1, "1000", "1001");
```

Remarks:
-- screenType:

When set to 0, return the state of the first touch panel.

When set to 1 or 2, locate the touch panel by PID and VID, then return its state.

### Timer API

--Interface call method: SdkApi.getInstance(). TimerSwitch(). Interface

--Function description: Turn on during the time period and turn off outside the time period

Instance :

Turn on at 9:00, turn off at 10:00, and the device operates from 9:00 to 10:00

The device starts at 10:00 and shuts down at 9:00. The operating time is from 10:00 to 9:00 the next day.

The boot time equals the shutdown time. This invalid setting maintains the current status.

### 5.1 Set timer switch

Interface name:

void setTimerSwitchOnoff(boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| onoff | boolean | Timer function switch<br>Default: Off |

Returned value :

-- None

Code example:

SdkApi.getInstance().TimerSwitch().setTimerSwitchOnoff(true)

Remarks :

The timer starts 2 minutes after the device is powered on or the system restarts. The timer does not take effect within the first 2 minutes of power-on.

### 5.2 Get the timer switch status

Interface name:

boolean getTimerSwitchOnoff()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  open<br>false:  close |

Code example:

boolean ret =
SdkApi.getInstance().TimerSwitch().getTimerSwitchOnoff()

Remarks :

-- None

### 5.3 Set a scheduled startup time

Interface name:

boolean setTimerOnTime(String onTime)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| onTime | String | Scheduled on-time<br>Format: "HH: mm" |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

boolean ret =
SdkApi.getInstance().TimerSwitch().setTimerOnTime("12:01")

Remarks :

-- None

### 5.4 Clear scheduled startup time

Interface name:

void clearTimerOnTime()

Parameter :

-- None

Returned value :

-- None

Code example:

SdkApi.getInstance().TimerSwitch().clearTimerOnTime()

Remarks :

--Default time: 00:00

### 5.5 Set scheduled startup time

Interface name:

String getTimerOnTime()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Scheduled on-time<br>Format: "HH: mm" |

Code example:

String time =  SdkApi.getInstance().TimerSwitch().getTimerOnTime()

Remarks :

--Default time: 00:00

### 5.6 Set scheduled shutdown time

Interface name:

boolean setTimerOffTime(String offTime)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| offTime | String | Scheduled shutdown time<br>Format: "HH: mm" |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

boolean ret =
SdkApi.getInstance().TimerSwitch().setTimerOffTime("12:01")

Remarks :

-- None

### 5.7 Clear scheduled shutdown time

Interface name:

void clearTimerOffTime()

Parameter :

-- None

Returned value :

-- None

Code example:

SdkApi.getInstance().TimerSwitch().clearTimerOffTime()

Remarks :

--Default time: 00:00

### 5.8 Set scheduled shutdown time

Interface name:

String getTimerOffTime()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Scheduled shutdown time<br>Format: "HH: mm" |

Code example:

```java
String time =
SdkApi.getInstance().TimerSwitch().getTimerOffTime()
```

Remarks :

--Default time: 00:00

### Dog-related API

--Interface call method: SdkApi.getInstance(). Watchdog(). Interface

Function: Watchdog reset. By default, the SDK performs watchdog feeding. When the App performs watchdog feeding, the SDK initiates the App watchdog feeding service and monitors the App watchdog feeding.

### 6.1 Set the dog feeder

Interface name:

boolean setWatchdogOnoff(boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| onoff | boolean | Is the dog feeder an app or a system<br>true: APP<br>false:  system<br>Default value: false |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

SdkApi.getInstance().Watchdog().setWatchdogOnoff(true)

Remarks :

-- None

### 6.2 Get dog feeder

Interface name:

boolean getWatchdogOnoff()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: APP<br>false:  system |

Code example:

SdkApi.getInstance().Watchdog ().get WatchdogOnoff ()

Remarks :

-- None

### 6.3 Set the watchdog timeout

Interface name:

boolean setWatchdogTimeout(int timeout)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| timeout | int | App watchdog timeout<br>Default: 3 minutes<br>Duration: 30 seconds to 5 minutes<br>Unit: seconds |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

Set the dog feeding timeout to 60 seconds
SdkApi.getInstance().Watchdog().setWatchdogTimeout(60)

Remarks :

-- None

### 6.4 Feed the Dog

Interface name:

boolean feedWatchdog()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

SdkApi.getInstance().Watchdog ().feedWatchdog ()

Remarks :

-- None

### System time-related API

--Interface call method: SdkApi.getInstance(). SystemTime(). Interface

### 7.1 Set system time

Interface name:

void setSystemTime(long time)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| time | long | Time difference from 1970<br>Unit: milliseconds |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemTime().setSystemTime(60*1000*1000)

Remarks :

-- None

### 7.2 Get RTC Time

Interface name:

String getRtcTime()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | RTC time<br>Time format: "yyyy/MM/dd hh:mm:ss" |

Code example:

```java
String time =
SdkApi.getInstance().TimerSwitch().getRtcTime ()
Return value example
2025/01/01 12:00:00
```

Remarks :

-- None

### 7.3 Set automatic calibration time

Interface name:

void setAutoTimeOnoff(boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| onoff | boolean | true: NTP enabled<br>false: NTP is disabled |

Returned value :

-- None

Code example:

SdkApi.getInstance().SystemTime().setAutoTimeOnoff(true)

Remarks :

-- None

### 7.4 Get the auto-calibration time switch status

Interface name:

boolean getAutoTimeOnoff()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true: NTP enabled<br>false: NTP is disabled |

Code example:

SdkApi.getInstance().SystemTime().getAutoTimeOnoff()

Remarks :

-- None

### Hardware-related API

--Interface call method: SdkApi.getInstance(). Hardware(). Interface

### 8.1 Get GPIO input level

Interface name:

int getGpioInput(int group, int num)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| group | int | The group of the GPIO;<br>For example, the group value of GPIO0_B3 is GPIO0.<br>See "GPIO Value Definition" |
| num | int | The group ID of the GPIO;<br>For example, GPIO0_B3, where the num value is B3<br>See "GPIO Value Definition" |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | 1: High level<br>0: Low level<br>-1: Read error |

Code example:

```java
//GPIO0_B3
int level = SdkApi.getInstance().Hardware().getGpioInput(
ApiTool.GpioPin.GPIO0, ApiTool.GpioPin.PB3);
```

Remarks :

-- None

### 8.2 Configure GPIO Output Status

Interface name:

boolean setGpioOutput(int group, int num, boolean level)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| group | int | The group of the GPIO;<br>For example, the group value of GPIO0_B3 is GPIO0.<br>See "GPIO Value Definition" |
| num | int | The group ID of the GPIO;<br>For example, GPIO0_B3, where the num value is B3<br>See "GPIO Value Definition" |
| level | boolean | true:  high level<br>false: low level |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | true:  success<br>false:  fail |

Code example:

```java
//GPIO0_B3
int level = SdkApi.getInstance().Hardware().setGpioOutput(
ApiTool.GpioPin.GPIO0, ApiTool.GpioPin.PB3, true);
```

Remarks :

-- None

### 8.3 Open serial port device 1

--Only supports opening the serial port in 8N1 mode

Interface name:

int openSerial(String serialName, int baud)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serialName | String | Serial port device name<br>Example: "/dev/ttyS0" |
| baud | int | Serial port baud rate |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | Serial port device handle |

Code example:

int serial Fd = SdkApi.getInstance().Hardware().openSerial("/dev/ttyS0", 115200);

Remarks :

-- None

### 8.4 Open Serial Port 2

-- universal method

Interface name:

int openSerial(String serialName, int baud,  int dataBits, int parity, int stopBits )

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serialName | String | Serial port device name<br>Example: "/dev/ttyS0" |
| baud | int | Serial port baud rate |
| dataBits | int | data bit<br>Values: 5, 6, 7, 8 |
| parity | int | even-odd check<br>0: NONE<br>1: ODD<br>2: EVEN |
| stopBits | int | stop bit<br>Value: 1, 2 |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | Serial port device handle |

Code example:

int serialFd = SdkApi.getInstance().Hardware().openSerial("/dev/ttyS0", 115200,  8, 0, 1);

Remarks :

-- None

### 8.5 Close serial port

Interface name:

int closeSerial(int serial)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serial | int | Serial port device handle |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | 0: Closed successfully<br>Other values: Failed to disable |

Code example:

int ret = SdkApi.getInstance().Hardware().closeSerial(serial);

Remarks :

-- None

### 8.6 Read serial port data

Interface name:

int readSerial(int serial, byte[] buf, int len)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serial | int | Handle for the serial port |
| buf | byte[] | Save the cached data |
| len | int | Length of data to read |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | Actual data read length |

Code example:

int readLen = SdkApi.getInstance().Hardware().readSerial(serial, buf, len);

Remarks :

-- None

### 8.7 Write serial port data

Interface name:

int writeSerial(int serial, byte[] buf, int len)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serial | int | Handle for the serial port |
| buf | byte[] | Write data cache |
| len | int | Length of data to write |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int | Actual data written length |

Code example:

```java
int writeLen =
SdkApi.getInstance().Hardware().writeSerial(serial, buf, len);
```

Remarks :

-- None

### 8.8 Obtain the serial port power switch

Interface name:

int getSerialPortOnOff(String serialName)

Parameter:

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serialName | String | Serial device name<br>Example: "/dev/ttyS0" |

Returned value:

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | int |   |

Code example:

int ret = SdkApi.getInstance().Hardware().getSerialPortOnOff("/dev/ttyS0");

Remarks :

-- None

### 8.9 Set serial port power switch

Interface name:

boolean setSerialPortOnOff(String serialName, Boolean onOff);

Parameter:

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| serialName | String | Serial device name<br>Example: "/dev/ttyS0" |
| onOff | boolean | True: Open<br>False: Close |

Returned value:

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | boolean | True: Successful<br>False: Failed |

Code example:

boolean ret = SdkApi.getInstance().Hardware().setSerialPortOnOff("/dev/ttyS0", true);

Remarks :

-- None

### 8.10 Query USB Port Number

Method Name:
int getUsbPortNumber(String pid, String vid);

Parameters:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| pid | String | USB device PID (decimal) |
| vid | String | USB device VID (decimal) |

Return Value:

| Parameter Name | Type | Description |
| :--- | :--- | :--- |
| Return Value | int | Port number |

```java
Code Example:
int ret = SdkApi.getInstance().Hardware().getUsbPortNumber("1000", "1001");
```

Remarks :

-- None

### 8.11 Set the USB port power switch

Interface name:

boolean setUsbPortPowerOnoff(int usbPort, boolean onoff)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| usbPort | int | USB port index number |
| onoff | boolean | true:  open<br>false:  close |

Returned value :

-- None

Code example:

SdkApi.getInstance().Hardware().setUsbPortPowerOnoff(port, true);

Remarks :

E USB Serial Number:

3576-U USB Serial Number:

### Other related APIs

--Interface call method: SdkApi.getInstance(). Other(). Interface

### 9.1 Obtain the ICCID number of the SIM card

Interface name:

String getSimICCID()

Parameter :

-- -- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | val: iccid |

Code example:

String iccid = SdkApi.getInstance().Other().getSimICCID();

Remarks :

The SIM card must be properly inserted into the 4G module.

### 9.2 Get IMEI

Interface name:

String getModemIMEI()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | val: imei |

Code example:

String imei = SdkApi.getInstance().Other().getModemIMEI();

Remarks :

The SIM card must be properly inserted into the 4G module.

### 9.3 Execute shell commands

Interface name:

void execShellCommand(String cmd)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| cmd | String | shell order<br>Example: input keyevent 26 |

Returned value :

-- None

Code example:

```java
int ret =
SdkApi.getInstance().Other ().execShellCommand ("input keyevent 26");
```

Remarks :

-- None

### 9.4 Read emmc lifespan

Interface name:

String getEmmcLeftTime()

Parameter :

-- None

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | val:<br>EMMC remaining life<br>Example: 010101 |

Code example:

String leftTime = SdkApi.getInstance().Other().getEmmcLeftTime();

Remarks :

-- None

### CommonEvent-related API

### 10.1 Register a Common listener

Interface name:

int registerCommonListener(int appId, ISdkCommonListener listener)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| appId | int | app Type Id<br>Defined in<br>ApiTool.CommonAppId |
| listener | ScaleListener | Listening listener |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Registration successful<br>ApiTool.API_RES_* |

Code example:

```java
int registerCommonListener(ApiTool.CommonAppId.ID_APP_CUSTOM, new ISdkCommonListener() { });
private ISdkCommonListener commonListener = new ISdkCommonListener.Stub() {
        @Override
        public void CommonListenerNotify(int actionId, String sVal, int[] iArray, int intArrayLen) throws RemoteException {
            if (actionId == ApiTool.CommonEvent.SWITCH_ETH) {
            //Set Ethernet switch result
            }
        }
        @Override
        public IBinder asBinder() {
            return super.asBinder();
        }
};
```

Remarks :

-- None

### 10.2 Unregister the Common listener

Interface name:

int unregisterCommonListener(int appId, ISdkCommonListener listener)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| appId | int | app Type Id<br>Defined in<br>ApiTool.CommonAppId |
| listener | listener | Listening listener |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | Is the cancellation successful?<br>ApiTool.API_RES_* |

Code example:

int unregisterCommonListener(ApiTool.CommonAppId.ID_APP_CUSTOM, listener);

Remarks :

-- None

### 10.3 Set Common Execution Events

Interface name:

int setCommonEvent(int appId, int actionId, String strParam, int[] dataArray, int arrayLen)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| appId | int | app Type Id<br>Defined in<br>ApiTool.CommonAppId |
| actionId | int | event Id<br>Define in ApiTool.ScaleEvent |
| strParam | String | The parameter can be null |
| dataArray | int[] | parameter<br>If not used, initialize it as an empty array. Never assign it to null. |
| arrayLen | int | Data array length |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| Returned value | String | The event result is notified to the listener. For details, see Section 11.4 of the ISDKCommonListener interface documentation. |

Code example:

```java
int setCommonEvent(ApiTool.CommonAppId.ID_APP_CUSTOM,
ApiTool.CommonEvent.EVENT_GET_LN);
```

Remarks :

-- None

### 10.4 ISdkCommonListener Interface Description

Interface name:

ISdkCommonListener

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| actionId | int | event Id<br>Defined in<br>ApiTool.CommonEvent and |
| sVal | String | The interface returns a parameter or the execution result. |
| iArray | int[] | This parameter typically has no special meaning. The reserved return array may assist the intArrayLen parameter in returning the system interfaces operation results. |
| intArrayLen | int | The iArray data length. The received length indicates the success/failure status of the interface callback, including error codes in case of failure. |

Returned value :

-- None

Code example:

-- None

Remarks :

The callback is triggered after executing setCommonEvent, enabling interfaces with return values to be easily handled.

### 10.5 CommonEvent Usage

### 10.6 Interface Call Usage Example

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, actionId, params, dataArray, dataArray.length());

parameter declaration :

--AppId: The process ID of the calling app

--actionId: The unique ID for the function call

--params: Input parameters. If multiple parameters are specified, the json parameter is a JSON string.

--dataArray: Backup parameter. If not used, initialize it as an empty array. Never set it to null.

dataArray.length()

### 10.7 actionId command set

| command name | command code | Explain | remarks |
| :--- | :--- | :--- | :--- |
| EVENT_GET_LT_VERSION | 0x1003 | Get LongXun chip version |   |
| SWITCH_WIFI | 0x9000 | WiFi functional switch |   |
| GET_WIFI_MAC | 0x9001 | Get WiFi MAC |   |
| SWITCH_BLUETOOTH | 0x9010 | Bluetooth function switch |   |
| GET_BLUETOOTH_MAC | 0x9011 | Get Bluetooth MAC |   |
| SWITCH_ETH | 0x9020 | Ethernet function switch |   |
| SWITCH_AP | 0x9030 | AP functional switch |   |
| GET_ETH_STATUS | 0x9040 | Get Ethernet status |   |
| SET_ETH_IP | 0x9050 | Set the Ethernet IP |   |
| GET_ETH_INFO | 0x9060 | Get network information |   |
| UPDATE_SYSTEM_OTA | 0x9091 | OTA upgrade |   |
| UPDATE_SYSTEM_ANIMATION | 0x9096 | Update startup animation | New plan |

#### 10.7.1 WiFi function switch

command name :

SWITCH_WIFI(0x9000)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | on:  open<br>off:  close |

Returned value :

-- None

Code example:

```java
// open WiFi
int[] dataArray = new int[]{ 0};
SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, SWITCH_WIFI, "on", dataArray, dataArray.length());
```

Remarks :

-- None

#### 10.7.2 Get WiFi MAC

command name :

GET_WIFI_MAC(0x9001)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | None |

Returned value :

-- None

Code example:

```java
Get WiFi MAC
int[] dataArray = new int[]{0};
SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, GET_WIFI_MAC, params, dataArray, dataArray.length());
```

Remarks :

-- None

#### 10.7.3 Bluetooth function switch

command name :

SWITCH_BLUETOOTH(0x9010)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | on:  open<br>off:  close |

Returned value :

-- None

Code example:

```java
Open Bluetooth
int[] dataArray = new int[]{0};
SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, GET_BLUETOOTH_MAC , "on", dataArray, dataArray.length());
```

Remarks :

-- None

#### 10.7.4 Ethernet Function Switch

command name :

SWITCH_ETH(0x9020)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | Option 1:<br>on:  open<br>off:  close<br>Option 2:<br>true:  open<br>false:  close<br>Option 3:<br>json class{<br>String ethNum; //Ethernet card name, such as eth0<br>bool flagSwitch; //Function switch<br>} |

Returned value :

-- None

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, SWITCH_ETH, params, dataArray, dataArray.length());

Remarks :

-- None

#### 10.7.5 AP Function Switch

command name :

SWITCH_AP(0x9030)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | See the class structure below<br>When using, convert class to JSON string |

The class structure of the parameters

```java
classApInfo{
bool switchONOff; //Function switch
    String apName; //AP name
bool apIp; // specifies the IP
String password; //AP password
    boolean offWhenNoDeviceConn;
    /*
*Security Type
    *SECURITY_Type_OPEN = 0
    *SECURITY_Type_WPA2_PSK = 1
    *SECURITY_Type_WPA3_SAE_TRANSITION = 2
    *SECURITY_Type_WPA3_SAE = 3
    */
    private int pwType;
    /*
* tape width
    *BAND_2GHZ = 1 << 0
    *BAND_5GHZ = 1 << 1
    *BAND_6GHZ = 1 << 2
    *BAND_60GHZ = 1 << 3
    */
    private int band;
}
```

Returned value :

-- None

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, SWITCH_AP, params, dataArray, dataArray.length());

Remarks :

-- None

#### 10.7.6 Get Ethernet Status

command name :

GET_ETH_STATUS(0x9040)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | Network drive name: iface<br>Example: eth0 |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| sVal | String | true:  open<br>false:  close |

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, GET_ETH_STATUS, params, dataArray, dataArray.length());

Remarks :

-- None

#### 10.7.7 Set Ethernet IP

command name :

SET_ETH_IP(0x9050)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | See the class structure below<br>When using, convert class to JSON string |

The class structure of the parameters

```java
class IpInfo{
String_iface; //Network interface name, such as eth0
bool isStatic; //true: static IP mode false: DHCP
    String ipAddr;
    String mask;
    String gate;
    String dns1;
    String dns2;
}
```

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| sVal | String | 0: Success<br>Other values: Failed |

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, SET_ETH_IP, params, dataArray, dataArray.length());

Remarks :

-- None

#### 10.7.8 Access Network Information

command name :

GET_ETH_INFO(0x9060)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | Network drive name: iface<br>Example: eth0<br>See the Network Drive Type Parameter Table |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| sVal | String | Network drive information |

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, GET_ETH_INFO, params, dataArray, dataArray.length());

Remarks :

-- None

#### 10.7.9 OTA update

command name :

UPDATE_SYSTEM_OTA(0x9091)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | Absolute path of the OTA file<br>The default path is /mnt/sdcard/update.zip |

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| iArray | int[] | iArray[0]:<br>0: Success<br>1: Failed<br>2: The interface is not supported |

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, UPDATE_SYSTEM_OTA, params, dataArray, dataArray.length());

Remarks :

Before upgrading, ensure there is enough local storage space.

#### 10.7.10 Update startup animation

command name :

UPDATE_SYSTEM_ANIMATION(0x9096)

Parameter :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| params | String | See the class structure below<br>When using, convert class to JSON string |

The class structure of the parameters

```java
class LogoInfo{
    int Type;    //0:animation  1:logo  2:wallpaper
String orientation; //horizontal: landscape vertical: portrait
String file path; //Absolute path to the file
    int fileType;        //0:image 1:zip  2:video
int actionType; //0: Import 1: Clear imported files
}
pay attention to :
--logo only supports
  zip and mp4 [h264 or h265] files
  >> Maximum file size 60MB
  The maximum resolution is 4K, subject to the actual capabilities of the board.
```

Returned value :

| Parameter name | Type | Explain |
| :--- | :--- | :--- |
| iArray | int[] | iArray[0]:<br>0: Success<br>1: Failed<br>2: The interface is not supported<br>iArray[1]:<br>Read only when iArray[0] = -1<br>--See the detailed error code in<br>LOGO file import error code table<br>" |

Code example:

SdkApi.getInstance().CommonEvent().setCommonEvent(AppId, UPDATE_SYSTEM_ANIMATION, params, dataArray, dataArray.length());

Remarks :

-- None

### API constant

#### LOGO file import error code table

| variable | Code value | Explain |
| :--- | :--- | :--- |
| FILE_NOT_EXIST | 0x0001 | The file does not exist |
| FILE_NOT_READABLE | 0x0002 | The file is not readable |
| GSON_FORMAT_FAIL | 0x0003 | String escape failed for JSON |
| INVALID_GSON_INFO | 0x0004 | The data does not match the LOGO class definition |
| INVALID_ORIENTATION | 0x0005 | Direction parameter field does not match |
| INVALID_SOURCE_FILE_PATH | 0x0006 | The path parameter field is empty |
| NO_ENOUGH_CACHE_SIZE | 0x0007 | The target directory has insufficient storage space |
| INVALID_FILE_SUFFIX | 0x0008 | Invalid file extension |
| FILE_COPY_FAIL | 0x0009 | File copy failed |
| FILE_SIZE_OVER_LIMIT | 0x000A | File size exceeds the limit |
| ANIMATION_MP4_SUFFIX_INVALID | 0x1020 | MP4 animation files are not supported |
| ANIMATION_MP4_HEADER_INVALID | 0x1021 | The MP4 animation file header format is incorrect |
| ANIMATION_MP4_DECODE_FAIL | 0x1022 | MP4 animation file decoding failed |
| ANIMATION_MP4_FORMAT_UNSUPPORTED | 0x1023 | MP4 animation file verification failed |
| ANIMATION_ZIP_FORMAT_NOT_STORED_ONLY | 0x1024 | The ZIP animation file is not stored in compressed format. |
| ANIMATION_ZIP_DESC_NOT_FOUND | 0x1025 | The description file in the ZIP animation file is not found. |
| ANIMATION_ZIP_DESC_INVALID | 0x1026 | The ZIP animation file contains an invalid description file. |
| ANIMATION_ZIP_PNG_INVALID | 0x1027 | The ZIP animation file contains PNG files. |
| ANIMATION_MP4_RESOLUTION_NOT_SUPPORT | 0x102A | MP4 animation file resolution is not supported |
| ANIMATION_ZIP_DECODE_FAIL | 0x102B | ZIP animation file decoding failed |

#### Network driver Type parameter table

| Variable | Code value | Description |
| :--- | :--- | :--- |
| NET_Type_ETH0 | eth0 | Ethernet Network Card 1 |
| NET_Type_ETH1 | eth1 | Ethernet Network Card 2 |
| NET_Type_WIFI | wifi | WiFi |
| NET_Type_MOBILE | mobile | Mobile network |

#### GPIO Value Definition

Group number is defined as follows

```java
public static final int GPIO0 = 0;
public static final int GPIO1 = 1;
public static final int GPIO2 = 2;
public static final int GPIO3 = 3;
public static final int GPIO4 = 4;
public static final int GPIO5 = 5;
public static final int GPIO6 = 6;
public static final int GPIO7 = 7;
public static final int GPIO8 = 8;
```

The IO sequence number is defined as follows

```java
public static final int PA0 = 0;
public static final int PA1 = 1;
public static final int PA2 = 2;
public static final int PA3 = 3;
public static final int PA4 = 4;
public static final int PA5 = 5;
public static final int PA6 = 6;
public static final int PA7 = 7;
public static final int PB0 = 8;
public static final int PB1 = 9;
public static final int PB2 = 10;
public static final int PB3 = 11;
public static final int PB4 = 12;
public static final int PB5 = 13;
public static final int PB6 = 14;
public static final int PB7 = 15;
public static final int PC0 = 16;
public static final int PC1 = 17;
public static final int PC2 = 18;
public static final int PC3 = 19;
public static final int PC4 = 20;
public static final int PC5 = 21;
public static final int PC6 = 22;
public static final int PC7 = 23;
public static final int PD0 = 24;
public static final int PD1 = 25;
public static final int PD2 = 26;
public static final int PD3 = 27;
public static final int PD4 = 28;
public static final int PD5 = 29;
public static final int PD6 = 30;
public static final int PD7 = 31;
```
