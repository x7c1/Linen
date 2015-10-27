package x7c1.linen.interfaces.res.values;

import android.content.Context;

public class CommentValues {

	private final Context context;

	private final int idNameClicked;
	private String nameClicked;

	private final int idContentClicked;
	private String contentClicked;

	public CommentValues(
			Context context,
			int nameClicked,
			int contentClicked
	) {
		this.context = context;
		this.idNameClicked = nameClicked;
		this.idContentClicked = contentClicked;
	}

	public String nameClicked(){
		if (nameClicked == null){
			nameClicked = context.getResources().getString(idNameClicked);
		}
		return nameClicked;
	}

	public String contentClicked(){
		if (contentClicked == null){
			contentClicked = context.getResources().getString(idContentClicked);
		}
		return contentClicked;
	}
}
