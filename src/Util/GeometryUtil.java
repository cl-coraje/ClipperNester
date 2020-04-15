package Util;

import Data.Bound;
import Data.NestPath;
import Data.Segment;
import Clipper.Clipper;
import Clipper.ClipperOffset;
import Clipper.DefaultClipper;
import Clipper.Path;
import Clipper.Paths;
import java.util.ArrayList;
import java.util.List;

import Algorithm.InsertPoly;

public class GeometryUtil {
	
    private static double TOL = Math.pow(10,-2);

    public static boolean almostEqual(double a, double b ){
        return Math.abs(a-b) < TOL;
    }

    public static boolean almostEqual(double a , double b  , double tolerance){
        return Math.abs(a - b) < tolerance;
    }
    //计算点到线段的最小距离
    public static double distance_segment(Segment pt, Segment ps, Segment pe) {
	    double pqx = pe.x - ps.x;
	    double pqy = pe.y - ps.y;

		double dx = pt.x - ps.x;
		double dy = pt.y - ps.y;
		double d = pqx * pqx + pqy*pqy;
		double t = pqx*dx + pqy*dy;
		if (d > 0)
		    t /= d;
		if (t < 0)
		    t = 0;
		else if (t > 1)
		    t = 1;
		dx = ps.x + t*pqx - pt.x;
		dy = ps.y + t*pqy - pt.y;
		return dx*dx + dy*dy;
	}
    //返回多变形长度
    public static double polygonLength(NestPath polygon){
        double xmin = polygon.getSegments().get(0).getX();
        double xmax = polygon.getSegments().get(0).getX();
        for(int i = 1 ; i < polygon.getSegments().size(); i ++){
            double x = polygon.getSegments().get(i).getX();
            if(x > xmax ){
                xmax = x;
            }
            else if(x < xmin){
                xmin = x;
            }
        }
        return xmax-xmin;
    }

    //计算多边形面积
    public static double polygonArea(NestPath polygon){
        double area = 0;
        for(int i = 0 , j = polygon.size() - 1; i < polygon.size() ; j = i++){
            Segment si = polygon.getSegments().get(i);
            Segment sj = polygon.getSegments().get(j);
            area += (sj.getX() + si.getX()) * (sj.getY() - si.getY());
        }
        return 0.5 * area;
    }
    
    //计算外接矩形的面积
    public static double polygonArea4( NestPath polygon){
        double xmin = polygon.getSegments().get(0).getX();
        double xmax = polygon.getSegments().get(0).getX();
        double ymin = polygon.getSegments().get(0).getY();
        double ymax = polygon.getSegments().get(0).getY();
        for(int i = 1 ; i < polygon.getSegments().size(); i ++){
            double x = polygon.getSegments().get(i).getX();
            double y = polygon.getSegments().get(i).getY();
            if(x > xmax ){
                xmax = x;
            }
            else if(x < xmin){
                xmin = x;
            }

            if(y > ymax ){
                ymax =y;
            }
            else if(y< ymin ){
                ymin = y;
            }
        }
        return (xmax - xmin) * (ymax - ymin);
    }
     
    //计算交点
    public static Segment lineIntersect(Segment A ,Segment B ,Segment E ,Segment F , Boolean infinite){
        double a1, a2, b1, b2, c1, c2, x, y;
        a1= B.y-A.y;
        b1= A.x-B.x;
        c1= B.x*A.y - A.x*B.y;
        a2= F.y-E.y;
        b2= E.x-F.x;
        c2= F.x*E.y - E.x*F.y;

        double denom=a1*b2 - a2*b1;
        
        x = (b1*c2 - b2*c1)/denom;
        y = (a2*c1 - a1*c2)/denom;

        if( !Double.isFinite(x) || !Double.isFinite(y)){
            return null;
        }

//        if(infinite == null || !infinite){
//            if (Math.abs(A.x-B.x) > TOL && (( A.x < B.x ) ? x < A.x || x > B.x : x > A.x || x < B.x )) return null;
//            if (Math.abs(A.y-B.y) > TOL && (( A.y < B.y ) ? y < A.y || y > B.y : y > A.y || y < B.y )) return null;
//            if (Math.abs(E.x-F.x) > TOL && (( E.x < F.x ) ? x < E.x || x > F.x : x > E.x || x < F.x )) return null;
//            if (Math.abs(E.y-F.y) > TOL && (( E.y < F.y ) ? y < E.y || y > F.y : y > E.y || y < F.y )) return null;
//        }
        
        return new Segment(x,y);
    }

