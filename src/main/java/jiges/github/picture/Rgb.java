package jiges.github.picture;

/**
 * Created by ccr at 2018/1/4.
 */
public class Rgb {
    int red;
    int green;
    int blue;

    public Rgb(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean isBetween(Rgb min,Rgb max) {
        return this.red >= min.red && this.red <= max.red &&
                this.green >= min.green && this.green <= max.green &&
                this.blue >= min.blue && this.blue <= max.blue;
    }
}
