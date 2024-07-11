package com.gavraanstudios.tipcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private EditText billAmountEditText, numberOfPeopleEditText, tipAmountEditText;
    private SeekBar tipSeekBar;
    private TextView tipPercentageTextView, totalAmountTextView, perPersonTextView;
    private Button copyButton, shareButton;
    private ImageView resetButton;


    private static final String AD_UNIT_ID = "ca-app-pub-1830908060344011/2710419977";
    private static final String TAG = "MyActivity";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adContainerView = findViewById(R.id.ad_view_container);

        // Initializing UI elements
        billAmountEditText = findViewById(R.id.billAmountEditText);
        numberOfPeopleEditText = findViewById(R.id.numberOfPeopleEditText);
        tipAmountEditText = findViewById(R.id.tipAmountEditText);
        tipSeekBar = findViewById(R.id.tipSeekBar);
        tipPercentageTextView = findViewById(R.id.tipPercentageTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        perPersonTextView = findViewById(R.id.perPersonTextView);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
         resetButton = findViewById(R.id.resetIcon);
       // settingsIcon = findViewById(R.id.settingsIcon);



        // Set default tip percentage
        tipPercentageTextView.setText(tipSeekBar.getProgress() + "%");
        tipAmountEditText.setText("0.00");
        tipSeekBar.setProgress(15); // Set default tip percentage to 15%
        tipPercentageTextView.setText("TIP " + tipSeekBar.getProgress() + "%");

        // Set limits for the number of people
        numberOfPeopleEditText.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "100") });


// Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        invalidateOptionsMenu();
                    }
                });

// This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            if (!initialLayoutComplete.getAndSet(true)
                                    && googleMobileAdsConsentManager.canRequestAds()) {
                                loadBanner();
                            }
                        });


        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());


        // Calculate tip amount when tip percentage changes
        tipSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tipPercentageTextView.setText("TIP " + progress +  "%");
                calculateTip();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle reset button click
                resetValues();
            }
        });

     /*** settingsIcon.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
              startActivity(intent);
          }
      });***/

        // Calculate tip amount when bill amount changes
        billAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTip();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Calculate tip amount when number of people changes
        numberOfPeopleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTip();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Copy button functionality
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });

        // Share button functionality
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText();
            }
        });
    }


    // Method to calculate tip
    private void calculateTip() {
        try {
            double billAmount = Double.parseDouble(billAmountEditText.getText().toString());
            int numberOfPeople = Integer.parseInt(numberOfPeopleEditText.getText().toString());
            double tipPercent = tipSeekBar.getProgress();

            // Check if bill amount is very small, set it to a minimum value of 0.01
            if (billAmount > 0 && billAmount < 0.01) {
                billAmount = 0.01;
            }

            // Calculate tip amount based on bill amount
            double tipAmount = billAmount * (tipPercent / 100);
            double totalAmount = billAmount + tipAmount;
            double perPersonAmount = totalAmount / numberOfPeople;

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

            totalAmountTextView.setText(decimalFormat.format(totalAmount));
            tipAmountEditText.setText(decimalFormat.format(tipAmount));
            perPersonTextView.setText(decimalFormat.format(perPersonAmount));

            // Set SeekBar progress
            tipSeekBar.setProgress((int) tipPercent);
            tipPercentageTextView.setText("TIP " + (int) tipPercent + "%");

        } catch (NumberFormatException e) {
            // Handle empty or invalid input
            totalAmountTextView.setText("0.00");
            tipAmountEditText.setText("");
            perPersonTextView.setText("0.00");
        }
    }

    private void resetValues() {
        billAmountEditText.setText("");
        numberOfPeopleEditText.setText("1");
        tipAmountEditText.setText("0.00");
        tipSeekBar.setProgress(15);
        tipPercentageTextView.setText("TIP 15%");
        totalAmountTextView.setText("0.00");
        perPersonTextView.setText("$ 0.00");

        calculateTip();
    }

   // Method to copy content to clipboard
    private void copyToClipboard() {
        String billAmount = billAmountEditText.getText().toString();
        String tipPercentage = tipSeekBar.getProgress() + "%";
        String tipAmount = tipAmountEditText.getText().toString();
        String numberOfPeople = numberOfPeopleEditText.getText().toString();
        String totalAmount = totalAmountTextView.getText().toString();
        String perPerson = perPersonTextView.getText().toString();

        String textToCopy = getString(R.string.bill_amount) + ": " + billAmount + "\n" +
                getString(R.string.tip_percentage) + ": " + tipPercentage + "\n" +
                getString(R.string.tip_amount) + ": " + tipAmount + "\n" +
                getString(R.string.number_of_people2) + ": " + numberOfPeople + "\n" +
                getString(R.string.per_person2) + ": " + perPerson + "\n" +
                getString(R.string.total_amount) + ": " + totalAmount;

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Tip Calculator", textToCopy);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    // Method to share content as text message
    private void shareText() {
        String billAmount = billAmountEditText.getText().toString();
        String tipPercentage = tipSeekBar.getProgress() + "%";
        String tipAmount = tipAmountEditText.getText().toString();
        String numberOfPeople = numberOfPeopleEditText.getText().toString();
        String totalAmount = totalAmountTextView.getText().toString();
        String perPerson = perPersonTextView.getText().toString();

        String textToShare = getString(R.string.bill_amount) + ": " + billAmount + "\n" +
                getString(R.string.tip_percentage) + ": " + tipPercentage + "\n" +
                getString(R.string.tip_amount) + ": " + tipAmount + "\n" +
                getString(R.string.number_of_people2) + ": " + numberOfPeople + "\n" +
                getString(R.string.per_person2) + ": " + perPerson + "\n" +
                getString(R.string.total_amount) + ": " + totalAmount;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void loadBanner() {
        // Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(AD_UNIT_ID);
        adView.setAdSize(getAdSize());

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(
                            @NonNull InitializationStatus initializationStatus) {}
                });

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner();
        }
    }

    // Get the ad size with screen width.
    public AdSize getAdSize() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            adWidthPixels = windowMetrics.getBounds().width();
        }

        float density = displayMetrics.density;
        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}
