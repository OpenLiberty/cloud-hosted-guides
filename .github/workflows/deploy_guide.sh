#!/bin/bash
# Script to deploy guides to AWB

LAB_ID=$1
OAUTH_TOKEN=$2
FILE_PATH=$3
CHANGE_LOG=$4
PUBLISH=$5
API_URL=$6

response=$(curl -s -w "%{http_code}" -o response.json -X POST "${API_URL}/api/v1/labs/${LAB_ID}/lab_versions/latest/drafts" \
  -H "Authorization: Bearer ${OAUTH_TOKEN}" \
  -F "file=@${FILE_PATH}" \
  -F "draft[changelog]=${CHANGE_LOG}" \
  -F "publish=${PUBLISH}")

http_status=$(tail -n1 <<< "$response")
response_body=$(head -n-1 response.json)

if [[ "$http_status" -ne 200 && "$http_status" -ne 201 ]]; then
  echo "Request failed with status $http_status"
  echo "Response Body: $response_body"
  exit 1
else
  echo "Request succeeded with status $http_status"
fi

rm response.json
