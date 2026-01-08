package device.sdk.sample.sam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import device.sdk.SamManager;
import device.common.SamIndex;

public class SamSampleActivity extends Activity  implements View.OnClickListener {
	private static final String TAG = SamSampleActivity.class.getSimpleName();

	private static SamManager mSam = null;

	private TextView mAtrStatusTextView;
	private TextView mApduResultTextView;
	private TextView mApduValueTextView;
	private EditText mApduValueEditText;

	int g_nStatus = 0;

	public byte sampleAPDU[]={0x00,/*CLA:class*/
			(byte)0xA4,/*INS:instruction*/
			0x04,/*P1:parameter 1*/
			0x00,/*P2:parameter 2*/
			0x0F,/*Lc:data length*/
			(byte)0x31, (byte)0x50, (byte)0x41, (byte)0x59, (byte)0x2E, (byte)0x53, (byte)0x59, (byte)0x53,
			(byte)0x2E, (byte)0x44, (byte)0x44, (byte)0x46, (byte)0x30, (byte)0x31,   //'1PAY.SYS.DDF01' /*Data*/
			0x00 /*Le:be expected response length*/           // APDU : Select file(AID)
	};

	public final int sampleAPDULength = sampleAPDU.length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.BtnSamATR).setOnClickListener(this);
		findViewById(R.id.BtnSamAPDU).setOnClickListener(this);
		findViewById(R.id.BtnSamPowerDown).setOnClickListener(this);

		mAtrStatusTextView = (TextView) findViewById(R.id.textViewSamAtrValue);
		mAtrStatusTextView.setMovementMethod(new ScrollingMovementMethod());
		mApduResultTextView = (TextView) findViewById(R.id.textViewApduResult);
		mApduResultTextView.setMovementMethod(new ScrollingMovementMethod());
		mApduValueTextView = (TextView) findViewById(R.id.textViewApduValue);
		mApduValueTextView.setMovementMethod(new ScrollingMovementMethod());
		mApduValueEditText = (EditText) findViewById(R.id.edittextWriteData);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        View rootView = findViewById(R.id.root_view);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

		mSam = new SamManager();
		if(mSam != null) {
			int i = 0;
			mSam.setEnabled(true);
		}

		String value = "DATA : ";
		for (int i = 0; i < sampleAPDU.length - 5; i++) {
			value += String.format("%02X ", sampleAPDU[i]);
		}
		mApduValueTextView.setText(value);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_info) {
			openInfo();
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

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.BtnSamATR) {
			int ret = mSam.sendAtrCommand();
			errorDescription(ret, "Power up");
		} else if (id == R.id.BtnSamAPDU) {
			byte data[] = new byte[260];
			byte writeData[] = new byte[260];
			String result = new String();
			int i, ret = -1;
			int recvLength = 0;
			String value = "DATA : ";
			if (mApduValueEditText.length() == 0) {
				for (i = 0; i < sampleAPDU.length - 5; i++) {
					value += String.format("%02X ", sampleAPDU[i]);
				}
				System.arraycopy(sampleAPDU, 0, writeData, 0, sampleAPDULength);
				recvLength = sampleAPDULength;
			}
			else {
				value += mApduValueEditText.getText().toString();
				recvLength = StringToHexData(mApduValueEditText.getText().toString(), writeData) + 5;
			}
			mApduValueTextView.setText(value);

			ret = mSam.sendApduCommand(writeData, recvLength, data, 260);
			recvLength = ret >> 8;
			ret= ret & 0xFF;
			mApduResultTextView.setText(result);
			errorDescription(ret, "APDU");
			if(ret == 0 && recvLength > 0)
			{
				result = "APDU : ";
				for (i = 0; i < recvLength; i++) {
					result += String.format("%02X ", data[i]);
				}
				mApduResultTextView.setText(result);
			}
		} else if (id == R.id.BtnSamPowerDown) {
			int ret = mSam.sendPowerDownCommand();
			errorDescription(ret, "Power down");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSam != null) {
			mSam.setEnabled(false);
		}
	}

	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		super.onResume();
	}

	private void errorDescription(int errCode, String str)
	{
		switch (errCode)
		{
			case SamIndex.TDA8029_ERROR:
				mAtrStatusTextView.setText("ERROR - UNKNOWN ERROR ");
				break;
			case SamIndex.TDA8029_PACKETPATTERNNOK:
				 mAtrStatusTextView.setText("ERROR - TDA8029 Execution Error");
				break;
			case SamIndex.TDA8029_PACKETPATTERNERROR:
				mAtrStatusTextView.setText("ERROR - BAD Packet PATTERN");
				break;
			case SamIndex.TDA8029_PACKETRESBADCMD:
				mAtrStatusTextView.setText("ERROR - Packet RESPONSE : BAD COMMAND");
				break;
			case SamIndex.TDA8029_PACKETBADLENGTH:
				mAtrStatusTextView.setText("ERROR - Packet RESPONSE : BAD LENGTH");
				break;
			case SamIndex.TDA8029_BUFFERTOOSMALL:
				mAtrStatusTextView.setText("ERROR - BUFFER TOO SMALL");
				break;
			case SamIndex.TDA8029_COMMERROR:
				mAtrStatusTextView.setText("ERROR - COMMUNICATION ERROR");
				break;
			case SamIndex.TDA8029_PACKETBADCHECKSUM:
				mAtrStatusTextView.setText("ERROR - Packet BAD CHECKSUM RECEIVED");
				break;
			case SamIndex.TDA8029_OK:
				mAtrStatusTextView.setText("SUCCESS : " + str);
			default:
				break;
		}
	}

	private byte HexData(char data)
	{
		byte ret = (byte)0xff;
		if (0x30 <= data && 0x39 >= data) {
			ret = (byte)(data - 0x30);
		}
		else if (0x41 <= data && 0x46 >= data) {
			ret = (byte)(data - 0x41 + 10);
		}
		else if (0x61 <= data && 0x66 >= data) {
			ret = (byte)(data - 0x61 + 10);
		}
		return ret;
	}

	private int StringToHexData(String data, byte [] writeData)
	{
		int length = 0, charlength = 0;
		int strlength = data.length();
		char temp;
		byte temp2;
		for (int i = 0; i < strlength; i++) {
			temp = data.charAt(i);
			if (temp == ' ') {
				if (charlength != 0)
					length++;
				charlength = 0;
				continue;
			}
			else if (charlength >= 2) {
				charlength = 0;
				length++;
			}

			temp2 = HexData(temp);
			if (temp2 != 0xff) {
				writeData[length] = (byte)((writeData[length] * (charlength * 0x10)) + (temp2 & 0xF));
				charlength++;
			}
		}
		if (charlength != 0)
			length++;

		return length;
	}
}
