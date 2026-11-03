# v0.4.0 — Experimental

First public release of E-GMP CarPlay EV Routing for Gen5W, using official HMG-API **1.0.0-beta.6 or newer** and its Wireless CarPlay + Android Auto mod.

One signed ZIP automatically selects the active vehicle profile at Apply. Live SOC, range and auxiliary power feed Apple Maps EV routing, with bundled charging/consumption contracts and rotating runtime logs.

- **Ioniq 5:** NEEV23 USA 260507, 77.4 kWh; final package installed and confirmed working by its owner.
- **EV6:** CVEV USA 260507; untested, with copied Ioniq 5 numerical values that are unvalidated and likely inaccurate.
- Other firmware/configurations, including 260903, are unsupported by this release.

## Install

Install HMG-API beta6+ and its Wireless CarPlay + Android Auto mod first; confirm CarPlay works. Put `egmp-carplay-ev-routing-v0.4.0.zip` **unchanged** in `HMGAPI/updates/` on USB. In HMG-API, use **Check USB updates**, install/enable, review and **Apply changes**. Remove USB and fully reboot. **Manually delete the existing iPhone–HU pairing on both devices, then pair again and allow CarPlay.** Reconnecting the existing pairing is not enough.

See [detailed installation, update and removal instructions](https://github.com/isaacwisdom/egmp-carplay-ev-routing/blob/main/docs/INSTALLATION.md). The mod is signed with author UUID `b45fb2d9-e6e8-83a2-bce9-cd5c40d2232d`; beta6 may show an unfamiliar-author warning. No OEM binaries are distributed.

## Known limits

Maps may recalculate routes at the five-second status cadence; this remains under investigation. Energy models are uncalibrated, and long-drive reliability needs more testing. Both source profiles are bundled, but only one active profile is applied automatically. The source ZIP is for developers; install only the mod ZIP.

Special credit to [augard/evmaps](https://github.com/augard/evmaps) for charging/consumption reference material, and to HMG-API, NEMESIS and egmp-aa-ev-feat.
