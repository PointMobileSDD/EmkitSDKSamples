package device.sdk.sample.serial;

import java.io.IOException;
import java.security.InvalidParameterException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import device.common.SerialPort;
import device.common.SerialPortFinder;

public class PortApplication extends android.app.Application {
	private SerialPort mSerialPort = null;
	private SerialPortFinder mSerialPortFinder = null;

	public SerialPortFinder getPortFinder() {
		if (mSerialPortFinder == null) {
			mSerialPortFinder = new SerialPortFinder();
		}

		return mSerialPortFinder;
	}

	public void setProfile(String path, int baud) {
		closeSerialPort();
		SharedPreferences sp = getSharedPreferences("device.sdk.sample.serial_preferences", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("DEVICE", path);
		editor.putString("BAUDRATE", String.valueOf(baud));
		editor.commit();
	}

	public SerialPort getSerialPort(boolean flow_control) throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			SharedPreferences sp = getSharedPreferences("device.sdk.sample.serial_preferences", MODE_PRIVATE);
			String path = sp.getString("DEVICE", "");
			int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			mSerialPort = new SerialPort(path, baudrate, 0, flow_control);
		}

		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.closePort();
			mSerialPort = null;
		}
	}
}
