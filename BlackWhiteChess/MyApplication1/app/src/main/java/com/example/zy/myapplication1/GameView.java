package com.example.zy.myapplication1;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Bitmap;
import android.os.Handler;
import android.media.AudioManager;
import android.media.SoundPool;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/23.
 */

public class GameView extends View {
    public GameView(Context context,Chessman.ChessmanType people_role,Chessman.ChessmanType program_role,CURRENT_PLAYER current_player,int gridtype)
    {
        super(context,null);

        Computer_Role = program_role;
        People_Role = people_role;
        Current_Player = current_player;
        second = 0;
        minute = 0;
        ComputerTurnChessCount = 0;
        PeopleTurnChessCount = 0;
        bCanDisplayRegretButton = false;
        bCanDoRegrect = false;
        initGameView(context,gridtype);

        bwAlgorithm = new BlackWhiteAlgorithm(chessman,BlockNum);
        if (Current_Player == CURRENT_PLAYER.COMPUTER)
        {
            ComputerRun();
            StartGameTimer();
        }
    }

    public boolean IfGameOver()
    {
        return bwAlgorithm.IfGameOver();
    }
    private void DrawChessAgain()
    {
        this.postInvalidate();
    }

    private void initGameView(Context context,int gridtype)
    {
        InitHandler();
        InitPicture();
        InitChessGrid(gridtype);
        soundPool = new SoundPool(10,AudioManager.STREAM_SYSTEM,5);
        soundPool.load(context,R.raw.notify,1);
    }
    private void InitPicture()
    {
        bitmap_black = BitmapFactory.decodeResource(getResources(),R.drawable.chess_black);
        bitmap_white = BitmapFactory.decodeResource(getResources(),R.drawable.chess_white);
        bitmap_orange = BitmapFactory.decodeResource(getResources(),R.drawable.chess_orange);
        bitmap_back = BitmapFactory.decodeResource(getResources(),R.drawable.back);
        bh = bitmap_black.getHeight();
        bw = bitmap_black.getWidth();
        back_button_w = bitmap_back.getWidth();
        back_button_h = bitmap_back.getHeight();
    }
    private void InitChessGrid(int gridtype)
    {
        chessman = new Chessman[BlockNum][BlockNum];
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                chessman[row][col] = new Chessman();
                chessman[row][col].x = hx_grid + bw * col;
                chessman[row][col].y = hy_grid + bh * row;
                chessman[row][col].w = bw;
                chessman[row][col].h = bh;
                chessman[row][col].ct = Chessman.ChessmanType.NONE;
                chessman[row][col].Orange = false;
            }
        }
        if (gridtype == 0)
        {
            chessman[BlockNum / 2 - 1][BlockNum / 2 - 1].ct = Chessman.ChessmanType.WHITE;
            chessman[BlockNum / 2 - 1][BlockNum / 2].ct = Chessman.ChessmanType.BLACK;
            chessman[BlockNum / 2][BlockNum / 2 - 1].ct = Chessman.ChessmanType.BLACK;
            chessman[BlockNum / 2][BlockNum  /2].ct = Chessman.ChessmanType.WHITE;
        }
        else
        {
            chessman[BlockNum / 2 - 1][BlockNum / 2 - 1].ct = Chessman.ChessmanType.BLACK;
            chessman[BlockNum / 2 - 1][BlockNum / 2].ct = Chessman.ChessmanType.WHITE;
            chessman[BlockNum / 2][BlockNum / 2 - 1].ct = Chessman.ChessmanType.WHITE;
            chessman[BlockNum / 2][BlockNum  /2].ct = Chessman.ChessmanType.BLACK;
        }
        ////////////////////////////////
        chessman_regret = new Chessman[BlockNum][BlockNum];
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                chessman_regret[row][col] = new Chessman();
                chessman_regret[row][col].ct = chessman[row][col].ct;
            }
        }
    }

    private void SaveRegretChess()
    {
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                chessman_regret[row][col].ct = chessman[row][col].ct;
            }
        }
    }
    private void RegretChess()
    {
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                chessman[row][col].ct = chessman_regret[row][col].ct;
            }
        }
        bwAlgorithm.UpdateChessman(chessman);
        DrawChessAgain();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        DrawAllChessman(canvas);
    }

    private void DrawAllChessman(Canvas canvas)
    {
        DrawChessText(canvas);
        DrawChessGrid(canvas);
        DrawChessman(canvas);
        DrawBackButton(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (Current_Player == CURRENT_PLAYER.COMPUTER)
            {
                //prompt a note.
                return true;
            }
            if ((bwAlgorithm.CalculateChessCount(People_Role) == 0) || (!bwAlgorithm.IfCanPutChessForPlayer(People_Role)))
            {
                //prompt a note
                ClearOrange();
                DrawChessAgain();
                return true;
            }
            StartGameTimer();
            int x = Math.round(event.getX());
            int y = Math.round(event.getY());
            PositionResult userchess = GetChessRowCol(x,y);
            if (userchess.col != -1 && userchess.row != -1)
            {
                if (bwAlgorithm.CanPutChessInPosition(userchess.row,userchess.col,People_Role))//user can put chess for current grid
                {
                    PeopleTurnChessCount = bwAlgorithm.GetTurnChessResult(People_Role,userchess.col,userchess.row);
                    if (PeopleTurnChessCount > 0)
                    {
                        Current_Player = CURRENT_PLAYER.COMPUTER;
                        if (bwAlgorithm.CalculateChessCount(Computer_Role) == 0)
                        {
                            //prompt a note.
                            DrawChessAgain();
                            return true;
                        }
                        if (!bwAlgorithm.IfCanPutChessForPlayer(Computer_Role))
                        {
                            //prompt a note.
                            Current_Player = CURRENT_PLAYER.PEOPLE;
                            DrawChessAgain();
                            return true;
                        }
                        InitComputerRun();
                        soundPool.play(1,3, 3, 0, 0, 1);
                    }
                }
            }
            else if (IsBackButtonArea(x,y) && bCanDoRegrect)
            {
                //press back button.
                RegretChess();
            }
        }
        DrawChessAgain();
        return true;
    }

    private void flashchess(int row,int col,boolean orange)
    {
        chessman[row][col].Orange = orange;
        this.postInvalidate();
    }

    private void InitHandler()
    {
        handler_analyze = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                timer_analyze.cancel();
                ComputerRun();
            }
        };
        ///////////////////
        handler_flashchess = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == FLASHNUM)
                {
                    flashnum = 0;
                    timer_flashchess.cancel();
                    if (computerchess.row < 0 || computerchess.row >= BlockNum || computerchess.col < 0 || computerchess.col >= BlockNum)
                    {
                        DrawChessAgain();
                        return;
                    }
                    ClearOrange();
                    ComputerTurnChessCount = bwAlgorithm.GetTurnChessResult(Computer_Role,computerchess.col,computerchess.row);
                    DrawChessAgain();
                    if (bwAlgorithm.CalculateChessCount(People_Role) == 0)
                    {
                        //game over
                        return;
                    }
                    if (!bwAlgorithm.IfCanPutChessForPlayer(People_Role))
                    {
                        //people can't put chess,computer run again.
                        ComputerRun();
                        return;
                    }
                    Current_Player = CURRENT_PLAYER.PEOPLE;
                    /////////////////////////////
                    SaveRegretChess();
                    bCanDoRegrect = true;
                    bCanDisplayRegretButton = true;
                }
                else
                {
                    flashchess(computerchess.row,computerchess.col,(flashnum % 2) == 0 ? true:false);
                }
            }
        };
        //////////////
        handler_time = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1)
                {
                    if (second < 59)
                    {
                        second++;
                    }
                    else
                    {
                        second = 0;
                        if (minute < 59)
                        {
                            minute++;
                        }
                        else
                        {
                            second = 0;
                            minute = 0;
                        }
                    }
                    DrawChessAgain();
                }
            }
        };
    }

    private void StopGameTimer()
    {
        if (timer_game != null)
            timer_game.cancel();
    }
    private void StartGameTimer()
    {
        if (timer_game != null)
            return;
        timer_game = new Timer();
        timer_game.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Message message = new Message();
                                    message.what = 1;
                                    handler_time.sendMessage(message);
                                }
                            }
                , 2000, 1000);
    }

    private void InitComputerRun()
    {
        bCanDoRegrect = false;
        timer_analyze = new Timer();
        timer_analyze.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler_analyze.sendMessage(message);
            }
        },1000,1000);
    }
    private void ComputerRun()
    {
        flashnum = 0;
        Current_Player = CURRENT_PLAYER.COMPUTER;
        if (bwAlgorithm.IfGameOver())
        {
            return;
        }
        if (!bwAlgorithm.IfCanPutChessForPlayer(Computer_Role))
        {
            Current_Player = CURRENT_PLAYER.PEOPLE;
            return;
        }
        computerchess = bwAlgorithm.GetBestChessPlaceAfterAnalyze(Computer_Role);
        if (computerchess.result > 0)
        {
            timer_flashchess = new Timer();
            timer_flashchess.schedule(new TimerTask(){
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = ++flashnum;
                    message.arg1 = computerchess.row;
                    message.arg2 = computerchess.col;
                    handler_flashchess.sendMessage(message);
                }
            },1000,1000);
        }
        else
        {
            ClearOrange();
        }
    }
    
    private boolean IsBackButtonArea(int x,int y)
    {
        return (back_button_x < x && x < back_button_x + back_button_w) && (back_button_y < y && y < back_button_y + back_button_h);
    }
    
    private PositionResult GetChessRowCol(int x,int y)
    {
        PositionResult p = new PositionResult();
        p.col = -1;
        p.row = -1;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if ((chessman[row][col].x < x && x < chessman[row][col].x + chessman[row][col].w) && (chessman[row][col].y < y && y < chessman[row][col].y + chessman[row][col].h))
                {
                    p.result = 1;
                    p.row = row;
                    p.col = col;
                    return p;
                }
            }
        }
        return p;
    }

    private void DrawChessText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setTextSize(20);
        canvas.drawText(GetTimeDurationFormat(second,minute),text_timer_x,text_timer_y,paint);
        canvas.drawText(GetTurnedCountString(),text_turned_x,text_turned_y,paint);
        canvas.drawText(GetChessCountAll(),text_chess_data_x,text_chess_data_y,paint);
        ///debug
        String txt = "";
        if (bwAlgorithm != null && bwAlgorithm.IfGameOver())
        {
            StopGameTimer();
            txt = getResources().getString(R.string.game_over);//"Game over,";
            int black_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.BLACK);
            int white_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.WHITE);
            if (Computer_Role == Chessman.ChessmanType.BLACK)
            {
                if (black_count > white_count)
                    txt = txt + getResources().getString(R.string.computer_win);//"Computer win";
                else if(black_count < white_count)
                    txt = txt + getResources().getString(R.string.people_win);//"People win";
                else
                    txt = txt + getResources().getString(R.string.draw);//"Draw";
            }
            else//computer_role == Chessman.ChessmanType.WHITE
            {
                if (white_count > black_count)
                    txt = txt + getResources().getString(R.string.computer_win);
                else if(white_count < black_count)
                    txt = txt + getResources().getString(R.string.people_win);//"People win";
                else
                    txt = txt + getResources().getString(R.string.draw);//"Draw";
            }
        }
        else
        {
            txt = (Current_Player == CURRENT_PLAYER.COMPUTER) ? getResources().getString(R.string.computer_run) : getResources().getString(R.string.people_run);
        }
        canvas.drawText(txt,text_debug_data_x,text_debug_data_y,paint);
        //////////////////
        if (computerchess != null && computerchess.Text != null && computerchess.Text.length() > 0)
        {
            canvas.drawText(computerchess.Text,text_debug_data_x,text_debug_data_y2,paint);
        }
    }
    private void DrawBackButton(Canvas canvas)
    {
        if (bCanDisplayRegretButton)
        {
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap_back,back_button_x,back_button_y,paint);    
        }
    }
    private void DrawChessman(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setTextSize(20);

        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (chessman[row][col].Orange)
                {
                    canvas.drawBitmap(bitmap_orange,chessman[row][col].x,chessman[row][col].y,paint);
                }
                else
                {
                    if (chessman[row][col].ct != Chessman.ChessmanType.NONE)
                    {
                        switch (chessman[row][col].ct)
                        {
                            case BLACK:
                                canvas.drawBitmap(bitmap_black,chessman[row][col].x,chessman[row][col].y,paint);
                                break;
                            case WHITE:
                                canvas.drawBitmap(bitmap_white,chessman[row][col].x,chessman[row][col].y,paint);
                                break;
                        }
                    }
                }
            }
        }
    }
    private void DrawChessGrid(Canvas canvas)
    {
        int i;
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        for (i = 0;i < 18; i++)
        {
            if (i < 9)
            {
                canvas.drawLine(hx_grid, hy_grid + bh * i, hx_grid + bw * BlockNum, hy_grid + bh * i, paint);
            }
            else
            {
                canvas.drawLine(hx_grid + bw * (i - 9) , hy_grid, hx_grid + bw * (i - 9), hy_grid + bh * BlockNum, paint);
            }
        }
    }

    private String GetChessCountAll()
    {
        String result = "";
        int black_count = 0,white_count = 0;
        if (bwAlgorithm != null)
        {
            black_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.BLACK);
            white_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.WHITE);
        }
        result = getResources().getString(R.string.black_chess_num) + Integer.toString(black_count) + " "+ getResources().getString(R.string.white_chess_num) + Integer.toString(white_count);
        return result;
    }
    private String GetTurnedCountString()
    {
        String result = "";
        if (Current_Player == CURRENT_PLAYER.COMPUTER)
        {
            result = getResources().getString(R.string.people_turn) + Integer.toString(PeopleTurnChessCount) + getResources().getString(R.string.chess);
        }
        else
        {
            result = getResources().getString(R.string.computer_turn) + Integer.toString(ComputerTurnChessCount) + getResources().getString(R.string.chess);
        }
        return result;
    }
    private String GetTimeDurationFormat(int sec,int min)
    {
        String result = "";
        if (min > 9)
        {
            result = Integer.toString(min);
        }
        else
        {
            result = "0" + Integer.toString(min);
        }
        result = result + ":";
        if (sec > 9)
        {
            result = result + Integer.toString(sec);
        }
        else
        {
            result = result + "0" + Integer.toString(sec);
        }
        return result;
    }

    private void ClearOrange()
    {
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                chessman[row][col].Orange = false;
            }
        }
    }

    private int second = 0;
    private int minute = 0;
    private final int FLASHNUM = 4;
    private final int BlockNum = 8;
    private final int hx_grid = 20;
    private final int hy_grid = 140;
    private final int text_timer_x = 20;
    private final int text_timer_y = 40;
    private final int text_turned_x = 80;
    private final int text_turned_y= 40;
    private final int text_chess_data_x = 20;
    private final int text_chess_data_y = 70;
    private final int text_debug_data_x = 20;
    private final int text_debug_data_y = 100;
    private final int text_debug_data_y2 = 600;
    private final int back_button_x = 40;
    private final int back_button_y = 40;
    private int back_button_w = 0;
    private int back_button_h = 0;
    private int bh = 0;
    private int bw = bh;
    private int flashnum = 0;
    private int ComputerTurnChessCount = 0;
    private int PeopleTurnChessCount = 0;
    private boolean bCanDisplayRegretButton = false;
    private boolean bCanDoRegrect = false;
    private Handler handler_flashchess;
    private Handler handler_time;
    private Handler handler_analyze;
    private Timer timer_flashchess;
    private Timer timer_game;
    private Timer timer_analyze;
    private PositionResult computerchess = new PositionResult();
    private Bitmap bitmap_black = null;
    private Bitmap bitmap_white = null;
    private Bitmap bitmap_orange = null;
    private Bitmap bitmap_back = null;
    private Chessman[][] chessman = null;
    private Chessman[][] chessman_regret = null;
    private BlackWhiteAlgorithm bwAlgorithm;
    public enum CURRENT_PLAYER  {PEOPLE,COMPUTER};
    private CURRENT_PLAYER Current_Player = CURRENT_PLAYER.PEOPLE;
    private Chessman.ChessmanType Computer_Role = Chessman.ChessmanType.NONE;
    private Chessman.ChessmanType People_Role = Chessman.ChessmanType.NONE;
    private SoundPool soundPool;
}
