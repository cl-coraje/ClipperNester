package Data;

import java.util.ArrayList;
import java.util.List;

import Clipper.*;
import Util.CommonUtil;
import Util.Config;
import Util.GeometryUtil;

public class NestPath  implements Comparable<NestPath>{
	
    public List<Segment> segments;         //vertex list
    public int rotation;                    //旋转角度
    public int[] rotations;                  //可以旋转的角度
    public Config config;                   //参数控制
    public double area;                     //零件面积
    public int bid;                         //零件标号
    public double length;                   //零件长度
    public int type;

	public int MAX_Rotation_Size = 10;
    
    public NestPath(){
        segments = new ArrayList<Segment>();
        rotations = new int[MAX_Rotation_Size];
        config = new Config();
        area = 0;
        length = 0;
    }

    public NestPath(NestPath srcNestPath){
        segments = new ArrayList<Segment>();
        for(Segment segment : srcNestPath.getSegments()){
            segments.add(new Segment(segment));
        }
        
        this.rotations = new int[MAX_Rotation_Size];
        for(int i = 0; i < MAX_Rotation_Size;i++) {
        	this.rotations[i] = srcNestPath.rotations[i];
        }
        
        this.rotation = srcNestPath.rotation;
        this.bid = srcNestPath.bid;
        this.area = srcNestPath.area;
        this.length = srcNestPath.length;
        this.type = srcNestPath.type;
        
    }

    /**********************************************************************************************
    ----------------------------------------去除自交点&减少点的个数-------------------------------------
    *********************************************************************************************/

    public static NestPath cleanNestPath(NestPath srcPath){
    	//去除自交的点
        Path path = CommonUtil.NestPath2Path(srcPath);
        Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.EVEN_ODD);
        
        Path biggest = simple.get(0);
        double biggestArea = Math.abs(biggest.area());
        for(int i = 0; i < simple.size(); i++){
            double area = Math.abs(simple.get(i).area());
            if(area > biggestArea ){
                biggest = simple.get(i);
                biggestArea = area;
            }
        }
        Path clean = biggest.cleanPolygon(srcPath.config.CURVE_DISTANCE * Config.CLIIPER_SCALE);
        if(clean.size() == 0 ){
            return null;
        }
        
