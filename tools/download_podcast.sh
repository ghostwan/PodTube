#!/usr/bin/env bash

adb shell am start -a "android.intent.action.SEND" --es "android.intent.extra.TEXT" "https://www.youtube.com/watch?v=Yr0EGzcS3e8" -t "text/plain"