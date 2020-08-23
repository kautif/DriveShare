package com.example.driveshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.sql.Driver;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, guestEditText;
    private RadioButton passengerBtn, driverBtn, guestPassengerBtn, guestDriverBtn;
    private Button userBtn, guestBtn;


    private Boolean signingUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username_editText);
        passwordEditText = findViewById(R.id.password_editText);

        userBtn = findViewById(R.id.user_button);
        passengerBtn = findViewById(R.id.passenger_radiobutton);
        driverBtn = findViewById(R.id.driver_radiobutton);
        guestPassengerBtn = findViewById(R.id.guest_passenger_radio_button);
        guestDriverBtn = findViewById(R.id.guest_driver_radio_button);
        guestBtn = findViewById(R.id.guest_button);

        if (ParseUser.getCurrentUser() != null) {
//            ParseUser.logOut();
//            Toast.makeText(MainActivity.this, "Previous user logged out", Toast.LENGTH_SHORT).show();
            toPassengerActivity();
            toDriverRidesActivity();
        }

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signingUp == false) {
                    if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Must enter username and password", Toast.LENGTH_SHORT).show();
                        return;
                    } else if ((passengerBtn.isChecked() == false) && (driverBtn.isChecked() == false)) {
                        Toast.makeText(MainActivity.this, "Must select user type to register", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        ParseUser user = new ParseUser();
                        user.setUsername(usernameEditText.getText().toString());
                        user.setPassword(passwordEditText.getText().toString());
                        if (passengerBtn.isChecked()) {
                            user.put("userType", "passenger");
                        }

                        if (driverBtn.isChecked()) {
                            user.put("userType", "driver");
                        }

                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(MainActivity.this, "Signed Up", Toast.LENGTH_SHORT).show();
                                    toPassengerActivity();
                                    toDriverRidesActivity();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
//                User is logging in
                if (signingUp == true) {
                    Log.i("username", usernameEditText.getText().toString());
                    Log.i("password", passwordEditText.getText().toString());
                    ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toast.makeText(MainActivity.this, "Logging in", Toast.LENGTH_SHORT).show();
                                toPassengerActivity();
                                toDriverRidesActivity();
                            } else {
                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        guestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((guestPassengerBtn.isChecked() == false) && (guestDriverBtn.isChecked() == false)) {
                    Toast.makeText(MainActivity.this, "Select a user type", Toast.LENGTH_SHORT).show();
                } else {
                    if (ParseUser.getCurrentUser() == null) {
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null && e == null) {
                                    Toast.makeText(MainActivity.this, "Logging in anonymously", Toast.LENGTH_SHORT).show();
                                    if (guestPassengerBtn.isChecked()) {
                                        user.put("userType", "passenger");
                                    }

                                    if (guestDriverBtn.isChecked()) {
                                        user.put("userType", "driver");
                                    }
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            toPassengerActivity();
                                            toDriverRidesActivity();
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.loginItem:
                if (signingUp == false) {
                    item.setTitle("Sign Up");
                    userBtn.setText("Login");
                    signingUp = true;
                } else {
                    item.setTitle("Login");
                    userBtn.setText("Sign Up");
                    signingUp = false;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toPassengerActivity () {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("userType").equals("passenger")) {
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void toDriverRidesActivity () {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("userType").equals("driver")) {
                Intent intent = new Intent(MainActivity.this, DriverRidesActivity.class);
                startActivity(intent);
            }
        }
    }

}
