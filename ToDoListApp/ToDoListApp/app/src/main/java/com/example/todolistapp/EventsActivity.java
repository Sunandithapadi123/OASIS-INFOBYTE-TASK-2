package com.example.todolistapp;


import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class EventsActivity extends AppCompatActivity {
    private EditText etEventNote;
    private ImageView calendarIcon;
    private TextView tvSelectedDate;
    private ListView lvEventsNotes;
    private DatabaseHelper dbHelper;
    private String selectedDate = "";
    private ArrayList<Event> eventsNotesList;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Initialize UI elements
        etEventNote = findViewById(R.id.etEventNote);
        calendarIcon = findViewById(R.id.calendarIcon);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        lvEventsNotes = findViewById(R.id.lvEventsNotes);

        dbHelper = new DatabaseHelper(this);
        eventsNotesList = new ArrayList<>();
        adapter = new EventAdapter(eventsNotesList);
        lvEventsNotes.setAdapter(adapter);

        // Fetch and display events from the database
        loadEventsFromDatabase();

        // Handle date picking using calendar icon click
        calendarIcon.setOnClickListener(v -> showDatePickerDialog());

        // Handle saving event with selected date
        findViewById(R.id.btnSaveEventNote).setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String note = etEventNote.getText().toString().trim();
        if (!note.isEmpty() && !selectedDate.isEmpty()) {
            // Insert event into database
            boolean isInserted = dbHelper.insertEvent(1, note, selectedDate);
            if (isInserted) {
                // Update list and notify adapter
                eventsNotesList.add(new Event(note, selectedDate));
                adapter.notifyDataSetChanged();

                // Clear input fields
                etEventNote.setText("");
                tvSelectedDate.setText("Select Date");
                selectedDate = "";  // Reset selected date
            } else {
                Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a note and select a date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
            tvSelectedDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void loadEventsFromDatabase() {
        Cursor cursor = dbHelper.getEvents(1);  // Assuming user ID is 1
        if (cursor.moveToFirst()) {
            do {
                String eventNote = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_NOTE));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_DATE));
                eventsNotesList.add(new Event(eventNote, eventDate));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private class Event {
        String note;
        String date;

        Event(String note, String date) {
            this.note = note;
            this.date = date;
        }
    }

    private class EventAdapter extends ArrayAdapter<Event> {
        EventAdapter(ArrayList<Event> events) {
            super(EventsActivity.this, 0, events);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Event event = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_item, parent, false);
            }

            TextView tvEventNote = convertView.findViewById(R.id.tvEventNote);
            Button btnEdit = convertView.findViewById(R.id.btnEdit);
            Button btnDelete = convertView.findViewById(R.id.btnDelete);

            tvEventNote.setText("Date: " + event.date + "\nNote: " + event.note);

            btnEdit.setOnClickListener(v -> {
                // Set the current event note and date in the input fields
                etEventNote.setText(event.note);
                selectedDate = event.date;
                tvSelectedDate.setText(selectedDate);

                // Create a unique identifier for the event being edited
                String oldNote = event.note;
                String oldDate = event.date;

                // When the save button is clicked after editing
                findViewById(R.id.btnSaveEventNote).setOnClickListener(saveEdit -> {
                    String newNote = etEventNote.getText().toString().trim();
                    if (!newNote.isEmpty() && !selectedDate.isEmpty()) {
                        // Update event in the database
                        boolean isUpdated = dbHelper.updateEvent(oldNote, oldDate, newNote);
                        if (isUpdated) {
                            // Create the updated event object
                            eventsNotesList.set(position, new Event(newNote, selectedDate)); // Update the list with the new event details
                            adapter.notifyDataSetChanged(); // Notify adapter to refresh the list

                            // Clear input fields
                            etEventNote.setText("");
                            tvSelectedDate.setText("Select Date");
                            selectedDate = "";  // Reset selected date
                        } else {
                            Toast.makeText(EventsActivity.this, "Error updating event", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EventsActivity.this, "Please enter a note and select a date", Toast.LENGTH_SHORT).show();
                    }
                });
            });



            // Delete Button Click
            btnDelete.setOnClickListener(v -> {
                dbHelper.deleteEvent(event.note, event.date); // Ensure this method is implemented in DatabaseHelper
                eventsNotesList.remove(position);
                notifyDataSetChanged();
                Toast.makeText(EventsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}
