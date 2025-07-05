package com.example.wakemeup.games.bubblepopper;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.wakemeup.games.common.BaseGameFragment;

public class BubblePopperFragment extends BaseGameFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        BubblePopperView bubblePopperView = new BubblePopperView(context, null);

        bubblePopperView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // win condition
        bubblePopperView.setCompletionListener(this::completeGame);

        return bubblePopperView;
    }
}
