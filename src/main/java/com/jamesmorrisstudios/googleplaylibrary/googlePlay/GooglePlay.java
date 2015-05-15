package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.request.GameRequest;
import com.jamesmorrisstudios.utilitieslibrary.Logger;

import java.util.ArrayList;

/**
 * Created by James on 5/11/2015.
 */
public class GooglePlay implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = "GooglePlay";

    private static GooglePlay instance;

    private GooglePlay() {}

    public static GooglePlay getInstance() {
        if(instance == null) {
            instance = new GooglePlay();
        }
        return instance;
    }

    //Normal class flow below this

    // Are we expecting the result of a resolution flow?
    boolean mExpectingResolution = false;
    // was the sign-in flow cancelled when we tried it?
    // if true, we know not to try again automatically.
    boolean mSignInCancelled = false;
    /**
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    Activity mActivity = null;
    // app context
    Context mAppContext = null;
    // the Google API client builder we will use to create GoogleApiClient
    GoogleApiClient.Builder mGoogleApiClientBuilder = null;
    // Api options to use when adding each API, null for none
    Games.GamesOptions mGamesApiOptions = Games.GamesOptions.builder().build();
    Api.ApiOptions.NoOptions mAppStateApiOptions = null;
    // Google API client object we manage.
    GoogleApiClient mGoogleApiClient = null;

    // Whether to automatically try to sign in on onStart(). We only set this
    // to true when the sign-in process fails or the user explicitly signs out.
    // We set it back to false when the user initiates the sign in process.
    boolean mConnectOnStart = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    ConnectionResult mConnectionResult = null;

    // The error that happened during sign-in.
    SignInFailureReason mSignInFailureReason = null;

    // Should we show error dialog boxes?
    boolean mShowErrorDialogs = true;

    // Print debug logs?
    boolean mDebugLog = false;

    Handler mHandler;

    /*
     * If we got an invitation when we connected to the games client, it's here.
     * Otherwise, it's null.
     */
    Invitation mInvitation;

    /*
     * If we got turn-based match when we connected to the games client, it's
     * here. Otherwise, it's null.
     */
    TurnBasedMatch mTurnBasedMatch;

    /*
     * If we have incoming requests when we connected to the games client, they
     * are here. Otherwise, it's null.
     */
    ArrayList<GameRequest> mRequests;

    // Listener
    GameHelperListener mListener = null;
    int mMaxAutoSignInAttempts = GooglePlayUtils.DEFAULT_MAX_SIGN_IN_ATTEMPTS;
    // configuration done?
    private boolean mSetupDone = false;
    // are we currently connecting?
    private boolean mConnecting = false;

    /**
     * Init the object, initially tied to the given Activity.
     * After constructing this object, call @link{setup} from the onCreate()
     * method of your Activity.
     *
     */
    public void initGooglePlay(@NonNull Activity activity) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
        mHandler = new Handler();
    }

    /**
     * Sets the maximum bitIndex of automatic sign-in attempts to be made on
     * application startup. This maximum is over the lifetime of the application
     * (it is stored in a Preferences file). So, for example, if you
     * specify 2, then it means that the user will be prompted to sign in on app
     * startup the first time and, if they cancel, a second time the next time
     * the app starts, and, if they cancel that one, never again. Set to 0 if
     * you do not want the user to be prompted to sign in on application
     * startup.
     */
    public void setMaxAutoSignInAttempts(int max) {
        mMaxAutoSignInAttempts = max;
    }

    void assertConfigured(@NonNull String operation) {
        if (!mSetupDone) {
            String error = "GameHelper error: Operation attempted without setup: "
                    + operation
                    + ". The setup() method must be called before attempting any other operation.";
            logError(error);
            throw new IllegalStateException(error);
        }
    }

    private void doApiOptionsPreCheck() {
        if (mGoogleApiClientBuilder != null) {
            String error = "GameHelper: you cannot call set*ApiOptions after the client "
                    + "builder has been created. Call it before calling createApiClientBuilder() "
                    + "or setup().";
            logError(error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * Sets the options to pass when setting up the Games API. Call before
     * setup().
     */
    public void setGamesApiOptions(@NonNull Games.GamesOptions options) {
        doApiOptionsPreCheck();
        mGamesApiOptions = options;
    }

    /**
     * Sets the options to pass when setting up the AppState API. Call before
     * setup().
     */
    public void setAppStateApiOptions(@NonNull Api.ApiOptions.NoOptions options) {
        doApiOptionsPreCheck();
        mAppStateApiOptions = options;
    }

    /**
     * Creates a GoogleApiClient.Builder for use with @link{#setup}. Normally,
     * you do not have to do this; use this method only if you need to make
     * nonstandard setup (e.g. adding extra scopes for other APIs) on the
     * GoogleApiClient.Builder before calling @link{#setup}.
     */
    @NonNull
    public GoogleApiClient.Builder createApiClientBuilder() {
        if (mSetupDone) {
            String error = "GameHelper: you called GameHelper.createApiClientBuilder() after "
                    + "calling setup. You can only get a client builder BEFORE performing setup.";
            logError(error);
            throw new IllegalStateException(error);
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(mActivity, this, this);

        //Add games
        builder.addApi(Games.API, mGamesApiOptions);
        builder.addScope(Games.SCOPE_GAMES);

        //Add snapshot
        builder.addScope(Drive.SCOPE_APPFOLDER);
        builder.addApi(Drive.API);

        mGoogleApiClientBuilder = builder;
        return builder;
    }

    /**
     * Performs setup on this GameHelper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener The listener to be notified of sign-in events.
     */
    public void setup(@NonNull GameHelperListener listener) {
        if (mSetupDone) {
            String error = "GameHelper: you cannot call GameHelper.setup() more than once!";
            logError(error);
            throw new IllegalStateException(error);
        }
        mListener = listener;
        debugLog("Setup: requested clients: ");

        if (mGoogleApiClientBuilder == null) {
            // we don't have a builder yet, so create one
            createApiClientBuilder();
        }

        mGoogleApiClient = mGoogleApiClientBuilder.build();
        mGoogleApiClientBuilder = null;
        mSetupDone = true;
    }

    /**
     * Returns the GoogleApiClient object. In order to call this method, you
     * must have called @link{setup}.
     */
    @NonNull
    public GoogleApiClient getApiClient() {
        if (mGoogleApiClient == null) {
            throw new IllegalStateException(
                    "No GoogleApiClient. Did you call setup()?");
        }
        return mGoogleApiClient;
    }

    /**
     * Returns whether or not the user is signed in.
     */
    public boolean isSignedIn() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    /**
     * Returns whether or not we are currently connecting
     */
    public boolean isConnecting() {
        return mConnecting;
    }

    /**
     * Returns whether or not there was a (non-recoverable) error during the
     * sign-in process.
     */
    public boolean hasSignInError() {
        return mSignInFailureReason != null;
    }

    /**
     * Returns the error that happened during the sign-in process, null if no
     * error occurred.
     */
    @NonNull
    public SignInFailureReason getSignInError() {
        return mSignInFailureReason;
    }

    // Set whether to show error dialogs or not.
    public void setShowErrorDialogs(boolean show) {
        mShowErrorDialogs = show;
    }

    /**
     * Call this method from your Activity's onStart().
     */
    public void onStart(@NonNull Activity act) {
        mActivity = act;
        mAppContext = act.getApplicationContext();

        debugLog("onStart");
        assertConfigured("onStart");

        if (mConnectOnStart) {
            if (mGoogleApiClient.isConnected()) {
                Log.w(TAG, "GameHelper: client was already connected on onStart()");
            } else {
                debugLog("Connecting client.");
                mConnecting = true;
                mGoogleApiClient.connect();
            }
        } else {
            debugLog("Not attempting to connect becase mConnectOnStart=false");
            debugLog("Instead, reporting a sign-in failure.");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyListener(false);
                }
            }, 1000);
        }
    }

    /**
     * Call this method from your Activity's onStop().
     */
    public void onStop() {
        debugLog("onStop");
        assertConfigured("onStop");

        if (mGoogleApiClient.isConnected()) {
            debugLog("Disconnecting client due to onStop");
            mGoogleApiClient.disconnect();
        } else {
            debugLog("Client already disconnected when we got onStop.");
        }
        mConnecting = false;
        mExpectingResolution = false;

        // let go of the Activity reference
        mActivity = null;
    }

    /**
     * Returns the invitation ID received through an invitation notification.
     * This should be called from your GameHelperListener's
     *
     * @return The id of the invitation, or null if none was received.
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's an
     * invitation available. In that
     * case, accept the invitation.
     */
    @Nullable
    public String getInvitationId() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getInvitationId() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return mInvitation == null ? null : mInvitation.getInvitationId();
    }

    /**
     * Returns the invitation received through an invitation notification. This
     * should be called from your GameHelperListener's
     *
     * @return The invitation, or null if none was received.
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's an
     * invitation available. In that
     * case, accept the invitation.
     */
    @Nullable
    public Invitation getInvitation() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getInvitation() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return mInvitation;
    }

    public boolean hasInvitation() {
        return mInvitation != null;
    }

    public boolean hasTurnBasedMatch() {
        return mTurnBasedMatch != null;
    }

    public boolean hasRequests() {
        return mRequests != null;
    }

    public void clearInvitation() {
        mInvitation = null;
    }

    public void clearTurnBasedMatch() {
        mTurnBasedMatch = null;
    }

    public void clearRequests() {
        mRequests = null;
    }

    /**
     * Returns the tbmp match received through an invitation notification. This
     * should be called from your GameHelperListener's
     *
     * @return The match, or null if none was received.
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's a
     * match available.
     */
    @Nullable
    public TurnBasedMatch getTurnBasedMatch() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getTurnBasedMatch() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return mTurnBasedMatch;
    }

    /**
     * Returns the requests received through the onConnected bundle. This should
     * be called from your GameHelperListener's
     *
     * @return The requests, or null if none were received.
     * @link{GameHelperListener#onSignInSucceeded method, to check if there are
     * incoming requests that must be
     * handled.
     */
    @Nullable
    public ArrayList<GameRequest> getRequests() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "Warning: getRequests() should only be called "
                    + "when signed in, "
                    + "that is, after getting onSignInSuceeded()");
        }
        return mRequests;
    }

    /**
     * Enables debug logging
     */
    public void enableDebugLog(boolean enabled) {
        mDebugLog = enabled;
        if (enabled) {
            debugLog("Debug log enabled.");
        }
    }

    /**
     * Sign out and disconnect from the APIs.
     */
    public void signOut() {
        if (!mGoogleApiClient.isConnected()) {
            // nothing to do
            debugLog("signOut: was already disconnected, ignoring.");
            return;
        }

        // For the games client, signing out means calling signOut and
        // disconnecting
        debugLog("Signing out from the Google API Client.");
        Games.signOut(mGoogleApiClient);

        // Ready to disconnect
        debugLog("Disconnecting client.");
        mConnectOnStart = false;
        mConnecting = false;
        mGoogleApiClient.disconnect();
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode, @Nullable Intent intent) {
        debugLog("onActivityResult: req="
                + (requestCode == GooglePlayUtils.RC_RESOLVE ? "RC_RESOLVE" : String
                .valueOf(requestCode)) + ", resp="
                + GooglePlayUtils.activityResponseCodeToString(responseCode));
        if (requestCode != GooglePlayUtils.RC_RESOLVE) {
            debugLog("onActivityResult: request code not meant for us. Ignoring.");
            return;
        }

        // no longer expecting a resolution
        mExpectingResolution = false;

        if (!mConnecting) {
            debugLog("onActivityResult: ignoring because we are not connecting.");
            return;
        }

        // We're coming back from an activity that was launched to resolve a
        // connection problem. For example, the sign-in UI.
        if (responseCode == Activity.RESULT_OK) {
            // Ready to try to connect again.
            debugLog("onAR: Resolution was RESULT_OK, so connecting current client again.");
            connect();
        } else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            debugLog("onAR: Resolution was RECONNECT_REQUIRED, so reconnecting.");
            connect();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // User cancelled.
            debugLog("onAR: Got a cancellation result, so disconnecting.");
            mSignInCancelled = true;
            mConnectOnStart = false;
            mUserInitiatedSignIn = false;
            mSignInFailureReason = null; // cancelling is not a failure!
            mConnecting = false;
            mGoogleApiClient.disconnect();

            // increment # of cancellations
            int prevCancellations = getSignInCancellations();
            int newCancellations = incrementSignInCancellations();
            debugLog("onAR: # of cancellations " + prevCancellations + " --> "
                    + newCancellations + ", max " + mMaxAutoSignInAttempts);

            notifyListener(false);
        } else {
            // Whatever the problem we were trying to solve, it was not
            // solved. So give up and show an error message.
            debugLog("onAR: responseCode="
                    + GooglePlayUtils
                    .activityResponseCodeToString(responseCode)
                    + ", so giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode(),
                    responseCode));
        }
    }

    void notifyListener(boolean success) {
        debugLog("Notifying LISTENER of sign-in "
                + (success ? "SUCCESS"
                : mSignInFailureReason != null ? "FAILURE (error)"
                : "FAILURE (no error)"));
        if (mListener != null) {
            if (success) {
                mListener.onSignInSucceeded();
            } else {
                mListener.onSignInFailed();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process, the GameHelperListener's
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */
    public void beginUserInitiatedSignIn() {
        debugLog("beginUserInitiatedSignIn: resetting attempt count.");
        resetSignInCancellations();
        mSignInCancelled = false;
        mConnectOnStart = true;

        if (mGoogleApiClient.isConnected()) {
            // nothing to do
            logWarn("beginUserInitiatedSignIn() called when already connected. "
                    + "Calling listener directly to notify of success.");
            notifyListener(true);
            return;
        } else if (mConnecting) {
            logWarn("beginUserInitiatedSignIn() called when already connecting. "
                    + "Be patient! You can only call this method after you get an "
                    + "onSignInSucceeded() or onSignInFailed() callback. Suggestion: disable "
                    + "the sign-in button on startup and also when it's clicked, and re-enable "
                    + "when you get the callback.");
            // ignore call (listener will get a callback when the connection
            // process finishes)
            return;
        }

        debugLog("Starting USER-INITIATED sign-in flow.");

        // indicate that user is actively trying to sign in (so we know to
        // resolve
        // connection problems by showing dialogs)
        mUserInitiatedSignIn = true;

        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
            mConnecting = true;
            resolveConnectionResult();
        } else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
            mConnecting = true;
            connect();
        }
    }

    void connect() {
        if (mGoogleApiClient.isConnected()) {
            debugLog("Already connected.");
            return;
        }
        debugLog("Starting connection.");
        mConnecting = true;
        mInvitation = null;
        mTurnBasedMatch = null;
        mGoogleApiClient.connect();
    }

    /**
     * Disconnects the API client, then connects again.
     */
    public void reconnectClient() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "reconnectClient() called when client is not connected.");
            // interpret it as a request to connect
            connect();
        } else {
            debugLog("Reconnecting client.");
            mGoogleApiClient.reconnect();
        }
    }

    /**
     * Called when we successfully obtain a connection to a client.
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        debugLog("onConnected: connected!");

        if (connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                debugLog("onConnected: connection hint has a room invite!");
                mInvitation = inv;
                debugLog("Invitation ID: " + mInvitation.getInvitationId());
            }

            // Do we have any requests pending?
            mRequests = Games.Requests
                    .getGameRequestsFromBundle(connectionHint);
            if (!mRequests.isEmpty()) {
                // We have requests in onConnected's connectionHint.
                debugLog("onConnected: connection hint has " + mRequests.size()
                        + " request(s)");
            }

            debugLog("onConnected: connection hint provided. Checking for TBMP game.");
            mTurnBasedMatch = connectionHint
                    .getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
        }

        // we're good to go
        succeedSignIn();
    }

    void succeedSignIn() {
        debugLog("succeedSignIn");
        mSignInFailureReason = null;
        mConnectOnStart = true;
        mUserInitiatedSignIn = false;
        mConnecting = false;
        notifyListener(true);
    }

    // Return the bitIndex of times the user has cancelled the sign-in flow in the
    // life of the app
    int getSignInCancellations() {
        SharedPreferences sp = mAppContext.getSharedPreferences(
                GooglePlayUtils.GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE);
        return sp.getInt(GooglePlayUtils.KEY_SIGN_IN_CANCELLATIONS, 0);
    }

    // Increments the counter that indicates how many times the user has
    // cancelled the sign in
    // flow in the life of the application
    int incrementSignInCancellations() {
        int cancellations = getSignInCancellations();
        SharedPreferences.Editor editor = mAppContext.getSharedPreferences(
                GooglePlayUtils.GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(GooglePlayUtils.KEY_SIGN_IN_CANCELLATIONS, cancellations + 1);
        editor.apply();
        return cancellations + 1;
    }

    // Reset the counter of how many times the user has cancelled the sign-in
    // flow.
    void resetSignInCancellations() {
        SharedPreferences.Editor editor = mAppContext.getSharedPreferences(
                GooglePlayUtils.GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(GooglePlayUtils.KEY_SIGN_IN_CANCELLATIONS, 0);
        editor.apply();
    }

    /**
     * Handles a connection failure.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // save connection result for later reference
        debugLog("onConnectionFailed");

        mConnectionResult = result;
        debugLog("Connection failure:");
        debugLog("   - code: "
                + GooglePlayUtils.errorCodeToString(mConnectionResult
                .getErrorCode()));
        debugLog("   - resolvable: " + mConnectionResult.hasResolution());
        debugLog("   - details: " + mConnectionResult.toString());

        int cancellations = getSignInCancellations();
        boolean shouldResolve = false;

        if (mUserInitiatedSignIn) {
            debugLog("onConnectionFailed: WILL resolve because user initiated sign-in.");
            shouldResolve = true;
        } else if (mSignInCancelled) {
            debugLog("onConnectionFailed WILL NOT resolve (user already cancelled once).");
            shouldResolve = false;
        } else if (cancellations < mMaxAutoSignInAttempts) {
            debugLog("onConnectionFailed: WILL resolve because we have below the max# of "
                    + "attempts, "
                    + cancellations
                    + " < "
                    + mMaxAutoSignInAttempts);
            shouldResolve = true;
        } else {
            shouldResolve = false;
            debugLog("onConnectionFailed: Will NOT resolve; not user-initiated and max attempts "
                    + "reached: "
                    + cancellations
                    + " >= "
                    + mMaxAutoSignInAttempts);
        }

        if (!shouldResolve) {
            // Fail and wait for the user to want to sign in.
            debugLog("onConnectionFailed: since we won't resolve, failing now.");
            mConnectionResult = result;
            mConnecting = false;
            notifyListener(false);
            return;
        }

        debugLog("onConnectionFailed: resolving problem...");

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve
     * starting a UI flow that lets the user give the appropriate consents
     * necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem
        if (mExpectingResolution) {
            debugLog("We're already expecting the result of a previous resolution.");
            return;
        }

        debugLog("resolveConnectionResult: trying to resolve result: "
                + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("Result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingResolution = true;
                mConnectionResult.startResolutionForResult(mActivity,
                        GooglePlayUtils.RC_RESOLVE);
            } catch (IntentSender.SendIntentException e) {
                // Try connecting again
                debugLog("SendIntentException, so connecting again.");
                connect();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode()));
        }
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            debugLog("Disconnecting client.");
            mGoogleApiClient.disconnect();
        } else {
            Log.w(TAG,
                    "disconnect() called when client was already disconnected.");
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error
     * message to the user, using a standard error dialog as appropriate to the
     * cause of the error. That dialog will indicate to the user how the problem
     * can be solved (for example, re-enable Google Play Services, upgrade to a
     * new version, etc).
     */
    void giveUp(@NonNull SignInFailureReason reason) {
        mConnectOnStart = false;
        disconnect();
        mSignInFailureReason = reason;

        //showFailureDialog();
        mConnecting = false;
        notifyListener(false);
    }

    /**
     * Called when we are disconnected from the Google API client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        debugLog("onConnectionSuspended, cause=" + cause);
        disconnect();
        mSignInFailureReason = null;
        debugLog("Making extraordinary call to onSignInFailed callback");
        mConnecting = false;
        notifyListener(false);
    }

    void debugLog(@NonNull String message) {
        Logger.d(Logger.LoggerCategory.MAIN, TAG, "GameHelper: " + message);
    }

    void logWarn(@NonNull String message) {
        Logger.w(Logger.LoggerCategory.MAIN, TAG, "!!! GameHelper WARNING: " + message);
    }

    void logError(@NonNull String message) {
        Logger.e(Logger.LoggerCategory.MAIN, TAG, "*** GameHelper ERROR: " + message);
    }

    // Not recommended for general use. This method forces the
    // "connect on start" flag
    // to a given state. This may be useful when using GameHelper in a
    // non-standard
    // sign-in flow.
    public void setConnectOnStart(boolean connectOnStart) {
        debugLog("Forcing mConnectOnStart=" + connectOnStart);
        mConnectOnStart = connectOnStart;
    }

    /**
     * Listener for sign-in success or failure events.
     */
    public interface GameHelperListener {
        /**
         * Called when sign-in fails. As a result, a "Sign-In" button can be
         * shown to the user; when that button is clicked, call
         *
         * @link{GamesHelper#beginUserInitiatedSignIn . Note that not all calls
         * to this method mean an
         * error; it may be a result
         * of the fact that automatic
         * sign-in could not proceed
         * because user interaction
         * was required (consent
         * dialogs). So
         * implementations of this
         * method should NOT display
         * an error message unless a
         * call to @link{GamesHelper#
         * hasSignInError} indicates
         * that an error indeed
         * occurred.
         */
        void onSignInFailed();

        /**
         * Called when sign-in succeeds.
         */
        void onSignInSucceeded();
    }

    // Represents the reason for a sign-in failure
    public static class SignInFailureReason {
        public static final int NO_ACTIVITY_RESULT_CODE = -100;
        int mServiceErrorCode = 0;
        int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

        public SignInFailureReason(int serviceErrorCode, int activityResultCode) {
            mServiceErrorCode = serviceErrorCode;
            mActivityResultCode = activityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode) {
            this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
        }

        public int getServiceErrorCode() {
            return mServiceErrorCode;
        }

        public int getActivityResultCode() {
            return mActivityResultCode;
        }

        @Override
        public String toString() {
            return "SignInFailureReason(serviceErrorCode:"
                    + GooglePlayUtils.errorCodeToString(mServiceErrorCode)
                    + ((mActivityResultCode == NO_ACTIVITY_RESULT_CODE) ? ")"
                    : (",activityResultCode:"
                    + GooglePlayUtils
                    .activityResponseCodeToString(mActivityResultCode) + ")"));
        }
    }
}
