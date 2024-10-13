metadata {
    definition (name: "Dynamic Hue Animated Scene Controller", namespace: "hueKeo", author: "Joe Rosiak") {
        capability "Switch"
        capability "Refresh"
        
        command "activateViaJson", ["string"]
        command "setSceneName", ["string"]
        command "setZoneName", ["string"]
    }
    
    preferences {
        input name: "hueIp", type: "string", title: "Hue Bridge IP", description: "Enter the IP of your Hue Bridge", required: true
        input name: "hueZone", type: "string", title: "Hue Zone/Room Name", description: "Enter the Hue Zone or Room name", required: true
        input name: "sceneName", type: "string", title: "Animated Scene Name", description: "Enter the Animated Scene name", required: true
        input name: "apiKey", type: "password", title: "Hue API Key", description: "Enter your Hue Bridge API Key", required: true
    }
}

def installed() {
    log.debug "Installing Hue Animated Scene Controller"
    initialize()
}

def updated() {
    log.debug "Updating Hue Animated Scene Controller settings"
    initialize()
}

def initialize() {
    log.debug "Initializing Hue Animated Scene Controller"
}

def on() {
    log.debug "Turning on the Hue Animated Scene"
    activateScene()
}

def off() {
    log.debug "Turning off the lights in the Hue Zone"
    deactivateZone()
}

def refresh() {
    log.debug "Refreshing Hue Animated Scene Controller"
    // Placeholder for future refresh functionality
}

def activateViaJson(String jsonInput) {
    log.debug "Setting Scene and Zone with JSON input: ${jsonInput}"
    
    try {
        def parsedJson = new groovy.json.JsonSlurper().parseText(jsonInput)
        def newZoneName = parsedJson.zone
        def newSceneName = parsedJson.scene
        def action = parsedJson.action
        
        if (!newZoneName || !newSceneName) {
            log.error "Zone or Scene not provided in the JSON input"
            return
        }

        // Now activate or deactivate the scene based on the action
        if (action == "on") {
            activateSceneById(newZoneName, newSceneName)
        } else if (action == "off") {
            deactivateZone(newZoneName)
        } else {
            log.error "Invalid action provided: ${action}. Expected 'on' or 'off'."
        }
        
    } catch (Exception e) {
        log.error "Error parsing JSON input: ${e.message}"
    }
}

def activateSceneById(zoneName, sceneName) {
    if (!hueIp || !apiKey) {
        log.error "Hue IP or API key is not set. Please configure the device."
        return
    }
    
    def zoneId = getZoneId(zoneName)
    if (!zoneId) {
        log.error "Failed to get zone ID for zone: ${zoneName}"
        return
    }

    def sceneId = getSceneId(zoneId, sceneName)
    if (!sceneId) {
        log.error "Failed to get scene ID for scene: ${sceneName} in zone: ${zoneName}"
        return
    }
    
    def url = "http://${hueIp}/api/${apiKey}/groups/${zoneId}/action"
    def body = [scene: sceneId]
    
    try {
        def params = [
            uri: url,
            body: body,
            contentType: "application/json"
        ]
        
        httpPut(params) { resp ->
            if (resp.status == 200) {
                log.debug "Successfully activated scene: ${sceneName} in zone: ${zoneName}"
            } else {
                log.error "Failed to activate scene: ${sceneName}. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error activating scene: ${e.message}"
    }
}

def deactivateZone(zoneName = null) {
    // Use the passed-in zoneName if provided; otherwise, fall back to the configured zone in preferences
    def hueZone = zoneName ?: hueZone

    // Check if the required values (hueIp, hueZone, apiKey) are set
    if (!hueIp || !hueZone || !apiKey) {
        log.error "Hue IP, Zone, or API key is not set. Please configure the device."
        return
    }
    
    // Retrieve the zone ID
    def zoneId = getZoneId(hueZone)
    if (!zoneId) {
        log.error "Failed to find Hue zone with name: ${hueZone}"
        return
    }
    
    // Formulate the URL and request body to turn off the lights
    def url = "http://${hueIp}/api/${apiKey}/groups/${zoneId}/action"
    def body = [on: false]
    
    try {
        def params = [
            uri: url,
            body: body,
            contentType: "application/json"
        ]
        
        // Send the HTTP PUT request
        httpPut(params) { resp ->
            if (resp.status == 200) {
                log.debug "Turned off the lights in zone: ${hueZone}"
            } else {
                log.error "Failed to turn off the lights in zone: ${hueZone}. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error turning off the zone: ${e.message}"
    }
}

def getZoneId(zoneName) {
    def url = "http://${hueIp}/api/${apiKey}/groups"
    def zoneId = null
    
    try {
        httpGet([uri: url, contentType: "application/json"]) { resp ->
            if (resp.status == 200) {
                def groups = resp.data
                groups.each { id, group ->
                    if (group.name == zoneName) {
                        zoneId = id
                    }
                }
            } else {
                log.error "Failed to retrieve zones. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error retrieving zones: ${e.message}"
    }
    
    return zoneId
}

def getSceneId(zoneId, sceneName) {
    def url = "http://${hueIp}/api/${apiKey}/scenes"
    def sceneId = null
    
    try {
        httpGet([uri: url, contentType: "application/json"]) { resp ->
            if (resp.status == 200) {
                def scenes = resp.data
                scenes.each { id, scene ->
                    if (scene.name == sceneName && scene.group == zoneId) {
                        sceneId = id
                    }
                }
            } else {
                log.error "Failed to retrieve scenes. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error retrieving scenes: ${e.message}"
    }
    
    return sceneId
}
