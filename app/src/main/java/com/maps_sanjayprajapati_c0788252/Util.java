package com.maps_sanjayprajapati_c0788252;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.maps.android.ui.IconGenerator;

public class Util {

    public static Bitmap generateCustomMarkerWithText(Context context, String text) {
        IconGenerator icg = new IconGenerator(context);
        icg.setColor(Color.parseColor("#006400"));
        icg.setTextAppearance(R.style.WhiteText);
        return icg.makeIcon(text);
    }

    public static double roundDecimal(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
