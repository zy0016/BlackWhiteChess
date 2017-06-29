package com.example.zy.myapplication1;

/**
 * Created by Administrator on 2017/6/23.
 */

public class BlackWhiteAlgorithm {
    private int BlockNum = 0;
    private int BlockCount = 0;
    private Chessman[][] sc = null;
    private CheckWeightPosition[] CWP = null;
    private final int DIRECTION_UP = 0x01;
    private final int DIRECTION_DOWN = 0x02;
    private final int DIRECTION_LEFT = 0x04;
    private final int DIRECTION_RIGHT = 0x08;
    private final int INVALIDVALUE = -1;
    private final int WEIGHT_MAX = 100;
    private final int WEIGHT_2 = -5;
    public enum DIRECTION {ROW,COL};
    private final int[][] ChessPositionWeight =
            {
                    {100,	-5,		10,		5,		5,		10,		-5,		100},
                    { -5,	-45,	1,		1,		1,		1,		-45,	-5},
                    { 10,	1,		3,		2,		2,		3,		1,		10},
                    { 5,	1,		2,		1,		1,		2,		1,		5},
                    { 5,	1,		2,		1,		1,		2,		1,		5},
                    { 10,	1,		3,		2,		2,		3,		1,		10},
                    { -5,	-45,	1,		1,		1,		1,		-45,	-5},
                    {100,	-5,		10,		5,		5,		10,		-5,		100}
            };
    public BlackWhiteAlgorithm(Chessman[][] chessman,int blocknum)
    {
        sc = chessman;
        BlockNum = blocknum;
        BlockCount = BlockNum * BlockNum;
        CWP = new CheckWeightPosition[BlockCount];
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc[row][col].Weight = ChessPositionWeight[row][col];
                CWP[row * BlockNum + col] = new CheckWeightPosition();
                CWP[row * BlockNum + col].row = row;
                CWP[row * BlockNum + col].col = col;
                CWP[row * BlockNum + col].BlockWeight = ChessPositionWeight[row][col];
            }
        }
        WeightPositionSort(CWP,BlockCount);
        UpdateCanTurnChessCountStatus();
    }

    public boolean CanPutChessInPosition(int row,int col,Chessman.ChessmanType chesstype)
    {
        UpdateCanTurnChessCountStatus();
        if ((row < 0 || row >= BlockNum) || (col < 0 || col >= BlockNum) || (sc[row][col].ct != Chessman.ChessmanType.NONE))
            return false;
        else
            return ((chesstype == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black > 0) || (chesstype == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White > 0));
    }

    //1:can put chess for current player
    //0:can NOT put chess for current player
    public boolean IfCanPutChessForPlayer(Chessman.ChessmanType blockstatus)
    {
        UpdateCanTurnChessCountStatus();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (blockstatus == Chessman.ChessmanType.BLACK)
                {
                    if (sc[row][col].iWinChessNum_Black != 0)
                        return true;
                }
                else if (blockstatus == Chessman.ChessmanType.WHITE)
                {
                    if (sc[row][col].iWinChessNum_White != 0)
                        return true;
                }
            }
        }
        return false;
    }
    //there are several position with same weight possibly,find the max win chess num
    //return: how many position of the num for this weight value
    public PositionResult GetPositionFromSameWeight(Chessman.ChessmanType blockstatus,int weightvalue)
    {
        PositionResult p = new PositionResult();
        int count = 0,MaxWinChessNum = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                switch (blockstatus)
                {
                    case BLACK:
                        if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == weightvalue && sc[row][col].iWinChessNum_Black >= MaxWinChessNum)
                        {
                            count++;
                            MaxWinChessNum = sc[row][col].iWinChessNum_Black;
                            p.col = col;
                            p.row = row;
                        }
                        break;
                    case WHITE:
                        if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == weightvalue && sc[row][col].iWinChessNum_White >= MaxWinChessNum)
                        {
                            count++;
                            MaxWinChessNum = sc[row][col].iWinChessNum_White;
                            p.col = col;
                            p.row = row;
                        }
                        break;
                }
            }
        }
        p.result = count;
        return p;
    }
    //1:find the best place for blockstatus
    //0:can't put cheess for current cycle.
    public PositionResult GetBestChessPlace(Chessman.ChessmanType blockstatus)
    {
        int i,row1,col1,row,col;
        PositionResult result = new PositionResult();
        UpdateCanTurnChessCountStatus();
        for (i = 0;i < BlockCount;i++)
        {
            //get coordinate of chess block
            row = CWP[i].row;
            col = CWP[i].col;
            if ((blockstatus == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black != 0) || (blockstatus == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White != 0))
            {
                result = GetPositionFromSameWeight(blockstatus,sc[row][col].Weight);
                if (result.result > 0)
                {
                    return result;
                }
            }
        }
        if (!IfCanPutChessForPlayer(blockstatus))//game over for white or black in this cycle
        {
            result.result = -1;
            return result;
        }
        else
        {
            System.exit(0);
        }
        return result;
    }

    public PositionResult StartAnalyzeChessForComputer(Chessman.ChessmanType blockstatus)
    {
        PositionResult result = new PositionResult();
        int col1,row1;
        int black_count = 0,white_count = 0;

        result = GetBestChessPlace(blockstatus);
        if (result.result == 1)//find a position
        {
            result.result = GetTurnChessResult(blockstatus,result.col,result.row);
            black_count = CalculateChessCount(Chessman.ChessmanType.BLACK);
            white_count = CalculateChessCount(Chessman.ChessmanType.WHITE);
        }
        else//can't put chess for current cycle, current user should skip this cycle.
        {
        }
        return result;
    }
    private int UpdateCanTurnChessCountStatus()
    {
        UpdateCWPWeightStatus();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc[row][col].iWinChessNum_Black = GetCanTurnChessNumAll(Chessman.ChessmanType.BLACK,col,row);
                sc[row][col].iWinChessNum_White = GetCanTurnChessNumAll(Chessman.ChessmanType.WHITE,col,row);
            }
        }
        return 1;
    }
    private int GetCanTurnChessNumAll(Chessman.ChessmanType blockstatus,int col,int row)
    {
        int result = 0;
        int[] score = new int[BlockNum];
        for (int i = 0;i < BlockNum;i++)
        {
            score[i] = 0;
        }
        if (sc[row][col].ct != Chessman.ChessmanType.NONE)
        {
            return 0;
        }
        //put chess on grid.
        if (col == 0 && row == 0)//left top corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
        }
        else if (col == BlockNum - 1 && row == 0)//right top corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
        }
        else if (col == 0 && row == BlockNum - 1)//left bottom corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
        }
        else if (col == BlockNum - 1 && row == BlockNum - 1)//right bottom corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
        }
        else if (col > 0 && col < BlockNum - 1 && row == 0)//top line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
        }
        else if (col > 0 && col < BlockNum - 1 && row == BlockNum - 1)//bottom line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
        }
        else if (col == 0 && row > 0 && row < BlockNum - 1)//left line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_UP|DIRECTION_RIGHT);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_DOWN|DIRECTION_RIGHT);
        }
        else if (col == BlockNum - 1 && row > 0 && row < BlockNum - 1)//right line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
        }
        else
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[5] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            score[6] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
            score[7] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
        }
        result = ScoreSummary(score,BlockNum);
        return result;
    }

    public int GetTurnChessResult(Chessman.ChessmanType blockstatus,int col,int row)
    {
        int result = 0;
        int[] score = new int[BlockNum];
        int[] turnresult = new int[BlockNum];
        if (col == 0 && row == 0)//left top corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN,score[2]);
        }
        else if (col == BlockNum - 1 && row == 0)//right top corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN,score[2]);
        }
        else if (col == 0 && row == BlockNum - 1)//left bottom corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP,score[2]);
        }
        else if (col == BlockNum - 1 && row == BlockNum - 1)//right bottom corner
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP,score[2]);
        }
        else if (col > 0 && col < BlockNum - 1 && row == 0)//top line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[2]);
            turnresult[3] = TurnWinChessToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN,score[3]);
            turnresult[4] = TurnWinChessToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN,score[4]);
        }
        else if (col > 0 && col < BlockNum - 1 && row == BlockNum - 1)//bottom line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[2]);
            turnresult[3] = TurnWinChessToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP,score[3]);
            turnresult[4] = TurnWinChessToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP,score[4]);
        }
        else if (col == 0 && row > 0 && row < BlockNum - 1)//left line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_UP|DIRECTION_RIGHT);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_DOWN|DIRECTION_RIGHT);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[2]);
            turnresult[3] = TurnWinChessToDirect(blockstatus,col + 1,row - 1,DIRECTION_UP|DIRECTION_RIGHT,score[3]);
            turnresult[4] = TurnWinChessToDirect(blockstatus,col + 1,row + 1,DIRECTION_DOWN|DIRECTION_RIGHT,score[4]);
        }
        else if (col == BlockNum - 1 && row > 0 && row < BlockNum - 1)//right line
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[2]);
            turnresult[3] = TurnWinChessToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP,score[3]);
            turnresult[4] = TurnWinChessToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN,score[4]);
        }
        else
        {
            score[0] = GetCanTurnChessNumToDirect(blockstatus,col,row - 1,DIRECTION_UP);
            score[1] = GetCanTurnChessNumToDirect(blockstatus,col,row + 1,DIRECTION_DOWN);
            score[2] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row,DIRECTION_LEFT);
            score[3] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT);
            score[4] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP);
            score[5] = GetCanTurnChessNumToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN);
            score[6] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP);
            score[7] = GetCanTurnChessNumToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN);
            turnresult[0] = TurnWinChessToDirect(blockstatus,col,row - 1,DIRECTION_UP,score[0]);
            turnresult[1] = TurnWinChessToDirect(blockstatus,col,row + 1,DIRECTION_DOWN,score[1]);
            turnresult[2] = TurnWinChessToDirect(blockstatus,col - 1,row,DIRECTION_LEFT,score[2]);
            turnresult[3] = TurnWinChessToDirect(blockstatus,col + 1,row,DIRECTION_RIGHT,score[3]);
            turnresult[4] = TurnWinChessToDirect(blockstatus,col - 1,row - 1,DIRECTION_LEFT|DIRECTION_UP,score[4]);
            turnresult[5] = TurnWinChessToDirect(blockstatus,col - 1,row + 1,DIRECTION_LEFT|DIRECTION_DOWN,score[5]);
            turnresult[6] = TurnWinChessToDirect(blockstatus,col + 1,row - 1,DIRECTION_RIGHT|DIRECTION_UP,score[6]);
            turnresult[7] = TurnWinChessToDirect(blockstatus,col + 1,row + 1,DIRECTION_RIGHT|DIRECTION_DOWN,score[7]);
        }
        sc[row][col].ct = blockstatus;//update current position
        if (FindInvalidValue(turnresult,BlockNum) == 1)
        {
            //OutputChessStatus();
            return -1;
        }
        UpdateCanTurnChessCountStatus();
        return ScoreSummary(score,BlockNum);
    }
    private int TurnWinChessToDirect(Chessman.ChessmanType blockstatus,int col,int row,int direct,int num)
    {
        int stepcol = 0,steprow = 0;
        if ((direct & DIRECTION_LEFT) != 0)
        {
            stepcol = -1;
        }
        else if ((direct & DIRECTION_RIGHT) != 0)
        {
            stepcol = 1;
        }
        if ((direct & DIRECTION_UP) != 0)
        {
            steprow = -1;
        }
        else if ((direct & DIRECTION_DOWN) != 0)
        {
            steprow = 1;
        }
        for (int i = 0;i < num;i++)
        {
            if (col < 0 || col >= BlockNum || row < 0 || row >= BlockNum)
            {
                break;
            }
            if (sc[row][col].ct == Chessman.ChessmanType.NONE || sc[row][col].ct == blockstatus)
            {
                return INVALIDVALUE;
            }
            sc[row][col].ct = blockstatus;
            col += stepcol;
            row += steprow;
        }
        return 1;
    }

    private int GetCanTurnChessNumToDirect(Chessman.ChessmanType blockstatus,int col,int row,int direct)
    {
        int result = 0,bfind = 0,stepcol = 0,steprow = 0;
        Chessman.ChessmanType checkedblock = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;
        if ((direct & DIRECTION_LEFT) != 0)
        {
            stepcol = -1;
        }
        else if ((direct & DIRECTION_RIGHT) != 0)
        {
            stepcol = 1;
        }
        if ((direct & DIRECTION_UP) != 0)
        {
            steprow = -1;
        }
        else if ((direct & DIRECTION_DOWN) != 0)
        {
            steprow = 1;
        }
        while(sc[row][col].ct == checkedblock)
        {
            if ((col + stepcol >= 0 && col + stepcol < BlockNum) && (row + steprow >= 0 && row + steprow < BlockNum))
            {
                result++;
                col += stepcol;
                row += steprow;
            }
            else
            {
                break;
            }
        }
        /*while ((col >= 0 && col < BlockNum - 1) && (row >= 0 && row < BlockNum - 1) && (sc[row][col].ct == checkedblock))
        {
            result++;
            col += stepcol;
            row += steprow;
        }*/
        if ((col >= 0 && col < BlockNum) && (row >= 0 && row < BlockNum) && (sc[row][col].ct == blockstatus))
        {
            bfind = 1;
        }
        return (bfind == 1) ? result : 0;
    }

    public boolean IfGameOver()
    {
        UpdateCanTurnChessCountStatus();
        if (CalculateChessCount(Chessman.ChessmanType.BLACK) == 0 || CalculateChessCount(Chessman.ChessmanType.WHITE) == 0)
            return true;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].ct == Chessman.ChessmanType.NONE && (sc[row][col].iWinChessNum_White > 0 || sc[row][col].iWinChessNum_Black > 0))
                {
                    return false;
                }
            }
        }
        return true;
    }
    public int CalculateChessCount(Chessman.ChessmanType blockstatus)
    {
        int result = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].ct == blockstatus)
                {
                    result++;
                }
            }
        }
        return result;
    }
    private int EmptyChessPositionCount(int col,int row,DIRECTION direct)
    {
        int iRemainChess = 0;
        if (direct == DIRECTION.COL)
        {
            for (int i = 0;i < BlockNum;i++)
            {
                if (sc[i][col].ct == Chessman.ChessmanType.NONE)
                    iRemainChess++;
            }
        }
        else//ROW
        {
            for (int i = 0;i < BlockNum;i++)
            {
                if (sc[row][i].ct == Chessman.ChessmanType.NONE)
                    iRemainChess++;
            }
        }
        return iRemainChess;
    }

    private int UpdateCWPWeightStatus()
    {
        int UpdateWeight = 0;
        int iRemainChessInRow = 0,col,row;
        for (int i = 0;i < BlockCount;i++)
        {
            row = CWP[i].row;
            col = CWP[i].col;
            if (sc[row][col].ct == Chessman.ChessmanType.NONE && CWP[i].BlockWeight == WEIGHT_2)
            {
                if (((row == 0 || row == BlockNum - 1) && (EmptyChessPositionCount(col,row,DIRECTION.ROW) == 1)) ||
                        ((col == 0 || col == BlockNum - 1) && (EmptyChessPositionCount(col,row,DIRECTION.COL) == 1)))
                {
                    CWP[i].BlockWeight = WEIGHT_MAX;
                    sc[row][col].Weight = WEIGHT_MAX;
                    UpdateWeight = 1;
                }
            }
        }
        if (UpdateWeight == 1)
        {
            WeightPositionSort(CWP,BlockCount);
        }
        return UpdateWeight;
    }
    private int FindInvalidValue(int value[],int size)
    {
        for (int i = 0;i < size;i++)
        {
            if (value[i] == INVALIDVALUE)
            {
                return 1;
            }
        }
        return 0;
    }
    private int ScoreSummary(int score[],int size)
    {
        int result = 0;
        for (int i = 0;i < size;i++)
        {
            result += score[i];
        }
        return result;
    }
    private void WeightPositionSort(CheckWeightPosition arraycwp[],int nNum)
    {
        CheckWeightPosition temp = new CheckWeightPosition();
        int i,j;
        for (i = 1; i < nNum;i++)
        {
            if (arraycwp[i].BlockWeight > arraycwp[i - 1].BlockWeight)
            {
                temp.row = arraycwp[i].row;
                temp.col = arraycwp[i].col;
                temp.BlockWeight = arraycwp[i].BlockWeight;

                arraycwp[i].row = arraycwp[i - 1].row;
                arraycwp[i].col = arraycwp[i - 1].col;
                arraycwp[i].BlockWeight = arraycwp[i - 1].BlockWeight;

                for (j = i - 1;j >= 0 && temp.BlockWeight > arraycwp[j].BlockWeight; --j)
                {
                    arraycwp[j + 1].row = arraycwp[j].row;
                    arraycwp[j + 1].col = arraycwp[j].col;
                    arraycwp[j + 1].BlockWeight = arraycwp[j].BlockWeight;
                }
                arraycwp[j + 1].row = temp.row;
                arraycwp[j + 1].col = temp.col;
                arraycwp[j + 1].BlockWeight = temp.BlockWeight;
            }
        }
        /*for (i = 1; i < nNum; ++i)
        {
            if (arraycwp[i].BlockWeight > arraycwp[i - 1].BlockWeight)
            {
                memcpy(&temp,&arraycwp[i],sizeof(CheckWeightPositon));
                memcpy(&arraycwp[i],&arraycwp[i - 1],sizeof(CheckWeightPositon));
                for (j = i - 1;j >= 0 && temp.BlockWeight > arraycwp[j].BlockWeight; --j)
                {
                    memcpy(&arraycwp[j + 1],&arraycwp[j],sizeof(CheckWeightPositon));
                }
                memcpy(&arraycwp[j + 1],&temp,sizeof(CheckWeightPositon));
            }
        }*/
    }
}
