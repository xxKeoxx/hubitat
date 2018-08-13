/**
 *  AT&T Digital Life System
 *
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
 *
 *  Version history
 *
 *	v0.1.08.12.18 - Ported to hubitat. commented out smartThings specific items.
 *  v0.1.04.06.16 - Removed Location Mode capability - might interfere with Rule Machine
 *  v0.1.03.24.16 - Published device as switch to allow easy manipulation via rules - on() sets it to away
 *                  and off() sets it to home. Also published commands setMode(mode), home(), away(), stay().
 *					You can now also use it as a "Switch Level" by setting the level to 0 for home, 1 for stay
 *                  and 2 or more for away.
 *  v0.1.03.22.16 - Initial beta release
 *
 */
metadata {
	definition (name: "AT&T Digital Life System", namespace: "Keo", author: "Joe Rosiak", oauth: true) {
		capability "Actuator"
        capability "Refresh"
        capability "Configuration"
        capability "Switch"
        capability "Switch Level"
        attribute "digital-life-mode", "enum", ["Home", "Stay", "Instant", "Away"]
        attribute "id", "string"
        attribute "module", "string"       
        attribute "type", "string"
        attribute "system-status", "enum", ["Home", "Home Bypass", "Home ExitDelay", "Stay", "Stay AlarmPending", "Away", "Away AlarmPending", "Instant", "Instant AlarmPending"]
        attribute "arm-state", "string"
        attribute "alarm-mode", "string"
        attribute "bypassed-device-list", "string"
        attribute "alarmed-device-list", "string"
        attribute "armed-stay-device-list", "string"
        attribute "armed-away-device-list", "string"
        attribute "armed-instant-device-list", "string"
        attribute "alarm-timeout", "number"
        attribute "entry-delay", "number"
        attribute "exit-delay", "number"
        attribute "abort-timeout", "number"
        
        command "home"
        command "stay"
        command "away"
        command "setMode"
        
        command "describeAttributes"
	}
/*
    simulator {
	}


	// UI tile definitions
	tiles(scale: 2) {
		standardTile("status", "device.system-status", width: 3, height: 3) {
            state "Home", label: 'Home', icon: "st.locks.lock.unlocked", backgroundColor: "#79b821", canChangeIcon: true, canChangeBackground: true
            state "Home Bypass", label: 'Home', icon: "st.locks.lock.locked", backgroundColor: "#ffa81e", canChangeIcon: true, canChangeBackground: true
            state "Home ExitDelay", label: 'Home', icon: "st.locks.lock.locked", backgroundColor: "#ffa81e", canChangeIcon: true, canChangeBackground: true
            state "Stay", label: 'Stay', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff", canChangeIcon: true, canChangeBackground: true
            state "Stay AlarmPending", label: 'Stay', icon: "st.locks.lock.locked", backgroundColor: "#ff381e", canChangeIcon: true, canChangeBackground: true
            state "Away", label: 'Away', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff", canChangeIcon: true, canChangeBackground: true
            state "Away AlarmPending", label: 'Away', icon: "st.locks.lock.locked", backgroundColor: "#ff381e", canChangeIcon: true, canChangeBackground: true
            state "Instant", label: 'Instant', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff", canChangeIcon: true, canChangeBackground: true
            state "Instant AlarmPending", label: 'Instant', icon: "st.locks.lock.locked", backgroundColor: "#ff381e", canChangeIcon: true, canChangeBackground: true
        }

		multiAttributeTile(name:"multi", type:"generic", width:6, height:4) {
			tileAttribute("device.system-status", key: "PRIMARY_CONTROL") {
            	attributeState "Home", label: 'Home', icon: "st.locks.lock.unlocked", backgroundColor: "#79b821"
            	attributeState "Home Bypass", label: 'Home', icon: "st.locks.lock.locked", backgroundColor: "#ffa81e"
    	        attributeState "Home ExitDelay", label: 'Home', icon: "st.locks.lock.locked", backgroundColor: "#ffa81e"
	            attributeState "Stay", label: 'Stay', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff"
            	attributeState "Stay AlarmPending", label: 'Stay', icon: "st.locks.lock.locked", backgroundColor: "#ff381e"
            	attributeState "Away", label: 'Away', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff"
            	attributeState "Away AlarmPending", label: 'Away', icon: "st.locks.lock.locked", backgroundColor: "#ff381e"
            	attributeState "Instant", label: 'Instant', icon: "st.locks.lock.locked", backgroundColor: "#1ea8ff"
            	attributeState "Instant AlarmPending", label: 'Instant', icon: "st.locks.lock.locked", backgroundColor: "#ff381e"
    		}
    		tileAttribute("device.system-status", key: "SECONDARY_CONTROL") {
      			attributeState "default", label: '', icon:"st.unknown.unknown", backgroundColor:"#1ea8ff"
            	attributeState "Home Bypass", label: 'Bypass in progress', icon: "st.unknown.unknown", backgroundColor: "#ffa81e"
    	        attributeState "Home ExitDelay", label: 'Exit Delay', icon: "st.unknown.unknown", backgroundColor: "#ffa81e"
            	attributeState "Stay AlarmPending", label: 'Alarm Pending', icon: "st.unknown.unknown", backgroundColor: "#ff381e"
            	attributeState "Away AlarmPending", label: 'Alarm Pending', icon: "st.unknown.unknown", backgroundColor: "#ff381e"
            	attributeState "Instant AlarmPending", label: 'Alarm Pending', icon: "st.unknown.unknown", backgroundColor: "#ff381e"
			}
		}

		standardTile("id", "device.id", decoration: "flat", width: 6, height: 1, ) {
            state "default", label: '${currentValue}', backgroundColor: "#808080", icon:"st.locks.lock.locked"
        }

		standardTile("mode1", "device.digital-life-mode", width: 3, height: 3, ) {
            state "default", label: 'Home', backgroundColor: "#79b821", icon:"st.locks.lock.unlocked", action:"home"
            state "Home", label: 'Stay', backgroundColor: "#1ea8ff", icon:"st.locks.lock.locked", action:"stay"
        }

		standardTile("mode2", "device.digital-life-mode", width: 3, height: 3, ) {
            state "default", label: 'Away', backgroundColor: "#1ea8ff", icon:"st.locks.lock.locked", action:"away"
            state "Away", label: 'Stay', backgroundColor: "#1ea8ff", icon:"st.locks.lock.locked", action:"stay"
        }

        main(["status"])
        details(['id', 'multi', 'mode1', 'mode2'])
	}
*/
}

