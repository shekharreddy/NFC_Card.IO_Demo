package com.hce.sample;


import com.hce.sample.utils.AESEncryptionHelper;
import com.hce.sample.utils.AppUtils;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * 
 * @author Shekhar.Nareddy
 *  Send/Receive Credit card details from other device. 
 *  Handling NFC functionality.
 */
public class NFCShareActivity extends ActionBarActivity {
	private static final String TAG = NFCShareActivity.class.getSimpleName();
	private NfcAdapter mNfcAdapter;
	private TextView cardDetailsView;

	private PendingIntent mNfcPendingIntent;
	private IntentFilter[] mNdefExchangeFilters;

	private String creditData = "";

	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate called");
		setContentView(R.layout.nfcschare_activity);
		cardDetailsView = ((TextView) findViewById(R.id.card_details));

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			creditData = extras.getString(AppUtils.CARD_KEY);
			if(creditData != null)
				cardDetailsView.setText(creditData);
		}
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		intiliazeNFCHandling();
	}

	// Initialize NFC to Handle data exchange.
	private void intiliazeNFCHandling(){
		Log.i(TAG, "Initializing NFC");

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// Handle all of our received NFC intents in this activity.
		mNfcPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Intent filters for reading a note from a tag or exchanging over p2p.
		final IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) { }
		mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume called");

		// Credit card details received from other device
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			NdefMessage[] messages = getNdefMessages(getIntent());
			byte[] payload = messages[0].getRecords()[0].getPayload();
			setNoteBody(new String(payload));
			setIntent(new Intent()); // Consume this intent.
		}
		enableNdefExchangeMode();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause called");

		mNfcAdapter.disableForegroundNdefPush(this);
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return super.onSupportNavigateUp();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "NFC Message Received");

		// NDEF exchange mode
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] msgs = getNdefMessages(intent);
			String body = new String(msgs[0].getRecords()[0].getPayload());
			if(body != null && !body.isEmpty()) {
				promptForContent(body);
			}
		}
	}

	// Prompt user to receive Credit Card details from other device.
	private void promptForContent(final String msg) {
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.alert))
		.setMessage(getString(R.string.get_nfc_details))
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				setNoteBody(msg);
			}
		})
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		}).show();
	}

	// Update Credit Card details on UI.
	private void setNoteBody(String body) {
		String decryptedData = AESEncryptionHelper.decrypt(body.getBytes());
		cardDetailsView.setText(decryptedData);
	}

	// To Get NFC Data
	private NdefMessage getNoteAsNdef() {
		byte[] textBytes = AESEncryptionHelper.encrypt(creditData) ;
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
				new byte[] {}, textBytes);
		return new NdefMessage(new NdefRecord[] {
				textRecord
		});
	}

	private NdefMessage[] getNdefMessages(Intent intent) {
		Log.i(TAG, "Parsing NFC Messages");

		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] {
						record
				});
				msgs = new NdefMessage[] {
						msg
				};
			}
		} else {
			Log.d(TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}

	private void enableNdefExchangeMode() {
		Log.i(TAG, "Enable NFC Message Exchange");
		mNfcAdapter.enableForegroundNdefPush(NFCShareActivity.this, getNoteAsNdef());
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
	}
}