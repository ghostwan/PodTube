#!/usr/bin/env bash

adb shell am start -a "android.intent.action.SEND" --es "android.intent.extra.TEXT" "https://www.youtube.com/watch?v=fy3UB7_wQ3Y" -t "text/plain"