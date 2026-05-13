package com.walkitalki.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public final class WalkiTalkiStatusView extends View {
    private final AppTalkViewModel viewModel;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WalkiTalkiStatusView(Context context, AppUiCopy copy) {
        this(context, AppTalkViewModel.createReadyModel());
    }

    public WalkiTalkiStatusView(Context context, AppTalkController controller) {
        this(context, AppTalkViewModel.fromController(controller));
    }

    public WalkiTalkiStatusView(Context context, AppTalkViewModel viewModel) {
        super(context);
        this.viewModel = viewModel;
        updateContentDescription(viewModel.state());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        AppTalkRenderModel model = AppTalkUiRenderer.render(viewModel);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            viewModel.onIntent(model.pressIntent());
            updateContentDescription(viewModel.state());
            invalidate();
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            viewModel.onIntent(model.releaseIntent());
            updateContentDescription(viewModel.state());
            invalidate();
            return true;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        AppUiCopy copy = viewModel.state();
        float width = getWidth();
        float height = getHeight();
        canvas.drawColor(Color.rgb(15, 23, 42));

        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawRoundRect(new RectF(32, 56, width - 32, height - 56), 36, 36, paint);

        paint.setColor(Color.rgb(226, 232, 240));
        paint.setTextSize(48);
        paint.setFakeBoldText(true);
        canvas.drawText(copy.title(), 64, 132, paint);

        paint.setTextSize(28);
        paint.setFakeBoldText(false);
        paint.setColor(Color.rgb(125, 211, 252));
        canvas.drawText(copy.status(), 64, 184, paint);

        paint.setColor(copy.transmitting() ? Color.rgb(244, 63, 94) : Color.rgb(20, 184, 166));
        canvas.drawCircle(width / 2, 348, 128, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(34);
        paint.setFakeBoldText(true);
        centerText(canvas, copy.buttonLabel(), width / 2, 360);

        paint.setFakeBoldText(false);
        paint.setTextSize(26);
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawText(copy.primaryAction(), 64, 548, paint);

        paint.setColor(Color.rgb(51, 65, 85));
        canvas.drawRoundRect(new RectF(64, 604, width - 64, 760), 24, 24, paint);
        paint.setColor(Color.rgb(203, 213, 225));
        paint.setTextSize(23);
        canvas.drawText(copy.peerSummary(), 88, 662, paint);
        canvas.drawText(copy.diagnostics(), 88, 716, paint);
    }

    private void updateContentDescription(AppUiCopy copy) {
        setContentDescription(AppTalkUiRenderer.render(copy).contentDescription());
    }

    private void centerText(Canvas canvas, String text, float centerX, float baselineY) {
        float textWidth = paint.measureText(text);
        canvas.drawText(text, centerX - textWidth / 2f, baselineY, paint);
    }
}
