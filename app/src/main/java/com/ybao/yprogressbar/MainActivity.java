package com.ybao.yprogressbar;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ybao.library.FDProgressBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     final    List<FDProgressBar.Segment> segments = new ArrayList<>();
        FDProgressBar.Segment segment = new FDProgressBar.Segment();
        segment.color = Color.RED;
        segment.dvalue = 20;
        segments.add(segment);
        segment = new FDProgressBar.Segment();
        segment.color = Color.BLUE;
        segment.dvalue = 5;
        segments.add(segment);
        segment = new FDProgressBar.Segment();
        segment.color = Color.GREEN;
        segment.dvalue = 15;
        segments.add(segment);
        segment = new FDProgressBar.Segment();
        segment.color = Color.YELLOW;
        segment.dvalue = 10;
        segments.add(segment);
        segment = new FDProgressBar.Segment();
        segment.color = Color.WHITE;
        segment.dvalue = 4;
        segments.add(segment);
        ((FDProgressBar) findViewById(R.id.ff)).setStrokeCap(Paint.Cap.SQUARE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((FDProgressBar) findViewById(R.id.ff)).setSegments(segments);
            }
        },1000);
    }
}
