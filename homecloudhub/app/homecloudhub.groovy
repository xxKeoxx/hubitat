/**
 *  ATTDL Interface
 *
 *
 *  NOTE: This application requires a local server connected to the same network as your Automation hub.
 *        Find more info at https://github.com/ady624/HomeCloudHub
 *
 *  Many thanks to Adrian Caramaliu who initally got this working on smartThings as well as many other developers on the hubitat forums
 *  who helped me port it over.
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
 *  v0.1.18.08.12 - Converted over to be used with hubitat.  Also removing support for IFTTT and myq.  Added additional child device AT&T_Digital_Life_Receiver
 *
**/

definition(
    name: "Home Cloud Hub",
    namespace: "Keo",
    author: "Joe Rosiak",
    description: "Provides integration with AT&T Digital Life",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

private getLocalServerURN() {
	return "urn:schemas-upnp-org:device:HomeCloudHubLocalServer:624"
}

preferences {
	page(name: "prefWelcome", title: "Welcome to Home Cloud Hub")
	page(name: "prefHCH", title: "Connect to Home Cloud Hub")
    page(name: "prefModulesPrepare", title: "Home Cloud Hub Modules")
    page(name: "prefModules", title: "Home Cloud Hub Modules")
	page(name: "prefATT", title: "AT&T Digital Life™ Integration")
	page(name: "prefATTConfirm", title: "AT&T Digital Life™ Integration")
}


/***********************************************************************/
/*                        INSTALLATION UI PAGES                        */
/***********************************************************************/
def prefWelcome() {
	state.ihch = [security: [:]]    
    return dynamicPage(name: "prefWelcome", title: "Welcome to Home Cloud Hub", uninstall: state.installed) {
        section("Connection Type") {
            paragraph "Welcome to Home Cloud Hub. Please select a server connection method to start"
        }
        section() {
            href(name: "href",
                 title: "Use a local Home Cloud Hub server you installed",
                 required: false,
                 params: [next: true, hchLocal: true],
                 page: "prefHCH",
                 description: "Select this if you have already installed a local server in your network",
                 state: state.ihch.useLocalServer ? "complete" : null)
        }
    }
}

def prefHCH(params) {
	if (params.next) {
		state.ihch.useLocalServer = params.hchLocal
    }
    return dynamicPage(name: "prefHCH", title: "Connect to Home Cloud Hub", nextPage: "prefModulesPrepare") {
    	if (state.ihch.useLocalServer) {
            atomicState.hchLocalServerIp = null
            searchForLocalServer()
            def cnt = 50
            def hchLocalServerIp = null
            while (cnt--) {
            	pause(100)
                hchLocalServerIp = atomicState.hchLocalServerIp
                if (hchLocalServerIp) {
                	state.ihch.localServerIp = hchLocalServerIp
                	break
                }
            }
            log.trace "Stopped waiting..."
            section("Automatic configuration") {
				if (hchLocalServerIp) {
					href(name: "href",
                         title: "Use local server ${hchLocalServerIp}",
                         required: false,
                         params: [next: true, hchLocalServerIp: hchLocalServerIp],
                         page: "prefModulesPrepare",
                         description: "Select this to use this detected local server",
                         state: "complete")
	            } else {
                	paragraph "Could not identify any local servers"
                }
            }           
        	section("Manual Configuration") {
			    input("hchLocalServerIp", "text", title: "Enter the IP of your local server", required: false, defaultValue: "")
                input("hchMacAddress", "string", title:"Enter the MAC Address of your nodejs server", defaultValue: "", required: true)
            }
        }
    }
}

def prefModulesPrepare(params) {
    log.info "params: " + params
    log.info "settings: " + hchLocalServerIp
    try {
	  if (params.hchLocalServerIp) {
    	  state.ihch.localServerIp = params.hchLocalServerIp
      } else {
          state.ihch.localServerIp = settings.hchLocalServerIp
      }
    }catch(NullPointerException){
      state.ihch.localServerIp = hchLocalServerIp
    }
    log.trace "IP is $state.ihch.localServerIp"
    if (doHCHLogin()) {
      log.trace "HERE 1"
	    //prefill states for the modules
    	doATTLogin(true, true)
		return prefModules()
	} else {
    	if (state.ihch.useLocalServer) {
			return dynamicPage(name: "prefModulesPrepare",  title: "Error connecting to Home Cloud Hub local server") {
				section(){
					paragraph "Sorry, your local server does not seem to respond at ${state.ihch.localServerIp}."
				}
	        }
        } 
    }
}

def prefModules() {
    return dynamicPage(name: "prefModules", title: "Add modules to Home Cloud Hub", install: state.ihch.useATT || state.ihch.useMyQ || state.ihch.useIFTTT) {
        section() {
            paragraph "Select and configure any of the modules below"
        }
        section() {
            href(name: "href",
                 title: "AT&T Digital Life™",
                 required: false,
                 page: "prefATT",
                 description: "Enable integration with AT&T Digital Life™",
                 state: state.ihch.useATT ? "complete" : null)
        }
    }    
}

def prefATT() {
    if (doHCHLogin()) {
		return dynamicPage(name: "prefATT", title: "AT&T Digital Life™ Integration", nextPage:"prefATTConfirm") {
            section() {
                paragraph "Home Cloud Hub will integrate your AT&T Digital Life™ alarm system into Hubitat."
                paragraph "NOTE: Since AT&T Digital Life™ does not support OAuth, your username and password are required for integration. They will be stored in your personal instance of the application on SmartThings' servers and are NEVER shared with anyone, not even with Home Cloud Hub. The credentials are used to generate a set of temporary tokens that are shared with Home Cloud Hub."
            }
            section("Login Credentials"){
                input("attUsername", "Username", title: "Username", description: "Your AT&T Digital Life™ login", required: false)
                input("attPassword", "password", title: "Password", description: "Your password", required: false)
            }
            section("Permissions") {
				input("attControllable", "bool", title: "Control AT&T Digital Life", required: true, defaultValue: true)
				input("attAutoBypass", "bool", title: "Automatically bypass", required: false, defaultValue: false)
				//input("attSyncLocationMode", "bool", title: "Sync Location Mode", required: true, defaultValue: false)
				//input("attSyncSmartHomeMonitor", "bool", title: "Sync Smart Home Monitor", required: true, defaultValue: false)
            }
    	}
	} else {
		return dynamicPage(name: "prefATT",  title: "Error connecting to Home Cloud Hub", install:false, uninstall:false) {
			section(){
				paragraph "Sorry, the credentials you provided for Home Cloud Hub are invalid. Please go back and try again."
			}
        }
    }
}

def prefATTConfirm() {
    //if (doATTLogin(true, true)) {
    doATTLogin(true, true)
    if (attConnectionStatus == true){
		return dynamicPage(name: "prefATTConfirm", title: "AT&T Digital Life™ Integration", nextPage:"prefModules") {
			section(){
				paragraph "Congratulations! You have successfully connected your AT&T Digital Life™ system."
			}
    	}
	} else {
		return dynamicPage(name: "prefATTConfirm",  title: "AT&T Digital Life™ Integration") {
			section(){
				paragraph "Sorry, the credentials you provided for AT&T Digital Life™ are invalid. Please go back and try again."
			}
        }
    }
}


/***********************************************************************/
/*                           LOGIN PROCEDURES                          */
/***********************************************************************/
/* Login to Home Cloud Hub                                             */
/***********************************************************************/
private doHCHLogin() {
	try {
		if (state.ihch.useLocalServer) {
        	atomicState.hchPong = false

			log.trace "Pinging local server at " + state.ihch.localServerIp
        	sendLocalServerCommand state.ihch.localServerIp, "ping", [:]

			def cnt = 50
        	def hchPong = false
        	//def hchpong = true
        	while (cnt--) {
            	pause(100)
            	hchPong = atomicState.hchPong
            	if (hchPong) {
                	return true
            	}
        	}
        	//return false
        	return true
		}
	} catch (e) { log.error "Error logging in to HCH...", e }
}

/***********************************************************************/
/* Login to AT&T Digital Life                                          */
/***********************************************************************/
private doATTLogin(installing, force) {
	try {
	def module_name = 'digitallife';
    //if cookies haven't expired and unless we need to force a login, we report all is pink
    if (!installing && !force && state.hch.security[module_name] && state.hch.security[module_name].connected && (state.hch.security[module_name].expires > now())) {
		log.info "Reusing previously login for AT&T Digital Life"
		return true;
    }
    //setup our security descriptor
    def hch = (installing ? state.ihch : state.hch)
    hch.useATT = false;
    hch.security[module_name] = [
    	'enabled': !!(settings.attUsername || settings.attPassword),
        'controllable': settings.attControllable,
        'syncLocationMode': settings.attSyncLocationMode,
        'syncSmartHomeMonitor': settings.attSyncSmartHomeMonitor,
        'connected': false
    ]
    //check if the AT&T Digital Life module is enabled
	if (hch.security[module_name].enabled) {
    	log.trace "Logging in to AT&T Digital Life..."
        //perform the initial login, retrieve cookies
        return httpPost([
        	uri: "https://my-digitallife.att.com/tg_wam/login.do",
            requestContentType: "application/x-www-form-urlencoded",
            headers: [
				'Referer': 'https://my-digitallife.att.com/dl/',
                'contentType': "application/json",
                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36'
            ],           
            body: "source=DLNA&targetURL=https://my-digitallife.att.com/dl/#/authenticate&loginURL=https://my-digitallife.att.com/dl/#/login&userid=${settings.attUsername}&password=${settings.attPassword}"
        ]) { response ->
        	//check response, sometimes they redirect, that's fine, we don't need to follow the redirect, we just need the cookies
			if ((response.status == 200) || (response.status == 302)) {
				def cookies = []
                def c = "";
                //the hard part, get the cookies
				response.getHeaders('Set-Cookie').each {
                    def cookie = it.value.split(';')[0]
					if (!cookie.startsWith('PD_')) cookies.push(cookie)
                    c = c + cookie + '; '
				}
                //using the cookies, retrieve the auth tokens
                return httpPost([
		       		uri: "https://my-digitallife.att.com/penguin/api/authtokens",
                    requestContentType: "application/x-www-form-urlencoded; charset=utf-8",
                    headers: [
                        "Referer": "https://my-digitallife.att.com/dl",
                        "contentType": "application/json",
                        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36",
                        "DNT": "1",
                        "Cookie": c
                    ],                              	    
	                body: "domain=DL&appKey=TI_3198CF46D58D3AFD_001"
       			]) { response2 ->
                	//check response, continue if 200 OK
                    //log.trace response2.status
                	if (response2.status == 200) {
                        if (response2.data && response2.data.content && response2.data.content.gateways && response2.data.content.gateways.length) {
                        	//save the cookies and tokens into the security descriptor
                            //cookies expire in 13 minutes, we'll use 12 minutes as an expiry to ensure we don't refuse reconnection
                        	hch.security[module_name].key = response2.data.content.gateways[0].id
                            hch.security[module_name].authToken = response2.data.content.authToken
                            hch.security[module_name].requestToken = response2.data.content.requestToken
                            hch.security[module_name].cookies = cookies
                            hch.security[module_name].connected = now()
                            hch.security[module_name].expires = now() + 720000 //expires in 12 minutes
                            log.info "Successfully connected to AT&T Digital Life"
                            attConnectionStatus = true
                            hch.useATT = true;
                            return true;
                        }
                    }
                    return false;
				}
            } else {
                return false
            }
        } 	
	} else {
    	return true;
    }
    } catch(e) { log.error "Error logging in to AT&T", e }
}



/***********************************************************************/
/*            INSTALL/UNINSTALL SUPPORTING PROCEDURES                  */
/***********************************************************************/
def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def uninstalled() {
	//call home and bid them fairwell
    if (state.hch && !state.hch.useLocalServer) {
		httpGet('https://www.homecloudhub.com/endpoint/02666328-0063-0086-0069-076278844647/manager/smartthingsapp/disconnect/' + state.hch.endpoint.bytes.encodeBase64())
    }
}

def initialize() {
    hchMacAddress = hchMacAddress.replace(":","").toUpperCase()
    try{
		log.info "Attempting to create AT&T_Digital_Life_Receiver"
		addChildDevice("Keo", "AT&T_Digital_Life_Receiver", hchMacAddress)
    }catch(IllegalArgumentException e){
        log.info "AT&T_Digital_Life_Receiver already exists"
        //log.info e
    }
    state.installed = true    
    //get the installing hch state
    state.hch = state.ihch

	//login to all services
   	doATTLogin(false, false)
   	//doMyQLogin(false, false)
	
	if (state.hch.useLocalServer) {
    	//initialize the local server
		sendLocalServerCommand state.hch.localServerIp, "init", [
        	server: getHubLanEndpoint(),
            modules: state.hch.security
        ]
    }
    
	state.hch.usesATT = !!(settings.attUsername || settings.attPassword)

    if ((state.hch.usesATT) && (settings.attControllable)) {
    	if (settings.attSyncLocationMode) {
	        /* subscribe to mode changes to allow sync with AT&T Digital Life */
	        subscribe(location, modeChangeHandler)
        }
    	if (settings.attSyncSmartHomeMonitor) {        
    		/* subscribe to SmartThings Home Monitor to allow sync with AT&T Digital Life status */
			subscribe(location, "alarmSystemStatus", shmHandler)
        }
    }
	if (state.hch.useLocalServer) {
		//listen to LAN incoming messages
		subscribe(location, null, lanEventHandler, [filterEvents:false])
	}
}


private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}


