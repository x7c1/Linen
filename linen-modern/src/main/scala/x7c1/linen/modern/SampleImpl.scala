package x7c1.linen.modern

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.{View, ViewGroup}
import android.widget.{BaseAdapter, Toast}
import x7c1.linen.glue.res.layout.CommentRowLayout
import x7c1.linen.glue.res.values.CommentValues
import x7c1.linen.glue.{LayoutProvider, SampleStruct, ValuesProvider}
import x7c1.wheat.modern.decorator.Imports

class SampleImpl extends SampleStruct {
  override def getFoo(activity: AppCompatActivity): String = "!" + activity.toString
}

class SampleAdapter(
  layoutProvider: LayoutProvider[CommentRowLayout],
  values: ValuesProvider[CommentValues]) extends BaseAdapter {

  import Imports._

  lazy val sampleList = (1 to 100) map { n =>
    new SampleComment(n, s"name-$n", s"comment-$n")
  }
  override def getItemId(i: Int) = sampleList(i).commentId

  override def getCount = sampleList.size

  override def getView(i: Int, view: View, parent: ViewGroup) = {
    val layout = layoutProvider.getOrInflate(view, parent, false)
    val comment = sampleList(i)

    layout.name.text = comment.name
    layout.name onClick
      onClickText(values.get.nameClicked format comment.name)

    layout.content.text = comment.content
    layout.content onClick
      onClickText(values.get.contentClicked format comment.content)

    layout.view
  }

  def onClickText(message: String) = (view: View) => {
    val toast = Toast.makeText(view.context, message, Toast.LENGTH_SHORT )
    toast.show()
    Log.e("SampleAdapter", message)
  }
  override def getItem(i: Int) = sampleList(i)

}

class SampleComment(
  val commentId: Int,
  val name: String,
  val content: String
)

