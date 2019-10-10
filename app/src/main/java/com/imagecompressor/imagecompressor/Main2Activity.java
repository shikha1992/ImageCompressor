package com.imagecompressor.imagecompressor;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zcw.togglebutton.ToggleButton;

//import com.suke.widget.SwitchButton;

public class Main2Activity extends AppCompatActivity {
//SwitchButton switch_button;
ToggleButton toggleBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
       toggleBtn=(ToggleButton)findViewById(R.id.toggal) ;
        toggleBtn.toggle();
//        toggleBtn.toggle(false);
        toggleBtn.setOnToggleChanged(new ToggleButton.OnToggleChanged(){
            @Override
            public void onToggle(boolean on) {
                if(on)
                {
                    Log.e("valueeee", String.valueOf(on));
                }else{
                    Log.e("valueeee", String.valueOf(on));
                }

            }
        });
        toggleBtn.setToggleOn();
        toggleBtn.setToggleOff();
//        toggleBtn.setToggleOn(false);
//        toggleBtn.setToggleOff(false);
//        toggleBtn.setAnimate(false);

    }
}
