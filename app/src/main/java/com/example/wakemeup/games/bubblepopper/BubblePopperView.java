package com.example.wakemeup.games.bubblepopper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BubblePopperView extends View {

    // A list to hold all the active Bubble objects.
    // CopyOnWriteArrayList is used because it's thread-safe, which prevents crashes
    // when we try to remove a bubble (on the UI thread) while the game loop is
    // iterating over the list (also on the UI thread, but can cause issues).
    private List<Bubble> bubbles = new CopyOnWriteArrayList<>();
    private final int BUBBLE_COUNT = 15;

    // The callback to be executed when the game is won.
    private Runnable completionListener;

    private boolean isGameRunning = true;

    // Paint objects for drawing
    private Paint textPaint;
    private Paint bubblePaint; // Reusable paint for all bubbles

    // --- Constructor ---
    public BubblePopperView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize the Paint for drawing our text.
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // **NEW**: Initialize a reusable Paint object for the bubbles
        bubblePaint = new Paint();
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setAntiAlias(true);
    }

    /**
     * This method is called by the BubblePopperFragment to set the callback.
     * @param listener The Runnable to execute upon game completion.
     */
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

    /**
     * **MODIFIED**: Creates all bubble objects by generating random properties
     * and calling the user-specified constructor.
     */
    private void initializeBubbles() {
        bubbles.clear();
        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < BUBBLE_COUNT; i++) {
            // Generate random properties for each bubble
            int radius = (int) (Math.random() * 50 + 40); // Radius between 40 and 90
            float x = (float) (Math.random() * (width - radius * 2) + radius);
            float y = (float) (Math.random() * (height - radius * 2) + radius);
            int color = Color.rgb((int)(Math.random() * 200 + 55), (int)(Math.random() * 200 + 55), (int)(Math.random() * 200 + 55));

            // Call the user's specified constructor
            bubbles.add(new Bubble(x, y, radius, color));
        }
        isGameRunning = true;
    }

    private void startGameLoop() {
        Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;

                // This assumes your Bubble.java has an update() method for movement.
                for (Bubble bubble : bubbles) {
                    bubble.update(getWidth(), getHeight());
                }

                postInvalidate();
                postDelayed(this, 16);
            }
        };
        post(gameLoop);
    }

    /**
     * **MODIFIED**: Now uses a single reusable Paint object for bubbles.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(17, 24, 39));

        if (!isGameRunning) return;

        // Draw every bubble in our list.
        for (Bubble bubble : bubbles) {
            // Set the color on the reusable paint object
            // This assumes Bubble.java has a public 'color' field.
            bubblePaint.setColor(bubble.color);

            // This assumes Bubble.java has public 'x', 'y', and 'radius' fields.
            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubblePaint);
        }

        // Draw the remaining bubble count text.
        String text = "Bubbles Remaining: " + bubbles.size();
        canvas.drawText(text, getWidth() / 2f, 80, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (Bubble bubble : bubbles) {
                // This assumes Bubble.java has public 'x', 'y', and 'radius' fields.
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
