package com.example.wakemeup.gameFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.wakemeup.R;

public class BubblePopperFragment extends BaseGameFragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bubble_game, container, false);

        Button winButton = view.findViewById(R.id.win_button);
        winButton.setOnClickListener(v -> completeGame()); // Simulate win

        return view;
    }
}
