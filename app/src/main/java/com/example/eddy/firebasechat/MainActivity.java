package com.example.eddy.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.lang.*;

public class MainActivity extends AppCompatActivity
{

    private static final int SIGN_IN_REQUEST_CODE = 10;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseAuth mAuth;

    private ArrayList<String> friends;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(currentUser.getDisplayName()).build();

            currentUser.updateProfile(profileUpdates);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        friends = new ArrayList<>();
        friends.add("friendname");

        // No user logged in
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),
                                   SIGN_IN_REQUEST_CODE);
        }
        else
        {
            // User is already logged in
            Toast.makeText(this,"Welcome " + FirebaseAuth.getInstance().getCurrentUser()
                            .getDisplayName(), Toast.LENGTH_LONG).show();

            //displayPMMessages();
            displayAllChatMessages();
        }


        FloatingActionButton send = (FloatingActionButton)findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText input = (EditText)findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                // ChatMessage contains a msg string and display name
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null)
                {
                    Toast.makeText(MainActivity.this, "Username is null", Toast.LENGTH_LONG).show();
                }

                else
                {
                Toast.makeText(MainActivity.this, "Posted msg as: " +
                        FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getDisplayName()
                        + " from " +
                        FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getEmail()
                        , Toast.LENGTH_LONG).show();
                }
                // Clear the input
                input.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void displayPMMessages()
    {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                if (messageUser.toString().compareTo(friends.get(0)) == 0)
                {
                    messageText.setText(model.getMessageText());
                    messageUser.setText(model.getMessageUser());

                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            model.getMessageTime()));
                }

            }
        };

        listOfMessages.setAdapter(adapter);
    }

    private void displayAllChatMessages()
    {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                // upon successful login get user instance
                FirebaseUser user = mAuth.getCurrentUser();

                Toast.makeText(this,
                        "Successfully signed in as " + user.getDisplayName(),
                        Toast.LENGTH_LONG).show();



                // apply profile change using display name.
               UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(user.getDisplayName()).build();

                user.updateProfile(profileUpdates);

                //displayPMMessages();
                displayAllChatMessages();
            }
            else
            {
                Toast.makeText(this, "Error when signing in. Please try again later.",
                        Toast.LENGTH_LONG).show();

                // Close app
                finish();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // If sign out button is tapped
        if(item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            Toast.makeText(MainActivity.this, "You have been signed out.",
                                    Toast.LENGTH_LONG).show();

                            // Close app
                            finish();
                        }
                    });
        }
        return true;
    }
}
