package com.example.wakemeup.games.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeView extends View {

    private static class Cell {
        boolean topWall = true, leftWall = true, bottomWall = true, rightWall = true;
        boolean visited = false;
        int col, row;

        Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    private Cell[][] maze;
    private final int COLS = 10;
    private final int ROWS = 15; // Making it taller for portrait screens
    private RectF destRect;

    private float playerX, playerY, playerRadius;
    private float playerSpeed = 400.0f; // Speed in pixels per second

    private Paint wallPaint, playerPaint, destPaint;
    private float cellSize, hMargin, vMargin;

    private Paint joystickBasePaint, joystickKnobPaint;
    private float joystickBaseX, joystickBaseY, joystickBaseRadius;
    private float joystickKnobX, joystickKnobY, joystickKnobRadius;
    private boolean isJoystickActive = false;

    private Runnable completionListener;
    private boolean gameRunning = true;
    private long lastFrameTime;

    public MazeView(Context context) {
        super(context);
        wallPaint = new Paint();
        wallPaint.setColor(Color.parseColor("#374151"));
        wallPaint.setStrokeWidth(8);
        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#3B82F6"));
        destPaint = new Paint();
        destPaint.setColor(Color.parseColor("#10B981"));
        joystickBasePaint = new Paint();
        joystickBasePaint.setColor(Color.argb(100, 255, 255, 255));
        joystickKnobPaint = new Paint();
        joystickKnobPaint.setColor(Color.argb(200, 255, 255, 255));
    }

    public void setCompletionListener(Runnable listener) {
        this.completionListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Calculate cell size and margins
        if (w / (float) h < (float) COLS / ROWS) {
            cellSize = w / (float) (COLS + 1);
        } else {
            cellSize = h / (float) (ROWS + 1);
        }
        hMargin = (w - cellSize * COLS) / 2;
        vMargin = (h - cellSize * ROWS) / 2;
        playerRadius = cellSize * 0.35f;

        joystickBaseX = w / 2f;
        joystickBaseY = h - (vMargin * 1.5f);
        joystickBaseRadius = vMargin * 0.5f;
        joystickKnobRadius = joystickBaseRadius / 2f;
        resetJoystick();

        generateMaze();
        initializePlayerAndDestination();
        startGameLoop();
    }

    private void generateMaze() {
        maze = new Cell[COLS][ROWS];
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                maze[c][r] = new Cell(c, r);
            }
        }

        Random random = new Random();
        Stack<Cell> stack = new Stack<>();
        Cell current = maze[random.nextInt(COLS)][random.nextInt(ROWS)];
        current.visited = true;
        stack.push(current);

        while (!stack.isEmpty()) {
            current = stack.pop();
            List<Cell> neighbors = getUnvisitedNeighbors(current);

            if (!neighbors.isEmpty()) {
                stack.push(current);
                Cell randomNeighbor = neighbors.get(random.nextInt(neighbors.size()));
                removeWall(current, randomNeighbor);
                randomNeighbor.visited = true;
                stack.push(randomNeighbor);
            }
        }
    }

    private List<Cell> getUnvisitedNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int c = cell.col;
        int r = cell.row;
        if (r > 0 && !maze[c][r - 1].visited) neighbors.add(maze[c][r - 1]);
        if (c < COLS - 1 && !maze[c + 1][r].visited) neighbors.add(maze[c + 1][r]);
        if (r < ROWS - 1 && !maze[c][r + 1].visited) neighbors.add(maze[c][r + 1]);
        if (c > 0 && !maze[c - 1][r].visited) neighbors.add(maze[c - 1][r]);
        return neighbors;
    }

    private void removeWall(Cell current, Cell neighbor) {
        if (current.col < neighbor.col) { current.rightWall = false; neighbor.leftWall = false; }
        if (current.col > neighbor.col) { current.leftWall = false; neighbor.rightWall = false; }
        if (current.row < neighbor.row) { current.bottomWall = false; neighbor.topWall = false; }
        if (current.row > neighbor.row) { current.topWall = false; neighbor.bottomWall = false; }
    }

    private void initializePlayerAndDestination() {
        playerX = hMargin + cellSize / 2;
        playerY = vMargin + cellSize / 2;

        float destLeft = hMargin + (COLS - 1) * cellSize;
        float destTop = vMargin + (ROWS - 1) * cellSize;
        destRect = new RectF(destLeft, destTop, destLeft + cellSize, destTop + cellSize);

        maze[COLS - 1][ROWS - 1].rightWall = false;
        maze[COLS - 1][ROWS - 1].bottomWall = false;
    }

    private void startGameLoop() {
        lastFrameTime = System.currentTimeMillis();
        Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!gameRunning) return;

                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
                lastFrameTime = currentTime;

                updatePlayerPosition(deltaTime);
                invalidate();
                postDelayed(this, 16);
            }
        };
        post(gameLoop);
    }

    private void updatePlayerPosition(float deltaTime) {
        if (!isJoystickActive) return;

        float dx = joystickKnobX - joystickBaseX;
        float dy = joystickKnobY - joystickBaseY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < joystickBaseRadius * 0.1) return;

        float moveDistance = playerSpeed * deltaTime;

        float moveX = (dx / distance) * moveDistance;
        float moveY = (dy / distance) * moveDistance;

        float nextX = playerX + moveX;
        float nextY = playerY + moveY;

        if (canMoveTo(nextX, playerY)) {
            playerX = nextX;
        }

        if (canMoveTo(playerX, nextY)) {
            playerY = nextY;
        }

        if (destRect.contains(playerX, playerY)) {
            winGame();
        }
    }

    private boolean canMoveTo(float nextX, float nextY) {
        int c = (int) ((nextX - hMargin) / cellSize);
        int r = (int) ((nextY - vMargin) / cellSize);

        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return false;

        Cell currentCell = maze[c][r];
        float wallMargin = playerRadius;
        float leftWallPos = hMargin + c * cellSize;
        float rightWallPos = leftWallPos + cellSize;
        float topWallPos = vMargin + r * cellSize;
        float bottomWallPos = topWallPos + cellSize;

        if (currentCell.leftWall && nextX < leftWallPos + wallMargin) return false;
        if (currentCell.rightWall && nextX > rightWallPos - wallMargin) return false;
        if (currentCell.topWall && nextY < topWallPos + wallMargin) return false;
        if (currentCell.bottomWall && nextY > bottomWallPos - wallMargin) return false;

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#111827"));
        canvas.drawRect(destRect, destPaint);

        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                float left = hMargin + c * cellSize;
                float top = vMargin + r * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                if (maze[c][r].topWall) canvas.drawLine(left, top, right, top, wallPaint);
                if (maze[c][r].leftWall) canvas.drawLine(left, top, left, bottom, wallPaint);
                if (maze[c][r].bottomWall) canvas.drawLine(left, bottom, right, bottom, wallPaint);
                if (maze[c][r].rightWall) canvas.drawLine(right, top, right, bottom, wallPaint);
            }
        }

        canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);
        canvas.drawCircle(joystickBaseX, joystickBaseY, joystickBaseRadius, joystickBasePaint);
        canvas.drawCircle(joystickKnobX, joystickKnobY, joystickKnobRadius, joystickKnobPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameRunning) return false;

        float touchX = event.getX();
        float touchY = event.getY();
        double dX = touchX - joystickBaseX;
        double dY = touchY - joystickBaseY;
        double distance = Math.sqrt(dX * dX + dY * dY);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isJoystickActive = true;
                if (distance > joystickBaseRadius) {
                    joystickKnobX = joystickBaseX + (float) (dX * joystickBaseRadius / distance);
                    joystickKnobY = joystickBaseY + (float) (dY * joystickBaseRadius / distance);
                } else {
                    joystickKnobX = touchX;
                    joystickKnobY = touchY;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                isJoystickActive = false;
                resetJoystick();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void resetJoystick() {
        joystickKnobX = joystickBaseX;
        joystickKnobY = joystickBaseY;
    }

    private void winGame() {
        gameRunning = false;
        if (completionListener != null) {
            post(completionListener);
        }
    }
}
