/*
 * Animation.java
 *
 * Animation class by Eric Giguere as seen on developers.sun
 * modified: added Thread.sleep before call to serialization
 *           else it runs too fast for jSimpleDice 2 frames.
 * 
 */

package jsd;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.Thread;

public class Animation extends Canvas implements Runnable {
    private Image[] frames;
    private int     next = 0;
    private boolean go = false;
    Display display;
    
    public Animation( Image[] frames, Display display ){
        this.display = display;
        this.frames = frames;
        //Thread thread = new Thread(this);
        //thread.start();
    }
    
    protected void paint( Graphics g ){
        g.drawImage( frames[next], 30, 20, g.TOP | g.LEFT );
    }
    
    public void startAnimation(){
        go = true;
        repaint();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) { System.out.println(e); }
        
        
        display.callSerially( this );
    }
    
    public void stopAnimation(){
        go = false;
    }
    
    public void run() { // called after previous repaint is finished
        if( go ){
            if( ++next >= frames.length ) next = 0;
            repaint();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { System.out.println(e); }
            
            
            display.callSerially( this );
        }
    }
    
    
}
