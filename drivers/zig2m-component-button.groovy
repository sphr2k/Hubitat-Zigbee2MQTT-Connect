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
 *  Last modified: 2021-11-16
 *
 *  Changelog:
 *  v0.3    - (Beta) Added battery parsing (if device supports)
 *  v0.2    - (Beta) Added additional vendor/model combinations for events
 *  v0.1    - (Beta) Initial Public Release
 */ 

import groovy.transform.Field
import java.util.concurrent.ConcurrentHashMap

// used for some "release" events (check last push/hold)
@Field static final ConcurrentHashMap<Long,Integer> lastButtonNumber = [:]

// Doesn't include button attributes, which are handled separately:
@Field static final List<String> parsableAttributes = ["battery"]

// [manufacturer: [model_id: ... ]]
@Field static final Map<String,Map<String,Map<String,Map<String,Integer>>>> buttonEventMap = [
   "AduroSmart": [
      "81825": [
         "on": [pushed: 1],
         "off": [pushed: 4],
         "up": [pushed: 2],
         "down": [pushed: 3]
      ]
   ],
   "IKEA": [
      "E1524": [ // 5-button "steering wheel"
         "toggle": [pushed: 1],
         "arrow_left_click": [pushed: 2],
         "arrow_left_hold": [held: 2],
         "arrow_left_release": [released: 2],
         "arrow_right_click": [pushed: 3],
         "arrow_right_hold": [held: 3],
         "arrow_right_release": [released: 3],
         "brightness_up_click": [pushed: 4],
         "brightness_up_hold": [held: 4],
         "brightness_up_release": [released: 4],
         "brightness_down_click": [pushed: 4],
         "brightness_down_hold": [held: 4],
         "brightness_down_release": [released: 4]
      ],
      "E1810": [ // 5-button "steering wheel"
         "toggle": [pushed: 1],
         "arrow_left_click": [pushed: 2],
         "arrow_left_hold": [held: 2],
         "arrow_left_release": [released: 2],
         "arrow_right_click": [pushed: 3],
         "arrow_right_hold": [held: 3],
         "arrow_right_release": [released: 3],
         "brightness_up_click": [pushed: 4],
         "brightness_up_hold": [held: 4],
         "brightness_up_release": [released: 4],
         "brightness_down_click": [pushed: 4],
         "brightness_down_hold": [held: 4],
         "brightness_down_release": [released: 4]
      ],
      "E2001": [ // STYRBAR remote control N2
         "toggle": [pushed: 1],
         "arrow_left_click": [pushed: 2],
         "arrow_left_hold": [held: 2],
         "arrow_left_release": [released: 2],
         "arrow_right_click": [pushed: 3],
         "arrow_right_hold": [held: 3],
         "arrow_right_release": [released: 3],
         "brightness_up_click": [pushed: 4],
         "brightness_up_hold": [held: 4],
         "brightness_up_release": [released: 4],
         "brightness_down_click": [pushed: 4],
         "brightness_down_hold": [held: 4],
         "brightness_down_release": [released: 4]
      ],
      "E2002": [ // STYRBAR remote control N2
         "toggle": [pushed: 1],
         "arrow_left_click": [pushed: 2],
         "arrow_left_hold": [held: 2],
         "arrow_left_release": [released: 2],
         "arrow_right_click": [pushed: 3],
         "arrow_right_hold": [held: 3],
         "arrow_right_release": [released: 3],
         "brightness_up_click": [pushed: 4],
         "brightness_up_hold": [held: 4],
         "brightness_up_release": [released: 4],
         "brightness_down_click": [pushed: 4],
         "brightness_down_hold": [held: 4],
         "brightness_down_release": [released: 4]
      ],
      "ICTC-G-1": [ // Tradfri wireless dimmer (round)
         "brightness_move_to_level": [pushed: 1],
         "brightness_move_up": [pushed: 2],
         "brightness_move_down": [pushed: 3],
         "brightness_stop": [released: 0]
      ],
      "E1743": [ // on/off switch
         "on": [pushed: 1],
         "off": [pushed: 2],
         "brightness_move_up": [held: 1],
         "brightness_move_down": [held: 2],
         "brightness_stop": [released: 0],
      ],
      "E1812": [ // shortcut button
         "on": [pushed: 1],
         "brightness_move_up": [held: 1],
         "brightness_stop": [released: 1],
      ],
      "E1766": [ // open/close remote (blinds?)
         "open": [pushed: 1],
         "close": [pushed: 2],
         "stop": [released: 0],
      ],
      "E1841": [ // open/close water remote
         "on": [pushed: 1],
         "off": [pushed: 2],
      ],
      "E1744": [ // Symfonisk sound controller
         "toggle": [pushed: 1],
         "brightness_step_up": [pushed: 2],
         "brightness_step_down": [pushed: 3],
         "brightness_move_up": [held: 2],
         "brightness_move_down": [held: 3],
         "brightness_stop": [released: 0],
      ]
   ],
]


metadata {
   definition (name: "Zigbee2MQTT Component Button", namespace: "RMoRobert", author: "Robert Morris",
               importUrl: "https://raw.githubusercontent.com/RMoRobert/HASSConnect/main/drivers/zig2m-button.groovy") {
      capability "Sensor"
      capability "Battery"
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
   if (rawBtnAction?.value) {
      Map<String,Integer> btnEvt = buttonEventMap.get(getDataValue("manufacturer"))?.get(getDataValue("model_id"))?.get(rawBtnAction.value)
      if (!btnEvt) {
         "no known button events; skipping"
      }
      else {
         String evtName = btnEvt.keySet()[0]
         Integer btnNum = btnEvt[btnEvt.keySet()[0]]
         if (btnNum == 0) {
            btnNum = lastButtonNumber[device.idAsLong] ?: 1
         }
         doSendButtonEvent(evtName, btnNum)
         lastButtonNumber[device.idAsLong] = btnNum
         evtName = null; btnNum = null
      }
   }
   else {
      // can uncomment if need more detail, but this will also appear for non-button events like battery, which are handled below:
      //if (enableDebug) log.warn "ignoring unknown button event: $actionData"
   }
   // Handle other possibilities, like battery:
   parsableAttributes.each { String attr ->
      if (actionData["$attr"] != null) {
         doSendEvent(attr, actionData["$attr"])
      }
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