#!/bin/bash

echo "storeFile=android-signing-keystore.jks"
echo "storePassword=${SIGNING_KEYSTORE_PASSWORD}" >>buildSrc/keystore.properties
echo "keyAlias=${SIGNING_KEY_ALIAS}" >>buildSrc/keystore.properties
echo "keyPassword=${SIGNING_KEY_PASSWORD}" >>buildSrc/keystore.properties

echo "$SIGNING_JKS_FILE" | base64 -di >android-signing-keystore.jks

printf "ext { \n VERSION_CODE=%d as Integer \n VERSION_SUFFIX=%s as String \n DEBUGGABLE=false }" "${BITBUCKET_BUILD_NUMBER}" "\"${BITBUCKET_DEPLOYMENT_ENVIRONMENT}\"" >>buildSrc/build_properties.gradle
