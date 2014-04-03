package com.squeed.chromequiz;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squeed.chromequiz.channel.ChromeQuizChannel;
import com.squeed.chromequiz.channel.Command;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";

	private static final String APP_NAME = "23BD4B33";
	
	private CastDevice mSelectedDevice;
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;

	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;

	private ChromeQuizChannel mChromeQuizChannel;

	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;
	
	// App-specific stuff...
	private boolean isGameMaster = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initMediaRouter();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		teardown();
		super.onDestroy();
	}
	
	private void initMediaRouter() {
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
				CastMediaControlIntent.categoryForCast(APP_NAME)).build();
		mMediaRouterCallback = new MediaRouterCallback();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(com.squeed.chromequiz.R.menu.menu, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(com.squeed.chromequiz.R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);

		// Set the MediaRouteActionProvider selector for device discovery.
		if (mediaRouteActionProvider != null)
			mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		
		return true;
	}
	
	
	/**
	 * An extension of the MediaRoute.Callback so we can invoke our own onRoute
	 * selected/unselected
	 */
	private class MediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
			Log.i(TAG, "onRouteSelected: " + route);
			mSelectedDevice = CastDevice.getFromBundle(route.getExtras());
			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
			Log.i(TAG, "onRouteUnselected: " + route);
			teardown();
			mSelectedDevice = null;
		}
	}
	
	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown();
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();

			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this).addApi(Cast.API, apiOptionsBuilder.build())
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener).build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		if (mApiClient != null) {
			if (mApplicationStarted) {
				try {
					Cast.CastApi.stopApplication(mApiClient);
					if (mChromeQuizChannel != null) {
						Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mChromeQuizChannel.getNamespace());
						mChromeQuizChannel = null;
					}
				} catch (IOException e) {
					Log.e(TAG, "Exception while removing channel", e);
				}
				mApplicationStarted = false;
			}
			if (mApiClient.isConnected()) {
				mApiClient.disconnect();
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						Log.d(TAG, "App is no longer running");
						teardown();
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
									mChromeQuizChannel.getNamespace(), mChromeQuizChannel);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					// Launch the receiver app
					Cast.CastApi.launchApplication(mApiClient, APP_NAME, false).setResultCallback(
							new ResultCallback<Cast.ApplicationConnectionResult>() {
								@Override
								public void onResult(ApplicationConnectionResult result) {
									Status status = result.getStatus();
									Log.d(TAG,
											"ApplicationConnectionResultCallback.onResult: statusCode"
													+ status.getStatusCode());
									if (status.isSuccess()) {
										ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
										String sessionId = result.getSessionId();
										String applicationStatus = result.getApplicationStatus();
										boolean wasLaunched = result.getWasLaunched();
										Log.d(TAG, "application name: " + applicationMetadata.getName() + ", status: "
												+ applicationStatus + ", sessionId: " + sessionId + ", wasLaunched: "
												+ wasLaunched);
										mApplicationStarted = true;

										// Create the custom message
										// channel
										mChromeQuizChannel = new ChromeQuizChannel(MainActivity.this);
										try {
											Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
													mChromeQuizChannel.getNamespace(), mChromeQuizChannel);
										} catch (IOException e) {
											Log.e(TAG, "Exception while creating channel", e);
										}

										// set the initial instructions
										// on the receiver
										// sendMessage(getString(R.string.app_name));
									} else {
										Log.e(TAG, "application could not launch");
										teardown();
									}
								}
							});
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to launch application", e);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended");
			mWaitingForReconnect = true;
		}
	}

	private void sendMessage(Command cmd) {
		if (mSelectedDevice == null || mApiClient == null || (mApiClient != null && !mApiClient.isConnected())) {
			if (mApiClient != null && mApiClient.isConnecting()) {
				Toast.makeText(MainActivity.this,
						"Currently connecting to Cast Device, please try again in a moment...", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(MainActivity.this,
						"Cast Device not connected, please try to disconnect and connect again", Toast.LENGTH_LONG)
						.show();
			}

			return;
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put("id", cmd.getId());
			obj.put("params", new JSONObject(cmd.getParams()));

			if (mApiClient != null && mChromeQuizChannel != null) {
				try {
					Cast.CastApi.sendMessage(mApiClient, mChromeQuizChannel.getNamespace(), obj.toString());
				} catch (Exception e) {
					Log.e(TAG, "Exception while sending message", e);
				}
			} else {
				Toast.makeText(MainActivity.this, "Unable to send CMD to receiver, no connection", Toast.LENGTH_SHORT)
						.show();
				launchReceiver();
			}
		} catch (JSONException e) {
			Toast.makeText(MainActivity.this, "Unable to serialize CMD into JSON: " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	
	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");

			teardown();
		}
	}

	/**
	 * Typically invoked from the comms channel if this sender is the first to have connected.
	 * 
	 * @param isGameMaster
	 */
	public void setIsGameMaster(boolean isGameMaster) {
		this.isGameMaster = isGameMaster;
	}

}
