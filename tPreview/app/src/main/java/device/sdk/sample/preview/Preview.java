package device.sdk.sample.preview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	private PreviewThread mPreview;

	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e(TAG, "Preview(Context, AttributeSet)");
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mPreview = new PreviewThread(holder);
		setFocusable(true);
	}

	public void stopPreview() {
		// mPreview.saveCapture();
	}

	public void setPreview(byte[] data, int width, int height) {
		mPreview.setPreview(data, width, height);
	}

	public void pausePreview() {
		mPreview.pause();
	}

	public void resumePreview() {
		mPreview.resume();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mPreview.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mPreview.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mPreview.stop();
	}

}
