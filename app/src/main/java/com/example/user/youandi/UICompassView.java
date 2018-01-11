package com.example.user.youandi;

/**
 * Created by user on 2018-01-10.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 나침반을 표시하기 위한 클래스를 정의합니다.
 *
 * @author Mike
 *
 */
public class UICompassView extends View {
    private Drawable mCompass;
    private float mAzimuth = 0;
    private int PADDING = 2;

    public UICompassView(Context ctx) {
        super(ctx);

        this.mCompass = ctx.getResources().getDrawable(R.drawable.ic_arrow_upward_black_24dp);
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.rotate( mAzimuth, PADDING + mCompass.getMinimumWidth()
                / 2, PADDING + mCompass.getMinimumHeight() / 2);
        mCompass.setBounds(PADDING, PADDING, PADDING
                + mCompass.getMinimumWidth(), PADDING
                + mCompass.getMinimumHeight());

        mCompass.draw(canvas);
        canvas.restore();

        super.onDraw(canvas);
    }

    public void setAzimuth(float aAzimuth) {
        mAzimuth = aAzimuth;
    }

}