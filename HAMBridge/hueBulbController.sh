#!/bin/bash

BASEURL="http://[add your hue hub ip here]/api/[add your whitelist user]"

LIGHTNAME="Outside_MBR"
BULBID=`curl -s ${BASEURL}/lights/ | /usr/local/bin/jq -r -e --arg LIGHTNAME "${LIGHTNAME}" '. as $object | keys[] | select($object[.].name == $LIGHTNAME)'`
curl -s ${BASEURL}/lights/${BULBID} -X PUT -d"{\"on\":${TOGGLEMODE},\"bri\":254,\"hue\":13548,\"sat\":200,\"transitiontime\":50}"
