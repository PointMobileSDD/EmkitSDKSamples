package device.sdk.sample.serial;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

public class ConsoleActivity extends BaseActivity {
	private static final String TAG = ConsoleActivity.class.getSimpleName();

	private static final int MAX_LINES = 200;
	private static ScrollView mScroll;
	private static LinearLayout mLayout;
	private EditText mInput;
	private static Runnable mRunnableScroll = new Runnable() {
		@Override
		public void run() {
			mScroll.fullScroll(ScrollView.FOCUS_DOWN);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
		
		setContentView(R.layout.activity_console);

		mScroll = (ScrollView) findViewById(R.id.scrollReception);
		mLayout = (LinearLayout) findViewById(R.id.layoutReception);
		mInput = (EditText) findViewById(R.id.editEmission);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

		View rootView = findViewById(R.id.root_layout);
		ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
			Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
			return WindowInsetsCompat.CONSUMED;
		});

		((Button) findViewById(R.id.button_send)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				writeData(mInput.getText().toString());
			}
		});
		// setHWFlowControl();
	}

	@Override
	protected void onDataReceived(final String buffer) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mLayout.getChildCount() > MAX_LINES) {
					mLayout.removeViewAt(0);
				}

				TextView tv = new TextView(getBaseContext());
				tv.setText(buffer);
				mLayout.addView(tv);
				mScroll.post(mRunnableScroll);
			}
		});
	}
}