/***********************************************************************/
/*                    SMARTTHINGS EVENT HANDLERS                       */
/***********************************************************************/
def shmHandler(evt) {
	def shmState = location.currentState("alarmSystemStatus")?.value;
    log.info "Received notification of SmartThings Home Monitor having changed status to ${shmState}"
    def mode = null
    switch (shmState) {
    	case 'off':
        	mode = 'Home'
            break
    	case 'stay':
        	mode = 'Stay'
            break
    	case 'away':
        	mode = 'Away'
            break
    }
    if (mode) {
		def children = getChildDevices()
    	children.each {
	    	//look for the digital-life-system device
	        if ((it.currentValue('module') == 'digitallife') && (it.currentValue('type') == 'digital-life-system')) {
				if (mode != it.currentValue('digital-life-mode')) {
                	log.info 'Requesting mode ' + mode + ' from ' + it.name
                    proxyCommand it, 'digital-life-mode', mode
                }
	        }
        }
 	}
    //return true;
}

def modeChangeHandler(event) {
	//abort if not controllable or not following mode
	if ((!settings.attControllable) || (!settings.attFollowMode)) {
    	return
    }
    if (event.name == 'mode') {
        log.info "Received notification of Location Mode having changed to ${event.value}"
        def mode = null;
        switch (event.value) {
            case 'Home':
                mode = 'Home'
                break
            case 'Night':
                mode = 'Stay'
                break
            case 'Away':
                mode = 'Away'
                break
        }
        if (mode) {
            def children = getChildDevices()
            children.each {
                //look for the digital-life-system device
                if ((it.currentValue('module') == 'digitallife') && (it.currentValue('type') == 'digital-life-system')) {
                    if (mode != it.currentValue('digital-life-mode')) {
                        log.info 'Requesting mode ' + mode + ' from ' + it.name
                        proxyCommand it, 'digital-life-mode', mode
                    }
                }
            }
        }
        //return true;
	}
}

