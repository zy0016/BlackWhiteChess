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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/23.
 */

public class GameView extends View {
    public GameView(Context context,Chessman.ChessmanType people_role,Chessman.ChessmanType program_role,CURRENT_PLAYER current_player)
    {
        super(context,null);

        Computer_Role = program_role;
        People_Role = people_role;
        Current_Player = current_player;
        second = 0;
        minute = 0;
        ComputerTurnChessCount = 0;
        PeopleTurnChessCount = 0;
        InitHandler();
        initGameView(context);
        InitChessGrid();
        bwAlgorithm = new BlackWhiteAlgorithm(chessman,BlockNum);
        if (Current_Player == CURRENT_PLAYER.COMPUTER)
        {
            ComputerRun();
        }
    }

    private void DrawChessAgain()
    {
        this.postInvalidate();
    }

    private void initGameView(Context context)
    {
        bitmap_black = BitmapFactory.decodeResource(getResources(),R.drawable.chess_black);
        bitmap_white = BitmapFactory.decodeResource(getResources(),R.drawable.chess_white);
        bitmap_orange = BitmapFactory.decodeResource(getResources(),R.drawable.chess_orange);
        bh = bitmap_black.getHeight();
        bw = bitmap_black.getWidth();
    }
    private void InitChessGrid()
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
        chessman[BlockNum / 2 - 1][BlockNum / 2 - 1].ct = Chessman.ChessmanType.BLACK;
        chessman[BlockNum / 2 - 1][BlockNum / 2].ct = Chessman.ChessmanType.WHITE;
        chessman[BlockNum / 2][BlockNum / 2 - 1].ct = Chessman.ChessmanType.WHITE;
        chessman[BlockNum / 2][BlockNum  /2].ct = Chessman.ChessmanType.BLACK;
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
            int x = Math.round(event.getX());
            int y = Math.round(event.getY());
            userchess = GetChessRowCol(x,y);
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
                        ComputerRun();
                    }
                }
            }
        }
        /*else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if (Current_Player == CURRENT_PLAYER.COMPUTER)
            {
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
                ComputerRun();
            }
        }*/
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
        handler_computer = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == FLASHNUM)
                {
                    flashnum = 0;
                    timer_computer.cancel();
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
                }
                else
                {
                    flashchess(computerchess.row,computerchess.col,(flashnum % 2) == 0 ? true:false);
                }
            }
        };
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
        if (computerchess.Text != null && computerchess.Text.length() > 0)
        {
            debugtext = computerchess.Text;
        }
        if (computerchess.result > 0)
        {
            timer_computer = new Timer();
            timer_computer.schedule(new TimerTask(){
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = ++flashnum;
                    message.arg1 = computerchess.row;
                    message.arg2 = computerchess.col;
                    handler_computer.sendMessage(message);
                }
            },1000,1000);
        }
        else
        {
            ClearOrange();
        }
    }
    private PositionResult GetChessRowCol(int x,int y)
    {
        PositionResult p = new PositionResult();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if ((chessman[row][col].x < x && x < chessman[row][col].x + chessman[row][col].w) && (chessman[row][col].y < y && y < chessman[row][col].y + chessman[row][col].h))
                {
                    p.result = 1;
                    p.row = row;
                    p.col = col;
                    break;
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
        if (bwAlgorithm.IfGameOver())
        {
            txt = "Game over,";
            int black_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.BLACK);
            int white_count = bwAlgorithm.CalculateChessCount(Chessman.ChessmanType.WHITE);
            if (Computer_Role == Chessman.ChessmanType.BLACK)
            {
                if (black_count > white_count)
                    txt = txt + "Computer win";
                else if(black_count < white_count)
                    txt = txt + "People win";
                else
                    txt = txt + "Draw";
            }
            else//computer_role == Chessman.ChessmanType.WHITE
            {
                if (white_count > black_count)
                    txt = txt + "Computer win";
                else if(white_count < black_count)
                    txt = txt + "People win";
                else
                    txt = txt + "Draw";
            }
        }
        else
        {
            txt = (Current_Player == CURRENT_PLAYER.COMPUTER) ? "Computer is playing" + debugtext: "People is playing";
        }
        canvas.drawText(txt,text_debug_data_x,text_debug_data_y,paint);
    }
    private void DrawChessman(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setTextSize(20);
        //CheckWeightPositon[] cwp = bwAlgorithm.GetCWP();
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
                    /*int row_cwp = cwp[row * BlockNum + col].row;
                    int col_cwp = cwp[row * BlockNum + col].col;
                    int weight = cwp[row * BlockNum + col].BlockWeight;
                    String res = Integer.toString(row_cwp) + ":" + Integer.toString(col_cwp);
                    canvas.drawText(res,chessman[row][col].x,chessman[row][col].y + 15,paint);
                    canvas.drawText(Integer.toString(weight),chessman[row][col].x,chessman[row][col].y + 30,paint);*/
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
        result = "Black chess:" + Integer.toString(black_count) + ".White chess:" + Integer.toString(white_count);
        return result;
    }
    private String GetTurnedCountString()
    {
        String result = "";
        if (Current_Player == CURRENT_PLAYER.COMPUTER)
        {
            result = "People turn " + Integer.toString(PeopleTurnChessCount) + " chess.";
        }
        else
        {
            result = "Computer turn " + Integer.toString(ComputerTurnChessCount) + " chess.";
        }
        return result;
    }
    private String GetTimeDurationFormat(int sec,int min)
    {
        String result = "none";
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
    private final int FLASHNUM = 6;
    private final int BlockNum = 8;
    private final int BlockCount = (BlockNum * BlockNum);
    private final int hx_grid = 20;
    private final int hy_grid = 120;
    private final int text_timer_x = 20;
    private final int text_timer_y = 20;
    private final int text_turned_x = 80;
    private final int text_turned_y= 20;
    private final int text_chess_data_x = 20;
    private final int text_chess_data_y = 50;
    private final int text_debug_data_x = 20;
    private final int text_debug_data_y = 80;
    private String debugtext = "";
    private int bh = 0;
    private int bw = bh;
    private int flashnum = 0;
    private int ComputerTurnChessCount = 0;
    private int PeopleTurnChessCount = 0;
    private Handler handler_computer;
    private Timer timer_computer;
    private PositionResult userchess = new PositionResult();
    private PositionResult computerchess = new PositionResult();
    private Bitmap bitmap_black = null;
    private Bitmap bitmap_white = null;
    private Bitmap bitmap_orange = null;
    private Chessman[][] chessman = null;
    private BlackWhiteAlgorithm bwAlgorithm;
    public enum CURRENT_PLAYER  {PEOPLE,COMPUTER};
    private CURRENT_PLAYER Current_Player = CURRENT_PLAYER.PEOPLE;
    private Chessman.ChessmanType Computer_Role = Chessman.ChessmanType.NONE;
    private Chessman.ChessmanType People_Role = Chessman.ChessmanType.NONE;
}
