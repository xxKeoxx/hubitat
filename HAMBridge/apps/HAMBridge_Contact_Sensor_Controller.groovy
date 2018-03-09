/**
 *  HAMBridge Contact Controller
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
    name: "HAMBridge Contact Sensor Controller",
    namespace: "HAMBridge",
    author: "jprosiak",
    description: "Sends one HAMBridge command when contact is open. Sends another when contact is closed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Which Contact Sensors..."){
		input "contactSensor", "capability.contactSensor", title: "Where?", multiple: true, required: true
	}
    section("HAMBridge Information:"){
		input "server", "text", title: "Server IP", description: "Your HAM Bridger Server IP", required: true
		input "port", "number", title: "Port", description: "Port Number", required: true
        input "HAMBcommandContactOpen", "text", title: "Command to send when contact opens", required: true
        input "HAMBcommandContactClosed", "text", title: "Command to send when contact closes (optional)", required: false
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
    //def luxThreshold = 200
    subscribe(contactSensor, "contact.open", openHandler)
	subscribe(contactSensor, "contact.closed", closeHandler)
}

def openHandler(evt) {
    log.debug "Here we are 2"
    if ((!modes || modes.contains(location.mode))) {
		//state.pending = false
    	HAMBcommand = settings.HAMBcommandContactOpen
    	//log.debug evt
    	//log.debug ${lightSensor.currentValue}
    	log.debug "HAMBCommand = ${HAMBCommand}"
    	sendHttp()
    }
}

def closeHandler(evt) {
    log.debug "Here we are 3"
    if (!modes || modes.contains(location.mode)){
		//state.pending = !("active" in motionSensor.currentMotion)
    	//log.debug state.pending
    	//log.debug "delayMinutes = ${delayMinutes}"
		switchesOff()
        //if(state.pending) {if(delayMinutes) runIn(delayMinutes*60, switchesOff) else switchesOff()}
    }
}

def switchesOff() {
    log.debug "Here we are 4"
    HAMBcommand = settings.HAMBcommandContactClosed
    log.debug "HAMBcommand = ${HAMBCommand}"
    log.debug state.pending
	//if(state.pending) 
    sendHttp() 
    //switches.off()
}

def sendHttp() {
    log.debug "Here we are 5"
    def ip = "${settings.server}:${settings.port}"
    log.debug "${ip}"
    log.debug "sending ${HAMBcommand} to HAMBridge on ${ip}"
    sendHubCommand(new hubitat.device.HubAction("""GET /?${HAMBcommand} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", hubitat.device.Protocol.LAN))
}
