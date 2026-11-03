# Optional NEMESIS testing package

`nemesis/` contains the 1.1 descriptor, lifecycle scripts and a Launch action opening HMG-API. `depends=hmgapi@>=0.99.6` requires the official HMG-API beta6 NEMESIS package or newer. The preinstall hook also verifies the running daemon version and refuses beta5/custom cores.

The builder adds the same signed multi-vehicle HMG-API mod ZIP and a compatibility table. HMG-API selects the active profile at Apply. NEMESIS installs our wrapper; it delegates mod import, apply, restore and removal to HMG-API. The wrapper never rebuilds/replaces an OEM APK, ODEX or library on disk. HMG-API owns our helper library and in-memory OEM recipes.

Updates retain the existing mod for replacement. Failed import/apply restores the previous signed mod/state; interrupted operations leave an explicit recovery transaction. Uninstall requires resolving incomplete transactions first. The USB archive's generated root descriptor installs the same NEMESIS package under packages/.

The primary release is now the single signed native HMG-API ZIP. These wrappers are generated only under `dist/testing/`; do not upload them as the primary release or mix wrapper/native management on one HU.