private searchForLocalServer() {
    if(!state.ihch.subscribed) {
		subscribe(location, null, lanEventHandler, [filterEvents:false])
		state.ihch.subscribed = true
    }       
	log.trace "Looking for local HCH server..."
	sendHubCommand(new hubitat.device.HubAction("lan discovery " + getLocalServerURN(), hubitat.device.Protocol.LAN))
}

//def lanEventHandler(evt) {
def lanEventHandler(fromChildDev) {
    //log.debug "message: $fromChildDev"
    try{
    	def parsedEvent = parseLanMessage(fromChildDev).json
    	def description = parsedEvent?.description
    	def hub = parsedEvent?.hubId
		//def parsedEvent = parseLanMessage(description)
        //log.debug "parsedEvent = ${parsedEvent}"
    	//log.debug "event = " + parsedEvent?.event
    	//log.debug "description = ${description}"
    	//log.debug "data = " + parsedEvent?.data
    	//log.debug "hub = ${hub}"
    	//discovery
    	//evt.each { log.trace it }
		if (parsedEvent.ssdpTerm && parsedEvent.ssdpTerm.contains(getLocalServerURN())) {
    		log.trace "DISCOVERY SUCCESSFUL"
        	atomicState.hchLocalServerIp = convertHexToIP(parsedEvent.networkAddress)
		}
    
    	//ping response
    	if (parsedEvent && parsedEvent.service && (parsedEvent.service == "hch")) {
	    	def msg = parsedEvent
        	if (msg.result == "pong") {
        		//log in successful to local server
            	//log.info "Successfully contacted local server"
				atomicState.hchPong = true
        	}   	
    	}
    	if (parsedEvent && parsedEvent.event) {
    	//parsedEvent.event = "init"
    	//if (parsedEvent) {
	    	log.trace "GOT LAN EVENT ${parsedEvent.event} and data ${parsedEvent.data}"
        	switch (parsedEvent.event) {
        		case "init":
                	sendLocalServerCommand state.hch.localServerIp, "init", [
                            server: getHubLanEndpoint(),
                            modules: processSecurity([module: parsedEvent.data])
                        ]
					break
        		case "event":
            		log.trace "Processing Event"
            		processEvent(parsedEvent.data);
                	break
        	}
    	}
    }catch(MissingMethodException){
        pass
    }
}

