package Data;

import Util.Config;

public class Segment {
	
    public double x;
    public double y;
    public boolean marked = false;
    public Segment start;
    public Segment end;
    
  public Segment() {
    	
    }

    public Segment(Segment srcSeg){
        this.x = srcSeg.x;
        this.y = srcSeg.y;
    }

    public Segment(int x , int y ){
        this.x = (double) x;
        this.y = (double) y;
    }

    public Segment(double x, double y) {
        int Ix =(int) (x * Config.CLIIPER_SCALE);
        int Iy =(int) (y * Config.CLIIPER_SCALE);

        this.x = (double)Ix*1.0/Config.CLIIPER_SCALE;
        this.y = (double)Iy*1.0/Config.CLIIPER_SCALE;
    }

    public boolean isMarked() {
        return marked;
    } 

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public Segment getStart() {
        return start;
    }

    public void setStart(Segment start) {
        this.start = start;
    }

    public Segment getEnd() {
        return end;
    }

    public void setEnd(Segment end) {
        this.end = end;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        int lx =  (int)(x * Config.CLIIPER_SCALE);
        this.x = lx*1.0/Config.CLIIPER_SCALE;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        int ly = (int)(y * Config.CLIIPER_SCALE);
        this.y =  ly*1.0/Config.CLIIPER_SCALE;
    }
    
    @Override
    public boolean equals(Object obj) {
        Segment s = (Segment) obj;
        if(x - s.x < 1e-6 && y - s.y < 1e-6){
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "x = "+ x +", y = "+y;
    }
}
