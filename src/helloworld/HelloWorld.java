/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

/**
 *
 * @author Zip
 */
public class HelloWorld extends Application {
    
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
    
    private double in_dim0;
    private double in_dim1;
    
    private File output_file;
    private TextField output_path;
    private Button output_btn;
    
    private HBox hb_out_info;
    private Label out0_label;
    private Label out1_label;
    private TextField out0_text;
    private Text out1_text;
    
    private double out_dim0;
    private double out_dim1;
    
    private Button convert_btn;
    
    @Override
    public void start(Stage primaryStage) {
        // This is to show symbol . instead of ,
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        // Define the maximum number of decimals (number of symbols #)
        DecimalFormat df = new DecimalFormat("#.###", otherSymbols);

        
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
            "Convert an image to a .wav file or a .wav  file to an image."
        );
        description.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 14));
        grid.add(description, 0, row, 3, 1);
        row++;

        // Input file path
        Label input_label = new Label("In:");
        grid.add(input_label, 0, row);

        input_path = new TextField();
        input_path.setMinWidth(500);
        grid.add(input_path, 1, row);
        
        FileChooser input_chooser = new FileChooser();
        
        Button input_btn = new Button("...");
        grid.add(input_btn, 2, row);
        input_btn.setOnAction(e -> {
            File file = input_chooser.showOpenDialog(primaryStage);
            if(file != null) {
                input_file = file;
                if(read_input()) {
                    input_path.setText(file.getAbsolutePath());
                    
                    in0_text.setText(df.format(in_dim0));
                    in1_text.setText(df.format(in_dim1));
                    
                    if(is_image) {
                        in0_label.setText("Width");
                        in1_label.setText("Height");
                    }
                    else {
                        in0_label.setText("Duration (secs)");
                        in1_label.setText("Samples");
                    }
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
        
        FileChooser output_chooser = new FileChooser();
        
        output_btn = new Button("...");
        output_btn.setDisable(true);
        grid.add(output_btn, 2, row);
        output_btn.setOnAction(e -> {
            File file = output_chooser.showSaveDialog(primaryStage);
            boolean valid_output = false;
            
            if(file != null) {
                String full_path = file.getAbsolutePath();
                String ext = "." + get_extension(full_path);
                
                valid_output = true;
                
                if(is_image) {
                    full_path = (
                        full_path.substring(0, full_path.length()-4) + ".wav"
                    );
                    
                    out_dim1 = 44100;
                    out_dim0 = (in_dim0 * in_dim1) / out_dim1;

                    out0_text.setText(Double.toString(out_dim0));
                    out1_text.setText(df.format(out_dim1));

                    out0_label.setText("Duration (secs)");
                    out1_label.setText("Samples");
                }
                else {
                    full_path = (
                        full_path.substring(0, full_path.length()-4) + ".png"
                    );
                    
                    double total = Math.ceil(in_dim0 * in_dim1);
                    double root = Math.sqrt(total);
                    
                    for(int i = (int)Math.floor(root); i > 0; i--) {
                        if(total%i == 0) {
                            out_dim0 = i;
                            out_dim1 = total / i;
                            break;
                        }
                    }
                    
                    out0_text.setText(Double.toString(out_dim0));
                    out1_text.setText(Double.toString(out_dim1));

                    out0_label.setText("Duration (secs)");
                    out1_label.setText("Samples");
                }
                
                output_file = new File(full_path);
                
                output_path.setText(output_file.getAbsolutePath());
                
            }
            hb_out_info.setVisible(valid_output);
            
            update_convert_btn();
        });
        row++;
        
        // output options
        hb_out_info = new HBox(20);
        
        out0_label = new Label("");
        hb_out_info.getChildren().add(out0_label);
        out0_text = new TextField("");
        hb_out_info.getChildren().add(out0_text);
        
        out1_label = new Label("");
        hb_out_info.getChildren().add(out1_label);
        out1_text = new Text("");
        hb_out_info.getChildren().add(out1_text);
        
        hb_out_info.setVisible(false);
        
        grid.add(hb_out_info, 0, row, 4, 1);
        row++;
        
        // go button
        Region r = new Region();
        r.setMinHeight(15);
        grid.add(r, 0, row, 3, 1);
        row++;
        
        convert_btn = new Button("Convert");
        convert_btn.setDisable(true);
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
                in_dim0 = (double)(ais.getFrameLength()) / format.getFrameRate();
                
                // samples per second
                in_dim1 = format.getSampleRate();
                
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
                in_dim0 = img.getWidth();
                
                // height of image
                in_dim1 = img.getHeight();
                
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