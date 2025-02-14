package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.apartmentmanageapp.R;

import java.util.List;

public class PropertySpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> properties;

    public PropertySpinnerAdapter(Context context, List<String> properties) {
        super(context, android.R.layout.simple_spinner_item, properties);
        this.context = context;
        this.properties = properties;
    }

    // This is the default view used for Spinner's item when selected
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // Get the TextView for displaying the property name
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(properties.get(position));

        return convertView;
    }

    // This is the view used when the Spinner dropdown list is shown
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        // Get the TextView for displaying the property name
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(properties.get(position));

        return convertView;
    }
}
