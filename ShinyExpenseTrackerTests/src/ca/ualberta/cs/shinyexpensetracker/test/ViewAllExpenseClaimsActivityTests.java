/**
 *  This Unit Test module tests the ExpenseClaimView activity,
 *  ensuring it is displayed correctly.
 *  
 *  Copyright (C) 2015  github.com/RostarSynergistics
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Issue #22
 * @author Tristan Meleshko
 */

package ca.ualberta.cs.shinyexpensetracker.test;

import java.text.ParseException;
import java.util.Date;

import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import ca.ualberta.cs.shinyexpensetracker.Application;
import ca.ualberta.cs.shinyexpensetracker.ExpenseClaimController;
import ca.ualberta.cs.shinyexpensetracker.R;
import ca.ualberta.cs.shinyexpensetracker.activities.AddExpenseClaimActivity;
import ca.ualberta.cs.shinyexpensetracker.activities.ExpenseClaimsActivity;
import ca.ualberta.cs.shinyexpensetracker.activities.ExpenseItemActivity;
import ca.ualberta.cs.shinyexpensetracker.activities.TabbedSummaryActivity;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.models.Tag;
import ca.ualberta.cs.shinyexpensetracker.models.TagList;
import ca.ualberta.cs.shinyexpensetracker.test.mocks.MockExpenseClaimListPersister;

