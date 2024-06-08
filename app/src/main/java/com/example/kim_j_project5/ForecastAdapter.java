package com.example.kim_j_project5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ForecastAdapter extends BaseAdapter {
    private Context context;
    private List<ForecastDetails> weatherList;

    public ForecastAdapter(Context context, List<ForecastDetails> weatherList) {
        this.context = context;
        this.weatherList = weatherList;
    }

    @Override
    public int getCount() {
        return weatherList.size();
    }

    @Override
    public Object getItem(int position) {
        return weatherList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_daily_forecast, parent, false);
        }
        ForecastDetails item = weatherList.get(position);

        TextView text1 = convertView.findViewById(R.id.date_text);
        TextView text2 = convertView.findViewById(R.id.lowest_temp_text);
        TextView text3 = convertView.findViewById(R.id.highest_temp_text);
        TextView text4 = convertView.findViewById(R.id.precipitation_text);

        text1.setText(String.format("Weather on %s", item.getDate()));
        text2.setText(String.format("Lowest Temp: %s", item.getLowestTemp()));
        text3.setText(String.format("Highest Temp: %s", item.getHighestTemp()));
        text4.setText(String.format("Precipitation: %s", item.getPrecipitation()));

        return convertView;
    }
}
