package com.hiepdt.donation.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hiepdt.donation.R;
import com.hiepdt.donation.api.DonationApi;
import com.hiepdt.donation.models.Donation;

import java.util.ArrayList;
import java.util.List;

public class Donate extends Base {

    private Button donateButton;
    private RadioGroup paymentMethod;
    private ProgressBar progressBar;
    private NumberPicker amountPicker;
    private EditText amountText;
    private TextView amountTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        donateButton = findViewById(R.id.donateButton);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donateButtonPressed(v);
            }
        });

        paymentMethod = findViewById(R.id.paymentMethod);
        progressBar = findViewById(R.id.progressBar);
        amountPicker = findViewById(R.id.amountPicker);
        amountText = findViewById(R.id.paymentAmount);
        amountTotal = findViewById(R.id.totalSoFar);

        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(1000);
        progressBar.setMax(10000);
        amountTotal.setText("$" + app.totalDonated);

    }

    public void donateButtonPressed(View view) {
        String method = paymentMethod.getCheckedRadioButtonId() == R.id.PayPal ? "PayPal" : "Direct";
        int donatedAmount = amountPicker.getValue();
        if (donatedAmount == 0) {
            String text = amountText.getText().toString();
            if (!text.equals(""))
                donatedAmount = Integer.parseInt(text);
        }
        if (donatedAmount > 0) {
            Donation donation = new Donation(donatedAmount, method, 0);
            app.newDonation(donation);
            progressBar.setProgress(app.totalDonated);
            String totalDonatedStr = "$" + app.totalDonated;
            amountTotal.setText(totalDonatedStr);
            new InsertTask(this).execute("/donations", donation);
        }
    }

    @Override
    public void reset(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete ALL Donations?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete ALL the donations");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                app.totalDonated = 0;
                amountTotal.setText("$" + app.totalDonated);
                new ResetTask(Donate.this).execute("/donations");
            }
        }).setNegativeButton("No", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetAllTask(this).execute("/donations");
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {

        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }

        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                Log.v("donate", "Donation App Getting All Donations");
                return DonationApi.getAll(params[0]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);

            //use result to calculate the totalDonated amount here

            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);

            if (dialog.isShowing())
                dialog.dismiss();
            Toast.makeText(Donate.this, "Get all donations success!!", Toast.LENGTH_SHORT).show();

        }
    }


    //Todo: Đã xử lý
    private class InsertTask extends AsyncTask<Object, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            String res = null;
            try {
                Log.v("donate", "Donation App Inserting");
                res = DonationApi.insert((String) params[0], (Donation) params[1]);

            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog.isShowing())
                dialog.dismiss();
            Toast.makeText(Donate.this, "Insert success!!", Toast.LENGTH_SHORT).show();

        }
    }

    //Todo: Đã xử lý
    private class ResetTask extends AsyncTask<Object, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;

        public ResetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donations....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            String res = null;
            try {
                //Todo: Xóa toàn bộ phần tử
                ArrayList<Donation>donations = (ArrayList<Donation>) DonationApi.getAll((String) params[0]);

                for (int i = 0; i < donations.size(); i++){
                    String id = donations.get(i)._id;
                    System.out.println(id);
                    res = DonationApi.delete((String) params[0], id);
                }
            } catch (Exception e) {
                Log.v("donate", " RESET ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            app.totalDonated = 0;
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);

            if (dialog.isShowing())
                dialog.dismiss();
            Toast.makeText(Donate.this, "Reset success!!", Toast.LENGTH_SHORT).show();
        }
    }


}