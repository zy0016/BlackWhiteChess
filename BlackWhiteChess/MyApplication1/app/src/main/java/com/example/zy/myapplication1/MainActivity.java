package com.example.zy.myapplication1;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity  {
    private GameView gameView = null;
    //private Button button_computer_first;
    //private Button button_people_first;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            ExitDialog((gameView != null) ? R.string.exit_current_game : R.string.exit);
        }
        return true;
    }

    protected void ExitDialog(int stringid)
    {
        if (gameView != null && gameView.IfGameOver())
        {
            setContentView(R.layout.activity_chessview);
            gameView = null;
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringid);
        builder.setTitle(R.string.clew);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (gameView != null)
                {
                    setContentView(R.layout.activity_chessview);
                    gameView = null;
                }
                else
                {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    public void computer_first_run(View view)
    {
        gameView = new GameView(this,Chessman.ChessmanType.WHITE,Chessman.ChessmanType.BLACK,GameView.CURRENT_PLAYER.COMPUTER,0);
        setContentView(gameView);
    }

    public void computer_first_run_2(View view)
    {
        gameView = new GameView(this,Chessman.ChessmanType.WHITE,Chessman.ChessmanType.BLACK,GameView.CURRENT_PLAYER.COMPUTER,1);
        setContentView(gameView);
    }

    public void people_first_run(View view)
    {
        gameView = new GameView(this,Chessman.ChessmanType.WHITE,Chessman.ChessmanType.BLACK,GameView.CURRENT_PLAYER.PEOPLE,0);
        setContentView(gameView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chessview);
        /*if (gameView == null)
        {
            gameView = new GameView(this,Chessman.ChessmanType.WHITE,Chessman.ChessmanType.BLACK,GameView.CURRENT_PLAYER.COMPUTER);
        }*/
        /////////////button
        //button_computer_first = (Button)findViewById(R.id.button_computer);
        //button_people_first = (Button)findViewById(R.id.button_people);
        /*button_computer_first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        button_people_first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
        //button
        //setContentView(R.layout.activity_main);
        //setContentView(gameView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(Menu.NONE, Menu.FIRST +1 , 1, "New Game").setIcon(android.R.drawable.ic_menu_call);
        menu.add(Menu.NONE, Menu.FIRST +2 , 2, "Double people");
        menu.add(Menu.NONE, Menu.FIRST +3 , 3, "Easy");
        menu.add(Menu.NONE, Menu.FIRST +4 , 4, "Middle");
        menu.add(Menu.NONE, Menu.FIRST +5 , 5, "Hard");
        menu.add(Menu.NONE, Menu.FIRST +6 , 6, "About");
        menu.add(Menu.NONE, Menu.FIRST +7 , 7, "Player name");
        menu.add(Menu.NONE, Menu.FIRST +8 , 8, "Player data");
        return true;
    }

    private void ShowPrivateDialog(int stringid)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringid);
        builder.setTitle(R.string.app_name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();

            }
        });
        AlertDialog innerAlertDialog = builder.create();
        innerAlertDialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id)
        {
            case Menu.FIRST + 1:
                break;
            case Menu.FIRST + 2:
                ShowPrivateDialog(R.string.no_ready_settings);
                break;
            case Menu.FIRST + 3:

                break;
            case Menu.FIRST + 4:

                break;
            case Menu.FIRST + 5:

                break;
            case Menu.FIRST + 6:

                break;
            case Menu.FIRST + 7:

                break;
            case Menu.FIRST + 8:

                break;
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
