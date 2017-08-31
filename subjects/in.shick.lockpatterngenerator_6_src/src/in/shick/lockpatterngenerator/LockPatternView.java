/*
Copyright 2010-2012 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package in.shick.lockpatterngenerator;

import in.shick.lockpatterngenerator.external.Point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LockPatternView extends View
{
    public static final int DEFAULT_LENGTH_PX = 100, DEFAULT_LENGTH_NODES = 3;
    public static final float CELL_NODE_RATIO = 0.75f, NODE_EDGE_RATIO = 0.33f;
    public static final int EDGE_COLOR = 0xffcccccc;
    public static final int BACKGROUND_COLOR = 0xff000000;
    public static final int DEATH_COLOR = 0xffff0000;
    public static final int PRACTICE_RESULT_DISPLAY_MILLIS = 1 * 1000;
    public static final long BUILD_TIMEOUT_MILLIS = 1 * 1000;
    public static final int TACTILE_FEEDBACK_DURATION = 35;

    protected int mLengthPx;
    protected int mLengthNodes;
    protected int mCellLength;
    protected NodeDrawable[][] mNodeDrawables;
    protected Paint mEdgePaint;
    protected HighlightMode mHighlightMode;
    protected boolean mPracticeMode;
    protected Point mTouchPoint;
    protected Point mTouchCell;
    protected boolean mDrawTouchExtension;
    protected int mTouchThreshold;
    protected boolean mDisplayingPracticeResult;
    protected HighlightMode mPracticeFailureMode;
    protected HighlightMode mPracticeSuccessMode;
    protected Handler mHandler;
    protected Vibrator mVibrator;
    protected boolean mTactileFeedback;

    protected List<Point> mCurrentPattern;
    protected List<Point> mPracticePattern;
    protected Set<Point> mPracticePool;

    public LockPatternView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mLengthPx = DEFAULT_LENGTH_PX;
        mLengthNodes = DEFAULT_LENGTH_NODES;
        mNodeDrawables = new NodeDrawable[0][0];
        mCurrentPattern = Collections.emptyList();
        mHighlightMode = new NoHighlight();
        mTouchPoint = new Point(-1, -1);
        mTouchCell = new Point(-1, -1);
        mDrawTouchExtension = false;
        mDisplayingPracticeResult = false;
        mPracticeFailureMode = new FailureHighlight();
        mPracticeSuccessMode = new SuccessHighlight();
        mHandler = new Handler();
        mVibrator = (Vibrator) getContext()
            .getSystemService(Context.VIBRATOR_SERVICE);

        mEdgePaint = new Paint();
        mEdgePaint.setColor(EDGE_COLOR);
        mEdgePaint.setStrokeCap(Paint.Cap.ROUND);
        mEdgePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    // called whenever either the actual drawn length or the nodewise length
    // changes
    private void buildDrawables()
    {
        mNodeDrawables = new NodeDrawable[mLengthNodes][mLengthNodes];

        mCellLength = mLengthPx / mLengthNodes;

        float nodeDiameter = ((float) mCellLength) * CELL_NODE_RATIO;
        mEdgePaint.setStrokeWidth(nodeDiameter * NODE_EDGE_RATIO);
        mTouchThreshold = (int) (nodeDiameter / 2);
        int cellHalf = mCellLength / 2;

        long buildStart = System.currentTimeMillis();
        for(int y = 0; y < mLengthNodes; y++)
        {
            for(int x = 0; x < mLengthNodes; x++)
            {
                // if just building the drawables is taking too long, bail!
                if(System.currentTimeMillis() - buildStart
                        >= BUILD_TIMEOUT_MILLIS)
                {
                    EmergencyExit.clearAndBail(getContext());
                }
                Point center = new Point(x * mCellLength + cellHalf,
                        y * mCellLength + cellHalf);
                mNodeDrawables[x][y] = new NodeDrawable(nodeDiameter, center);
            }
        }

        // re-highlight nodes if not in practice
        if(!mPracticeMode)
        {
            loadPattern(mCurrentPattern, mHighlightMode);
        }
    }

    private void clearPattern(List<Point> pattern)
    {
        for(Point e : pattern)
        {
            mNodeDrawables[e.x][e.y]
                .setNodeState(NodeDrawable.STATE_UNSELECTED);
        }
    }
    private void loadPattern(List<Point> pattern, HighlightMode highlightMode)
    {
        for(int ii = 0; ii < pattern.size(); ii++)
        {
            Point e = pattern.get(ii);
            NodeDrawable node = mNodeDrawables[e.x][e.y];
            int state = highlightMode.select(node, ii, pattern.size(),
                    e.x, e.y, mLengthNodes);
            node.setNodeState(state); // rolls off the tongue
            // if another node follows, then tell the current node which way
            // to point
            if(ii < pattern.size() - 1)
            {
                Point f = pattern.get(ii+1);
                Point centerE = mNodeDrawables[e.x][e.y].getCenter();
                Point centerF = mNodeDrawables[f.x][f.y].getCenter();

                mNodeDrawables[e.x][e.y].setExitAngle((float)
                        Math.atan2(centerE.y - centerF.y,
                            centerE.x - centerF.x));
            }
        }
    }
    // only works properly with practice mode due to highlighting, should
    // probably be generalized and used to replace the bulk of loadPattern()
    private void appendPattern(List<Point> pattern, Point node)
    {
        NodeDrawable nodeDraw = mNodeDrawables[node.x][node.y];
        nodeDraw.setNodeState(NodeDrawable.STATE_SELECTED);
        if(pattern.size() > 0)
        {
            Point tailNode = pattern.get(pattern.size() - 1);
            NodeDrawable tailDraw = mNodeDrawables[tailNode.x][tailNode.y];

            Point tailCenter = tailDraw.getCenter();
            Point nodeCenter = nodeDraw.getCenter();

            tailDraw.setExitAngle((float)
                    Math.atan2(tailCenter.y - nodeCenter.y,
                        tailCenter.x - nodeCenter.x));
        }
        pattern.add(node);
    }

    private void testPracticePattern()
    {
        mDisplayingPracticeResult = true;
        HighlightMode mode = mPracticeFailureMode;
        if(mPracticePattern.equals(mCurrentPattern))
        {
            mode = mPracticeSuccessMode;
        }
        loadPattern(mPracticePattern, mode);
        // clear the result display after a delay
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mDisplayingPracticeResult) {
                    resetPractice();
                    invalidate();
                }
            }
        }, PRACTICE_RESULT_DISPLAY_MILLIS);
    }

    private void resetPractice()
    {
        clearPattern(mPracticePattern);
        mPracticePattern.clear();
        mPracticePool.clear();
        mDisplayingPracticeResult = false;
    }

    //
    // android.view.View overrides
    //

    @Override
    protected void onDraw(Canvas canvas)
    {
        // draw pattern edges first
        Point edgeStart, edgeEnd;
        List<Point> pattern = mCurrentPattern;
        if(mPracticeMode)
        {
            pattern = mPracticePattern;
        }
        CenterIterator patternPx = new CenterIterator(pattern.iterator());

        if(patternPx.hasNext())
        {
            edgeStart = patternPx.next();
            while(patternPx.hasNext())
            {
                edgeEnd = patternPx.next();
                canvas.drawLine(edgeStart.x, edgeStart.y, edgeEnd.x, edgeEnd.y,
                        mEdgePaint);

                edgeStart = edgeEnd;
            }
            if(mDrawTouchExtension)
            {
                canvas.drawLine(edgeStart.x, edgeStart.y, mTouchPoint.x,
                        mTouchPoint.y, mEdgePaint);
            }
        }

        // then draw nodes
        for(int y = 0; y < mLengthNodes; y++)
        {
            for(int x = 0; x < mLengthNodes; x++)
            {
                mNodeDrawables[x][y].draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!mPracticeMode)
        {
            return super.onTouchEvent(event);
        }
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(mDisplayingPracticeResult)
                {
                    resetPractice();
                }
                mDrawTouchExtension = true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX(), y = event.getY();
                mTouchPoint.x = (int) x;
                mTouchPoint.y = (int) y;
                mTouchCell.x = (int) x / mCellLength;
                mTouchCell.y = (int) y / mCellLength;
                if(mTouchCell.x < 0 || mTouchCell.x >= mLengthNodes
                        || mTouchCell.y < 0 || mTouchCell.y >= mLengthNodes)
                {
                    break;
                }
                Point nearestCenter =
                    mNodeDrawables[mTouchCell.x][mTouchCell.y].getCenter();
                int dist = (int) Math.sqrt(Math.pow(x - nearestCenter.x, 2)
                        + Math.pow(y - nearestCenter.y, 2));
                if(dist < mTouchThreshold
                        && !mPracticePool.contains(mTouchCell))
                {
                    if(mTactileFeedback)
                    {
                        mVibrator.vibrate(TACTILE_FEEDBACK_DURATION);
                    }
                    Point newPoint = new Point(mTouchCell);
                    appendPattern(mPracticePattern, newPoint);
                    mPracticePool.add(newPoint);
                }
                break;
            case MotionEvent.ACTION_UP:
                mDrawTouchExtension = false;
                testPracticePattern();
                break;
            default:
                return super.onTouchEvent(event);
        }
        invalidate();
        return true;
    }

    // expand to be as large as the smallest dictated size, or to the default
    // length if both dimensions are unspecified
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int length = 0;
        int width = View.MeasureSpec.getSize(widthMeasureSpec); 
        int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec); 
        int hMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if(wMode == View.MeasureSpec.UNSPECIFIED
                && hMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = DEFAULT_LENGTH_PX;
            setMeasuredDimension(length, length);
        }
        else if(wMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = height;
        }
        else if(hMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = width;
        }
        else
        {
            length = Math.min(width,height);
        }

        setMeasuredDimension(length,length);
    }

    // update draw values dependent on view size so it doesn't have to happen
    // in every onDraw()
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mLengthPx = Math.min(w,h);
        buildDrawables();
        if(!mPracticeMode)
        {
            loadPattern(mCurrentPattern, mHighlightMode);
        }
    }

    //
    // Accessors / Mutators
    //

    public void setPattern(List<Point> pattern)
    {
        clearPattern(mCurrentPattern);
        loadPattern(pattern, mHighlightMode);

        mCurrentPattern = pattern;
    }
    public List<Point> getPattern()
    {
        return mCurrentPattern;
    }

    public void setGridLength(int length)
    {
        mLengthNodes = length;
        mCurrentPattern = Collections.emptyList();
        buildDrawables();
    }
    public int getGridLength()
    {
        return mLengthNodes;
    }

    public void setHighlightMode(HighlightMode mode)
    {
        setHighlightMode(mode, mPracticeMode);
    }
    public void setHighlightMode(HighlightMode mode, boolean suppressRepaint)
    {
        mHighlightMode = mode;
        if(!suppressRepaint)
        {
            loadPattern(mCurrentPattern, mHighlightMode);
        }
    }
    public HighlightMode getHighlightMode()
    {
        return mHighlightMode;
    }

    public void setPracticeMode(boolean mode)
    {
        mDisplayingPracticeResult = false;
        mPracticeMode = mode;
        if(mode)
        {
            mPracticePattern = new ArrayList<Point>();
            mPracticePool = new HashSet<Point>();
            clearPattern(mCurrentPattern);
        }
        else
        {
            clearPattern(mPracticePattern);
            loadPattern(mCurrentPattern, mHighlightMode);
        }
    }
    public boolean getPracticeMode()
    {
        return mPracticeMode;
    }

    public void setTactileFeedbackEnabled(boolean enabled)
    {
        mTactileFeedback = enabled;
    }
    public boolean getTactileFeedbackEnabled()
    {
        return mTactileFeedback;
    }

    //
    // Inner classes
    //

    private class CenterIterator implements Iterator<Point>
    {
        private Iterator<Point> nodeIterator;

        public CenterIterator(Iterator<Point> iterator)
        {
            nodeIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public Point next() {
            Point node = nodeIterator.next();
            return mNodeDrawables[node.x][node.y].getCenter();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Interface for choosing what state to put a node in based on its position
    // in the pattern, allowing for things like highlighting the first node etc.
    public interface HighlightMode
    {
        int select(NodeDrawable node, int patternIndex, int patternLength,
                int nodeX, int nodeY, int gridLength);
    }

    public static class NoHighlight implements HighlightMode
    {
        @Override
        public int select(NodeDrawable node, int patternIndex,
                int patternLength, int nodeX, int nodeY, int gridLength)
        {
            return NodeDrawable.STATE_SELECTED;
        }
    }
    public static class FirstHighlight implements HighlightMode
    {
        @Override
        public int select(NodeDrawable node, int patternIndex,
                int patternLength, int nodeX, int nodeY, int gridLength)
        {
            if(patternIndex == 0)
            {
                return NodeDrawable.STATE_HIGHLIGHTED;
            }
            return NodeDrawable.STATE_SELECTED;
        }
    }
    public static class RainbowHighlight implements HighlightMode
    {
        @Override
        public int select(NodeDrawable node, int patternIndex,
                int patternLength, int nodeX, int nodeY, int gridLength)
        {
            float wheelPosition = ((float) patternIndex / (float) patternLength)
                * 360.0f;
            int color = Color.HSVToColor(
                    new float[] { wheelPosition, 1.0f, 1.0f });
            node.setCustomColor(color);

            return NodeDrawable.STATE_CUSTOM;
        }
    }
    public static class FailureHighlight implements HighlightMode
    {
        @Override
        public int select(NodeDrawable node, int patternIndex,
                int patternLength, int nodeX, int nodeY, int gridLength)
        {
            return NodeDrawable.STATE_INCORRECT;
        }
    }
    public static class SuccessHighlight implements HighlightMode
    {
        @Override
        public int select(NodeDrawable node, int patternIndex,
                int patternLength, int nodeX, int nodeY, int gridLength)
        {
            return NodeDrawable.STATE_CORRECT;
        }
    }
}
