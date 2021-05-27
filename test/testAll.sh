#!/bin/bash
set -euxo pipefail
mkdir /home/project/test-logs
while IFS= read -r guide
do
  cd /home/project
  rm -fr $guide
  echo testing $guide ...
  git clone https://github.com/openliberty/$guide.git -b $1
  cd $guide/finish
  if [[ -f ../scripts/testAppSN.sh ]]; then
    chmod +x ../scripts/testAppSN.sh
    ../scripts/testAppSN.sh > /home/project/test-logs/$guide.log
  else
    if [[ -f ../scripts/testApp.sh ]]; then
      chmod +x ../scripts/testApp.sh
      ../scripts/testApp.sh > /home/project/test-logs/$guide.log
    fi
    if [[ -f ../scripts/testAppFinish.sh ]]; then
      chmod +x ../scripts/testAppFinish.sh
      ../scripts/testAppFinish.sh > /home/project/test-logs/$guide.log
    fi
    if [[ -f ../scripts/testAppStart.sh ]]; then
      chmod +x ../scripts/testAppStart.sh
      ../scripts/testAppStart.sh > /home/project/test-logs/$guide.log
    fi
  fi
done < /home/project/allGuideIDs.txt
echo tests completed
