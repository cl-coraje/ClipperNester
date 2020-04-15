package Algorithm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import Clipper.Clipper;
import Clipper.DefaultClipper;
import Clipper.Path;
import Clipper.Paths;
import Data.*;
import Data.Vector;
import Io.instance;
import Io.polygon_test;
import Util.*;

import java.util.*;

public class Nest {
	
	public instance inst;
    private  NestPath binPath;
    private  List<NestPath> parts;
    private  Config config;
    private Map<String,List<NestPath>> nfpCache;
    private static Gson gson = new GsonBuilder().create();
    public Random rand;

    public Nest(NestPath binPath, List<NestPath> parts, Config config,instance _inst) {
        this.binPath = binPath;
        this.parts = parts;
        this.config = config;
        this.inst = _inst;
        nfpCache = new HashMap<String, List<NestPath>>();
        rand = new Random(1);
    }   
    
    public  List<Placement> startNest(){
    	
    	//记录原始的零件序列
        List<NestPath> part1 = new ArrayList<NestPath>();
        for(int i = 0; i < parts.size(); i++) {
         part1.add(new NestPath(parts.get(i)));
        }
        List<NestPath> part2 = new ArrayList<NestPath>();
        
        List<NestPath> tree = new ArrayList<NestPath>();
           double para = 0.02;
           while(part1.size() > 0) {
            List<NestPath> part = CommonUtil.clean(part1,inst.sumarea/inst.nestpath.size()/5000,para);
            CommonUtil.offsetTree(part , 0.5 * config.SPACING);
            for(int i = 0; i < part.size(); i++) {
             if(!lingMinDis(part1.get(i),part.get(i))) {
              part2.add(part1.get(i)); 
             }
             else {
              tree.add(part.get(i)); 
             }
            }
            if(part2.size() == 0)
             break;
            part1.clear();
            for(int i = 0; i < part2.size(); i++) {
             part1.add(part2.get(i));
            }
            part2.clear();
            para += 0.01;
           }
           
           int num = 0;
           int sizenum = 0;
          for(int i = 0 ; i < tree.size(); i++) {
        	  if(tree.get(i).size() > 4) {
        		  sizenum += tree.get(i).size();
        	  }
        	  num++;
          }
          System.out.println(sizenum/num);
        
        binPath.config = config;
        binPath.config.SPACING = inst.minside;
        NestPath binPolygon = new NestPath(binPath);
        NestPath offsetBin = CommonUtil.polygonOffset(binPolygon , 0.5 * config.SPACING - binPath.config.SPACING);
        binPolygon = offsetBin;
        binPolygon.bid = 0;
        binPolygon.setType(0);
        zero(binPolygon);

        for(NestPath nestPath: parts){
            nestPath.config = config;
        }
        
        for(int i = 0 ; i < tree.size(); i ++){
            if(GeometryUtil.polygonArea(tree.get(i)) > 0 ){
                tree.get(i).reverse();
            }
        }
        
        List<NestPath> adam = new ArrayList<NestPath>();
        for(NestPath nestPath : tree ){
            NestPath clone  = new NestPath(nestPath);
            adam.add(clone);
        }
        
        //面积从大到小
//        for(int i = 0 ;i < adam.size();i++) {
//        	adam.get(i).area = GeometryUtil.polygonArea4(adam.get(i));
//        }
          for(int i = 0 ;i < adam.size();i++) {//选择初始排序顺序
    	      if(inst.sort_selection == 1) {
    		      adam.get(i).area = Math.abs(GeometryUtil.polygonArea(adam.get(i)));
    		      }
    	      if(inst.sort_selection == 2) {
    		      adam.get(i).area = GeometryUtil.polygonArea4(adam.get(i));
    	      }
          }
        Collections.sort(adam);
        ArrayList<NestPath> list = new ArrayList<NestPath>();
        for(int i = 0; i < adam.size();i++) {
        	list.add(adam.get(i));
        	adam.remove(i);
        	i++;
        }
        
        for(int i = 0; i < list.size();i++) {
        	adam.add(list.get(i));
        }
        
        
        double t1 = System.nanoTime() / 1e9;
        BuildAllRotateNfp(adam, binPolygon, config);
        double t2 = System.nanoTime() / 1e9;
        System.out.println("构建所有NFP时间：" + (t2 - t1) + "\t" + "NFP总数：" + nfpCache.size());
        
        //构建初始解
        Result best = null;
        Result result = null;
        double t3 = System.nanoTime() / 1e9;
        result = launchWorkers(adam, binPolygon, config);
        double t4 = System.nanoTime() / 1e9;
        System.out.println(inst.pid + "构建初始解的时间：" + (t4 - t3) + "\t" + "初始解的利用率：" + result.fitness);
        best = result;
      
        boolean [][]fre = new boolean[inst.nestpath.get(inst.nestpath.size() - 1).type][inst.nestpath.get(inst.nestpath.size() - 1).type];
        
      int iter = 0;
      while(iter < inst.iter_number) {
      	double t5 = System.nanoTime() / 1e9;
      	List<NestPath> newpaths = random_exchange(adam, fre);
      	result = launchWorkers(newpaths, binPolygon, config); //初始解
      	double t6 = System.nanoTime() / 1e9;
          System.out.println(iter + "\t" + inst.pid + "交换一次解的时间：" + (t6 - t5) + "\t" + "当前解的利用率：" + result.fitness);
          
          if(result.fitness > best.fitness) {
          	best = result;
          }
          
      	iter++;
      }
      
        List<Placement> best_appliedPlacement = applyPlacement(best);
        return best_appliedPlacement;
    }
    
