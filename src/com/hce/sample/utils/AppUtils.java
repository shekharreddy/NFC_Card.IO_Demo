package com.hce.sample.utils;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.util.Log;

/**
 * 
 * @author Shekhar.Nareddy
 * Common Utils to handle multiple functions.
 *
 */
public class AppUtils {

	private static final String TAG = AppUtils.class.getSimpleName();
	public static final String CARD_KEY = "card_key";

	// Check whether device supports NFC or not
	public static boolean isNFCSupported(Context appContext) {
		NfcManager nfcManager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
		NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();
		if (nfcAdapter != null) {
			Log.i(TAG, "NFC Supproted");
			return true;
			// Device compatible for NFC support
		} 
		Log.i(TAG, "NFC not Supproted");
		return false;
	}

	// Check whether NFC is enabled on the device on not
	public static boolean isNFCEnabled(Context appContext){
		NfcManager manager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
		NfcAdapter adapter = manager.getDefaultAdapter();
		if (adapter != null && adapter.isEnabled()) {
			Log.i(TAG, "NFC Supproted and Enabled");
			return true;
		} else if(adapter != null && !adapter.isEnabled()) { 
			Log.i(TAG, "NFC Supproted and Disabled");
			return false;
		}
		return false;
	}
}
