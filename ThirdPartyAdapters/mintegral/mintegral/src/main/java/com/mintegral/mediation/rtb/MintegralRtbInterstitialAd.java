package com.mintegral.mediation.rtb;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.newinterstitial.out.MBBidNewInterstitialHandler;
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener;
import com.mbridge.msdk.out.MBridgeIds;
import com.mbridge.msdk.out.RewardInfo;
import com.mintegral.mediation.MintegralConstants;
import com.mintegral.mediation.MintegralUtils;


public class MintegralRtbInterstitialAd implements MediationInterstitialAd, NewInterstitialListener {

    private static final String TAG = MintegralRtbInterstitialAd.class.getSimpleName();
    /**
     * Data used to render an RTB interstitial ad.
     */
    private  MediationInterstitialAdConfiguration adConfiguration;

    /**
     * Callback object to notify the Google Mobile Ads SDK if ad rendering succeeded or failed.
     */
    private  MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback;

    private MBBidNewInterstitialHandler mbBidNewInterstitialHandler;
    private MediationInterstitialAdCallback interstitialAdCallback;

    public MintegralRtbInterstitialAd(MediationInterstitialAdConfiguration adConfiguration,
                                      MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback){
        this.adConfiguration = adConfiguration;
        this.callback = callback;
        String unitId = adConfiguration.getServerParameters().getString(MintegralConstants.AD_UNIT_ID);
        String placementId = adConfiguration.getServerParameters().getString(MintegralConstants.PLACEMENT_ID);
        mbBidNewInterstitialHandler = new MBBidNewInterstitialHandler(adConfiguration.getContext(),placementId,unitId);
        mbBidNewInterstitialHandler.setInterstitialVideoListener(this);
    }

    public void load(){
        String token = adConfiguration.getBidResponse();
        if(TextUtils.isEmpty(token)){
            AdError error = MintegralConstants.createAdapterError(MintegralConstants.ERROR_INVALID_BID_RESPONSE,"Failed to load rewarded ad from MIntegral. Missing or invalid bid response.");
            callback.onFailure(error);
            return;
        }
        mbBidNewInterstitialHandler.loadFromBid(token);
    }
    @Override
    public void showAd(@NonNull Context context) {
        boolean muted = MintegralUtils.shouldMuteAudio(adConfiguration.getMediationExtras());
        mbBidNewInterstitialHandler.playVideoMute(muted? MBridgeConstans.REWARD_VIDEO_PLAY_MUTE:MBridgeConstans.REWARD_VIDEO_PLAY_NOT_MUTE);
        mbBidNewInterstitialHandler.showFromBid();
    }

    @Override
    public void onLoadCampaignSuccess(MBridgeIds mBridgeIds) {

    }

    @Override
    public void onResourceLoadSuccess(MBridgeIds mBridgeIds) {
        interstitialAdCallback = callback.onSuccess(this);
    }

    @Override
    public void onResourceLoadFail(MBridgeIds mBridgeIds, String s) {
        AdError error = MintegralConstants.createSdkError(MintegralConstants.ERROR_SDK_INTER_ERROR, s);
        Log.w(TAG, error.toString());
        callback.onFailure(error);
    }

    @Override
    public void onAdShow(MBridgeIds mBridgeIds) {
        if(interstitialAdCallback != null){
            interstitialAdCallback.onAdOpened();
            interstitialAdCallback.reportAdImpression();
        }
    }

    @Override
    public void onAdClose(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
        if (interstitialAdCallback != null) {
            interstitialAdCallback.onAdClosed();
        }
    }

    @Override
    public void onShowFail(MBridgeIds mBridgeIds, String s) {
        if(interstitialAdCallback != null){
            AdError error = MintegralConstants.createAdapterError(MintegralConstants.ERROR_SDK_INTER_ERROR,s);
            Log.w(TAG, error.toString());
            interstitialAdCallback.onAdFailedToShow(error);
        }
    }

    @Override
    public void onAdClicked(MBridgeIds mBridgeIds) {
        if(interstitialAdCallback != null){
            interstitialAdCallback.reportAdClicked();
        }
    }

    @Override
    public void onVideoComplete(MBridgeIds mBridgeIds) {

    }

    @Override
    public void onAdCloseWithNIReward(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {

    }

    @Override
    public void onEndcardShow(MBridgeIds mBridgeIds) {

    }
}
