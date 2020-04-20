import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.imgscalr.Scalr;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

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

        Button show_best_match_btn = new Button("Show differences");
        show_best_match_btn.setPrefWidth(80);
        show_best_match_btn.setOnAction(event ->{
            LabelledImage to_match = table.getSelectionModel().getSelectedItem();
            if (to_match!= null){
                if (images_collection.size() == 1){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Определить схожесть изображения с другими");
                    alert.setHeaderText(null);
                    alert.setContentText("Должно быть хотя-бы 2 изображения");
                    alert.showAndWait();
                    return;
                }

                Stage sub_window = new Stage();
                HBox root = new HBox();
                root.setSpacing(20);

                ArrayList<Pair<LabelledImage,Double>> sorted_matched = get_best_match(to_match);
                String css_image_block = "-fx-border-color: gray;\n" +
                        "-fx-border-insets: 5;\n" +
                        "-fx-border-width: 2;\n" +
                        "-fx-border-style: dashed;\n";

                for (Pair<LabelledImage,Double> match : sorted_matched){
                    ImageView imageview = new ImageView(SwingFXUtils.toFXImage(match.getKey().getImg(), null ));
                    imageview.setFitWidth(500);
                    imageview.setFitHeight(500);
                    Label img_path = new Label("Путь: " + match.getKey().getImageParams().path.getValue());

                    Label divergence_cf = new Label("Коэффициент отдаленности: " + match.getValue());

                    VBox image_block = new VBox(img_path,divergence_cf,imageview);
                    image_block.setStyle(css_image_block);
                    root.getChildren().add(image_block);
                }
                sub_window.setScene(new Scene(root));
                sub_window.showAndWait();
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Определить схожесть изображения с другими");
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

    private ArrayList<Pair<LabelledImage,Double>> get_best_match(LabelledImage img) {
        //массив для хранения пар <ссылка на объект, кф.отдаленности>
        ArrayList<Pair<LabelledImage,Double>> unsorted_pairs = new ArrayList<>();

        int resized_size = 32;

        BufferedImage comparable_img_resized = Scalr.resize(img.getImg(), Scalr.Mode.FIT_EXACT, resized_size);
        double [][] comparable_img_brightness = new double[resized_size][resized_size];

        for (int i = 0; i < resized_size; i++){
            for (int j = 0; j< resized_size; j++){
                int rgb = comparable_img_resized.getRGB(i,j);
                comparable_img_brightness[i][j] = get_pix_brightness(rgb);
            }
        }

        double best_match_cf = Double.MAX_VALUE;

        for (LabelledImage tmp_img: images_collection) {
            if (img == tmp_img) {
                continue;
            }

            BufferedImage tmp_img_resized = Scalr.resize(tmp_img.getImg(), Scalr.Mode.FIT_EXACT, resized_size);
            double distance_cf = 0.0;

            for (int i = 0; i < resized_size; i++){
                for (int j = 0; j< resized_size; j++){
                    int rgb = tmp_img_resized.getRGB(i,j);
                    distance_cf += Math.sqrt(Math.abs(get_pix_brightness(rgb) - comparable_img_brightness[i][j]));
                }
            }

            if (distance_cf < best_match_cf){
                best_match_cf = distance_cf;
            }

            unsorted_pairs.add(new Pair<>(tmp_img, distance_cf));
        }

        unsorted_pairs.sort(Comparator.comparingDouble(Pair::getValue));
        return unsorted_pairs;
    }

    private boolean add_image_to_list(String pathname){
        try{
            this.images_collection.add(new LabelledImage(pathname));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private double get_pix_brightness(int rgb){
        return 0.299 * (double) ((rgb >> 16) & 0xff) + 0.587 *  (double) ((rgb >>  8) & 0xff) + 0.114 * (double) ((rgb ) & 0xff);
    }
}