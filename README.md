# Linen
Experiment to build android app by gradle including jars assembled on sbt.

## Project structure

This project is classified into two kinds, linen and wheat.

### Linen

Application layer.

 * linen-starter
   * starts application, generates apk file, manages resources handled by Android SDK.
 * linen-glue
   * defines interfaces and classes written in Java to be called from Scala projects like `linen-modern`.
 * linen-repository
   * provides modules for data sources mainly around SQLite.
 * linen-scene
   * provides modules shared with entries in linen-modern, which require linen-repository, linen-glue, wheat-lore.
 * linen-modern
   * includes Scala entry files depending on linen-scene.
 * linen-pickle
   * preserves jars which are not changed usually such as Scala standard library.

### Wheat

Library layer, which is independent from concrete application.

 * wheat-build
   * provides some useful sbt tasks like generating Java sources from layout XML.
 * wheat-modern
   * Scala library which provides, for instance, some decorators to hide redundant Java API.
 * wheat-ancient
   * Java library dependent on Android SDK and Scala codes using it.
 * wheat-lore
   * provides modules which require wheat-modern and wheat-ancient.
 * wheat-macros
   * Compile-time library to avoid reflections on runtime.
 * wheat-calendar
   * Substitutes for hairy java.util.Calendar

## Tips

* ./gradlew --daemon --parallel assembleDebug
* adb -s ${device} uninstall x7c1.linen
* sbt ";wheat-build/publishLocal;reload;wheat:layoutLocations"
* sbt "wheat:generateLayout comment_row.xml"

## License

This repository is published under the MIT License.

 * https://github.com/x7c1/Linen/blob/master/LICENSE
