#!/bin/bash
set -euxo pipefail

if [[ ! -d /home/project/test-logs ]]; then
  mkdir /home/project/test-logs
fi

while IFS= read -r guide
do
  cd /home/project
  rm -fr $guide
  echo testing $guide ...
  git clone https://github.com/openliberty/$guide.git -b $1
  chmod +x $guide/scripts/*.sh
  cd $guide/finish
  if [[ -f ../scripts/testAppSN.sh ]]; then
    ../scripts/testAppSN.sh > /home/project/test-logs/$guide.log 2>&1
  else
    if [[ -f ../scripts/testApp.sh ]]; then
      ../scripts/testApp.sh > /home/project/test-logs/$guide.log 2>&1
    fi
    if [[ -f ../scripts/testAppFinish.sh ]]; then
      ../scripts/testAppFinish.sh > /home/project/test-logs/$guide.finish.log 2>&1
    fi
    if [[ -f ../scripts/testAppStart.sh ]]; then
      ../scripts/testAppStart.sh > /home/project/test-logs/$guide.start.log 2>&1
    fi
  fi
done < /home/project/allGuideIDs.txt
echo tests completed
