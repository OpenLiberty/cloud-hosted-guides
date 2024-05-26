#!/bin/bash
# Script to deploy guides to AWB

LAB_ID=$1
OAUTH_TOKEN=$2
FILE_PATH=$3
CHANGE_LOG=$4
PUBLISH=$5
API_URL=$6

response=$(curl -s -X POST "${API_URL}/api/v1/labs/${LAB_ID}/lab_versions/latest/drafts" \
  -H "Authorization: Bearer ${OAUTH_TOKEN}" \
  -F "file=@${FILE_PATH}" \
  -F "draft[changelog]=${CHANGE_LOG}" \
  -F "publish=${PUBLISH}")

if [[ "$response" -ne 200 && "$response" -ne 201 ]]; then
  echo "Request failed with status $response"
  exit 1
else
  echo "Request succeeded."
fi