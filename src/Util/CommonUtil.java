package Util;

import Data.NestPath;
import Data.Segment;
import Data.Vector;
import Util.coor.ClipperCoor;
import Util.coor.NestCoor;

import java.util.ArrayList;
import java.util.List;

import Clipper.*;

public class CommonUtil {
	
	/**********************************************************************************************
    --------------------------path和NestPath的转换--------------Clipper和NestPath的转换----------------
    *********************************************************************************************/

    public static NestPath Path2NestPath (Path path){
        NestPath nestPath = new NestPath();
        for(int  i = 0; i < path.size() ; i++){
            Point.LongPoint lp = path.get(i);
            NestCoor coor = CommonUtil.toNestCoor(lp.getX(),lp.getY());
            nestPath.add(new Segment(coor.getX() , coor.getY()));
        }
        return nestPath;
    }
    
    public static NestPath clipperToNestPath(Path polygon){
        NestPath normal = new NestPath();
        for(int i = 0; i < polygon.size() ; i++){
            NestCoor nestCoor = toNestCoor(polygon.get(i).getX() , polygon.get(i).getY());
            normal.add(new Segment(nestCoor.getX() , nestCoor.getY()));
        }
        return normal;
    }
    
    public static Path NestPath2Path (NestPath nestPath ){
        Path path = new Path();
        for(Segment s : nestPath.getSegments()){
            ClipperCoor coor = CommonUtil.toClipperCoor(s.getX() , s.getY());
            Point.LongPoint lp = new Point.LongPoint(coor.getX() , coor.getY());
            path.add(lp);
        }
        return path;
    }
    
    public static ClipperCoor toClipperCoor(double x , double y ){
        return new ClipperCoor((long)(x*Config.CLIIPER_SCALE) , (long) (y * Config.CLIIPER_SCALE));
    }

    public static NestCoor toNestCoor(long x , long y ){
        return new NestCoor(((double)x/Config.CLIIPER_SCALE) , ((double)y/Config.CLIIPER_SCALE));
    }
    
    /**********************************************************************************************
    ---------------------------------------将零件以树的方式存储-----------------------------------------
    *********************************************************************************************/
    
    public static List<NestPath> clean(List<NestPath> parts,double _area,double para){
        List<NestPath> polygons = new ArrayList<NestPath>();
        for(int i = 0 ; i < parts.size();i++){
         NestPath clone = new NestPath(parts.get(i));
            NestPath cleanPoly = GeometryUtil.cleanNode(clone, _area, para);
            polygons.add(cleanPoly);
        }
        return polygons;
    }
    
    public static void offsetTree(List<NestPath> t , double offset ){
        for(int i = 0 ; i < t.size() ; i ++){
            NestPath offsetPath = polygonOffset(t.get(i) , offset);
            t.get(i).clear();
            NestPath from = offsetPath;
            for(Segment s : from.getSegments()){
                t.get(i).add(s);
            }
        }
    }
    
    public static NestPath polygonOffset(NestPath polygon , double offset){
        NestPath result = new NestPath();
        
        Path p = new Path();
        for(Segment s : polygon.getSegments()){
            ClipperCoor cc = CommonUtil.toClipperCoor(s.getX(),s.getY());
            p.add(new Point.LongPoint(cc.getX() ,cc.getY()));
        }

        int miterLimit = 2;
        ClipperOffset co = new ClipperOffset(miterLimit , Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
        co.addPath(p, Clipper.JoinType.MITER , Clipper.EndType.CLOSED_POLYGON);
        Paths newpaths = new Paths();
        co.execute(newpaths , offset * Config.CLIIPER_SCALE);
        result = CommonUtil.clipperToNestPath(newpaths.get(0));
        result.bid = polygon.bid;
        result.type = polygon.type;
        result.setRotation(polygon.rotation);
        
        if(offset > 0 ){
            NestPath from = result;
            if(GeometryUtil.polygonArea(from) > 0 ){
                from.reverse();
            }
            from.add(from.get(0));from.getSegments().remove(0);
        }
        return result;
    }
}
