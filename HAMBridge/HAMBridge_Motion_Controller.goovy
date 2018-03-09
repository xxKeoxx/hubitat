/**
 *  HAMBridge motion Controller
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
 */

definition(
    name: "HAMBridge motion Controller",
    namespace: "HAMBridge",
    author: "jprosiak",
    description: "Sends one HAMBridge command when it's dark and motion is detected. Sends another when it becomes light or some time after motion ceases.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Which Motion Sensors..."){
		input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
	}
	section("And then off when it's light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
    section("HAMBridge Information:"){
		input "server", "text", title: "Server IP", description: "Your HAM Bridger Server IP", required: true
		input "port", "number", title: "Port", description: "Port Number", required: true
        input "HAMBcommandMotionActive", "text", title: "Command to send when motion starts", required: true
        input "HAMBcommandMotionInactive", "text", title: "Command to send when motion stops (optional)", required: false
	}
	section("Using which light sensor"){
		input "lightSensor", "capability.illuminanceMeasurement", required: false
	}
	section("Set mode restrictions"){
        input (
            name		: "modes"
            ,type		: "mode"
            ,title		: "Set for specific mode(s)"
            ,multiple	: true
            ,required	: false
        )
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
    log.debug "Here we are 1"
	//if (lightSensor) {
		//subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	//}
    subscribe(motionSensor, "motion.active", activeHandler)
	subscribe(motionSensor, "motion.inactive", inactiveHandler)
}

def activeHandler(evt) {
    if ((!modes || modes.contains(location.mode)) && (crntLux < 200)) {
    	log.debug "Here we are 2"
		state.pending = false
    	HAMBcommand = settings.HAMBcommandMotionActive
    	//log.debug evt
    	//log.debug ${lightSensor.currentValue}
    	def crntLux = lightSensor.currentValue("illuminance").toInteger()
    	log.debug "HAMBCommand = ${HAMBCommand}"
    	log.debug "Lux Level is ${crntLux}"
    	sendHttp()
    }
}

def inactiveHandler(evt) {
    if (!modes || modes.contains(location.mode)){
    	log.debug "Here we are 3"
		state.pending = !("active" in motionSensor.currentMotion)
    	log.debug state.pending
    	log.debug "delayMinutes = ${delayMinutes}"
		if(state.pending) {if(delayMinutes) runIn(delayMinutes*60, switchesOff) else switchesOff()}
    }
}

def switchesOff() {
    log.debug "Here we are 4"
    HAMBcommand = settings.HAMBcommandMotionInactive
    log.debug "HAMBcommand = ${HAMBCommand}"
    log.debug state.pending
	if(state.pending) sendHttp() 
    //switches.off()
}

def sendHttp() {
    log.debug "Here we are 5"
    def ip = "${settings.server}:${settings.port}"
    log.debug "${ip}"
    log.debug "sending ${HAMBcommand} to HAMBridge on ${ip}"
    sendHubCommand(new hubitat.device.HubAction("""GET /?${HAMBcommand} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", hubitat.device.Protocol.LAN))
}