// parse events into attributes
def parse(String description) {
}

def describeAttributes(payload) {
	payload.attributes = [
    	[ name: "system-status",	type: "enum",	options: ["Home", "Home Bypass", "Home ExitDelay", "Stay", "Stay AlarmPending", "Away", "Away AlarmPending", "Instant", "Instant AlarmPending"]	],
    	[ name: "digital-life-mode",	type: "enum",	options: ["Home", "Stay", "Instant", "Away"]	],
    ]
    return null
}

def configure(mode) {
	setMode(mode)
}

def setLevel(level) {
	if (level < 0) {
    	level = 0
    }
    if (level > 3) {
    	level = 3
    }
	switch (level) {
    	case 0:
        	home()
            break
    	case 1:
        	stay()
            break
    	case 2:
        	instant()
            break
    	default:
        	away()
            break
    }
}

def setMode(mode) {
	switch (mode.toLowerCase()) {
    	case 'home':
    	case 'disarm':
    	case 'disarmed':
    	case 'off':
        	home()
            break
    	case 'stay':
    	case 'night':
        	stay()
            break
    	case 'instant':
        	instant()
            break
    	case 'away':
        	away()
            break
    }
}

def on() {
	stay()
}

def off() {
	home()
}

def home() {
	parent.proxyCommand(device, 'digital-life-mode', 'Home');
}

def stay() {
	parent.proxyCommand(device, 'digital-life-mode', 'Stay');
}

def instant() {
	parent.proxyCommand(device, 'digital-life-mode', 'Instant');
}

def away() {
	parent.proxyCommand(device, 'digital-life-mode', 'Away');
}