  /********************************************************************************************
   ------------------------------------------得到一个解------------------------------------------
   ********************************************************************************************/
	public Result launchWorkers(List<NestPath> adam ,NestPath binPolygon ,Config config){
        ArrayList<NestPath> polygons = new ArrayList<NestPath>();            //记录所有零件
        for(NestPath nest:adam) {
        	polygons.add(new NestPath(nest));
        }
        
        int iter = 0;
        NestPath best_path = null;                                           //记录最好的零件
        Vector best_vec = null;                                              //记录零件最好的放置位置
        List<NestPath> placed = new ArrayList<NestPath>();                   //记录放入的零件
        List<Vector> placements = new ArrayList<Vector>();                   //记录放入零件的偏移位置
        List<NestPath> newpath = new ArrayList<NestPath>();
        InsertPoly insert = new InsertPoly(binPolygon,config,inst);
        
        //先放瑕疵点
        for(int i = 0; i < polygons.size(); i++) {
        	if(polygons.get(i).bid < 0) {
        		placed.add(polygons.get(i));
        		Vector v = new Vector(0, 0, polygons.get(i).bid, 0);
        		polygons.get(i).setRotation(0);
        		placements.add(v);
        		polygons.remove(i);
        		i--;
        	}
        }
        
        while(polygons.size() > 0) {
          //放置第一个零件，放置面积最大的零件，（第一个零件需要判断是否满足瑕疵距离）
      	  if(iter == 0) {
      		  for(int i = 0; i < polygons.size(); i++) {
          		  best_path = polygons.get(i);
//      			  BuildPathBinNfp(best_path, binPolygon, config);		  
          		  best_vec = insert.firstAdd(best_path, placed, placements, nfpCache);
          		  if(best_vec != null) {
//          			 System.out.println("第一个零件：" + best_path.bid + "\t" + best_path.type + "\t" + "旋转角度：" +best_vec.rotation);
          			 placed.clear();
          			 placements.clear();
          			 break;
          		  }
      		  }
      	  }
      	  else {
      		  best_path = insert.selectAdd(polygons, placed, placements);//选择插入的零件
//      		  System.out.print("插入零件：" + best_path.bid + "\t" + "零件类型：" + best_path.type + "\t");
//      		  BuildPathBinNfp(best_path, binPolygon,config);
//      		  for(NestPath place:placed) {
//      			  BuildPath2PathNfp2(best_path, place, config);
//      		  }	  
      		  best_vec = insert.selectPosition(best_path, placed, placements, nfpCache);//选择插入位置 
//      		  System.out.print("旋转角度：" + best_vec.rotation);
//      		  System.out.println();
      		  
      		  if(!insert.min_distance(best_vec, inst, best_path)) {
      			  best_vec = null;
      		  }
      		  
      		  if(best_vec == null) {// 未找到合适位置，则暂时将该零件从polygons中移除，直到下一个有可加入位置再加入polygons
      			  System.out.println("溢出零件：" + best_path.bid);
      	      	  for(int i = 0; i < polygons.size(); i++) {
      	      		  if(polygons.get(i).bid == best_path.bid) {
      	      			  polygons.remove(i);
      	      		  }
      	      	  }
      	      	  newpath.add(best_path);
      	      	  //若polygons.size()=0，而还有未放入的零件在newpath里如何处理
      	      	  
      			  continue;
      		  }
      		  else {
      			  if(newpath.size() > 0) {
      				  for(int j = newpath.size() - 1; j >= 0; j--) {//还是按照原来的顺序排列（如面积从大到小）
      				    NestPath nest = newpath.get(j);
      					polygons.add(0, nest);
      				  }  
      			  }
      			  newpath.clear();
      		  }
      	  }
      	  
      	  best_path.setRotation(best_vec.rotation);
  		  placed.add(best_path);
  		  placements.add(best_vec);
      	  
      	  for(int i = 0; i < polygons.size(); i++) {
      		  if(polygons.get(i).bid == best_path.bid) {
      			  polygons.remove(i);
      		  }
      	  }
      	  iter++;
        }
        
        
        double binarea = Math.abs(GeometryUtil.polygonArea(binPolygon)); 
        double fitness = 0;
        Result result = new Result(placements, fitness, placed, binarea);
        List<Placement> appliedPlacement = applyPlacement(result);
        fitness = Utilization(appliedPlacement);
        result.fitness = fitness;
        return result;
    }
	
