package com.hiepdt.donation.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hiepdt.donation.R;
import com.hiepdt.donation.api.DonationApi;
import com.hiepdt.donation.models.Donation;

import java.util.List;

public class Report extends Base implements AdapterView.OnItemClickListener, View.OnClickListener {
    ListView listView;
    DonationAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        listView = findViewById(R.id.reportList);
        mSwipeRefreshLayout = findViewById(R.id.report_swipe_refresh_layout);

        new GetAllTask(this).execute("/donations");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetAllTask(Report.this).execute("/donations");
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View row, int pos, long id) {

        new GetTask(this).execute("/donations", row.getTag().toString());
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof Donation) {
            onDonationDelete((Donation) view.getTag());
        }
    }

    public void onDonationDelete(final Donation donation) {
        String stringId = donation._id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the \'Donation with ID \' \n [ "
                + stringId + " ] ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                new DeleteTask(Report.this).execute("/donations", donation._id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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
                return DonationApi.getAll(params[0]);
            } catch (Exception e) {
                Log.v("ASYNC", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);

            app.donations = result;
            adapter = new DonationAdapter(context, app.donations);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(Report.this);
            mSwipeRefreshLayout.setRefreshing(false);

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class GetTask extends AsyncTask<String, Void, Donation> {

        protected ProgressDialog dialog;
        protected Context context;

        public GetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donation Details");
            this.dialog.show();
        }

        @Override
        protected Donation doInBackground(String... params) {

            try {
                return DonationApi.get(params[0], params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Donation result) {
            super.onPostExecute(result);

            Donation donation = result;

            Toast.makeText(Report.this, "Donation Data [ " + donation.upvotes + "]\n " +
                    "With ID of [" + donation._id + "]", Toast.LENGTH_LONG).show();

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;

        public DeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return DonationApi.delete(params[0], params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            String s = result;
            Log.v("donate", "DELETE REQUEST : " + s);

            new GetAllTask(Report.this).execute("/donations");

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


    class DonationAdapter extends ArrayAdapter<Donation> {
        private Context context;
        public List<Donation> donations;

        public DonationAdapter(Context context, List<Donation> donations) {
            super(context, R.layout.row_donate, donations);
            this.context = context;
            this.donations = donations;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.row_donate, parent, false);
            Donation donation = donations.get(position);

            ImageView imgDelete = view.findViewById(R.id.imgDelete);
            imgDelete.setTag(donation);
            imgDelete.setOnClickListener(Report.this);

            TextView amountView = view.findViewById(R.id.row_amount);
            TextView methodView = view.findViewById(R.id.row_method);
            TextView upvotesView = view.findViewById(R.id.row_upvotes);

            amountView.setText("" + donation.amount);
            methodView.setText(donation.paymenttype);
            upvotesView.setText("" + donation.upvotes);

            view.setTag(donation._id); // setting the 'row' id to the id of the donation

            return view;
        }

        @Override
        public int getCount() {
            return donations.size();
        }
    }
}
