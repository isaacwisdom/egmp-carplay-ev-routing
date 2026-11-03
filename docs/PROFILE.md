# Vehicle profiles

One signed mod ZIP contains both supported vehicle profiles as `profiles/neev23.json` and `profiles/cvev.json`. Each UTF-8 JSON file contains vehicle metadata, battery capacity, connectors and both [contracts](CONTRACTS.md).

HMG-API’s vehicle-specific `parts` declarations select one profile when **Apply changes** runs, and add that file at `/system/etc/carplay.ev-routing/profile.json`. This is an HMG-API-managed file: removal restores its prior state. The routing code reads only this fixed path during startup; it does not select a model during status updates. The signed bundle retains both source profiles so the same ZIP works on both vehicles.

`vehicle.display_name` is used directly. `default_display_name` is reserved for a future reset feature; renaming is not implemented. Model IDs are decimal JSON integers identifying the charging and consumption models, not the car.

Both bundled profiles use 77400 Wh. EV6 numerical values are copied from Ioniq 5, unvalidated and likely inaccurate. Product matching does not identify battery trim.

Runtime checks schema, name, model roles, connectors and array lengths. Auxiliary power starts from the profile default, then uses live or retained values in transmitted consumption JSON. The profile file is not rewritten. Profile changes require rebuilding and signing the package; automatic downloads are not implemented.
