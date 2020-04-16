import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

class ImageParams{
    public SimpleStringProperty path;
    public SimpleStringProperty extension;
    public SimpleStringProperty creationTime;
    public SimpleIntegerProperty width;
    public SimpleIntegerProperty height;
    Pixel myMediumPixel;

    public ImageParams(String path, String extension, String creationTime, int width, int height, Pixel mediumPixel){
        this.path = new SimpleStringProperty(path);
        this.extension = new SimpleStringProperty(extension);
        this.creationTime = new SimpleStringProperty(creationTime);
        this.width = new SimpleIntegerProperty(width);
        this.height = new SimpleIntegerProperty(height);
        this.myMediumPixel = mediumPixel;
    }
}