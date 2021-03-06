package com.odbol.dualwield.onboarding;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.burns.android.ancssample.MidiGattServer;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.util.LayoutUtil;

import java.util.List;

import com.burns.android.ancssample.R;

/**
 * Onboarding.
 */
public class OnboardingActivity extends AppIntro {

    public static final String TAG = "OnboardingActivity";

    public static final int PERMISSIONS_SLIDES_START_IDX = 2;

    private static final int REQUEST_ENABLE_BT = 2344;

    private int slideIndex = 0;

    private MidiGattServer midiServer;

    private ViewPager.PageTransformer transformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View page, float position) {
            // position is from -1 to 0 to 1

            // Get the page index from the tag. This makes
            // it possible to know which page index you're
            // currently transforming - and that can be used
            // to make some important performance improvements.
            int pagePosition = (int) page.getTag();

            OnboardingSlide slide = (OnboardingSlide) getSlides().get(pagePosition);
            List<ImageView> images = slide.getImageViews();

            Log.d(TAG, "pagePosition: " + pagePosition + " images: " + images);

            if (images.isEmpty()) return;

            // Here you can do all kinds of stuff, like get the
            // width of the page and perform calculations based
            // on how far the user has swiped the page.
            float pageWidth = page.getWidth();
            float pageWidthTimesPosition = pageWidth * position;
            float absPosition = Math.abs(position);

            float imageWidth = images.get(0).getWidth();
            float centerX = pageWidth / 2f - imageWidth / 2f;

            float maxDisplacement = imageWidth * 4f/5f;

            if (images.size() > 1) {
                images.get(1).setTranslationY(-maxDisplacement);
            }

            for (int i = 0; i < images.size(); i++) {
                float imagePosition = images.size() > 1 ? i - 1 : 0; // so center is first element
                float displacement = maxDisplacement * imagePosition;
                float endX = displacement;
                float startX = displacement;

                ImageView image = images.get(i);
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
//                params.setMarginStart((int) endX);
                image.setTranslationX(endX);
//                images[i].setTranslationX((startX - endX) * (1f - position));

                Log.d(TAG, i + "displacement: " + displacement + " pos: " + position);
            }


            // Now it's time for the effects
            if (position <= -1.0f || position >= 1.0f) {

                // The page is not visible. This is a good place to stop
                // any potential work / animations you may have running.

            } else if (position == 0.0f) {

                // The page is selected. This is a good time to reset Views
                // after animations as you can't always count on the PageTransformer
                // callbacks to match up perfectly.

            } else {

                // The page is currently being scrolled / swiped. This is
                // a good place to show animations that react to the user's
                // swiping as it provides a good user experience.

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_0,
                R.drawable.logo
        ));

        addSlide(OnboardingSlide.newInstance(
                getString(R.string.onboarding_desc_1),
                R.string.onboarding_label_1,
                R.drawable.logo
        ));

        addSlide(OnboardingSlide.newInstance(
                getString(R.string.onboarding_desc_2),
                R.string.onboarding_label_2,
                R.drawable.garageband_qr_code
        ));

        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_3,
                R.drawable.tutorial_1
        ));
        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_4,
                R.drawable.tutorial_2
        ));
        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_4,
                R.drawable.tutorial_3
        ));
        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_5,
                R.drawable.tutorial_4
        ));
        addSlide(OnboardingSlide.newInstance(
                getString(R.string.onboarding_desc_5),
                R.string.onboarding_label_5,
                R.drawable.tutorial_5
        ));
        addSlide(OnboardingSlide.newInstance(
                R.string.onboarding_label_6,
                R.drawable.logo
        ));

        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_SLIDES_START_IDX);


        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(getColor(R.color.primary));
        setIndicatorColor(getColor(R.color.text_light), getColor(R.color.text_light_disabled));
        setColorDoneText(getColor(R.color.text_light));
        setColorSkipButton(getColor(R.color.text_light_disabled));
        setNextArrowColor(getColor(R.color.text_light));
        setSeparatorColor(getColor(R.color.primary));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        // TODO: re-enable when you're sober
//        setCustomTransformer(transformer);

    }

    @Override
    public void onDestroy() {
        if (midiServer != null) {
            midiServer.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        int index = ((OnboardingSlide)currentFragment).getSlideIndex();
        if (index < PERMISSIONS_SLIDES_START_IDX) {
            goToPermissionsSlide();
        } else {
            goToDoneSlide();
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

        if (newFragment == null) return;

        // Start broadcasting BLE service once they've granted permissions.
        int index = ((OnboardingSlide)newFragment).getSlideIndex();
        Log.d(TAG, "onSlideChanged " + index);
        if (index == 2) {
            startBroadcasting();
        } else if (index == getSlides().size() - 2) {
            showSkipButton(false);
            setProgressButtonEnabled(false);
        } else {
            setProgressButtonEnabled(true);
        }
    }

    private void startBroadcasting() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (midiServer == null) {
            midiServer = new MidiGattServer(this);
            midiServer.setListener(this::onConnected);
        }
    }

    private void onConnected(BluetoothDevice bluetoothDevice) {
        runOnUiThread(() -> {
            new DeviceRepo(this).savePairedDevice(bluetoothDevice);
            setResult(RESULT_OK);
            goToDoneSlide();
            setProgressButtonEnabled(true);
        });
    }

    private void goToDoneSlide() {
        if (LayoutUtil.isRtl(getResources())) {
            pager.setCurrentItem(0);
        } else {
            pager.setCurrentItem(getSlides().size() - 1);
        }
    }

    public void goToPermissionsSlide() {
        if (LayoutUtil.isRtl(getResources())) {
            pager.setCurrentItem(getSlides().size() - PERMISSIONS_SLIDES_START_IDX);
        } else {
            pager.setCurrentItem(PERMISSIONS_SLIDES_START_IDX);
        }
    }

    public void addSlide(@NonNull OnboardingSlide fragment) {
        fragment.setSlideIndex(slideIndex++);
        super.addSlide(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBroadcasting();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}