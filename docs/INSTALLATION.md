# Installation through HMG-API

Requires official HMG-API **1.0.0-beta.6 or newer**, a checked car map, and its enabled/applied **Wireless CarPlay + Android Auto** mod. HMG-API itself may be installed through NEMESIS. Confirm ordinary CarPlay works first. Uninstall older Leafia-based EV extensions and Leafia before migrating.

## USB

1. Download [egmp-carplay-ev-routing-v0.4.0.zip](https://github.com/isaacwisdom/egmp-carplay-ev-routing/releases/download/v0.4.0/egmp-carplay-ev-routing-v0.4.0.zip). This single signed ZIP supports the listed Ioniq 5 and EV6 configurations. EV6 values are copied and unvalidated; both profiles use 77.4 kWh.
2. Copy the ZIP unchanged into `HMGAPI/updates/` on a USB stick. Do not unzip it. HMG-API chooses the applicable profile at Apply using the HU’s model/region/firmware; the routing code reads one active file at `/system/etc/carplay.ev-routing/profile.json`.
3. Open HMG-API > **Check USB updates**, select the package and install. Enable it and review **Apply changes**. Do not use **Install anyway** for an unsupported car/firmware.
4. After successful apply, remove the stick and fully reboot.
5. Complete the [required iPhone re-pairing](#re-pair-your-iphone-required) below, then connect CarPlay and open Apple Maps.

These follow the upstream [mod installation directions](https://github.com/VysseCodes/HMG-API/wiki/Installing-and-removing-mods). The wireless mod is also imported from a signed upstream ZIP in `HMGAPI/updates/`; the HMG-API NEMESIS installer does not bundle it.

Our author key is not in beta6’s trusted-author list, so first import may show an unfamiliar-author warning. Expected UUID: `b45fb2d9-e6e8-83a2-bce9-cd5c40d2232d`. Updates require the same signing key. The mod ID is `egmp-carplay-ev-routing`; the download, signing key and code are shared by both vehicles.

## Computer alternative

From the source/release directory, for either supported vehicle:

```sh
adb push dist/egmp-carplay-ev-routing-v0.4.0.zip /data/local/tmp/carplay-ev-routing.zip
adb shell hmgapid engine install /data/local/tmp/carplay-ev-routing.zip --uuid b45fb2d9-e6e8-83a2-bce9-cd5c40d2232d
adb shell hmgapid engine plan
```

If the plan has no refused hooks or unexpected changes:

```sh
adb shell hmgapid engine apply
adb reboot
```

## Re-pair your iPhone (required)

After installing and rebooting, **manually delete the existing pairing and pair the iPhone to the HU again**. This applies to both USB and computer installation; simply reconnecting the existing pairing is not enough.

1. On the HU, delete the iPhone from the Bluetooth/phone-projection paired-device list.
2. On the iPhone, forget the HU in both Bluetooth and CarPlay settings.
3. Start a new pairing with the HU and accept the Bluetooth and CarPlay prompts.
4. Connect CarPlay and open Apple Maps to set up EV routing.

## Update, disable and remove

Import the newer signed ZIP in HMG-API and apply. Existing signed local HMG test versions can be updated using the same author key. Profile selection is automatic on each apply. HMG-API owns and removes both our helper library and active profile.

Remove in HMG-API, then apply changes and reboot; the wireless mod stays installed. Temporarily disabling also needs apply and a full reboot, because the native recipe is applied in process memory. Re-enabling requires a reboot to register the status listener again. Runtime native-hash and required-point checks keep unknown layouts inactive.

## Optional NEMESIS wrapper

`dist/testing/` retains `egmp-carplay-ev-routing-v0.4.0-nemesis-package.zip` and `-nemesis-usb.zip` for wrapper testing. That wrapper owns the inner mod’s installation/removal; do not mix management routes. If already installed as NEMESIS package `carplay.ev-routing`, uninstall that wrapper through NEMESIS, reboot, then install the native mod above. A direct HMG-API test mod without the wrapper can be updated in place.

Interrupted wrapper recovery remains available as `sh /data/nemesis/apps/carplay.ev-routing/scripts/recover.sh` through a privileged HMG-API shell. Keep its backup until recovered. NEMESIS intentionally leaves `/system` writable on this HU; that alone is not a failure.
