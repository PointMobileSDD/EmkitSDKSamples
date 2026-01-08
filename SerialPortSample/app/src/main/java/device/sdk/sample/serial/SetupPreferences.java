package device.sdk.sample.serial;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import device.common.SerialPortFinder;

public class SetupPreferences extends PreferenceActivity {
	private SerialPortFinder mSerialPortFinder;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

		mSerialPortFinder = ((PortApplication) getApplication()).getPortFinder();

		addPreferencesFromResource(R.layout.preferences_setup);

		View contentView = findViewById(android.R.id.content);
		if (contentView != null) {
			ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
				Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
				v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
				return WindowInsetsCompat.CONSUMED;
			});
		}

		// Devices
		final ListPreference devices = (ListPreference)findPreference("DEVICE");
		String[] entries = mSerialPortFinder.getAllDevices();
		String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		devices.setEntries(entries);
		devices.setEntryValues(entryValues);
		devices.setSummary(devices.getValue());
		devices.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				return true;
			}
		});

		// Baud rates
		final ListPreference baudrates = (ListPreference)findPreference("BAUDRATE");
		baudrates.setSummary(baudrates.getValue());
		baudrates.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				return true;
			}
		});
	}
}
