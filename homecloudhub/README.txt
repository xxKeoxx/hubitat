Device Drivers:
  All files named AT&T Digital Life*.grooy
App:
  homecloudhub.groovy
  
Supported Features:
  AT&T Digital Life controll using Rule Machine
  Virtual Devices of supported Drivers. (See Device Drivers list)
  
**** NOTE: The homecloudhub app still needs to have items cleaned up.
     This was the fastest port and I will be rewriteing it to make the interface less complicated to debug.  
     Since AT&T Digital life will be the only supported feature some of the complexity is no longer needed.

Installation:
  Copy all Drivers into the Drivers Code section of hubitat.
  Copy homecloudhub.groovy into the Apps Code section of hubitat.
  Install the HomeCloudHub NodeJS server.
  
Install NodeJS. You can follow these instructions to install Node JS 4.x or 5.x.
https://nodejs.org/en/download/package-manager/

On your linux machine, create a folder /var/node (if it doesn't exist yet). Download the homecloudhub.local folder onto your linux machine. I use this on a Raspberry Pi running Raspbian. Install necessary modules:

sudo npm install -g request
sudo npm install -g colors
sudo npm install -g node-ssdp
NOTE: If your npm modules are installed in a different location than /usr/lib/node_modules, and you plan on running this as a system service, then please change each of the javascript files' first line to reflect the correct path.

Change
  module.paths.push('/usr/lib/node_modules');
to
  module.paths.push('<your node modules path goes here>');

Test the application:
  node /var/node/homecloudhub.local/homecloudhub.js
  
#Making the app a bash executable (optional)

Create the file /usr/bin/homecloudhub with this content:

#!/usr/bin/env node
//
// This executable sets up the environment and runs the HomeCloudHub.
//

'use strict';
process.title = 'homecloudhub';

// Run HomeCloudHub
require('/var/node/homecloudhub.local/homecloudhub.js');

Give it execute rights:

sudo chmod 755 /usr/bin/homecloudhub

#Testing your server
VERY IMPORTANT: If you have a firewall installed, make sure you allow inbound connections to port 42457.

To run the server, run either
  node /var/node/homecloudhub.local/homecloudhub.js
or, alternatively, if you made an executable at the optional step above:
  homecloudhub
  
With homecloudhub running, Load New App within Hubitat app. Scroll down and click on the HomeCloudHub app. 
Select the local server method and it should automatically detect your server. If it doesn't, you can enter the IP manually, but it should detect it. Click next and enter your AT&T Digital Life credentials. These will be stored into the SmartApp settings collection, if security is a concern. Click Done to finish installing the application. At this point, within a few seconds, your Things should be automatically populated based on the Device Handlers you elected to install.

Installing homecloudhub as a system service
Create a new system username to run homecloudhub under:
  sudo useradd --system homecloudhub
  
VERY IMPORTANT Make sure the new user has read/write access to configuration file!
  sudo chown homecloudhub:homecloudhub /var/node/homecloudhub.local/config/homecloudhub.json 
NOTE: Some Operating Systems may require the .service extension within the systemd ecosystem. CentOS/RedHat seems to be one of them.

Create the /etc/default/homecloudhub file with this content:
  # Defaults / Configuration options for homecloudhub

  # If you uncomment the following line, homecloudhub will log more.
  # You can display this via systemd's journalctl: journalctl -f -u homecloudhub
  # DEBUG=*
  
Create the /etc/systemd/system/homecloudhub file with this content:
  [Unit]
  Description=Node.js Local Home Cloud Hub Server
  After=syslog.target

  [Service]
  Type=simple
  User=homecloudhub
  EnvironmentFile=/etc/default/homecloudhub
  ExecStart=/usr/bin/node /var/node/homecloudhub.local/homecloudhub.js
  Restart=on-failure
  RestartSec=10
  KillMode=process

  [Install]
  WantedBy=multi-user.target
  Setup the systemctl service by running:

  sudo systemctl daemon-reload
  sudo systemctl enable homecloudhub
  sudo systemctl start homecloudhub

Check the service status:
  sudo systemctl status homecloudhub


