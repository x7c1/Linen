
#usage
#  sh linen-install.sh debug x7c1.linen MainActivity

output=`adb devices -l`
lines=`echo "$output" | sed -n '2,$p'`
message=`echo "$output" | head -1`

echo $message
echo "$lines" | awk '{print "[" NR - 1 "]", $0}'

if test ${#lines[@]} -eq 1 ; then
  number=0
else
  /bin/echo -n "input target number : "
  read number
fi

PRE_IFS=$IFS
IFS=$'\n'
devices=(`echo "$lines" | awk '{print $1}'`)
device=${devices[$number]}
IFS=$PRE_IFS

echo "device ($device)" selected

#create jar of scala project
sbt ";linen-pickle/assembly;linen-modern/assembly"

#create apk for debug-mode
./gradlew --daemon --parallel assembleDebug

#install apk to a connected device
adb -s $device install -r ./linen-starter/build/outputs/apk/linen-starter-$1.apk

#start an activity
adb -s $device shell am start -n $2/$2.$3