    public static boolean exchange_condition(double a, double b ){
        return Math.abs(a-b) > 10000;
    }
	
	/********************************************************************************************
	 -------------------------------------------随机交换的过程---------------------------------------
	 ********************************************************************************************/
    public List<NestPath> random_exchange(List<NestPath> paths, boolean[][] fre) {
    	int i = 0;
    	int j = 0;
    	while(true) {
    		i = rand.nextInt(paths.size());
    		NestPath p1 = paths.get(i);
    		if(p1.bid > 0) {
            	j = rand.nextInt(paths.size());
            	NestPath p2 = paths.get(j);
            	if(p1.type != p2.type && p2.bid > 0 && fre[p1.type][p2.type] == false) {
            		//	&& exchange_condition(p1.area, p2.area)) {
            		fre[p1.type][p2.type] = fre[p2.type][p1.type] = true;
            		break;
            	}
//            	if(p1.type != p2.type ) {
//            		fre[p1.type][p2.type]++;
//            		fre[p2.type][p1.type]++;
//            		break;
//            		}
            	}
    		}
    	
    	
    	System.out.println("交换多边形：" + paths.get(i).bid + "\t" + paths.get(j).bid + "\t" + paths.get(i).area + "\t" + paths.get(j).area);
    
    	//将中间段的倒序排列
    	List<NestPath> newpaths1 = new ArrayList<NestPath>();
    	for(int k = Math.min(i, j); k <= Math.max(i, j); k++) {
    		newpaths1.add(paths.get(k));
    	}
    	
    	List<NestPath> newpaths2 = new ArrayList<NestPath>();
    	for(int k = 0; k < Math.min(i, j); k++) {
    		newpaths2.add(paths.get(k));
    	}
    	for(int k = newpaths1.size() - 1 ; k >= 0 ; k--){
    		newpaths2.add(newpaths1.get(k));
    	}
    	for(int k = Math.max(i, j); k < paths.size(); k++) {
    		newpaths2.add(paths.get(k));
    	}
    	
    	return newpaths2;
    	
//    	List<NestPath> newpaths = new ArrayList<NestPath>();
//    	for(int p = 0; p < paths.size(); p++) {
//    		if(p == i) {
//    			newpaths.add(new NestPath(paths.get(j)));
//    		}
//    		else if(p == j) {
//    			newpaths.add(new NestPath(paths.get(i)));
//    		}
//    		else
//    			newpaths.add(new NestPath(paths.get(p)));	
//    	}
//    	return newpaths;
    }
    
    /********************************************************************************************
     ***************************************构建NFP**********************************************
     ********************************************************************************************/
    
