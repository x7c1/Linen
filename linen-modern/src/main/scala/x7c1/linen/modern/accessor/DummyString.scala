package x7c1.linen.modern.accessor

import scala.util.Random

object DummyString {
  def generateWord: String = {
    val wordRange = 3 to 10
    val wordLength = wordRange(Random.nextInt(wordRange.size - 1))
    Random.alphanumeric.take(wordLength).mkString
  }
  def words(inf: Int, sup: Int): String = {
    val range = inf to sup
    val count = range(Random.nextInt(range.size - 1))

    (0 to count).map(_ => generateWord).mkString(" ")
  }
}
