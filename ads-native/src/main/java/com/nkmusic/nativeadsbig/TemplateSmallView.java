package com.musicplayer.nativeadsbig;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class TemplateSmallView extends FrameLayout {

  private int templateType;
  private NativeSmallTemplateStyle styles;
  private NativeAd nativeAd;
  private NativeAdView nativeAdView;
  private TextView primaryView;
  private TextView tertiaryView;
  private ImageView iconView;
  private TextView callToActionView;

  private static final String MEDIUM_TEMPLATE = "medium_template";
  private static final String SMALL_TEMPLATE = "small_template";

  public TemplateSmallView(Context context) {
    super(context);
  }

  public TemplateSmallView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  public TemplateSmallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context, attrs);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public TemplateSmallView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initView(context, attrs);
  }

  public void setStyles(NativeSmallTemplateStyle styles) {
    this.styles = styles;
    this.applyStyles();
  }

  public NativeAdView getNativeAdView() {
    return nativeAdView;
  }

  private void applyStyles() {

    Drawable mainBackground = styles.getMainBackgroundColor();
    if (mainBackground != null) {
      primaryView.setBackground(mainBackground);
      tertiaryView.setBackground(mainBackground);
    }

    Typeface primary = styles.getPrimaryTextTypeface();
    if (primary != null) {
      primaryView.setTypeface(primary);
    }

    Typeface secondary = styles.getSecondaryTextTypeface();
    if (secondary != null) {
    }

    Typeface tertiary = styles.getTertiaryTextTypeface();
    if (tertiary != null) {
      tertiaryView.setTypeface(tertiary);
    }

    Typeface ctaTypeface = styles.getCallToActionTextTypeface();
    int primaryTypefaceColor = styles.getPrimaryTextTypefaceColor();
    if (primaryTypefaceColor > 0) {
      primaryView.setTextColor(primaryTypefaceColor);
    }

    int secondaryTypefaceColor = styles.getSecondaryTextTypefaceColor();
    if (secondaryTypefaceColor > 0) {
    }

    int tertiaryTypefaceColor = styles.getTertiaryTextTypefaceColor();
    if (tertiaryTypefaceColor > 0) {
      tertiaryView.setTextColor(tertiaryTypefaceColor);
    }

    int ctaTypefaceColor = styles.getCallToActionTypefaceColor();

    float ctaTextSize = styles.getCallToActionTextSize();


    float primaryTextSize = styles.getPrimaryTextSize();
    if (primaryTextSize > 0) {
      primaryView.setTextSize(primaryTextSize);
    }

    float secondaryTextSize = styles.getSecondaryTextSize();
    if (secondaryTextSize > 0) {
    }

    float tertiaryTextSize = styles.getTertiaryTextSize();
    if (tertiaryTextSize > 0) {
      tertiaryView.setTextSize(tertiaryTextSize);
    }

    Drawable ctaBackground = styles.getCallToActionBackgroundColor();
    Drawable primaryBackground = styles.getPrimaryTextBackgroundColor();
    if (primaryBackground != null) {
      primaryView.setBackground(primaryBackground);
    }

    Drawable secondaryBackground = styles.getSecondaryTextBackgroundColor();
    if (secondaryBackground != null) {
    }

    Drawable tertiaryBackground = styles.getTertiaryTextBackgroundColor();
    if (tertiaryBackground != null) {
      tertiaryView.setBackground(tertiaryBackground);
    }

    invalidate();
    requestLayout();
  }

  private boolean adHasOnlyStore(NativeAd nativeAd) {
    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    return !isNullOrEmpty(store) && isNullOrEmpty(advertiser);
  }

  private boolean adHasOnlyAdvertiser(NativeAd nativeAd) {
    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    return !isNullOrEmpty(advertiser) && isNullOrEmpty(store);
  }

  private boolean adHasBothStoreAndAdvertiser(NativeAd nativeAd) {
    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    return (!isNullOrEmpty(advertiser)) && (!isNullOrEmpty(store));
  }

  private boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  public void setNativeAd(NativeAd nativeAd) {
    this.nativeAd = nativeAd;

    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    String headline = nativeAd.getHeadline();
    String body = nativeAd.getBody();
    String cta = nativeAd.getCallToAction();
    Double starRating = nativeAd.getStarRating();
    NativeAd.Image icon = nativeAd.getIcon();

    String tertiaryText;
    callToActionView.setText(cta);
    nativeAdView.setCallToActionView(callToActionView);

    if (adHasOnlyStore(nativeAd)) {
      nativeAdView.setStoreView(tertiaryView);
      tertiaryText = store;
    } else if (adHasOnlyAdvertiser(nativeAd)) {
      nativeAdView.setAdvertiserView(tertiaryView);
      tertiaryText = advertiser;
    } else if (adHasBothStoreAndAdvertiser(nativeAd)) {
      nativeAdView.setAdvertiserView(tertiaryView);
      tertiaryText = advertiser;
    } else {
      tertiaryText = "";
    }

    primaryView.setText(headline);
    tertiaryView.setText(tertiaryText);

    // Set the secondary view to be the star rating if available.
    // Otherwise fall back to the body text.

    if (icon != null) {
      iconView.setVisibility(VISIBLE);
      iconView.setImageDrawable(icon.getDrawable());
    } else {
      iconView.setVisibility(GONE);
    }

    nativeAdView.setNativeAd(nativeAd);
  }

  /**
   * To prevent memory leaks, make sure to destroy your ad when you don't need it anymore. This
   * method does not destroy the template view.
   * https://developers.google.com/admob/android/native-unified#destroy_ad
   */
  public void destroyNativeAd() {
    nativeAd.destroy();
  }

  public String getTemplateTypeName() {
    if (templateType == R.layout.gnt_small_template_view) {
      return MEDIUM_TEMPLATE;
    }
    return "";
  }

  private void initView(Context context, AttributeSet attributeSet) {
    TypedArray attributes =
        context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TemplateSmallView, 0, 0);

    try {
      templateType =
          attributes.getResourceId(
              R.styleable.TemplateSmallView_gnt_template_type_small, R.layout.gnt_small_template_view);
    } finally {
      attributes.recycle();
    }
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(templateType, this);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    nativeAdView = (NativeAdView) findViewById(R.id.native_ad_view);
    primaryView = (TextView) findViewById(R.id.primary);
    tertiaryView = (TextView) findViewById(R.id.tertiary);
    iconView = (ImageView) findViewById(R.id.icon);
    callToActionView = (TextView) findViewById(R.id.cta);
  }
}
