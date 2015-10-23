package x7c1.linen.modern

import android.support.v7.app.AppCompatActivity
import android.view.{View, ViewGroup}
import android.widget.BaseAdapter
import x7c1.linen.interfaces.{CommentRowHolder, SampleStruct, ViewInspector}

class SampleImpl extends SampleStruct {
  override def getFoo(activity: AppCompatActivity): String = "!" + activity.toString
}

class SampleAdapter(inspector: ViewInspector[CommentRowHolder]) extends BaseAdapter {

  lazy val sampleList = (1 to 100) map { n =>
    new SampleComment(n, s"name-$n", s"comment-$n")
  }

  override def getItemId(i: Int) = sampleList(i).commentId

  override def getCount = sampleList.size

  override def getView(i: Int, view: View, parent: ViewGroup) = {
    val holder = inspector.createHolder(view, parent)
    val comment = sampleList(i)
    holder.name.setText(comment.name)
    holder.content.setText(comment.content)
    holder.layout
  }

  override def getItem(i: Int) = sampleList(i)
}

class SampleComment(
  val commentId: Int,
  val name: String,
  val content: String
)
