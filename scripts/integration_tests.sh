#!/bin/bash
set -Eeuo pipefail

set_abi=false
set_api_lvl=false
set_api_tgt=false
create_snapshot=false
apk_installed=false
valid_api_lvls=("23" "24" "25" "26" "27" "28" "29" "30" "31" "32" "33")
valid_api_tgts=("default" "google_apis" "google_apis_playstore" "google_atd" "google-tv" \
    "aosp_atd" "android-tv" "android-desktop" "android-wear" "android-wear-cn")
timeout=1800  # default timeout set to 30 minutes

function check_launch() {
    emu_status=$(adb devices | grep "emulator-5554" | cut -f1)
    if [ "${emu_status}" = "emulator-5554" ]; then
        return 0;
    else
        return 1;
    fi
}

function check_boot() {
    boot_status=$(adb -s emulator-5554 shell getprop sys.boot_completed)
    if [ "${boot_status}" = "1" ]; then
        return 0;
    else
        return 1;
    fi
}

function wait_for() {
    timeout=$1
    shift 1
    until [ $timeout -le 0 ] || ("$@" &> /dev/null); do
        sleep 1
        timeout=$(( timeout - 1 ))
    done
    if [ $timeout -le 0 ]; then
        echo -e "\nError: Timeout" >&2
        exit 1
    fi
}

while getopts 'a:l:t:sx:h' OPTION; do
    case "$OPTION" in
        a)
            abi="$OPTARG"
            case "$abi" in
                x86_64)
                    api_lvl_default="30"
                    api_tgt_default="google_apis_playstore"
                    arch="x86_64"
                    ;;
                x86) 
                    api_lvl_default="30"
                    api_tgt_default="google_apis_playstore"
                    arch="x86"
                    ;;
                arm64-v8a)
                    api_lvl_default="30"
                    api_tgt_default="google_apis_playstore"
                    arch="x86_64"
                    ;;
                armeabi-v7a)
                    api_lvl_default="30"
                    api_tgt_default="google_apis_playstore"
                    arch="x86"
                    ;;
                *)
                    echo "Error: Invalid ABI" >&2
                    echo "Try '$(basename $0) -h' for more information." >&2
                    exit 1
                    ;;
            esac
            set_abi=true
            ;;
        l)
            api_lvl="$OPTARG"

            # Check API level is valid
            # tr -d '-' is used to remove all hyphons as they count as word boundaries for grep
            if [[ $(echo ${valid_api_lvls[@]} | tr -d '-' | grep -ow "$(echo ${api_lvl} | tr -d '-')" | wc -w) != 1 ]]; then
                echo "Error: Invalid API level" >&2
                echo "Try '$(basename $0) -h' for more information." >&2
                exit 1
            fi
                        
            set_api_lvl=true
            ;;
        t)
            api_tgt="$OPTARG"

            # Check API target is valid
            # tr -d '-' is used to remove all hyphons as they count as word boundaries for grep
            if [[ $(echo ${valid_api_tgts[@]} | tr -d '-' | grep -ow "$(echo ${api_tgt} | tr -d '-')" | wc -w) != 1 ]]; then
                echo "Error: Invalid API target" >&2
                echo "Try '$(basename $0) -h' for more information." >&2
                exit 1
            fi
                        
            set_api_tgt=true
            ;;
        s)
            create_snapshot=true
            ;;
        x)
            timeout="$OPTARG"
            
            # Check timeout is an integer
            if [ -z "${timeout##*[!0-9]*}" ]; then
                echo "Error: Timeout must be an integer" >&2
                exit 1
            fi
            ;;
        h)
            echo -e "\nRun integration tests. Requires Android Studio cmdline-tools."
            echo -e "\n  -a\t\tSelect ABI (required)"
            echo -e "      \t\t  Options:"
            echo -e "      \t\t  'x86_64' - default system image: API 30 google_apis_playstore x86_64"
            echo -e "      \t\t  'x86' - default system image: API 30 google_apis_playstore x86"
            echo -e "      \t\t  'arm64-v8a' - default system image: API 30 google_apis_playstore x86_64"
            echo -e "      \t\t  'armeabi-v7a' - default system image: API 30 google_apis_playstore x86"
            echo -e "\n  -l\t\tSelect API level (optional)"
            echo -e "      \t\t  Minimum API level: 23"
            echo -e "\n  -t\t\tSelect API target (optional)"
            echo -e "      \t\t  See examples on selecting system images below"
            echo -e "\n  -s\t\tCreate an AVD and snapshot for quick-boot (optional)"
            echo -e "      \t\t  Does not run integration tests"
            echo -e "\n  -x\t\tSet timeout in seconds for emulator launch and AVD boot-up (optional)"
            echo -e "      \t\t  Default: 1800"
            echo -e "      \t\t  Must be an integer"
            echo -e "\nExamples:"
            echo -e "  '$(basename $0) -a x86_64 -s'\tCreates an AVD and quick-boot snapshot for x86_64 ABI"
            echo -e "  '$(basename $0) -a x86_64'   \tRuns integration tests for x86_64 ABI from snapshot"
            echo -e "  '$(basename $0) -a x86 -l 29 -t google_apis'"
            echo -e "                             \t\tSelect system image \"system-images;android-29;google_apis;x86\""
            echo -e "\nRecommended system images for testing ARM ABIs:"
            echo -e "  armeabi-v7a:"
            echo -e "    \"system-images;android-30;google_apis_playstore;x86\" - default"
            echo -e "    \"system-images;android-30;google-tv;x86\""
            # TODO: add list of supported images for arm64-v8a
            echo -e "\nFor a full list of system images run 'sdkmanager --list'"
            exit 1
            ;;
        ?)
            echo "Try '$(basename $0) -h' for more information." >&2
            exit 1
            ;;
    esac
