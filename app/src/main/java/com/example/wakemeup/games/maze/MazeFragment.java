package com.example.wakemeup.games.maze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wakemeup.games.common.BaseGameFragment;

public class MazeFragment extends BaseGameFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MazeView mazeView = new MazeView(requireContext());

        mazeView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        // win condition
        mazeView.setCompletionListener(this::completeGame);
        return mazeView;
    }
}
