package Algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Clipper.Clipper;
import Clipper.DefaultClipper;
import Clipper.Path;
import Clipper.Paths;
import Clipper.Point;
import Data.Bound;
import Data.NestPath;
import Data.NfpKey;
import Data.Segment;
import Data.Vector;
import Io.instance;
import Util.CommonUtil;
import Util.Config;
import Util.GeometryUtil;
import Util.coor.ClipperCoor;

public class InsertPoly {

	public instance inst;
    public NestPath binPolygon;
    public Config config;
    private static Gson gson = new GsonBuilder().create();
	
    public InsertPoly(NestPath binPolygon, Config config, instance _inst) {
        this.binPolygon = binPolygon;
        this.config = config;
        this.inst = _inst;
    }
    
    public Vector firstAdd(NestPath best_path, List<NestPath> placed, List<Vector> placements, Map<String, List<NestPath>> nfpCache) {
    	Vector position = null;
        String key = null;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        
        for(int u = 0; u < best_path.get_rotations_size(); u++) {
        	int rotate = best_path.rotations[u];
        	NestPath path = GeometryUtil.rotatePolygon2Polygon(best_path, rotate);
        	key = gson.toJson(new NfpKey(binPolygon.getType() , path.getType() , true , 0 , rotate));
    	    if(!nfpCache.containsKey(key)) {
    	    	System.out.println("未构建该零件与面板之间的NFP\t" + path.bid + "\t" + "rotate:\t" + 0);
    	    	System.exit(0);
    	    }
    	    
    	    List<NestPath> binNfp = nfpCache.get(key);

    	    for(int i = 0 ; i < binNfp.size(); i ++){
                for(int j = 0 ; j < binNfp.get(i).size() ; j ++){
              	    double off_x = binNfp.get(i).get(j).x - path.get(0).x;
              	    double off_y = binNfp.get(i).get(j).y - path.get(0).y;
              	    Vector vec = new Vector(off_x, off_y, path.bid, rotate);
              	    //第一个零件需要判断和瑕疵之间的距离
              	    if(!min_distance(vec, inst, path)){	
              		    return null;
              	    }
              	    
              	    if (off_x < minX || (almostEqual(off_x, minX) && off_y < minY)) {
              	    	minX = off_x;
              	    	minY = off_y;
                        position = new Vector(off_x, off_y, path.bid, rotate);
                    }
              	}
            }
        }
        if(position == null) {
        	System.out.println("放置第一个零件时未找到可行位置：" + best_path.bid);
        	return null;
        }
        return position;
    }
    
    public NestPath selectAdd(List<NestPath> paths, List<NestPath> placed, List<Vector> placements) {
        String key = null;
        List<NestPath> nfp = null;
        NestPath best_nest = null;         //记录最优的放入的零件
        
        double minwidth = Double.MAX_VALUE; 
        double minarea = Double.MAX_VALUE;
        double mindis = Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        Bound bound = null;
        
    	for(int i = 0; i < paths.size(); i++) {
    		
      	}
    	best_nest = paths.get(0);
    	return best_nest;
    }
    
