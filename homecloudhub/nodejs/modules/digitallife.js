/**
 *  AT&T Digital Life HCH module
 *
 *  Copyright 2016 Adrian Caramaliu
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
**/

/* module paths - please add your own path for node_modules if not here already */
module.paths.push('/usr/lib/node_modules');
module.paths.push('/usr/local/lib/node_modules');

var exports = module.exports = new function () {
    var
        app = null,
        module = null,
        config = {},
        callback = null,
        failed = false,
        https = require('https'),
        request = require('request').defaults({
            jar: true,
            encoding: 'utf8',
            followRedirect: true,
            followAllRedirects: true
        }),
        jar = null,
        devices = [],
        //recover timer for re-sync - if initial recover request failed
        tmrRecover = null,
        tmrTimeout = null,
        //listener socket (client) for events from digitallife
        listener = null,
        key = null,
        authToken = null,
        requestToken = null,

        //initialization of cookies
        doInit = function () {
            log('Initializing...');
            failed = false;
            if (config.key && config.authToken && config.requestToken) {
                log('Successfully got tokens.');
                //we need a cookie jar to be able to connect to AT&T Digital Life - they love cookies :)
                jar = request.jar();
                for (i in config.cookies) {
                    var cookie = request.cookie(config.cookies[i]);
                    jar.setCookie(cookie, 'https://my-digitallife.att.com');
                }
                //disable the automatic recovery
                if (tmrRecover) clearTimeout(tmrRecover);
                tmrRecover = null;
                doGetDevices();
                doListen();
            }
        },

        //recovering procedures
        doRecover = function () {
            //if (failed) {
            //    return;
            //}

            alert('Refreshing security tokens...');
            failed = true;
            //abort any existing listener client

            if (listener && listener.abort) {
                try {
                    listener.abort();
                } catch (e) {}
            }
            listener = null;
            config = {};

            //abort listener silence detector
            if (tmrTimeout) clearTimeout(tmrTimeout);
            tmrTimeout = null;

            //setup auto recovery
            if (tmrRecover) clearTimeout(tmrRecover);
            tmrRecover = setTimeout(invokeRecover, 120000); //recover in 2 minutes if for some reason the tokens are not refreshed

            app.refreshTokens(module);
        },

		invokeRecover = function() {
			//ensure all reasons to invoke recover are exhausted prior to actually recovering
			//this is to avoid multiple relogins

			log('invokeRecover was summoned.');

            if (listener && listener.abort) {
                try {
                    listener.abort();
                } catch (e) {}
            }
            listener = null;
            config = {};

            if (tmrRecover) clearTimeout(tmrRecover);
            tmrRecover = setTimeout(doRecover, 5000); //recover in 5 seconds if for some reason the tokens are not refreshed
		},

        //get devices
        doGetDevices = function (initial) {
            try {
                //we will quietly rescan devices about every one minute - we don't want to fill the logs up
                if (true) {
                    log('Getting list of devices...');
                }
                request
                    .get({
                            url: 'https://my-digitallife.att.com/penguin/api/' + config.key + '/devices',
                            jar: jar,
                            headers: {
                                'Referer': 'https://my-digitallife.att.com/dl/',
                                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36',
                                'DNT': '1',
                                'appKey': 'TI_3198CF46D58D3AFD_001', //seems to be hard-coded for their Digital Life IOS App
                                'authToken': config.authToken,
                                'requestToken': config.requestToken
                            }
                        },
                        function (err, response, body) {
                            if (!err && response.statusCode == 200) {
                                if (body) {
                                    try {
                                        var data = JSON.parse(body);
                                        if ((data) && (data.content) && (data.content.length)) {
                                            var t = 0;
                                            //cycle through each device
                                            for (d in data.content) {
                                                var dev = data.content[d];
                                                var device = {
                                                    'id': dev.deviceGuid,
                                                    'module': module,
                                                    'type': dev.deviceType,
                                                    'movable': dev.movable,
                                                    'events': ''
                                                };
                                                for (prop in dev.events) {
                                                    var event = dev.events[prop];
                                                    device.event += (device.event ? ',' : '') + event.event;
                                                }
                                                for (prop in dev.attributes) {
                                                    var attr = dev.attributes[prop];
                                                    doSetDeviceAttribute(device, attr.label, attr.value);
                                                }
                                                var existing = false;
                                                var notify = false;
                                                //we only push updates to other modules if there are any changes made
                                                for (i in devices) {
                                                    if (devices[i].id == device.id) {
                                                        //found an existing device
                                                        existing = true;
                                                        if (JSON.stringify(devices[i]) != JSON.stringify(device)) {
                                                            devices[i] = device;
                                                            notify = true;
                                                        }
                                                        break;
                                                    }
                                                }
                                                if (!existing) {
                                                    notify = true;
                                                    devices.push(device);
                                                }
                                                if (notify) {
                                                    //since notifications can come in bulk, we need to slow them down to about 4 per second
                                                    (function (device) {
                                                        setTimeout(function () {
                                                            callback({
                                                                name: 'discovery',
                                                                module: module,
                                                                data: {
                                                                    device: device,
                                                                    description: 'Discovered device "' + device.name + '" <' + device.id + '>'
                                                                }
                                                            })
                                                        }, t);
                                                    })(device);
                                                    //increase the timeout by 250ms every time we send one event
                                                    t += 250;
                                                }
                                            }
                                            if (initial) {
                                                log('Successfully got device list');
                                            }
                                            return;
                                        }
                                    } catch (e) {
                                        //reinitialize after an error
                                        error('Error reading device list: ' + e);
                                        invokeRecover();
                                        return;
                                    }
                                }
                            }
                            //reinitialize on error
                            error('Error getting device list: ' + err);
                            invokeRecover();
                        });
            } catch (e) {}
        },

        //listen for events
        doListen = function () {
            log('Listening for events... token is ' + config.requestToken);
            var buffer = '';
            if (listener && listener.abort) {
                try {
                    listener.abort();
                } catch (e) {}
            }
            try {
                listener = request
                    .get({
                        url: 'https://my-digitallife.att.com/messageRelay/pConnection?uuid=&app2="""&key=' + config.key,
                        jar: jar,
                        headers: {
                            'Referer': 'https://my-digitallife.att.com/dl/',
                            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36',
                            'DNT': '1',
                            'appKey': 'TI_3198CF46D58D3AFD_001',
                            'authToken': config.authToken,
                            'requestToken': config.requestToken
                        }
                    })
                    .on('data', function (str) {
                        try {
                            //log("RCVD: " + str);
                            if (tmrTimeout) clearTimeout(tmrTimeout)
                            tmrTimeout = setTimeout(function () {
                                error("Haven't received anything in one minute, we must have been disconnected...")
                               	invokeRecover();
                            }, 60000); //if we get nothing more in the next 60 seconds, we dropped the ball (we should get something every 30s)
                            buffer += str.replace(/\*|\n|\r/g, '');
                            var p = buffer.indexOf('"""');
                            if (p > 0) {
                                var event = JSON.parse(buffer.substr(0, p));
                                buffer = buffer.substr(p + 3);
                                if (event.type == 'device') {
                                    doProcessDeviceEvent(event);
                                } else {
                                    //console.log(event);
                                }
                            }
                        } catch (e) {
                            //reinitialize after an error
                            error('Error reading listener data: ' + e);
                            invokeRecover();
                            return;
                        }
                    })
                    .on('response', function (response) {
                        if (response.statusCode == 200) {
                            log('Connected and listening for events...');
                            return;
                        }
                        //reinitialize on error
                        error('Could not connect listener: ' + response.statusCode);
                        invokeRecover();
                    })
                    .on('end', function () {
                        //reinitialize on error
                        log('Listener connection terminated, recovering...');
                       invokeRecover();
                    })
                    .on('error', function (e) {
                        //reinitialize on error
                        error('An error occurred within the listener: ' + e);
                        invokeRecover();
                    });
            } catch (e) {
                error('Failed to setup listener: ' + e);
                invokeRecover();
            }
        },

        //process device events
        doProcessDeviceEvent = function (event) {
            try {
                for (i in devices) {
                    if (devices[i].id == event.dev) {
                        //found device
                        var device = devices[i];
                        var attribute = event.label;
                        var value = event.value;
                        var result = doSetDeviceAttribute(device, attribute, value);

                        if (result) {
                            callback({
                                name: 'update',
                                data: {
                                    device: device,
                                    module: module,
                                    event: event,
                                    attribute: attribute,
                                    oldValue: result.oldValue,
                                    newValue: result.newValue,
                                    value: value,
                                    description: 'Device "' + device.name + '" <' + device.id + '> (type: ' + device.type + ') changed its "' + attribute + '" value from "' + result.oldValue + '" to "' + result.newValue + '"'
                                }
                            });
                        }
                        return;
                    }
                }
            } catch (e) {
                //reinitialize after an error
                error('Failed to process device event: ' + e);
                invokeRecover();
                return;
            }
        },

        //rename/revalue device attributes to make them compatible to SmartThings
        doSetDeviceAttribute = function (device, attribute, value) {

            switch (device.type) {
            case 'smoke-sensor':
                if (attribute == 'smoke') {
                    value = (value == '0' ? 'clear' : 'detected');
                }
                break;
            case 'motion-sensor':
                if (attribute == 'motion') {
                    value = (value == '0' ? 'inactive' : 'active');
                }
                break;
            case 'indoor-siren':
                if (attribute == 'alarm') {
                    value = (value == '0' ? 'off' : 'siren');
                }
                break;
            }

            attribute = attribute
                .replace('contact-state', 'contact')
                .replace('battery-level', 'battery')
                .replace('signal-strength', 'rssi');

            var attr = (attribute != 'name' ? 'data-' : '') + attribute;
            //return true if the value changed
            if (device[attr] != value) {
                var oldValue = device[attr]
                device[attr] = value;
                return {
                    attr: attr,
                    oldValue: oldValue,
                    newValue: value
                }
            }
            return false;
        },

        //log
        log = function (message) {
            callback({
                name: 'log',
                data: {
                    message: message
                }
            });
        },

        //alert
        alert = function (message) {
            callback({
                name: 'log',
                data: {
                    alert: message
                }
            });
        },

        //error
        error = function (message) {
            callback({
                name: 'log',
                data: {
                    error: message
                }
            });
        }


    //public functions
    this.start = function (_app, _module, _config, _callback) {
        if (_app && _module && _config && _callback) {
            app = _app;
            module = _module;
            config = _config;
            callback = _callback;
            doInit();
            return true;
        }
        return false;
    };

    this.failed = function () {
        return !!failed;
    }

}();
