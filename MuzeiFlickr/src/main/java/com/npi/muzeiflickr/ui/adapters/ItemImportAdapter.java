package com.npi.muzeiflickr.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.npi.muzeiflickr.R;
import com.npi.muzeiflickr.data.ImportableData;

import java.util.ArrayList;
import java.util.List;

public class ItemImportAdapter extends ArrayAdapter<ImportableData> {

    private final List<ImportableData> mItems;
    private Context mContext;
    private List<ImportableData> mCheckedItem = new ArrayList<ImportableData>();

    public ItemImportAdapter(Context context, int textViewResourceId, List<ImportableData> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mItems = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LocalHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.import_item, null);
            holder = new LocalHolder();
            holder.name = (TextView) convertView.findViewById(R.id.text);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.item_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (LocalHolder) convertView.getTag();
        }




        final ImportableData currentItem = mItems.get(position);
        holder.checkbox.setChecked(mCheckedItem.contains(currentItem));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkbox.setChecked(!holder.checkbox.isChecked());
            }
        });


        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedItem.add(currentItem);
                }
            }
        });


        holder.name.setText(currentItem.getName());
        return convertView;
    }

    static class LocalHolder {
        public TextView name;
        public CheckBox checkbox;
    }


    public void setAllChecked(boolean isChecked) {
        if (isChecked) {
            mCheckedItem.clear();
            mCheckedItem.addAll(mItems);
        } else {
            mCheckedItem.clear();
        }
        notifyDataSetChanged();
    }

    public List<ImportableData> getCheckedItems() {
        return mCheckedItem;
    }
}