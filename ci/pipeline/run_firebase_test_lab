#!/bin/bash
root_path=./test-reports/$1
logs_path=$root_path/logs
log_file=$logs_path/firebase_logs

mkdir -p $root_path
mkdir -p $logs_path

gcloud firebase test android run --type instrumentation --app $2 --test $3 --device model=$4 --timeout 20m --use-orchestrator --num-flaky-test-attempts $5 --verbosity info &> $log_file
cat $log_file | grep -o 'gs:.*/' | head -n 1 |  xargs -I{} gsutil cp "{}**merged.xml" $root_path