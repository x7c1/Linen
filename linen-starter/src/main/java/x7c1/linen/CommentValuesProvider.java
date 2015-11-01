package x7c1.linen;

import android.content.Context;

import x7c1.wheat.ancient.resource.ValuesProvider;
import x7c1.linen.glue.res.values.CommentValues;

public class CommentValuesProvider implements ValuesProvider<CommentValues>{
	private final CommentValues comment;

	public CommentValuesProvider(Context context) {
		comment = new CommentValuesImpl(
				context,
				R.string.comment__name_clicked,
				R.string.comment__content_clicked
				);
	}

	@Override
	public CommentValues get() {
		return comment;
	}

	private static class CommentValuesImpl implements CommentValues {

		private final Context context;

		private final int idNameClicked;
		private String nameClicked;

		private final int idContentClicked;
		private String contentClicked;

		CommentValuesImpl(
				Context context,
				int nameClicked,
				int contentClicked
		) {
			this.context = context;
			this.idNameClicked = nameClicked;
			this.idContentClicked = contentClicked;
		}

		@Override
		public String nameClicked(){
			if (nameClicked == null){
				nameClicked = context.getResources().getString(idNameClicked);
			}
			return nameClicked;
		}

		@Override
		public String contentClicked(){
			if (contentClicked == null){
				contentClicked = context.getResources().getString(idContentClicked);
			}
			return contentClicked;
		}
	}
}