private sendLocalServerCommand(ip, command, payload) {
	try {
        ip = ip ?: state.sch.localServerIp
        log.trace "Sending command $command with payload size ${"${groovy.json.JsonOutput.toJson(payload).bytes.encodeBase64()}".size()} to IP $ip"
        log.trace payload
        sendHubCommand(new hubitat.device.HubAction(
            method: "GET",
            path: "/${command}",
            headers: [
                HOST: "${ip}:42457"
            ],
            //body: [payload: payload]
            query: payload ? [payload: groovy.json.JsonOutput.toJson(payload).bytes.encodeBase64()] : []
        ))
	} catch (e) { log.error "Got an error...", e }
}


private getHubLanEndpoint() {
	def server = [:]
	location.hubs.each {
	    //look for enabled modules
        def ip = it?.localIP
        def port = it?.localSrvPortTCP
        if (ip && port) {
        	log.trace "Found local endpoint at ${ip}:${port}"
        	server.ip = ip
            server.port = port
            return server
        }
    }
    return server
}



/***********************************************************************/
/*                      EXTERNAL EVENT MAPPINGS                        */
/***********************************************************************/
mappings {
    path("/event") {
        action: [
            GET: "processEvent",
            PUT: "processEvent"
        ]
    }
    path("/security") {
        action: [
            GET: "processSecurity"
        ]
    }
}



