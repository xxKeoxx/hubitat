#!/bin/sh

TOGGLEMODE=[[PARAM1]]

if [[ -n $1 ]]; then
    TOGGLEMODE=$1
fi

case ${TOGGLEMODE} in
    'on') TOGGLEMODE="true";;
    'off') TOGGLEMODE="false";;
    *) echo "Only on and off are valid"; exit 100;;
esac

#curl http://10.0.1.2/api/afHFrFgGgrh6bcOlNJYT45RJX1VW6qx7K8KBRG56/lights/3/state -X PUT -d"{\"on\":${TOGGLEMODE},\"bri\":254,\"hue\":13548,\"sat\":200,\"transitiontime\":50}"
#curl http://10.0.1.2/api/afHFrFgGgrh6bcOlNJYT45RJX1VW6qx7K8KBRG56/lights/3/state -X PUT -d"{\"on\":${TOGGLEMODE},\"bri\":254,\"hue\":13548,\"sat\":200,\"transitiontime\":0}"
curl http://10.0.1.2/api/afHFrFgGgrh6bcOlNJYT45RJX1VW6qx7K8KBRG56/lights/3/state -X PUT -d"{\"on\":${TOGGLEMODE},\"bri\":254,\"hue\":16979,\"sat\":43,\"transitiontime\":50}"
