package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UnitSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> units;

    public UnitSpinnerAdapter(Context context, List<String> units) {
        super(context, android.R.layout.simple_spinner_item, units);
        this.context = context;
        this.units = units;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // Get the TextView for displaying the unitId
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(units.get(position)); // Set unitId (unit number) as the text

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        // Get the TextView for displaying the unitId in the dropdown view
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(units.get(position)); // Set unitId (unit number) as the text

        return convertView;
    }
}