done
if [[ $set_abi == false ]]; then 
    echo "Error: ABI not specified" >&2
    echo "Try '$(basename $0) -h' for more information." >&2
    exit 1
fi

# Set defaults
if [[ $set_api_lvl == false ]]; then
    api_lvl=$api_lvl_default
fi
if [[ $set_api_tgt == false ]]; then
    api_tgt=$api_tgt_default
fi

# Setup working directory
if [ ! -d "./android/app" ]; then
    echo "Error: Incorrect working directory" >&2
    echo "Try './scripts/$(basename $0)' from zingo-mobile root directory." >&2
    exit 1
fi
cd android

echo -e "\nInstalling latest build tools, platform tools, and platform..."
sdkmanager --install 'build-tools;33.0.2' platform-tools

echo "Installing latest emulator..."
sdkmanager --install emulator --channel=0

echo "Installing system image..."
avd_name="android-${api_lvl}_${api_tgt}_${arch}"
sdk="system-images;android-${api_lvl};${api_tgt};${arch}"
sdkmanager --install "${sdk}"
sdkmanager --licenses

# Kill all emulators
../scripts/kill_emulators.sh

if [[ $create_snapshot == true ]]; then
    echo -e "\nCreating AVD..."
    echo no | avdmanager create avd --force --name "${avd_name}" --package "${sdk}"

    echo -e "\n\nWaiting for emulator to launch..."
    emulator -avd "${avd_name}" -netdelay none -netspeed full -no-window -no-audio -gpu swiftshader_indirect -no-boot-anim \
        -no-snapshot-load -port 5554 &> /dev/null &
    wait_for $timeout check_launch
    echo "$(adb devices | grep "emulator-5554" | cut -f1) launch successful"

    echo -e "\nWaiting for AVD to boot..."
    wait_for $timeout check_boot
    echo $(adb -s emulator-5554 emu avd name | head -1)
    echo "Boot completed"
    echo -e "\nSnapshot saved"
