package com.example.roomi.roomi;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomSelector extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private int i;
    private String[] keyList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private User user;
    private FirebaseUser fbUser;
    private ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selector);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        setTitle(R.string.home);
        getDatabase();
        retrieveData();

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            fbUser = mAuth.getCurrentUser();
            getDatabase();
            logoutListener();
            retrieveData();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_home) {
                            Intent myIntent = new Intent(getApplicationContext(), RoomSelector.class);
                            startActivity(myIntent);
                        } else if (id == R.id.nav_security) {
                            // Goes to Security Activity
                            Intent security = new Intent(getApplicationContext(), SecuritySelector.class);
                            startActivity(security);
                        } else if (id == R.id.nav_settings) {
                            // Goes to Settings Page
                            Intent settings = new Intent(getApplicationContext(), Settings.class);
                            startActivity(settings);
                        } else if (id == R.id.nav_aboutus) {
                            Intent mAboutUs = new Intent(getApplicationContext(), AboutUs.class);
                            startActivity(mAboutUs);
                        } else if (id == R.id.nav_logout) {
                            // Logs out and displays the Log In Screen

                            mAuth.signOut();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else if (id == R.id.nav_exit) {
                            finishAffinity();
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("rooms");
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fetchRooms(dataSnapshot);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("roomi", "Data retrieval error...", databaseError.toException());
            }
        });
    }

    private void fetchRooms(DataSnapshot dataSnapshot) {
        List<RoomDatastructure> roomList = new ArrayList<>();
        keyList = new String[(int) dataSnapshot.getChildrenCount()];
        i = 0;
        roomList.clear();
        for (DataSnapshot roomSnapShot: dataSnapshot.getChildren()) {
            RoomDatastructure room = roomSnapShot.getValue(RoomDatastructure.class);
            keyList[i] = roomSnapShot.getKey();
            roomList.add(room);
            i++;
        }
        generateRoomButtons(roomList, keyList);
    }

    private void generateRoomButtons(List<RoomDatastructure> roomList, final String[] keyList) {
        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, 200);
        params.setMargins(0, 0, 0, 65);

        for (RoomDatastructure room: roomList) {
            final String key = keyList[i];
            final String name = room.getName();
            final int temperature = room.getTemperature();
            final int brightness = room.getBrightness();
            Button button = new Button(this);
            button.setTag(room.getName());
            button.setBackgroundResource(R.drawable.element_background_dark);
            button.setLayoutParams(params);
            button.setText(room.getName());
            button.setTextSize(20);
            button.setTextColor(Color.WHITE);
            button.setTransformationMethod(null);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RoomSelector.this, RoomSettings.class);
                    intent.putExtra("key", key);
                    intent.putExtra("name", name);
                    intent.putExtra("temperature", temperature);
                    intent.putExtra("brightness", brightness);
                    startActivity(intent);
                }
            });
            i++;
            buttonContainer.addView(button);
        }

        Button addRoomButton = new Button(this);
        addRoomButton.setTag("add_room");
        addRoomButton.setBackgroundResource(R.drawable.element_background_dark);
        addRoomButton.setLayoutParams(params);
        addRoomButton.setText("+");
        addRoomButton.setTextSize(20);
        addRoomButton.setTextColor(Color.WHITE);
        addRoomButton.setTransformationMethod(null);

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddRoom.class);
                startActivity(intent);
            }
        });

        buttonContainer.addView(addRoomButton);
    }

    private void logoutListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };
    }

}
