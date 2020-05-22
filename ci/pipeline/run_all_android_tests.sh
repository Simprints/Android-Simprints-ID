#!/bin/bash
if [ "help" = "$1" ]; then
    echo "Tool to run SID modules' tests on Firebase Test Lab."
    echo "Run tests - ./run_all_android_tests universal.apk module1_name:module1_test_apk ...."
    echo "Stop any background tests - ./run_all_android_tests stop"
    exit 1
fi

if [ "stop" = "$1" ]; then 
	echo "Stopping all gcloud instances...."
	for pid in $(ps -ef | awk '/gcloud/ {print $2}'); do kill -9 $pid; done
	exit 1
fi

clear 

######################################################################
# Config
######################################################################
        
single_runner="run_firebase_test_lab.sh"
device="walleye,version=28,locale=en,orientation=portrait"
n_attemps_per_module="1"
root_path="test-reports"
template_module_log_file="$root_path/@/logs/firebase_logs"

######################################################################


# Initialisations
rm -Rf $root_path
modules=()
main_apk=$1

# Creates a file and all required folders if they don't exist
function mkfile() { 
	mkdir -p $( dirname "$1") && touch "$1" 
}

# Given module_name:apk, extracts module_name
function get_module_name {
    echo $1 | tr ':' $'\n' | sed -n 1p
} 

# Given module_name:apk, extracts apk
function get_module_apk {
    echo $1 | tr ':' $'\n' | sed -n 2p  
} 

# Check if a string($2) is present in a file($1)
function exist_in_file {
	if grep "$2" $1 > /dev/null; then echo 1; else echo 0; fi;
}

# Figures out the state of the FTL instance for a specific module: 
# Uploading/Running/Failed/Completed
function get_test_state {	
	local logs=$(echo $template_module_log_file | sed "s/@/$1/")
	local reports_path=$(echo $root_path/$1)

	if [ -d $reports_path ] && [ $(find $reports_path -name "*xml" | wc -l) -eq 1 ]; then echo "\xE2\x9C\x85 Completed"; return; fi

	local to_search="TEST_AXIS_VALUE"
	local found=$(exist_in_file $logs "$to_search") 
	if [ $found -eq 1 ]; then echo "\xE2\x9C\x85 Storing results"; return; fi

	local to_search="Error"
	local found=$(exist_in_file $logs "$to_search") 
	if [ $found -eq 1 ]; then echo "\xE2\x9D\x8C Failed"; return; fi

	local to_search="Test results will be streamed"
	local found=$(exist_in_file $logs "$to_search") 
	if [ $found -eq 1 ]; then echo "\xE2\x8C\x9B Running"; return; fi

	echo "\xE2\x8C\x9B Uploading"
} 

# Extracts the tests results from the FTL output
function get_test_output {
    local module_logs=$(echo $template_module_log_file | sed "s/@/$1/")

	local to_search="TEST_DETAILS"
	local found=$(exist_in_file $module_logs "$to_search") 
	if [ $found -eq 1 ]; then 
		local output_result=`tail -5 $module_logs | sed -n 4p`
	fi

	echo "# Results: $output_result"
} 

# Extracts Firebase URL with the test results
function get_firebase_url {
    local module_logs=$(echo $template_module_log_file | sed "s/@/$1/")
    local url=`grep -o 'https://console.firebase.google.com/project/simprints-android-ci/.*]' $module_logs | head -n 1 | sed 's/.$//'`
    echo "Firebase: $url"
} 

# Extracts Google Storage URL with the test results
function get_gs_url {
    local module_logs=$(echo $template_module_log_file | sed "s/@/$1/")
    local url=`grep -o 'https://console.developers.google.com/storage/.*/' $module_logs | head -n 1`
    echo "Google Storage: $url"
} 

# Extracts log file for a specific module ($1)
function get_log_file {
	echo $(echo $template_module_log_file | sed "s/@/$module/")
}

function print_state {

	echo "" > $root_path/logs_tmp

	printf "\n\n\n" >> $root_path/logs_tmp
	echo "######################################################################" >> $root_path/logs_tmp
    echo "# Run Android tests on Firebase test lab." >> $root_path/logs_tmp
    echo "######################################################################" >> $root_path/logs_tmp
        
	for module in "${modules_arg[@]}"
    do
    	local name_text="# Module: $module"
    	local log_text="# Log file: $(get_log_file $module)"
    	local state_text="# State: $(get_test_state $module)"
    	local report_text="# Reports:"
    	local report_firebase_text="\t$(get_firebase_url $module)"
    	local report_gs_text="\t$(get_gs_url $module)"
    	local output_text="$(get_test_output $module)"

    	printf "$name_text \n$log_text \n$state_text \n$output_text \n$report_text \n$report_gs_text \n$report_firebase_text \n\n" >> $root_path/logs_tmp
    done	
 	
    if ! diff -q "$root_path/logs" "$root_path/logs_tmp"; then
		clear && printf '\e[3J'
		cp $root_path/logs_tmp $root_path/logs
		cat $root_path/logs
	fi
}

# Prints an overview of the test runners
function monitor {
	local modules_arg=("$@")
	printf "\n\n"
	mkfile "$root_path/logs"

	while sleep 10; do
		print_state $modules_arg	

		local modules_still_running=`cat $root_path/logs | grep -e "Running" -e "Uploading" | wc -l`
		if [ $modules_still_running -le 0 ]
	   	then
			local found_failed=$(exist_in_file $root_path/logs "Failed")
			local found_error=$(exist_in_file $root_path/logs "Error")  
			if [ $found_failed -eq 1 ] || [ $found_error -eq 1 ]; then
				echo "Tests failed!"
				exit 1
			fi 
			
			echo "Tests passed!"
			exit 0
		fi		
	done
}


if [ ! -f $main_apk ]; then
    echo "Main apk doesn't exist"
    exit 1
fi

for arg in "$@"
do
	if [ "$arg" = "$main_apk" ]; then
	    continue
	else
		module=$(get_module_name $arg)
		test_apk=$(get_module_apk $arg)
		modules+=("$module")

		logs_module=$(get_log_file $module)
		mkfile $logs_module
		echo "Processing $arg $logs_module"

		if [ ! -f $test_apk ]; then
    		echo "Test apk $test_apk doesn't exist"
    		exit 1
		else 
	    	nohup ./$single_runner $module $main_apk $test_apk $device $n_attemps_per_module &>/dev/null &
	    fi
	fi
done

monitor "${modules[@]}"
exit 0