#!/usr/bin/env bash

adb shell am start -a "android.intent.action.SEND" --es "android.intent.extra.TEXT" $1 -t "text/plain"