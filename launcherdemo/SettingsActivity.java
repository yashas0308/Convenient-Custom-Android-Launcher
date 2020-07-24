package com.example.launcherdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    ImageView mHomeScreenImage;
    EditText nNumRow, nNumColumn;
    //EditText drawer_Col, drawer_Row;
    Uri imageUri;
    int REQUEST_CODE_IMAGE = 1;
    String PREFS_NAME = "NovaPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button mHomeScreenButton = findViewById(R.id.homeScreenButton);
        Button mGridSizeButtton = findViewById(R.id.gridSizeButton);
       // Button mDrawerSizeButtton = findViewById(R.id.drawerSizeButton);

        mHomeScreenImage = findViewById(R.id.homeScreenImage);
        nNumRow = findViewById(R.id.numRow);
        nNumColumn = findViewById(R.id.numColumn);
      //  drawer_Col = findViewById(R.id.drawerCol);
        // drawer_Row = findViewById(R.id.drawerRow);


        mGridSizeButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

       /* mDrawerSizeButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
*/
        mHomeScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        getData();
    }

    private void getData(){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUriString = sharedPreferences.getString("imageUri", null);
        int numRow = sharedPreferences.getInt("numRow", 7);
        int numColumn = sharedPreferences.getInt("numColumn", 5);
        /* int drawerROWS = sharedPreferences.getInt("drawerRow", 7);
        int drawerCOLS = sharedPreferences.getInt("drawerCol", 5);
       */if(imageUriString != null){
            imageUri = Uri.parse(imageUriString);
            mHomeScreenImage.setImageURI(imageUri);
        }

        nNumRow.setText(String.valueOf(numRow));
        nNumColumn.setText(String.valueOf(numColumn));
        /*drawer_Row.setText(String.valueOf(drawerROWS));
        drawer_Col.setText(String.valueOf(drawerCOLS));

*/
        Toast.makeText(getApplicationContext(),"Number of rows "+numRow,Toast.LENGTH_LONG).show();
    }
    private void saveData(){
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        if(imageUri != null)
            sharedPreferences.putString("imageUri", imageUri.toString());

        sharedPreferences.putInt("numRow", Integer.valueOf(nNumRow.getText().toString()));
        sharedPreferences.putInt("numColumn", Integer.valueOf(nNumColumn.getText().toString()));
       /* sharedPreferences.putInt("drawerRow", Integer.valueOf(drawer_Row.getText().toString()));
        sharedPreferences.putInt("drawerCol", Integer.valueOf(drawer_Col.getText().toString()));
        */sharedPreferences.apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK){
            imageUri = data.getData();
            mHomeScreenImage.setImageURI(imageUri);
            saveData();
        }
    }
}
