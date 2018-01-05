package jiges.github.picture;

/**
 * Created by ccr at 2018/1/3.
 */
public class Point {

    private int x;

    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    static double computeDistance(Point x, Point y) {
        return Math.sqrt(Math.abs(Math.pow(x.getX() - y.getX(),2) + Math.pow(x.getY() - y.getY(),2)));
    }

    @Override
    protected Point clone() throws CloneNotSupportedException {
        return new Point(this.x,this.y);
    }

    @Override
    public String toString() {
        return this.x + "," + this.y;
    }
}
