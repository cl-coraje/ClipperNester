package Data;

import Clipper.Paths;

public class Vector {
    public double x;         //零件坐标的x偏移量
    public double y;         //零件坐标的y偏移量
    public int id;           //零件标号
    public int rotation;     //方向
    public Paths nfp;        
    
    public Vector() {
        nfp = new Paths();
    }

    public Vector(double x, double y, int id, int rotation) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.rotation = rotation;
        this.nfp = new Paths();
    }

    public Vector(double x, double y, int id, int rotation, Paths nfp) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.rotation = rotation;
        this.nfp = nfp;
    }

    @Override
    public String toString() {
        return  "x = "+ x +" , y = "+y ;
    }
    
}
