package com.sebastianlundquist.wazzapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ParagraphStyle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		intent = getIntent();
		activeUser = intent.getStringExtra("username");
		setTitle("WazzApp: Chat with " + activeUser);
		messageEditText = findViewById(R.id.messageEditText);
		chatListView = findViewById(R.id.chatListView);

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
		ParseQuery<ParseObject> query = ParseQuery.or(queries);
		query.orderByAscending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					if (objects.size() > 0) {
						messageData.clear();
						for (ParseObject message : objects) {
							Map<String, String> messageInfo = new HashMap<>();
							messageInfo.put("message", message.getString("message"));
							messageInfo.put("sender", message.getString("sender"));
							messageData.add(messageInfo);
						}
						simpleAdapter.notifyDataSetChanged();
					}
				}
			}
		});
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
}
