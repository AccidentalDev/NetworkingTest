package com.example.brerlappin.networkingtest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class NetworksTest extends Activity {
    TextView textView;
    EditText searchField;
    Button searchButton;
    static String url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    public String resultString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networks_test);

        textView = (TextView) findViewById(R.id.display);
        searchField = (EditText) findViewById(R.id.editText);
        searchButton = (Button) findViewById(R.id.submit);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!searchField.getText().toString().equals("")){
                    Thread connectionThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resultString = SearchRequest(searchField.getText().toString());
                                responseHandler.sendEmptyMessage(0);
                            }catch (Exception e){
                                Log.e("Search Exception", "Error in SearchRequest");
                                e.printStackTrace();
                            }
                        }
                    });
                    connectionThread.start();
                }
            }
        });
    }

    public Handler responseHandler = new Handler(){
        public void handleMessage(Message msg){
            try {
                ProcessResponse(resultString);
            } catch (Exception e) {
                Log.e("Parsing Exception", "Error in ProcessResponse");
                textView.setText("\n" + e.getMessage() + "\n" + resultString);
                e.printStackTrace();
            }
            searchField.setText("");
        }
    };

    public String SearchRequest(String searchString)
            throws MalformedURLException, IOException{

        String newFeed = url+searchString;
        StringBuilder response = new StringBuilder();
        Log.v("Search", "URL: "+newFeed);
        URL url = new URL(newFeed);

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()), 8192);
            String strLine = null;
            while((strLine = input.readLine()) != null){
                response.append(strLine);
            }
            input.close();
        }
        return response.toString();
    }

    public void ProcessResponse(String resp) throws IllegalStateException,
            IOException, JSONException, NoSuchAlgorithmException{

        Log.v("Search", "Result: "+resp);

        JSONObject mResponseObject = new JSONObject(resp);
        int status = mResponseObject.getInt("responseStatus");
        //Log.v("Search", Integer.toString(status));

        if(status < 300) {
            StringBuilder strB = new StringBuilder();
            JSONObject respObject = mResponseObject.getJSONObject("responseData");
            JSONArray array = respObject.getJSONArray("results");
            Log.v("Search", "Number of results: "+array.length());

            for(int i=0; i<array.length(); i++){
                Log.v("Result", i+"-"+array.get(i).toString());
                String title = array.getJSONObject(i).getString("title");
                String urlLink = array.getJSONObject(i).getString("visibleURL");

                strB.append(title);
                strB.append("\n");
                strB.append(urlLink);
                strB.append("\n");
            }
            textView.setText(strB.toString());
        }else{
            String textResult = "\nError Code: "+status+"\n\nDetails: "+mResponseObject.getString("responseDetails");
            textView.setText(textResult);
        }
    }
}
