package com.example.sqlproject.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sqlproject.Fragment.Adapter.CompleteAdapter;
import com.example.sqlproject.Fragment.Adapter.DbHelper;
import com.example.sqlproject.Fragment.Adapter.UpComingAdapter;
import com.example.sqlproject.Fragment.modelSql.EventModel;
import com.example.sqlproject.R;

import java.util.ArrayList;

public class CompleteFragment extends Fragment {

    RecyclerView rcvComplete;
    SwipeRefreshLayout swipeCom;
    DbHelper dbHelper;
    CompleteAdapter completeAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete, container, false);
        rcvComplete = view.findViewById(R.id.rcvComplete);
        swipeCom = view.findViewById(R.id.swipeCom);

        dbHelper = new DbHelper(requireContext()); // Initialize the dbHelper

        swipeCom.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ArrayList<EventModel> completedDataList = dbHelper.getAllCompletedData();
                completeAdapter = new CompleteAdapter(requireContext(), completedDataList);
                rcvComplete.setAdapter(completeAdapter);
                rcvComplete.setLayoutManager(new LinearLayoutManager(requireContext()));
                swipeCom.setRefreshing(false);
            }
        });

        completeAdapter = new CompleteAdapter(requireContext(), new ArrayList<>()); // Initialize the adapter

        completeAdapter.SetUpInterFace1(new CompleteAdapter.CompleteClick() {
            @Override
            public void DeleteClick1(EventModel eventModel) {
                // Show a confirmation dialog
                showDeleteConfirmationDialog(eventModel);
            }
        });

        return view;
    }

    private void showDeleteConfirmationDialog(EventModel eventModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform the delete operation here
                dbHelper.deleteCompletedEvent(eventModel.getId()); // Modify this to match your database
                refreshCompleteList();
                Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.create().show();
    }

    private void refreshCompleteList() {
        ArrayList<EventModel> completedDataList = dbHelper.getAllCompletedData();
        completeAdapter = new CompleteAdapter(requireContext(), completedDataList);
        rcvComplete.setAdapter(completeAdapter);
    }


}
