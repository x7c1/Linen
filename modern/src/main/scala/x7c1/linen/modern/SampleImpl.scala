package x7c1.linen.modern

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.OnClickListener
import android.view.{View, ViewGroup}
import android.widget.{BaseAdapter, TextView, Toast}
import x7c1.linen.interfaces.res.CommentRowLayout
import x7c1.linen.interfaces.{SampleStruct, LayoutProvider}
import x7c1.linen.modern.decorator.{RichTextView, RichListenableView}

class SampleImpl extends SampleStruct {
  override def getFoo(activity: AppCompatActivity): String = "!" + activity.toString
}

class SampleAdapter(inspector: LayoutProvider[CommentRowLayout]) extends BaseAdapter {
  import Imports._

  lazy val sampleList = (1 to 100) map { n =>
    new SampleComment(n, s"name-$n", s"comment-$n")
  }
  override def getItemId(i: Int) = sampleList(i).commentId

  override def getCount = sampleList.size

  override def getView(i: Int, view: View, parent: ViewGroup) = {
    val layout = inspector.getOrInflate(view, parent, false)
    val comment = sampleList(i)

    layout.name.text = comment.name
    layout.name onClick onClickText(comment.name)

    layout.content.text = comment.content
    layout.content onClick onClickText(comment.content)
    layout.view
  }

  def onClickText(value: String) = (view: View) => {
    val toast = Toast.makeText(view.context,
      s"clicked [$value]", Toast.LENGTH_SHORT )

    toast.show()
    Log.e("SampleAdapter", s"clicked [$value]")
  }
  override def getItem(i: Int) = sampleList(i)

}

class SampleComment(
  val commentId: Int,
  val name: String,
  val content: String
)

package decorator {

  class RichListenableView[A <: View](view: A){

    def context: Context = view.getContext

    def onClick[B](f: A => B) = view.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = f(view.asInstanceOf[A])
    })
  }
  class RichTextView[A <: TextView](view: A){

    def text: CharSequence = view.getText

    def text_=(value: String): Unit = view.setText(value)
  }
}

object Imports {
  import scala.language.implicitConversions

  implicit def toRichListenableView[A <: View](view: A): RichListenableView[A]
    = new RichListenableView(view)

  implicit def toRichTextView[A <: TextView](view: A): RichTextView[A]
    = new RichTextView(view)
}