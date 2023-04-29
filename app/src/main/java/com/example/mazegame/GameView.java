package com.example.mazegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View
{
    private enum Direction{UP,DOWN,LEFT,RIGHT};
    private  Cell[][] cells;
    private Cell player, exit;
    private int COLS = 10;
    private int ROWS = 20;
    private float cellSize;
    private float hMargin;
    private float vMargin;
    private float strokeWidth = 6;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;

    public GameView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(strokeWidth);
        random = new Random();
        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);
        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);
        createMaze();
    }

    private void createMaze()
    {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;
        cells = new Cell[COLS][ROWS];
        for(int i=0;i<COLS;i++)
        {
            for(int j=0;j<ROWS;j++)
                cells[i][j]= new Cell(i,j);
        }
        player = cells[0][0];
        exit = cells[COLS-1][ROWS-1];
        current = cells[0][0];
        current.visited = true;
        do {
            next = getNeighbour(current);
            if (next != null)
            {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            }
            else
                current = stack.pop();
        }while(!stack.empty());
    }

    private void removeWall(Cell current, Cell next)
    {
        if(current.col == next.col && current.row == next.row+1)
        {
            current.TopWall = false;
            next.BottomWall = false;
        }
        if(current.col == next.col && current.row == next.row-1)
        {
            next.TopWall = false;
            current.BottomWall = false;
        }
        if(current.col == next.col+1 && current.row == next.row)
        {
            current.LeftWall = false;
            next.RightWall = false;
        }
        if(current.col == next.col-1 && current.row == next.row)
        {
            next.LeftWall = false;
            current.RightWall = false;
        }
    }

    private Cell getNeighbour(Cell cell)
    {
        ArrayList<Cell> neighbours = new ArrayList<>();
        if(cell.col>0) {
            if (!cells[cell.col - 1][cell.row].visited)
                neighbours.add(cells[cell.col - 1][cell.row]);
        }
        if(cell.row>0)
        {
            if (!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);
        }
        if(cell.col<COLS-1) {
            if (!cells[cell.col + 1][cell.row].visited)
                neighbours.add(cells[cell.col + 1][cell.row]);
        }
        if(cell.row<ROWS-1)
        {
            if (!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);
        }
        if(neighbours.size()>0)
        {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
        int width = getWidth();
        int height = getHeight();
        if(width>height && COLS<ROWS)
        {
            COLS = 18;
            ROWS = 7;
            createMaze();
        }
        else if(height>width && COLS>ROWS)
        {
            COLS = 10;
            ROWS = 18;
            createMaze();
        }
        if((float)width/height < (float)COLS/ROWS)
            cellSize = width/(COLS+1);
        else
            cellSize = height/(ROWS+1);
        hMargin = (width - (COLS * cellSize))/2;
        vMargin = (height - ROWS * cellSize)/2;
        canvas.translate(hMargin, vMargin);
        for(int i=0;i<COLS;i++)
        {
            for(int j=0;j<ROWS;j++)
            {
                if(cells[i][j].TopWall)
                    canvas.drawLine(
                            i*cellSize,
                            j*cellSize,
                            (i+1)*cellSize,
                            (j)*cellSize,
                            wallPaint);
                if(cells[i][j].LeftWall)
                    canvas.drawLine(
                            i*cellSize,
                            j*cellSize,
                            i*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
                if(cells[i][j].RightWall)
                    canvas.drawLine(
                            (i+1)*cellSize,
                            j*cellSize,
                            (i+1)*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
                if(cells[i][j].BottomWall)
                    canvas.drawLine(
                            i*cellSize,
                            (j+1)*cellSize,
                            (i+1)*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
            }
        }
        float margin = cellSize/15;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.right);
        bmp = Bitmap.createScaledBitmap(bmp, (int)(cellSize-2*margin), (int)(cellSize-2*margin), false);
        canvas.drawBitmap(bmp,
                player.col*cellSize+margin,
                player.row*cellSize+margin,
                null);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp = Bitmap.createScaledBitmap(bmp, (int)(cellSize-2*margin), (int)(cellSize-2*margin), false);
        canvas.drawBitmap(bmp,
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                null);
    }

    private void checkExit()
    {
        if(player == exit)
            createMaze();
    }

    private void movePlayer(Direction direction)
    {
        switch (direction)
        {
            case UP:
                if(!player.TopWall)
                    player = cells[player.col][player.row-1];
                break;
            case DOWN:
                if(!player.BottomWall)
                    player = cells[player.col][player.row+1];
                break;
            case LEFT:
                if(!player.LeftWall)
                    player = cells[player.col-1][player.row];
                break;
            case RIGHT:
                if(!player.RightWall)
                    player = cells[player.col+1][player.row];
                break;
        }
        checkExit();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
            return true;
        if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float x = event.getX();
            float y = event.getY();
            float centerX = hMargin+(player.col+0.5f)*cellSize;
            float centerY = vMargin+(player.row+0.5f)*cellSize;
            float dx = x-centerX;
            float dy = y-centerY;
            float absdx = Math.abs(dx);
            float absdy = Math.abs(dy);
            if((absdx>cellSize*0.5 && absdx < cellSize*1.5) || (absdy>cellSize*0.5 && absdy < cellSize*1.5))
            {
                if(absdx>absdy)
                {
                    if(dx>0)
                        movePlayer(Direction.RIGHT);
                    else
                        movePlayer(Direction.LEFT);
                }
                else
                {
                    if(dy>0)
                        movePlayer(Direction.DOWN);
                    else
                        movePlayer(Direction.UP);
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class Cell
    {
        boolean TopWall = true;
        boolean BottomWall = true;
        boolean LeftWall = true;
        boolean RightWall = true;
        int col,row;
        boolean visited = false;
        public Cell(int col, int row)
        {
            this.col = col;
            this.row = row;
        }
    }
}