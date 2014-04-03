package com.squeed.chromequiz.channel;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.squeed.chromequiz.MainActivity;

public class ChromeQuizChannel implements MessageReceivedCallback {
	
	public static final String PROTOCOL = "urn:x-cast:com.squeed.chromequiz";
	public static final String TAG = "ChromeQuizChannel";
	
	private MainActivity activity;

	/**
	 * Construct the channel instance, provide reference to the activity so we can perform
	 * convenient callbacks.
	 * 
	 * @param activity
	 */
	public ChromeQuizChannel(MainActivity activity) {
		this.activity = activity;		
	}

	public String getNamespace() {
		return PROTOCOL;
	}

	/*
	 * Receive message from the receiver app
	 */
	@Override
	public void onMessageReceived(CastDevice castDevice, String namespace,
			String message) {
		Log.d(TAG, "onMessageReceived: " + message);
		
		if(!namespace.equalsIgnoreCase(PROTOCOL)) {
			Log.i(TAG, "Discarded message from unknown namespace: " + namespace);
		}
		try {
			JSONObject msg = new JSONObject(message);
			String msgType = msg.getString(ChannelDef.TYPE);
			if(msgType.equalsIgnoreCase(ChannelDef.EVENT_TYPE)) {
				EventDef evt = EventDef.valueOf(msg.getString(ChannelDef.EVENT_ID));
				switch(evt) {
				case EVENT_GAME_MASTER_TRUE:
					activity.setIsGameMaster(true);
					break;
				case EVENT_GAME_MASTER_FALSE:
					activity.setIsGameMaster(false);
					break;
				default:
					break;
				}
			}
			
			else if(msgType.equalsIgnoreCase(ChannelDef.RESPONSE_TYPE)) {
				ResponseDef rsp = ResponseDef.valueOf(msg.getString(ChannelDef.RESPONSE_ID));
				switch(rsp) {
					default:
						break;					
				}
			}
			
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
