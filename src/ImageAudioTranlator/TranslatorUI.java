/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAudioTranlator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
        
public class TranslatorUI extends Application {
    
    private File input_file;
    private TextField input_path;
    
    private boolean is_image;
    
    private BufferedImage img;
    private AudioInputStream ais;
    
    private HBox hb_in_info;
    private Label in0_label;
    private Label in1_label;
    private Text in0_text;
    private Text in1_text;
    
    private File output_file;
    private TextField output_path;
    private Button output_btn;
    
    private HBox hb_out_info;
    private Label out0_label;
    private Label out1_label;
    private TextField out_sample_field;
    private ComboBox out_height_field;
    private Text out1_text;
    
    private int width;
    private int height;
    private int total;
    private double duration;
    private int samples;
    
    private Button convert_btn;
    
    private DecimalFormat df;
    
    @Override
    public void start(Stage primaryStage) {
        // This is to show symbol . instead of ,
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        // Define the maximum number of decimals (number of symbols #)
        df = new DecimalFormat("#.###", otherSymbols);

        primaryStage.setTitle("Welcome Daniel and Forrest");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        int row = 0;
        
        Text scenetitle = new Text("Hello, Daniel and Forrest, Welcome...");
        scenetitle.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, row, 3, 1);
        row++;
        
