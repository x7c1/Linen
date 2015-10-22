
#usage
#  sh android-install.sh starter-debug x7c1.linen MainActivity

output=`adb devices -l`
lines=`echo "$output" | sed -n '2,$p'`
message=`echo "$output" | head -1`

echo $message
echo "$lines" | awk '{print "[" NR - 1 "]", $0}'

/bin/echo -n "input target number : "
read number

PRE_IFS=$IFS
IFS=$'\n'
devices=(`echo "$lines" | awk '{print $1}'`)
device=${devices[$number]}
IFS=$PRE_IFS

echo "device ($device)" selected

#create jar of scala project
sbt ";pickle/assembly;modern/assembly"

#create apk for debug-mode
./gradlew --daemon --parallel assembleDebug

#install apk to a connected device
adb -s $device install -r ./starter/build/outputs/apk/$1.apk

#start an activity
adb -s $device shell am start -n $2/$2.$3

