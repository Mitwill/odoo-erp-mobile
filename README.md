To create build the apk from command line:


Run the following command to build APK file 

->    Test App
      To create Test app :
            ./gradlew assembleDebug
      To install Test APK directly to connected device:
            ./gradlew installDebug

->    Prod App
      To create Prod App:
            ./gradlew assembleRelease
      To install Prod APK directly to connected device:
            ./gradlew installRelease

->    Local App
      To create Local App:
            ./gradlew assembleLocal
      To install Local APK directly to connected device:
            ./gradlew installLocal


=> Increse the build version for the relese automatically with following 
gradle command.

./gradlew bumperVersionMajor
./gradlew bumperVersionMinor
./gradlew bumperVersionPatch 


=> API Endpoint Methods of this application are as follows:

./web/webclient/version_info 
./web/session/get_session_info 
./web/session/authenticate 
./web/dataset/search_read 
./web/dataset/exec_workflow 
./web/dataset/call_kw/+model+/read 
./web/dataset/call_kw

