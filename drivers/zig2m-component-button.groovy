/**
 * ============================  Zigbee2MQTT Button (Driver) =============================
 *
 *  Copyright 2021 Robert Morris
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
 *  Last modified: 2021-11-12
 *
 *  Changelog:
 *  v0.1    - (Beta) Initial Public Release
 */ 

import groovy.transform.Field
import java.util.concurrent.ConcurrentHashMap

// used for some "release" events (check last push/hold)
@Field static final ConcurrentHashMap<Long,Integer> lastButtonNumber = [:]


// [manufacturer: [model_id: ... ]]
@Field static final Map<String,Map<String,Map<String,Map<String,Integer>>>> buttonEventMap = [
   "ADUROLIGHT": [
      "Adurolight_NCC": [
         "on": [pushed: 1],
         "off": [pushed: 4],
         "up": [pushed: 2],
         "down": [pushed: 3]
      ]
   ]
]


metadata {
   definition (name: "Zigbee2MQTT Component Button", namespace: "RMoRobert", author: "Robert Morris",
               importUrl: "https://raw.githubusercontent.com/RMoRobert/HASSConnect/main/drivers/zig2m-button.groovy") {
      capability "Sensor"
      capability "PushableButton"
      capability "HoldableButton"
      capability "ReleasableButton"

      command "setNumberOfButtons", ["NUMBER"]
      // Should not be needed during normal use:
      //command "setVendorAndModel". ["STRING", "STRING"]
   }
   
   preferences() {
      input name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true
      input name: "enableDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true
   }
}

void installed() {
   log.debug "Installed..."
   initialize()
}

void updated() {
   log.debug "Updated..."
   initialize()
}

void initialize() {
   if (enableDebug) log.debug "Initializing..."
   if (enableDebug) {
      Integer disableTime = 1800
      log.debug "Debug logging will be automatically disabled in ${disableTime/60} minutes"
      runIn(disableTime, debugOff)
   }
}

void debugOff() {
   log.warn "Disabling debug logging"
   device.updateSetting("enableDebug", [value:"false", type:"bool"])
}

void setNumberOfButtons(Number number) {
   if (enableDebug) log.debug "setNumberOfButtons($number)"
   doSendEvent("numberOfButtons", number)
}

void parse(description) {
   log.warn "not implemented: parse(Object $description)"
}

void parse(List<Map> actionData) {
   if (enableDebug) log.debug "parse(List $actionData)"
   Map rawBtnAction = actionData.find { it.name == "action" }
   if (!rawBtnAction.value) return
   if (rawBtnAction) {
      Map<String,Integer> btnEvt = buttonEventMap.get(getDataValue("manufacturer"))?.get(getDataValue("model_id"))?.get(rawBtnAction.value)
      if (!btnEvt) {
         "no known button events; skipping"
      }
      else {
         doSendButtonEvent(btnEvt.keySet()[0], btnEvt[btnEvt.keySet()[0]])
      }
   }
   else {
         if (enableDebug) log.warn "ignoring unknown button event: $actionData"
   }
}

void push(Integer buttonNumber) {
   doSendEvent("pushed", buttonNumber)
}

void hold(Integer buttonNumber) {
   doSendEvent("held", buttonNumber)
}

void release(Integer buttonNumber) {
   doSendEvent("released", buttonNumber)
}

void setVendorAndModel(String vendor, String model) {
   device.updateDataValue("vendor", vendor)
   device.updateDataValue("model", model)
}

private void doSendButtonEvent(String eventName, Integer eventValue, Boolean forceStateChange = true) {
   //if (enableDebug) log.debug ("Creating event for $eventName...")
   String descriptionText = "${device.displayName} button ${eventValue} is ${eventName}"
   if (settings.enableDesc) log.info descriptionText
   sendEvent(name: eventName, value: eventValue, descriptionText: descriptionText, isStateChange: forceStateChange)
}

private void doSendEvent(String eventName, eventValue) {
   //if (enableDebug) log.debug ("Creating event for $eventName...")
   String descriptionText = "${device.displayName} ${eventName} is ${eventValue}"
   if (settings.enableDesc) log.info descriptionText
   sendEvent(name: eventName, value: eventValue, descriptionText: descriptionText)
}