/**
 *  AT&T Digital Life Controller
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
metadata {
	definition (name: "AT&T Digital Life Controller", namespace: "Keo", author: "Joe Rosiak", oauth: true) {
		capability "Battery"
        attribute "id", "string"
        attribute "module", "string"       
        attribute "type", "string"
        attribute "power-source", "enum", ["AC", "DC"]
        attribute "battery-level", "number"
        attribute "connectivity", "number"
        attribute "appliance-ip", "string"
        attribute "header-ip", "string"
        attribute "proxy-ip", "string"
        attribute "proxy-port", "string"
        attribute "alarmed-device-list", "string"
        attribute "armed-stay-device-list", "string"
        attribute "armed-away-device-list", "string"
        attribute "armed-instant-device-list", "string"
        attribute "alarm-timeout", "number"
        attribute "entry-delay", "number"
        attribute "exit-delay", "number"
        attribute "abort-timeout", "number"
	}
/*
    simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
		standardTile("power-source", "device.power-source", width: 2, height: 2) {
            state "AC", label: 'AC', icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png", backgroundColor: "#79b821", canChangeIcon: true, canChangeBackground: true
            state "DC", label: 'DC', icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png", backgroundColor: "#ffa81e", canChangeIcon: true, canChangeBackground: true
        }

		standardTile("id", "device.id", decoration: "flat", width: 6, height: 1, ) {
            state "default", label: '${currentValue}', backgroundColor: "#808080", icon:"http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png"
        }

		multiAttributeTile(name:"multi", type:"generic", width:6, height:4) {
			tileAttribute("device.power-source", key: "PRIMARY_CONTROL") {
      			attributeState "AC", label:'AC POWER', icon:"http://cdn.device-icons.smartthings.com/Appliances/appliances17@2x.png", backgroundColor:"#79b821"
      			attributeState "DC", label: 'ON BATTERY', icon:"http://cdn.device-icons.smartthings.com/Appliances/appliances17@2x.png", backgroundColor:"#ffa81e"
    		}
    		tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
      			attributeState "default", label: 'Battery ${currentValue}%', icon:"st.unknown.unknown", backgroundColor:"#ffa81e", unit:"%"
			}
		}

		standardTile("lan", "device.connectivity", width: 2, height: 2) {
            state "0", label: 'LAN ERROR', icon: "st.Weather.weather8", backgroundColor: "#ff381e"
            state "1", label: 'LAN ERROR', icon: "st.Weather.weather8", backgroundColor: "#ff381e"
            state "2", label: 'LAN OK', icon: "st.Weather.weather8", backgroundColor: "#79b821"
            state "3", label: 'LAN OK', icon: "st.Weather.weather8", backgroundColor: "#79b821"
        }

		standardTile("ip", "device.header-ip", decoration: "flat", width: 2, height: 2) {
            state "default", label: '${currentValue}', icon: "st.Weather.weather8"
        }

		standardTile("wan", "device.connectivity", width: 2, height: 2) {
            state "0", label: 'WAN ERROR', icon: "st.Weather.weather15", backgroundColor: "#ff381e"
            state "1", label: 'WAN OK', icon: "st.Weather.weather15", backgroundColor: "#79b821"
            state "2", label: 'WAN ERROR', icon: "st.Weather.weather15", backgroundColor: "#ff381e"
            state "3", label: 'WAN OK', icon: "st.Weather.weather15", backgroundColor: "#79b821"
        }

		main(["power-source"])
        details(['id', 'multi', 'lan', 'ip', 'wan'])
	}*/
}

// parse events into attributes
def parse(String description) {
}
