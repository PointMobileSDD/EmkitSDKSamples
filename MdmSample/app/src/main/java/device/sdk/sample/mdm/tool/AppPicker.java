package device.sdk.sample.mdm.tool;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import device.sdk.sample.mdm.R;

public class AppPicker extends ListActivity {
    public static final String EXTRA_REQUESTIING_PERMISSION = "device.sdk.sample.mdm.extra.REQUESTIING_PERMISSION";
    private AppListAdapter mAdapter;
    private String mPermissionName;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPermissionName = getIntent().getStringExtra(EXTRA_REQUESTIING_PERMISSION);
        mAdapter = new AppListAdapter(this);
        if (mAdapter.getCount() <= 0) {
            finish();
        } else {
            setListAdapter(mAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        GotInfo app = mAdapter.getItem(position);
        Intent intent = new Intent();
        if (app.info != null) intent.setAction(app.info.packageName);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class GotInfo {
        ApplicationInfo info;
        CharSequence label;
    }

    public class AppListAdapter extends ArrayAdapter<GotInfo> {
        private final List<GotInfo> mPackageInfoList = new ArrayList<GotInfo>();
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            List<ApplicationInfo> pkgs = context.getPackageManager().getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_DISABLED_COMPONENTS |
                    PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
            for (ApplicationInfo ai : pkgs) {
                if (ai.uid == Process.SYSTEM_UID) {
                    continue;
                }
                if (getPackageName().equals(ai.packageName)) {
                    continue;
                }
                if (mPermissionName != null && !mPermissionName.isEmpty()) {
                    boolean requestsPermission = false;
                    try {
                        PackageInfo pi = getPackageManager().getPackageInfo(ai.packageName, PackageManager.GET_PERMISSIONS);
                        if (pi.requestedPermissions == null) {
                            continue;
                        }
                        for (String requestedPermission : pi.requestedPermissions) {
                            if (requestedPermission.equals(mPermissionName)) {
                                requestsPermission = true;
                                break;
                            }
                        }
                        if (!requestsPermission) {
                            continue;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        continue;
                    }
                } else if ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1 ||
                        (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    continue;
                }

                GotInfo info = new GotInfo();
                info.info = ai;
                info.label = info.info.loadLabel(getPackageManager()).toString();
                mPackageInfoList.add(info);
            }
            Collections.sort(mPackageInfoList, sDisplayNameComparator);
            GotInfo nothing = new GotInfo();
            nothing.label = context.getText(R.string.no_application);
            mPackageInfoList.add(0, nothing);
            addAll(mPackageInfoList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.preference_app, null);
                mInflater.inflate(R.layout.widget_text_views, (ViewGroup) convertView.findViewById(android.R.id.widget_frame));
                holder = new AppViewHolder();
                holder.rootView = convertView;
                holder.appName = (TextView) convertView.findViewById(android.R.id.title);
                holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
                holder.summary = (TextView) convertView.findViewById(R.id.widget_text1);
                holder.disabled = (TextView) convertView.findViewById(R.id.widget_text2);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder)convertView.getTag();
            }
            convertView = holder.rootView;
            GotInfo info = getItem(position);
            holder.appName.setText(info.label);
            if (info.info != null) {
                holder.appIcon.setImageDrawable(info.info.loadIcon(getPackageManager()));
                holder.summary.setText(info.info.packageName);
            } else {
                holder.appIcon.setImageDrawable(null);
                holder.summary.setText("");
            }
            holder.disabled.setVisibility(View.GONE);
            return convertView;
        }

        public class AppViewHolder {
            public View rootView;
            public TextView appName;
            public ImageView appIcon;
            public TextView summary;
            public TextView disabled;
        }
    }

    private final static Comparator<GotInfo> sDisplayNameComparator = new Comparator<GotInfo>() {
        public final int compare(GotInfo a, GotInfo b) {
            return collator.compare(a.label, b.label);
        }
        private final Collator collator = Collator.getInstance();
    };
}