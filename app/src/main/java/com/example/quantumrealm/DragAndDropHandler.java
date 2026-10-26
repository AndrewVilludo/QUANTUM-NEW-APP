package com.example.quantumrealm;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DragAndDropHandler {
    private final LinearLayout dropZoneLayout;
    private final LinearLayout blockSourceLayout;
    private final List<Integer> solutionOrder;
    private final List<Integer> userAnswer;
    private final TextView outputTextView;
    private final String expectedOutput;
    private final MiniGame miniGame;

    public DragAndDropHandler(MiniGame miniGame,
                              LinearLayout dropZoneLayout,
                              LinearLayout blockSourceLayout,
                              List<Integer> solutionOrder,
                              String expectedOutput,
                              TextView outputTextView) {
        this.miniGame = miniGame;
        this.dropZoneLayout = dropZoneLayout;
        this.blockSourceLayout = blockSourceLayout;
        this.solutionOrder = solutionOrder;
        this.expectedOutput = expectedOutput;
        this.outputTextView = outputTextView;
        this.userAnswer = new ArrayList<>();
        setupListeners();
    }

    private void setupListeners() {
        dropZoneLayout.setOnDragListener(new BlockDragListener());
    }

    public void reset() {
        dropZoneLayout.removeAllViews();
        userAnswer.clear();
        outputTextView.setVisibility(View.GONE);
    }

    private class BlockDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();
                    owner.removeView(draggedView);
                    dropZoneLayout.addView(draggedView);
                    draggedView.setVisibility(View.VISIBLE);

                    int blockId = (int) draggedView.getTag();
                    userAnswer.add(blockId);

                    if (userAnswer.size() == solutionOrder.size()) {
                        if (userAnswer.equals(solutionOrder)) {
                            Toast.makeText(dropZoneLayout.getContext(), "✅ Correct!", Toast.LENGTH_SHORT).show();
                            outputTextView.setText(expectedOutput);
                            outputTextView.setVisibility(View.VISIBLE);
                            miniGame.checkAnswer(true); // Enable Next
                        } else {
                            Toast.makeText(dropZoneLayout.getContext(), "❌ Try Again!", Toast.LENGTH_SHORT).show();
                            outputTextView.setVisibility(View.GONE);
                            miniGame.checkAnswer(false); // Keep Next disabled
                        }
                    }
                    break;
            }
            return true;
        }
    }

    public class BlockTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(data, shadowBuilder, view, 0);
                return true;
            }
            return false;
        }
    }
}
