package com.example.zy.myapplication1;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/23.
 */

public class BlackWhiteAlgorithm {
    private int BlockNum = 0;
    private int BlockCount = 0;
    private Chessman[][] sc = null;
    private Chessman[][] sc_backup = null;
    private CheckWeightPosition[] CWP = null;
    private CheckWeightPosition[] CWP_backup = null;
    private final int DIRECTION_UP = 0x01;
    private final int DIRECTION_DOWN = 0x02;
    private final int DIRECTION_LEFT = 0x04;
    private final int DIRECTION_RIGHT = 0x08;
    private final int INVALIDVALUE = -1;
    private final int WEIGHT_MAX = 100;
    private final int WEIGHT_LEVEL1 = 10;
    private final int WEIGHT_LEVEL2 = 5;
    private final int WEIGHT_LEVEL3 = 3;
    private final int WEIGHT_LEVEL4 = 2;
    private final int WEIGHT_LEVEL5 = 1;
    private final int WEIGHT_LEVEL6 = -5;
    private final int WEIGHT_LEVEL7 = -45;
    private final int WEIGHT_LEVEL8 = 50;
    public enum DIRECTION {ROW,COL};
    private final int[][] ChessPositionWeight =
            {
                    {100,	-5,		10,		10,		10,		10,		-5,		100},
                    { -5,	-45,	1,		1,		1,		1,		-45,	-5},
                    { 10,	1,		3,		2,		2,		3,		1,		10},
                    { 10,	1,		2,		1,		1,		2,		1,		10},
                    { 10,	1,		2,		1,		1,		2,		1,		10},
                    { 10,	1,		3,		2,		2,		3,		1,		10},
                    { -5,	-45,	1,		1,		1,		1,		-45,	-5},
                    {100,	-5,		10,		10,		10,		10,		-5,		100}
            };
    public BlackWhiteAlgorithm(Chessman[][] chessman,int blocknum)
    {
        sc = chessman;
        BlockNum = blocknum;
        BlockCount = BlockNum * BlockNum;
        CWP = new CheckWeightPosition[BlockCount];
        CWP_backup = new CheckWeightPosition[BlockCount];
        sc_backup = new Chessman[BlockNum][BlockNum];
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc[row][col].Weight = ChessPositionWeight[row][col];
                CWP[row * BlockNum + col] = new CheckWeightPosition();
                CWP[row * BlockNum + col].row = row;
                CWP[row * BlockNum + col].col = col;
                CWP[row * BlockNum + col].BlockWeight = ChessPositionWeight[row][col];
                ///////////////////////////
                sc_backup[row][col] = new Chessman();
                sc_backup[row][col].Weight = sc[row][col].Weight;
                sc_backup[row][col].ct = sc[row][col].ct;
                ///////////////////////////
                CWP_backup[row * BlockNum + col] = new CheckWeightPosition();
                CWP_backup[row * BlockNum + col].row = row;
                CWP_backup[row * BlockNum + col].col = col;
                CWP_backup[row * BlockNum + col].BlockWeight = ChessPositionWeight[row][col];
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
    public PositionResult GetPositionFromSameWeight(Chessman.ChessmanType blockstatus,int weightvalue,boolean bCheckForEnemy)
    {
        PositionResult p = new PositionResult();
        int count = 0,MaxWinChessNum = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == weightvalue && GetWinChessNum(blockstatus,col,row) > MaxWinChessNum)
                {
                    if (bCheckForEnemy)
                    {
                        if (!IfEnemyGetNewMaxWeightPosition(blockstatus,col,row))
                        {
                            count++;
                            MaxWinChessNum = GetWinChessNum(blockstatus,col,row);
                            p.col = col;
                            p.row = row;
                        }
                    }
                    else
                    {
                        count++;
                        MaxWinChessNum = GetWinChessNum(blockstatus,col,row);
                        p.col = col;
                        p.row = row;
                    }
                }
            }
        }
        p.result = (count > 0) ? MaxWinChessNum : -1;
        return p;
    }

    public PositionResult GetBestChessPlaceAfterAnalyze(Chessman.ChessmanType blockstatus)
    {
        PositionResult result_weight = AnalyzeWeightBalance(blockstatus);
        int row = result_weight.row;
        int col = result_weight.col;

        UpdateCWPWeightStatusForLevel1(blockstatus);
        UpdateCWPWeightStatusForLevel6(blockstatus);
        if (sc[row][col].Weight == WEIGHT_MAX)
        {
            result_weight.Text = "Find WEIGHT_MAX";
            return result_weight;
        }
        PositionResult result_edge = AnalyzeEdgeStatus(blockstatus);
        if (result_edge.result != -1)
        {
            result_edge.Text = "AnalyzeEdgeStatus";
            return result_edge;
        }
        if (sc[row][col].Weight == WEIGHT_LEVEL1 || sc[row][col].Weight == WEIGHT_LEVEL2)
        {
            if (GetBetterPositionForWeight_Level12(blockstatus,col,row))
            {
                return result_weight;
            }
            /*if (IfEnemyGetNewMaxWeightPosition(blockstatus,result_weight.col,result_weight.row))
            {
                PositionResult result_alphabeta_improve = GetBetterPositionWithoutMaxWeightForEnemy(blockstatus,result_weight.col,result_weight.row);
                result_alphabeta_improve.Text = "S1 Old Row:" + Integer.toString(result_weight.row) + ",Col:" + Integer.toString(result_weight.col) +
                        " R:" + Integer.toString(result_weight.result) + " W:" + Integer.toString(sc[result_weight.row][result_weight.row].Weight) +
                        " New Row:" + Integer.toString(result_alphabeta_improve.row) + ",Col:" + Integer.toString(result_alphabeta_improve.col) +
                        " R:" + Integer.toString(result_alphabeta_improve.result) + " W:" + Integer.toString(sc[result_alphabeta_improve.row][result_alphabeta_improve.col].Weight);
                return result_alphabeta_improve;
            }
            else
            {
                return result_weight;
            }*/
        }
        PositionResult result_alphabeta = AnalyzeAlphaBeta(blockstatus);
        if (result_alphabeta.result > 0 &&
                sc[result_alphabeta.row][result_alphabeta.col].Weight == WEIGHT_LEVEL3 ||
                sc[result_alphabeta.row][result_alphabeta.col].Weight == WEIGHT_LEVEL4 ||
                sc[result_alphabeta.row][result_alphabeta.col].Weight == WEIGHT_LEVEL5)
        {
            if (IfEnemyGetNewMaxWeightPosition(blockstatus,result_alphabeta.col,result_alphabeta.row))
            {
                PositionResult result_alphabeta_improve = GetBetterPositionWithoutMaxWeightForEnemy(blockstatus,result_alphabeta.col,result_alphabeta.row);
                result_alphabeta_improve.Text = "S2 Old Row:" + Integer.toString(result_alphabeta.row) + ",Col:" + Integer.toString(result_alphabeta.col) +
                        " R:" + Integer.toString(result_alphabeta.result) + " W:" + Integer.toString(sc[result_alphabeta.row][result_alphabeta.row].Weight) +
                        " New Row:" + Integer.toString(result_alphabeta_improve.row) + ",Col:" + Integer.toString(result_alphabeta_improve.col) +
                        " R:" + Integer.toString(result_alphabeta_improve.result) + " W:" + Integer.toString(sc[result_alphabeta_improve.row][result_alphabeta_improve.col].Weight);
                return result_alphabeta_improve;
            }
            else
            {
                result_alphabeta.Text = "Use AnalyzeAlphaBeta";
                return result_alphabeta;
            }
        }
        if (sc[result_weight.row][result_weight.col].Weight == WEIGHT_LEVEL6||sc[result_weight.row][result_weight.col].Weight == WEIGHT_LEVEL7)
        {
            PositionResult result_weight67 = GetBetterPositionForWeight_Level67(blockstatus);
            if (result_weight67.result > 0)
            {
                result_weight67.Text = "S3 old row:" + Integer.toString(result_weight.row) + ",col:" + Integer.toString(result_weight.col) +
                        " r:" + Integer.toString(result_weight.result) + " w:" + Integer.toString(sc[result_weight.row][result_weight.row].Weight) +
                        " new row:" + Integer.toString(result_weight67.row) + ",col:" + Integer.toString(result_weight67.col) +
                        " r:" + Integer.toString(result_weight67.result) + " w:" + Integer.toString(sc[result_weight67.row][result_weight67.col].Weight);
                return result_weight67;
            }
        }
        result_weight.Text = "Use AnalyzeWeightBalance";
        return result_weight;
    }

    private PositionResult GetBetterPositionWithoutMaxWeightForEnemy(Chessman.ChessmanType blockstatus,int col_ori,int row_ori)
    {
        ArrayList<PositionResult> resultlist = new ArrayList<PositionResult>();
        boolean bfindposition = false;
        PositionResult result = new PositionResult();
        result.row = row_ori;
        result.col = col_ori;
        result.result = GetWinChessNum(blockstatus,col_ori,row_ori);
        int iWinChessNum = 0;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;

        BackupChessman();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                RestoreChessman();
                iWinChessNum = GetWinChessNum(blockstatus,col,row);
                if (sc[row][col].ct == Chessman.ChessmanType.NONE && iWinChessNum > 0)
                {
                    if (GetTurnChessResult(blockstatus,col,row) > 0)
                    {
                        PositionResult result_people = AnalyzeWeightBalance(enemychess);
                        if (result_people.result > 0 && sc[result_people.row][result_people.col].Weight != WEIGHT_MAX)
                        {
                            result.result = iWinChessNum;
                            result.row = row;
                            result.col = col;
                            bfindposition = true;
                            resultlist.add(result);
                        }
                    }
                }
            }
        }
        RestoreChessman();
        if (bfindposition)
        {
            for (int i = 0;i < resultlist.size();i++)
            {
                if (resultlist.get(i).result > result.result)
                {
                    result.result = resultlist.get(i).result;
                    result.row = resultlist.get(i).row;
                    result.col = resultlist.get(i).col;
                }
            }
        }
        return result;
    }

    private boolean IfEnemyGetNewMaxWeightPosition(Chessman.ChessmanType blockstatus,int col,int row)
    {
        int  iEnemyHasMaxWeightPositionsOld = 0,iEnemyHasMaxWeightPositionsNew = 0;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;

        iEnemyHasMaxWeightPositionsOld = GetMaxWeightPositionCount(enemychess);
        if (CanPutChessInPosition(row,col,blockstatus))
        {
            BackupChessman();
            if (GetTurnChessResult(blockstatus, col, row) > 0)
            {
                iEnemyHasMaxWeightPositionsNew = GetMaxWeightPositionCount(enemychess);
            }
            RestoreChessman();
        }
        return (iEnemyHasMaxWeightPositionsNew > iEnemyHasMaxWeightPositionsOld);
    }
    private boolean GetBetterPositionForWeight_Level12(Chessman.ChessmanType blockstatus,int col,int row)
    {
        //PositionResult result_weight = new PositionResult();
        boolean bputchess = true;
        if (row == 0)
        {
            if (sc[row][0].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col + 1,row,DIRECTION_RIGHT) ||
               (sc[row][BlockNum - 1].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col - 1,row,DIRECTION_LEFT)))
            {
                bputchess = false;
            }
        }
        else if (row == BlockNum - 1)
        {
            if (sc[row][0].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col + 1,row,DIRECTION_RIGHT) ||
               (sc[row][BlockNum - 1].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col - 1,row,DIRECTION_LEFT)))
            {
                bputchess = false;
            }
        }
        if (col == 0)
        {
            if ((sc[0][col].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col,row + 1,DIRECTION_DOWN)) ||
                (sc[BlockNum - 1][col].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col,row - 1,DIRECTION_UP)))
            {
                bputchess = false;
            }
        }
        else if (col == BlockNum - 1)
        {
            if ((sc[0][col].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col,row + 1,DIRECTION_DOWN)) ||
                (sc[BlockNum - 1][col].ct == Chessman.ChessmanType.NONE && HaveEnemyChessInDirect(blockstatus,col,row - 1,DIRECTION_UP)))
            {
                bputchess = false;
            }
        }
        /*if (bputchess)
        {
            result_weight.col = col;
            result_weight.row = row;
            result_weight.result = sc[row][col].Weight;
            result_weight.Text = "Find WEIGHT_LEVEL1";
        }
        else
        {
            //result_weight = AnalyzeWeightBalance(blockstatus,col,row);
            //result_weight.Text = "Except row:" + Integer.toString(row) + " col:" + Integer.toString(col);
            result_weight.result = -1;
        }*/
        return bputchess;
    }

    private PositionResult GetBetterPositionForWeight_Level67(Chessman.ChessmanType blockstatus)
    {
        ArrayList<PositionResult> resultlist = new ArrayList<PositionResult>();
        boolean bfindposition = false;
        PositionResult result = new PositionResult();
        int iWinChessNum = 0;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;

        BackupChessman();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                RestoreChessman();
                if (((row == 0 || row == BlockNum - 1) && (col == 1 || col == BlockNum - 2)) ||
                     ((col == 0 || col == BlockNum - 1) && (row == 1 || row == BlockNum - 2)) ||
                        ((row == 1 && col == 1) ||
                         (row == 1 && col == BlockNum - 2) ||
                         (row == BlockNum - 2 && col == 1) ||
                         (row == BlockNum - 2 && col == BlockNum - 2))
                        )
                {
                    iWinChessNum = GetWinChessNum(blockstatus,col,row);
                    if (sc[row][col].ct == Chessman.ChessmanType.NONE && iWinChessNum > 0)
                    {
                        if (GetTurnChessResult(blockstatus,col,row) > 0)
                        {
                            PositionResult result_people = AnalyzeWeightBalance(enemychess);
                            if (result_people.result > 0 && sc[result_people.row][result_people.col].Weight != WEIGHT_MAX)
                            {
                                result.result = iWinChessNum;
                                result.row = row;
                                result.col = col;
                                bfindposition = true;
                                resultlist.add(result);
                            }
                        }
                    }
                }
            }
        }
        RestoreChessman();
        if (bfindposition)
        {
            for (int i = 0;i < resultlist.size();i++)
            {
                if (resultlist.get(i).result > result.result)
                {
                    result.result = resultlist.get(i).result;
                    result.row = resultlist.get(i).row;
                    result.col = resultlist.get(i).col;
                }
            }
        }
        return result;
    }

    private boolean HaveEnemyChessInDirect(Chessman.ChessmanType mychess,int col,int row,int direct)
    {
        Chessman.ChessmanType enemychess = (mychess == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;
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
        while(sc[row][col].ct != mychess)
        {
            if ((col + stepcol >= 0 && col + stepcol < BlockNum) && (row + steprow >= 0 && row + steprow < BlockNum))
            {
                col += stepcol;
                row += steprow;
            }
            else
            {
                break;
            }
        }
        return ((col >= 0 && col < BlockNum) && (row >= 0 && row < BlockNum) && (sc[row][col].ct == enemychess));
    }

    public PositionResult AnalyzeEdgeStatus(Chessman.ChessmanType blockstatus)
    {
        PositionResult result = new PositionResult();
        result.result = -1;
        UpdateCanTurnChessCountStatus();
        for (int col = 1;col < BlockNum - 1;col++)
        {
            if (sc[0][col].ct == Chessman.ChessmanType.NONE)
            {
                int toplineleft = GetCanTurnChessNumToDirect(blockstatus,col - 1,0,DIRECTION_LEFT);
                int toplineright = GetCanTurnChessNumToDirect(blockstatus,col + 1,0,DIRECTION_RIGHT);
                if ((toplineleft > 0 && sc[0][0].ct == blockstatus) ||
                    (toplineright > 0 && sc[0][BlockNum - 1].ct == blockstatus) ||
                   ((toplineleft > 0 || toplineright > 0) && sc[0][0].ct == Chessman.ChessmanType.NONE && sc[0][BlockNum - 1].ct == Chessman.ChessmanType.NONE)
                    )
                {
                    result.col = col;
                    result.row = 0;
                    result.result = toplineleft + toplineright;
                    break;
                }
            }
            if (sc[BlockNum - 1][col].ct == Chessman.ChessmanType.NONE)
            {
                int bottomlinelet = GetCanTurnChessNumToDirect(blockstatus,col - 1,BlockNum - 1,DIRECTION_LEFT);
                int bottomlineright = GetCanTurnChessNumToDirect(blockstatus,col + 1,BlockNum - 1,DIRECTION_RIGHT);
                if ((bottomlinelet > 0 && sc[BlockNum - 1][0].ct == blockstatus) ||
                    (bottomlineright > 0 && sc[BlockNum - 1][BlockNum - 1].ct == blockstatus) ||
                   ((bottomlinelet > 0 || bottomlineright > 0) && sc[BlockNum - 1][0].ct == Chessman.ChessmanType.NONE && sc[BlockNum - 1][BlockNum - 1].ct == Chessman.ChessmanType.NONE)
                        )
                {
                    result.col = col;
                    result.row = BlockNum - 1;
                    result.result = bottomlinelet + bottomlineright;
                    break;
                }
            }
        }
        for (int row = 1;row < BlockNum - 1;row++)
        {
            if (sc[row][0].ct == Chessman.ChessmanType.NONE)
            {
                int leftlineup = GetCanTurnChessNumToDirect(blockstatus,0,row - 1,DIRECTION_UP);
                int leftlinedown = GetCanTurnChessNumToDirect(blockstatus,0,row + 1,DIRECTION_DOWN);
                if ((leftlineup > 0 && sc[0][0].ct == blockstatus) ||
                    (leftlinedown > 0 && sc[BlockNum - 1][0].ct == blockstatus) ||
                   ((leftlineup > 0 || leftlinedown > 0) && sc[0][0].ct == Chessman.ChessmanType.NONE && sc[BlockNum - 1][0].ct == Chessman.ChessmanType.NONE)
                        )
                {
                    result.col = 0;
                    result.row = row;
                    result.result = leftlineup + leftlinedown;
                    break;
                }
            }
            if (sc[row][BlockNum - 1].ct == Chessman.ChessmanType.NONE)
            {
                int rightlineup = GetCanTurnChessNumToDirect(blockstatus,BlockNum - 1,row - 1,DIRECTION_UP);
                int rightlinedown = GetCanTurnChessNumToDirect(blockstatus,BlockNum - 1,row + 1,DIRECTION_DOWN);
                if ((rightlineup > 0 && sc[0][BlockNum - 1].ct == blockstatus) ||
                    (rightlinedown > 0 && sc[BlockNum - 1][BlockNum - 1].ct == blockstatus) ||
                   ((rightlineup > 0 || rightlinedown > 0) && sc[0][BlockNum - 1].ct == Chessman.ChessmanType.NONE && sc[BlockNum - 1][BlockNum - 1].ct == Chessman.ChessmanType.NONE)
                        )
                {
                    result.col = BlockNum - 1;
                    result.row = row;
                    result.result = rightlineup + rightlinedown;
                    break;
                }
            }
        }
        if (IfEnemyGetNewMaxWeightPosition(blockstatus,result.col,result.row))
        {
            result.result = -1;
        }
        return result;
    }
    //1:find the best place for blockstatus
    //0:can't put chess for current cycle.
    public PositionResult AnalyzeWeightBalance(Chessman.ChessmanType blockstatus)
    {
        PositionResult result = new PositionResult();
        result.result = -1;
        UpdateCanTurnChessCountStatus();
        for (int i = 0;i < BlockCount;i++)
        {
            //get coordinate of chess block
            int row = CWP[i].row;
            int col = CWP[i].col;
            if ((blockstatus == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black != 0) ||
                (blockstatus == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White != 0))
            {
                result = GetPositionFromSameWeight(blockstatus,sc[row][col].Weight,true);
                if (result.result > 0)
                {
                    return result;
                }
            }
        }
        ///////////////////////////
        for (int i = 0;i < BlockCount;i++)
        {
            //get coordinate of chess block
            int row = CWP[i].row;
            int col = CWP[i].col;
            if ((blockstatus == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black != 0) ||
                (blockstatus == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White != 0))
            {
                result = GetPositionFromSameWeight(blockstatus,sc[row][col].Weight,false);
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
            System.exit(0);//to study
        }
        return result;
    }

    /*public PositionResult AnalyzeWeightBalance(Chessman.ChessmanType blockstatus,int colexcept,int rowexcept)
    {
        PositionResult result = new PositionResult();
        result.result = -1;
        UpdateCanTurnChessCountStatus();
        for (int i = 0;i < BlockCount;i++)
        {
            //get coordinate of chess block
            int row = CWP[i].row;
            int col = CWP[i].col;
            if (row == rowexcept && col == colexcept)
                continue;
            if ((blockstatus == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black != 0) ||
                (blockstatus == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White != 0))
            {
                result = GetPositionFromSameWeight(blockstatus,sc[row][col].Weight,col,row);
                if (result.row == rowexcept && result.col == colexcept)
                    continue;
                if (result.result > 0)
                {
                    return result;
                }
            }
        }
        result = AnalyzeWeightBalance(blockstatus);
        return result;
    }public PositionResult GetPositionFromSameWeight(Chessman.ChessmanType blockstatus,int weightvalue,int exceptcol,int exceptrow)
    {
        PositionResult p = new PositionResult();
        int count = 0,MaxWinChessNum = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (row == exceptrow && col == exceptcol)
                    continue;
                switch (blockstatus)
                {
                    case BLACK:
                        if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == weightvalue && sc[row][col].iWinChessNum_Black > MaxWinChessNum)
                        {
                            count++;
                            MaxWinChessNum = sc[row][col].iWinChessNum_Black;
                            p.col = col;
                            p.row = row;
                        }
                        break;
                    case WHITE:
                        if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == weightvalue && sc[row][col].iWinChessNum_White > MaxWinChessNum)
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
    }*/

    public PositionResult AnalyzeAlphaBeta(Chessman.ChessmanType blockstatus)
    {
        PositionResult result = new PositionResult();
        PositionResult result_computer = new PositionResult();
        PositionResult result_people = new PositionResult();
        int computer_count,people_count,row,col,maxloop;
        Chessman.ChessmanType current_player ;
        Chessman.ChessmanType computer_side = blockstatus;
        Chessman.ChessmanType people_side = (blockstatus == Chessman.ChessmanType.WHITE ? Chessman.ChessmanType.BLACK:Chessman.ChessmanType.WHITE);
        ArrayList<PositionResult> resultlist = new ArrayList<PositionResult>();

        result.result = -1;
        BackupChessman();
        for (row = 0;row < BlockNum;row++)
        {
            for (col = 0;col < BlockNum;col++)
            {
                RestoreChessman();
                if (sc[row][col].ct == Chessman.ChessmanType.NONE &&
                        (computer_side == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black > 0) ||
                        (computer_side == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White > 0))
                {
                    computer_count = GetTurnChessResult(computer_side,col,row);
                    if (computer_count <= 0)
                        continue;

                    maxloop = 1;
                    current_player = people_side;
                    while(maxloop < 60)//GetEmptyChessPosition() / 2
                    {
                        if (IfGameOver())
                        {
                            break;
                        }
                        if (current_player == people_side)
                        {
                            result_people = AnalyzeWeightBalance(current_player);
                            if (result_people.result > 0)
                            {
                                if (GetTurnChessResult(current_player,result_people.col,result_people.row) <= 0)
                                    break;
                            }
                            current_player = computer_side;
                        }
                        else
                        {
                            result_computer = AnalyzeWeightBalance(current_player);
                            if (result_computer.result > 0)
                            {
                                if (GetTurnChessResult(current_player,result_computer.col,result_computer.row) <= 0)
                                    break;
                            }
                            current_player = people_side;
                        }
                        maxloop++;
                    }
                    computer_count = CalculateChessCount(computer_side);
                    people_count = CalculateChessCount(people_side);
                    if (computer_count > people_count)
                    {
                        result_computer.row = row;
                        result_computer.col = col;
                        result_computer.result = computer_count - people_count;
                        resultlist.add(result_computer);
                    }
                }
            }
        }
        for (int i = 0;i < resultlist.size();i++)
        {
            if (resultlist.get(i).result > result.result)
            {
                result.result = resultlist.get(i).result;
                result.row = resultlist.get(i).row;
                result.col = resultlist.get(i).col;
            }
        }
        RestoreChessman();
        return result;
    }

    private void UpdateCanTurnChessCountStatus()
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
    }
    private int GetCanTurnChessNumAll(Chessman.ChessmanType blockstatus,int col,int row)
    {
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
        return ScoreSummary(score);
    }

    public int GetTurnChessResult(Chessman.ChessmanType blockstatus,int col,int row)
    {
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
        if (FindInvalidValue(turnresult))
        {
            //OutputChessStatus();
            return -1;
        }
        UpdateCanTurnChessCountStatus();
        return ScoreSummary(score);
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
        int result = 0,stepcol = 0,steprow = 0;
        boolean bfind = false;
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
        if ((col >= 0 && col < BlockNum) && (row >= 0 && row < BlockNum) && (sc[row][col].ct == blockstatus))
        {
            bfind = true;
        }
        return bfind ? result : 0;
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
    private int GetEmptyChessPosition()
    {
        int iRemainChess = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].ct == Chessman.ChessmanType.NONE)
                    iRemainChess++;
            }
        }
        return iRemainChess;
    }
    private int GetWinChessNum(Chessman.ChessmanType blockstatus,int col,int row)
    {
        //UpdateCanTurnChessCountStatus();
        if (blockstatus == Chessman.ChessmanType.WHITE)
            return sc[row][col].iWinChessNum_White;
        else if (blockstatus == Chessman.ChessmanType.BLACK)
            return sc[row][col].iWinChessNum_Black;
        else
            return 0;
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

    private boolean UpdateCWPWeightStatus()
    {
        boolean UpdateWeight = false;
        int col,row;
        for (int i = 0;i < BlockCount;i++)
        {
            row = CWP[i].row;
            col = CWP[i].col;
            if (sc[row][col].ct == Chessman.ChessmanType.NONE && CWP[i].BlockWeight == WEIGHT_LEVEL6)
            {
                if (((row == 0 || row == BlockNum - 1) && (EmptyChessPositionCount(col,row,DIRECTION.ROW) == 1)) ||
                        ((col == 0 || col == BlockNum - 1) && (EmptyChessPositionCount(col,row,DIRECTION.COL) == 1)))
                {
                    CWP[i].BlockWeight = WEIGHT_LEVEL8;
                    sc[row][col].Weight = WEIGHT_LEVEL8;
                    UpdateWeight = true;
                }
            }
        }
        if (UpdateWeight)
        {
            WeightPositionSort(CWP,BlockCount);
        }
        return UpdateWeight;
    }

    private void UpdatePositionWeight(int col1,int row1,int col2,int row2,int col3,int row3)
    {
        CWP[row1 * BlockNum + col1].BlockWeight = WEIGHT_LEVEL8;
        CWP[row2 * BlockNum + col2].BlockWeight = WEIGHT_LEVEL8;
        CWP[row3 * BlockNum + col3].BlockWeight = WEIGHT_LEVEL8;
        sc[row1][col1].Weight = WEIGHT_LEVEL8;
        sc[row2][col2].Weight = WEIGHT_LEVEL8;
        sc[row3][col3].Weight = WEIGHT_LEVEL8;
    }
    private void UpdateCWPWeightStatusForLevel6(Chessman.ChessmanType blockstatus)
    {
        boolean UpdateWeight = false;
        int row1,row2,col1,col2,row3,col3;
        if (sc[0][0].ct == blockstatus)
        {
            row1 = 0;
            col1 = 1;
            row2 = 1;
            col2 = 0;
            row3 = 1;
            col3 = 1;
            UpdatePositionWeight(col1,row1,col2,row2,col3,row3);
            UpdateWeight = true;
        }
        if (sc[0][BlockNum - 1].ct == blockstatus)
        {
            row1 = 0;
            col1 = BlockNum - 2;
            row2 = 1;
            col2 = BlockNum - 1;
            row3 = 1;
            col3 = BlockNum - 2;
            UpdatePositionWeight(col1,row1,col2,row2,col3,row3);
            UpdateWeight = true;
        }
        if (sc[BlockNum - 1][0].ct == blockstatus)
        {
            row1 = BlockNum - 2;
            col1 = 0;
            row2 = BlockNum - 1;
            col2 = 1;
            row3 = BlockNum - 2;
            col3 = 1;
            UpdatePositionWeight(col1,row1,col2,row2,col3,row3);
            UpdateWeight = true;
        }
        if (sc[BlockNum - 1][BlockNum - 1].ct == blockstatus)
        {
            row1 = BlockNum - 2;
            col1 = BlockNum - 1;
            row2 = BlockNum - 1;
            col2 = BlockNum - 2;
            row3 = BlockNum - 2;
            col3 = BlockNum - 2;
            UpdatePositionWeight(col1,row1,col2,row2,col3,row3);
            UpdateWeight = true;
        }
        if (UpdateWeight)
        {
            WeightPositionSort(CWP,BlockCount);
        }
    }
    private void UpdateCWPWeightStatusForLevel1(Chessman.ChessmanType blockstatus)
    {
        boolean UpdateWeight = false;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].ct == Chessman.ChessmanType.NONE && sc[row][col].Weight == WEIGHT_LEVEL1)
                {
                    if ((row == 0 || row == BlockNum - 1) && (sc[row][col - 1].ct == enemychess && sc[row][col + 1].ct == enemychess))
                    {
                        CWP[row * BlockNum + col].BlockWeight = WEIGHT_LEVEL8;
                        sc[row][col].Weight = WEIGHT_LEVEL8;
                    }
                    if ((col == 0 || col == BlockNum - 1) && (sc[row - 1][col].ct == enemychess && sc[row + 1][col].ct == enemychess))
                    {
                        CWP[row * BlockNum + col].BlockWeight = WEIGHT_LEVEL8;
                        sc[row][col].Weight = WEIGHT_LEVEL8;
                    }
                }
            }
        }
        if (UpdateWeight)
        {
            WeightPositionSort(CWP,BlockCount);
        }
    }
    private int GetMaxWeightPositionCount(Chessman.ChessmanType blockstatus)
    {
        int  iMaxWeightPositions = 0;
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (sc[row][col].Weight == WEIGHT_MAX && sc[row][col].ct == Chessman.ChessmanType.NONE && GetWinChessNum(blockstatus,col,row) > 0)
                {
                    iMaxWeightPositions++;
                }
            }
        }
        return iMaxWeightPositions;
    }

    private boolean FindInvalidValue(int value[])
    {
        for (int i = 0;i < value.length;i++)
        {
            if (value[i] == INVALIDVALUE)
            {
                return true;
            }
        }
        return false;
    }
    private int ScoreSummary(int score[])
    {
        int result = 0;
        for (int i = 0;i < score.length;i++)
        {
            result += score[i];
        }
        return result;
    }
    private void BackupChessman()
    {
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc_backup[row][col].x = sc[row][col].x;
                sc_backup[row][col].y = sc[row][col].y;
                sc_backup[row][col].w = sc[row][col].w;
                sc_backup[row][col].h = sc[row][col].h;
                sc_backup[row][col].ct = sc[row][col].ct;
                sc_backup[row][col].Orange = sc[row][col].Orange;
                sc_backup[row][col].Weight = sc[row][col].Weight;
                sc_backup[row][col].iWinChessNum_Black = sc[row][col].iWinChessNum_Black;
                sc_backup[row][col].iWinChessNum_White = sc[row][col].iWinChessNum_White;
            }
        }
        for (int i = 0;i < BlockCount;i++)
        {
            CWP_backup[i].BlockWeight = CWP[i].BlockWeight;
            CWP_backup[i].col = CWP[i].col;
            CWP_backup[i].row = CWP[i].row;
        }
        WeightPositionSort(CWP,BlockCount);
        UpdateCanTurnChessCountStatus();
    }
    private void RestoreChessman()
    {
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc[row][col].x = sc_backup[row][col].x;
                sc[row][col].y = sc_backup[row][col].y;
                sc[row][col].w = sc_backup[row][col].w;
                sc[row][col].h = sc_backup[row][col].h;
                sc[row][col].ct = sc_backup[row][col].ct;
                sc[row][col].Orange = sc_backup[row][col].Orange;
                sc[row][col].Weight = sc_backup[row][col].Weight;
                sc[row][col].iWinChessNum_Black = sc_backup[row][col].iWinChessNum_Black;
                sc[row][col].iWinChessNum_White = sc_backup[row][col].iWinChessNum_White;
            }
        }
        for (int i = 0;i < BlockCount;i++)
        {
            CWP[i].BlockWeight = CWP_backup[i].BlockWeight;
            CWP[i].col = CWP_backup[i].col;
            CWP[i].row = CWP_backup[i].row;
        }
        WeightPositionSort(CWP,BlockCount);
        UpdateCanTurnChessCountStatus();
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
    }
}
