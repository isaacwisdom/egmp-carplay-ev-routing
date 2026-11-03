# Live vehicle data

While subscribed, read SOC/range from HSCAN and auxiliary power from the HEV service every five seconds. Auxiliary watts = 20 × climate + 10 × electronics + 20 × battery compressor + 100 × battery heater. Reject invalid/sentinel readings; retain the last complete valid auxiliary total, using the profile default before the first valid read. Source CAN-cache freshness is not independently established.

HSCAN failures omit live battery/range fields; auxiliary reads remain independent. A successful SOC/range sample expires after 15 seconds within a polling operation. Current Wh and full-charge range are estimates from SOC and configured capacity.

Every status currently includes both JSON contracts. Only auxiliary power changes inside the consumption contract; the installed profile is not rewritten. The five-second interval and repeated-contract policy are implementation choices, not established Apple requirements. Route recalculation at that cadence is under investigation.
