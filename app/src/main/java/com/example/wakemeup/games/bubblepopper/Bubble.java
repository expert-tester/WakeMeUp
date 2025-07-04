package com.example.wakemeup.games.bubblepopper;

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

    public void update(int canvasWidth, int canvasHeight) {
        y -= speedY;
        x += speedX;

        if (y < -radius) {
            y = canvasHeight + radius;
            x = (float) (Math.random() * (canvasWidth - radius * 2) + radius);
        }

        if (x - radius < 0 || x + radius > canvasWidth) {
            speedX = -speedX;
        }
    }
}