    /**
     * *构建零件和bin之间的nfp
     * @param path
     * @param binPolygon
     * @param config
     */
    public void BuildPathBinNfp(NestPath path,NestPath binPolygon ,Config config ){
    	List<NfpPair> nfpPairs = new ArrayList<NfpPair>();
    	NfpKey key = null;
        for(int i = 0; i < path.get_rotations_size(); i++) {
        	int rotate = path.rotations[i];
        	key = new NfpKey(binPolygon.getType() , path.getType() , true , 0 , rotate);
        	String test_key = gson.toJson(new NfpKey(binPolygon.getType() , path.getType() , true , 0 , rotate));
        	if(!nfpCache.containsKey(test_key)) {
        		nfpPairs.add(new NfpPair(binPolygon , path , key));
        	}
        }
        
        List<ParallelData> generatedNfp = new ArrayList<ParallelData>();
        for(NfpPair nfpPair : nfpPairs){
            ParallelData data = NfpUtil.nfpGenerator(nfpPair,config);
            generatedNfp.add(data);
        }
        //再将ParallelData存储转变为hashMap存储
        for(int i = 0 ; i < generatedNfp.size() ; i++){
            ParallelData Nfp = generatedNfp.get(i);
            String tkey = gson.toJson(Nfp.getKey());
            nfpCache.put(tkey , Nfp.value);
        }
    }
    
    /**
     * *构建零件与确定零件之间的nfp
     * @param path
     * @param binPolygon
     * @param config
     */
	public void BuildPath2PathNfp(NestPath path, NestPath place ,Config config ){
        NfpKey key = null;
        List<NfpPair> nfpPairs = new ArrayList<NfpPair>();
        for(int i = 0; i < path.get_rotations_size(); i++) {
        	int rotate = path.rotations[i];
        	key = new NfpKey(place.getType() , path.getType() , false , place.getRotation() , rotate);
        	String test_key = gson.toJson(new NfpKey(place.getType() , path.getType() , false , place.getRotation() , rotate));
        	if(!nfpCache.containsKey(test_key)) {
        		nfpPairs.add(new NfpPair(place , path , key));
        	}
        }
        
        List<ParallelData> generatedNfp = new ArrayList<ParallelData>();
        for(NfpPair nfpPair : nfpPairs){
            ParallelData data = NfpUtil.nfpGenerator(nfpPair,config);
            generatedNfp.add(data);
        }
        //再将ParallelData存储转变为hashMap存储
        for(int i = 0 ; i < generatedNfp.size() ; i++){
            ParallelData Nfp = generatedNfp.get(i);
            String tkey = gson.toJson(Nfp.getKey());
            nfpCache.put(tkey , Nfp.value);
        }
    }
	
	/**
     * *构建零件与确定零件之间的nfp(考虑0-0，180-180相同的情况)
     * @param path
     * @param binPolygon
     * @param config
     */
	public void BuildPath2PathNfp2(NestPath path, NestPath place ,Config config ){
        NfpKey key = null;
        List<NfpPair> nfpPairs = new ArrayList<NfpPair>();
        for(int i = 0; i < path.get_rotations_size(); i++) {
        	int rotate = path.rotations[i];
        	key = new NfpKey(place.getType() , path.getType(), false, place.getRotation() , rotate);
        	String test_key1 = gson.toJson(new NfpKey(place.getType(), path.getType(), false , place.getRotation() , rotate));
        	//存储对应角度的nfpkey值
        	String test_key2 = gson.toJson(new NfpKey(place.getType(), path.getType(), false , (180 - place.getRotation()), (180 - rotate)));
        	if(!nfpCache.containsKey(test_key1) && !nfpCache.containsKey(test_key2)) {
            NfpPair nfpPair = new NfpPair(place, path, key);
            ParallelData data = NfpUtil.nfpGenerator(nfpPair,config);
            nfpCache.put(test_key1, data.value);
//        		nfpPairs.add(new NfpPair(place , path , key));
//        		System.out.println("构建nfp" + "\t" + place.type + "\t" + path.getType() + "\t" +place.getRotation() + "\t" + rotate);
        	}
        	if(!nfpCache.containsKey(test_key1) && nfpCache.containsKey(test_key2)) {
        		List<NestPath> nfps = nfpCache.get(test_key2);
        		List<NestPath> copy_nfps = new ArrayList<NestPath>();  
        		for(NestPath nfp:nfps) {
        			copy_nfps.add(new NestPath(nfp));
        		}
        		for(NestPath nfp:copy_nfps) {
        			for(int k = 0; k < nfp.size(); k++) {
        				nfp.get(k).x = -nfp.get(k).x;
        				nfp.get(k).y = -nfp.get(k).y;
        			}
        		}
        		nfpCache.put(test_key1, copy_nfps);
        	}
        	
        }
       
    }
	
