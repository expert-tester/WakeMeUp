package com.example.wakemeup.games;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.R;
import com.example.wakemeup.games.common.BaseGameFragment;

public class GameHostActivity extends AppCompatActivity implements BaseGameFragment.GameCompleteListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_host);
        loadNextGame();
    }

    private void loadNextGame() {
        Fragment nextGame = GameManager.getInstance().getNextGameFragment();
        if (nextGame != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, nextGame)
                    .commit();
        } else {
            finishAlarm(); // All games complete
        }
    }

    private void finishAlarm() {
        // Stop alarm sound, dismiss activity, etc.
        finish();
    }

    @Override
    public void onGameComplete() {
        loadNextGame();
    }
}