        NestPath cleanPath = CommonUtil.Path2NestPath(clean);
        cleanPath.bid = srcPath.bid;
        cleanPath.setRotation(srcPath.rotation);
        return cleanPath;
    }
    
    //new
    public static NestPath cleanNest(NestPath srcPath , double Dis){
    	//去除自交的点
        Path path = CommonUtil.NestPath2Path(srcPath);
        Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.EVEN_ODD);
        
        Path biggest = simple.get(0);
        double biggestArea = Math.abs(biggest.area());
        for(int i = 0; i < simple.size(); i++){
            double area = Math.abs(simple.get(i).area());
            if(area > biggestArea ){
                biggest = simple.get(i);
                biggestArea = area;
            }
        }
        Path clean = biggest.cleanPolygon(Dis * Config.CLIIPER_SCALE);
        if(clean.size() == 0 ){
            return null;
        }
        
        NestPath cleanPath = CommonUtil.Path2NestPath(clean);
        cleanPath.bid = srcPath.bid;
        cleanPath.type = srcPath.type;
        cleanPath.setRotation(srcPath.rotation);
        return cleanPath;
    }

    /**************************************************************************************************
     ---------------------------------------------将图形坐标归零化-----------------------------------------
     **************************************************************************************************/
    public void Zerolize(){
        ZeroX();ZeroY();
    }

    private void ZeroX(){
        double xMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(xMin > s.getX() ){
                xMin = s.getX();
            }
        }
        for(Segment s :segments ){
            s.setX(s.getX() - xMin );
        }
    }
    
    private void ZeroY(){
        double yMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(yMin > s.getY() ){
                yMin = s.getY();
            }
        }
        for(Segment s : segments ){
            s.setY(s.getY() - yMin);
        }
    }
    
    /**************************************************************************************************
    ----------------------------------------------其他操作-----------------------------------------------
    **************************************************************************************************/

    @Override
    public boolean equals(Object obj) {
        NestPath nestPath = (NestPath) obj;
        if(segments.size() != nestPath.size()){
            return false;
        }
        for(int  i = 0 ; i < segments.size(); i ++){
            if(!segments.get(i).equals(nestPath.get(i))){
                return false;
            }
        }
        return true;
    }
    
    public void reverse(){
        List<Segment> rever = new ArrayList<Segment>();
        for(int i = segments.size() - 1; i >= 0; i--){
            rever.add(segments.get(i));
        }
        segments.clear();
        for(Segment s : rever ){
            segments.add(s);
        }
    }
    
    public double getMaxY(){
        double MaxY = Double.MIN_VALUE;
        for(Segment s : segments){
            if(MaxY < s.getY()){
                MaxY = s.getY();
            }
        }
        return MaxY;
    }

    public void translate(double x,  double y ){
        for(Segment s : segments){
            s.setX(s.getX() + x );
            s.setY(s.getY() + y);
        }
    }

    public void add(double x , double y ){
        this.add(new Segment(x,y));
    }
    
    public void add(Segment s){
        segments.add(s);
    }

    public void pop(){
        segments.remove(segments.size() - 1);
    }
    
    public void clear(){
        segments.clear();
    }

    public int size(){
        return segments.size();
    }
    
    /**********************************************************************************************
     ----------------------------------------get()&set()-------------------------------------------
     *********************************************************************************************/
    
    public List<Segment> getSegments() {
        return segments;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Segment get(int i){
        return segments.get(i);
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
    
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
    
    public int get_rotations_size() {
    	int count0 = 0;
    	boolean flag = false;
    	for(int i = 0; i < MAX_Rotation_Size; i++) {
    		double rotate = rotations[i];
    		if(almostEqual(rotate, 0)) {
    			flag = true;
    		}
    		if(flag == true && rotate == 0) {
    			count0++;
    		}
    	}
    	return MAX_Rotation_Size - count0 + 1;
    }
    
    public boolean almostEqual(double a, double b ){
        return Math.abs(a-b) < 1e-2;
    }
 
    /**************************************************************************************************
    ---------------------------------------------输出&从大到小排序-----------------------------------------
    **************************************************************************************************/
    
    @Override
    public String toString() {
        String res = "";
        res += "bid = "+ bid +" , rotation = "+rotation +"\n";
        int count = 0;
        for(Segment s :segments){
            res += "Segment " + count +"\n";
            count++;
            res += s.toString() +"\n";
        }
        count = 0 ;
        return res;
    }

  //按面积从大到小
  public int compareTo(NestPath o) {
      double area0  = this.area;
      double area1 = o.area;
      if(area0 < area1 ){         //从小到大（面积为负，所以是从大到小）
          return 1;
      }
      else if(area0 == area1){
          return 0;
      }
      return -1;
  }
  //按长度排序
//  public int compareTo(NestPath o) {
//      double length0 = this.length;
//      double length1 = o.length;
//      if(length0 < length1 ){            //>从大到小
//          return 1;
//      }
//      else if(length0 == length1){
//          return 0;
//      }
//      return -1;
//  }
  //设置两个优先级进行排序
//  public int compareTo(NestPath o) {
//  	double length0 = this.length;
//      double length1 = o.length;
//      double area0  = this.area;
//      double area1 = o.area;
//      if(GeometryUtil.almostEqual(area0, area1, 20)){
//      	if(length0 < length1 ){            
//              return 1;
//          }
//          else if(length0 == length1){
//              return 0;
//          }
//          return -1;
//      }else if(area0 < area1){
//          return 1;
//      }else {
//      	return -1;	
//      }
//  }
    
}