	/**
	 * 构建所有的NFP
	 * @param adam
	 * @param binPolygon
	 * @param config
	 */
    public void BuildAllNfp(List<NestPath> adam ,NestPath binPolygon ,Config config ){
    	List<NestPath> placelist = new ArrayList<NestPath>();
        
        for(int i = 0 ; i < adam.size(); i++){
      	    placelist.add(new NestPath(adam.get(i)));
            placelist.get(i).setRotation(0);//都设为不旋转
        }
          
          List<NfpPair> nfpPairs = new ArrayList<NfpPair>();
          NfpKey key = null;

          for(int i = 0 ; i < placelist.size();i++){
              NestPath part = placelist.get(i);           
              
              if(part.bid > 0) {                                 // 瑕疵是固定的，和bin之间不需要nfp
                  key = new NfpKey(binPolygon.getType() , part.getType() , true , 0 , part.getRotation());
                  nfpPairs.add(new NfpPair(binPolygon,part,key));
              }
              for(int j = 0 ; j < placelist.size() ; j ++){
                  NestPath placed = placelist.get(j);
                  if(part.bid > 0 && placed.bid > 0 && i != j) { //零件与零件之间构建nfp
                  NfpKey keyed = new NfpKey(placed.getType() , part.getType() , false , placed.getRotation(), part.getRotation());
                  nfpPairs.add( new NfpPair(placed , part , keyed));
                  }
              }
          }
          //其他polygon与疵点构成nfp 疵点只能是nfp中不动的polygon,所有点都需要与疵点建立nfp
          for(int i = 0; i < placelist.size(); i++) {
          	if(placelist.get(i).bid < 0) {
          		for(int j = 0; j < placelist.size(); j++) {
          			if(placelist.get(j).bid > 0) {
          				NfpKey k = new NfpKey(placelist.get(i).getType(), placelist.get(j).getType(), false, placelist.get(i).getRotation(), placelist.get(j).getRotation());
          			    nfpPairs.add(new NfpPair(placelist.get(i), placelist.get(j), k));
          			}
          		}
          	}
          }
          //存储着nfpKey对应的nfp链表 
          System.out.println("nfp number："+ nfpPairs.size());
          List<ParallelData> generatedNfp = new ArrayList<ParallelData>();
          for(NfpPair nfpPair : nfpPairs){
              ParallelData data = NfpUtil.nfpGenerator(nfpPair,config);
              generatedNfp.add(data);
          }
          //再将ParallelData存储转变为hashMap存储
          for(int i = 0 ; i < generatedNfp.size() ; i++){
              ParallelData Nfp = generatedNfp.get(i);
              String tkey = gson.toJson(Nfp.getKey());
              nfpCache.put(tkey , Nfp.value);
          }
    }
    
