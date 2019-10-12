package com.sebastianlundquist.wazzapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

	EditText messageEditText;
	String activeUser = "";
	Intent intent;
	final List<Map<String, String>> messageData = new ArrayList<>();
	ListView chatListView;
	SimpleAdapter simpleAdapter;
	ParseQuery<ParseObject> query;
	Date lastUpdated;
	CountDownTimer timer;
	Boolean isOnStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		intent = getIntent();
		activeUser = intent.getStringExtra("username");
		setTitle("WazzApp: Chat with " + activeUser);
		messageEditText = findViewById(R.id.messageEditText);
		chatListView = findViewById(R.id.chatListView);
		lastUpdated = new Date(0);
		isOnStart = true;

		simpleAdapter = new SimpleAdapter(ChatActivity.this, messageData, android.R.layout.simple_list_item_2, new String[] {"message", "sender"}, new int[] { android.R.id.text1, android.R.id.text2 });
		chatListView.setAdapter(simpleAdapter);

		ParseQuery<ParseObject> query1 = new ParseQuery<>("Message");
		query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());
		query1.whereEqualTo("recipient", activeUser);
		ParseQuery<ParseObject> query2 = new ParseQuery<>("Message");
		query2.whereEqualTo("sender", activeUser);
		query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
		List<ParseQuery<ParseObject>> queries = new ArrayList<>();
		queries.add(query1);
		queries.add(query2);
		query = ParseQuery.or(queries);
		query.orderByDescending("createdAt");
		query.whereGreaterThan("createdAt", lastUpdated);
		query.setLimit(10);
		messageData.clear();
		findNewMessages(isOnStart);
		isOnStart = false;
		timer = new CountDownTimer(3100, 1000) {
			@Override
			public void onTick(long l) { }

			@Override
			public void onFinish() {
				findNewMessages(isOnStart);
				timer.start();
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null)
			timer.cancel();
	}

	public void sendMessage(View view) {
		if (!messageEditText.getText().toString().equals("")) {
			ParseObject message = new ParseObject("Message");
			String messageContent = messageEditText.getText().toString();
			message.put("sender", ParseUser.getCurrentUser().getUsername());
			message.put("recipient", activeUser);
			message.put("message", messageContent);
			Map<String, String> info = new HashMap<>();
			info.put("message", messageContent);
			info.put("sender", ParseUser.getCurrentUser().getUsername());
			messageData.add(info);
			simpleAdapter.notifyDataSetChanged();
			messageEditText.setText("");
			message.saveInBackground();
		}
	}

	public void findNewMessages(final Boolean isOnStart) {
		query.whereGreaterThan("createdAt", lastUpdated);
		lastUpdated = new Date();
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					Collections.reverse(objects);
					if (objects.size() > 0) {
						for (ParseObject message : objects) {
							if (!message.getString("sender").equals(ParseUser.getCurrentUser().getUsername()) || isOnStart) {
								Map<String, String> messageInfo = new HashMap<>();
								messageInfo.put("message", message.getString("message"));
								messageInfo.put("sender", message.getString("sender"));
								messageData.add(messageInfo);
							}
						}
						simpleAdapter.notifyDataSetChanged();
					}
				}
			}
		});
	}
}
