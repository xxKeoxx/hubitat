#!/bin/bash
  
GROUPDIR="/var/log/HAM/hue/groups"
BASEURL="http://[add you ip]/api/[add your whitelist user]"

if [ ! -d ${GROUPDIR} ]; then
        mkdir -p ${GROUPDIR}
fi

if [[ -n $1 ]] && [[ $1 != "--help" ]]; then
        GROUPID=$1
else
        GROUPID=[[PARAM1]]
fi

if [[ -n $2 ]]; then
        TOGGLEMODE=$2
else
        TOGGLEMODE=[[PARAM2]]
fi

if [[ -n $3 ]]; then
        SCENEID=$3
else
        SCENEID=[[PARAM3]]
fi

echo ${TOGGLEMODE} >> /var/log/HAM/hueGroupController.log
echo ${GROUPID} >> /var/log/HAM/hueGroupController.log
echo ${SCENEID} >> /var/log/HAM/hueGroupController.log

if [[ `echo ${GROUPID} | grep PARAM` ]]; then
        curl -s ${BASEURL}/groups/ | jq -r -e '. | keys as $k | $k[] as $myKey |
                if .[$myKey].name | startswith("fwActive")
                        then empty
                else if .[$myKey].type == "Room"
                        then "\($myKey):\(.[$myKey].name):\([.[$myKey].lights[]])"
                else empty
                end
        end' | sed 's/ /_/g' | tee ${GROUPDIR}/groupids.hue
else
        GROUPSTATE=`curl -s ${BASEURL}/groups/${GROUPID}/ | jq -r -e .state.all_on`
fi

echo ${GROUPSTATE} >> /var/log/HAM/hueGroupController.log

if [[ ! `echo ${TOGGLEMODE} | grep PARAM` ]]; then
        case ${TOGGLEMODE} in
                'on') TOGGLEMODE="true";
                      JSONDATA="{\"scene\":\"${SCENEID}\"}";;
                'off') TOGGLEMODE="false";
                       JSONDATA="{\"on\":${TOGGLEMODE}}";;
                *) echo "Only on and off are valid"; exit 100;;
        esac
        if [[ ${GROUPSTATE} != ${TOGGLEMODE} ]]; then
                curl -s ${BASEURL}/groups/${GROUPID}/action -X PUT -d"${JSONDATA}"
        fi
fi
