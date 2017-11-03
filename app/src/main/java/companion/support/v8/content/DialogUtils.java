package companion.support.v8.content;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

/**
 * Utility class to create simple dialogs.
 * 
 * @author Vitor Ribeiro
 */
public class DialogUtils {

	/** This prevents the class from being instantiated. 
	 */
	private DialogUtils() {
	}

	public static final DialogInterface.OnClickListener DO_NOTHING = new DialogInterface.OnClickListener() { 
		public void onClick(DialogInterface dialog, int which) {
		}
	};

	/** Create a dialog. 
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param description message, or null if no message is to be displayed.
	 * @param iconRes icon resource, or null if no icon is to be displayed.
	 * @param onNegativeListener negative callback.
	 * @param negativeButtonText negative button text, or null if this button is not to be displayed.
	 * @param onNeutralListener neutral callback.
	 * @param neutralButtonText neutral button text, or null if this button is not to be displayed.
	 * @param onPositiveListener positive callback.
	 * @param positiveButtonText positive button text, or null if this button is not to be displayed.
	 * @return dialog object.
	 */
	public static AlertDialog createDialog(
		Context finalContext, String title, String description, Integer iconRes,
		DialogInterface.OnClickListener onNegativeListener, String negativeButtonText, 
		DialogInterface.OnClickListener onNeutralListener, String neutralButtonText, 
		DialogInterface.OnClickListener onPositiveListener, String positiveButtonText
	) {
				
		Builder builder = new AlertDialog.Builder(finalContext);

		if (title!=null) {
			builder.setTitle(title);
		}
		if (description!=null) {
			builder.setMessage(description);
		}
		if (iconRes!=null) {
			builder.setIcon(iconRes);
		}
		if (negativeButtonText!=null) {
			builder.setNegativeButton(negativeButtonText, onNegativeListener);
		}
		if (neutralButtonText!=null) {
			builder.setNeutralButton(neutralButtonText, onNeutralListener);	
		}
		if (positiveButtonText!=null) {
			builder.setPositiveButton(positiveButtonText, onPositiveListener);
		}

		return builder.create();
	}

	/** Create a simple dialog with a neutral ok button.
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param description message, or null if no message is to be displayed.
	 * @param buttonListener button callback.
	 * @return dialog object.
	 */
	public static AlertDialog createSimpleDialog(
		Context finalContext, String title, String description, 
		DialogInterface.OnClickListener buttonListener
	) {

		return createDialog(
			finalContext, title, description, null, DO_NOTHING, null, 
			buttonListener, finalContext.getString(android.R.string.ok), DO_NOTHING, null
		);
	}

	/** Create an error dialog.
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param description message, or null if no message is to be displayed.
	 * @return dialog object.
	 */
	public static AlertDialog createErrorDialog(Context finalContext, String title, String description) {
		return createSimpleDialog(finalContext, title, description, DO_NOTHING);
	}

	/** Create an informative dialog.
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param description message, or null if no message is to be displayed.
	 * @return dialog object.
	 */
	public static AlertDialog createInfoDialog(Context finalContext, String title, String description) {
		return createSimpleDialog(finalContext, title, description, DO_NOTHING);
	}

	/** Create a dialog with a list of items.
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param list the item list.
	 * @param onPositiveListener positive callback.
	 * @param onNegativeListener negative callback.
	 * @return dialog object.
	 */
	public static <T> AlertDialog createListDialog(
		Context finalContext, String title,
		List<T> list, final DialogInterface.OnClickListener onPositiveListener,
		final DialogInterface.OnClickListener onNegativeListener
	) {
		final ArrayAdapter<T> adapter = new ArrayAdapter<T>(finalContext, android.R.layout.select_dialog_item, list);
		Builder builder = new AlertDialog.Builder(finalContext)
		.setAdapter(adapter, onPositiveListener)
		.setNegativeButton(android.R.string.cancel, onNegativeListener);

		if (title!=null) {
			builder.setTitle(title);
		}

		return builder.create();
	}

	/** Create a dialog with a list of items.
	 * @param finalContext caller's context.
	 * @param title message, or null if no title is to be displayed.
	 * @param list the item list.
	 * @param onPositiveListener positive callback.
	 * @return dialog object.
	 */
	public static <T> AlertDialog createListDialog(
		Context finalContext, String title, 
		List<T> list, final DialogInterface.OnClickListener onPositiveListener
	) {
		return createListDialog(finalContext, title, list, onPositiveListener, DO_NOTHING);
	}

	/** Create a dialog attached to a view.
	 * @param finalContext caller's context.
	 * @param title message.
	 * @param layout view object.
	 * @param onPositiveListener positive callback.
	 * @return dialog object.
	 */
	public static AlertDialog createFrameDialog(
		Activity finalContext, String title, View layout,
		DialogInterface.OnClickListener onPositiveListener
	) {
		final FrameLayout frame = new FrameLayout(finalContext);

		frame.addView(layout, new FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.MATCH_PARENT,
			FrameLayout.LayoutParams.WRAP_CONTENT)
		);

		Builder builder = new AlertDialog.Builder(finalContext)
		.setTitle(title)
		.setView(frame)
		.setCancelable(true)
		.setNegativeButton(android.R.string.cancel, DO_NOTHING)
		.setPositiveButton(android.R.string.ok, onPositiveListener);

		return builder.create();
	}
}