    public Vector selectPosition(NestPath best_path, List<NestPath> placed, List<Vector> placements, Map<String, List<NestPath>> nfpCache) {
        String key = null;
        List<NestPath> nfp = null;
        Vector position = null;                          //记录放入零件最优放入位置的偏移量
        
        double minwidth = Double.MAX_VALUE; 
        double minarea = Double.MAX_VALUE;
        double mindis = Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double max_overlap_area = 0;
        double max_fitness = Double.MAX_VALUE;
        
        for(int i = 0; i < best_path.get_rotations_size(); i++) {
        	int rotate = best_path.rotations[i];
        	
        	NestPath path = GeometryUtil.rotatePolygon2Polygon(best_path, rotate);
    	    key = gson.toJson(new NfpKey(binPolygon.getType() , path.getType() , true , 0 , rotate));
    	    if(!nfpCache.containsKey(key)) {
    	    	System.out.println("未构建该零件与面板之间的NFP\t" + path.bid);
    	    	System.exit(0);
    	    }
    	    
    	    List<NestPath> binNfp = nfpCache.get(key);

            Paths clipperBinNfp = new Paths();
            for(int j = 0; j < binNfp.size(); j++){
                NestPath binNfpj = binNfp.get(j);
                clipperBinNfp.add(scaleUp2ClipperCoordinates(binNfpj));
            }
            
            DefaultClipper clipper = new DefaultClipper();
            Paths combinedNfp = new Paths();
            //将要放进去的nestpath与已经放进去的所有的nestpath构成的nfp进行位置更新 
            for(int j = 0 ; j < placed.size(); j ++){

                  key = gson.toJson(new NfpKey(placed.get(j).getType() , path.getType() , false , placed.get(j).getRotation() , rotate));
                  nfp = nfpCache.get(key); 
                  if(nfp == null ){
                      System.out.println("未成功构建零件和零件之间的NFP：" + placed.get(j).bid + "\t" + path.bid);
                      System.exit(0);
                  }
                  //将要放进来的零件与已经放进去的零件构成的nfp将nfp的位置进行移动
                  for(int k = 0 ; k < nfp.size(); k++){
                      Path clone = scaleUp2ClipperCoordinates(nfp.get(k));
                      for(int m = 0 ; m < clone.size() ; m++){
                          long clx = clone.get(m).getX();
                          long cly = clone.get(m).getY();
                          clone.get(m).setX(clx + (long)(placements.get(j).x * Config.CLIIPER_SCALE));
                          clone.get(m).setY(cly + (long)(placements.get(j).y * Config.CLIIPER_SCALE));
                      }
                      
                      clone = clone.cleanPolygon(0.0001 * Config.CLIIPER_SCALE);                        //减少点集
                      double area = Math.abs(clone.area());
                      if(clone.size() > 2 && area > 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE){ //是有效的nfp
                          clipper.addPath(clone , Clipper.PolyType.SUBJECT , true);                     //被裁剪
                      }
                  }
              }
              if(!clipper.execute(Clipper.ClipType.UNION, combinedNfp, Clipper.PolyFillType.NON_ZERO , Clipper.PolyFillType.NON_ZERO)){
            	  System.out.println("未成功构建与已放置零件的并集：\t" + path.bid);
            	  return null;
              }
              
              Paths finalNfp = new Paths();
              clipper = new DefaultClipper();
              clipper.addPaths(combinedNfp , Clipper.PolyType.CLIP , true);
              clipper.addPaths(clipperBinNfp , Clipper.PolyType.SUBJECT , true);
              if(!clipper.execute(Clipper.ClipType.DIFFERENCE , finalNfp , Clipper.PolyFillType.NON_ZERO , Clipper.PolyFillType.NON_ZERO)){
            	  System.out.println("未成功构建与面板之间的交集：\t" + path.bid);
            	  return null;
              }
       
              finalNfp = finalNfp.cleanPolygons(0.0001 * Config.CLIIPER_SCALE);
              for(int j = 0 ; j < finalNfp.size() ; j ++){
                  double area = Math.abs(finalNfp.get(j).area());
                  if(finalNfp.get(j).size() < 3 || area < 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE){
                      finalNfp.remove(j);
                      j--;
                  }
              }
              if(finalNfp == null || finalNfp.size() == 0 ){
            	  System.out.println("无可行的NFP：\t" + path.bid);
            	  return null;
              }
              
              //将clipper转化为NestPath
              List<NestPath> f = new ArrayList<NestPath>();
              for(int j = 0 ; j < finalNfp.size() ; j ++){
                  f.add(toNestCoordinates(finalNfp.get(j)));
              }
              List<NestPath> finalNfpf = f;
              
              double area = 0;
              double center_dis = 0;
              
              NestPath nf = null;
              Vector shifvector = null;
              
              for(int j = 0 ; j < finalNfpf.size() ; j ++){
                  nf = finalNfpf.get(j);
                  if(Math.abs( GeometryUtil.polygonArea(nf)) < 1){
                      continue;
                  }
                  for(int k = 0 ; k < nf.size() ; k ++){
                  	double off_x = nf.get(k).x - path.get(0).x;
                  	double off_y = nf.get(k).y - path.get(0).y;  
                  	
                  	Vector vec = new Vector(off_x, off_y, path.bid, rotate);
              	    //第一个零件需要判断和瑕疵之间的距离
              	    if(!min_distance(vec, inst, path)){	
              		    continue;
              	    }
              	    
                  	NestPath allpoints = new NestPath();
                      for(int m = 0; m < placed.size(); m++){
                          for(int n = 0 ; n < placed.get(m).size(); n++){
                              allpoints.add(new Segment(placed.get(m).get(n).x + placements.get(m).x ,
                              		                  placed.get(m).get(n).y +placements.get(m).y));
                          }
                      }
                      Bound bound1 = GeometryUtil.getPolygonBounds(allpoints);
                      double meanx = bound1.getXmin() + bound1.getWidth() / 2;
                      double meany = bound1.getYmin() + bound1.getHeight() / 2;
                      double area1 = bound1.getWidth() * bound1.getHeight();
                      
                      NestPath now = new NestPath();
                      shifvector = new Vector(off_x, off_y,path.bid, rotate,combinedNfp);
                      for(int m = 0 ;m < path.size(); m++){
                          allpoints.add(new Segment(path.get(m).x + shifvector.x , path.get(m).y + shifvector.y));
                          now.add(path.get(m).x + shifvector.x , path.get(m).y + shifvector.y);
                      }
                      
                      Bound rectBounds = GeometryUtil.getPolygonBounds(allpoints);
                      area = rectBounds.getWidth() * 2 + rectBounds.getHeight();
                      double combined_area = rectBounds.getWidth() * rectBounds.getHeight();
                      
                      Bound bound2 = GeometryUtil.getPolygonBounds(now);
                      double newmeanx = bound2.getXmin() + bound2.getWidth() / 2;
                      double newmeany = bound2.getYmin() + bound2.getHeight() / 2;
                      double area2 = bound2.getWidth() * bound2.getHeight();
                      
                      center_dis = (newmeanx - meanx)*(newmeanx - meanx) + (newmeany - meany)*(newmeany - meany);
                      
                      double overlap_area = Math.max(area1 + area2 - combined_area, 0);
                      
                      double fitness = overlap_area / (bound2.getXmin() + bound2.getWidth());
                      
                      if(max_fitness == Double.MAX_VALUE  || fitness > max_fitness || (almostEqual(fitness, 0) && shifvector.x < minX)) {
                          minarea = area;
                          minwidth = rectBounds.getWidth();
                          position = shifvector;
                          minX = shifvector.x;  
                          mindis = center_dis;
                          max_fitness = fitness;
                      }
                      
//                      if(area < minarea || (GeometryUtil.almostEqual(minarea,area) && (shifvector.x < minX))) {
//                    //  	&& center_dis < mindis){
//                          minarea = area;
//                          minwidth = rectBounds.getWidth();
//                          position = shifvector;
//                          minX = shifvector.x;  
//                          mindis = center_dis;
//                          best_rotation = rotate;
//                      }
                      
//                      if(overlap_area > max_overlap_area || (almostEqual(overlap_area, max_overlap_area) && (shifvector.x < minX))
//                          	){
//                  	        max_overlap_area = overlap_area;
//                              minarea = area;
//                              minwidth = rectBounds.getWidth();
//                              position = shifvector;
//                              minX = shifvector.x;
//                              mindis = center_dis;
//                              best_rotation = rotate;
//                          }
                      
//                      if(shifvector.x < minX || (almostEqual(shifvector.x, minX) && shifvector.y < minY))
//                         {
//                                minarea = area;
//                                minwidth = rectBounds.getWidth();
//                                position = shifvector;
//                                minX = shifvector.x;
//                                  minY = shifvector.y;
//                                mindis = center_dis;
//                                best_rotation = rotate;
//                            }
                  }
              }
            }
              
              if(position == null) {
            	  System.out.println("未找到符合条件的位置：" + best_path.bid);
            	  return null;
              }
                            
              return position;
    }
    
