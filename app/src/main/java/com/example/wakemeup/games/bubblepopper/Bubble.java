package com.example.wakemeup.games.bubblepopper;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Bubble {
    public float x, y;
    public int radius;
    public int color;
    public float speedX, speedY;
    public int lifespan;
    public int maxLifespan;

    public Bubble(float x, float y, int radius, int color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        // Assign some random speed to each bubble to make them move differently
        this.speedY = (float) (Math.random() * 2.5 + 1.5); // Random upward speed (1.5 to 4.0)
        this.speedX = (float) (Math.random() * 4 - 2);   // Random horizontal drift (-2.0 to +2.0)
        this.maxLifespan = 300 + (int) (Math.random() * 200); // 5-8 seconds at 60fps
        this.lifespan = maxLifespan;
    }

    /**
     * This method is called on every frame of the game loop.
     * It updates the bubble's position and handles screen boundary collisions.
     * @param canvasWidth The width of the screen/view.
     * @param canvasHeight The height of the screen/view.
     */
    public void update(int canvasWidth, int canvasHeight) {
        // --- Move the bubble ---
        // Subtract from y to move up, add to x to move horizontally
        y -= speedY;
        x += speedX;

        // --- Handle Screen Boundaries ---

        // 1. Top Boundary: If the bubble goes completely off the top of the screen...
        if (y < -radius) {
            // ...reset its position to just below the bottom of the screen.
            y = canvasHeight + radius;
            // Give it a new random horizontal position to make the pattern less repetitive.
            x = (float) (Math.random() * (canvasWidth - radius * 2) + radius);
        }

        // 2. Side Boundaries: If the bubble hits the left or right wall...
        if (x - radius < 0 || x + radius > canvasWidth) {
            // ...reverse its horizontal speed to make it "bounce" off the wall.
            speedX = -speedX;
        }
    }
}
