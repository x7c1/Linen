package x7c1.wheat.build

import sbt.InputKey
import sbt.complete.Parser

object WheatTasks {
  def settings = Seq(
    InputKey[Unit]("generate-layout") := LayoutGenerator.task.evaluated
  )
}

object WheatParser {
  import sbt.complete.DefaultParsers._

  def exclusiveParser(items: Seq[String]): Parser[Seq[String]] = {
    val base = items match {
      case Nil => failure("item not remain")
      case _ => items.map(token(_)).reduce(_ | _)
    }
    val recurse = (Space ~> base) flatMap { item =>
      val (consumed, remains) = items.partition(_ == item)
      exclusiveParser(remains) map { input => consumed ++ input }
    }
    recurse ?? Nil
  }
}