    //完全包裹多边形的最小矩形
    public static Bound getPolygonBounds( NestPath polygon){
        double xmin = polygon.getSegments().get(0).getX();
        double xmax = polygon.getSegments().get(0).getX();
        double ymin = polygon.getSegments().get(0).getY();
        double ymax = polygon.getSegments().get(0).getY();
        for(int i = 1 ; i < polygon.getSegments().size(); i ++){
            double x = polygon.getSegments().get(i).getX();
            double y = polygon.getSegments().get(i).getY();
            if(x > xmax ){
                xmax = x;
            }
            else if(x < xmin){
                xmin = x;
            }

            if(y > ymax ){
                ymax =y;
            }
            else if(y< ymin ){
                ymin = y;
            }
        }
        return new Bound(xmin,ymin,xmax-xmin,ymax-ymin);
    }

    //将多边形旋转一定角度后，返回旋转后多边形外接矩形
    public static Bound rotatePolygon (NestPath polygon ,int angle){
        if(angle == 0 ){
            return getPolygonBounds(polygon);
        }
        double Fangle = angle * Math.PI / 180;
        NestPath rotated = new NestPath();
        for(int i = 0; i < polygon.size(); i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x * Math.cos(Fangle) - y * Math.sin(Fangle);
            double y1 = x * Math.sin(Fangle) + y * Math.cos(Fangle);
            rotated.add(x1,y1);
        }
        Bound bounds = getPolygonBounds(rotated);
        return bounds;
    }

    //将多边形旋转一定角度后，返回该旋转后的多边形
    public static NestPath rotatePolygon2Polygon(NestPath polygon , double degrees ){
        NestPath rotated = new NestPath();
        double angle = degrees * Math.PI / 180;
        for(int i = 0 ; i< polygon.size() ; i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x * Math.cos(angle) - y * Math.sin(angle);
            double y1 = x * Math.sin(angle) + y * Math.cos(angle);
            rotated.add(new Segment(x1 , y1));
        }
        rotated.bid = polygon.bid;
        rotated.type = polygon.type;
        return rotated;
    }

    public static Segment normalizeVector(Segment v ){
        if( almostEqual(v.x * v.x + v.y * v.y , 1)){
            return v;
        }
        double len = Math.sqrt(v.x * v.x + v.y *v.y);
        double inverse = 1/len;
        return new Segment(v.x * inverse , v.y * inverse);
    }
    
    //判断p是否在多边形内部
    public static Boolean pointInPolygon(Segment point ,NestPath polygon){
        boolean inside = false;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i).x;
            double yi = polygon.get(i).y;
            double xj = polygon.get(j).x;
            double yj = polygon.get(j).y;

            if(onSegment( new Segment(xi,yi),new Segment(xj,yj) , point)){
                return false ; // exactly on the segment
            }

            boolean intersect = ((yi > point.y) != (yj > point.y)) && (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        
        return inside;
    }
    
    //判断p是否在A和B组成的线段上 ,在延长线上不算
    public static boolean onSegment(Segment A, Segment B , Segment p ){
        // vertical line
        if(almostEqual(A.x, B.x) && almostEqual(p.x, A.x)){
            if(!almostEqual(p.y, B.y) && !almostEqual(p.y, A.y) && p.y < Math.max(B.y, A.y) && p.y > Math.min(B.y, A.y)){
                return true;
            }
            else{
                return false;
            }
        }

        // horizontal line
        if(almostEqual(A.y, B.y) && almostEqual(p.y, A.y)){
            if(!almostEqual(p.x, B.x) && !almostEqual(p.x, A.x) && p.x < Math.max(B.x, A.x) && p.x > Math.min(B.x, A.x)){
                return true;
            }
            else{
                return false;
            }
        }

        //range check
        if((p.x < A.x && p.x < B.x) || (p.x > A.x && p.x > B.x) || (p.y < A.y && p.y < B.y) || (p.y > A.y && p.y > B.y)){
            return false;
        }

        // exclude end points
        if((almostEqual(p.x, A.x) && almostEqual(p.y, A.y)) || (almostEqual(p.x, B.x) && almostEqual(p.y, B.y))){
            return false;
        }

        double cross = (p.y - A.y) * (B.x - A.x) - (p.x - A.x) * (B.y - A.y);
        if(Math.abs(cross) > TOL){
            return false;
        }

        double dot = (p.x - A.x) * (B.x - A.x) + (p.y - A.y) * (B.y - A.y);
        if(dot < 0 || almostEqual(dot, 0)){
            return false;
        }

        double len2 = (B.x - A.x)*(B.x - A.x) + (B.y - A.y)*(B.y - A.y);
        if(dot > len2 || almostEqual(dot, len2)){
            return false;
        }
        
        return true;
    }
    
