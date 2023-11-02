package com.example.sqlproject.Fragment.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqlproject.Fragment.modelSql.EventModel;
import com.example.sqlproject.R;

import java.util.ArrayList;
import java.util.List;

public class UpComingAdapter extends RecyclerView.Adapter<UpComingAdapter.DataComingAdapter> {


    private Context context;
    private List<EventModel> eventList;
    DbHelper dbHelper;
    UpComingClick upComingClick;

    public interface UpComingClick{
        void EditClick(EventModel upComingClick);
        void DeleteClick(EventModel upComingClick);
    }
    public  void SetUpInterFace(UpComingClick upComingClick){
        this.upComingClick=upComingClick;
    }
    public UpComingAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
        dbHelper = new DbHelper(context);
    }
    public void setEventList(ArrayList<EventModel> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }
   /* public void updateData(ArrayList<EventModel> newDataList) {
        eventList.clear();
        eventList.addAll(newDataList);
        notifyDataSetChanged();
    }*/



    @NonNull
    @Override
    public DataComingAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_item_file, parent, false);
        return new DataComingAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataComingAdapter holder, int position) {
        EventModel event = eventList.get(position);
        holder.dateTextView.setText(event.getDate());
        holder.timeTextView.setText(event.getTime());
        holder.descriptionTextView.setText(event.getDescription());


        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((upComingClick!=null)){
                    upComingClick.EditClick(eventList.get(position));
                }
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (upComingClick != null) {
                upComingClick.DeleteClick(eventList.get(position));
               }
            }
        });
        holder.btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Move Task to Completed?");
                builder.setMessage("Do you want to mark this task as completed?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteEvent(event.getId());
                        dbHelper.addNewCptReminder(event.getDate(), event.getTime(), event.getDescription());
                        eventList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, eventList.size());
                        Toast.makeText(v.getContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Dismiss the dialog if "No" is clicked
                    }
                });
                builder.create().show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class DataComingAdapter extends RecyclerView.ViewHolder {
        TextView dateTextView, timeTextView, descriptionTextView;
        ImageView btnDelete,btnEdit,btnCancel,btnMark;

        public DataComingAdapter(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnCancel=itemView.findViewById(R.id.btnCancel);
            btnMark=itemView.findViewById(R.id.btnMark);

        }


    }


}
