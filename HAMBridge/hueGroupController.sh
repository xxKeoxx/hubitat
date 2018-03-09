#!/bin/bash

BASEURL="http://[add your hue hub ip here]/api/[add your whitelist user]"

function get_scene_id {
    if [[ "${GROUPSTATE}" == "${TOGGLEMODE}" ]]; then
        echo "GROUPSTATE and TOGGLEMODE are the same. Nothing will be done"
        echo "${GROUPSTATE} = ${TOGGLEMODE}"
        exit 0
    fi
    if [[ -n ${SCENENAME} ]] && [[ ! `echo ${SCENENAME} | grep PARAM` ]]; then
        SCENEID=`curl -s ${BASEURL}/scenes -X GET | /usr/local/bin/jq -r -e --arg SCENENAME "${SCENENAME}" '. | keys as $k | $k[] as $myKey |
            if .[$myKey].name == $SCENENAME then
               if (.[$myKey].lastupdated | length) > 0 then
                   "\(.[$myKey].lastupdated),\($myKey),\(.[$myKey].name)"
                  #"\($myKey)"
               else empty
               end
            else empty
        end' | tail -n 1 | awk -F"," '{print $2}'`
        echo "SCENEID = $SCENEID"
    fi
}

if [ ! -d ${GROUPDIR} ]; then
	mkdir -p ${GROUPDIR}
fi

if [[ -n $1 ]]; then
	ROOMNAME=$1
else
	ROOMNAME=[[PARAM1]]
fi

if [[ -n $2 ]]; then
        TOGGLEMODE=$2
else
        TOGGLEMODE=[[PARAM2]]
fi

if [[ -n $3 ]]; then
	SCENENAME=$3
else
	SCENENAME=[[PARAM3]]
fi

ROOMNAME=`echo $ROOMNAME | sed -e 's/_/ /g'`
SCENENAME=`echo ${SCENENAME} | sed -e 's/_/ /g'`
echo "ROOMNAME = ${ROOMNAME}"
echo "TOGGLEMODE = ${TOGGLEMODE}"
echo "SCENENAME = ${SCENENAME}"

if [[ -n ${ROOMNAME} ]] && [[ ! `echo ${ROOMNAME} | grep PARAM` ]]; then
        GROUPID=`curl -s ${BASEURL}/groups/ | /usr/local/bin/jq -r -e --arg ROOMNAME "${ROOMNAME}" '. as $object | keys[] | select($object[.].name == $ROOMNAME )'`
	GROUPSTATE=`curl -s ${BASEURL}/groups/${GROUPID}/ | /usr/local/bin/jq -r -e .state.all_on`
	echo "GROUPSTATE = ${GROUPSTATE} (true = all on, false = all off)"
	echo "GROUPID = ${GROUPID}"
fi

case ${TOGGLEMODE} in
	'on') TOGGLEMODE="true";
	      get_scene_id;
              JSONDATA="{\"scene\":\"${SCENEID}\"}";;
        'off') TOGGLEMODE="false";
	       get_scene_id;
               JSONDATA="{\"on\":${TOGGLEMODE}}";;
        *) echo "Only on and off are valid"; exit 100;;
esac

echo "JSONDATA = ${JSONDATA}"
curl -s ${BASEURL}/groups/${GROUPID}/action -X PUT -d"${JSONDATA}"