/***********************************************************************/
/*                      EXTERNAL EVENT HANDLERS                        */
/***********************************************************************/
private processEvent(data) {
    //log.trace "processEvent(data) = " + data
	if (!data) {
    	data = params
        //log.trace "processEvent(data) when null = $data"
    }

    def eventName = data?.event
    def eventValue = data?.value
    def deviceId = data?.id
    def deviceModule = data?.module
    def deviceName = data?.name.capitalize()
    def deviceType = data?.type
    def description = data?.description
    if (description) {
    	log.info 'Received event: ' + description
    } else {
    	log.info "Received ${eventName} event for module ${deviceModule}, device ${deviceName} of type ${deviceType}, value ${eventValue}, data: $data"
    }
	// see if the specified device exists and create it if it does not exist
    def deviceDNI = (deviceModule + '-' + deviceId).toLowerCase();
    def device = getChildDevice(deviceDNI)
    if(!device) {   
    	//build the device type
        def deviceHandler = null;
        //support for various modules
        switch (deviceModule) {
        	case 'digitallife':
            	deviceHandler = 'AT&T Digital Life ' + deviceType.replace('digital-life-', '').replace('-', ' ').split()*.capitalize().join(' ')
                break
        	case 'myq':
            	deviceHandler = 'MyQ ' + deviceType.replace('GarageDoorOpener', 'Garage Door').replace('-', ' ').split()*.capitalize().join(' ')
                break
        }
        if (deviceHandler) {
        	// we have a valid device type, create it
            try {
        		device = addChildDevice("Keo", deviceHandler, deviceDNI, null, [label: deviceName])
        		device.sendEvent(name: 'id', value: deviceId);
        		device.sendEvent(name: 'module', value: deviceModule);
        		device.sendEvent(name: 'type', value: deviceType);
            } catch(e) {
            	log.info "Home Cloud Hub discovered a device that is not yet supported by your hub. Please find and install the [${deviceHandler}] device handler from https://github.com/ady624/HomeCloudHub/tree/master/devicetypes/ady624"
            	log.info "If the repository is missing the [${deviceHandler}] device handler, please provide the device data to the author of this software so he can add it. Thank you. Device data is [${data}]"
            }
        }
    }
    if (device) {
    	// we have a valid device that existed or was just created, now set the state
        for(param in data) {
            def key = param.key
        	def value = param.value
            if (deviceId != device.currentValue("id")) {
            	device.sendEvent(name: "id", value: deviceId);
            }
        	if ((key.size() > 5) && (key.substring(0, 5) == 'data-')) {
            	key = key.substring(5);
                def oldValue = device.currentValue(key);
                if (oldValue != value) {
					device.sendEvent(name: key, value: value);
                    //list of capabilities
                    //http://docs.smartthings.com/en/latest/capabilities-reference.html

                    //digital life alarm sync
                    if (true) {
                        if ((deviceType == 'digital-life-system') && (key == 'system-status') && (eventValue == value)) {
                            log.info "Digital Life alarm status changed from ${oldValue} to ${value}"
                            def mode = null;
                            def level = null;
                            def sweetch = null; //can't use "switch"
                            def shmState = null;                            
                            switch (value) {
                                case "Home":
                                    mode = "Home"
                                    level = 0
                                    sweetch = "off"
                                    shmState = "off"
                                    break
                                case "Away":
                                    mode = "Away"
                                    level = 3
                                    sweetch = "on"
                                    shmState = "away"
                                    break
                                case "Stay":
                                    mode = "Night"
                                    level = 1
                                    sweetch = "on"
                                    shmState = "stay"
                                    break
                                case "Instant":
                                    mode = "Night"
                                    level = 2
                                    sweetch = "on"
                                    shmState = "stay"
                                    break                        
                            }
                            //push that bypass button
                            if (settings.attAutoBypass && value.contains("Bypass")) {
                            	def lastRequestedMode = state.lastRequestedMode
                                def bypassedDeviceList = data["data-bypassed-device-list"]?:''
                                if (bypassedDeviceList?.size() && (lastRequestedMode in ['Stay', 'Away', 'Instant'])) {
                            		log.info "Bypass required for $lastRequestedMode mode, automatically bypassing the not ready devices: $bypassedDeviceList"                               
                            		cmd_digitallife(device, 'digital-life-mode', lastRequestedMode, false, bypassedDeviceList)
                                }
                            }
							if (mode) {
                            	//set device mode
                                if (value != device.currentValue('digital-life-mode')) {
                                    log.info 'Switching Digital Life mode from ' + device.currentValue('digital-life-mode') + ' to ' + value
	                                device.sendEvent(name: 'digital-life-mode', value: value);
                                    device.sendEvent(name: 'level', value: level);
                                    device.sendEvent(name: 'switch', value: sweetch);
                                }
                                //sync location mode
                                if ((settings.attSyncLocationMode) && (mode != location.mode)) {
                                    log.info 'Switching location mode from ' + location.mode + ' to ' + mode
                                    location.setMode(mode);
                                }
                            	//sync SmartThings Home Monitor
                            	def currentShmState = location.currentState('alarmSystemStatus')?.value
                            	if ((settings.attSyncSmartHomeMonitor) && (shmState) && (shmState != currentShmState)) {
                                	log.info 'Switching SmartThings Home Monitor from ' + currentShmState + ' to ' + shmState
                                	sendLocationEvent(name: 'alarmSystemStatus', value: shmState)
                            	}
							}
                    	}
                	}
            	}
        	}
    	}
    }
}

