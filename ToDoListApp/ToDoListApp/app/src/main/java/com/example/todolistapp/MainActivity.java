package com.example.todolistapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText etTask;
    Button btnAddTask;
    Button btnEvents; // Declare the events button
    Button btnLogout; // Declare the logout button
    ListView lvTasks;
    CustomAdapter customAdapter;

    int userId; // We'll assume the userId is passed from the login activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        // Assume userId is passed from the LoginActivity
        userId = getIntent().getIntExtra("USER_ID", -1); // Replace this with actual login data

        etTask = findViewById(R.id.etTask);
        btnAddTask = findViewById(R.id.btnAddTask);
        btnEvents = findViewById(R.id.btnEvents); // Initialize the events button
        btnLogout = findViewById(R.id.btnLogout); // Initialize the logout button
        lvTasks = findViewById(R.id.lvTasks);

        // Fetch and display tasks
        Cursor cursor = db.getTasks(userId);
        customAdapter = new CustomAdapter(this, cursor, 0, db);
        lvTasks.setAdapter(customAdapter);

        // Add Task Button functionality
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = etTask.getText().toString();
                if (!task.isEmpty()) {
                    db.insertTask(userId, task); // Insert task for logged-in user
                    Cursor newCursor = db.getTasks(userId); // Refresh the task list
                    customAdapter.swapCursor(newCursor); // Update adapter with new data
                    etTask.setText(""); // Clear the input field
                    Toast.makeText(MainActivity.this, "Task Added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Events Button functionality
        btnEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        // Logout Button functionality
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session if applicable
                // Redirect to Login Activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish(); // Finish the current activity
            }
        });
    }
}
