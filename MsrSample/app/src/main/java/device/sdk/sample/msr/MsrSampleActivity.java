package device.sdk.sample.msr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MsrSampleActivity extends Activity {
	private static final String TAG = MsrSampleActivity.class.getSimpleName();

	private static final int MSG_CHANGE_INFORMATION_UI = 1000;
	private static final int MSG_CHANGE_RESULT_TRACK1 = 1001;
	private static final int MSG_CHANGE_RESULT_TRACK2 = 1002;
	private static final int MSG_CHANGE_RESULT_TRACK3 = 1003;
	private static final int MSG_CHANGE_RESULT_RESULT = 1004;
	private static final int MSG_CHANGE_REPEAT = 1005;

	private static final int READER_READ_START = 1;
	private static final int READER_READ_STOP = 2;

	private MsrSampleManager mMsrSampleManager = null;

	private boolean mIsTriggered = false;

	private Button mStartReadButton = null;
	private TextView mResultTextView = null;
	private TextView mStatusTextView = null;
	private TextView mTrack1View = null;
	private TextView mTrack2View = null;
	private TextView mTrack3View = null;
	private CheckBox mAutoScanModeCheck = null;
	ProgressDialog mProgressDialog = null; 

	private int mtrack1ReadStatus = 0;
	private int mtrack2ReadStatus = 0;
	private int mtrack3ReadStatus = 0;
	private int mError = 0;

	private final Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			String str = (String) msg.obj;
			int status = (int) msg.arg1;
			switch (msg.what) {
				case MSG_CHANGE_INFORMATION_UI:
					mStatusTextView.setText(str);
					mtrack1ReadStatus = (status >> 8) & 1;
					mtrack2ReadStatus = (status >> 8) & 2;
					mtrack3ReadStatus = (status >> 8) & 4;
					mError = status & 0xFF;
					mMsrSampleManager.GetResult();
					if (mAutoScanModeCheck.isChecked()) {
						if (mIsTriggered) {
							mMsrSampleManager.SetRepeat();
						}
					} else {
						mIsTriggered = false;
						mStartReadButton.setEnabled(true);
						mStartReadButton.setText(R.string.btn_read_start);
					}
					break;
				case MSG_CHANGE_RESULT_TRACK1:
					if (mError != 0) {
						mTrack1View.setText("");
					}
					else if (mtrack1ReadStatus != 0) {
						mTrack1View.setText("[Read fail]");
					}
					else if (str.length() == 0) {
						mTrack1View.setText("[No Data]");
					}
					else {
						mTrack1View.setText(str);
					}
					break;
				case MSG_CHANGE_RESULT_TRACK2:
					if (mError != 0) {
						mTrack2View.setText("");
					}
					else if (mtrack2ReadStatus != 0) {
						mTrack2View.setText("[Read fail]");
					}
					else if (str.length() == 0) {
						mTrack2View.setText("[No Data]");
					}
					else {
						mTrack2View.setText(str);
					}
					break;
				case MSG_CHANGE_RESULT_TRACK3:
					if (mError != 0) {
						mTrack3View.setText("");
					}
					else if (mtrack3ReadStatus != 0) {
						mTrack3View.setText("[Read fail]");
					}
					else if (str.length() == 0) {
						mTrack3View.setText("[No Data]");
					}
					else {
						mTrack3View.setText(str);
					}
					break;
				case MSG_CHANGE_RESULT_RESULT:
					mResultTextView.setText(str);
					break;
				case MSG_CHANGE_REPEAT:
					new Reader(READER_READ_START).execute();
					str = mStatusTextView.getText().toString();
					mStatusTextView.setText(str + "  /  Repeating...");
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMsrSampleManager = new MsrSampleManager(this);

		mStatusTextView = (TextView) findViewById(R.id.textViewMsrStatus);
		mTrack1View = (TextView) findViewById(R.id.textViewMsrTrack1);
		mTrack2View = (TextView) findViewById(R.id.textViewMsrTrack2);
		mTrack3View = (TextView) findViewById(R.id.textViewMsrTrack3);
		mResultTextView = (TextView) findViewById(R.id.textViewMsrResult);
		mAutoScanModeCheck = (CheckBox) findViewById(R.id.check_auto_scan);
		mStartReadButton = (Button) findViewById(R.id.button_card_reader);
		
		mStartReadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsTriggered = !mIsTriggered;
				if (mIsTriggered) {
					mMsrSampleManager.clearResult();
					mStartReadButton.setText(R.string.btn_read_stop);
					new Reader(READER_READ_START).execute();
					mStatusTextView.setText("Reading...");
					mTrack1View.setText("");
					mTrack2View.setText("");
					mTrack3View.setText("");
					mResultTextView.setText("");
				} else {
					mStartReadButton.setText(R.string.btn_read_start);
					new Reader(READER_READ_STOP).execute();
					mStatusTextView.setText("Stopping...");
				}
			}
		});

		MsrSampleManager.setListner(new MsrSampleManager.EventListener() {
			@Override
			public int printCardInformation(String result, int state) {
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_INFORMATION_UI, state, 0, result));
				return 0;
			}

			public int setResult(String track1, String track2, String track3, String result) {
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_RESULT_TRACK1, 1, 0, track1));
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_RESULT_TRACK2, 2, 0, track2));
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_RESULT_TRACK3, 3, 0, track3));
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_RESULT_RESULT, 4, 0, result));
				return 0;
			}
			
			public int setDelayedRepeat(int delay) {
				mUIHandler.sendMessage(Message.obtain(mUIHandler, MSG_CHANGE_REPEAT, delay, 0, null));
				return 0;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAutoScanModeCheck.setChecked(false);
		if (mIsTriggered) {
			if (mMsrSampleManager != null)
				mMsrSampleManager.ReadStop();
		}
	}

	@Override
	protected void onDestroy() {
		if (mMsrSampleManager != null) {
			try {
				mMsrSampleManager.PowerDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}

	private class Reader extends AsyncTask<Void, Void, Void> {
		int mFlag = 0;

		public Reader(int flag) {
			mFlag = flag;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(MsrSampleActivity.this, "", getString(R.string.wait_msg));;
		};

		@Override
		protected Void doInBackground(Void... params) {
			switch (mFlag) {
				/* Not used yet. */
				case READER_READ_START:
					mMsrSampleManager.ReadStart();
					break;
				case READER_READ_STOP:
					mMsrSampleManager.ReadStop();
					break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	/* Application version information */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_info) {
			openInfo();
			return true;
		} else if (id == R.id.action_encryption) {
			mMsrSampleManager.setUsedEncryption();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openInfo() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		String version = getString(R.string.msg_version_suffix);
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			if (pi != null) {
				version = pi.versionName;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		alert.setMessage(getString(R.string.app_name) + " v" + version);
		alert.show();
	}
}
