import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

class optimization2 {
    private int N;
    private int M;
    private Point[] x;
    private Point[] m;
    private double[] ox;
    private double[] oy;
    private int[][] graph;
    private int maxX;
    private int maxY;
    private int scale=32;
    private ArrayList<Point> coveredPoints;
    private Point[] radiusIndex;
    private double smallestRadius;
    private double middleX;
    private double middleY;


    public static void main(String[] args) throws IOException {
        optimization2 t = new optimization2();
        t.input();
        t.process();
        t.output();
    }

    public void input() throws IOException {
        BufferedReader f = new BufferedReader(new FileReader("crops2"));
        //BufferedReader f = new BufferedReader(new InputStreamReader(System.in));

        N = Integer.parseInt(f.readLine());
        x = new Point[N];
        maxX = 0;
        maxY = 0;

        for (int i = 0; i < N; i++) {
            StringTokenizer st = new StringTokenizer(f.readLine());
            Point p = new Point(Integer.parseInt(st.nextToken())*scale,Integer.parseInt(st.nextToken())*scale);
            if(p.x>maxX)maxX=p.x;
            if(p.y>maxY)maxY=p.y;
            x[i] = p;
        }
        Arrays.sort(x, new PointCmp2(true));//sorts x coords


        M = Integer.parseInt(f.readLine());
        m = new Point[M];//x value is the radius, y value is the index for output

        for (int i = 0; i < M; i++) {
            StringTokenizer st = new StringTokenizer(f.readLine());
            Point p = new Point(Integer.parseInt(st.nextToken())*scale,i);
            m[i]=p;
        }
        Arrays.sort(m, new PointCmp2(true));//sort via radius size, smallest to largest

    }

