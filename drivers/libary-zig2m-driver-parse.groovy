// Version 0.9.0

library (
  author: "RMoRobert",
  category: "internal",
  description: "Internal use by Zigbee2MQTT Connect virtaul devices; includes default parse methods",
  name: "zigbee2MQTTComponentDriverLibrary_Parse",
  namespace: "RMoRobert",
  documentationLink: "comingSoon"
)

void parse(String description) { log.warn "parse(String description) not implemented" }

void parse(List<Map> description) {
   if (enableDebug) log.debug ("parse($description)")
   description.each {
      if (it.name in parsableAttributes) {
         if (enableDesc && device.currentValue(it.name) != it.value) {
            if (it.descriptionText != null) log.info it.descriptionText
            else log.info "${device.displayName} ${it.name} is ${it.value}"
         }
         sendEvent(it)
      }
   }
}