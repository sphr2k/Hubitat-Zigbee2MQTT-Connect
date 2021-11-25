/**
 * ====================  Generic Component Motion/Temperature Driver ==========================
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * =======================================================================================
 *
 *  Changelog:
 *  2021-11-24 - Initial release
 */

import groovy.transform.Field

@Field static final List<String> parsableAttributes = ["battery", "motion", "temperature", "humidity"]
@Field static final Integer debugAutoDisableMinutes = 30

metadata {
   definition(name: "Zigbee2MQTT Generic Device", namespace: "RMoRobert", author: "Robert Morris", component: true) {
      capability "Sensor"

      command "publish", [[name: "topic", type: "STRING", description: "MQTT topic (base topic and device friendly name automatically prepended)"],
                         [name: "payload", type: "STRING", description: "MQTT payload"]]

      command "logDefinition"

   }
   preferences {
      input name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true
      input name: "enableDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true
   }
}

// Note: prepends base topic to 'topic' parameter
void publish(String topic=null, String payload=null) {
   if (enableDebug) log.debug "publish(topic=$topic, payload=$payload)"
   parent.componentPublish(this.device, topic, payload)
}

void logDefinition() {
   if (enableDebug) log.debug "logDefinition()"
   parent.getDefinitionForDevice(this.device)
}

void parse(String description) { log.warn "parse(String description) not implemented" }

void parse(List<Map> description) {
   if (enableDebug) log.debug ("parse($description)")
   description.each {
      if (enableDesc && device.currentValue(it.name) != it.value) {
         if (it.descriptionText != null) log.info it.descriptionText
         else log.info "${device.displayName} ${it.name} is ${it.value}"
      }
      // won't really do much, but at least could create visible event in UI for easier troubleshooting, etc:
      sendEvent(it)
   }
}

#include RMoRobert.zigbee2MQTTComponentDriverLibrary_Common