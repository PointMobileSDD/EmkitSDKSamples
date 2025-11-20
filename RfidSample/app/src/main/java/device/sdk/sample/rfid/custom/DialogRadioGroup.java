package device.sdk.sample.rfid.custom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import device.sdk.sample.rfid.R;
import device.sdk.sample.rfid.util.Utils;


public class DialogRadioGroup extends Activity {

	private TextView mTextTitle;
	private RadioGroup mRadioGroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_radiogroup);
		setFinishOnTouchOutside(false);

		mTextTitle = (TextView)findViewById(R.id.tv_title);
		mRadioGroup = (RadioGroup)findViewById(R.id.radio_contents);

		findViewById(R.id.btn_apply).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_cancel).setOnClickListener(mOnClickListener);
		init();
	}

	private void init() {

		Intent intent = getIntent();
		String title = intent.getStringExtra(Utils.EXTRA_TITLE);
		mTextTitle.setText(title);

		int selected;
		if(title.equalsIgnoreCase(getString(R.string.sleep_title)))
			selected = getSelectedSleepIndex();
		else
			selected = intent.getIntExtra(Utils.EXTRA_SELECTED_INT_VALUE, 0);

		String[] contents = intent.getStringArrayExtra(Utils.EXTRA_CONTENT_LIST);
		for(int i = 0; i < contents.length; i++)
		{
			RadioButton radioButton  = new RadioButton(this);
			radioButton.setText(contents[i] );
			radioButton.setTextColor(0xff000000);
			radioButton.setTextSize(16);
			radioButton.setId(i);
			if(i == selected)
				radioButton.setChecked(true);
			mRadioGroup.addView(radioButton);
		}
	}

	private int getSelectedSleepIndex()
	{
		int result = 0;
		String selectedSleep = getIntent().getStringExtra(Utils.EXTRA_SELECTED_STRING_VALUE);
		if(selectedSleep.equals(getString(R.string.sleep_30s)))
			result = 0;
		else if(selectedSleep.equals(getString(R.string.sleep_1m)))
			result = 1;
		else if(selectedSleep.equals(getString(R.string.sleep_5m)))
			result = 2;
		else if(selectedSleep.equals(getString(R.string.sleep_10m)))
			result = 3;
		else if(selectedSleep.equals(getString(R.string.sleep_30m)))
			result = 4;
		else if(selectedSleep.equals(getString(R.string.sleep_1h)))
			result = 5;
		else if(selectedSleep.equals(getString(R.string.sleep_2h)))
			result = 6;
		else
			result = 7;
		return result;
	}

	private String getSelectedSleepString()
	{
		String result = "";
		int selectedIndex = mRadioGroup.getCheckedRadioButtonId();
		if(selectedIndex == 0)
			result = getString(R.string.sleep_30s);
		else if(selectedIndex == 1)
			result = getString(R.string.sleep_1m);
		else if(selectedIndex == 2)
			result = getString(R.string.sleep_5m);
		else if(selectedIndex == 3)
			result = getString(R.string.sleep_10m);
		else if(selectedIndex == 4)
			result = getString(R.string.sleep_30m);
		else if(selectedIndex == 5)
			result = getString(R.string.sleep_1h);
		else if(selectedIndex == 6)
			result = getString(R.string.sleep_2h);
		return result;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(v.getId() == R.id.btn_apply)
			{
				Intent data = new Intent();
				if(mTextTitle.getText().toString().equals(getString(R.string.sleep_title)))
					data.putExtra(Utils.EXTRA_STRING_RESULT, getSelectedSleepString());
				else
					data.putExtra(Utils.EXTRA_INT_RESULT, mRadioGroup.getCheckedRadioButtonId());
				setResult(Activity.RESULT_OK, data);
				finish();
				overridePendingTransition(0, 0);
			}
			else if(v.getId() == R.id.btn_cancel)
			{
				setResult(Activity.RESULT_CANCELED, null);
				finish();
				overridePendingTransition(0, 0);
			}
		}
	};
}
