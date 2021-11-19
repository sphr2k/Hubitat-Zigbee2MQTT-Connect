// Version 0.9.0

library (
  author: "RMoRobert",
  category: "internal",
  description: "Internal use by Zigbee2MQTT Connect virtaul devices; includes common methods",
  name: "zigbee2MQTTComponentDriverLibrary_Common",
  namespace: "RMoRobert",
  documentationLink: "comingSoon"
)

void installed() {
   log.debug "installed()"
   device.updateSetting("enableDesc", [type:"bool", value:true])
   refresh()
}

void updated() {
   log.debug "updated()"
   log.warn "description logging is: ${enableDesc == true}"
}

void initialize() {
   log.debug "initialize()"
   if (enableDebug) {
      log.debug "Debug logging will be automatically disabled in ${debugAutoDisableMinutes} minutes"
      runIn(debugAutoDisableMinutes*60, "debugOff")
   }
}

void debugOff() {
   log.warn "Disabling debug logging"
   device.updateSetting("enableDebug", [value:false, type:"bool"])
}

void refresh() {
   if (enableDebug) log.debug "refresh()"
   parent?.componentRefresh(this.device)
}
