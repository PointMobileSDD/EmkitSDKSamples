package device.sdk.sample.mdm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
//import android.content.pm.IPackageDeleteObserver; // Deprecated !!!
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import device.sdk.Control;
import device.sdk.sample.mdm.tool.AppPicker;

public class DevelopmentPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_MOCK_LOCATION_APP = "mock_location_app";
    private static final String KEY_UNINSTALL_BLOCK = "uninstall_block";
    private static final String KEY_UNINSTALL_APP = "uninstall_app";
    private static final String KEY_HIDE_APP = "hide_app";
    private static final String KEY_DISALBE_APP = "disable_app";
    private static final String PREF_DISABLED_APP = "pref_disabled_app";
    private static final int RESULT_MOCK_LOCATION_APP = 1001;
    private static final int RESULT_UNINSTALL_BLOCK = 1002;
    private static final int RESULT_UNINSTALL_APP = 1003;
    private static final int RESULT_HIDE_APP = 1004;
    private static final int RESULT_DISABLE_APP = 1005;
    private static final int UNINSTALL_COMPLETE = 1;
    private SwitchPreference mKeepScreenOn;
    private Preference mMockLocationApp;
    private Preference mUninstallBlock;
    private Preference mUninstallApp;
    private Preference mHideApp;
    private Preference mDisableApp;

    private static final Control mControl = new Control();

    /* // Deprecated !!!
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UNINSTALL_COMPLETE:
                    String packageName = " " + (String) msg.obj;
                    String status = "";
                    switch (msg.arg1) {
                        case Control.DELETE_SUCCEEDED: // PackageManager.DELETE_SUCCEEDED
                            status = "SUCCEEDED";
                            break;
                        case Control.DELETE_FAILED_INTERNAL_ERROR: // PackageManager.DELETE_FAILED_INTERNAL_ERROR
                            status = "FAILED_INTERNAL_ERROR";
                            break;
                        case Control.DELETE_FAILED_DEVICE_POLICY_MANAGER: // PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER
                            status = "FAILED_DEVICE_POLICY_MANAGER";
                            break;
                        case Control.DELETE_FAILED_USER_RESTRICTED: // PackageManager.DELETE_FAILED_USER_RESTRICTED
                            status = "FAILED_USER_RESTRICTED";
                            break;
                        case Control.DELETE_FAILED_OWNER_BLOCKED: // PackageManager.DELETE_FAILED_OWNER_BLOCKED
                            status = "FAILED_OWNER_BLOCKED";
                            break;
                        case Control.DELETE_FAILED_ABORTED: // PackageManager.DELETE_FAILED_ABORTED
                            status = "FAILED_ABORTED";
                            break;
                        default:
                            break;
                    }
                    Toast.makeText(getActivity(), status + packageName, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            mHandler.sendMessage(msg);
        }
    }
    */

    private void updateMockLocation() {
        String packageName = mControl.getMockLocationApp();
        if (!TextUtils.isEmpty(packageName)) {
            String label = packageName;
            try {
                ApplicationInfo ai = getContext().getPackageManager().getApplicationInfo(packageName, 0);
                CharSequence appLabel = getContext().getPackageManager().getApplicationLabel(ai);
                if (appLabel != null) {
                    label = appLabel.toString();
                }
            } catch (PackageManager.NameNotFoundException e) {
                /* ignore */
            }
            mMockLocationApp.setSummary(getString(R.string.pref_summary_mock_location_app_set, label));
        } else {
            mMockLocationApp.setSummary(getString(R.string.pref_summary_mock_location_app_not_set));
        }
    }

    private void bindPreferenceChange() {
        /* KEY_KEEP_SCREEN_ON */
        mKeepScreenOn = findPreference(KEY_KEEP_SCREEN_ON);
        mKeepScreenOn.setChecked(mControl.isEnabledStayAwake());
        mKeepScreenOn.setOnPreferenceChangeListener(this);
        /* KEY_MOCK_LOCATION_APP */
        mMockLocationApp = findPreference(KEY_MOCK_LOCATION_APP);
        mMockLocationApp.setOnPreferenceClickListener(this);
        updateMockLocation();
        /* KEY_UNINSTALL_BLOCK */
        mUninstallBlock = findPreference(KEY_UNINSTALL_BLOCK);
        mUninstallBlock.setOnPreferenceClickListener(this);
        /* KEY_UNINSTALL_APP */
        mUninstallApp = findPreference(KEY_UNINSTALL_APP);
        mUninstallApp.setOnPreferenceClickListener(this);
        /* KEY_HIDE_APP */
        mHideApp = findPreference(KEY_HIDE_APP);
        mHideApp.setOnPreferenceClickListener(this);
        /* KEY_DISALBE_APP */
        mDisableApp = findPreference(KEY_DISALBE_APP);
        mDisableApp.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        if (KEY_KEEP_SCREEN_ON.equals(key)) {
            mControl.setEnabledStayAwake((Boolean) value);
        }
        return result;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mMockLocationApp) {
            Intent intent = new Intent(getActivity(), AppPicker.class);
            intent.putExtra(AppPicker.EXTRA_REQUESTIING_PERMISSION, "android.permission.ACCESS_MOCK_LOCATION");
            startActivityForResult(intent, RESULT_MOCK_LOCATION_APP);
        } else if (preference == mUninstallBlock) {
            Intent intent = new Intent(getActivity(), AppPicker.class);
            startActivityForResult(intent, RESULT_UNINSTALL_BLOCK);
        } else if (preference == mUninstallApp) {
            Intent intent = new Intent(getActivity(), AppPicker.class);
            startActivityForResult(intent, RESULT_UNINSTALL_APP);
        } else if (preference == mHideApp) {
            Intent intent = new Intent(getActivity(), AppPicker.class);
            startActivityForResult(intent, RESULT_HIDE_APP);
        } else if (preference == mDisableApp) {
            Intent intent = new Intent(getActivity(), AppPicker.class);
            startActivityForResult(intent, RESULT_DISABLE_APP);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_MOCK_LOCATION_APP:
                if (resultCode == Activity.RESULT_OK) {
                    String packageName = data.getAction();
                    if (packageName != null && !packageName.isEmpty()) {
                        mControl.setMockLocationApp(packageName);
                        updateMockLocation();
                    }
                }
                break;
            case RESULT_UNINSTALL_BLOCK:
                if (resultCode == Activity.RESULT_OK) {
                    String packageName = data.getAction();
                    if (packageName != null && !packageName.isEmpty()) {
                        boolean enable = !mControl.isUninstallBlocked(packageName);
                        mControl.setUninstallBlocked(packageName, enable);
                        String state = "Uninstaller " + (enable ? "locked" : "is unlocked") + " for " + packageName;
                        Toast.makeText(getActivity(), state, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_UNINSTALL_APP:
                if (resultCode == Activity.RESULT_OK) {
                    String packageName = data.getAction();
                    if (packageName != null && !packageName.isEmpty()) {
                        // mControl.deletePackage(packageName, new PackageDeleteObserver(), Control.DELETE_ALL_USERS); // Deprecated !!!
                        mControl.deletePackage(packageName, Control.DELETE_ALL_USERS);
                    }
                }
                break;
            case RESULT_HIDE_APP:
                if (resultCode == Activity.RESULT_OK) {
                    String packageName = data.getAction();
                    if (packageName != null && !packageName.isEmpty()) {
                        boolean enable = !mControl.isApplicationHidden(packageName);
                        mControl.setApplicationHidden(packageName, enable);
                        String state = packageName + " is " + (enable ? "hidden" : "shown");
                        Toast.makeText(getActivity(), state, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_DISABLE_APP:
                if (resultCode == Activity.RESULT_OK) {
                    String packageName = data.getAction();
                    if (packageName != null && !packageName.isEmpty()) {
                        String state = packageName + " is ";
                        SharedPreferences pref = getActivity().getPreferences(getActivity().MODE_PRIVATE);
                        Set<String> packages = pref.getStringSet(PREF_DISABLED_APP, new HashSet<String>());
                        if (packages.contains(packageName)) {
                            mControl.setApplicationEnabledSetting(packageName, Control.COMPONENT_ENABLED_STATE_DEFAULT);
                            packages.remove(packageName);
                            state += "enabled";
                        } else {
                            mControl.setApplicationEnabledSetting(packageName, Control.COMPONENT_ENABLED_STATE_DISABLED);
                            packages.add(packageName);
                            state += "disabled";
                        }
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.putStringSet(PREF_DISABLED_APP, packages);
                        editor.commit();
                        Toast.makeText(getActivity(), state, Toast.LENGTH_LONG).show();
                    }
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_development);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindPreferenceChange();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), MdmSampleActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
