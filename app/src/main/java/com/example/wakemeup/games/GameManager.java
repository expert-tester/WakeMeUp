package com.example.wakemeup.games;

import androidx.fragment.app.Fragment;

import com.example.wakemeup.games.common.BaseGameFragment;
import com.example.wakemeup.games.bubblepopper.BubblePopperFragment;
import com.example.wakemeup.games.location.LocationTracker;
import com.example.wakemeup.games.maze.MazeFragment;
import com.example.wakemeup.games.shaker.ShakerFragment;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private List<Class<? extends BaseGameFragment>> gameQueue;
    private int currentGameIndex = 0;

    private GameManager() {
        gameQueue = new ArrayList<>();
        setupNewGameSession();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void setupNewGameSession() {
        gameQueue.clear(); // Clear any old games from a previous session
        currentGameIndex = 0;

        // Add the minigames for this new session
//        gameQueue.add(BubblePopperFragment.class);
//        gameQueue.add(LocationTracker.class);
//        gameQueue.add(MazeFragment.class);
        gameQueue.add(ShakerFragment.class);
    }

    public Fragment getNextGameFragment() {
        if (currentGameIndex < gameQueue.size()) {
            try {
                return gameQueue.get(currentGameIndex++).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null; // All games done
    }

    public boolean hasMoreGames() {
        return currentGameIndex < gameQueue.size();
    }

    public void reset() {
        currentGameIndex = 0;
    }
}
