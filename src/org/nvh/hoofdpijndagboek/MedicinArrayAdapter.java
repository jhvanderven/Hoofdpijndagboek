package org.nvh.hoofdpijndagboek;

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
		name.setText(Utils.parseDescription(values.get(position), this.context.getString(R.string.pill_name)));
		number.setText(Utils.parseDescription(values.get(position), this.context.getString(R.string.pill_number)));
		effect.setText(Utils.parseDescription(values.get(position), this.context.getString(R.string.pill_effect)));
		when.setText(Utils.parseDescription(values.get(position), this.context.getString(R.string.pill_timing)));
		String[] effects = this.context.getResources().getStringArray(R.array.pill_effect_array);
		if(effect.getText().equals(effects[0])){
			image.setImageResource(R.drawable.weet_niet);
		}else if(effect.getText().equals(effects[1])){
			image.setImageResource(R.drawable.minder);
		}else if(effect.getText().equals(effects[2])){
			image.setImageResource(R.drawable.over_wit);
		}else if(effect.getText().equals(effects[3])){
			image.setImageResource(R.drawable.erger);
		}else if(effect.getText().equals(effects[4])){
			image.setImageResource(R.drawable.geen);
		}
		return rowView;
	}
}
