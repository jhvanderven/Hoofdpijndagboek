package org.nvh.hoofdpijndagboek;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
	public static int THEME = R.style.Theme_Sherlock;

	TabHost mTabHost;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	Location whereIsPhone;
	LocationListener locationListener;
	Address here = null;
	int geoCodeExceptionCounter = 0;

	final int CHOOSE_A_CSV_FILE = 123;

	private HeadacheAttack attack = null;

	private boolean newHeadache;

	public HeadacheAttack getAttack() {
		if (attack == null) {
			attack = new HeadacheAttack();
		}
		return attack;
	}

	public List<PainPoint> getPainPoints(int side) {
		if (side == 0) {
			return getAttack().leftPainPoints;
		} else {
			return getAttack().rightPainPoints;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeAttack();
		newHeadache = true; // we start out so the user can enter new headache
							// directly
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Wanneer").setIndicator("",
						getResources().getDrawable(R.drawable.calendar)),
				HPDTime.TimingFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Waar").setIndicator("",
						getResources().getDrawable(R.drawable.righthead)),
				HPDHeadLeft.HurtingFragmentLeft.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Raaw").setIndicator("",
						getResources().getDrawable(R.drawable.lefthead)),
				HPDHeadRight.HurtingFragmentRight.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Symptomen").setIndicator("",
						getResources().getDrawable(R.drawable.symptoms)),
				HPDDetails.SymptomsFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Medicijnen").setIndicator("",
						getResources().getDrawable(R.drawable.medicin2)),
				HPDMedicins.ArrayListFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				makeUseOfNewLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates. The tablet only has wifi. It never will respond to this.
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		whereIsPhone = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	private void initializeAttack() {
		HeadacheAttack a = getAttack();
		Calendar c = Calendar.getInstance();
		// users can destroy apps by entering stupid things
		int value = 0;
		try {
			value = Integer.valueOf(HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0)
					.getString("pref_attack_start", "-5"));
		} catch (NumberFormatException nfe) {
			value = -5;
		}
		c.add(Calendar.HOUR_OF_DAY, value);
		c.set(Calendar.MINUTE, 0);
		a.start = c;
		a.end = (Calendar) c.clone();
		try {
			value = Integer.valueOf(HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0)
					.getString("pref_attack_length", "4"));
		} catch (NumberFormatException nfe) {
			value = 4;
		}
		a.end.add(Calendar.HOUR_OF_DAY, value);
		a.leftPainPoints = new ArrayList<PainPoint>();
		a.rightPainPoints = new ArrayList<PainPoint>();
		a.misselijk = false;
		a.menstruatie = false;
		a.doorslapen = false;
		a.duizelig = false;
		a.geur = false;
		a.humeur = "";
		a.inslapen = false;
		a.licht = false;
		a.stoelgang = false;
		a.weer = "";
	}

	protected void makeUseOfNewLocation(Location location) {
		// Check whether the new location fix is more or less accurate
		int accuracyDelta;
		if (whereIsPhone == null) {
			accuracyDelta = -1; // must be better :-)
		} else {
			accuracyDelta = (int) (location.getAccuracy() - whereIsPhone
					.getAccuracy());
		}
		if (accuracyDelta < 0) {
			whereIsPhone = location;
			// find the address
			Geocoder gcd = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = null;
			try {
				addresses = gcd.getFromLocation(whereIsPhone.getLatitude(),
						whereIsPhone.getLongitude(), 1);
				if (addresses.size() > 0) {
					here = addresses.get(0);
				}
				// stop listening, save battery
				LocationManager locationManager = (LocationManager) this
						.getSystemService(Context.LOCATION_SERVICE);
				locationManager.removeUpdates(locationListener);
			} catch (IOException e) {
				geoCodeExceptionCounter++;
				if (geoCodeExceptionCounter > 10) {
					// give up, save battery
					LocationManager locationManager = (LocationManager) this
							.getSystemService(Context.LOCATION_SERVICE);
					locationManager.removeUpdates(locationListener);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_save_headache).setVisible(newHeadache);
		menu.findItem(R.id.menu_new_headache).setVisible(!newHeadache);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		// Check which request we're responding to
		if (requestCode == CHOOSE_A_CSV_FILE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// prepare for a progress bar dialog
				final ProgressDialog progressBar = new ProgressDialog(this);
				progressBar.setMessage(getString(R.string.import_headaches)
						+ ": " + data.getDataString());
				progressBar.show();
				new Thread(new Runnable() {
					public void run() {
						Utils.importHeadaches(data.getData(), MainActivity.this);
						View hcv = findViewById(R.id.hcv);
						if (hcv != null) {
							((HeadacheCalendarView) hcv).redraw();
						}
						progressBar.dismiss();
					}
				}).start();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// we have to gather the information from all the tabs and
		// create one or more (medication) entries in the calendar
		switch (item.getItemId()) {
		case R.id.menu_remove_headaches:
			deleteAttacks(getString(R.string.calendar_entry_title));
			return true;
		case R.id.menu_remove_pills:
			deleteAttacks(getString(R.string.calendar_pill_title));
			return true;
		case R.id.menu_prefs:
			startActivity(new Intent(this, HeadachePreferences.class));
			return true;
		case R.id.menu_edit_pills:
			startActivity(new Intent(this, EditPillsActivity.class));
			return true;
		case R.id.menu_new_headache:
			newHeadache = true;
			initializeAttack();
			// repaint current screen
			repaintTabs();
			invalidateOptionsMenu();
			return true;
		case R.id.menu_import_headaches:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("text/csv");

			// Do this if you need to be able to open the returned URI as a
			// stream
			// (for example here to read the image data).
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			// intent.addCategory(Intent.EXTRA_LOCAL_ONLY);
			Intent finalIntent = Intent.createChooser(intent,
					getString(R.string.select_csv_file));
			startActivityForResult(finalIntent, CHOOSE_A_CSV_FILE);
			return true;
		case R.id.menu_save_headaches:
			// save all headaches to disk
			final File path = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			final Calendar now = Calendar.getInstance();
			final File file = new File(path,
					String.format("%s-%s.csv", getString(R.string.app_name),
							Utils.formatNoSeparators(now)));
			final ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage(getString(R.string.save_headaches)+": "+file.getAbsolutePath());
			pd.show();
			new Thread(new Runnable() {
				@Override
				public void run() {
					Utils.saveHeadacheEvents(file, MainActivity.this);
					// Tell the media scanner about the new file so that it is
					// immediately available to the user.
					MediaScannerConnection.scanFile(MainActivity.this,
							new String[] { file.toString() }, null,
							new MediaScannerConnection.OnScanCompletedListener() {
								public void onScanCompleted(String path, Uri uri) {
									Log.i("ExternalStorage", "Scanned " + path + ":");
									Log.i("ExternalStorage", "-> uri=" + uri);
								}
							});
					File pillFile = new File(path, String.format("%s-%s.csv",
							getString(R.string.calendar_pill_title),
							Utils.formatNoSeparators(now)));
					Utils.savePillEvents(pillFile, MainActivity.this);
					// Tell the media scanner about the new file so that it is
					// immediately available to the user.
					MediaScannerConnection.scanFile(MainActivity.this,
							new String[] { pillFile.toString() }, null,
							new MediaScannerConnection.OnScanCompletedListener() {
								public void onScanCompleted(String path, Uri uri) {
									Log.i("ExternalStorage", "Scanned " + path + ":");
									Log.i("ExternalStorage", "-> uri=" + uri);
								}
							});
					pd.dismiss();
				}
			}).start();
			return true;
		case R.id.menu_save_headache:
			// headaches starting more than a year ago
			// headaches ending in the future
			// headaches starting in the future
			// headaches lasting longer than a week
			// headaches that overlap existing headaches
			String startDate = HPDTime.TimingFragment.startDate.getText()
					.toString();
			String startTime = HPDTime.TimingFragment.startTime.getText()
					.toString();
			String startDateTime = String.format("%s %s", startDate, startTime);
			Calendar start = Calendar.getInstance();
			try {
				start.setTimeInMillis(Utils.parse(startDateTime,
						getString(R.string.very_long_date_time)).getTime());
			} catch (ParseException e1) {
				return true;
			}
			String endDate = HPDTime.TimingFragment.endDate.getText()
					.toString();
			String endTime = HPDTime.TimingFragment.endTime.getText()
					.toString();
			String endDateTime = String.format("%s %s", endDate, endTime);
			Calendar stop = Calendar.getInstance();
			try {
				stop.setTimeInMillis(Utils.parse(endDateTime,
						getString(R.string.very_long_date_time)).getTime());
			} catch (ParseException e1) {
				return true;
			}
			validateStartAndEndOfAttack(start, stop);
			return true;
		}
		return false;
	}

	private void deleteAttacks(final String title) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					final ProgressDialog progressDialog = new ProgressDialog(
							MainActivity.this);
					progressDialog.setMessage(getString(R.string.remove_headaches) + ": " + title);
					progressDialog.show();
					new Thread(new Runnable() {
						public void run() {
							Utils.deleteAttacks(MainActivity.this, title);
							View hcv = findViewById(R.id.hcv);
							if (hcv != null) {
								((HeadacheCalendarView) hcv).redraw();
							}
							progressDialog.dismiss();
						}
					}).start();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// we do nothing
					break;
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
				.setMessage(getString(R.string.delete_message))
				.setPositiveButton(getString(android.R.string.ok),
						dialogClickListener)
				.setNegativeButton(getString(android.R.string.cancel),
						dialogClickListener).show();
	}

	private boolean saveAttackInCalendar(Calendar start, Calendar stop) {
		HeadacheAttack attack = getAttack();
		attack.start = start;
		attack.end = stop;
		Utils.addToCalendar(attack, this);
		// This is only needed when we are on the first tab
		View hcv = findViewById(R.id.hcv);
		if (hcv != null) {
			((HeadacheCalendarView) hcv).redraw();
		}
		// provide some feedback
		Toast.makeText(MainActivity.this, R.string.event_saved,
				Toast.LENGTH_LONG).show();
		return true;
	}

	private boolean validateStartAndEndOfAttack(final Calendar start,
			final Calendar stop) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					saveAttackInCalendar(start, stop);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// we do nothing
					break;
				}
			}
		};
		Calendar now = Calendar.getInstance();
		String message = "";
		if ((now.getTimeInMillis() - start.getTimeInMillis()) > (30L * 24 * 60 * 60 * 1000)) {
			// headaches starting more than a month ago
			message = getString(R.string.attack_starts_more_than_a_month_ago);
		} else if (stop.getTimeInMillis() > now.getTimeInMillis()) {
			// headaches ending in the future
			message = getString(R.string.attack_ends_in_the_future);
		} else if (start.getTimeInMillis() > now.getTimeInMillis()) {
			// headaches starting in the future
			message = getString(R.string.attack_starts_in_the_future);
		} else if ((stop.getTimeInMillis() - start.getTimeInMillis()) > (7L * 24 * 60 * 60 * 1000)) {
			// headaches lasting longer than a week
			message = getString(R.string.attack_lasts_longer_than_one_week);
		} else {
			// headaches that overlap existing headaches
			int n = Utils.getNumberOfAttacks(start, stop, getContentResolver(),
					getString(R.string.calendar_entry_title));
			if (n > 0) {
				message = getString(R.string.attack_overlaps_another_attack);
			}
		}
		if (message != "") {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.attack_verification))
					.setMessage(message)
					.setPositiveButton(getString(android.R.string.ok),
							dialogClickListener)
					.setNegativeButton(getString(android.R.string.cancel),
							dialogClickListener).show();
		} else {
			// we can find no strange situation, so we store it
			saveAttackInCalendar(start, stop);
		}
		return true;
	}

	public void repaintTabs() {
		HPDTime.TimingFragment.pleaseUpdate(getAttack());
		HPDDetails.SymptomsFragment.pleaseUpdate(getAttack(), getResources()
				.getStringArray(R.array.weather_array), getResources()
				.getStringArray(R.array.humeur_array));
		HPDHeadLeft.HurtingFragmentLeft.pleaseUpdate(getAttack());
		HPDHeadRight.HurtingFragmentRight.pleaseUpdate(getAttack());
	}

	public String getLocation() {
		if (here == null) {
			return "";
		} else {
			return here.getLocality();
		}
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			@SuppressWarnings("unused")
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost,
				ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
			View v = mViewPager.getChildAt(position);
			if (v == null)
				return;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	}

	public void setWorkingOnNewHeadache(boolean b) {
		newHeadache = b;
		// I want to change the menu immediately here,
		// read on the internet that this may crash before API level 11...
		invalidateOptionsMenu();
	}
}
