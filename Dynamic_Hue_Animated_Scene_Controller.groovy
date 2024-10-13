metadata {
    definition (name: "Dynamic Hue Animated Scene Controller", namespace: "hueKeo", author: "Joe Rosiak") {
        capability "Switch"
        capability "Refresh"
        
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

def setSceneName(String newSceneName) {
    log.debug "Setting new Scene Name: ${newSceneName}"
    sceneName = newSceneName
    activateScene()
}

def setZoneName(String newZoneName) {
    log.debug "Setting new Zone Name: ${newZoneName}"
    hueZone = newZoneName
    activateScene()
}

def activateScene() {
    if (!hueIp || !hueZone || !sceneName || !apiKey) {
        log.error "Hue IP, Zone, Scene, or API key is not set. Please configure the device."
        return
    }
    
    def zoneId = getZoneId(hueZone)
    if (!zoneId) {
        log.error "Failed to find Hue zone with name: ${hueZone}"
        return
    }
    
    def sceneId = getSceneId(zoneId, sceneName)
    if (!sceneId) {
        log.error "Failed to find scene with name: ${sceneName} in zone: ${hueZone}"
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
                log.debug "Activated scene: ${sceneName} in zone: ${hueZone}"
            } else {
                log.error "Failed to activate scene: ${sceneName}. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error activating scene: ${e.message}"
    }
}

def deactivateZone() {
    if (!hueIp || !hueZone || !apiKey) {
        log.error "Hue IP, Zone, or API key is not set. Please configure the device."
        return
    }
    
    def zoneId = getZoneId(hueZone)
    if (!zoneId) {
        log.error "Failed to find Hue zone with name: ${hueZone}"
        return
    }
    
    def url = "http://${hueIp}/api/${apiKey}/groups/${zoneId}/action"
    def body = [on: false]
    
    try {
        def params = [
            uri: url,
            body: body,
            contentType: "application/json"
        ]
        
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