	/**
	 * 构建所有的NFP(考虑旋转角度)
	 * @param adam
	 * @param binPolygon
	 * @param config
	 */
    public void BuildAllRotateNfp(List<NestPath> adam ,NestPath binPolygon ,Config config ){
    	
    	List<NestPath> placelist = new ArrayList<NestPath>();
        
        for(int i = 0 ; i < adam.size(); i++){
      	    placelist.add(new NestPath(adam.get(i)));
        }
          
          NfpKey key = null;
          NfpPair nfpPair = null;
          ParallelData data = null;
          String test_key1 = null;
          String test_key2 = null;
          

          for(int i = 0 ; i < placelist.size();i++){
              NestPath part = placelist.get(i);           
              for(int r1 = 0; r1 < part.get_rotations_size(); r1++) {
            	  int part_rotate = part.rotations[r1];
            	  if(part.bid > 0) { // 瑕疵是固定的，和bin之间不需要nfp
                      key = new NfpKey(binPolygon.getType() , part.getType() , true , 0 , part_rotate);
                      nfpPair = new NfpPair(binPolygon,part,key);
                      data = NfpUtil.nfpGenerator(nfpPair,config);
                      String tkey = gson.toJson(key);
                      nfpCache.put(tkey , data.value);
                  }
            	  
                  for(int j = 0 ; j < i ; j ++){ 	  
                      NestPath placed = placelist.get(j);
                	  for(int r2 = 0; r2 < placed.get_rotations_size(); r2++) {
                		  int placed_rotate = placed.rotations[r2];
                          if(part.bid > 0 && placed.bid > 0 && i != j) { //零件与零件之间构建nfp
                              key = new NfpKey(placed.getType() , part.getType() , false , placed_rotate, part_rotate);
                          	  test_key1 = gson.toJson(new NfpKey(placed.getType(), part.getType(), false , placed_rotate, part_rotate));
                          	  test_key2 = gson.toJson(new NfpKey(placed.getType(), part.getType(), false , (180 - placed_rotate), (180 - part_rotate)));
                          	  if(!nfpCache.containsKey(test_key1) && !nfpCache.containsKey(test_key2)) {
                                  nfpPair = new NfpPair(placed, part, key);
                                  data = NfpUtil.nfpGenerator(nfpPair,config);
                                  nfpCache.put(test_key1, data.value);
                          	  }
                          	  if(!nfpCache.containsKey(test_key1) && nfpCache.containsKey(test_key2)) {
                          		  List<NestPath> nfps = nfpCache.get(test_key2);
                          		  List<NestPath> copy_nfps = new ArrayList<NestPath>();  
                          		  for(NestPath nfp:nfps) {
                          			  copy_nfps.add(new NestPath(nfp));
                          		  }
                          		  for(NestPath nfp:copy_nfps) {
                          			  for(int k = 0; k < nfp.size(); k++) {
                          				  nfp.get(k).x = -nfp.get(k).x;
                          				  nfp.get(k).y = -nfp.get(k).y;
                          			  }
                          		  }
                          		  nfpCache.put(test_key1, copy_nfps);
                          	  }
                              
                              key = new NfpKey(part.getType(), placed.getType(), false, part_rotate , placed_rotate);
                          	  test_key1 = gson.toJson(new NfpKey(part.getType(), placed.getType(), false , part_rotate, placed_rotate));
                          	  test_key2 = gson.toJson(new NfpKey(part.getType(), placed.getType(), false , (180 - part_rotate), (180 - placed_rotate)));
                          	  if(!nfpCache.containsKey(test_key1) && !nfpCache.containsKey(test_key2)) {
                                  nfpPair = new NfpPair(part, placed, key);
                                  data = NfpUtil.nfpGenerator(nfpPair,config);
                                  nfpCache.put(test_key1, data.value);
                          	  }
                          	  if(!nfpCache.containsKey(test_key1) && nfpCache.containsKey(test_key2)) {
                          		  List<NestPath> nfps = nfpCache.get(test_key2);
                          		  List<NestPath> copy_nfps = new ArrayList<NestPath>();  
                          		  for(NestPath nfp:nfps) {
                          			  copy_nfps.add(new NestPath(nfp));
                          		  }
                          		  for(NestPath nfp:copy_nfps) {
                          			  for(int k = 0; k < nfp.size(); k++) {
                          				  nfp.get(k).x = -nfp.get(k).x;
                          				  nfp.get(k).y = -nfp.get(k).y;
                          			  }
                          		  }
                          		  nfpCache.put(test_key1, copy_nfps);
                          	  }
                          }
                	  }
                  }
              }
              
              
          }
          
          //其他polygon与疵点构成nfp 疵点只能是nfp中不动的polygon,所有点都需要与疵点建立nfp
          for(int i = 0; i < placelist.size(); i++) {
          	if(placelist.get(i).bid < 0) {
          		for(int j = 0; j < placelist.size(); j++) {
          			if(placelist.get(j).bid > 0) {
          				for(int r = 0; r < placelist.get(j).get_rotations_size(); r++) {
          					int rotate = placelist.get(j).rotations[r];
          					key = new NfpKey(placelist.get(i).getType(), placelist.get(j).getType(), false, 0, rotate);
              			    nfpPair = new NfpPair(placelist.get(i), placelist.get(j), key);
              			    data = NfpUtil.nfpGenerator(nfpPair,config);
              			    nfpCache.put(gson.toJson(key), data.value);
          				}	
          			}
          		}
          	}
          }
          
          System.out.println("nfp number："+ nfpCache.size());
          
    }
    

    /************************************************************************************************************
    --------------------------------------将translate和rotate绑定到对应板件上-----------------------------------------
    ************************************************************************************************************/
    public static List<Placement> applyPlacement(Result best){
        List<Placement> applyPlacement = new ArrayList<Placement>();
        for(int i = 0; i < best.placements.size();i++){
            Vector v = best.placements.get(i);
            NestPath nestPath = best.paths.get(i);
            Placement placement = new Placement(nestPath.bid , new Segment(v.x,v.y) , v.rotation);
            applyPlacement.add(placement);
        }
        return applyPlacement;
    }
    
