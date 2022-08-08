#!/usr/bin/env bash
sh ./gradlew app:assembleDebug

#将Android Library生成aar供其它业务使用
#sh ./gradlew libcommon:assembleRelease

#sh ./gradlew app:bundleRelease