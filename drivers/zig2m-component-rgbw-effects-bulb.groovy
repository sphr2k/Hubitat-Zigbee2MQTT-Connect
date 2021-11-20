/**
 * ========================  Zigbee2MQTT Component RGBW Bulb  =============================
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
 *  2021-11-19 Initial release
 */

import groovy.transform.Field

@Field static final List<String> parsableAttributes = ["switch", "level", "colorMode", "colorName",
   "colorTemperature", "hue", "saturation", "effect"]
@Field static final Integer debugAutoDisableMinutes = 30

metadata {
   definition(name: "Zigbee2MQTT Component RGBW Effects Bulb", namespace: "RMoRobert", author: "Robert Morris", component: true) {
      capability "Actuator"
      capability "ColorControl"
      capability "ColorTemperature"
      capability "Refresh"
      capability "Switch"
      capability "SwitchLevel"
      //capability "LevelPreset"  // haven't found Z2M bulb that supports yet?
      capability "ChangeLevel"
      capability "Light"
      capability "ColorMode"
      //capability "Flash"  // TODO if possible (use effect if supported?)
      capability "LightEffects"
   }

   preferences {
      input name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true
      input name: "enableDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true
   }
}

#include RMoRobert.zigbee2MQTTComponentDriverLibrary_Common
#include RMoRobert.zigbee2MQTTComponentDriverLibrary_Parse

void on() {
   if (enableDebug) log.debug "on()"
   parent.componentOn(this.device)
}

void off() {
   if (enableDebug) log.debug "off()"
   parent.componentOff(this.device)
}


void setLevel(Number level) {
   if (enableDebug) log.debug "setLevel($level)"
   parent.componentSetLevel(this.device,level)
}

void setLevel(Number level, Number ramp) {
   if (enableDebug) log.debug "setLevel($level, $ramp)"
   parent.componentSetLevel(this.device, level, ramp)
}

void startLevelChange(String direction) {
   if (enableDebug) log.debug "startLevelChange($direction)"
   parent.componentStartLevelChange(this.device, direction)
}

void stopLevelChange() {
   if (enableDebug) log.debug "stopLevelChange()"
   parent.componentStopLevelChange(this.device)
}

void setColorTemperature(Number colorTemperature, Number level=null, Number ramp=null) {
   if (enableDebug) log.debug "setColorTemperature($colorTemperature, $level, $ramp)"
   parent.componentSetColorTemperature(this.device, colorTemperature, level, ramp)
}

void setColor(Map color) {
   if (enableDebug) log.debug "setColor($color)"
   parent.componentSetColor(this.device, color)
}

void setHue(Number hue) {
   if (enableDebug) log.debug "setHue($hue)"
   parent.componentSetColor(this.device, hue)
}

void setSaturation(Number saturation) {
   if (enableDebug) log.debug "setSaturation($saturation)"
   parent.componentSetColor(this.device, saturation)
}

// Hubitat uses effect number as standard, but String is easier to work with
// in Z2M, so custom bulb driver implements both.
// Use the String variant only when calling parent for now!

void setEffect(Number effectNum) {
   if (enableDebug) log.debug "setEffect(Number $effectNum)"
   //parent.componentSetEffect(this.device, effectNum)
   String effect = state.lightEffects?.get("$effectNum" as String)
   state.crntEffectId = effectNum
   if (effect != null) setEffect(effect)
}

void setEffect(String effectName) {
   if (enableDebug) log.debug "setEffect(String $effectName)"
   def effect = state.lightEffects?.find { it.value == effectName }
   log.trace "effect = $effect"
   Integer id =  Integer.parseInt(effect?.key ?: 0)
   state.crntEffectId = id
   //setEffect(id)
   parent.componentSetEffect(this.device, effectName)
}

void setNextEffect() {
   if (enableDebug) log.debug "setNextEffect()"
   Integer currentEffect = (state.crntEffectId != null) ? state.crntEffectId : -1
   currentEffect++
   if (currentEffect > maxEffectNumber) currentEffect = 0
   setEffect(currentEffect)
}

void setPreviousEffect() {
   if (enableDebug) log.debug "setPreviousEffect()"
   Integer currentEffect = state.crntEffectId ?: 0
   currentEffect--
   if (currentEffect < 0) currentEffect = maxEffectNumber
   setEffect(currentEffect)
}

Integer getMaxEffectNumber() {
   if (state.lightEffects?.size() > 0) {
      return state.lightEffects?.size() - 1
   }
   else {
      return 0
   }
}

// Non-standard commands used by parent to generate lightEffects attribute in driver:
void setLightEffects(Map effectMap) {
   if (enableDebug != false) log.debug "setLightEffects(Map $effectMap)"
   groovy.json.JsonBuilder le = new groovy.json.JsonBuilder(effectMap)
   state.lightEffects = effectMap
   sendEvent(name: "lightEffects", value: le)
}

void setLightEffects(List effectList) {
   if (enableDebug != false) log.debug "setLightEffects(List $effectList)"
   Map<String,String> effectMap = [:]
   effectList.eachWithIndex { String effectName, Integer i ->
      effectMap << ["$i": effectName]
   }
   setLightEffects(effectMap)
}
