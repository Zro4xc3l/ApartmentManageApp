package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class AmenitySpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> amenities;

    public AmenitySpinnerAdapter(Context context, List<String> amenities) {
        super(context, android.R.layout.simple_spinner_item, amenities);
        this.context = context;
        this.amenities = amenities;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(amenities.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(amenities.get(position));
        return convertView;
    }
}
