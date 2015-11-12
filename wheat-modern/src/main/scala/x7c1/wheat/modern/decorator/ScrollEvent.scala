package x7c1.wheat.modern.decorator

import android.view.View

trait ScrollEvent[A <: View] {
  def targetView: A
  def dx: Int
  def dy: Int
}

object ScrollEvent {
  import scala.language.implicitConversions

  implicit def dumpToString[A <: View](e: ScrollEvent[A]): String =
    s"x:${e.dx}, y:${e.dy}"
}
