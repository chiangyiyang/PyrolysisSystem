package com.example.yiyang.pyrolysissystem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class HeaterActivity extends AppCompatActivity {

    private String mMeterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heater);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.heater_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = this.getIntent();
        mMeterId = intent.getStringExtra("meter");
        ((TextView) findViewById(R.id.txtDeviceName)).setText(mMeterId);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.heater_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                Intent intent = new Intent(HeaterActivity.this, MainActivity.class);
                intent.putExtra("msg", mMeterId + ",S," + ((TextView) findViewById(R.id.edtTemperature)).getText().toString()+"\r\n");
                setResult(RESULT_OK, intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}