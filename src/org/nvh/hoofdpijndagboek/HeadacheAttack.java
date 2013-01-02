package org.nvh.hoofdpijndagboek;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;

public class HeadacheAttack {
	Calendar start;
	Calendar end;
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
		sb.append(context.getString(R.string.ernst)).append(":")
				.append(this.getErnst()).append("\n");
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
			sb.append(context.getString(R.string.left)).append(":")
					.append(context.getString(R.string.ouch)).append("\n");
			handlePainPoints(this.leftPainPoints, context, sb);
		}
		if (this.rightPainPoints != null) {
			sb.append(context.getString(R.string.right)).append(":")
					.append(context.getString(R.string.ouch)).append("\n");
			handlePainPoints(this.rightPainPoints, context, sb);
		}
		return sb.toString();
	}

	private void handlePainPoints(List<PainPoint> points, Context context,
			StringBuilder sb) {
		for (PainPoint p : points) {
			sb.append(context.getString(R.string.ouch)).append(":")
					.append(String.format(Locale.US, "%1.3f", p.x)).append(";")
					.append(String.format(Locale.US, "%1.3f", p.y)).append(";")
					.append(p.colorIndex).append("\n");
		}
	}

	int getErnst() {
		int totalPain = 0;
		int n = 0;
		if (this.leftPainPoints != null) {
			for (PainPoint p : this.leftPainPoints) {
				totalPain += p.colorIndex;
				n++;
			}
		}
		if (this.rightPainPoints != null) {
			for (PainPoint p : this.rightPainPoints) {
				totalPain += p.colorIndex;
				n++;
			}
		}
		if (n == 0) {
			return 0;
		}
		return (int) Math.ceil((double) totalPain / n);
	}
}
