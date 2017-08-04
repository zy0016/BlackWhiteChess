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
    private Chessman[][] sc_backup2 = null;
    private CheckWeightPosition[] CWP = null;
    private CheckWeightPosition[] CWP_backup = null;
    private CheckWeightPosition[] CWP_backup2 = null;
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
        CWP_backup2 = new CheckWeightPosition[BlockCount];
        sc_backup = new Chessman[BlockNum][BlockNum];
        sc_backup2 = new Chessman[BlockNum][BlockNum];
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
                sc_backup2[row][col] = new Chessman();
                sc_backup2[row][col].Weight = sc[row][col].Weight;
                sc_backup2[row][col].ct = sc[row][col].ct;
                ///////////////////////////
                CWP_backup[row * BlockNum + col] = new CheckWeightPosition();
                CWP_backup[row * BlockNum + col].row = row;
                CWP_backup[row * BlockNum + col].col = col;
                CWP_backup[row * BlockNum + col].BlockWeight = ChessPositionWeight[row][col];
                CWP_backup2[row * BlockNum + col] = new CheckWeightPosition();
                CWP_backup2[row * BlockNum + col].row = row;
                CWP_backup2[row * BlockNum + col].col = col;
                CWP_backup2[row * BlockNum + col].BlockWeight = ChessPositionWeight[row][col];
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
                        if (!IfEnemyGetNewMaxWeightPosition(blockstatus,col,row) && !IfGetBadPosition1(blockstatus,col,row))
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

    private void UpdateCWpWeightStatus(Chessman.ChessmanType blockstatus)
    {
        UpdateCWPWeightStatusForLevel1(blockstatus);
        UpdateCWPWeightStatusForLevel6(blockstatus);
        UpdateCWPWeightStatusForLevel7();
    }

    private PositionResult HandleEmergencyStatus(Chessman.ChessmanType blockstatus)
    {
        int  iEnemyHasMaxWeightPositionsOld = 0,iEnemyHasMaxWeightPositionsNew = 0;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;
        PositionResult result = new PositionResult();
        result.result = -1;
        BackupChessman2();
        for (int row = 0;row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                if (CanPutChessInPosition(row,col,blockstatus))
                {
                    RestoreChessman2();
                    iEnemyHasMaxWeightPositionsOld = GetMaxWeightPositionCount(enemychess);
                    if (GetTurnChessResult(blockstatus, col, row) > 0)
                    {
                        iEnemyHasMaxWeightPositionsNew = GetMaxWeightPositionCount(enemychess);
                        if (iEnemyHasMaxWeightPositionsOld > iEnemyHasMaxWeightPositionsNew)
                        {
                            RestoreChessman2();
                            result.row = row;
                            result.col = col;
                            result.result = GetWinChessNum(blockstatus,col,row);
                            result.Text = "HandleEmergencyStatus";
                            return result;
                        }
                    }
                }
            }
        }
        RestoreChessman2();
        return result;
    }

    public PositionResult GetBestChessPlaceAfterAnalyze(Chessman.ChessmanType blockstatus)
    {
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;
        UpdateCWpWeightStatus(blockstatus);

        if (GetMaxWeightPositionCount(enemychess) > 0)
        {
            PositionResult emergency_result = HandleEmergencyStatus(blockstatus);
            if (emergency_result.result != -1)
            {
                return emergency_result;
            }
        }

        PositionResult result_weight = AnalyzeWeightBalance(blockstatus);
        int row = result_weight.row;
        int col = result_weight.col;

        if (sc[row][col].Weight == WEIGHT_MAX || sc[row][col].Weight == WEIGHT_LEVEL8)
        {
            result_weight.Text = "Find WEIGHT_MAX/WEIGHT_LEVEL8";
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
                result_weight.Text = "GetBetterPositionForWeight_Level12";
                return result_weight;
            }
        }
        PositionResult result_alphabeta = AnalyzeAlphaBeta(blockstatus);
        if (result_alphabeta.result > 0)
        {
            return result_alphabeta;
        }
        return result_weight;
    }

    private boolean IfGetBadPosition1(Chessman.ChessmanType blockstatus,int col,int row)
    {
        boolean badposition = false;
        if (row == 0 || row == BlockNum - 1 || col == 0 || col == BlockNum - 1)
        {
            BackupChessman();
            if (CanPutChessInPosition(row,col,blockstatus) && GetTurnChessResult(blockstatus, col, row) > 0)
            {
                int iStartIndex = 2;
                if (row == 0 || row == BlockNum - 1)
                {
                    for (int col1 = iStartIndex;col1 < BlockNum - iStartIndex;col1++)
                    {
                        if (sc[row][col1].ct == Chessman.ChessmanType.NONE && sc[row][col1 - 1].ct == blockstatus && sc[row][col1 + 1].ct == blockstatus)
                        {
                            badposition = true;
                        }
                    }
                }
                if (col == 0 || col == BlockNum - 1)
                {
                    for (int row1 = iStartIndex;row1 < BlockNum - iStartIndex;row1++)
                    {
                        if (sc[row1][col].ct == Chessman.ChessmanType.NONE && sc[row1 - 1][col].ct == blockstatus && sc[row1 + 1][col].ct == blockstatus)
                        {
                            badposition = true;
                        }
                    }
                }
            }
            RestoreChessman();
        }
        return badposition;
    }

    private boolean IfEnemyGetNewMaxWeightPosition(Chessman.ChessmanType blockstatus,int col,int row)
    {
        int  iEnemyHasMaxWeightPositionsOld = 0,iEnemyHasMaxWeightPositionsNew = 0;
        Chessman.ChessmanType enemychess = (blockstatus == Chessman.ChessmanType.WHITE) ? Chessman.ChessmanType.BLACK : Chessman.ChessmanType.WHITE;

        BackupChessman();
        iEnemyHasMaxWeightPositionsOld = GetMaxWeightPositionCount(enemychess);
        if (CanPutChessInPosition(row,col,blockstatus) && GetTurnChessResult(blockstatus, col, row) > 0)
        {
            iEnemyHasMaxWeightPositionsNew = GetMaxWeightPositionCount(enemychess);
        }
        RestoreChessman();
        return (iEnemyHasMaxWeightPositionsNew > iEnemyHasMaxWeightPositionsOld);
    }
    private boolean GetBetterPositionForWeight_Level12(Chessman.ChessmanType blockstatus,int col,int row)
    {
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
        if (bputchess && (IfEnemyGetNewMaxWeightPosition(blockstatus,col,row) || IfGetBadPosition1(blockstatus,col,row)))
        {
            bputchess = false;
        }
        return bputchess;
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
        int iStartIndex = 1;
        ArrayList<PositionResult> resultlist = new ArrayList<PositionResult>();
        PositionResult result = new PositionResult();
        result.result = -1;
        UpdateCanTurnChessCountStatus();
        for (int col = iStartIndex;col < BlockNum - iStartIndex;col++)
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
                    resultlist.add(result);
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
                    resultlist.add(result);
                }
            }
        }
        for (int row = iStartIndex;row < BlockNum - iStartIndex;row++)
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
                    resultlist.add(result);
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
                    resultlist.add(result);
                }
            }
        }
        int maxresult = 0;
        for (int i = 0;i < resultlist.size();i++)
        {
            if ((resultlist.get(i).result > maxresult) &&
                    !IfEnemyGetNewMaxWeightPosition(blockstatus,resultlist.get(i).col,resultlist.get(i).row) &&
                    !IfGetBadPosition1(blockstatus,resultlist.get(i).col,resultlist.get(i).row)
                    )
            {
                maxresult = resultlist.get(i).result;
                result.result = resultlist.get(i).result;
                result.row = resultlist.get(i).row;
                result.col = resultlist.get(i).col;
            }
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
                    result.Text = "GetPositionFromSameWeight:true";
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
                    result.Text = "GetPositionFromSameWeight:false";
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

    public PositionResult AnalyzeAlphaBeta(Chessman.ChessmanType blockstatus)
    {
        PositionResult result = new PositionResult();
        PositionResult result_computer = new PositionResult();
        PositionResult result_people = new PositionResult();
        int computer_count,people_count,row,col/*,maxloop*/;
        Chessman.ChessmanType current_player ;
        Chessman.ChessmanType computer_side = blockstatus;
        Chessman.ChessmanType people_side = (blockstatus == Chessman.ChessmanType.WHITE ? Chessman.ChessmanType.BLACK:Chessman.ChessmanType.WHITE);
        ArrayList<PositionResult> resultlist = new ArrayList<PositionResult>();

        result.result = -1;
        BackupChessman2();
        for (row = 0;row < BlockNum;row++)
        {
            for (col = 0;col < BlockNum;col++)
            {
                if (row == 1 && col == 1 || row == 1 && col == BlockNum - 2 || row == BlockNum - 2 && col == 1 || row == BlockNum - 2 && col == BlockNum - 2)
                    continue;

                if (sc[row][col].ct == Chessman.ChessmanType.NONE &&
                        (computer_side == Chessman.ChessmanType.BLACK && sc[row][col].iWinChessNum_Black > 0) ||
                        (computer_side == Chessman.ChessmanType.WHITE && sc[row][col].iWinChessNum_White > 0))
                {
                    RestoreChessman2();
                    if (IfEnemyGetNewMaxWeightPosition(blockstatus,col,row) ||
                            IfGetBadPosition1(blockstatus,col,row) ||
                            GetTurnChessResult(computer_side,col,row) <= 0)
                        continue;

                    current_player = people_side;
                    while (IfCanPutChessForPlayer(computer_side) || IfCanPutChessForPlayer(people_side))
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
                result.Text = "AnalyzeAlphaBeta:" + Integer.toString(result.result);
            }
        }
        RestoreChessman2();
        return result;
    }

    private void UpdateCanTurnChessCountStatus()
    {
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
        int iscore = ScoreSummary(score);
        if (iscore > 0)
        {
            sc[row][col].ct = blockstatus;//update current position
        }
        else
        {
            return -1;
        }
        if (FindInvalidValue(turnresult))
        {
            //OutputChessStatus();
            return -1;
        }
        UpdateCanTurnChessCountStatus();
        return iscore;
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
    private int GetWinChessNum(Chessman.ChessmanType blockstatus,int col,int row)
    {
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

    private boolean UpdateCWPWeightStatusForLevel7()
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
                    if ((row == 0 || row == BlockNum - 1) && (sc[row][col].ct == enemychess))
                    {
                        if (sc[row][col - 1].ct == Chessman.ChessmanType.NONE)
                        {
                            CWP[row * BlockNum + (col - 1)].BlockWeight = WEIGHT_LEVEL7;
                            sc[row][col - 1].Weight = WEIGHT_LEVEL7;
                            UpdateWeight = true;
                        }
                        if (sc[row][col + 1].ct == Chessman.ChessmanType.NONE)
                        {
                            CWP[row * BlockNum + (col + 1)].BlockWeight = WEIGHT_LEVEL7;
                            sc[row][col + 1].Weight = WEIGHT_LEVEL7;
                            UpdateWeight = true;
                        }
                    }
                    if ((col == 0 || col == BlockNum - 1) && (sc[row][col].ct == enemychess))
                    {
                        if (sc[row - 1][col].ct == Chessman.ChessmanType.NONE)
                        {
                            CWP[(row - 1) * BlockNum + col].BlockWeight = WEIGHT_LEVEL7;
                            sc[row - 1][col].Weight = WEIGHT_LEVEL7;
                            UpdateWeight = true;
                        }
                        if (sc[row + 1][col].ct == Chessman.ChessmanType.NONE)
                        {
                            CWP[(row + 1) * BlockNum + col].BlockWeight = WEIGHT_LEVEL7;
                            sc[row + 1][col].Weight = WEIGHT_LEVEL7;
                            UpdateWeight = true;
                        }
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
                if (
                   (row == 0 && col == 0 ||
                    row == 0 && col == BlockNum - 1 ||
                    row == BlockNum - 1 && col == 0 ||
                    row == BlockNum - 1 && col == BlockNum - 1) &&
                    (sc[row][col].ct == Chessman.ChessmanType.NONE && GetWinChessNum(blockstatus,col,row) > 0))
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
    }
    private void BackupChessman2()
    {
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc_backup2[row][col].x = sc[row][col].x;
                sc_backup2[row][col].y = sc[row][col].y;
                sc_backup2[row][col].w = sc[row][col].w;
                sc_backup2[row][col].h = sc[row][col].h;
                sc_backup2[row][col].ct = sc[row][col].ct;
                sc_backup2[row][col].Orange = sc[row][col].Orange;
                sc_backup2[row][col].Weight = sc[row][col].Weight;
                sc_backup2[row][col].iWinChessNum_Black = sc[row][col].iWinChessNum_Black;
                sc_backup2[row][col].iWinChessNum_White = sc[row][col].iWinChessNum_White;
            }
        }
        for (int i = 0;i < BlockCount;i++)
        {
            CWP_backup2[i].BlockWeight = CWP[i].BlockWeight;
            CWP_backup2[i].col = CWP[i].col;
            CWP_backup2[i].row = CWP[i].row;
        }
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
    private void RestoreChessman2()
    {
        for (int row = 0; row < BlockNum;row++)
        {
            for (int col = 0;col < BlockNum;col++)
            {
                sc[row][col].x = sc_backup2[row][col].x;
                sc[row][col].y = sc_backup2[row][col].y;
                sc[row][col].w = sc_backup2[row][col].w;
                sc[row][col].h = sc_backup2[row][col].h;
                sc[row][col].ct = sc_backup2[row][col].ct;
                sc[row][col].Orange = sc_backup2[row][col].Orange;
                sc[row][col].Weight = sc_backup2[row][col].Weight;
                sc[row][col].iWinChessNum_Black = sc_backup2[row][col].iWinChessNum_Black;
                sc[row][col].iWinChessNum_White = sc_backup2[row][col].iWinChessNum_White;
            }
        }
        for (int i = 0;i < BlockCount;i++)
        {
            CWP[i].BlockWeight = CWP_backup2[i].BlockWeight;
            CWP[i].col = CWP_backup2[i].col;
            CWP[i].row = CWP_backup2[i].row;
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
