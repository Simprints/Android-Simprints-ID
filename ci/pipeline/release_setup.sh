#!/bin/bash

echo "$SIGNING_JKS_FILE" | base64 -di >android-signing-keystore.jks

printf "ext { \n
store_file = \"$\rootDir/android-signing-keystore.jks\" \n
store_password = %s \n
key_alias=%s \n
key_password=%s } " "\"${SIGNING_KEYSTORE_PASSWORD}\"" "\"${SIGNING_KEY_ALIAS}\"" "\"${SIGNING_KEY_PASSWORD}\"" >>buildSrc/keystore.properties

printf "ext { \n
VERSION_CODE=%d as Integer \n
VERSION_SUFFIX=%s as String \n
DEBUGGABLE=false }" "${BITBUCKET_BUILD_NUMBER}" "\"${BITBUCKET_DEPLOYMENT_ENVIRONMENT}\"" >>buildSrc/build_properties.gradle
