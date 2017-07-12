package com.example.zy.myapplication1;

/**
 * Created by Administrator on 2017/6/23.
 */

public class Chessman {
    public int x;
    public int y;
    public int w;
    public int h;
    public enum ChessmanType
    {
        NONE,
        BLACK,
        WHITE,
    }
    public boolean Orange;
    ChessmanType ct;
    /////////////////////////
    int Weight;
    int iWinChessNum_Black;
    int iWinChessNum_White;

    public Chessman()
    {
        ct = ChessmanType.NONE;
        Orange = false;
    }
}
