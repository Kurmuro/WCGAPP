package de.wassersportclub.wcg;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Steganlage extends AppCompatActivity {

    ImageView stegbelegungIMGV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.steganlage);

        stegbelegungIMGV = findViewById(R.id.imageViewStegA);
        stegbelegungIMGV.setTag("A");

        //Listener aufrufen
        doListen();
    }

    //alle Listener
    public void doListen(){
        stegbelegungIMGV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stegbelegungIMGV.getTag() == ("A")){
                    stegbelegungIMGV.setImageResource(R.drawable.stegb);
                    stegbelegungIMGV.setTag("B");
                }else{
                    stegbelegungIMGV.setImageResource(R.drawable.stega);
                    stegbelegungIMGV.setTag("A");
                }
            }
        });
    }
}
