metadata {
    definition (name: "Hue Motion Sensor Controller", namespace: "hueKeo", author: "Joe Rosiak") {
        capability "Switch"
        capability "Refresh"
        
        command "setMotionSensorName", ["string"]

        attribute "motionSensorState", "string" // Custom attribute to display the sensor's current state (enabled/disabled)
    }
    
    preferences {
        input name: "hueIp", type: "string", title: "Hue Bridge IP", description: "Enter the IP of your Hue Bridge", required: true
        input name: "motionSensor", type: "string", title: "Motion Sensor Name", description: "Enter the Motion Sensor name", required: true
        input name: "apiKey", type: "password", title: "Hue API Key", description: "Enter your Hue Bridge API Key", required: true
    }
}

def installed() {
    log.debug "Installing Hue Motion Sensor Controller"
    initialize()
}

def updated() {
    log.debug "Updating Hue Motion Sensor Controller settings"
    initialize()
}

def initialize() {
    log.debug "Initializing Hue Motion Sensor Controller"
    refresh() // Automatically fetch the current state on initialization
}

def on() {
    log.debug "Enabling the motion sensor"
    enableMotionSensor()
}

def off() {
    log.debug "Disabling the motion sensor"
    disableMotionSensor()
}

def refresh() {
    log.debug "Refreshing Hue Motion Sensor Controller"
    getMotionSensorState()
}

def setMotionSensorName(String newSensorName) {
    log.debug "Setting new Motion Sensor Name: ${newSensorName}"
    motionSensor = newSensorName
    getMotionSensorState() // Fetch the current state when changing the sensor
}

def enableMotionSensor() {
    if (!hueIp || !motionSensor || !apiKey) {
        log.error "Hue IP, Motion Sensor, or API key is not set. Please configure the device."
        return
    }
    
    def sensorId = getMotionSensorId(motionSensor)
    if (!sensorId) {
        log.error "Failed to find Hue motion sensor with name: ${motionSensor}"
        return
    }
    
    def url = "http://${hueIp}/api/${apiKey}/sensors/${sensorId}/config"
    def body = [on: true]
    
    try {
        def params = [
            uri: url,
            body: body,
            contentType: "application/json"
        ]
        
        httpPut(params) { resp ->
            if (resp.status == 200) {
                log.debug "Enabled motion sensor: ${motionSensor}"
                sendEvent(name: "motionSensorState", value: "enabled") // Update the state to "enabled"
            } else {
                log.error "Failed to enable motion sensor: ${motionSensor}. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error enabling motion sensor: ${e.message}"
    }
}

def disableMotionSensor() {
    if (!hueIp || !motionSensor || !apiKey) {
        log.error "Hue IP, Motion Sensor, or API key is not set. Please configure the device."
        return
    }
    
    def sensorId = getMotionSensorId(motionSensor)
    if (!sensorId) {
        log.error "Failed to find Hue motion sensor with name: ${motionSensor}"
        return
    }
    
    def url = "http://${hueIp}/api/${apiKey}/sensors/${sensorId}/config"
    def body = [on: false]
    
    try {
        def params = [
            uri: url,
            body: body,
            contentType: "application/json"
        ]
        
        httpPut(params) { resp ->
            if (resp.status == 200) {
                log.debug "Disabled motion sensor: ${motionSensor}"
                sendEvent(name: "motionSensorState", value: "disabled") // Update the state to "disabled"
            } else {
                log.error "Failed to disable motion sensor: ${motionSensor}. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error disabling motion sensor: ${e.message}"
    }
}

def getMotionSensorId(sensorName) {
    def url = "http://${hueIp}/api/${apiKey}/sensors"
    def sensorId = null
    
    try {
        httpGet([uri: url, contentType: "application/json"]) { resp ->
            if (resp.status == 200) {
                def sensors = resp.data
                sensors.each { id, sensor ->
                    if (sensor.name == sensorName && sensor.type == "ZLLPresence") {
                        sensorId = id
                    }
                }
            } else {
                log.error "Failed to retrieve sensors. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error retrieving sensors: ${e.message}"
    }
    
    return sensorId
}

def getMotionSensorState() {
    if (!hueIp || !motionSensor || !apiKey) {
        log.error "Hue IP, Motion Sensor, or API key is not set. Please configure the device."
        return
    }

    def sensorId = getMotionSensorId(motionSensor)
    if (!sensorId) {
        log.error "Failed to find Hue motion sensor with name: ${motionSensor}"
        return
    }

    def url = "http://${hueIp}/api/${apiKey}/sensors/${sensorId}"
    
    try {
        httpGet([uri: url, contentType: "application/json"]) { resp ->
            if (resp.status == 200) {
                def sensorData = resp.data
                def isEnabled = sensorData.config.on
                if (isEnabled) {
                    sendEvent(name: "motionSensorState", value: "enabled") // Update the state to "enabled"
                } else {
                    sendEvent(name: "motionSensorState", value: "disabled") // Update the state to "disabled"
                }
                log.debug "Motion sensor ${motionSensor} is currently ${isEnabled ? 'enabled' : 'disabled'}"
            } else {
                log.error "Failed to get the motion sensor state. Response: ${resp.status}"
            }
        }
    } catch (Exception e) {
        log.error "Error retrieving motion sensor state: ${e.message}"
    }
}