    public static boolean almostEqual(double a, double b ){
        return Math.abs(a-b) < 1e-2;
    }
    
    /*******************************************************************************************************
    -------------------------------------------判断检验最小距离----------------------------------------------
    *******************************************************************************************************/
    
    public  boolean min_distance(Vector vec, instance _inst, NestPath _nest) {	
    	NestPath nest = getnest(_nest.bid,_inst.nestpath);
    	nest = rotatePolygon2Polygon(nest, vec.rotation);
    	for(int i = 0 ;i < nest.size();i++) {
    		nest.get(i).setX(nest.get(i).x + vec.x - 0.5 * config.SPACING + inst.minside);
    		nest.get(i).setY(nest.get(i).y + vec.y - 0.5 * config.SPACING + inst.minside);
    	}
    	
    	if(!nodeMinDis(nest,_inst)) {
    		//System.out.println(nest.bid + "零件与瑕疵之间距离过近");
    		return false;	
    	}
    	return true;
    }
  //零件与瑕疵点之间的最小距离
    public Boolean nodeMinDis(NestPath A, instance _inst) {
    	Segment[] r = new Segment[_inst.r_x.length];
     	for(int i = 0; i < _inst.r_x.length; i++) {
    		r[i] = new Segment(_inst.r_x[i] ,_inst.r_y[i]);
    	}
    	double t_shortest_square_dis = Double.MAX_VALUE;
    	
    	for(int i = 0; i < r.length; i++) {
    		if(GeometryUtil.pointInPolygon(r[i],A))
    			return false;
			int prev = A.size() - 1;
			int cur = 0;
			while(prev != cur) {
				Segment seg_prev = A.get(prev);
				Segment seg_cur = A.get(cur);
				double dis = distance_segment(r[i], seg_prev, seg_cur);
				if(Math.sqrt(dis) - _inst.r[i]  - inst.linside < 1e-6) 
					return false;
				if(dis < t_shortest_square_dis) {
					t_shortest_square_dis = dis;
				}
				prev = cur++;
				if(cur == A.size())
					break;
				}
    	}
    	return true;
    }
    
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
    