public class ViewAllExpenseClaimsActivityTests extends
		ActivityInstrumentationTestCase2<ExpenseClaimsActivity> {
	
	ExpenseClaimsActivity activity;
	ExpenseClaimList claimsList;
	ExpenseClaimController controller;

	// Flag for thread crashes
	static boolean failed;
	
	ListView claimListView;
	
	public ViewAllExpenseClaimsActivityTests() {
		super(ExpenseClaimsActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// Inject an empty list so that saving/loading doesn't interfere,
		// just in case.
		claimsList = new ExpenseClaimList();
		controller = new ExpenseClaimController(new MockExpenseClaimListPersister(claimsList));
		
		Application.setExpenseClaimController(controller);
		
		activity = getActivity();
		claimListView = (ListView) activity.findViewById(R.id.expense_claim_list);
	}
	
	/**
	 * Add a claim to the claimsList safely
	 * @param claim
	 * @return
	 */
	private ExpenseClaim addClaim(final ExpenseClaim claim) {
		// Run on the activity thread.
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				claimsList.addClaim(claim);
			}
		});
		getInstrumentation().waitForIdleSync();
		return claim;
	}
	
	/**
	 * Delete a claim from the claimsList safely
	 * @param claim
	 */
	private void deleteClaim(final ExpenseClaim claim) {
		// Run on the activity thread
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				claimsList.removeClaim(claim);
			}
		});
		getInstrumentation().waitForIdleSync();
	}

	private ExpenseClaim getClaim(int i) {
		return (ExpenseClaim) claimListView.getItemAtPosition(i);
	}
	
	/**
	 * Tests if the longPressDialog displays correctly.
	 * 
	 * ListViews don't seem to expose a performItemLongClick
	 * function. I can't test UI functionality without this, so
	 * we'll assume that long click works and calls an
	 * "askDeleteItemAtPosition" function correctly. That way,
	 * the unit test only need to test if this function does
	 * its job, rather than checking the API functionality itself.
	 * 
	 * --This test may be removed in future iterations.
	 */
	public void testLongPressDialog() {
		// Not this test's responsibility to check what was deleted.
		addClaim(new ExpenseClaim("Test Claim"));

		// Fake long press
		//
		// NOTE: This will probably give a "Window Leaked" warning.
		// This is because we're kidnapping the dialog from the activity
		// via return call after the activity closes. Methods that call
		// askDeleteClaimAt should NEVER keep the value.
		// In fact, the only reason it's exposed is because ListViews
		// don't seem to expose performItemLongClick.
		assertTrue(activity.askDeleteClaimAt(0).isShowing());
	}
	
	/**
	 * Adds a claim and ensures that it is visible in the listview.
	 */
	public void testAddedClaimIsVisible() {
		ExpenseClaim claim = addClaim(new ExpenseClaim("Test Claim"));
		ExpenseClaim visibleClaim;
		
		// Get the last position in the list
		// TODO Get last position. See issue #64.
		int lastPosition = claimListView.getCount() - 1;
		assertFalse("Claim List is empty", claimListView.getCount() == 0);
		assertFalse("Claim List has too many objects", claimListView.getCount() > 1);
		
		// Get the expense claim object
		visibleClaim = getClaim(lastPosition);
		assertNotNull("No object found in the list", visibleClaim);
		
		// Ensure that the claim that was added to the list
		// is also the claim in the listview.
		assertTrue("Claim not visible", visibleClaim.equals(claim));
	}
	
	/* All of the testClaimsSorted check roughly the same thing,
	 * going through all 6 permutations of {new, mid, old}.
	 */
	public void testClaimsSorted() throws Exception {
		ExpenseClaim[] testingClaims = {
				new ExpenseClaim("Old Claim", new Date(1000)),
				new ExpenseClaim("Mid Claim", new Date(2000)),
				new ExpenseClaim("New Claim", new Date(3000)),
		};
		int numTests = 0;

		// Check that our test case items compare correctly
		// -- Equality Cases
		assertEquals(0, testingClaims[0].compareTo(testingClaims[0]));
		assertEquals(0, testingClaims[1].compareTo(testingClaims[1]));
		assertEquals(0, testingClaims[2].compareTo(testingClaims[2]));
		// -- Inequality Cases
		assertEquals(-1, testingClaims[0].compareTo(testingClaims[1]));
		assertEquals(-1, testingClaims[0].compareTo(testingClaims[2]));
		assertEquals(-1, testingClaims[1].compareTo(testingClaims[2]));
		// -- Greater-than tests are done in the loop.

		// Iterate through all permutations
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					// Skip tests with equality--
					// These depend on stability of sort 
					if (i==j || j == k || i == k) {
						continue;
					}
					
					// Count tests actually executed
					numTests++;
					
					// Build up our list
					addClaim(testingClaims[i]);
					addClaim(testingClaims[j]);
					addClaim(testingClaims[k]);

					// Sanity check: 3 items in the list
					assertEquals(3, controller.getCount());
					
					// check index 0 is newer than index 1
					assertEquals("Comparison failed. Wanted <" + getClaim(0) + "> newer than <" + getClaim(1) + ">;",
							1, getClaim(0).compareTo(getClaim(1)));
					
					// Can skip 0 > 2 by transitivity.
					// Leave it in as a sanity check.
					assertEquals("Comparison failed. Wanted <" + getClaim(0) + "> newer than <" + getClaim(2) + ">;",
							1, getClaim(0).compareTo(getClaim(2)));
					
					// check index 1 is newer than index 2
					assertEquals("Comparison failed. Wanted <" + getClaim(1) + "> newer than <" + getClaim(2) + ">;",
							1, getClaim(1).compareTo(getClaim(2)));

					// Reset the test
					// --> Can't replace the ClaimList because we'll lose
					// 	   lose observers
					deleteClaim(testingClaims[i]);
					deleteClaim(testingClaims[j]);
					deleteClaim(testingClaims[k]);
				}
			}
		}
		
		// Sanity check: make sure we run all permutations
		assertEquals(6, numTests);
	}
	
	/*
	 * Inserts some data with same dates. They can be in
	 * either order in the test, but things that are not
	 * equal must be on the outside. Testing for outside
	 * values is done in testClaimsSorted
	 */
	public void testEqualSorted() {
		ExpenseClaim claim1 = addClaim(new ExpenseClaim("Mid Claim 1", new Date(2000)));
		addClaim(new ExpenseClaim("Old Claim", new Date(1000)));
		addClaim(new ExpenseClaim("New Claim", new Date(3000)));
		ExpenseClaim claim2 = addClaim(new ExpenseClaim("Mid Claim 2", new Date(2000)));
		
		// index 0 newer than index 1
		assertEquals(1, getClaim(0).compareTo(getClaim(1)));
		// index 1 same as index 2
		assertEquals(0, getClaim(1).compareTo(getClaim(2)));
		// index 2 newer than index 3
		assertEquals(1, getClaim(2).compareTo(getClaim(3)));
		
		// Make sure middle two indexes are truly different objects
		assertNotSame(claim1, claim2);
		
	}
	
	/**
	 * Deletes a claim and ensure it isn't visible in the listview.
	 * Does not test dialogs.
	 */
	public void testDeleteVisibleClaim() {
		// Build 2 claims with dates so that claim 1 < claim 2.
		TagList tags1 = new TagList();
		tags1.addTag(new Tag("Test 1"));
		ExpenseClaim claim1 = addClaim(
				new ExpenseClaim("Delete Claim 1", new Date(), null, ExpenseClaim.Status.IN_PROGRESS, tags1));
		TagList tags2 = new TagList();
		tags2.addTag(new Tag("Test 2"));
		ExpenseClaim claim2 = addClaim(
				new ExpenseClaim("Delete Claim 2", new Date(), null, ExpenseClaim.Status.IN_PROGRESS, tags2));
		ExpenseClaim visibleClaim;
		
		// Check that an item is actually deleted
		deleteClaim(claim2);
		assertEquals("Item wasn't deleted", 1, claimListView.getCount());
		
		// Check that the right item was deleted.
		visibleClaim = getClaim(0);
		assertEquals("Remaining claim isn't claim 1", claim1, visibleClaim);

		// Check that the remaining item is actually deleted
		deleteClaim(visibleClaim);
		assertEquals("Item wasn't deleted", 0, claimListView.getCount());
	}

	/**
	 * Checks if pressing the NewClaim button opens the
	 * appropriate activity and adds a new claim i
	 */
	
	public void testMenuClickNewActivity() {
		
		// Check to see if AddExpenseActivity is visible once the MenuItem is clicked.
		// Code taken from: stackoverflow.com/questions/3084891/how-to-test-menu
				
		ActivityMonitor am = getInstrumentation().addMonitor(AddExpenseClaimActivity.class.getName(), null, false);
		getInstrumentation().invokeMenuActionSync(activity, R.id.action_new_claim, 0);
		
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
		
		// Wait for the application to become idle
		getInstrumentation().waitForIdleSync();

	}
	
	/**
	 * Test for crash on new expense claim.
	 * See #91 for details.
	 */
	public void testCrashOnNewExpense() {
		// Monitor for AddExpenseClaimActivity
		ActivityMonitor claimMonitor = getInstrumentation().addMonitor(AddExpenseClaimActivity.class.getName(), null, false);

		// Press the "New Claim" button
		getInstrumentation().invokeMenuActionSync(activity, R.id.action_new_claim, 0);

		// Get the create claim activity
		final AddExpenseClaimActivity createClaim = (AddExpenseClaimActivity) getInstrumentation().waitForMonitorWithTimeout(claimMonitor, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(claimMonitor, 1));
		
		// Fill in the data
		createClaim.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	EditText name = ((EditText) createClaim.findViewById(R.id.editTextExpenseClaimName));
		    	EditText startDate = ((EditText) createClaim.findViewById(R.id.editTextStartDate));
		    	EditText endDate = ((EditText) createClaim.findViewById(R.id.editTextEndDate));
		    	Button doneButton = (Button) createClaim.findViewById(R.id.addExpenseClaimDoneButton);
		    	
		    	// Set values
		    	name.setText("Test Claim");
		    	startDate.setText("03-05-2015");
		    	endDate.setText("04-05-2015");
		    	
		    	// Close the activity
		    	doneButton.performClick();
				
			}
		});
		
		// Monitor for TabbedSummaryActivity
		ActivityMonitor summaryMonitor = getInstrumentation().addMonitor(TabbedSummaryActivity.class.getName(), null, false);
		
		// Get the summary activity
		TabbedSummaryActivity summaryActivity = (TabbedSummaryActivity) getInstrumentation().waitForMonitorWithTimeout(summaryMonitor, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(summaryMonitor, 1));
		
		// Close the summary
		summaryActivity.finish();

		// Monitor for TabbedSummaryActivity again
		summaryMonitor = getInstrumentation().addMonitor(TabbedSummaryActivity.class.getName(), null, false);
		
		// Tap an item in the list view this time to display the summary
		final ListView claimsList = (ListView) activity.findViewById(R.id.expense_claim_list);
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				claimsList.performItemClick(claimsList.getChildAt(0), 0, 0);
			}
		});
		
		// Get the summary activity
		summaryActivity = (TabbedSummaryActivity) getInstrumentation().waitForMonitorWithTimeout(summaryMonitor, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(summaryMonitor, 1));
		
		// Monitor for ExpenseItemActivity
		ActivityMonitor expenseMonitor = getInstrumentation().addMonitor(ExpenseItemActivity.class.getName(), null, false);
		
		// Press the "Add Expense" button
		getInstrumentation().invokeMenuActionSync(summaryActivity, R.id.addExpenseItem, 0);
		
		// Get the expense item activity
		final ExpenseItemActivity createExpense = (ExpenseItemActivity) getInstrumentation().waitForMonitorWithTimeout(expenseMonitor, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(expenseMonitor, 1));
		
		// Try to close the createExpense activity
		failed = false;
		createExpense.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Button done = (Button) createExpense.findViewById(R.id.expenseItemDoneButton);
				EditText name = (EditText) createExpense.findViewById(R.id.expenseItemNameEditText);
				EditText date = (EditText) createExpense.findViewById(R.id.expenseItemDateEditText);
				EditText amount = (EditText) createExpense.findViewById(R.id.expenseItemAmountEditText);
				
				// Set values
				name.setText("Test Expense");
				date.setText("03-05-2015");
				amount.setText("100");
				
				// Create the expense item
				try {
					assertTrue(createExpense.createExpenseItem());
				} catch (ParseException e) {
					fail("Parse Exception :(");
					e.printStackTrace();
				} catch (NullPointerException e) {
					failed = true;
					e.printStackTrace();
				}
				
			}
		});
		
		// Close the activity safely outside of a thread
		// Source: Email from Alex Wilson (March 15, 2015)
		try {
			createExpense.finish();
		} catch (NullPointerException e) {
			failed = true;
		}
		// Clean up
		summaryActivity.finish();
		
		// Check if there were problems
		assertFalse("Bug #91 reproduced", failed); 
		
		// Wait for the application to become idle
		getInstrumentation().waitForIdleSync();

		
	}
}
