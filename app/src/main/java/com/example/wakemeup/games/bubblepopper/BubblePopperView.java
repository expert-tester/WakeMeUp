package com.example.wakemeup.games.bubblepopper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BubblePopperView extends View {

    private List<Bubble> bubbles = new CopyOnWriteArrayList<>();
    private final int BUBBLE_COUNT = 15;

    private Runnable completionListener;

    private boolean isGameRunning = true;

    private Paint titlePaint, subtitlePaint;
    private Paint bubblePaint;

    public BubblePopperView(Context context, AttributeSet attrs) {
        super(context, attrs);

        titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(60f);
        titlePaint.setAntiAlias(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.GRAY);
        subtitlePaint.setTextSize(40f);
        subtitlePaint.setAntiAlias(true);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        bubblePaint = new Paint();
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setAntiAlias(true);
    }

    public void setCompletionListener(Runnable listener) {
        this.completionListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bubbles.isEmpty() && w > 0 && h > 0) {
            initializeBubbles();
            startGameLoop();
        }
    }

    private void initializeBubbles() {
        bubbles.clear();
        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < BUBBLE_COUNT; i++) {
            // generate random properties for each bubble
            int radius = (int) (Math.random() * 50 + 40); // Radius between 40 and 90
            float x = (float) (Math.random() * (width - radius * 2) + radius);
            float y = (float) (Math.random() * (height - radius * 2) + radius);
            int color = Color.rgb((int)(Math.random() * 200 + 55), (int)(Math.random() * 200 + 55), (int)(Math.random() * 200 + 55));

            bubbles.add(new Bubble(x, y, radius, color));
        }
        isGameRunning = true;
    }

    private void startGameLoop() {
        Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;

                // random movement
                for (Bubble bubble : bubbles) {
                    bubble.update(getWidth(), getHeight());
                }

                postInvalidate();
                postDelayed(this, 16);
            }
        };
        post(gameLoop);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(17, 24, 39));

        if (!isGameRunning) return;

        for (Bubble bubble : bubbles) {
            bubblePaint.setColor(bubble.color);

            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubblePaint);
        }

        // TODO: add fonts, make nicer interface
        String title = "POP ALL THE BUBBLES!";
        String remainingBubbleText = "Bubbles Remaining: " + bubbles.size();
        canvas.drawText(title, getWidth() / 2f, 80, titlePaint);
        canvas.drawText(remainingBubbleText, getWidth() / 2f, 140, subtitlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (Bubble bubble : bubbles) {
                double distance = Math.sqrt(Math.pow(touchX - bubble.x, 2) + Math.pow(touchY - bubble.y, 2));
                if (distance < bubble.radius) {
                    bubbles.remove(bubble);

                    if (bubbles.isEmpty()) {
                        winGame();
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void winGame() {
        isGameRunning = false;
        if (completionListener != null) {
            post(completionListener);
        }
    }
}
