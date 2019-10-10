package com.sebastianlundquist.wazzapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener {

	boolean signUpModeActive = true;
	TextView loginText;
	EditText userInput;
	EditText passwordInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.login_title);
		loginText = findViewById(R.id.loginText);
		userInput = findViewById(R.id.userInput);
		passwordInput = findViewById(R.id.passwordInput);
		passwordInput.setOnKeyListener(this);
		redirectIfLoggedIn();
	}

	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
			signUpOrLogin(view);
		return false;
	}

	public void dismissKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	public void toggleLoginMode(View view) {
		Button signUpButton = findViewById(R.id.signUpButton);
		if (signUpModeActive) {
			signUpModeActive = false;
			signUpButton.setText(R.string.login);
			loginText.setText(R.string.or_sign_up);
		}
		else {
			signUpModeActive = true;
			signUpButton.setText(R.string.sign_up);
			loginText.setText(R.string.or_login);
		}
	}

	public void signUpOrLogin(View view) {
		if (!userInput.getText().toString().matches("") && !passwordInput.getText().toString().matches("")) {
			if (signUpModeActive) {
				ParseUser newUser = new ParseUser();
				newUser.setUsername(userInput.getText().toString());
				newUser.setPassword(passwordInput.getText().toString());
				newUser.signUpInBackground(new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null)
							redirectIfLoggedIn();
						else
							Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
			else {
				ParseUser.logInInBackground(userInput.getText().toString(), passwordInput.getText().toString(), new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (user != null)
							redirectIfLoggedIn();
						else
							Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		else {
			Toast.makeText(this, "Username and password are required.", Toast.LENGTH_SHORT).show();
		}
	}

	public void redirectIfLoggedIn() {
		if (ParseUser.getCurrentUser() != null) {
			Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
			startActivity(intent);
		}
	}
}
