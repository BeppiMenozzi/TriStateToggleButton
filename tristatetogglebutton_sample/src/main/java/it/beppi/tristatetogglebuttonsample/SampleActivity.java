package it.beppi.tristatetogglebuttonsample;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        final TriStateToggleButton tstb_1 = (TriStateToggleButton) findViewById(R.id.tstb_1);
        final TextView tstb_1_text = (TextView) findViewById(R.id.tstb_1_text);

        final TriStateToggleButton tstb_2 = (TriStateToggleButton) findViewById(R.id.tstb_2);
        final TextView tstb_2_text = (TextView) findViewById(R.id.tstb_2_text);

        final TriStateToggleButton tstb_3 = (TriStateToggleButton) findViewById(R.id.tstb_3);
        final TextView tstb_3_text = (TextView) findViewById(R.id.tstb_3_text);

        final TriStateToggleButton tstb_4 = (TriStateToggleButton) findViewById(R.id.tstb_4);

// Example 1: a default tristate toggle without any customization

        tstb_1_text.setText("Off");
        tstb_1.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off: tstb_1_text.setText("Off"); break;
                    case mid: tstb_1_text.setText("Half way"); break;
                    case on: tstb_1_text.setText("On"); break;
                }
            }
        });

// Example 2: a default tristate toggle that starts in the middle status and never gets back again to it

        tstb_2_text.setText("Almost...");
        tstb_2.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off: tstb_2_text.setText("Off"); break;
                    case mid: tstb_2_text.setText("Almost..."); break;
                    case on: tstb_2_text.setText("On"); break;
                }
            }
        });

// Example 3: a customized tristate toggle that is undefined in the middle and enables / disables another toggle

        tstb_3_text.setText("undefined");
        tstb_3.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off: tstb_3_text.setText("False"); break;
                    case mid: tstb_3_text.setText("undefined"); break;
                    case on: tstb_3_text.setText("True"); break;
                }
            }
        });

// Example 4: an out of the box classic 2-state toggle, using booleans, controls toggle 3

        tstb_4.setToggleStatus(true);
        tstb_4.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                tstb_3.setEnabled(booleanToggleStatus);
            }
        });

// Example 5: random restyle of toggle 3

        ((Button) findViewById(R.id.button_restyle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restyle(tstb_3);
            }
        });

    }

    void restyle(TriStateToggleButton toggleButton) {
        Random rnd = new Random();
        for (int w = 0; w < 7; w++) {
            int randomColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            switch (w) {
                case 0: toggleButton.setMidColor(randomColor); break;
                case 1: toggleButton.setBorderColor(randomColor); break;
                case 2: toggleButton.setOffBorderColor(randomColor); break;
                case 3: toggleButton.setOffColor(randomColor); break;
                case 4: toggleButton.setOnColor(randomColor); break;
                case 5: toggleButton.setSpotColor(randomColor); break;
                case 6: toggleButton.setSpotSize(rnd.nextInt(20) + 30); break;
            }
        }
    }

}
