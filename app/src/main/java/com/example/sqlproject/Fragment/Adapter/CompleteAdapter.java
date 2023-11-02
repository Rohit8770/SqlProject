package com.example.sqlproject.Fragment.Adapter;

import android.content.Context;
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

import java.util.List;

public class CompleteAdapter extends RecyclerView.Adapter<CompleteAdapter.CompleteViewHolder>{
    Context context;
    List<EventModel> eventModelList;
    DbHelper dbHelper;
    CompleteAdapter.CompleteClick completeClick;

    public interface CompleteClick{
        void DeleteClick1(EventModel completeClick);
    }
    public  void SetUpInterFace1(CompleteAdapter.CompleteClick completeClick){
        this.completeClick=completeClick;
    }

    public CompleteAdapter(Context context, List<EventModel> eventModelList) {
        this.context = context;
        this.eventModelList = eventModelList;
    }
    public void deleteItem(int position) {
        if (position >= 0 && position < eventModelList.size()) {
            EventModel eventModel = eventModelList.get(position);
            eventModelList.remove(position);
            notifyItemRemoved(position);
            // Delete the item from the database using the dbHelper
            dbHelper.deleteCompletedEvent(eventModel.getId());
        }
    }

    @NonNull
    @Override
    public CompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.complete_item_file,parent,false);
        return  new CompleteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteViewHolder holder, int position) {
        EventModel model=eventModelList.get(position);
        holder.ComDate.setText(model.getDate());
        holder.ComeTime.setText(model.getTime());
        holder.ComDesc.setText(model.getDescription());

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //      Toast.makeText(context, "delete ", Toast.LENGTH_SHORT).show();
                if (completeClick != null) {
                    completeClick.DeleteClick1(eventModelList.get(position));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return eventModelList.size();
    }

    public  class CompleteViewHolder extends RecyclerView.ViewHolder {

        TextView ComDate,ComeTime,ComDesc;
        ImageView buttonDelete;
        public CompleteViewHolder(@NonNull View itemView) {
            super(itemView);
            ComDate=itemView.findViewById(R.id.Comdate);
            ComeTime=itemView.findViewById(R.id.ComTime);
            ComDesc=itemView.findViewById(R.id.ComDesc);
            buttonDelete=itemView.findViewById(R.id.buttonDelete);
        }
    }
}
