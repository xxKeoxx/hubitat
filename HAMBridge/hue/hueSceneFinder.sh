#!/bin/bash

BASEURL="http://[add your hue hub ip here]/api/[add your whitelist user]"

if [ ! -d ${GROUPDIR} ]; then
	mkdir -p ${GROUPDIR}
fi

SCENENAME="KitchenDay"

SCENEID=`curl -s ${BASEURL}/scenes -X GET | jq -r -e --arg SCENENAME "${SCENENAME}" '. | keys as $k | $k[] as $myKey |
        if .[$myKey].name == $SCENENAME then
                if (.[$myKey].lastupdated | length) > 0 then
                        "\(.[$myKey].lastupdated),\($myKey),\(.[$myKey].name)"
                        #"\($myKey)"
                else empty
                end
        else empty
        end' | tail -n 1 | awk -F"," '{print $2}'`

echo $SCENEID
