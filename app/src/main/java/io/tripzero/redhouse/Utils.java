package io.tripzero.redhouse;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ammonrees on 12/29/14.
 */
public class Utils {


    public static float centerX(View view){
        return ViewHelper.getX(view) + view.getWidth()/2;
    }

    public static float centerY(View view){
        return ViewHelper.getY(view) + view.getHeight()/2;
    }
}
