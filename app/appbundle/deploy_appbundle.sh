cd ~/ASProjectsForInterview/NavApp/app/appbundle || exit

bundletool='bundletool-all-1.3.0.jar'
file_aab='../release/app-release.aab'
#file_aab="$HOME/ASProjectsForInterview/NavApp/app/build/outputs/bundle/release/app-release.aab"
file_apks='pkg.apks'

key_store_path='../navapp.jks'
key_store_password='navapp'
key_alias='key0'
key_password='navapp'

file_apks_spec_extract='pkg_spec_extract'
device_spec='device_spec.json'

java -jar $bundletool get-device-spec --output=$device_spec

java -jar $bundletool build-apks --bundle=$file_aab --output=$file_apks --ks=$key_store_path --ks-pass=pass:$key_store_password --ks-key-alias=$key_alias --key-pass=pass:$key_password

java -jar $bundletool extract-apks --apks=$file_apks --output-dir=$file_apks_spec_extract --device-spec=$device_spec

java -jar $bundletool install-apks --apks=$file_apks
