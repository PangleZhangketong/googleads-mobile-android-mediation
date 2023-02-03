package com.google.ads.mediation.mintegral.waterfall;

import static com.google.ads.mediation.mintegral.MintegralConstants.ERROR_BANNER_SIZE_UNSUPPORTED;
import static com.google.ads.mediation.mintegral.MintegralMediationAdapter.TAG;

import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.ads.mediation.mintegral.MintegralConstants;
import com.google.ads.mediation.mintegral.MintegralUtils;
import com.google.ads.mediation.mintegral.mediation.MintegralBannerAd;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MediationUtils;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.mbridge.msdk.out.BannerSize;
import com.mbridge.msdk.out.MBBannerView;

import java.util.ArrayList;

public class MintegralWaterfallBannerAd extends MintegralBannerAd {

  public MintegralWaterfallBannerAd(
      @NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
      @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
          mediationAdLoadCallback) {
    super(mediationBannerAdConfiguration, mediationAdLoadCallback);
  }

  @Override
  public void loadAd() {
    BannerSize bannerSize = getMintegralBannerSizeFromAdMobAdSize(adConfiguration.getAdSize(),
        adConfiguration.getContext());
    if (bannerSize == null) {
      AdError bannerSizeError = MintegralConstants.createAdapterError(ERROR_BANNER_SIZE_UNSUPPORTED,
          String.format("The requested banner size: %s is not supported by Mintegral SDK.",
              adConfiguration.getAdSize()));
      Log.e(TAG, bannerSizeError.toString());
      adLoadCallback.onFailure(bannerSizeError);
      return;
    }

    String adUnitId = adConfiguration.getServerParameters()
        .getString(MintegralConstants.AD_UNIT_ID);
    String placementId = adConfiguration.getServerParameters()
        .getString(MintegralConstants.PLACEMENT_ID);
    AdError error = MintegralUtils.validateMintegralAdLoadParams(adUnitId, placementId);
    if (error != null) {
      adLoadCallback.onFailure(error);
      return;
    }
    mbBannerView = new MBBannerView(adConfiguration.getContext());
    mbBannerView.init(bannerSize, placementId, adUnitId);
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(bannerSize.getWidth(),
        bannerSize.getHeight());
    mbBannerView.setLayoutParams(layoutParams);
    mbBannerView.setBannerAdListener(this);
    mbBannerView.load();
  }
}