    //判断两个多边形是否相同
    public static boolean isSame(NestPath A , NestPath B) {
    	boolean same = false;
    	int t = 0;
    	if(almostEqual(GeometryUtil.polygonArea(A), GeometryUtil.polygonArea(B))) {//A.size() == B.size() &&
    		for(t = 0; t < B.size(); t++) {
				   Segment s1 = A.get(t);
				   Segment s2 = B.get(t);
				   if(!almostEqual(s1.getX(), s2.getX()) || !almostEqual(s1.getY(), s2.getY())) {
					   break;
				   }
			   }
			   if(t == B.size())
				   same = true;
       }
    	   return same;
     }
    
    /**
     * 减少顶点
     * @param _path
     * @return
     */
   
    public static NestPath cleanNode(NestPath _path,double _area, double para) {
 	   
 	   NestPath nestpath = new NestPath();
 	    //通过复制迭代减少凹点
 	   	Path path = CommonUtil.NestPath2Path(_path);
 	    Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.EVEN_ODD);
 	    //先外扩0.01，再操作
 	    if(para == 0)
 	    	nestpath = CommonUtil.clipperToNestPath(simple.get(0));
 	    else {
 	    	int miterLimit = 5;
 	        ClipperOffset co = new ClipperOffset(miterLimit , Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
 	        co.addPath(simple.get(0), Clipper.JoinType.MITER , Clipper.EndType.CLOSED_POLYGON);
 	        Paths newpaths = new Paths();
 	        co.execute(newpaths , para * Config.CLIIPER_SCALE);
 	        nestpath = CommonUtil.clipperToNestPath(newpaths.get(0));	
 	    }
 	   
 	    NestPath nest1 = new NestPath();
 	    for(int i = 0; i < nestpath.size() ; i++) {
 	    	nest1 = new NestPath(nestpath);
 	       	int size = nestpath.size();
 	       	Segment s1 = new Segment(); Segment s2 = new Segment(); Segment s3 = new Segment();
 	       	s1 = nestpath.segments.get(i);
 	       	if(i == size - 1) {
 	       		s2 = nestpath.segments.get(0);
 	       		s3 = nestpath.segments.get(1);
 	       		nest1.segments.remove(0);
 	       	}else if(i == size - 2) {
 	       		s2 = nestpath.segments.get(i+1);
 	       		s3 = nestpath.segments.get(0);
 	       		nest1.segments.remove(i+1);
 	       	}else {
 	       		s2 = nestpath.segments.get(i+1);
 	       		s3 = nestpath.segments.get(i+2);
 	       		nest1.segments.remove(i+1);
 	       	}
 	        
 	       	if(pointInPolygon(s2,nest1)) {
 	       		if(distance_segment(s2,s1,s3) > 2)
 	       			continue;
 	       		nestpath = new NestPath(nest1);
 	       		i = -1;
 	       	}
 	       }
 	    
 	    //通过延长线减少交点
 	    for(int i = 0; i < nestpath.size() ; i++) {
 	    	int size = nestpath.size();
 	       	Segment s1 = new Segment(); Segment s2 = new Segment();
 	       	Segment s3 = new Segment(); Segment s4 = new Segment();
 	       	s1 = nestpath.get(i);
 	       	if(i == nestpath.size() - 1) {
 	       		s2 = nestpath.get(0);
 	       		s3 = nestpath.get(1);
 	       		s4 = nestpath.get(2);
 	       	}else if(i == nestpath.size() - 2) {
 	       		s2 = nestpath.get(i + 1);
 	       		s3 = nestpath.get(0);
 	       		s4 = nestpath.get(1);
 	       	}else if(i == nestpath.size() - 3) {
 	       		s2 = nestpath.get(i + 1);
 	       		s3 = nestpath.get(i + 2);
 	       		s4 = nestpath.get(0);
 	       	}else {
 	       		s2 = nestpath.get(i + 1);
 	       		s3 = nestpath.get(i + 2);
 	       		s4 = nestpath.get(i + 3);
 	       	}
 	       	
 	       	Segment s = lineIntersect(s1,s2,s3,s4,true);
 	       
 	       	if(s != null) {
 	       		if(distance_segment(s,s2,s3) > 2)
 	       			continue;
 	       		double area = Math.abs(s2.x*s.y + s.x*s3.y + s3.x*s2.y - s2.x*s3.y - s.x*s2.y - s3.x*s.y)/2;
 	       		if(area > _area)
 	           		continue;
 	           	if(pointInPolygon(s,nestpath))
 	           		continue;
 	           	if(i == size - 3) {
 	           		nestpath.segments.remove(size - 1);
 	               	nestpath.segments.remove(size - 2);
 	           		nestpath.segments.add(s);
 	           		
 	           	}else if(i == size - 2 ) {
 	           		nestpath.segments.remove(size - 1);
 	               	nestpath.segments.remove(0);
 	           		nestpath.segments.add(0,s);
 	           		
 	           	}else if(i == size - 1) {
 	           		nestpath.segments.remove(1);
 	               	nestpath.segments.remove(0);
 	           		nestpath.segments.add(0,s);
 	 
 	           	}else {
 	           		nestpath.segments.remove(i + 2);
 	               	nestpath.segments.remove(i + 1);
 	           		nestpath.segments.add(i+1,s);
 	           		
 	           	}
 	           	i = -1;
 	       	}
        }
        nestpath.bid = _path.bid;
        nestpath.area = _path.area;
        nestpath.length = _path.length;
        nestpath.type = _path.type;
        nestpath.setRotation(_path.rotation);
        for(int i = 0; i < _path.MAX_Rotation_Size;i++) {
     	   nestpath.rotations[i] =  _path.rotations[i];
        }
        return nestpath;
    }