private processSecurity(data) {
    log.trace "Running processSecurity"
	if (!data) {
    	data = params
    }
    
	def module = data?.module
    if (module) {
		log.info "Received request for security tokens for module ${module}"
    } else {
		log.info "Received request for security tokens for all modules"
    }
	//we are provided an endpoint to which to send command requests
	state.hch.security.each {
	    //look for enabled modules
        def name = it?.key
        def config = it?.value
        if (config.enabled && ((name == module) || !module)) {
        	switch (name) {
            	case "digitallife":
                	doATTLogin(false, !module)
                    break
            	case "myq":
                	doMyQLogin(false, !module)
                    break
            }
	        config.age = (config.connected ? (now() - config.connected) / 1000 : null)
        }
    }
    log.info "Providing security tokens to Home Cloud Hub"
    if (module) {
        //we only requested one module for refresh
        def sl = [:]
        if (state.hch.security[module]) {
        	sl[module] = state.hch.security[module];
        }
        return sl;
    } else {
    	return state.hch.security;
    }
}




/***********************************************************************/
/*                       EXTERNAL COMMAND PROXY                        */
/***********************************************************************/
def proxyCommand(device, command, value) {
	//child devices will use us to proxy things over to the homecloudhub.com service
	def module = device.currentValue('module')
    try {
        return "cmd_${module}"(device, command, value, false)
    } catch(e) {
    	return "Error proxying command: ${e}"
    }
	def id = device.currentValue('id')
    if (id && module && state.hch.security[module] && state.hch.security[module].controllable) {
       	def uri = 'https://www.homecloudhub.com/endpoint/' + state.hch.endpoint + '/' + module + '/' + id + '/' + command + '/' + value.bytes.encodeBase64()
        try {
            httpGet(uri) { resp ->
                return true;
            }
        } catch (e) {
            log.error 'Oh oh, something went wrong while requesting command ' + uri + ': ' + e
        }
    }
    return(null)
}



