package com.example.sqlproject.Fragment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sqlproject.Fragment.Adapter.AlarmReceiver;
import com.example.sqlproject.Fragment.Adapter.DbHelper;
import com.example.sqlproject.Fragment.Adapter.UpComingAdapter;
import com.example.sqlproject.Fragment.modelSql.EventModel;
import com.example.sqlproject.NotificationActivity;
import com.example.sqlproject.R;
import com.example.sqlproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpcomingFragment extends Fragment {
    RecyclerView rcvSql;
    FloatingActionButton btnAddData;
    DbHelper dbHelper;
    UpComingAdapter adapter;
    SwipeRefreshLayout Swipe;


    private ActivityMainBinding binding;
    private MaterialTimePicker timePicker;
    private  Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcomming, container, false);
        btnAddData = view.findViewById(R.id.btnAddData);
        rcvSql = view.findViewById(R.id.rcvSql);
        dbHelper = new DbHelper(requireContext());
        Swipe=view.findViewById(R.id.Swipe);


        ArrayList<EventModel> eventList = new ArrayList<>();
        adapter = new UpComingAdapter(requireContext(), eventList);
        rcvSql.setAdapter(adapter);
        rcvSql.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEventList();

        Swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadEventList();
                Swipe.setRefreshing(false);
            }
        });
        createNotificationChannel();
        btnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView etDate, etTime, etDesc;
                Button btnAdd, btnCancel;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = getLayoutInflater().inflate(R.layout.add_dialog_file, null);
                builder.setView(dialogView);
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();

                etDate = dialog.findViewById(R.id.etDate);
                etTime = dialog.findViewById(R.id.etTime);
                etDesc = dialog.findViewById(R.id.etDesc);
                btnAdd = dialog.findViewById(R.id.btnAdd);
                btnCancel = dialog.findViewById(R.id.btnCancel);


                etDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        etDate.setFocusable(false);
                        etDate.setClickable(true);

                        Calendar currentDate = Calendar.getInstance();
                        int year = currentDate.get(Calendar.YEAR);
                        int month = currentDate.get(Calendar.MONTH);
                        int day = currentDate.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(selectedYear, selectedMonth, selectedDay);
                                if (selectedDate.getTimeInMillis() >= currentDate.getTimeInMillis()) {
                                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                                    etDate.setText(date);
                                }
                            }
                        }, year, month, day);
                        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
                        datePickerDialog.show();
                    }
                });
                etTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        etTime.setFocusable(false);
                        etTime.setClickable(true);

                        Calendar currentTime = Calendar.getInstance();
                        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = currentTime.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                String timeFormat;
                                String amPm;
                                if (selectedHour >= 12) {
                                    amPm = "PM";
                                    if (selectedHour > 12) {
                                        selectedHour -= 12;
                                    }
                                } else {
                                    amPm = "AM";
                                    if (selectedHour == 0) {
                                        selectedHour = 12;
                                    }
                                }
                                String time = String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour, selectedMinute, amPm);
                                etTime.setText(time);
                            }
                        }, hour, minute, false);
                        timePickerDialog.updateTime(hour, minute);
                        timePickerDialog.show();
                    }
                });


                etDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Schedule the alarm with the provided data

                        String date = etDate.getText().toString();
                        String time = etTime.getText().toString();
                        String description = etDesc.getText().toString();



                        if (!date.isEmpty() && !time.isEmpty() && !description.isEmpty()) {
                            scheduleAlarm(date, time, description);
                            long rowId = dbHelper.insertEvent(date, time, description);

                            if (rowId != -1) {
                                Cursor cursor = dbHelper.getAllEvents();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        adapter.SetUpInterFace(new UpComingAdapter.UpComingClick() {
            @Override
            public void EditClick(EventModel event) {
                updateEvent(event);
            }

            @Override
            public void DeleteClick(EventModel event) {
                DeleteEvent(event);
            }
        });
        return view;
    }

    private void DeleteEvent(EventModel event) {

        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(requireContext());
        confirmationDialog.setTitle("Delete Event");
        confirmationDialog.setMessage("Are you sure you want to delete this event?");
        confirmationDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int eventId = (int) event.getId();
                int rowsAffected = dbHelper.deleteEvent(eventId);

                if (rowsAffected > 0) {
                    // Event deleted successfully
                    loadEventList(); // Refresh the event list after deletion
                } else {
                    Toast.makeText(requireContext(), "Failed to delete the event", Toast.LENGTH_SHORT).show();
                }
            }
        });
        confirmationDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No," do nothing
            }
        });
        confirmationDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateEvent(EventModel event)  {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View editDialogView = getLayoutInflater().inflate(R.layout.edit_item_file, null);
        builder.setView(editDialogView);
        builder.setTitle("Edit Reminder");

        EditText etDate1 = editDialogView.findViewById(R.id.etDate1);
        EditText etTime1 = editDialogView.findViewById(R.id.etTime1);
        EditText etDesc1 = editDialogView.findViewById(R.id.etDesc1);

        etDate1.setText(event.getDate());
        etTime1.setText(event.getTime());
        etDesc1.setText(event.getDescription());

        // Add an OnClickListener to etvEditDate to open DatePicker
        etDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentDate = Calendar.getInstance();
                int year = currentDate.get(Calendar.YEAR);
                int month = currentDate.get(Calendar.MONTH);
                int day = currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                                etDate1.setText(selectedDate);
                            }
                        },
                        year,
                        month,
                        day
                );
                datePickerDialog.show();
            }
        });

        // Add an OnClickListener to etvEditTime to open TimePicker
        etTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                String amPm;
                                if (selectedHour >= 12) {
                                    amPm = "PM";
                                    if (selectedHour > 12) {
                                        selectedHour -= 12;
                                    }
                                } else {
                                    amPm = "AM";
                                    if (selectedHour == 0) {
                                        selectedHour = 12;
                                    }
                                }
                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                            //    etTime1.setText(selectedTime);
                                if (selectedHour >= 12) {
                                    etTime1.setText(selectedTime + " PM");
                                } else {
                                    etTime1.setText(selectedTime + " AM");
                                }
                            }
                        },
                        currentHour,
                        currentMinute,
                        true
                );
                timePickerDialog.show();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedDate = etDate1.getText().toString();
                String editedTime = etTime1.getText().toString();
                String editedDescription = etDesc1.getText().toString();

                // Update the data in the database
                long eventId = event.getId(); // Get the event ID
                int rowsAffected = dbHelper.updateEvent((int) eventId, editedDate, editedTime, editedDescription);

                event.setDate(editedDate);
                event.setTime(editedTime);
                event.setDescription(editedDescription);
              /*  ArrayList<EventModel> eventModels = (ArrayList<EventModel>) dbHelper.getAllEvents();
                adapter.updateData(eventModels);*/

                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(), "Item updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();


    }
    private void loadEventList() {
        ArrayList<EventModel> eventList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllEvents();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DbHelper.COLUMN_ID);
                int dateIndex = cursor.getColumnIndex(DbHelper.COLUMN_DATE);
                int timeIndex = cursor.getColumnIndex(DbHelper.COLUMN_TIME);
                int descriptionIndex = cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPTION);

                do {
                    int id = cursor.getInt(idIndex);
                    String date = cursor.getString(dateIndex);
                    String time = cursor.getString(timeIndex);
                    String description = cursor.getString(descriptionIndex);

                    EventModel event = new EventModel(id, date, time, description);
                    eventList.add(event);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        adapter.setEventList(eventList);
    }

    private  void  createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name ="akChannel";
            String desc = "Channel for Alarm Manager";
            int imp  = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("androidKnowledge",name,imp);
            channel.setDescription(desc);

            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void scheduleAlarm(String date, String time, String desc) {
        // Parse the date and time strings into a Calendar object
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        try {
            Date dateTime = sdf.parse(date + " " + time);
            calendar.setTime(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create an Intent for the AlarmReceiver
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("description", desc);
        intent.putExtra("time", time);
        intent.putExtra("date", date);

        // Create a PendingIntent
        int alarmId = (int) System.currentTimeMillis(); // Unique ID for each alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmId, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);


        // Set the alarm
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }


        Toast.makeText(requireContext(), "Alarm set", Toast.LENGTH_SHORT).show();
    }

   /* private void scheduleAlarm(String date, String time, String description) {

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        // Parse date and time strings to get the timestamp
        long alarmTimestamp = getAlarmTimestamp(date, time);

        // Create an intent for the AlarmReceiver
        Intent alarmIntent = new Intent(requireContext(), AlarmReceiver.class);
        alarmIntent.putExtra("description", description);
        alarmIntent.putExtra("date", date);
        alarmIntent.putExtra("time", time);

        // Use a unique requestCode to distinguish different alarms
        int requestCode = generateRequestCode();

        // Create a PendingIntent to be triggered when the alarm fires
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Set the alarm using AlarmManager
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimestamp, pendingIntent);
        }
    }

    private long getAlarmTimestamp(String date, String time) {
        try {
            String dateTimeString = date + " " + time;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            Date dateTime = format.parse(dateTimeString);
            return dateTime != null ? dateTime.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int generateRequestCode() {
        // You can implement your logic for generating a unique request code here
        // For simplicity, you can use a counter or a random number generator
        return (int) System.currentTimeMillis();
    }*/

    private void cancelAlarm(int alarmId) {
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}