#!/usr/bin/env bash

adb shell am start -a "android.intent.action.SEND" --es "android.intent.extra.TEXT" "https://www.youtube.com/watch?v=6v2L2UGZJAM" -t "text/plain"