package com.example.todolistapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    public static final String DATABASE_NAME = "todo_list.db";
    public static final int DATABASE_VERSION = 2; // Set database version for upgrading if needed

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_EVENTS = "events"; // New table for events

    // User Table Columns
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "username";
    public static final String COL_USER_PASSWORD = "password";

    // Task Table Columns
    public static final String COL_TASK_ID = "_id"; // Updated
    public static final String COL_TASK_USER = "user_id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_TASK_STATUS = "task_status";

    // Event Table Columns
    public static final String COL_EVENT_ID = "_id"; // Event ID
    public static final String COL_EVENT_USER_ID = "user_id";
    public static final String COL_EVENT_NOTE = "event_note";
    public static final String COL_EVENT_DATE = "event_date"; // Stores the selected date

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_PASSWORD + " TEXT)");

        // Create Tasks Table
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_USER + " INTEGER, " +
                COL_TASK_NAME + " TEXT, " +
                COL_TASK_STATUS + " TEXT)");

        // Create Events Table
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_USER_ID + " INTEGER, " +
                COL_EVENT_NOTE + " TEXT, " +
                COL_EVENT_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS); // Drop the events table if it exists
        onCreate(db);
    }

    // Insert User (for Signup)
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_NAME, username);
        contentValues.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1; // Return true if inserted successfully
    }

    // Insert Task (for adding tasks)
    public boolean insertTask(int userId, String taskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TASK_USER, userId);
        contentValues.put(COL_TASK_NAME, taskName);
        contentValues.put(COL_TASK_STATUS, "pending"); // Default task status is 'pending'
        long result = db.insert(TABLE_TASKS, null, contentValues);
        return result != -1; // Return true if the task was inserted successfully
    }

    // Insert Event
    public boolean insertEvent(int userId, String eventNote, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EVENT_USER_ID, userId);
        contentValues.put(COL_EVENT_NOTE, eventNote);
        contentValues.put(COL_EVENT_DATE, eventDate);
        long result = db.insert(TABLE_EVENTS, null, contentValues);
        return result != -1; // Return true if inserted successfully
    }

    // Retrieve Events for a User
    public Cursor getEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE " + COL_EVENT_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // Check if user exists (for Login)
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_USER_NAME + " = ? AND " + COL_USER_PASSWORD + " = ?", new String[]{username, password});
        boolean userExists = cursor.getCount() > 0; // Store the result
        cursor.close(); // Close the cursor
        return userExists; // Return result
    }

    // Get User ID by Username
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_USER_NAME + " = ?", new String[]{username});

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            cursor.close();
            return userId;
        }
        cursor.close();
        return -1; // Return -1 if user not found
    }

    // Get tasks by user (for displaying tasks)
    public Cursor getTasks(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COL_TASK_USER + " = ?", new String[]{String.valueOf(userId)});
    }

    // Update task
    public boolean updateTask(int taskId, String newTaskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TASK_NAME, newTaskName); // Update the task name
        int result = db.update(TABLE_TASKS, contentValues, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return result > 0; // Return true if the update was successful
    }

    // Delete Task (for removing tasks)
    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return rowsAffected > 0; // Return true if a row was deleted
    }

    // Delete Event
    public boolean deleteEvent(String eventNote, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_EVENTS, COL_EVENT_NOTE + " = ? AND " + COL_EVENT_DATE + " = ?", new String[]{eventNote, eventDate});
        return rowsAffected > 0; // Return true if a row was deleted
    }

    // Update Event
    public boolean updateEvent(String oldNote, String oldDate, String newNote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EVENT_NOTE, newNote);
        // Assuming you're updating by matching the old note and date
        int rowsAffected = db.update(TABLE_EVENTS, contentValues, COL_EVENT_NOTE + " = ? AND " + COL_EVENT_DATE + " = ?", new String[]{oldNote, oldDate});
        return rowsAffected > 0; // Return true if the update was successful
    }


}
