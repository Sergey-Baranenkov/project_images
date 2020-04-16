import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.imgscalr.Scalr;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main extends Application {

    private final ObservableList<LabelledImage> images_collection = FXCollections.observableArrayList();

    public static void main(String[] args){
        Application.launch();
    }

    @Override
    public void start(Stage stage) {
        TableView<LabelledImage> table = new TableView<>(images_collection);

        //создаем правила для колонок
        TableColumn<LabelledImage, Object> picture = new TableColumn<>("picture");
        picture.setCellValueFactory(p -> {
            ImageView imageview = new ImageView();
            imageview.setFitHeight(50);
            imageview.setFitWidth(50);
            Image img = SwingFXUtils.toFXImage(p.getValue().getImg(), null );
            imageview.setImage(img);
            return new SimpleObjectProperty<>(imageview);
        });

        TableColumn<LabelledImage, String> extension = new TableColumn<>("extension");
        extension.setCellValueFactory(p -> p.getValue().getImageParams().extension);
        extension.setStyle( "-fx-alignment: CENTER;");
        extension.setMinWidth(100);

        TableColumn<LabelledImage, String> creation_time = new TableColumn<>("creation_time");
        creation_time.setCellValueFactory(p -> p.getValue().getImageParams().creationTime);
        creation_time.setStyle( "-fx-alignment: CENTER;");
        creation_time.setMinWidth(200);

        TableColumn<LabelledImage, Number> width = new TableColumn<>("width");
        width.setCellValueFactory(p -> p.getValue().getImageParams().width);
        width.setStyle( "-fx-alignment: CENTER;");
        width.setMinWidth(100);


        TableColumn<LabelledImage, Number> height = new TableColumn<>("height");
        height.setCellValueFactory(p -> p.getValue().getImageParams().height);
        height.setStyle( "-fx-alignment: CENTER;");
        height.setMinWidth(100);

        TableColumn<LabelledImage, Number> med_red = new TableColumn<>("med_red");
        med_red.setCellValueFactory(p -> p.getValue().getImageParams().myMediumPixel.red);
        med_red.setStyle( "-fx-alignment: CENTER;");
        med_red.setMinWidth(100);

        TableColumn<LabelledImage, Number> med_green = new TableColumn<>("med_green");
        med_green.setCellValueFactory(p -> p.getValue().getImageParams().myMediumPixel.green);
        med_green.setStyle( "-fx-alignment: CENTER;");
        med_green.setMinWidth(100);

        TableColumn<LabelledImage, Number> med_blue = new TableColumn<>("med_blue");
        med_blue.setCellValueFactory(p -> p.getValue().getImageParams().myMediumPixel.blue);
        med_blue.setStyle( "-fx-alignment: CENTER;");
        med_blue.setMinWidth(100);

        //добавляем колонки в таблицу
        table.getColumns().add(picture);
        table.getColumns().add(extension);
        table.getColumns().add(creation_time);
        table.getColumns().add(width);
        table.getColumns().add(height);
        table.getColumns().add(med_red);
        table.getColumns().add(med_green);
        table.getColumns().add(med_blue);


        FileChooser fileChooser = new FileChooser();

        // создаем кнопки
        Button add_btn = new Button("Add");
        add_btn.setPrefWidth(80);
        add_btn.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                if (!add_image_to_list(file.getAbsolutePath())){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Добавить изображение");
                    alert.setHeaderText(null);
                    alert.setContentText("Произошла ошибка при добавлении файла!\n" +
                            "Пожалуйста, убедитесь что это изображение формата png jpg или bmp");
                    alert.showAndWait();
                }
            }
        });

        Button del_btn = new Button("Del");
        del_btn.setPrefWidth(80);
        del_btn.setOnAction(event -> {
            LabelledImage to_del = table.getSelectionModel().getSelectedItem();
            if (to_del!= null){
                images_collection.remove(to_del);
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Удалить изображение");
                alert.setContentText("Выберите изображение для удаления!");
                alert.showAndWait();
            }
        });

        Button show_best_match_btn = new Button("Best match");
        show_best_match_btn.setPrefWidth(80);
        show_best_match_btn.setOnAction(event ->{
            LabelledImage to_match = table.getSelectionModel().getSelectedItem();
            if (to_match!= null){
                ImageView imageview = new ImageView();
                Pair<LabelledImage,Double> matched = get_best_match(to_match);

                Image img = SwingFXUtils.toFXImage(matched.getKey().getImg(), null );
                imageview.setImage(img);
                Stage sub_window = new Stage();
                Label img_path = new Label("Path: " + matched.getKey().getImageParams().path.getValue());
                Label divergence_cf = new Label("Divergence_cf: " + matched.getValue());

                sub_window.setScene(new Scene(new VBox(img_path,divergence_cf,imageview)));
                sub_window.showAndWait();
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Найти максимально схожее изображение");
                alert.setHeaderText(null);
                alert.setContentText("Выберите изображение для поиска!");
                alert.showAndWait();
            }
        });


        FlowPane root = new FlowPane(add_btn,del_btn,show_best_match_btn,table);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private Pair<LabelledImage, Double> get_best_match(LabelledImage img) {
        //возвращает себя, если картинка единственная, иначе максимально близкую по расстоянию между пикселями 32x32 копий

        int resized_size = 32;
        BufferedImage comparable_img_resized = Scalr.resize(img.getImg(), Scalr.Mode.FIT_EXACT, resized_size);
        double [][] comparable_img_brightness = new double[resized_size][resized_size];

        for (int i = 0; i < resized_size; i++){
            for (int j = 0; j< resized_size; j++){
                int rgb = comparable_img_resized.getRGB(i,j);
                //formula that calculates brightness from red green blue values
                double pix_brightness = 0.299 * (double) ((rgb >> 16) & 0xff) + 0.587 *  (double) ((rgb >>  8) & 0xff) + 0.114 * (double)((rgb ) & 0xff);
                comparable_img_brightness[i][j] = pix_brightness;
            }
        }

        double best_match_cf = Double.MAX_VALUE;

        LabelledImage best_match = img;

        for (LabelledImage tmp_img: images_collection) {
            if (img == tmp_img) {
                continue;
            }

            BufferedImage tmp_img_resized = Scalr.resize(tmp_img.getImg(), Scalr.Mode.FIT_EXACT, resized_size);
            double distance_cf = 0.0;

            for (int i = 0; i < resized_size; i++){
                for (int j = 0; j< resized_size; j++){
                    int rgb = tmp_img_resized.getRGB(i,j);
                    double pix_brightness = 0.299 * (double) ((rgb >> 16) & 0xff) + 0.587 *  (double) ((rgb >>  8) & 0xff) + 0.114 * (double)((rgb ) & 0xff);
                    distance_cf += Math.sqrt(Math.abs(pix_brightness - comparable_img_brightness[i][j]));
                }
            }

            if (distance_cf < best_match_cf){
                best_match_cf = distance_cf;
                best_match = tmp_img;
            }
        }

        best_match_cf = (best_match_cf == Double.MAX_VALUE)? 0 : best_match_cf;

        return new Pair<>(best_match, best_match_cf);
    }

    private boolean add_image_to_list(String pathname){
        try{
            this.images_collection.add(new LabelledImage(pathname));
            return true;
        }catch (Exception e){
            return false;
        }
    }
}