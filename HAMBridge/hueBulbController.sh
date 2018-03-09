#!/bin/bash

BASEURL="http://10.0.1.2/api/[add api user here]"

LIGHTNAME="Outside_MBR"
BULBID=`curl -s ${BASEURL}/lights/ | /usr/local/bin/jq -r -e --arg LIGHTNAME "${LIGHTNAME}" '. as $object | keys[] | select($object[.].name == $LIGHTNAME)'`
curl -s ${BASEURL}/lights/${BULBID} -X PUT -d"{\"on\":${TOGGLEMODE},\"bri\":254,\"hue\":13548,\"sat\":200,\"transitiontime\":50}"