else
    echo -e "\nChecking for AVD..."
    if [[ $(emulator -list-avds | grep -ow "${avd_name}" | wc -w) != 1 ]]; then
        echo "AVD not found"
        echo -e "\nCreating AVD..."
        echo no | avdmanager create avd --force --name "${avd_name}" --package "${sdk}"
        echo -e "\n\nTo to create a quick-boot snapshot for faster integration tests use the '-s' flag"
        echo "Try '$(basename $0) -h' for more information."
    else
        echo "AVD found: ${avd_name}"
    fi

    echo -e "\nBuilding APKs..."
    ./gradlew assembleDebug assembleAndroidTest -Psplitapk=true

    # Create integration test report directory
    test_report_dir="app/build/outputs/integration_test_reports/${abi}"
    rm -rf "${test_report_dir}"
    mkdir -p "${test_report_dir}"

    echo -e "\n\nWaiting for emulator to launch..."
    emulator -avd "${avd_name}" -netdelay none -netspeed full -no-window -no-audio -gpu swiftshader_indirect -no-boot-anim \
        -no-snapshot-save -read-only -port 5554 &> "${test_report_dir}/emulator.txt" &
    wait_for $timeout check_launch
    echo "$(adb devices | grep "emulator-5554" | cut -f1) launch successful"

    echo -e "\nWaiting for AVD to boot..."
    wait_for $timeout check_boot
    echo $(adb -s emulator-5554 emu avd name | head -1)
    echo "Boot completed"

    # Disable animations
    adb shell input keyevent 82
    adb shell settings put global window_animation_scale 0.0
    adb shell settings put global transition_animation_scale 0.0
    adb shell settings put global animator_duration_scale 0.0

    echo -e "\nInstalling APKs..."
    i=0
    until [[ $apk_installed == true ]]; do
        if adb -s emulator-5554 install-multi-package -r -t -d --abi "${abi}" \
                "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" \
                "app/build/outputs/apk/debug/app-${abi}-debug.apk" &> "${test_report_dir}/apk_installation.txt"; then
            apk_installed=true
            echo "APK installation succeeded"
        fi              
        if [[ $i -ge 100 ]]; then
            echo "Error: APK installation failed" >&2
            echo "For more information see 'android/${test_report_dir}/apk_installation.txt'" >&2
            exit 1
        fi
        i=$((i+1))
        sleep 1
    done

    # Store emulator info and start logging
    adb -s emulator-5554 shell getprop &> "${test_report_dir}/getprop.txt"
    adb -s emulator-5554 shell cat /proc/meminfo &> "${test_report_dir}/meminfo.txt"
    adb -s emulator-5554 shell cat /proc/cpuinfo &> "${test_report_dir}/cpuinfo.txt"
    adb -s emulator-5554 shell logcat -v threadtime -b main &> "${test_report_dir}/logcat.txt" &

    # Create additional test output directory
    adb -s emulator-5554 shell rm -rf "/sdcard/Android/media/org.ZingoLabs.Zingo/additional_test_output"
    adb -s emulator-5554 shell mkdir -p "/sdcard/Android/media/org.ZingoLabs.Zingo/additional_test_output"

    echo -e "\nRunning integration tests..."
    adb -s emulator-5554 shell am instrument -w -r -e class org.ZingoLabs.Zingo.IntegrationTestSuite \
        -e additionalTestOutputDir /sdcard/Android/media/org.ZingoLabs.Zingo/additional_test_output \
        -e testTimeoutSeconds 31536000 org.ZingoLabs.Zingo.test/androidx.test.runner.AndroidJUnitRunner \
        | tee "${test_report_dir}/test_results.txt"

    # Store additional test outputs
    if [ -n "$(adb -s emulator-5554 shell ls -A /sdcard/Android/media/org.ZingoLabs.Zingo/additional_test_output 2>/dev/null)" ]; then
        adb -s emulator-5554 shell cat /sdcard/Android/media/org.ZingoLabs.Zingo/additional_test_output/* \
            &> "${test_report_dir}/additional_test_output.txt"
    fi

    echo -e "\nTest reports saved: android/${test_report_dir}"
fi

# Kill all emulators
../scripts/kill_emulators.sh

