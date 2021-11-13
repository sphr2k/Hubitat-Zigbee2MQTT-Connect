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
 *  2021-10-30 - Initial release
 */

import groovy.transform.Field

@Field static final List<String> parsableAttributes = ["battery", "motion", "temperature", "humidity"]
@Field static final Integer debugAutoDisableMinutes = 30

metadata {
   definition(name: "Generic Component Motion/Temperature/Humidity Sensor", namespace: "RMoRobert", author: "Robert Morris", component: true) {
      capability "Sensor"
      capability "Battery"
      capability "MotionSensor"
      capability "TemperatureMeasurement"
      capability "RelativeHumidityMeasurement"
      capability "Refresh"
   }
   preferences {
      input name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true
      input name: "enableDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true
   }
}

void installed() {
   log.debug "installed()"
   device.updateSetting("enableDesc", [type:"bool",value:true])
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

void refresh() {
   if (enableDebug) log.debug "refresh()"
   parent?.componentRefresh(this.device)
}
