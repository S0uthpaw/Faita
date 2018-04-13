package com.example.tyler.spaceinvaders_2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    public Button but1;
    public Button butOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


    }

    public void onClickExit(View v) {
        finish();
        System.exit(0);
    }

    public void onClickOptions(View v){
        butOptions = (Button)findViewById(R.id.butOptions);
        butOptions.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent act = new Intent(MainMenu.this,OptionsActivity.class);
                startActivity(act);
            }
        });
    }
    public void init(View view) {
        but1 = (Button)findViewById(R.id.but1);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent act = new Intent(MainMenu.this,SpaceInvadersActivity.class);
                startActivity(act);
            }
        });
    }


}
