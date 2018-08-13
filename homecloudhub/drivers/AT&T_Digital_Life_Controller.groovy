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

}

// parse events into attributes
def parse(String description) {
}