    /**
     * 专门为环绕矩形生成的nfp
     * @param A
     * @param B
     * @return
     */
    
    public static List<NestPath> noFitPolygonRectangle(NestPath A , NestPath B){
        double minAx = A.get(0).x;
        double minAy = A.get(0).y;
        double maxAx = A.get(0).x;
        double maxAy = A.get(0).y;

        for(int i = 1; i < A.size(); i++){
            if(A.get(i).x < minAx){
                minAx = A.get(i).x;
            }
            if(A.get(i).y < minAy){
                minAy = A.get(i).y;
            }
            if(A.get(i).x > maxAx){
                maxAx = A.get(i).x;
            }
            if(A.get(i).y > maxAy){
                maxAy = A.get(i).y;
            }
        }

        double minBx = B.get(0).x;
        double minBy = B.get(0).y;
        double maxBx = B.get(0).x;
        double maxBy = B.get(0).y;
        for(int i=1; i<B.size(); i++){
            if(B.get(i).x < minBx){
                minBx = B.get(i).x;
            }
            if(B.get(i).y < minBy){
                minBy = B.get(i).y;
            }
            if(B.get(i).x > maxBx){
                maxBx = B.get(i).x;
            }
            if(B.get(i).y > maxBy){
                maxBy = B.get(i).y;
            }
        }

        if(maxBx-minBx > maxAx - minAx){
            return null;
        }
   
        double diffBy = maxBy - minBy;
        double diffAy = maxAy - minAy;

        if(diffBy > diffAy){
            return null;
        }

        List<NestPath> nfpRect = new ArrayList<NestPath>();
        NestPath res = new NestPath();
        res.add(minAx - minBx + B.get(0).x , minAy - minBy + B.get(0).y);
        res.add(maxAx - maxBx + B.get(0).x , minAy - minBy + B.get(0).y);
        res.add(maxAx - maxBx + B.get(0).x , maxAy - maxBy + B.get(0).y);
        res.add(minAx - minBx + B.get(0).x , maxAy - maxBy + B.get(0).y);
        nfpRect.add(res);
        return nfpRect;
    }

    /**
     * 闵可夫斯基法生成nfp
     * @param A
     * @param B
     * @return
     */
    
    public static List<NestPath> minkowskiDifference(NestPath A, NestPath B){
        Path Ac = InsertPoly.scaleUp2ClipperCoordinates(A);
        Path Bc = InsertPoly.scaleUp2ClipperCoordinates(B);
        for(int i = 0 ; i < Bc.size();i++){
            long X = Bc.get(i).getX();
            long Y = Bc.get(i).getY();
            Bc.get(i).setX(-1 * X);
            Bc.get(i).setY(-1 * Y);
        }
        Paths solution =  DefaultClipper.minkowskiSum(Ac , Bc , true);
        double largestArea = Double.MAX_VALUE;
        NestPath clipperNfp = null;
        for(int  i = 0; i < solution.size() ; i ++){
            NestPath n = InsertPoly.toNestCoordinates(solution.get(i));
            double sarea = GeometryUtil.polygonArea(n);
            if(largestArea > sarea){
                clipperNfp = n;
                largestArea = sarea;
            }
        }

        for(int  i = 0 ; i < clipperNfp.size() ; i ++){
            clipperNfp.get(i).x += B.get(0).x ;
            clipperNfp.get(i).y += B.get(0).y ;
        }
        
        List<NestPath> nfp = new ArrayList<NestPath>();
        nfp.add(clipperNfp);
        return nfp;
    }
}
