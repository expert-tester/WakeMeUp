package com.example.wakemeup;

import androidx.fragment.app.Fragment;

import com.example.wakemeup.games.common.BaseGameFragment;
import com.example.wakemeup.games.bubblepopper.BubblePopperFragment;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private List<Class<? extends BaseGameFragment>> gameQueue;
    private int currentGameIndex = 0;

    private GameManager() {
        gameQueue = new ArrayList<>();
        // Add your game fragments here
        gameQueue.add(BubblePopperFragment.class);
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void setupNewGameSession() {
        gameQueue.clear(); // Clear any old games from a previous session

        // Add the minigames for this new session
        gameQueue.add(BubblePopperFragment.class);
        // gameQueue.add(new AnotherGameFragment()); // Add more games here
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
