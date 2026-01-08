package device.sdk.sample.rfid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import device.sdk.sample.rfid.R;

public class BaseActivity extends AppCompatActivity
{
    private final String TAG = getClass().getSimpleName();
    private device.sdk.sample.rfid.RFIDApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupWindowInsets();
    }
    
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setupWindowInsets();
    }
    
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setupWindowInsets();
    }
    
    private void setupWindowInsets() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Try to find the root_layout first, otherwise use content view
        View rootView = findViewById(R.id.root_layout);
        if (rootView == null) {
            rootView = findViewById(android.R.id.content);
        }
        
        if (rootView != null) {
            final View finalRootView = rootView;
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply padding to the root view to avoid overlapping with system bars
                finalRootView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    public device.sdk.sample.rfid.RFIDApplication getRFIDApplication()
    {
        return (device.sdk.sample.rfid.RFIDApplication) getApplication();
    }

    public void showToast(final Context context, final String msg, final boolean isLong)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(context, msg, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showWarning(String title, String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
