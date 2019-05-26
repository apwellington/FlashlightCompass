package com.gibittec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.widget.ImageView;
 
public class ImageCompass extends ImageView {
     Paint mydPaint;
      int myddirection = 0;
 
      public ImageCompass(Context context) {
        super(context);
 
        mydPaint = new Paint();
        mydPaint.setColor(Color.WHITE);
        mydPaint.setStrokeWidth(2);
        mydPaint.setStyle(Style.STROKE);
 
        this.setImageResource(R.drawable.compassnavigator);
      }
 
      @Override
      public void onDraw(Canvas canvas) {
        int height = this.getHeight();
        int width = this.getWidth();
 
        canvas.rotate(myddirection, width / 2, height / 2);
        super.onDraw(canvas);
      }
 
      public void setDirection(int direction) {
        this.myddirection = direction;
        this.invalidate();
      }
 
  }
