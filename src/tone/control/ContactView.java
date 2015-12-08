package tone.control;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactView extends RelativeLayout {

	private Context wholeContext;
	
	public ContactView (Context context, String name, String ringtone, 
			Uri toneUri, long id, int index, ToneControlActivity parent, int layout) {
		super(context);
		
		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT));
		this.setClickable(true);
		this.setBackgroundResource(android.R.drawable.list_selector_background);
		
		wholeContext = context;
		LayoutInflater.from(wholeContext).inflate(layout, this);
		
		((TextView) findViewById(R.id.name)).setText(name);
		
		((TextView) findViewById(R.id.ringtone)).setText(ringtone);
		
		this.name = name;
		this.passParent = parent;
		this.passId = id;
		this.passIndex = index;
		this.passToneUri = toneUri;
		this.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				passParent.setRingtoneAtIndex(passId, passIndex, passToneUri);
			}
		});
	}
	
	public ContactView giveNewVersion(int layout, Uri toneUri, String toneName){
		ContactView view = new ContactView(this.wholeContext, this.name, toneName, 
				toneUri, this.passId, this.passIndex, this.passParent, layout);
		
		return view;
	}
	
	private String name;
	private ToneControlActivity passParent;
	private long passId;
	private int passIndex;
	private Uri passToneUri;
	public void setToneUri (Uri toneUri) {
		this.passToneUri = toneUri;
	}
}