    /**
     * 根据bid来选择获取原始的NestPath
     * @param bid
     * @param _nest
     * @return
     */
    
    public NestPath getnest(int bid ,ArrayList<NestPath> _nest) {
    	NestPath n = new NestPath();
    	for(NestPath n1 : _nest) {
    		if(n1.bid == bid) {
    			n = new NestPath(n1);
    			break;
    		}
    	}
    return n;
    }
    
    /**
     * 消除自交的情况
     */
    
    public NestPath clean(NestPath srcPath) {
    	Path path = CommonUtil.NestPath2Path(srcPath);
        Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.EVEN_ODD);
        NestPath cleanPath = CommonUtil.Path2NestPath(simple.get(0));
        cleanPath.bid = srcPath.bid;
        cleanPath.setRotation(srcPath.rotation);
        return cleanPath;
    }
    
    /**
     * 旋转后的多边形
     * @param polygon
     * @param degrees
     * @return
     */
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
   
    /******************************************************************************************
    -----------------------------------将所有的板件坐标归零-------------------------------------
    *****************************************************************************************/
    
    public void zero(NestPath binPolygon) {
    	 double xbinmax = binPolygon.get(0).x;
         double xbinmin = binPolygon.get(0).x;
         double ybinmax = binPolygon.get(0).y;
         double ybinmin = binPolygon.get(0).y;

         for(int i = 1 ; i<binPolygon.size(); i ++){
             if(binPolygon.get(i).x > xbinmax ){
                 xbinmax = binPolygon.get(i).x;
             }
             else if (binPolygon.get(i).x < xbinmin ){
                 xbinmin = binPolygon.get(i) .x;
             }

             if(binPolygon.get(i).y > ybinmax ){
                 ybinmax = binPolygon.get(i).y;
             }
             else if (binPolygon.get(i). y <ybinmin ){
                 ybinmin = binPolygon.get(i).y;
             }
         }
         for(int i=0; i<binPolygon.size(); i++){
             binPolygon.get(i).x -= xbinmin;
             binPolygon.get(i).y -= ybinmin;
         }

         if(GeometryUtil.polygonArea(binPolygon) > 0 ){
             binPolygon.reverse();
         }
    }
    
    
    /*******************************************************************************************************
    -------------------------------------------与库交互必须坐标转换----------------------------------------------
    *******************************************************************************************************/
   public static Path scaleUp2ClipperCoordinates(NestPath polygon){
       Path p = new Path();
       for(Segment s : polygon.getSegments()){
           ClipperCoor cc = CommonUtil.toClipperCoor(s.x , s.y);
           p.add(new Point.LongPoint(cc.getX() , cc.getY()));
       }
       return p;
   }
   public static NestPath toNestCoordinates(Path polygon){
       NestPath clone = new NestPath();
       for(int i = 0 ; i< polygon.size() ; i ++){
           Segment s = new Segment((double)polygon.get(i).getX()/Config.CLIIPER_SCALE , (double)polygon.get(i).getY()/Config.CLIIPER_SCALE);
           clone.add(s);
       }
       return clone ;
   }


}
