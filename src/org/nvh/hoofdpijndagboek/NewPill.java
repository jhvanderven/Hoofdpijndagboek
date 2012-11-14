package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class NewPill extends SherlockDialogFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		@SuppressWarnings("unused")
		View v = inflater.inflate(R.layout.enter_pill, container, false);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