        Text description = new Text(
            "Convert an image to a .wav file! (or a .wav  file to an image...)"
        );
        description.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 14));
        grid.add(description, 0, row, 3, 1);
        row++;

        // Input file path
        Label input_label = new Label("In:");
        grid.add(input_label, 0, row);

        input_path = new TextField();
        input_path.setMinWidth(500);
        input_path.setEditable(false);
        grid.add(input_path, 1, row);
        
        FileChooser input_chooser = new FileChooser();
        FileChooser output_chooser = new FileChooser();
        
        input_chooser.setInitialDirectory(new File(
            System.getProperty("user.home"), "Pictures"
        ));
        
        Button input_btn = new Button("select");
        grid.add(input_btn, 2, row);
        input_btn.setOnAction(e -> {
            File file = input_chooser.showOpenDialog(primaryStage);
            if(file != null) {
                input_file = file;
                if(read_input()) {
                    String full_path = file.getAbsolutePath();
                    
                    input_path.setText(full_path);
                    
                    if(is_image) {
                        in0_label.setText("Width");
                        in0_text.setText(Integer.toString(width));
                        in1_label.setText("Height");
                        in1_text.setText(Integer.toString(height));
                    }
                    else {
                        in0_label.setText("Duration (secs)");
                        in0_text.setText(df.format(duration));
                        in1_label.setText("Samples");
                        in1_text.setText(Integer.toString(samples));
                    }
                    
                    String ext = is_image ? ".wav" : ".jpg";
                    full_path = (
                        full_path.substring(0, full_path.length()-4) + ext
                    );
                    output_path.setText(full_path);
                    output_file = new File(full_path);
                    
                    if(file.getParentFile() != null) {
                        input_chooser.setInitialDirectory(file.getParentFile());
                        output_chooser.setInitialDirectory(file.getParentFile());
                    }

                    update_output(full_path);
                    
                    hb_out_info.setVisible(true);
                }
            }
            
            boolean input_empty = input_path.getText().isEmpty();
            hb_in_info.setVisible(!input_empty);
            output_path.setDisable(input_empty);
            output_btn.setDisable(input_empty);    
            
            update_convert_btn();
        });
        row++;
        
        // input info
        hb_in_info = new HBox(20);
                
        in0_label = new Label("Width:");
        hb_in_info.getChildren().add(in0_label);
        in0_text = new Text("");
        hb_in_info.getChildren().add(in0_text);
        
        in1_label = new Label("Height:");
        hb_in_info.getChildren().add(in1_label);
        in1_text = new Text("");
        hb_in_info.getChildren().add(in1_text);
        
        hb_in_info.setVisible(false);
        
        grid.add(hb_in_info, 0, row, 4, 1);
        row++;
        
        // Output file path
        Label output_label = new Label("Out:");
        grid.add(output_label, 0, row);

        output_path = new TextField();
        grid.add(output_path, 1, row);
        
        output_path.setDisable(true);
        output_path.setEditable(false);
        
        output_btn = new Button("select");
        output_btn.setDisable(true);
        grid.add(output_btn, 2, row);
        output_btn.setOnAction(e -> {
            File file = output_chooser.showSaveDialog(primaryStage);
            boolean valid_output = false;
            
            if(file != null) {
                String full_path = file.getAbsolutePath();
                
                valid_output = true;
                
                output_file = new File(full_path);
                
                output_path.setText(output_file.getAbsolutePath());
                
                if(file.getParentFile() != null)
                    output_chooser.setInitialDirectory(file.getParentFile());
            }
            else {
                valid_output = output_file.exists();
            }
            
            hb_out_info.setVisible(valid_output);
            
            update_convert_btn();
        });
        row++;
        
        // output options
        hb_out_info = new HBox(20);
        
        out0_label = new Label("");
        hb_out_info.getChildren().add(out0_label);
        out_height_field = new ComboBox();
        hb_out_info.getChildren().add(out_height_field);
        out_sample_field = new TextField("");
        hb_out_info.getChildren().add(out_sample_field);
        out_sample_field.setVisible(false);
        
        out1_label = new Label("");
        hb_out_info.getChildren().add(out1_label);
        out1_text = new Text("");
        hb_out_info.getChildren().add(out1_text);
        
        hb_out_info.setVisible(false);
        
        out_height_field.setOnAction(e -> {
            height = (Integer) out_height_field.getValue();
            width = total / height;
            out1_text.setText(Integer.toString(width));
        });
        
        grid.add(hb_out_info, 0, row, 4, 1);
        row++;
        
        // go button
        Region r = new Region();
        r.setMinHeight(15);
        grid.add(r, 0, row, 3, 1);
        row++;
        
        convert_btn = new Button("Convert");
        convert_btn.setDisable(true);
        
        convert_btn.setOnAction(e -> {
            FileTranslator ft = new FileTranslator(
                input_path.getText(),
                output_path.getText(),
                width,
                height,
                duration,
                samples
            );
            
            String errors = (
                is_image ? ft.translate_image() : ft.translate_audio()
            );
            
            if(errors.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Convert!");
                alert.setHeaderText("Success!");
                
                if(is_image)
                    alert.setContentText(
                        "Converted a dumb ol' image to a sick-ass audio file!"
                    );
                else
                    alert.setContentText(
                        "Converted a dumb ol' audio file to a sick-ass image!"
                    );

                alert.showAndWait();
            }
            else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Convert!");
                alert.setHeaderText("Errors encountered!");
                alert.setContentText(errors);
                
                alert.showAndWait();
            }
        });
        
        HBox hbBtn = new HBox(0);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(convert_btn);
        grid.add(hbBtn, 0, row, 3, 1);
        row++;
        
        Scene scene = new Scene(grid);//, 100, 100);
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
    
    private static String get_extension(String filepath) {
        int index = filepath.lastIndexOf(".");
        if(index == -1)
            return "";
        return filepath.substring(index + 1);
    }
    
    /**
     * read input file and update input options
     */
    private boolean read_input() {
        String ext = get_extension(input_file.getAbsolutePath());
        
        // input is wav file
        if (ext.equals("wav")) {
            try {
                ais = AudioSystem.getAudioInputStream(input_file);
                AudioFormat format = ais.getFormat();
                
                // duration in seconds
                duration = (double)(ais.getFrameLength()) / format.getFrameRate();
                
                // samples per second
                samples = (int)format.getSampleRate();
                
                is_image = false;
                
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error loading .wav file.");
            }
        }
        
        // input is png or jpg
        else if (ext.equals("png") || ext.equals("jpg")) {
            try {
                img = ImageIO.read(input_file);
                
                // width of image
                width = (int)img.getWidth();
                
                // height of image
                height = (int)img.getHeight();
                
                is_image = true;
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error loading image file.");
            }
        }
        
        // unsupported file extension
        else
            System.out.println("Only .wav, .png, and .jpg are supported.");
        return false;
    }
    
    private void update_output(String full_path) {
        full_path = full_path.substring(0, full_path.length()-4);
        if(is_image) {
            full_path += ".wav";
            
            // calculate total pixels, default to 44100 samples
            samples = 44100;
            duration = (double)(width * height) / samples;
            
            out0_label.setText("Samples");
            out_sample_field.setText(Integer.toString(samples));
            out1_label.setText("Duration (secs)");
            out1_text.setText(df.format(duration));
            
            out_sample_field.setVisible(true);
            out_height_field.setVisible(false);
        }
        else {
            full_path += ".jpg";

            // calculate total number of samples in wav file
            // find all integer factors of total and add them to combobox
            total = (int)(samples * duration);
            int root = (int)Math.ceil(Math.sqrt(total));
            
            out_height_field.getItems().clear();
            height = -1;
            
            ArrayList factors = new ArrayList();
            
            for(int i = root; i > 0; i--) {
                if(total % i == 0) {
                    if(height == -1) {
                        height = i;
                        out_height_field.setValue(i);
                    }
                    factors.add(i);
                    factors.add(total / i);
                }
            }
            
            Collections.sort(factors);
            out_height_field.getItems().addAll(factors);
            
            width = total / height;

            out0_label.setText("Height");
            out1_label.setText("Width");
            out1_text.setText(Integer.toString(width));
            
            out_sample_field.setVisible(false);
            out_height_field.setVisible(true);
        }
    }
    
    private void update_convert_btn() {
        convert_btn.setDisable(
            input_path.getText().isEmpty() || output_path.getText().isEmpty()
        );
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}


/**
 * TODO
 * move setOnAction to run when input / output text fields are set rathe than
 *  when the file dialog buttons are clicked, that way they'll also validate
 *  if the user enters text directly
 * force output to have correct file extension
 * add default output path when input is specified
 * hook up convert button
 */