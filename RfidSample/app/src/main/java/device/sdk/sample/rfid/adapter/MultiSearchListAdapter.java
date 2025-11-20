package device.sdk.sample.rfid.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import device.sdk.sample.rfid.MultiSearchSettingsActivity;
import device.sdk.sample.rfid.R;

public class MultiSearchListAdapter extends RecyclerView.Adapter<MultiSearchListAdapter.MultiSearchListHolder> {
    private final String TAG = getClass().getSimpleName();

    private   Context context;
    private ArrayList<MultiSearchSettingsActivity.TagData> tagIdList;

    public MultiSearchListAdapter(Context context, ArrayList<MultiSearchSettingsActivity.TagData> tagIdList) {
        this.context = context;
        this.tagIdList = tagIdList;
    }

    @NonNull
    @Override
    public MultiSearchListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_multi_search_setting, parent, false);
        return new MultiSearchListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiSearchListHolder holder, int position) {
        holder.tvIndex.setText(context.getString(R.string.index, position));
        holder.etTagId.removeTextChangedListener(holder.watcher);
        holder.etTagId.setText(tagIdList.get(position).getTagId());
        Log.d(TAG,"position : " + position + " data : " + tagIdList.get(position));
        holder.etTagId.addTextChangedListener(holder.watcher);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG,"list size : " + tagIdList.size());
        return tagIdList.size();
    }

    public ArrayList<MultiSearchSettingsActivity.TagData> apply() {
        return tagIdList;
    }

    public void clearAll() {
        Log.d(TAG,"clear all");
        for(int i = 0; i < tagIdList.size(); i++) {
            tagIdList.get(i).setTagId("");
            Log.d(TAG,"tagid : " + tagIdList.get(i).getTagId());
        }
        notifyDataSetChanged();
    }

    public class MultiSearchListHolder extends RecyclerView.ViewHolder {

        private TextView tvIndex;
        private EditText etTagId;
        private Button btnClear;

        private int position = 0;

        public MultiSearchListHolder(@NonNull View itemView) {
            super(itemView);

            tvIndex = itemView.findViewById(R.id.tv_index);
            etTagId = itemView.findViewById(R.id.et_tag_id);
            etTagId.setOnFocusChangeListener(onFocusChangeListener);
            etTagId.addTextChangedListener(watcher);
            etTagId.setOnKeyListener(onKeyListener);
            btnClear = itemView.findViewById(R.id.btn_clear);
            btnClear.setOnClickListener(onClickListener);
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_clear) {
                    etTagId.setText("");
                }
            }
        };

        private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId() == R.id.et_tag_id) {
                    if(hasFocus) {
                        position = getAdapterPosition();
                        Log.d(TAG, "onFocus position : " + position);
                    }
                }
            }
        };

        public TextWatcher watcher = new TextWatcher() {

            private String previousString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString() != null) {
                    previousString = s.toString();
                    Log.d(TAG,"position : " + position + " data :" + s.toString());
                    tagIdList.get(position).setTagId(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (etTagId.getLineCount() >= 2) {
                    etTagId.setText(previousString);
                    etTagId.setSelection(etTagId.length());
                }
            }
        };

        private View.OnKeyListener onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER) {

                }
                return false;
            }
        };
    }
}
