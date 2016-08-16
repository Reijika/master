package com.macaps.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class TrackBox extends View{    

    private float mLeftTopPosX = 0;
	private float mLeftTopPosY = 0;
	private float mRightTopPosX = 0;
	private float mRightTopPosY = 0;
	private float mLeftBottomPosX = 0;
	private float mLeftBottomPosY = 0;
	private float mRightBottomPosX = 0;
	private float mRightBottomPosY = 0;	   
    
    private Paint topLine;
    private Paint bottomLine;
    private Paint leftLine;
    private Paint rightLine;
    private Paint guideLine;
    
    private boolean drawNew = true;
    
 
    public TrackBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

	public TrackBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrackBox(Context context) {
        super(context);
        init(context);
    }
	
    private void init(Context context) {
    	topLine = new Paint();
        bottomLine = new Paint();
        leftLine = new Paint();
        rightLine = new Paint();
     
        setLineParameters(Color.WHITE,2);
        
        guideLine = new Paint();
        guideLine.setColor(Color.GREEN);
        guideLine.setStrokeWidth(4);
        guideLine.setStyle(Paint.Style.STROKE);
	}
    
    private void setLineParameters(int color, float width){
    	 
        topLine.setColor(color);
        topLine.setStrokeWidth(width);
     
        bottomLine.setColor(color);
        bottomLine.setStrokeWidth(width);
     
        leftLine.setColor(color);
        leftLine.setStrokeWidth(width);
     
        rightLine.setColor(color);
        rightLine.setStrokeWidth(width);
     
    }
	
	@Override
    public void onDraw(Canvas canvas) {		
			super.onDraw(canvas);
			
			if (drawNew == true){
				canvas.drawLine(mLeftTopPosX, mLeftTopPosY,	mRightTopPosX, mRightTopPosY, topLine);
				canvas.drawLine(mLeftBottomPosX, mLeftBottomPosY, mRightBottomPosX, mRightBottomPosY, bottomLine);
				canvas.drawLine(mLeftTopPosX,mLeftTopPosY, mLeftBottomPosX,mLeftBottomPosY,leftLine);
				canvas.drawLine(mRightTopPosX,mRightTopPosY, mRightBottomPosX,mRightBottomPosY,rightLine);
				
				//rectangular bounds
				canvas.drawLine(120, 400, 600, 400, guideLine);
				canvas.drawLine(120, 880, 600, 880, guideLine);
				canvas.drawLine(600, 880, 600, 400, guideLine);
				canvas.drawLine(120, 880, 120, 400, guideLine);
				
				//Circular bounds
				//canvas.drawCircle(360, 640, 230, guideLine);				
			}		
    }
	
	public void ScaleFacetoView(Face[] data, int width, int height, TextView q){
	    
		//Compute the scale factors
	     float xScaleFactor = 1;
	     float yScaleFactor = 1;
	     
	     if (height > width){
	    	 xScaleFactor = (float) width/2000.0f;
	    	 yScaleFactor = (float) height/2000.0f;	    	 
	     }
	     else if (height < width){
	    	 xScaleFactor = (float) height/2000.0f;
	    	 yScaleFactor = (float) width/2000.0f;
	     }
	     
		//Take the face coordinates, rotate 90 degrees and reflect across y-axis
		mLeftTopPosX = ((data[0].rect.top * -1.0f) + 1000) * xScaleFactor;
		mLeftTopPosY = ((data[0].rect.left * -1.0f) + 1000) * yScaleFactor;;
		mRightTopPosX = ((data[0].rect.top * -1.0f) + 1000) * xScaleFactor;
		mRightTopPosY = ((data[0].rect.right * -1.0f) + 1000) * yScaleFactor;;
		mLeftBottomPosX = ((data[0].rect.bottom * -1.0f) + 1000) * xScaleFactor;
		mLeftBottomPosY = ((data[0].rect.left * -1.0f) + 1000) * yScaleFactor;;
		mRightBottomPosX = ((data[0].rect.bottom * -1.0f) + 1000) * xScaleFactor;
		mRightBottomPosY = ((data[0].rect.right * -1.0f) + 1000) * yScaleFactor;;

		
		float w = Math.abs(mLeftBottomPosX - mLeftTopPosX); //width
		float h = Math.abs(mLeftTopPosY - mRightTopPosY); //height
		float area =  w * h;		
		float screenarea = width * height;
		float screencoverage = (area/screenarea) * 100;
		
		//debug textview if you want to see the trackbox parameters during photo capture
		q.setText("Left Top Corner: " + mRightBottomPosX + ", " + mRightBottomPosY + "\n" +
				"Right Top Corner: " + mRightTopPosX + ", " + mRightTopPosY + "\n" +
				"Left Bottom Corner: " + mLeftBottomPosX + ", " + mLeftBottomPosY + "\n" +
				"Right Bottom Corner: " + mLeftTopPosX + ", " + mLeftTopPosY + "\n" +
				"Area: " + area + "\n" +
				"Screen Area: " + screenarea + "\n" +
				"Coverage: " + screencoverage + "%"
				);	
	}
	
	public void setInvalidate() {
		drawNew = true;
		invalidate();		
	}
	
	public void clearView(){
		drawNew = false;
		invalidate();
	}

}
