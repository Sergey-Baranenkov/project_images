import javafx.beans.property.SimpleDoubleProperty;

class Pixel {
    SimpleDoubleProperty red;
    SimpleDoubleProperty green;
    SimpleDoubleProperty blue;

    public Pixel(int red, int green, int blue) {
        this.red = new SimpleDoubleProperty(red);
        this.green = new SimpleDoubleProperty(green);
        this.blue = new SimpleDoubleProperty(blue);
    }
}