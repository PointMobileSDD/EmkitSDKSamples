package device.sdk.sample.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.util.Random;

public class PreviewThread implements Runnable {
	private static final String TAG = "PreviewThread";
	private static final boolean DEBUG = false;
	boolean onStart, onPause;
	private int canvasWidth, canvasHeight;
	private Paint mBackPaint;
	private Paint mTextPaint;
	private RectF mBackRect;
	private Bitmap mPreviewImage;
	private SurfaceHolder mSurfaceHolder;
	private Random mRandom;

	Thread mThread;

	public PreviewThread(SurfaceHolder surfaceHolder) {
		Log.e(TAG, "PreviewThread(SurfaceHolder)");
		mSurfaceHolder = surfaceHolder;
		mBackPaint = new Paint();
		mBackPaint.setAntiAlias(true);
		mBackPaint.setARGB(255, 255, 255, 255);
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setARGB(255, 0, 0, 0);
		mTextPaint.setTextSize(30);
		mBackRect = new RectF(0, 0, 0, 0);
		mRandom = new Random();
	}

	public void setSurfaceSize(int width, int height) {
		synchronized (mSurfaceHolder) {
			canvasWidth = width;
			canvasHeight = height;
			mBackRect.set(0, 0, canvasWidth, canvasHeight);
		}
	}

	private static ByteBuffer makeBuffer(byte[] src) {
		byte[] bits = new byte[src.length * 4];
		for (int i = 0; i < src.length; i++) {
			bits[i * 4] = src[i];
			bits[i * 4 + 1] = src[i];
			bits[i * 4 + 2] = src[i];
			bits[i * 4 + 3] = (byte) 0xff;
		}
		return ByteBuffer.wrap(bits);
	}

	public void setPreview(byte[] data, int width, int height) {
		synchronized (mSurfaceHolder) {
			if (width == 0 || height == 0) {
				mPreviewImage = BitmapFactory.decodeByteArray(data, 0, data.length);
			} else {
				if (mPreviewImage == null) {
					mPreviewImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				}
				mPreviewImage.copyPixelsFromBuffer(makeBuffer(data));
			}
		}
	}

	public void start() {
		onStart = true;
		mThread = new Thread(this);
		mThread.start();
	}

	public void stop() {
		onStart = false;
		resume();
		if (mPreviewImage != null) {
			mPreviewImage.recycle();
			mPreviewImage = null;
		}
	}

	public void pause() {
		onPause = true;
	}

	public void resume() {
		onPause = false;
		try {
			synchronized (this) {
				notifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (onStart) {
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				doDraw(c);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null)
					mSurfaceHolder.unlockCanvasAndPost(c);

				if (!onStart)
					break;

				if (onPause) {
					try {
						synchronized (this) {
							wait();
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}

				if (!onStart)
					break;
			}
		}
	}

	public void doDraw(Canvas canvas) {
		synchronized (mSurfaceHolder) {
			if (canvas != null) {
				if (DEBUG) {
					mBackPaint.setARGB(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
					canvas.drawRect(mBackRect, mBackPaint);
				}
				if (mPreviewImage != null) {
					int oriWidth = mPreviewImage.getWidth();
					int oriHeight = mPreviewImage.getHeight();
					int drawLeft = 0;
					int drawRight = 0;
					int drawTop = 0;
					int drawBottom = 0;
					float rate = 0;
					if((float)((float)oriWidth/(float)oriHeight) < (float)((float)canvasWidth/(float)canvasHeight)) { // by height
						rate = (float)canvasHeight/(float)oriHeight;
						drawRight = (int)(oriWidth * rate);
						drawLeft = (canvasWidth-drawRight) / 2;
						drawRight += drawLeft;
						drawBottom = canvasHeight;
						drawTop = 0;
					}
					else {
						rate = (float)canvasWidth/(float)oriWidth;
						drawRight = canvasWidth;
						drawLeft = 0;
						drawBottom = (int)(oriHeight * rate);
						drawTop = (canvasHeight-drawBottom) / 2;
						drawBottom += drawTop;
					}
					canvas.drawBitmap(mPreviewImage,
                                null,
                                new Rect(drawLeft, drawTop, drawRight, drawBottom),
                                null);
				}
			}
		}
	}
}
