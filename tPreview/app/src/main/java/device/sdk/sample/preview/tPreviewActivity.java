package device.sdk.sample.preview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;

import device.common.ScanConst;
import device.sdk.ScanManager;

public class tPreviewActivity extends Activity {
    public static final String TAG = "tPreviewActivity";

    private static ScanManager mScanner = null;
    private static int mScannerType = 0;
    private static byte[] mPart = new byte[100000];
    private static byte[] mBuffer = null;
    private static int mRawSize = 0;
    private static int mPreviewWidth = 0;
    private static int mPreviewHeight = 0;

    private static boolean mKeyLock = false;
    private static Preview mPreview = null;
    private static Button mButton = null;
    private static StreamReadTask mReadTask = null;

    private static Context mContext = null;
    private AlertDialog mDialog = null;

    private BroadcastReceiver mScanKeyEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ScanConst.INTENT_SCANKEY_EVENT.equals(intent.getAction())) {
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(ScanConst.EXTRA_SCANKEY_EVENT);
                switch (event.getKeyCode()) {
                    case 262: // KEYCODE_OLD_SCANNER_F for Android 5 SDK
                    case 276: // KEYCODE_OLD_SCANNER_F for Android 6 SDK
                    case 260: // KEYCODE_OLD_SCANNER_R for Android 5 SDK
                    case 277: // KEYCODE_OLD_SCANNER_R for Android 6 SDK
                    case 261: // KEYCODE_OLD_SCANNER_L for Android 5 SDK
                    case 278: // KEYCODE_OLD_SCANNER_L for Android 6 SDK
                    case 263: // KEYCODE_OLD_SCANNER_B for Android 5 SDK
                    case 290: // KEYCODE_OLD_SCANNER_B for Android 6 SDK
                    case 1010:// KEYCODE_SCANNER_F for Unified SDK
                    case 1011:// KEYCODE_SCANNER_R for Unified SDK
                    case 1012:// KEYCODE_SCANNER_L for Unified SDK
                    case 1013:// KEYCODE_SCANNER_B for Unified SDK
                        if (mScanner != null) {
                            if (event.getAction() == KeyEvent.ACTION_DOWN && !mKeyLock) {
                                previewStart();
                            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                                previewStop();
                                try {Thread.sleep(100);} catch (InterruptedException e) {}
                                capture();
                            }
                        }
                        break;
                }
            }
        }
    };

    private void checkPermission(){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try{
            if (grantResults[0] != 0){
                finish();
                return;
            }
        }catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);
        Log.e(TAG, "onCreate()");

        try{
            checkPermission();
        }catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }

        mContext = this;
        mScanner = new ScanManager();
        if(mScanner != null) {
            mScannerType = mScanner.aDecodeGetModuleType();

            switch (mScannerType) {
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N5600:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N5603:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N6603:
                    mPreviewWidth = 416;
                    mPreviewHeight = 320;
                    mRawSize = mPreviewWidth * mPreviewHeight;
                    break;
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N6703:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N3601:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N3603:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N2600:
                    mPreviewWidth = 640;
                    mPreviewHeight = 400;
                    mRawSize = mPreviewWidth * mPreviewHeight;
                    break;
                case ScanConst.ScannerType.DCD_MODULE_TYPE_SE4710:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_SE4750:
                    mRawSize = 256 * 256 + 256;
                    break;
                default:
                    Toast.makeText(mContext,
                            "This device does not support 2D decode function.",
                            Toast.LENGTH_LONG).show();
                    return;
            }
        }
        mBuffer = new byte[mRawSize * 4];

        mPreview = (Preview) findViewById(R.id.preview);
        mButton = (Button) findViewById(R.id.PreviewCaptureButton);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScanner != null) {
                    if (mKeyLock) {
                        previewStop();
                        try {Thread.sleep(100);} catch (InterruptedException e) {}
                        capture();
                    } else {
                        previewStart();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mScanner != null) {
            mScanner.aDecodeSetScanImageMode(ScanConst.ScanMode.DCD_MODE_SCAN);
            mScanner = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        if (mPreview != null) {
            mPreview.resumePreview();
        }
        if (mScanner != null) {
            if (mScanner.aDecodeGetDecodeEnable() == 1) {
                if (getEnableDialog().isShowing()) {
                    getEnableDialog().dismiss();
                }
                mScanner.aDecodeSetScanImageMode(ScanConst.ScanMode.DCD_MODE_IMAGE);
            } else {
                if (!getEnableDialog().isShowing()) {
                    getEnableDialog().show();
                }
            }
        }
        registerReceiver(mScanKeyEventReceiver, new IntentFilter(ScanConst.INTENT_SCANKEY_EVENT));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mScanKeyEventReceiver);
        if (mScanner != null) {
            if (mKeyLock) {
                previewStop();
            }
            mScanner.aDecodeSetScanImageMode(ScanConst.ScanMode.DCD_MODE_SCAN);
        }
        if (mPreview != null) {
            mPreview.pausePreview();
        }
        super.onPause();
    }

    private AlertDialog getEnableDialog() {
        if (mDialog == null) {
            final AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle(R.string.app_name);
            dialog.setMessage("Your scanner is disabled. Do you want to enable it?");
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(ScanConst.LAUNCH_SCAN_SETTING_ACITON);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
            dialog.setCancelable(false);
            mDialog = dialog;
        }
        return mDialog;
    }

    private static void previewStart() {
        if (mScanner != null) {
            mScanner.aDecodeImageStreamInit();
            if (!mScanner.aDecodeImageStreamStart()) {
                Toast.makeText(mContext,
                        "The image stream isn't started.\nPlease try again.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mReadTask = new StreamReadTask();
            mReadTask.execute();
            mButton.setText("Stop/Capture");
            mKeyLock = true;
        }
    }

    private static void previewStop() {
        if (mReadTask != null) {
            mReadTask.stop();
            mReadTask = null;
        }
        if (mScanner != null) {
            mScanner.aDecodeImageStreamStop();
        }
        mButton.setText("Preview");
        mKeyLock = false;
    }

    private static void capture() {
        if (mScanner != null) {
            int size = 0;
            int pos = 0;
            Arrays.fill(mBuffer, (byte) 0);
            do {
                Arrays.fill(mPart, (byte) 0);
                size = mScanner.aDecodeImageCapture(mPart);
                Log.d(TAG, "Partial size : " + size);
                System.arraycopy(mPart, 0, mBuffer, pos, size);
                Log.d(TAG, "Buffer position : " + pos);
                pos += size - 1;
            } while (mPart.length <= size);

            if (pos > 0) {
                saveCapture(mBuffer);
            }
        }
    }

    private static ByteBuffer makeBuffer(byte[] src) {
        byte[] bits = new byte[src.length*4];
        for (int i = 0; i < src.length; i++) {
            bits[i * 4] = (byte) (src[i]);
            bits[i * 4 + 1] = (byte) (src[i]);
            bits[i * 4 + 2] = (byte) (src[i]);
            bits[i * 4 + 3] = (byte) 0xff;
        }
        return ByteBuffer.wrap(bits);
    }

    private static int getDate() {
        int data = 0;
        Calendar cal = Calendar.getInstance();
        data = cal.get(Calendar.YEAR) * 10000;
        data += (cal.get(Calendar.MONTH)+1) * 100;
        data += cal.get(Calendar.DAY_OF_MONTH);
        return data;
    }

    private static int getTime() {
        int time = 0;
        Calendar cal = Calendar.getInstance();
        time = cal.get(Calendar.HOUR_OF_DAY) * 10000;
        time += cal.get(Calendar.MINUTE) * 100;
        time += cal.get(Calendar.SECOND);
        return time;
    }

    private static void saveCapture(byte[] buffer) {
        String folderPath;
        String savePath;
        Bitmap captureImage, rotationImage;
        folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/" + mContext.getString(R.string.app_name);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        savePath = folderPath + "/" + getDate() + "_" + getTime() + ".jpeg";
        try {
            float degrees = 0;
            FileOutputStream out = new FileOutputStream(savePath);
            switch (mScannerType) {
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N5600:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N5603:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N6603:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N6703:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N3601:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_N3603:
                    if (mPreviewWidth == 0 || mPreviewHeight == 0) {
                        Log.e(TAG, "Preview size is zero");
                        return;
                    }
                    captureImage = Bitmap.createBitmap(mPreviewWidth * 2, mPreviewHeight * 2, Bitmap.Config.ARGB_8888);
                    captureImage.copyPixelsFromBuffer(makeBuffer(buffer));
                    break;
                case ScanConst.ScannerType.DCD_MODULE_TYPE_SE4710:
                case ScanConst.ScannerType.DCD_MODULE_TYPE_SE4750:
                    captureImage = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                    if (captureImage == null) {
                        Log.e(TAG, "capture image is null (byte size: " + buffer.length + ")");
                        return;
                    }
                    break;
                default: {
                    Log.e(TAG, "This (" + mScannerType + ") scanner isn't supported on this device.");
                    return;
                }
            }

            captureImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            captureImage.recycle();
            startFileMediaScan(mContext, savePath);
        } catch (FileNotFoundException e) {}
    }

    private static void startFileMediaScan(Context context, String path){
        if (context != null) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        }
    }

    private static class StreamReadTask extends AsyncTask<Void, Void, Void> {
        byte[] mBuffer = new byte[mRawSize];
        int mSize = 0;
        boolean mStop = false;

        public void stop() {
            mPreview.stopPreview();
            mStop = true;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (!mStop) {
                mSize = mScanner.aDecodeImageStreamRead(mBuffer);
                if (mSize != 0) {
                    mPreview.setPreview(mBuffer, mPreviewWidth, mPreviewHeight);
                }
                try {Thread.sleep(25);} catch (InterruptedException e) {}
            }
            return null;
        }
    }
}
