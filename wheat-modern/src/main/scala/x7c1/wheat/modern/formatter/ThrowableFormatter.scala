package x7c1.wheat.modern.formatter

object ThrowableFormatter {
  def format[A <: Throwable](e: A, depth: Int = 15)(message: String): String = {
    (message +: e.toString +: e.getStackTrace) take depth mkString "\n"
  }
}
