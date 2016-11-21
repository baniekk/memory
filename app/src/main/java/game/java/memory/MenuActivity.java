package game.java.memory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MenuActivity extends AppCompatActivity {
    private Button start_btn, sendButton;
    private EditText editLogin;
    private TextView gameID;
    private static String serverIP = "http://192.168.0.2:8080";
    private static String server = "/MemoryService_war_exploded/MemoryService/";
    private static String getGameUrl = serverIP + server + "GetGame";
    private static String isMyTurnUrl = serverIP + server + "GetActivePlayer";
    private static String makeMoveUrl = serverIP + server + "MakeMove";
    private static String opponentMovesUrl = serverIP + server + "GetNotShownMoves";
    private static String gameScoreUrl = serverIP + server + "GetGameScore";
    private String playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        OnClickButtonListener();
        OnClickEditTextListener();
    }

    private class AsyncCaller extends AsyncTask<String, Void, JSONObject>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String url = "";
            for(int i = 0; i < params.length; i++)
            {
                url += params[i] + "/";
            }

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.getJSONFromUrl(url, null);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            //this method will be running on UI thread

            doSomethingImportantWithJSON(result);
        }
    }

    private void doSomethingImportantWithJSON(JSONObject json) {
        gameID = (TextView) findViewById(R.id.textView);
        try {
            String gameId = json.get("GameId").toString();
            String playerNo =  json.get("PlayerNo").toString();
            gameID.setText("Game id: " + gameId + ", playerNo: " + playerNo);
        } catch (JSONException e) {
            e.printStackTrace();
        };
    }

    public void OnClickButtonListener()
    {
        start_btn = (Button)findViewById(R.id.btn_start);
        start_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("game.java.memory.GameActivity");
                        startActivity(intent);
                    }
                }
        );

        sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncCaller().execute(getGameUrl, "6");
                    }
                }
        );
    }

    public void OnClickEditTextListener()
    {
        editLogin = (EditText) findViewById(R.id.editText);
        editLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editLogin.setText("");
                    }
                }
        );
    }
}
