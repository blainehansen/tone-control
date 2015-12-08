package tone.control;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

public class ToneControlActivity extends Activity {

	private static final String defaultSilent = "Default ringtone (Silent)";

	private LinearLayout list;
	private Context overallContext;
	
	private Uri defaultToneUri;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		setProgressBarIndeterminate(true);
		setProgressBarVisibility(true);
		
		this.list  = (LinearLayout) findViewById(R.id.list);
		this.overallContext = this.getApplicationContext();

		// Retrieve contacts with name and ringtone.
		ContentResolver cr = getContentResolver();
		Uri contacts = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.CUSTOM_RINGTONE, ContactsContract.Contacts.IN_VISIBLE_GROUP};

		// Get a cursor to iterate over the contacts.
		Cursor c = cr.query(contacts, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		// Determine the column indices of the various database columns.
		int idColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
		int nameColumnIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		int toneColumnIndex = c.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE);
		int visColumnIndex = c.getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP);

		// Declare all the variables we're going to be working with.
		String name;
		Uri toneUri;
		String toneName;
		long id;

		// Grab the default Uri and check if it is null (silent).
		defaultToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		String defaultToneName;
		try {
			defaultToneName = RingtoneManager.getRingtone(this.overallContext,
					defaultToneUri).getTitle(this.overallContext);
		}
		catch (Exception e) {
			defaultToneName = defaultSilent;
		}

		// Iterate over the contacts.
		if (c.moveToFirst()){
			int index = 0;

			do {
				if (c.getInt(visColumnIndex) == 0){
					continue;
				}

				name = c.getString(nameColumnIndex);
				if (name == null){
					name = "Unknown";
					Log.d("name", "There was a null name at " + index);
				}

				try {
					toneUri = Uri.parse(c.getString(toneColumnIndex));

					toneName = RingtoneManager.getRingtone(this.overallContext,
							toneUri).getTitle(this.overallContext);
				}
				catch (Exception e){
					toneUri = defaultToneUri;
					toneName = defaultToneName;
				}

				id = c.getLong(idColumnIndex);

				// We add the views to our list in order.
				int layout = toneUri.equals(defaultToneUri) ? R.layout.contactview: R.layout.differenttone;
				ContactView view = new ContactView(this.list.getContext(), name, toneName, 
						toneUri, id, index, this, layout);

				this.list.addView(view, index);
				index++;
			}
			while (c.moveToNext());
		}
		setProgressBarVisibility(false);
	}

	public void setRingtoneAtIndex(long id, int index, Uri toneUri){
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, toneUri);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);

		this.personId = id;
		this.personIndex = index;

		this.startActivityForResult(intent, 0);
		return;
	}

	private long personId;
	private int personIndex;

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if (resultCode == Activity.RESULT_OK){
			// Yank out the data passed by the initial call.
			Uri toneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			int index = personIndex;
			long id = personId;

			String tonePath;
			String toneName;
			try {
				tonePath = toneUri.toString();
				toneName = RingtoneManager.getRingtone(this.overallContext, toneUri).getTitle(this.overallContext);
			}
			catch (Exception e) {
				tonePath = null;
				toneName = defaultSilent;
			}

			// Update the ringtone.
			ContentValues values = new ContentValues(1);
			values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, tonePath);
			getContentResolver().update(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id),
					values, "", new String[]{});

			// Change the view to reflect.
			int layout = toneUri.equals(defaultToneUri) ? R.layout.contactview: R.layout.differenttone;
			ContactView view = (ContactView) this.list.getChildAt(index);
			this.list.removeViewAt(index);
			this.list.addView(view.giveNewVersion(layout, toneUri, toneName), index);
			
			super.onActivityResult(requestCode, resultCode, data);
		}
		return;
	}
}