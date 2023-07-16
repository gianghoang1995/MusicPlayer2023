package com.downloadmp3player.nativeadsbig;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

/**
 * Base class for a template view. *
 */
public class TemplateView extends FrameLayout {

    private int templateType;
    private NativeTemplateStyle styles;
    private NativeAd nativeAd;
    private NativeAdView nativeAdView;
    private TextView primaryView;
    private TextView tertiaryView;
    private ImageView iconView;
    private MediaView mediaView;
    private TextView callToActionView;

    private static final String MEDIUM_TEMPLATE = "medium_template";
    private static final String SMALL_TEMPLATE = "small_template";

    public TemplateView(Context context) {
        super(context);
    }

    public TemplateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public TemplateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TemplateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public void setStyles(NativeTemplateStyle styles) {
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
        if (ctaTypeface != null) {
            callToActionView.setTypeface(ctaTypeface);
        }

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
        if (ctaTypefaceColor > 0) {
            callToActionView.setTextColor(ctaTypefaceColor);
        }

        float ctaTextSize = styles.getCallToActionTextSize();
        if (ctaTextSize > 0) {
            callToActionView.setTextSize(ctaTextSize);
        }

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
        if (ctaBackground != null) {
            callToActionView.setBackground(ctaBackground);
        }

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

        String tertiaryText;

        nativeAdView.setCallToActionView(callToActionView);
        nativeAdView.setMediaView(mediaView);

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
        callToActionView.setText(cta);

        if (nativeAd.getIcon() == null) {
            iconView.setVisibility(GONE);
        } else {
            nativeAdView.setIconView(iconView);
            iconView.setVisibility(VISIBLE);
            iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
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
        if (templateType == R.layout.gnt_medium_template_view) {
            return MEDIUM_TEMPLATE;
        }
        return "";
    }

    private void initView(Context context, AttributeSet attributeSet) {

        TypedArray attributes =
                context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TemplateView, 0, 0);

        try {
            templateType =
                    attributes.getResourceId(
                            R.styleable.TemplateView_gnt_template_type, R.layout.gnt_medium_template_view);
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
        callToActionView = (TextView) findViewById(R.id.cta);
        iconView = (ImageView) findViewById(R.id.ad_app_icon);
        mediaView = (MediaView) findViewById(R.id.media_view);
    }
}
