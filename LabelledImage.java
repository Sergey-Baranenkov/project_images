import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

class LabelledImage{
    private final BufferedImage img;
    private final ImageParams params;
    public LabelledImage(String pathname) throws IOException {
        File file = new File(pathname);
        
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(ImageIO.createImageInputStream(file));
        this.img = ImageIO.read(file);
        String path = file.getCanonicalPath();
        String creation_time = Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toString();
        int width = img.getWidth();
        int height = img.getHeight();
        String extension = imageReaders.next().getFormatName();
        Pixel myMediumPixel = new Pixel(0,0,0);

        for (int i = 0; i< width; i++){
            for (int j = 0; j< height; j++){
                int rgb = img.getRGB(i,j);
                myMediumPixel.red.set(myMediumPixel.red.getValue() + (double)((rgb >> 16) & 0xff));
                myMediumPixel.green.set(myMediumPixel.green.getValue() + (double)((rgb >> 8) & 0xff));
                myMediumPixel.blue.set(myMediumPixel.blue.getValue() + (double)((rgb) & 0xff));
            }
        }

        double size = height*width;
        if (size == 0) {
            throw new IllegalArgumentException("Picture size is 0 !");
        }
        myMediumPixel.red.set(myMediumPixel.red.getValue()/size);
        myMediumPixel.green.set(myMediumPixel.green.getValue()/size);
        myMediumPixel.blue.set(myMediumPixel.blue.getValue()/size);

        this.params = new ImageParams(path, extension,creation_time,width,height, myMediumPixel);
    }

    public ImageParams getImageParams(){
        return params;
    }

    public BufferedImage getImg(){
        return img;
    }
}