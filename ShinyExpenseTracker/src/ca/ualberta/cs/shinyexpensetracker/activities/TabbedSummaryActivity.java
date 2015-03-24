package ca.ualberta.cs.shinyexpensetracker.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ca.ualberta.cs.shinyexpensetracker.R;
import ca.ualberta.cs.shinyexpensetracker.adapters.SectionsPagerAdapter;
import ca.ualberta.cs.shinyexpensetracker.fragments.ClaimSummaryFragment;
import ca.ualberta.cs.shinyexpensetracker.fragments.DestinationListFragment;
import ca.ualberta.cs.shinyexpensetracker.fragments.ExpenseItemListFragment;

// Source: https://github.com/astuetz/PagerSlidingTabStrip
// on March 11 2015
//http://www.androidhive.info/2013/10/android-tab-layout-with-swipeable-views-1/
//on March 12 2015
/**
 * Deals with displaying the ClaimSummaryFragment, ExpenseItemListFragment, and
 * DestinationListFragment.  Allows user to either click on the action bar tabs
 * or swipe left and right to view the other tabs.  
 * 
 * Called when user clicks on an Expense claim in the expenseClaimListActivity
 * 
 * Begins by loading the ClaimSummaryFragment.
 *
 */
public class TabbedSummaryActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabbed_summary);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		context = getBaseContext();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i, context))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tabbed_summary, menu);
		return true;
	}
	
	/**
	 * Called on MenuItem "Add Expense Item" click
	 * Goes to ExpenseItemActivity to allow user to add an expense item to their claim
	 * @param menu
	 */
	public void addExpenseItemMenuItem(MenuItem menu) {
		Intent intent = getIntent();
		int claimIndex = intent.getIntExtra("claimIndex", -1);
		intent = new Intent(TabbedSummaryActivity.this, ExpenseItemActivity.class);
		intent.putExtra("claimIndex", claimIndex);
		startActivity(intent);
	}
	
	/**
	 * Called on MenuItem "Add Tag" click
	 * @param menu
	 */
	public void addTagMenuItem(MenuItem menu) {
	}
	
	/** 
	 * Called on MenuItem "Add Destination" click
	 * @param menu
	 */
	public void addDestinationMenuItem(MenuItem menu) {
		Intent intent = getIntent();
		int claimIndex = intent.getIntExtra("claimIndex", -1);
		intent = new Intent(TabbedSummaryActivity.this, AddDestinationActivity.class);
		intent.putExtra("claimIndex", claimIndex);
		startActivity(intent);
	}
	
	/**
	 * Called on MenuItem "Edit Claim" click
	 * @param menu
	 */
	public void editClaimMenuItem(MenuItem menu) {
		Intent intent = getIntent();
		int claimIndex = intent.getIntExtra("claimIndex", -1);
		intent = new Intent(this, AddExpenseClaimActivity.class);
		intent.putExtra("claimIndex", claimIndex);
		startActivity(intent);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	/**
	 * Force selects the summary tab 
	 * @return the corresponding fragment.
	 */
	public ClaimSummaryFragment selectClaimSummaryTab() {
		// Select the 0'th tab
		getActionBar().selectTab(getActionBar().getTabAt(0));
		return (ClaimSummaryFragment) getCurrentFragment();
	}

	/**
	 * Force selects the expense list tab 
	 * @return the corresponding fragment.
	 */
	public ExpenseItemListFragment selectExpenseListTab() {
		// Select the 1st tab
		getActionBar().selectTab(getActionBar().getTabAt(1));
		return (ExpenseItemListFragment) getCurrentFragment();
	}

	/**
	 * Force selects the destination list tab
	 * @return the corresponding fragment.
	 */
	public DestinationListFragment selectDestinationListTab() {
		// Select the 2nd tab
		getActionBar().selectTab(getActionBar().getTabAt(2));
		return (DestinationListFragment) getCurrentFragment();
	}
	
	/**
	 * @return The currently visible fragment.
	 */
	public Fragment getCurrentFragment() {
		// http://stackoverflow.com/questions/18609261/getting-the-current-fragment-instance-in-the-viewpager
		// March 13, 2015
		int index = mViewPager.getCurrentItem();
		SectionsPagerAdapter adapter = ((SectionsPagerAdapter) mViewPager.getAdapter());
		Fragment fragment = adapter.getFragment(index);
		return fragment;
	}
	
}
