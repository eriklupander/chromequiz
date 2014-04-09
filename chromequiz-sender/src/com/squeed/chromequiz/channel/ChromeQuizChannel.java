package com.squeed.chromequiz.channel;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.squeed.chromequiz.MainActivity;
import com.squeed.chromequiz.model.Question;

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
				case EVENT_QUESTION:
					activity.showQuestion(buildQuestion(msg.get("question")));
					break;
				case EVENT_GAME_STARTED:
					activity.updateGuiForGameStarted();
					break;
				case EVENT_QUESTION_TIMEOUT:
					activity.timeoutCurrentQuestion();
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
			} else if(msgType.equalsIgnoreCase(ChannelDef.COMMAND_TYPE)) {
				CommandDef cmdDef = CommandDef.valueOf(msg.getString(ChannelDef.COMMAND_ID));
				switch(cmdDef) {
				case COMMAND_NAME_REQUEST:
					activity.sendNameResponse(msg.getString(ChannelDef.PRM_CAST_ID));
					break;
				case COMMAND_SET_IS_MASTER:
					activity.setIsGameMaster(msg.getString(ChannelDef.PRM_CAST_ID));
					break;
				default:
					break;					
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	private Question buildQuestion(Object object) {
		// TODO Auto-generated method stub
		return null;
	}
}
