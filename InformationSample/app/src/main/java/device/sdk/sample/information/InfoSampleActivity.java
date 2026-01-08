package device.sdk.sample.information;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import device.sdk.Information;

public class InfoSampleActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 1;
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

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        View rootView = findViewById(R.id.root_view);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Android 6.0 이상에서는 런타임 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        getInformations();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getInformations();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                mInfoTextView.setText("권한이 거부되어 정보를 가져올 수 없습니다.");
            }
        }
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
            try {
                mBuffer.append(mInformation.getWifiIpAddress() + "\n");
            } catch (SecurityException e) {
                mBuffer.append("권한 없음 (Permission denied)\n");
            }

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