/***********************************************************************/
/*                  AT&T DIGITAL LIFE MODULE COMMANDS                  */
/***********************************************************************/
def cmd_digitallife(device, command, value, retrying, bypass = '') {
	//are we allowed to use ATT?
   	def module_name = "digitallife"
	if (!state.hch.useATT || !(state.hch.security && state.hch.security[module_name] && state.hch.security[module_name].controllable)) {
    	//we are either not using this module or we can't controll it
    	return "No permission to control AT&T Digital Life"
    }
	if (!doATTLogin(false, retry)) {
    	log.error "Failed sending command to AT&T Digital Life because we could not connect"
    }
    def cookies = ""   
    //the hard part, get the cookies
    state.hch.security[module_name].cookies.each {
        cookies = cookies + it.value + "; "
    }
    //using the cookies, retrieve the auth tokens
    def path = null
    def data = null
    switch (device.currentValue("type")) {
    	case "digital-life-system":
        	switch (command) {
            	case "digital-life-mode":
                	state.lastRequestedMode = value
                	path = "alarm"
                    data = [
                    		bypass: bypass?:'',
                            status: value
                    	]
                	break;
            }
        	break;
    }
    def result = false
    def message = ""
    if (path && data) {
    	try {
            result = httpPostJson([
                uri: "https://my-digitallife.att.com/penguin/api/${state.hch.security[module_name].key}/${path}",
                headers: [
					"Referer": "https://my-digitallife.att.com/dl",
                    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36",
                    "DNT": "1",
                    "appKey": "TI_3198CF46D58D3AFD_001",
                    "authToken": state.hch.security[module_name].authToken,
                    "requestToken": state.hch.security[module_name].requestToken,
                    "Cookie": cookies
                ],
                //tlsVersion: "TLSv1.1",
                body: data
            ]) { response ->
                //check response, continue if 200 OK
                message = response.data
                if (response.status == 200) {
                	return true
                }
                return false
            }
            if (result) {
            	return "Successfuly sent command to AT&T Digital Life: device ${device} command ${command} value ${value} result ${message}"
            } else {
            	//if we failed and this was already a retry, give up
            	if (retrying) {
		            return "Failed sending command to AT&T Digital Life: ${message}"
                }
            	//we failed the first time, let's retry
                return "cmd_${module_name}"(device, command, value, true)
            }
		} catch(e) {
    		message = "Failed sending command to AT&T Digital Life: ${e}"
        }
    }
}
