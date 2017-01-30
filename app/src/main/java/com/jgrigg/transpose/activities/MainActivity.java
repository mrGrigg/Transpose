package com.jgrigg.transpose.activities;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgrigg.transpose.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    Typeface typeface;
    private ImageView cursor;
    private TextView gameText;
    private Runnable blinker;
    private FrameLayout gameField;
    private boolean cursorVisible = false;

    private int MIN_LEFT;
    private int MAX_LEFT;

    private int CHARACTER_WIDTH;
    private Handler blinkHandler;
    private int puzzleLength;
    private int cursorIndex;
    private String puzzle;
    private int swapCount;
    private TextView movesCount;

    private String puzzleSolution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        typeface = createFontAsset();
        cursor = (ImageView) findViewById(R.id.game_cursor);
        gameText = (TextView) findViewById(R.id.game_field_text);
        gameField = (FrameLayout) findViewById(R.id.game_field);
        movesCount = (TextView) findViewById(R.id.moves_count);

        gameText.setTypeface(typeface);
        movesCount.setTypeface(typeface);

        puzzleSolution = "123456789";

        blinkHandler = new Handler();
        blinker = new Runnable() {
            @Override
            public void run() {
                cursorVisible = !cursorVisible;
                setCursorVisibility();

                setBlinkRunnable();
            }
        };

        makeGame();
        final View v = findViewById(R.id.activity_main);
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                setBlinkRunnable();
                getGameFieldWidth();
            }
        });
    }

    private void makeGame() {
        swapCount = 0;

        puzzle = shufflePuzzle(puzzleSolution);
        puzzleLength = puzzle.length();
        cursorIndex = 0;

        movesCount.setText(String.format(getResources().getString(R.string.swap_count), swapCount));
        gameText.setText(puzzle);
    }

    private void checkVictoryCondition() {
        Log.d("VictoryCondition", puzzleSolution + " = " + gameText.getText());

        if (puzzleSolution.equals(gameText.getText())) {
            gameText.setText(getResources().getString(R.string.victory_text));

            resetBlinkRunnable();
            setBlinkRunnable();
            getGameFieldWidth();
        }
    }
    
    private void getGameFieldWidth() {
        Rect offsetViewBounds = new Rect();
        //returns the visible bounds
        gameText.getDrawingRect(offsetViewBounds);
        // calculates the relative coordinates to the parent
        gameField.offsetDescendantRectToMyCoords(gameText, offsetViewBounds);

        int textWidth = gameText.getWidth();
        CHARACTER_WIDTH = textWidth/puzzleLength;

        int relativeLeft = offsetViewBounds.left;
        MIN_LEFT = relativeLeft;
        MAX_LEFT = relativeLeft + (CHARACTER_WIDTH * puzzleLength);

        cursor.setX(relativeLeft);
    }

    private void setCursorVisibility() {
        cursor.setVisibility(cursorVisible ? View.VISIBLE : View.GONE);
    }

    private void resetBlinkRunnable() {
        blinkHandler.removeCallbacks(blinker);
        cursorVisible = true;
        setCursorVisibility();
        setBlinkRunnable();
    }

    private void setBlinkRunnable() {
        blinkHandler.postDelayed(blinker, 650);
    }

    private Typeface createFontAsset() {
        AssetManager am = getAssets();
        String path = String.format(Locale.US, "fonts/%s", "Hack-Regular.ttf");

        return Typeface.createFromAsset(am, path);
    }

    // Implementing Fisher–Yates shuffle
    private String shufflePuzzle(String ar) {

        List<Character> characterList = new ArrayList<>();
        for (char c : ar.toCharArray()) {
            characterList.add(c);
        }

        Collections.shuffle(characterList);

        StringBuilder sb = new StringBuilder(ar.length());
        for (Character ch : characterList) {
            sb.append(ch);
        }
        return sb.toString();
    }

    @OnClick(R.id.game_pad_right)
    void gamePadRight() {
        int currentLeft = (int) cursor.getX();
        int nextLeft = currentLeft + CHARACTER_WIDTH;

        Log.d("GamePadRight", Integer.toString(nextLeft));
        if (nextLeft <= MAX_LEFT) {
            cursor.setX(nextLeft);
            cursorIndex++;
        }

        resetBlinkRunnable();
    }

    @OnClick(R.id.game_pad_left)
    void gamePadLeft() {
        int currentLeft = (int) cursor.getX();
        int nextLeft = currentLeft - CHARACTER_WIDTH;

        Log.d("GamePadLeft", Integer.toString(nextLeft));
        if (nextLeft >= MIN_LEFT) {
            cursor.setX(nextLeft);
            cursorIndex--;
        }

        resetBlinkRunnable();
    }

    @OnClick(R.id.game_pad_swap)
    void gamePadSwap() {
        if (cursorIndex > 0 && cursorIndex < puzzleLength) {
            int leftIndex = cursorIndex;
            int rightIndex = cursorIndex;

            String leftPuzzle = puzzle.substring(0, leftIndex - 1);
            String rightPuzzle = puzzle.substring(rightIndex + 1, puzzleLength);

            char leftChar = puzzle.charAt(leftIndex - 1);
            char rightChar = puzzle.charAt(rightIndex);

            String transposed = leftPuzzle + rightChar + leftChar + rightPuzzle;

            Log.d("TRANSPOSE", transposed);
            puzzle = transposed;
            gameText.setText(puzzle);

            swapCount++;
            movesCount.setText(String.format(Locale.US, "Swap count: %d", swapCount));

            checkVictoryCondition();
        }

        resetBlinkRunnable();
    }

    @OnClick(R.id.reset_puzzle)
    void resetPuzzle() {
        makeGame();

        resetBlinkRunnable();
        setBlinkRunnable();
        getGameFieldWidth();
    }
}
