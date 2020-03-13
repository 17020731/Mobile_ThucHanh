package com.hiepdt.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.hiepdt.donation.R;
import com.hiepdt.donation.main.DonationApp;

public class Base extends AppCompatActivity {

    public DonationApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = new DonationApp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_donate, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem report = menu.findItem(R.id.menuReport);
        MenuItem donate = menu.findItem(R.id.menuDonate);
        MenuItem reset = menu.findItem(R.id.menuReset);

        if (app.donations.isEmpty()) {
            report.setEnabled(false);
            reset.setEnabled(false);
        } else {
            report.setEnabled(true);
            reset.setEnabled(true);
        }
        if (this instanceof Donate) {
            donate.setVisible(false);
            if (!app.donations.isEmpty()) {
                report.setVisible(true);
                reset.setEnabled(true);
            }
        } else {
            report.setVisible(false);
            donate.setVisible(true);
            reset.setVisible(false);
        }

        return true;
    }

    public void report(MenuItem item) {
        startActivity(new Intent(this, Report.class));
    }

    public void donate(MenuItem item) {
        startActivity(new Intent(this, Donate.class));
    }

    public void reset(MenuItem item) {
    }

}