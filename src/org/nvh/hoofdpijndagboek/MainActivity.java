package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Wanneer").setIndicator(
						"",
						getResources().getDrawable(
								android.R.drawable.ic_menu_agenda)),
				HPDTime.TimingFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Waar").setIndicator(
						"",
						getResources().getDrawable(
								android.R.drawable.ic_menu_directions)),
								HPDHead.HurtingFragmentLeft.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Waar").setIndicator(
						"",
						getResources().getDrawable(
								android.R.drawable.ic_menu_more)),
								HPDHead.HurtingFragmentRight.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Medicijnen").setIndicator(
						"",
						getResources().getDrawable(
								android.R.drawable.ic_media_rew)),
				HPDMedicins.MedicinFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("Symptomen").setIndicator(
						"",
						getResources().getDrawable(
								android.R.drawable.ic_menu_manage)),
				HPDDetails.SymptomsFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean isLight = MainActivity.THEME == R.style.Theme_Sherlock_Light;

		menu.add("Save")
				.setIcon(
						isLight ? R.drawable.ic_compose_inverse
								: R.drawable.ic_compose)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// we have to gather the information from all the tabs and
		// create one or more (medication) entries in the calendar
		String startDateTime = HPDTime.TimingFragment.startDateTime.getText()
				.toString();
		Calendar start = parseIsoDate(startDateTime);
		if (start == null) {
			return true;
		}
		String endDateTime = HPDTime.TimingFragment.endDateTime.getText()
				.toString();
		Calendar stop = parseIsoDate(endDateTime);
		if (stop == null) {
			return true;
		}
		StringBuilder message = new StringBuilder();
		message.append(HPDTime.TimingFragment.getData());
		message.append(HPDDetails.SymptomsFragment.getData());
		message.append(getString(R.string.left)).append(":").append(getString(R.string.ouch)).append("\n");
		message.append(HPDHead.left.getData());
		message.append(getString(R.string.right)).append(":").append(getString(R.string.ouch)).append("\n");
		message.append(HPDHead.right.getData());
		Intent intent = new Intent(Intent.ACTION_INSERT)
				.setType("vnd.android.cursor.item/event")
				.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
						start.getTimeInMillis())
				.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
						stop.getTimeInMillis())
				.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
				// just included for completeness
				.putExtra(Events.TITLE, "Hoofdpijnaanval")
				.putExtra(Events.DESCRIPTION, message.toString())
				.putExtra(Events.EVENT_LOCATION, "Hoogland")
				.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
				.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
		startActivity(intent);
		return true;
	}

	private Calendar parseIsoDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date someTime = new Date();
		try {
			someTime = dateFormat.parse(date);
		} catch (ParseException e) {
			Toast.makeText(this, "Error: " + e.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someTime);
		return cal;
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
}
