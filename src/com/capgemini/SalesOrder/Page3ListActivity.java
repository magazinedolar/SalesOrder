package com.capgemini.SalesOrder;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capgemini.SalesOrder.zgwsample_srv.v0.ZGWSAMPLE_SRVRequestHandler;
import com.capgemini.SalesOrder.zgwsample_srv.v0.entitytypes.SalesOrder;
import com.capgemini.SalesOrder.zgwsample_srv.v0.entitytypes.SalesOrderLineItem;
import com.capgemini.SalesOrder.zgwsample_srv.v0.helpers.IZGWSAMPLE_SRVRequestHandlerListener;
import com.capgemini.SalesOrder.zgwsample_srv.v0.helpers.ZGWSAMPLE_SRVRequestID;
import com.sap.gwpa.proxy.RequestStatus;
import com.sap.gwpa.proxy.RequestStatus.StatusType;
import com.sap.mobile.lib.request.IResponse;
import com.sap.mobile.lib.supportability.ILogger;
import com.sap.mobile.lib.supportability.Logger;

/**
 * List screen.
 */
public class Page3ListActivity extends ListActivity implements
		IZGWSAMPLE_SRVRequestHandlerListener
{
	public static final String TAG = "SalesOrder";
	private ILogger logger;
	protected static SalesOrder parentEntry;
	
	// result of List Request
	private List<SalesOrderLineItem> entries;

	// need handler for callbacks to the UI thread
	final Handler mHandler = new Handler(); 

	// create runnable for posting
	final Runnable mUpdateResults = new Runnable()
	{
		public void run()
		{
			updateResultsInUi();
		}
	};

	private Page3ListAdapter listAdapter;
	
	// connectivity error message
	private String emessage = "";

	/**
	 * Refresh UI from background thread
	 */
	protected void updateResultsInUi()
	{
		// Error occurred
		if (entries == null)
		{
			View loadingView = findViewById(com.capgemini.SalesOrder.R.id.loading_view);
			loadingView.setVisibility(View.GONE);
			Toast.makeText(getApplicationContext(), emessage, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		listAdapter = new Page3ListAdapter(this);
		listAdapter.setEntries(entries);

		setListAdapter(listAdapter);
		RelativeLayout loadingView = (RelativeLayout) findViewById(com.capgemini.SalesOrder.R.id.loading_view);
		
		// No results - empty set
		if (entries.size() == 0)
		{
			ProgressBar progressBar = (ProgressBar) findViewById(com.capgemini.SalesOrder.R.id.progressBar1);
			progressBar.setVisibility(View.GONE);
			
			TextView textView = new TextView(getApplicationContext());
			textView.setText(com.capgemini.SalesOrder.R.string.no_results);
			loadingView.addView(textView);
			return;
			
		}
		
		// No error and has results
		loadingView.setVisibility(View.GONE);
		
		// show search box
		View searchBoxView = findViewById(com.capgemini.SalesOrder.R.id.linearLayout1);
		searchBoxView.setVisibility(View.VISIBLE);

		EditText searchBox = (EditText) findViewById(com.capgemini.SalesOrder.R.id.search_box);
		FilterTextWatcher filterTextWatcher = new FilterTextWatcher(listAdapter);
		searchBox.addTextChangedListener(filterTextWatcher);
		searchBox.setVisibility(View.VISIBLE);
	}

	private class FilterTextWatcher implements TextWatcher
	{
		private Page3ListAdapter adapter;

		public FilterTextWatcher(Page3ListAdapter adapter)
		{
			this.adapter = adapter;
		}

		public void afterTextChanged(Editable s)
		{
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			adapter.filter(s);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(com.capgemini.SalesOrder.R.layout.list);
		setTitle("Order: " + parentEntry.getSoId());
		// initialize the Logger
		logger = new Logger();
		
		ZGWSAMPLE_SRVRequestHandler.getInstance(getApplicationContext()).register(this,
				ZGWSAMPLE_SRVRequestID.LOAD_LINEITEMS_FOR_SALESORDER);

		// make the request
		// the response should be in "requestCompleted"
		ZGWSAMPLE_SRVRequestHandler.getInstance(getApplicationContext()).loadLineItemsForSalesOrder((SalesOrder) parentEntry);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Intent intent = new Intent(this, Page5DetailsActivity.class);
		SalesOrderLineItem item = (SalesOrderLineItem) listAdapter.getItem(position);
		
		Page5DetailsActivity.parentEntry = item;
		
		// navigation to next screen
		startActivity(intent);

		super.onListItemClick(l, v, position, id);
	}

	@SuppressWarnings("unchecked")
	public void requestCompleted(ZGWSAMPLE_SRVRequestID requestID, List<?> entries, RequestStatus requestStatus) 
	{
		// first check the request's status
		StatusType type = requestStatus.getType();

		if (type == StatusType.OK) 
		{
			if (requestID.equals(ZGWSAMPLE_SRVRequestID.LOAD_LINEITEMS_FOR_SALESORDER))
			{
				// cast to the right type
				this.entries = (List<SalesOrderLineItem>) entries;
				// post in the UI
				mHandler.post(mUpdateResults);
			}
		} 
		else 
		{
			// do some error handling
			logger.e(TAG, "The request has returned with an error");
			entries = null;
			emessage = requestStatus.getMessage();
			mHandler.post(mUpdateResults);
		}

	}

	public void authenticationNeeded(String message) 
	{
		logger.e(TAG, "Authentication is needed");
		entries = null;
		emessage = message;
		mHandler.post(mUpdateResults);
		// navigate back to login page
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void batchCompleted(String batchID, IResponse response,
			RequestStatus requestStatus)
	{
		// here you can handle the response of the batch request.
	} 		
}
