#!/bin/bash

GROUPDIR="/Users/jlrosiak/HAM/hue/groups"
#GROUPID=0
#RETVAL=0
BASEURL="http://10.0.1.2/api/43594f081bb6d23e9ccd254927fa47"

if [ ! -d ${GROUPDIR} ]; then
	mkdir -p ${GROUPDIR}
fi

#if [[ -n $1 ]] && [[ $1 != "--help" ]]; then
#	GROUPID=$1
#else
#	GROUPID=[[PARAM1]]
#fi
#
#if [[ -n $2 ]]; then
#        TOGGLEMODE=$2
#else
#        TOGGLEMODE=[[PARAM2]]
#fi
#
#if [[ -n $3 ]]; then
#	SCENEID=$3
#else
#	SCENEID=[[PARAM3]]
#fi

SCENENAME="KitchenDay"

#curl -s ${BASEURL}/scenes/ | /usr/local/bin/jq -r -e --arg SCENENAME "${SCENENAME}" '. as $object | keys[] | select($object[.].name == $SCENENAME)'
#curl -s ${BASEURL}/scenes/ | /usr/local/bin/jq -r -e --arg SCENENAME "${SCENENAME}" '.' 

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

#| sed -e 's/[:-]//g' | sort | tail -n 1

#GOUPJSON="{\"scene\":\"${SCENEID}\"}"
