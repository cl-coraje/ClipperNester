package Util;

import java.util.List;

import Data.*;

public class NfpUtil {
	
    public static ParallelData nfpGenerator(NfpPair pair , Config config){

        NestPath A = GeometryUtil.rotatePolygon2Polygon(pair.getA() , pair.getKey().getArotation());
        NestPath B = GeometryUtil.rotatePolygon2Polygon(pair.getB() , pair.getKey().getBrotation());
        List<NestPath> nfp;
        
        if(pair.getKey().isInside()){                           
            nfp = GeometryUtil.noFitPolygonRectangle(A,B);
            if(nfp != null && nfp.size() > 0){
                for(int i = 0 ; i < nfp.size() ; i ++){
                    if(GeometryUtil.polygonArea(nfp.get(i)) > 0 ){
                        nfp.get(i).reverse();
                    }
                }
            }
        }
        else{
            nfp = GeometryUtil.minkowskiDifference(A,B);
            if( nfp == null || nfp.size() == 0 ){
                return null;
            }
            for(int i = 0; i < nfp.size() ; i++){
                if(i == 0){
                    if(Math.abs(GeometryUtil.polygonArea(nfp.get(i))) < Math.abs(GeometryUtil.polygonArea(A))){
                        nfp.remove(i);
                        return null;
                    }
                }
            }
            if(nfp.size() == 0 ){
                return null;
            }
            
            for(int i = 0; i < nfp.size(); i++){
                if(GeometryUtil.polygonArea(nfp.get(i)) > 0){
                    nfp.get(i).reverse();
                }
            }
        }  
        return new ParallelData(pair.getKey() , nfp);
    }
}
