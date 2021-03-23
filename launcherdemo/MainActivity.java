package com.example.launcherdemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    boolean isBottom = true;
    ViewPager mViewPager;
    int cellHeight;

    //int NUMBER_OF_ROWS = 5;
    int DRAWER_PEEK_HEIGHT = 200;
    String PREFS_NAME = "NovaPrefs";

    int numRow = 0, numColumn = 0;
    //int drawerROWS = 0, drawerCOLS  = 0;
    public static ArrayList<AppObject> apps = new ArrayList<AppObject>();
    public static ArrayList<Integer> pos = new ArrayList<Integer>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        getPermissions();
        getData();


        final LinearLayout mTopDrawerLayout = findViewById(R.id.topDrawerLayout);
        mTopDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                DRAWER_PEEK_HEIGHT = mTopDrawerLayout.getHeight();
                initializeHome();
                initializeDrawer();
            }
        });

        ImageButton mSettings = findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
    }




    ViewPagerAdapter mViewPagerAdapter;
    private void initializeHome() {
        ArrayList<PagerObject> PagerAppList = new ArrayList<>();

        ArrayList<AppObject> appList1 = new ArrayList<>();
        ArrayList<AppObject> appList2 = new ArrayList<>();
        ArrayList<AppObject> appList3 = new ArrayList<>();


        for(int i = 0;i < numColumn*numRow ;i++)
            appList1.add(new AppObject("", " ", getResources().getDrawable(R.drawable.transp), false));
        for(int i = 0;i < numColumn*numRow ;i++)
            appList2.add(new AppObject("", " ", getResources().getDrawable(R.drawable.transp),false));
        for(int i = 0;i < numColumn*numRow ;i++)
            appList3 .add(new AppObject(""," ", getResources().getDrawable(R.drawable.transp),false));

        PagerAppList.add(new PagerObject(appList1));
        PagerAppList.add(new PagerObject(appList2));
        PagerAppList.add(new PagerObject(appList3));

        cellHeight = ((getDisplayContentHeight() - DRAWER_PEEK_HEIGHT)/ numRow)+22;

        mViewPager = findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this,PagerAppList,cellHeight, numColumn);
        mViewPager.setAdapter(mViewPagerAdapter);
    }


    List<AppObject> installedAppList = new ArrayList<>();
    GridView mDrawerGridView;
    BottomSheetBehavior mBottomSheetBehavior;

    private void initializeDrawer(){
        View mBottomSheet = findViewById(R.id.bottomSheet);
        mDrawerGridView = findViewById(R.id.drawGrid);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(DRAWER_PEEK_HEIGHT);

        installedAppList = getInstalledAppList();

        mDrawerGridView.setAdapter(new AppAdapter(this, installedAppList, cellHeight));

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(mAppDrag != null)
                    return;

                if(newState == BottomSheetBehavior.STATE_COLLAPSED && mDrawerGridView.getChildAt(0).getY() !=0)
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(newState == BottomSheetBehavior.STATE_DRAGGING && mDrawerGridView.getChildAt(0).getY() !=0)
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });
    }

    public AppObject mAppDrag = null;
    public void itemPress(AppObject app){

        if (mAppDrag != null && app.getIsAppInDrawer() && !app.getName().equals(" ")) {
                Toast.makeText(this, "Cell Already Occupied", Toast.LENGTH_SHORT).show();
                return;
        }
        if(mAppDrag != null && !app.getIsAppInDrawer()){

            app.setPackageName(mAppDrag.getPackageName());
            app.setName(mAppDrag.getName());
            app.setImage(mAppDrag.getImage());
            app.setIsAppInDrawer(false);

            if(!mAppDrag.getIsAppInDrawer()){
                mAppDrag.setPackageName("");
                mAppDrag.setName("");
                mAppDrag.setImage(getResources().getDrawable(R.drawable.transp));
                mAppDrag.setIsAppInDrawer(false);
            }

            mAppDrag = null;
            mViewPagerAdapter.notifyGridChanged();
            return;
        }else {
            Intent launchAppIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            if (launchAppIntent != null)
                getApplicationContext().startActivity(launchAppIntent);
        }
    }

    public void itemLongPress(AppObject app){
        collapseDrawer();
        mAppDrag = app;
    }

    private void collapseDrawer() {
        mDrawerGridView.setY(DRAWER_PEEK_HEIGHT);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private List<AppObject> getInstalledAppList() {
        List<AppObject> list = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> untreatedAppList = getApplicationContext().getPackageManager().queryIntentActivities(intent,0);

        for(ResolveInfo untreatedApp : untreatedAppList){
            String appName = untreatedApp.activityInfo.loadLabel(getPackageManager()).toString();
            String appPackageName = untreatedApp.activityInfo.packageName;
            Drawable appImage = untreatedApp.activityInfo.loadIcon(getPackageManager());

            AppObject app = new AppObject(appPackageName,appName,appImage,true);
            if(!list.contains(app))
                list.add(app);
        }

        return  list;
    }

    private int getDisplayContentHeight() {
        final WindowManager windowManager = getWindowManager();
        final Point size = new Point();
        int screeenheight = 0, actionBarHeight = 0, statusBarHeight = 0;
        if (getActionBar() != null) {
            actionBarHeight = getActionBar().getHeight();
        }

        int resourceID = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceID > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceID);
        }
        int contentTop = (findViewById(android.R.id.content)).getTop();
        windowManager.getDefaultDisplay().getSize(size);
        screeenheight = size.y;
        return screeenheight - contentTop - actionBarHeight - statusBarHeight;


    }


    private void getData(){
        ImageView mHomeScreenImage = findViewById(R.id.homeScreenImage);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUri = sharedPreferences.getString("imageUri", null);
        int numRow = sharedPreferences.getInt("numRow", 7);
        int numColumn = sharedPreferences.getInt("numColumn", 5);

        if (this.numRow != numRow || this.numColumn != numColumn){
            this.numRow = numRow;
            this.numColumn = numColumn;
            initializeHome();
        }

        if(imageUri != null){
            mHomeScreenImage.setImageURI(Uri.parse(imageUri));
        }
        Toast.makeText(getApplicationContext(),"Number of rows "+numRow,Toast.LENGTH_LONG).show();
    }
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

}
