# 0.4.2 — experimental

- Floor live battery state of charge to a whole percent before sending it to Apple Maps, matching Hyundai's EV app treatment of the half-percent `DISPLAY_SOC` signal.
- Add regression coverage for half-percent SOC values and invalid inputs.
- Native patch, energy models, auxiliary-power behavior and five-second update policy are unchanged.

# 0.4.1 — experimental

- Accept the 2022 US Ioniq 5 (`NEEV.USA`, firmware `260507`) with the existing 77.4 kWh profile; on-car validation pending.
- Include both detected native-library MD5 hashes in rejection logs; exact-hash guards remain unchanged.
- Native patch, energy models and five-second update policy are unchanged.

# 0.4.0 — experimental first release

- One signed native HMG-API mod download with automatic profile selection at Apply, requiring official beta6 or newer.
- Live SOC/range/auxiliary power and bundled vehicle contracts.
- Exact native-hash gates, HMG-API map checks, signed updates, and rotating logs. Optional NEMESIS wrappers remain available for testing.
- Ioniq 5 integration tested; EV6 profile copied and unvalidated.
- Known issue: route recalculation at the five-second status cadence remains under investigation.