    /************************************************************************************************************
    -------------------------------------------------将所有的板件坐标归零---------------------------------------------
    ************************************************************************************************************/
    
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

    /******************************************************************************************
     ----------------------------------------------利用率计算-----------------------------------
     *****************************************************************************************/
    
  	public double Utilization(List<Placement> _placement) {
  	     double[] tot = Utilizate(_placement);
  	     return (inst.sumarea/ (inst.height * tot[0]));
  	}
    
    public double[] Utilizate(List<Placement> applyplace) {
    	double ox = 0;
	    double oy = 0;
	    double rotate = 0;
	    double max_x = 0;
	    double max_y = 0;
	    double[] u = new double[2];
	    //注意要去除瑕疵点计算
    	for(int i = 0; i < parts.size(); i++) {
  	      NestPath polygon = new NestPath(parts.get(i));
  	      if(polygon.bid > 0) {//不包括瑕疵点
  	          for(int p = 0; p < applyplace.size(); p++) {
  	              Placement place = applyplace.get(p);
  	              if(place.bid == polygon.bid) {
  	                  ox = place.translate.getX();
  	                  oy = place.translate.getY();
  	                  rotate = place.rotate;
  	                  break;
  	                  }
  	              }
  	          if(rotate != 0) {
  	        	  polygon = GeometryUtil.rotatePolygon2Polygon(polygon, rotate);
  	        	  }
  	          for(int j = 0; j < polygon.size(); j++) {
  	        	  double nx = 0;
  	        	  double ny = 0;
  	        	  nx = ox + polygon.get(j).x + inst.minside/2;
  	        	  ny = oy + polygon.get(j).y + inst.minside/2;
  	        	  if(max_x < nx)
  	        		  max_x = nx;
  	        	  if(max_y < ny)
  	        		  max_y = ny;
  	        	  }
  	          }
  	      }
    	u[0] = max_x;
    	u[1] = max_y;
    	return u;
    }
    
    public boolean lingMinDis(NestPath A, NestPath B) {
        double t_shortest_square_dis = Double.MAX_VALUE;
     for(int i = 0; i < B.size(); i++) {
      int prev = A.size() - 1;
      int cur = 0;
      while(prev != cur) {
       Segment seg_prev = A.get(prev);
       Segment seg_cur = A.get(cur);
       double dis = distance_segment(B.get(i), seg_prev, seg_cur);
       if(Math.sqrt(dis) - 2.5 < 1e-6) {
//        System.out.println("外点到内点的距离小于2.5");
        return false;
       }
       if(dis < t_shortest_square_dis)
        t_shortest_square_dis = dis;
       prev = cur++;
       if(cur == A.size())
        break;
      }
     }
     for(int i = 0; i < A.size(); i++) {
      int prev = B.size() - 1;
      int cur = 0;
      while(prev != cur) {
       Segment seg_prev = B.get(prev);
       Segment seg_cur = B.get(cur);
       double dis = distance_segment(A.get(i), seg_prev, seg_cur);
       if(Math.sqrt(dis) - 2.5 < 1e-6) {
//        System.out.println("内点到外点的距离小于2.5" + "距离是" + Math.sqrt(dis));
//        System.out.println(A.get(i).x +"  "+ A.get(i).y +"到 "+ seg_prev.x +"  "+ seg_prev.y +"和"+seg_cur.x + " "+ seg_prev.y);
        return false; 
       }
       if(dis < t_shortest_square_dis)
        t_shortest_square_dis = dis;
       prev = cur++;
       if(cur == B.size())
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
   
    /******************************************************************************************
    -------------------------------------去除自交点------------------------------------------
    *****************************************************************************************/
    public NestPath clean(NestPath srcPath) {
    	Path path = CommonUtil.NestPath2Path(srcPath);
        Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.EVEN_ODD);
        NestPath cleanPath = CommonUtil.Path2NestPath(simple.get(0));
        cleanPath.bid = srcPath.bid;
        cleanPath.type = srcPath.type;
        cleanPath.setRotation(srcPath.rotation);
        return cleanPath;
    }

    public  void add(NestPath np ){
        parts.add(np);
    }

    public  NestPath getBinPath() {
        return binPath;
    }

    public  List<NestPath> getParts() {
        return parts;
    }

    public void setBinPath(NestPath binPath) {
        this.binPath = binPath;
    }

    public void setParts(List<NestPath> parts) {
        this.parts = parts;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
