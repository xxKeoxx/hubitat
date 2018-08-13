/**
 *  AT&T Digital Life Contact Sensor
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
metadata {
	definition (name: "AT&T Digital Life Contact Sensor", namespace: "Keo", author: "Joe Rosiak", oauth: true) {
    	capability "Sensor"
		capability "Contact Sensor"
        capability "Battery"
        capability "Refresh"
        attribute "id", "string"
        attribute "module", "string"       
        attribute "type", "string"
        attribute "arm-state", "string"
	}
}

// parse events into attributes
def parse(String description) {
}

def on() {
	log.trace('on() ' + device.dump() + ' - ' + device.name + ' - ' + device.label)
//    log.debug(parent.('proxyCommand'));
    parent.proxyCommand(device, 'switch', 'on')
}

def off() {
    log.debug(parent.proxyCommand(device, 'switch', 'off'))
}
