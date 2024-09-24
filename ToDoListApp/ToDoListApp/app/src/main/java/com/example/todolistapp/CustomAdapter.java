package com.example.todolistapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AlertDialog;

public class CustomAdapter extends CursorAdapter {

    private DatabaseHelper dbHelper;

    public CustomAdapter(Context context, Cursor c, int flags, DatabaseHelper dbHelper) {
        super(context, c, flags);
        this.dbHelper = dbHelper;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView tvTaskName = view.findViewById(R.id.tvTaskName);
        Button btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        Button btnEditTask = view.findViewById(R.id.btnEditTask); // Add this line

        final int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("_id")); // Use your task ID column name
        String taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name"));

        tvTaskName.setText(taskName);

        // Handle Delete Task functionality
        btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteTask(taskId);
                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show();

                // Refresh the ListView after deletion
                Cursor newCursor = dbHelper.getTasks(1); // Replace with the logged-in user's ID
                swapCursor(newCursor);
            }
        });

        // Handle Edit Task functionality
        btnEditTask.setOnClickListener(v -> {
            showEditTaskDialog(taskId, taskName, context); // Call the method to show the dialog
        });
    }

    // Method to show the edit task dialog
    private void showEditTaskDialog(int taskId, String currentTaskName, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");

        // Set up the input
        final EditText input = new EditText(context);
        input.setText(currentTaskName);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTaskName = input.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                dbHelper.updateTask(taskId, newTaskName); // Update the task in the database
                Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show();

                // Refresh the ListView after updating
                Cursor newCursor = dbHelper.getTasks(1); // Replace with the logged-in user's ID
                swapCursor(newCursor);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
