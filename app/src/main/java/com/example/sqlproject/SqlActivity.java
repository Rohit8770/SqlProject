package com.example.sqlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sqlproject.Fragment.CompleteFragment;
import com.example.sqlproject.Fragment.UpcomingFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.Executor;

public class SqlActivity extends AppCompatActivity {
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences sharedPreferences;
    private ImageView imgLock;
    LinearLayout main_layout;
    SwitchCompat etSwitch;
    private static final String SWITCH_STATE_KEY = "biometricSwitchState";
    private boolean isBiometricEnabled = false;
    private boolean userLoggedIn = false;
    TabLayout tab1;
    ViewPager2 view1;
    UpcomingFragment upcommingFragment;
    CompleteFragment completeFragment;
    Intent i;
    String from,newData, newTime, newDesc;
    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql);

        etSwitch = findViewById(R.id.etSwitch);
        imgLock=findViewById(R.id.imgLock);
        main_layout=findViewById(R.id.main_layout);


        i = getIntent();

        from = i.getStringExtra("from");
        if (from != null && from.equals("notification")){
            newDesc =i.getStringExtra("description");
            newData =i.getStringExtra("date");
            newTime =i.getStringExtra("time");
            showNotification(newDesc,newData,newTime);
        }


        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isBiometricEnabled = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        etSwitch.setChecked(isBiometricEnabled);

        etSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBiometricEnabled = isChecked;
            sharedPreferences.edit().putBoolean(SWITCH_STATE_KEY, isBiometricEnabled).apply();

            if (isChecked) {
                imgLock.setImageResource(R.drawable.lock_color);
                enableBiometricAuthentication();
            } else {
                imgLock.setImageResource(R.drawable.lock_unlock);
                disableBiometricAuthentication();
            }
        });

        if (isBiometricEnabled) {
            main_layout.setVisibility(View.GONE);
            enableBiometricAuthentication();
        }



        tab1=findViewById(R.id.tab1);
        view1=findViewById(R.id.view1);
        upcommingFragment=new UpcomingFragment();
        completeFragment=new CompleteFragment();

        view1.setAdapter(new ViewPagerAdapter(SqlActivity.this));
        new TabLayoutMediator(tab1, view1, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position==0)
                    tab.setText("UpComing");
                else
                    tab.setText("Complete");
            }
        }).attach();
    }
    public static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position==0)
                return new UpcomingFragment();
            else
                return  new CompleteFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
    private void disableBiometricAuthentication() {
        Toast.makeText(this, "App Lock Deactivated", Toast.LENGTH_SHORT).show();
    }

    private void enableBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(SqlActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(SqlActivity.this, "App Lock Activated", Toast.LENGTH_SHORT).show();
                    main_layout.setVisibility(View.VISIBLE);
                    etSwitch.setChecked(true);
                    userLoggedIn = true;
                    imgLock.setImageResource(R.drawable.lock_color);
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(SqlActivity.this, errString, Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(SqlActivity.this);
                    builder.setTitle("Authentication");
                    builder.setMessage("Authentication is Required. \nDo you want to continue without Security?");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            main_layout.setVisibility(View.GONE);
                            imgLock.setImageResource(R.drawable.lock_unlock);
                            dialogInterface.dismiss();
                            enableBiometricAuthentication();
                            main_layout.setVisibility(View.VISIBLE);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!userLoggedIn) {
                                etSwitch.setChecked(true);
                                finish();
                            } else {
                                etSwitch.setChecked(false); // Keep the switch in the checked state
                                main_layout.setVisibility(View.VISIBLE);
                                imgLock.setImageResource(R.drawable.lock_unlock);
                                dialogInterface.dismiss();
                            }
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Lock Reminder")
                    .setDescription("Use PIN, PATTERN or PASSWORD to unlock")
                    .setDeviceCredentialAllowed(true)
                    .setNegativeButtonText(null)
                    .build();
            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Device Doesn't Support Biometric Authentication", Toast.LENGTH_SHORT).show();
            etSwitch.setChecked(false);
            imgLock.setImageResource(R.drawable.lock_unlock);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
        }
    }


    @SuppressLint("MissingInflatedId")
    private void showNotification(String description, String data, String time){
        AlertDialog.Builder builder =new AlertDialog.Builder(SqlActivity.this);
        View notificationView = getLayoutInflater().inflate(R.layout.notification_item_file,null);
        builder.setView(notificationView);
        builder.setTitle("Notification Details");
        builder.setPositiveButton("OK",((dialog, which) -> dialog.dismiss()));

       TextView txnotiDesc = notificationView.findViewById(R.id.txnotiDesc);
        TextView txnotiDate = notificationView.findViewById(R.id.txnotiDate);
        TextView txnotiTime = notificationView.findViewById(R.id.txnotiTime);

        txnotiDesc.setText(description);
        txnotiDate.setText(data);
        txnotiTime.setText(time);

        builder.show();
        Log.e("NotificationExtras","Description" +description +",Data" +data +",Time" +time);

    }

}
