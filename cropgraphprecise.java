import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

class cropgraphprecise {
    private int N;//number of points
    private int M;//number of radii
    private Point[] points;//saved points
    private Point[] m;//saved radii, x=radius, y=index (keeps track of the order they come in)
    private double[] ox;//output x coords
    private double[] oy;//output y coords
    private int[][] graph;//the graph used to plot the points
    private int maxX;//maximum x value of the points
    private int maxY;//maximum y value of the points
    private int scale=1;//sets the precision of the graph; ex: 2 = nearest 0.5, 10 = nearest 0.1, 50 = nearest 0.02


    public static void main(String[] args) throws IOException { //calls all three processes; for cleaner separation
        cropgraphprecise t = new cropgraphprecise();
        t.input();
        t.process();
        t.output();
    }

    public void input() throws IOException {
        BufferedReader f = new BufferedReader(new FileReader("crops5"));//input

        //saving the points into the array
        N = Integer.parseInt(f.readLine());
        points = new Point[N];
        maxX = 0;
        maxY = 0;

        for (int i = 0; i < N; i++) {
            StringTokenizer st = new StringTokenizer(f.readLine());
            Point p = new Point();
            p.x = Integer.parseInt(st.nextToken())*scale;
            p.y = Integer.parseInt(st.nextToken())*scale;
            if(p.x>maxX)maxX=p.x;
            if(p.y>maxY)maxY=p.y;
            points[i] = p;
        }
        Arrays.sort(points, new PointCmp2(true));//sorts by x coords, smallest to largest

        M = Integer.parseInt(f.readLine());
        m = new Point[M];//x value is the radius, y value is the index for output

        for (int i = 0; i < M; i++) {
            StringTokenizer st = new StringTokenizer(f.readLine());
            Point p = new Point();
            p.x= Integer.parseInt(st.nextToken());
            p.y = i;
            m[i]=p;
        }
        Arrays.sort(m, new PointCmp2(true));//sort via radius size, smallest to largest

    }

    public void process(){
        int totalPoints = 0;
        Point[] coords = new Point[m.length];
        int index = m.length-1;//start with the largest
        ArrayList<Point> remainingPoints = new ArrayList<>(Arrays.asList(points));

        graph = new int[(maxX+1+m[index].x*scale)][(maxY+1+m[index].x*scale)];//graph from 0...maxX and 0...maxY. adds the largest radius for extra room

        int radius =m[index].x*scale;
        ArrayList<Point> mask = new ArrayList<>();
        //create mask: makes it less computationally heavy when filling in each circle
        int masky = -radius;
        while( masky<=radius){
            int maskx = -radius;
            while(maskx<=radius){
                if(Math.sqrt(Math.pow(Math.abs(maskx),2)+Math.pow(Math.abs(masky),2))<=radius){
                    Point p = new Point();
                    p.x=maskx;
                    p.y=masky;
                    mask.add(p);//the array list, mask, is the points from 0,0 that are within the circle
                }
                maskx++;
            }
            masky++;
        }

        for(Point p:remainingPoints){//takes each coordinate within the radius away from the point and adds it by one
            if(p.x>=radius&&p.y>=radius)//if the center is not near the bottom
                for (Point a : mask) graph[p.x + a.x][p.y + a.y]++;
            else
                for (Point a : mask)//if it is near the bottom, then check if it is out of bounds each time
                    if(p.x + a.x>=0&&p.y+a.y>=0) graph[p.x + a.x][p.y + a.y]++;
        }
        while(index>=0){
            System.out.println(index + " circles remaining");//indicates progress
            int radiusNew =m[index].x*scale;
            if(radiusNew<radius){//creates an inverse mask is the next radius is smaller than the current radius
                ArrayList<Point> mask2 = new ArrayList<>();
                masky = -radius;
                while( masky<=radius){
                    int maskx = -radius;
                    while(maskx<=radius){
                        double d =Math.sqrt(Math.pow(Math.abs(maskx),2)+Math.pow(Math.abs(masky),2));
                        if(d>radiusNew&&d<=radius){
                            Point p = new Point();
                            p.x=maskx;
                            p.y=masky;
                            mask2.add(p);//this inverse mask is the points between the smaller and larger circles to remove
                        }
                        maskx++;
                    }
                    masky++;
                }

                for(Point p:remainingPoints){//carries out the inverse mask, subtracting 1 from the coords of the inverse mask
                    if(p.x>=radius&&p.y>=radius) {
                        for (Point a : mask2) {
                            graph[p.x + a.x][p.y + a.y]--;
                        }
                    }
                    else{
                        for (Point a : mask2) {
                            if(p.x + a.x>=0&&p.y+a.y>=0)
                                graph[p.x + a.x][p.y + a.y]--;
                        }
                    }
                }


                mask = new ArrayList<>();//makes a new normal mask for later use when removing points
                radius = radiusNew;
                masky = -radius;
                while( masky<=radius){
                    int maskx = -radius;
                    while(maskx<=radius){
                        if(Math.sqrt(Math.pow(Math.abs(maskx),2)+Math.pow(Math.abs(masky),2))<=radius){
                            Point p = new Point();
                            p.x=maskx;
                            p.y=masky;
                            mask.add(p);
                        }
                        maskx++;
                    }
                    masky++;
                }
            }


            //check all numbers in the graph for the largest one
            int maxNum = 0;
            Point center = new Point();
            for(int xCoord = 0; xCoord<graph.length;xCoord++){
                for(int yCoord = 0; yCoord<graph[0].length;yCoord++){
                    if(graph[xCoord][yCoord]>maxNum){
                        maxNum =graph[xCoord][yCoord];
                        center.x=xCoord;
                        center.y=yCoord;
                    }
                }
            }

            //searches the remaining points to find the points covered by the circle to remove
            coords[index]=center;
            int i = 0;
            while(!(remainingPoints.get(i).x>=center.x-radius)){//iterates through until it gets to the the points in the x boundary of the circle
                i++;
            }
            while(i<remainingPoints.size()&&(remainingPoints.get(i).x<=center.x+radius)) {
                Point point = remainingPoints.get(i);
                if (point.y >= center.y - radius && point.y <= center.y + radius&&Math.sqrt(Math.pow(Math.abs((double) point.x - (double) center.x), 2) + Math.pow(Math.abs((double) point.y - (double) center.y), 2)) <= (double) radius) {
                    //checks if the point is within the y boundary of the circle, then checks if it is within the circle
                        remainingPoints.remove(i);//removes point from list
                        if(point.x>=radius&&point.y>=radius) {//uses the mask from before to subtract 1 from the coords that this point covered previously
                            for (Point a : mask) {
                                graph[point.x + a.x][point.y + a.y]--;
                            }
                        }
                        else{
                            for (Point a : mask) {
                                if(point.x + a.x>=0&&point.y+a.y>=0)
                                    graph[point.x + a.x][point.y + a.y]--;
                            }
                        }
                        totalPoints++;//keeps track of total points covered
                }
                else{
                    i++;
                }
            }
            index--;
        }
        print("total",totalPoints);

        ox = new double[M];
        oy = new double[M];
        for(int i = 0; i<M; i++){//puts the results in the correct order for output
            ox[m[i].y]= (double)coords[i].x/(double)scale;
            oy[m[i].y]= (double)coords[i].y/(double)scale;
        }
    }
    public void output() throws IOException{
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("test.out")));
        for(int i = 0; i<ox.length;i++){//prints results
            out.println(ox[i]+ " "+ oy[i]);
        }
        out.close();
    }
    public void print(String s, Object o){
        System.out.println(s +": "+ o.toString());
    }
}

