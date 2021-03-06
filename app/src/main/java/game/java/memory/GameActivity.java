package game.java.memory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import game.java.memory.containers.GetActivePlayer;
import game.java.memory.containers.GetGameScore;
import game.java.memory.containers.GetNoShownMoves;
import game.java.memory.containers.MakeMove;
import game.java.memory.containers.Point;

public class GameActivity extends AppCompatActivity {

    public enum GameMoves { MOVE, STOPMOVE, MOVEOPP, STOPMOVEOPP, CHANGETIME, TURA, SCORE, END }
    private Activity activity = this;
    Button[][] buttonArray;
    ArrayList<Point> pointArrayList = new ArrayList<>();
    private static TableLayout tl;
    private static TextView tura;
    private static TextView yourScore;
    private static TextView oppScore;
    private static TextView time;

    private boolean chanegePosition;
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    private int mx1;
    private int my1;

    private int scorePlayer1;
    private int scorePlayer2;

    boolean endGame = false;
    int gameId;
    int player;
    Thread game = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            gameId = b.getInt("gameId");
            player = b.getInt("player");
        }

        chanegePosition = false;

        scorePlayer1 = 0;
        scorePlayer2 = 0;
        x1 = -1;
        y1 = -1;
        x2 = -1;
        y2 = -1;
        mx1 = -1;
        my1 = -1;

        tura = (TextView)findViewById(R.id.tura);
        yourScore = (TextView)findViewById(R.id.your_score);
        oppScore = (TextView)findViewById(R.id.opp_score);
        time = (TextView)findViewById(R.id.time);

        TableRow.LayoutParams lp = new TableRow.LayoutParams(1,android.widget.TableRow.LayoutParams.MATCH_PARENT,1f);
        Button btn;
        tl = (TableLayout) findViewById(R.id.map);
        buttonArray = new Button[6][6];
        for (Integer y = 0; y < 6; y++) {
            TableRow row = new TableRow(this);
            for (Integer x = 0; x< 6; x++) {
                btn = new Button(this);
                btn.setLayoutParams(lp);
                setOnClick(btn, x, y);
                row.addView(btn);
                buttonArray[x][y] = btn;
            }
            tl.addView(row);
        }
        tl.requestLayout();

        game = new Thread() {
            public void run() {
                game(this);
            }
        };
        game.start();
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                            exit();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Poddac sie ?").setPositiveButton("Tak", dialogClickListener).setNegativeButton("Nie", dialogClickListener).show();
    }



    private void exit()
    {
        game.interrupt();
        this.finish();
    }



    final Handler handler = new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            if(msg.what== GameMoves.MOVE.ordinal())
            {
                MakeMove makeMove = (MakeMove)msg.obj;
                if(mx1 != -1 && my1 != -1)
                {
                    buttonArray[mx1][my1].getBackground().clearColorFilter();
                    mx1 = -1;
                    my1 = -1;
                }
                buttonArray[makeMove.x1][makeMove.y1].setText(Integer.toString(makeMove.value1));
                buttonArray[makeMove.x2][makeMove.y2].setText(Integer.toString(makeMove.value2));
                buttonArray[makeMove.x1][makeMove.y1].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                buttonArray[makeMove.x2][makeMove.y2].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

            }
            if(msg.what== GameMoves.STOPMOVE.ordinal())
            {
                MakeMove makeMove = (MakeMove)msg.obj;
                Button btn1 = buttonArray[makeMove.x1][makeMove.y1];
                Button btn2 = buttonArray[makeMove.x2][makeMove.y2];
                btn1.getBackground().clearColorFilter();
                btn2.getBackground().clearColorFilter();
                if(btn1.getText().equals(btn2.getText()))
                {
                    if(player == 1) {
                        setScorePlayer1(getScorePlayer1() + 1);
                        yourScore.setText(Integer.toString(getScorePlayer1()));
                    }
                    else {
                        setScorePlayer2(getScorePlayer2() + 1);
                        yourScore.setText(Integer.toString(getScorePlayer2()));
                    }
                    buttonArray[makeMove.x1][makeMove.y1] = null;
                    buttonArray[makeMove.x2][makeMove.y2] = null;
                }
                else
                {
                    btn1.setText("");
                    btn2.setText("");
                }

            }
            if(msg.what== GameMoves.MOVEOPP.ordinal())
            {
                GetNoShownMoves makeMove = (GetNoShownMoves)msg.obj;
                buttonArray[makeMove.x1][makeMove.y1].setText(Integer.toString(makeMove.value1));
                buttonArray[makeMove.x2][makeMove.y2].setText(Integer.toString(makeMove.value2));
                buttonArray[makeMove.x1][makeMove.y1].getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                buttonArray[makeMove.x2][makeMove.y2].getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
            }
            if(msg.what== GameMoves.STOPMOVEOPP.ordinal())
            {
                GetNoShownMoves makeMove = (GetNoShownMoves)msg.obj;
                Button btn1 = buttonArray[makeMove.x1][makeMove.y1];
                Button btn2 = buttonArray[makeMove.x2][makeMove.y2];
                btn1.getBackground().clearColorFilter();
                btn2.getBackground().clearColorFilter();
                if(btn1.getText().equals(btn2.getText()))
                {
                    if(player == 2) {
                        setScorePlayer1(getScorePlayer1() + 1);
                        oppScore.setText(Integer.toString(getScorePlayer1()));
                    }
                    else {
                        setScorePlayer2(getScorePlayer2() + 1);
                        oppScore.setText(Integer.toString(getScorePlayer2()));
                    }

                    buttonArray[makeMove.x1][makeMove.y1] = null;
                    buttonArray[makeMove.x2][makeMove.y2] = null;
                }
                else
                {
                    btn1.setText("");
                    btn2.setText("");
                }

            }
            if(msg.what== GameMoves.CHANGETIME.ordinal())
            {
                if(msg.arg1 == -1)
                    time.setText("X");
                else
                    time.setText(Integer.toString(msg.arg1));
            }
            if(msg.what== GameMoves.TURA.ordinal())
            {
                if(msg.arg1 == player)
                {
                    tura.setText("Tura: twoja");
                }
                else
                {
                    tura.setText("Tura: przeciwnika");
                }
            }
            if(msg.what== GameMoves.SCORE.ordinal())
            {
//                if(msg.arg1 == player)
//                {
//                    yourScore.setText(Integer.toString(msg.arg1));
//                    oppScore.setText(Integer.toString(msg.arg2));
//                }
//                else
//                {
//                    yourScore.setText(Integer.toString(msg.arg2));
//                    oppScore.setText(Integer.toString(msg.arg1));
//                }
            }
            if(msg.what== GameMoves.END.ordinal())
            {
                int yscore, opscore;
                if(player == 1)
                {
                    yscore = getScorePlayer1();
                    opscore = getScorePlayer2();
                }
                else
                {
                    yscore = getScorePlayer2();
                    opscore = getScorePlayer1();
                }


                if(yscore > opscore)
                {
                    end("you win!!!", "OK");
                }
                else if(yscore < opscore)
                {
                    end("you lose", "OK");
                }
                else
                {
                    end("draw", "OK");
                }
            }
        }
    };
    private void end(String text, String text1)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        exit();
                        finish();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton(text1, dialogClickListener).show();
    }

    private void sendHandler(GameMoves gameMoves, Object obj, int arg1, int arg2)
    {
        Message msg = handler.obtainMessage();
        msg.what = gameMoves.ordinal();
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        handler.sendMessage(msg);
    }

    private void setOnClick(final Button btn, final int x, final int y){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChanegePosition()) {
                    if(buttonArray[x][y] != null) {
                        if (getX1() == -1) {
                            setX1(x);
                            setY1(y);
                            mx1 = x;
                            my1 = y;
                            buttonArray[x][y].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                        } else if(x != mx1 || y != my1){
                            setX2(x);
                            setY2(y);
                            setChanegePosition(false);
                            buttonArray[x][y].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                        }
                    }
                }
            }
        });
    }

    private void game(Thread thread)
    {
        GetActivePlayer getActivePlayer = null;
        try {
            getActivePlayer = WebAPI.getActivePlayer(gameId);
            sendHandler(GameMoves.TURA, null, getActivePlayer.playerID, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(!thread.isInterrupted())
        {
            try {
                getActivePlayer = WebAPI.getActivePlayer(gameId);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showOppMove();
            if(getScorePlayer1() + getScorePlayer2() == 18)//getActivePlayer.playerID == 0
            {
                sendHandler(GameMoves.END, null, 0, 0);
                break;
            }
            if(getActivePlayer.playerID == player)//getActivePlayer.playerID == player
            {
                makeMove();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showOppMove()
    {
        List<GetNoShownMoves> getNoShownMovesList = null;
        GetGameScore getGameScore = null;
        try {
            getNoShownMovesList = WebAPI.getNotShownMoves(gameId);
            getGameScore = WebAPI.getGameScore(gameId);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetNoShownMoves getNoShownMoves;
        for(int i = 0; i < getNoShownMovesList.size(); i++)
        {
            getNoShownMoves = getNoShownMovesList.get(i);
            sendHandler(GameMoves.MOVEOPP, getNoShownMoves, 0, 0);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendHandler(GameMoves.STOPMOVEOPP, getNoShownMoves, 0, 0);
        }
        sendHandler(GameMoves.SCORE, null, getGameScore.player1Score, getGameScore.player2Score);
    }

    private long countTimeElapsed(long startTime) {
        Long a = System.currentTimeMillis() - startTime;
        return a/1000;
    }

    private void makeMove() {
        sendHandler(GameMoves.TURA, null, player, 0);
        Long startTime = System.currentTimeMillis();
        setChanegePosition(true);
        while (countTimeElapsed(startTime) < 5 && isChanegePosition()) {
            if ((5 - countTimeElapsed(startTime)) > 4)
                sendHandler(GameMoves.CHANGETIME, null, 5, 0);
            else if ((5 - countTimeElapsed(startTime)) > 3)
                sendHandler(GameMoves.CHANGETIME, null, 4, 0);
            else if ((5 - countTimeElapsed(startTime)) > 2)
                sendHandler(GameMoves.CHANGETIME, null, 3, 0);
            else if ((5 - countTimeElapsed(startTime)) > 1)
                sendHandler(GameMoves.CHANGETIME, null, 2, 0);
            else if ((5 - countTimeElapsed(startTime)) > 0)
                sendHandler(GameMoves.CHANGETIME, null, 1, 0);
        }
        setChanegePosition(false);
        sendHandler(GameMoves.CHANGETIME, null, -1, 0);
        MakeMove makeMove = null;
        try {
            if(getX1() == -1 || getY1() == -1 || getX2() == -1 || getY2() == -1)
            {
                Random generator = new Random();
                Button b,a;
                int x1=-1, y1=-1;
                int x2=-1, y2=-1;
                int max = 0;
                do{
                    if(max < 5) {
                        x1 = generator.nextInt(6);
                        x2 = generator.nextInt(6);
                        y1 = generator.nextInt(6);
                        y2 = generator.nextInt(6);
                    }
                    else
                    {
                        max =0;
                        for (Integer y = 0; y < 6; y++) {
                            for (Integer x = 0; x< 6; x++) {
                                if(buttonArray[x][y] != null)
                                {
                                    if(max==0)
                                    {
                                        x1 = x;
                                        y1 = y;
                                        max++;
                                    }
                                    else
                                    {
                                        x2 = x;
                                        y2 = y;
                                        max++;
                                        break;
                                    }

                                }
                            }
                            if(max==2)
                                break;
                        }
                    }
                    max++;
                    if(x1 != x2)
                        if(y1 != y2)
                            if(buttonArray[x1][y1] != null)
                                if(buttonArray[x2][y2] != null)
                                    break;;

                }while(true);
                makeMove = WebAPI.makeMove(player, gameId, x1, y1, x2, y2);
            }
            else
            {
                makeMove = WebAPI.makeMove(player, gameId, getX1(), getY1(), getX2(), getY2());
            }

            sendHandler(GameMoves.MOVE, makeMove, 0, 0);
            Thread.sleep(2000);
            sendHandler(GameMoves.STOPMOVE, makeMove, 0, 0);
            Thread.sleep(200);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setX1(-1);
        setY1(-1);
        setX2(-1);
        setY2(-1);
        GetActivePlayer getActivePlayer = null;
        GetGameScore getGameScore = null;
        try {
            getActivePlayer = WebAPI.getActivePlayer(gameId);
            getGameScore = WebAPI.getGameScore(gameId);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendHandler(GameMoves.SCORE, null, getGameScore.player1Score, getGameScore.player2Score);
        if (getActivePlayer.playerID == player)
        {
            if(getScorePlayer1() + getScorePlayer2() == 18)//getActivePlayer.playerID == 0
                return;
            makeMove();
        }
        else
        {
            sendHandler(GameMoves.TURA, null, getActivePlayer.playerID, 0);
        }
    }

    public synchronized int getY1() {
        return y1;
    }

    public synchronized void setY1(int y) {
        this.y1 = y;
    }

    public synchronized int getX1() {
        return x1;
    }

    public synchronized void setX1(int x) {
        this.x1 = x;
    }

    public synchronized int getY2() {
        return y2;
    }

    public synchronized void setY2(int y2) {
        this.y2 = y2;
    }

    public synchronized int getX2() {
        return x2;
    }

    public synchronized void setX2(int x2) {
        this.x2 = x2;
    }

    public synchronized int getScorePlayer2() {
        return scorePlayer2;
    }

    public synchronized void setScorePlayer2(int scorePlayer2) {
        this.scorePlayer2 = scorePlayer2;
    }

    public synchronized int getScorePlayer1() {
        return scorePlayer1;
    }

    public synchronized void setScorePlayer1(int scorePlayer1) {
        this.scorePlayer1 = scorePlayer1;
    }

    public synchronized boolean isChanegePosition() {
        return chanegePosition;
    }

    public synchronized void setChanegePosition(boolean chanegePosition) {
        this.chanegePosition = chanegePosition;
    }


}
