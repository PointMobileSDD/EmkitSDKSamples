package device.sdk.sample.information;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;

import device.sdk.Information;

public class InfoSampleActivity extends Activity {
    private StringBuffer mBuffer;
    private TextView mInfoTextView;

    private Information mInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.information_text);

        mInformation = new Information();
        mBuffer = new StringBuffer();

        getInformations();
    }

    private void getInformations() {
        try {
            mBuffer.append("getHardwareRevision : ");
            mBuffer.append(mInformation.getHardwareRevision() + "\n");

            mBuffer.append("getAndroidVersion : ");
            mBuffer.append(mInformation.getAndroidVersion() + "\n");

            mBuffer.append("getKernelVersion : ");
            mBuffer.append(mInformation.getKernelVersion() + "\n");

            mBuffer.append("getBuildNumber : ");
            mBuffer.append(mInformation.getBuildNumber() + "\n");

            mBuffer.append("getManufacturer : ");
            mBuffer.append(mInformation.getManufacturer() + "\n");

            mBuffer.append("getModelName : ");
            mBuffer.append(mInformation.getModelName() + "\n");

            mBuffer.append("getProcessorInfo : ");
            mBuffer.append(mInformation.getProcessorInfo() + "\n");

            mBuffer.append("getSerialNumber : ");
            mBuffer.append(mInformation.getSerialNumber() + "\n");

            mBuffer.append("getPartNumber : ");
            mBuffer.append(mInformation.getPartNumber() + "\n");

            mBuffer.append("getManufactureDate : ");
            mBuffer.append(mInformation.getManufactureDate() + "\n");

            mBuffer.append("getCameraType : ");
            mBuffer.append(mInformation.getCameraType() + "\n");

            mBuffer.append("getDisplayType : ");
            mBuffer.append(mInformation.getDisplayType() + "\n");

            mBuffer.append("getKeyboardType : ");
            mBuffer.append(mInformation.getKeyboardType() + "\n");

            mBuffer.append("getNandType : ");
            mBuffer.append(mInformation.getNandType() + "\n");

            mBuffer.append("getScannerType : ");
            int scanner = mInformation.getScannerType();
            mBuffer.append(scanner + "\n");

            mBuffer.append("getTouchType : ");
            mBuffer.append(mInformation.getTouchType() + "\n");

            mBuffer.append("getBluetoothType : ");
            mBuffer.append(mInformation.getBluetoothType() + "\n");

            mBuffer.append("getGpsType : ");
            mBuffer.append(mInformation.getGpsType() + "\n");

            mBuffer.append("getPhoneType : ");
            mBuffer.append(mInformation.getPhoneType() + "\n");

            mBuffer.append("getWifiType : ");
            mBuffer.append(mInformation.getWifiType() + "\n");

            mBuffer.append("getSensorAccelerometerType : ");
            mBuffer.append(mInformation.getSensorAccelerometerType() + "\n");

            mBuffer.append("getSensorLightType : ");
            mBuffer.append(mInformation.getSensorLightType() + "\n");

            mBuffer.append("getSensorProximityType : ");
            mBuffer.append(mInformation.getSensorProximityType() + "\n");

            mBuffer.append("getSensorSarType : ");
            mBuffer.append(mInformation.getSensorSarType() + "\n");

            mBuffer.append("getBluetoothDriverVersion : ");
            mBuffer.append(mInformation.getBluetoothDriverVersion() + "\n");

            mBuffer.append("getBluetoothMacAddress : ");
            mBuffer.append(mInformation.getBluetoothMacAddress() + "\n");

            mBuffer.append("getWifiDriverVersion : ");
            mBuffer.append(mInformation.getWifiDriverVersion() + "\n");

            mBuffer.append("getWifiFirmwareVersion : ");
            mBuffer.append(mInformation.getWifiFirmwareVersion() + "\n");

            mBuffer.append("getWifiMacAddress : ");
            mBuffer.append(mInformation.getWifiMacAddress() + "\n");

            mBuffer.append("getWifiIpAddress : ");
            mBuffer.append(mInformation.getWifiIpAddress() + "\n");

            mBuffer.append("getMainBatteryStatus : ");
            mBuffer.append(mInformation.getMainBatteryStatus() + "\n");

            mBuffer.append("getBackupBatteryStatus : ");
            mBuffer.append(mInformation.getBackupBatteryStatus() + "\n");

            mBuffer.append("getBatterySerialNumber : ");
            mBuffer.append(mInformation.getBatterySerialNumber() + "\n");

            mBuffer.append("getChargingMainBatteryFromUsbFlag : ");
            mBuffer.append(mInformation.getChargingMainBatteryFromUsbFlag() + "\n");

            mBuffer.append("getChargingBackupBatteryFromMainBatteryFlag : ");
            mBuffer.append(mInformation.getChargingBackupBatteryFromMainBatteryFlag() + "\n");

            mBuffer.append("getLowBatteryWarningLevel : ");
            mBuffer.append(mInformation.getLowBatteryWarningLevel() + "\n");

            mBuffer.append("getCriticalBatteryWarningLevel : ");
            mBuffer.append(mInformation.getCriticalBatteryWarningLevel() + "\n");

            mBuffer.append("getScannerClass : ");
            mBuffer.append(mInformation.getScannerClass(scanner) + "\n");

            mBuffer.append("getScannerName : ");
            mBuffer.append(mInformation.getScannerName(scanner) + "\n");

            mBuffer.append("getScannerClassName : ");
            mBuffer.append(mInformation.getScannerClassName(scanner));
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            mInfoTextView.setText(mBuffer.toString());
        }
    }

}
