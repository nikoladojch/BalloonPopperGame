package com.example.nikola.gameapp.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Nikola on 05.01.2018.
 */

public class PixelHelper {

    public static int PixelsToDp(int px, Context context){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, px,
                context.getResources().getDisplayMetrics());
    }
}
