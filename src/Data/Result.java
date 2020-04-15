package Data;

import java.util.List;

public class Result {
    public List<Vector> placements;
    public double fitness;
    public List<NestPath> paths;
    public double area;

    public Result() {
    	
    }
    
    public Result(List<Vector> placements, double fitness, List<NestPath> paths, double area) {
        this.placements = placements;
        this.fitness = fitness;
        this.paths = paths;
        this.area = area;
    }
}
