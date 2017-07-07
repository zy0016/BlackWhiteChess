package com.example.zy.myapplication1;

import android.accounts.Account;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity  {
    private GameView gameView = null;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            ExitDialog();
        }
        return true;
    }

    protected void ExitDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Exit?");
        builder.setTitle("Clew");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LinearLayout layout = (LinearLayout)findViewById(R.id.LinearLayout1);
        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300,500);
        //params.setMargins(5,5,5,5);
        if (gameView == null)
        {
            gameView = new GameView(this,Chessman.ChessmanType.WHITE,Chessman.ChessmanType.BLACK,GameView.CURRENT_PLAYER.COMPUTER);
        }

        //setContentView(R.layout.activity_main);
        setContentView(gameView);
        //layout.addView(gameView);
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