    public void process(){
        int totalPoints = 0;
        Point2D[] coords = new Point2D[m.length];
        ArrayList<Point> remainingPoints = new ArrayList<>(Arrays.asList(x));
        ArrayList<Point> remainingRadii = new ArrayList<>(Arrays.asList(m));
        int radius =remainingRadii.get(remainingRadii.size()-1).x;
        graph = new int[(maxX+1+radius)][(maxY+1+radius)];//graph from 0...maxX and 0...maxY -> scale =1 for integers


        ArrayList<Point> mask = new ArrayList<>();
        //create mask
        int masky = -radius;
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

        for(Point p:remainingPoints){
            if(p.x>=radius&&p.y>=radius) {
                for (Point a : mask) {
                    graph[p.x + a.x][p.y + a.y]++;
                }
            }
            else{
                for (Point a : mask) {
                    if(p.x + a.x>=0&&p.y+a.y>=0)
                        graph[p.x + a.x][p.y + a.y]++;
                }
            }
        }


        while(remainingRadii.size()>0){

            System.out.println(remainingRadii.size() + " circles remaining");

            int radiusNew =remainingRadii.get(remainingRadii.size()-1).x;
            if(radiusNew<radius){
                ArrayList<Point> mask2 = new ArrayList<>();
                //create mask
                masky = -radius;
                while( masky<=radius){
                    int maskx = -radius;
                    while(maskx<=radius){
                        double d =Math.sqrt(Math.pow(Math.abs(maskx),2)+Math.pow(Math.abs(masky),2));
                        if(d>radiusNew&&d<=radius){
                            Point p = new Point();
                            p.x=maskx;
                            p.y=masky;
                            mask2.add(p);
                        }
                        maskx++;
                    }
                    masky++;
                }

                for(Point p:remainingPoints){
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


                mask = new ArrayList<>();
                radius = radiusNew;
                //create mask
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

            while(true) {
                //check all numbers for the largest one
                int maxNum = 0;
                Point center = new Point();
                for (int xCoord = 0; xCoord < graph.length; xCoord++) {
                    for (int yCoord = 0; yCoord < graph[0].length; yCoord++) {
                        if (graph[xCoord][yCoord] > maxNum) {
                            maxNum = graph[xCoord][yCoord];
                            center.x = xCoord;
                            center.y = yCoord;
                        }
                    }
                }
                //remove points within the point boundry
                coveredPoints = new ArrayList<>();
                radiusIndex = new Point[maxNum];
                int i = 0;
                while (!(remainingPoints.get(i).x >= center.x - radius)) {
                    i++;
                }
                while (i < remainingPoints.size() && (remainingPoints.get(i).x <= center.x + radius)) {
                    Point point = remainingPoints.get(i);
                    if (point.y >= center.y - radius && point.y <= center.y + radius) {
                        double d = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
                        if (d <=(double) radius) {
                            remainingPoints.remove(i);
                            totalPoints+=1;

                            coveredPoints.add(point);
                            Point radi = new Point((int) (d * 10000.0), coveredPoints.size() - 1);
                            radiusIndex[coveredPoints.size() - 1] = radi;
                            //totalPoints++;

                            if(point.x>=radius&&point.y>=radius) {//removes area
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

                        } else {
                            i++;
                        }

                    } else {
                        i++;
                    }
                }

                Arrays.sort(radiusIndex, new PointCmp2(true));//sorts by radius coords, smallest to largest

                Collections.reverse(Arrays.asList(radiusIndex));
                smallestRadius = remainingRadii.get(remainingRadii.size()-1).x;


                Point2D smallestCenter = new Point2D.Double(center.x,center.y);
                if(radiusIndex.length==1){
                    smallestRadius = 1;
                    middleX=coveredPoints.get(radiusIndex[0].y).x;
                    middleY=coveredPoints.get(radiusIndex[0].y).y;
                }
                else {
                    for (int j = 0; j < radiusIndex.length - 1; j++) {
                        for (int k = j + 1; k < radiusIndex.length; k++) {
                            Point p1 = coveredPoints.get(radiusIndex[j].y);
                            Point p2 = coveredPoints.get(radiusIndex[k].y);
                            middleX = 0.5 * (p1.x + p2.x);
                            middleY = 0.5 * (p1.y + p2.y);


                            double rad = Math.sqrt(Math.pow((double) p1.x - middleX, 2) + Math.pow((double) p1.y - middleY, 2));
                            boolean outsideOfTheCircle = false;
                            for (int l = 0; l < coveredPoints.size(); l++) {//check every point
                                if (!(Math.sqrt(Math.pow((double) coveredPoints.get(l).x - middleX, 2) + Math.pow((double) coveredPoints.get(l).y - middleY, 2)) <= rad)) {
                                    outsideOfTheCircle = true;
                                    break;
                                }
                            }
                            if (!outsideOfTheCircle) {
                                if(rad<smallestRadius){
                                    smallestRadius = rad;
                                    smallestCenter = new Point2D.Double(middleX,middleY);
                                }
                            }
                        }
                    }

                    for (int j = 0; j < radiusIndex.length - 2; j++) {
                        for (int k = j + 1; k < radiusIndex.length - 1; k++) {
                            for (int v = k + 1; v < radiusIndex.length; v++) {
                                Point p1 = coveredPoints.get(radiusIndex[j].y);
                                Point p2 = coveredPoints.get(radiusIndex[k].y);
                                Point p3 = coveredPoints.get(radiusIndex[v].y);
                                double a = p1.x + p2.x, b = p1.y + p2.y, c = 0.5 * (p1.x * p1.x + p1.y * p1.y - p2.x * p2.x - p2.y * p2.y);
                                double d = p3.x + p2.x, e = p3.y + p2.y, f = 0.5 * (p2.x * p2.x + p2.y * p2.y - p3.x * p3.x - p3.y * p3.y);
                                double n = b * d - e * a;

                                middleX = (f * b - c * e) / n;
                                middleY = (c * d - f * a) / n;

                                double rad = Math.sqrt(Math.pow((double) p1.x - middleX, 2) + Math.pow((double) p1.y - middleY, 2));
                                boolean outsideOfTheCircle = false;
                                for (int l = 0; l < coveredPoints.size(); l++) {//check every point
                                    if (!(Math.sqrt(Math.pow((double) coveredPoints.get(l).x - middleX, 2) + Math.pow((double) coveredPoints.get(l).y - middleY, 2)) <= rad)) {
                                        outsideOfTheCircle = true;
                                        break;
                                    }
                                }
                                if (!outsideOfTheCircle) {
                                    if(rad<smallestRadius){
                                        smallestRadius = rad;
                                        smallestCenter = new Point2D.Double(middleX,middleY);
                                    }
                                }
                            }
                        }
                    }
                }


                //find smallest radius remaining
                int dex = 0;//start with index
                while (dex<remainingRadii.size()&&remainingRadii.get(dex).x< smallestRadius) {
                    dex++;//increase
                }
                if(dex>=remainingRadii.size()-1){
                    dex=remainingRadii.size()-1;
                    smallestRadius = remainingRadii.get(dex).x;
                    coords[remainingRadii.get(dex).y]=smallestCenter;
                    remainingRadii.remove(dex);
                    break;
                }
                smallestRadius = remainingRadii.get(dex).x;
                if(smallestRadius<radius) System.out.println("Radius shrunk from "+radius + " to "+smallestRadius);
                coords[remainingRadii.get(dex).y]=smallestCenter;
                remainingRadii.remove(dex);
                if(remainingRadii.size()==0)break;
            }
        }
        print("total",totalPoints);




        ox = new double[M];
        oy = new double[M];
        for(int i = 0; i<M; i++){
            ox[i]= coords[i].getX()/scale;
            oy[i]= coords[i].getY()/scale;
        }
    }

    public void output() throws IOException{
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("test.out")));
        //PrintWriter out = new PrintWriter(new PrintStream(System.out));
        for(int i = 0; i<ox.length;i++){
            out.println(ox[i]+ " "+ oy[i]);
        }
        out.close();
    }

    public void printLists(){
        System.out.println(Arrays.toString(x));
        System.out.println(Arrays.toString(m));
    }
    public void print(String s, Object o){
        System.out.println(s +": "+ o.toString());
    }
}

