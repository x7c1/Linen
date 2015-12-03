package x7c1.linen.modern.accessor

import scala.util.Random

object DummyString {
  def word: String = {
    val wordRange = 3 to 10
    val wordLength = wordRange(Random.nextInt(wordRange.size - 1))
    Random.alphanumeric.take(wordLength).mkString
  }
  def words(n: Int): String = {
    (0 to n).map(_ => word).mkString(" ")
  }
}
