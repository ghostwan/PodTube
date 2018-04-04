#!/usr/bin/env bash

adb shell am start -a "android.intent.action.SEND" --es "android.intent.extra.TEXT" "https://www.youtube.com/channel/UC_YDc9mzxrGD6Ug086J_lJw" -t "text/plain"