import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.macros.reify.HasConstructor

class ReificationTest extends FlatSpecLike with Matchers {

  it can "reify" in {
    SampleImpl.execute()
  }
}

trait SampleAction {
  def execute(parameter: String): Unit
}
case class SampleConfig(
  foo: String,
  bar: Int
)

class HelloAction(config: SampleConfig) extends SampleAction {
  override def execute(parameter: String) = {
    println(s"hello, $parameter")
  }
}
object HelloAction {
  implicit object reify extends HasConstructor[SampleConfig => HelloAction]{
    override def newInstance: SampleConfig => HelloAction = new HelloAction(_)
  }
}

object SampleCaller {
  val config = SampleConfig(
    foo = "foo",
    bar = 123
  )
  def execute[A <: SampleAction]()(implicit i: HasConstructor[SampleConfig => A]) = {
    i newInstance config execute "some parameters"
  }
}
object SampleImpl {
  def execute() = {
    SampleCaller.execute[HelloAction]()
  }
}
