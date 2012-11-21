package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MedicinArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;

	public MedicinArrayAdapter(Context context, List<String> items) {
		super(context, R.layout.medicin_row, items);
		this.context = context;
		this.values = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.medicin_row, parent, false);
		ImageView image = (ImageView) rowView.findViewById(R.id.pill_image);
		TextView name = (TextView) rowView.findViewById(R.id.pill_name);
		TextView number = (TextView) rowView.findViewById(R.id.pill_count);
		TextView effect = (TextView) rowView.findViewById(R.id.pill_effect);
		TextView when = (TextView) rowView.findViewById(R.id.pill_when);
		TextView whenDiff = (TextView) rowView
				.findViewById(R.id.pill_when_diff);
		name.setText(Utils.parseDescription(values.get(position),
				this.context.getString(R.string.pill_name)));
		number.setText(Utils.parseDescription(values.get(position),
				this.context.getString(R.string.pill_number)));
		effect.setText(Utils.parseDescription(values.get(position),
				this.context.getString(R.string.pill_effect)));
		String pill_time = Utils.parseDescription(values.get(position),
				this.context.getString(R.string.pill_timing));
		Date d;
		try {
			d = Utils.parse(pill_time,
					this.context.getString(R.string.very_long_date_time));
			when.setText(String.format("%02d:%02d", d.getHours(), d.getMinutes()));
			Calendar s = ((MainActivity) getContext()).getAttack().start;
			Calendar e = ((MainActivity) getContext()).getAttack().end;
			long msStart = s.getTimeInMillis();
			long msPill = d.getTime();
			long msEnd = e.getTimeInMillis();
			double diff;
			String wanneer;
			if(msPill < msStart){
				diff = msPill - msStart;
				wanneer = "%.0f %s voor de aanval";
			}else if (msPill < msEnd) {
				diff = msPill - msStart;
				wanneer = "na %.0f %s gedurende de aanval";
			} else {
				diff = msEnd - msPill;
				wanneer = "%.0f %s na de aanval";
			}
			diff=Math.abs(diff);
			double diffMinutes = diff / (60 * 1000);
			double diffHours = diff / (60 * 60 * 1000);
			double diffDays = diff / (24 * 60 * 60 * 1000);
			String diffTimeString="";
			if (diffDays > 1) {
				diffTimeString = String.format(wanneer, diffDays, "dagen");
			} else if (diffHours > 1) {
				diffTimeString = String.format(wanneer, diffHours, "uur");
			} else {
				diffTimeString = String.format(wanneer, diffMinutes, "min");
			}
			whenDiff.setText(diffTimeString);
		} catch (ParseException e) {
		}
		String[] effects = this.context.getResources().getStringArray(
				R.array.pill_effect_array);
		if (effect.getText().equals(effects[0])) {
			image.setImageResource(R.drawable.weet_niet);
		} else if (effect.getText().equals(effects[1])) {
			image.setImageResource(R.drawable.minder);
		} else if (effect.getText().equals(effects[2])) {
			image.setImageResource(R.drawable.over_wit);
		} else if (effect.getText().equals(effects[3])) {
			image.setImageResource(R.drawable.erger);
		} else if (effect.getText().equals(effects[4])) {
			image.setImageResource(R.drawable.geen);
		}
		return rowView;
	}
}
