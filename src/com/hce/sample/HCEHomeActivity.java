package com.hce.sample;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hce.sample.utils.AESEncryptionHelper;
import com.hce.sample.utils.AppUtils;

/**
 * 
 * @author Shekhar.Nareddy
 * Launcher activity to show Credit Card scan and NFC send/receive options.
 * Displays Credit card details after scanning.
 */
public class HCEHomeActivity extends ActionBarActivity {
	private static final String TAG = HCEHomeActivity.class.getSimpleName();

	private static final String MY_CARDIO_APP_TOKEN = "3cd3b0c1d8cd439a8ae36df56b5f4d00"; 	// Card.IO app token 
	private static final int MY_SCAN_REQUEST_CODE = 2273;

	private TextView cardDetailsView =null; 

	private String creditCardDetails = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hcehome);
		Log.i(TAG, "onCreate called");

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_MAIN.equals(action)) {
			final Intent homeIntent = new Intent(getApplicationContext(), HCEHomeActivity.class);
			startActivity(homeIntent);
			finish();
		}
		cardDetailsView = (TextView) findViewById(R.id.card_details);

		if(!AppUtils.isNFCSupported(getApplicationContext())){
			// Disable the NFC button if NFC hardware not found.
			findViewById(R.id.nfc_share).setEnabled(false);
			findViewById(R.id.nfc_not_supported).setVisibility(View.VISIBLE);
		}
	}

	// Click Lister for Credit Card scan button
	public void onScanPress(View v) {
		Log.i(TAG, "Credit Card scan request called");

		final Intent scanIntent = new Intent(this, CardIOActivity.class);

		// required for authentication with card.io
		scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, MY_CARDIO_APP_TOKEN);

		// customize these values to suit your needs.
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: true
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

		// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
		startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
	}

	// Click Lister for NFC Sharing button.
	public void onNFCPress(View v) {

		if(!AppUtils.isNFCEnabled(getApplicationContext())){
			promptForNFCEnable();
		} else {
			final Intent nfcIntent = new Intent(this, NFCShareActivity.class);
			if(creditCardDetails != null){
				nfcIntent.putExtra(AppUtils.CARD_KEY, creditCardDetails);
			}
			startActivity(nfcIntent);
		}
	}

	// Prompt User to enable NFC if disabled.
	private void promptForNFCEnable() {
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.alert))
		.setMessage(getString(R.string.enable_nfc_msg))
		.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
			}
		})
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		}).show();
	}


	// Get and process Credit Card details after scanning.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult called for Credit Card scan");

		if (requestCode == MY_SCAN_REQUEST_CODE) {
			StringBuffer resultDisplayStr = new StringBuffer();

			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
				CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				// Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
				//	resultDisplayStr.append("Card Number: ").append(scanResult.getRedactedCardNumber()).append("\n");
				resultDisplayStr.append("Card Number: ").append(scanResult.getFormattedCardNumber()).append("\n");


				if (scanResult.isExpiryValid()) {
					resultDisplayStr.append("Expiration Date: ").append(scanResult.expiryMonth).append("/").append(scanResult.expiryYear).append("\n");
				}

				if (scanResult.cvv != null) {
					// Never log or display a CVV
					resultDisplayStr.append("CVV has ").append(scanResult.cvv.length()).append(" digits.\n");
				}

				if (scanResult.postalCode != null) {
					resultDisplayStr.append("Postal Code: ").append(scanResult.postalCode).append("\n");
				}
				creditCardDetails = resultDisplayStr.toString();
			}
			else {
				resultDisplayStr.append("Scan was canceled.");
			}
			// do something with resultDisplayStr, maybe display it in a textView
			cardDetailsView.setText(resultDisplayStr);
		}
		// else handle other activity results
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hcehome, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_info) {
			final Intent infoIntent = new Intent(getApplicationContext(), InfoActivity.class);
			startActivity(infoIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
