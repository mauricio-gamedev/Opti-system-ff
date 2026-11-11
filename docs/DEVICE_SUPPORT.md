# Device support matrix

## Primary reference device

| Field | Reference |
|---|---|
| Device | Samsung Galaxy A06 |
| Model | SM-A065M |
| Android | Android 16 |
| API level | 36 |
| Architecture | arm64-v8a |
| Root | Not required |
| Priority | Stability and consistent gameplay |

This is a development target, not a guarantee that every OEM feature is exposed.

## Compatibility rules
- Gate APIs by Android API level and handle unavailable values.
- Do not infer chipset-specific controls from the marketing device name.
- Separate Samsung One UI behavior from AOSP behavior in test notes.
- Validate package identifiers against installed official builds before release.
- Do not recommend Pixel-only, rooted-only or unsupported OEM commands as universal actions.
- Record exact firmware/build and test conditions for device-specific claims.

## Current implementation
The diagnostic snapshot uses public Android framework APIs for:
- System-wide total/available memory and Android's low-memory state.
- Device low-RAM classification.
- Data-partition storage.
- Android thermal status.
- Power-save mode and battery percentage where reported.
- Current display refresh rate and supported display modes.
- SoC manufacturer/model and supported CPU ABIs when available from Build fields.

The app does not inspect Free Fire process memory, CPU/GPU utilization, actual in-game FPS, frame time or touch latency. The display refresh rate must not be interpreted as the game's FPS.

## Galaxy A06 validation
The user confirmed that Foundation 0.1.0 installed and opened on the SM-A065M. Device-specific values collected by Device Intelligence still need to be reviewed from a fresh 0.2.0 build on the phone; no chipset or display capability is hardcoded as an A06 fact.
