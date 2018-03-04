package com.skripsi.cloudotentikasi;

import android.*;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.skripsi.cloudotentikasi.R.id.listproject;

public class SistemCloud extends AppCompatActivity {

    String[] nomorProject;
    String[] idProject;
    String[] state;
    String[] namaProject;
    String[] waktu;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sistem_cloud);
        Intent intent = getIntent();

        nomorProject = intent.getStringArrayExtra("Number");
        idProject = intent.getStringArrayExtra("Id");
        state = intent.getStringArrayExtra("lifecycle");
        namaProject = intent.getStringArrayExtra("name");
        waktu = intent.getStringArrayExtra("time");

        //System.out.println("Hello " + namaProject[0]);
        listView = (ListView) findViewById(listproject);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_view, R.id.textView, namaProject);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = (String) listView.getItemAtPosition(position);
                Toast.makeText(SistemCloud.this,"Nomor Proyek: " + namaProject[position]
                        + "\n" + "Identitas Proyek : " + idProject[position]
                        + "\n" + "Status : " + state[position]
                        + "\n" + "Nama Proyek : " + namaProject[position]
                        + "\n" + "Waktu dibuat : " + waktu[position],Toast.LENGTH_LONG).show();
            }
        });

    }
}
