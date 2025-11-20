package device.sdk.sample.serial;

import java.io.IOException;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import device.common.SerialPort;

public abstract class BaseActivity extends Activity {
	private static final String TAG = BaseActivity.class.getSimpleName();

	private SerialPort mSerialPort;
	private ReadThread mReadThread;
	private boolean mReadLine = false;
	private boolean mHWFlowControl = false;

	protected abstract void onDataReceived(final String buffer);

	protected boolean writeData(int oneByte) {
		if (mSerialPort == null) {
			return false;
		}
		try {
			byte[] b = {(byte) (oneByte & 0xFF)};;
			mSerialPort.write(b);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected boolean writeData(String buffer) {
		if (mSerialPort == null || buffer == null || buffer.isEmpty()) {
			return false;
		}
		try {
			mSerialPort.write(buffer.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void setToReadLine() {
		mReadLine = true;
	}

	protected void setHWFlowControl() {
		mHWFlowControl = true;
	}

	protected void initSerialPort() {
		try {
			mSerialPort = ((PortApplication) getApplication()).getSerialPort(mHWFlowControl);
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
	}

	protected void closeSerialPort() {
		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
		((PortApplication) getApplication()).closeSerialPort();
		mSerialPort = null;
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			int ret = 0;
			byte[] bytes = new byte[1024];
			StringBuffer buffer = new StringBuffer();
			buffer.setLength(0);
			while (ret >= 0) {
				if (mSerialPort == null) break;
				try {
					ret = mSerialPort.read(bytes);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				if (ret > 0) {
					buffer.append(new String(bytes));
					if (mReadLine) {
						int carriage = buffer.lastIndexOf("\r");
						int line = buffer.lastIndexOf("\n");
						if (line != -1) {
							onDataReceived(buffer.substring(0, line));
							buffer.delete(0, line + 1);
						} else if (carriage != -1) {
							onDataReceived(buffer.substring(0, carriage));
							buffer.delete(0, carriage + 1);
						}
					} else {
						if (buffer.length() > 0) {
							onDataReceived(buffer.toString());
							buffer.setLength(0);
						}
					}
				}
			}
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				BaseActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	public void onResume() {
		Log.e(TAG, "onResume");
		super.onResume();
		initSerialPort();
	}

	@Override
	public void onPause() {
		Log.e(TAG, "onPause");
		closeSerialPort();
		super.onPause();
	}
}
