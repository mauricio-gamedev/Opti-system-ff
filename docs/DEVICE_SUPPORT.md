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
The first diagnostic snapshot uses public Android framework APIs for system-wide available RAM, data-partition storage and thermal status where supported. It does not inspect Free Fire process memory, CPU/GPU utilization, actual in-game FPS or touch latency.
