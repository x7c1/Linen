package x7c1.linen.testing

import java.io.{FileOutputStream, PrintStream}

import org.robolectric.shadows.ShadowLog

trait LogSetting {
  ShadowLog.stream = new PrintStream(
    new FileOutputStream("hoge.log"), true
  )
}
