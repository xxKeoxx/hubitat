/**
 *  Momentary HAM
 *
 *  Copyright 2015 Scottin Pollock
 *
 *
 */
definition(
    name: "Momentary HAM",
    namespace: "HAM Bridge",
    author: "Scottin Pollock",
    description: "Sends a HAMBridge command when a momentary switch is pushed. For more information on HAM Bridge visit http://soletc.com/HAMBridge",
    category: "My Apps",
    iconUrl: "http://scottinpollock.us/stuff/STIcons/HB.png",
    iconX2Url: "http://scottinpollock.us/stuff/STIcons/HB@2x.png")


preferences {
	section("When the following is turned on...") {
		input name: "master", title: "Which Switch?", type: "capability.switch", required: true
	}
    //section("Turn on all of these switches as well") {
		//input "switches", "capability.switch", multiple: true, required: false
	//}
	//section("And turn off all of these switches") {
		//input "offSwitches", "capability.switch", multiple: true, required: false
	//}
	section("Send this command to HAM Bridge"){
		input "HAMBcommandOn", "text", title: "Command to send when turned on...", required: false
		input "server", "text", title: "Server IP", description: "IP Address", defaultValue: "10.0.0.12", required: false
		input "port", "number", title: "Port", description: "Port number", defaultValue: "8080", required: false
    }
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(master, "switch.on", onHandler, [filterEvents: false])
}

def onHandler(evt) {
	log.debug evt.value
	log.debug onSwitches()
	onSwitches()?.on()
    offSwitches()?.off()
    if (HAMBcommandOn) {
    		def theCOM = HAMBcommandOn
            doHAMB(theCOM) 
}}

private onSwitches() {
	if(switches && onSwitches) { switches + onSwitches }
	else if(switches) { switches }
	else { onSwitches }
}

private offSwitches() {
	if(switches && offSwitches) { switches + offSwitches }
	else if(switches) { switches }
	else { offSwitches }
}



def doHAMB(theCOM) {
def ip = "${settings.server}:${settings.port}"
sendHubCommand(new hubitat.device.HubAction("""GET /?$theCOM HTTP/1.1\r\nHOST: $ip\r\n\r\n""", hubitat.device.Protocol.LAN))
}
