# Linen
Experiment to build android app by gradle including jars assembled on sbt.

## Project structure

This project is classified into two kinds, linen and wheat.

### Linen

Application layer.

 * linen-starter
   * starts application, generate apk file, manage resources handled by Android SDK.
 * linen-glue
   * defines interfaces and classes written in Java to be called from Scala projects like `linen-modern`.
 * linen-modern
   * includes Scala files depending on linen-glue.
 * linen-pickle
   * preserve jars which is not changed usually such as Scala standard library.

### Wheat

Library layer, which is independent from concrete application.

 * wheat-build
   * provides some useful tasks like generating Java sources from layout XML.
 * wheat-modern
   * Scala library which provides, for instance, some decorators to hide redundant Java API.
 * wheat-ancient
   * Java library called by Java projects like `linen-glue`.

## License

This repository is published under the MIT License.

 * https://github.com/x7c1/Linen/blob/master/LICENSE
