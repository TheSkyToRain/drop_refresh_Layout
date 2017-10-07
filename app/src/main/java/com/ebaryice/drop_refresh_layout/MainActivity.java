package com.ebaryice.drop_refresh_layout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.ebaryice.drop_refresh_layout.layout.TouchPullView;

public class MainActivity extends AppCompatActivity {

    private float mTouchMoveStartY = 0;

    private static final float TOUCH_MOVE_Y_MAX = 600;

    private TouchPullView pullView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pullView = (TouchPullView) findViewById(R.id.touchPull);


        findViewById(R.id.activity_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //得到意图
                int action = motionEvent.getActionMasked();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        mTouchMoveStartY = motionEvent.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float y = motionEvent.getY();
                        if (y>=mTouchMoveStartY){
                            float moveSize = y-mTouchMoveStartY;
                            float progress = moveSize>=TOUCH_MOVE_Y_MAX
                                    ?1:moveSize/TOUCH_MOVE_Y_MAX;
                            pullView.setProgress(progress);
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        pullView.release();
                        break;
                    default:
                }
                return false;
            }
        });
    }
}
