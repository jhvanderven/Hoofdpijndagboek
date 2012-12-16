package org.nvh.hoofdpijndagboek;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;

public class HeadacheAttack {
	Calendar start;
	Calendar end;
	String ernst;
	List<PainPoint> leftPainPoints;
	List<PainPoint> rightPainPoints;
	boolean misselijk;
	boolean menstruatie;
	boolean licht;
	boolean duizelig;
	boolean geur;
	boolean inslapen;
	boolean doorslapen;
	boolean stoelgang;
	String weer;
	String humeur;

	public String toString() {
		Context context = HeadacheDiaryApp.getApp();
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.weer)).append(":")
				.append(this.weer).append("\n");
		sb.append(context.getString(R.string.humeur)).append(":")
				.append(this.humeur).append("\n");
		if (this.menstruatie == true) {
			sb.append(context.getString(R.string.menstruatie)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.misselijk == true) {
			sb.append(context.getString(R.string.misselijk)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.licht == true) {
			sb.append(context.getString(R.string.licht)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.duizelig == true) {
			sb.append(context.getString(R.string.duizelig)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.geur == true) {
			sb.append(context.getString(R.string.geur)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.inslapen == true) {
			sb.append(context.getString(R.string.inslapen)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.doorslapen == true) {
			sb.append(context.getString(R.string.doorslapen)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}
		if (this.stoelgang == true) {
			sb.append(context.getString(R.string.stoelgang)).append(":")
					.append(context.getString(R.string.ja)).append("\n");
		}

		if (this.leftPainPoints != null) {
			sb.append(context.getString(R.string.left)).append(":").append(context.getString(R.string.ouch));
			for (int i = 0; i < leftPainPoints.size(); i++) {
				PainPoint p = leftPainPoints.get(i);
				sb.append("au").append(":")
						.append(String.format(Locale.US, "%1.3f", p.x))
						.append(";")
						.append(String.format(Locale.US, "%1.3f", p.y))
						.append(";").append(p.colorIndex).append("\n");

			}
		}
		if (this.rightPainPoints != null) {
			sb.append(context.getString(R.string.right)).append(":").append(context.getString(R.string.ouch));
			for (int i = 0; i < rightPainPoints.size(); i++) {
				PainPoint p = rightPainPoints.get(i);
				sb.append("au").append(":")
						.append(String.format(Locale.US, "%1.3f", p.x))
						.append(";")
						.append(String.format(Locale.US, "%1.3f", p.y))
						.append(";").append(p.colorIndex).append("\n");

			}
		}
		return sb.toString();
	}
}
