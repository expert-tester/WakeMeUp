package com.example.wakemeup.gameFragments;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

public abstract class BaseGameFragment extends Fragment {
    protected GameCompleteListener listener;

    public interface GameCompleteListener {
        void onGameComplete();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof GameCompleteListener) {
            listener = (GameCompleteListener) getActivity();
        }
    }

    protected void completeGame() {
        if (listener != null) {
            listener.onGameComplete();
        }
    }
}
