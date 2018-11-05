/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAudioTranlator;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Zip
 */
public class FileTranslator {
    private String in_path;
    private String out_path;
    private int width;
    private int height;
    private double duration;
    private int samples;
    
    public FileTranslator(
        String _in_path,
        String _out_path,
        int _width,
        int _height,
        double _duration,
        int _samples
    ) {
        in_path = _in_path;
        out_path = _out_path;
        width = _width;
        height = _height;
        duration = _duration;
        samples = _samples;
    }
    
    public String translate_image() {
        // read in image
        BufferedImage image;
        try {
            image = ImageIO.read(new File(in_path));
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Failed to open image file";
        }
        
        // get image data
        byte[] pixels = (
            ((DataBufferByte) image.getRaster().getDataBuffer()).getData()
        );
        
        short num_channels = 1;
        short bits_per_sample = 8;
        
        try {
            DataOutputStream out_stream = new DataOutputStream(
                new FileOutputStream(new File(out_path))
            );
            
            // write the wav file per the wav file format
            out_stream.writeBytes("RIFF"); // 00 - RIFF
            out_stream.write(intToByteArray(36 + pixels.length), 0, 4); // 04 - how big is the rest of this file?
            out_stream.writeBytes("WAVE"); // 08 - WAVE
            out_stream.writeBytes("fmt "); // 12 - fmt
            out_stream.write(intToByteArray(16), 0, 4); // 16 - size of this chunk
            out_stream.write(shortToByteArray((short) 1), 0, 2); // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            out_stream.write(shortToByteArray(num_channels), 0, 2); // 22 - mono or stereo? 1 or 2? (or 5 or ???)
            out_stream.write(intToByteArray(samples), 0, 4); // 24 - samples per second (numbers per second)
            out_stream.write(intToByteArray(8), 0, 4); // 28 - bytes per second
            out_stream.write(shortToByteArray((short) ((bits_per_sample / 8) * num_channels)), 0, 2); // 32 - # of bytes in one sample, for all channels
            out_stream.write(shortToByteArray((short) bits_per_sample), 0, 2); // 34 - how many bits in a sample(number)? usually 16 or 24
            out_stream.writeBytes("data"); // 36 - data
            out_stream.write(intToByteArray(pixels.length), 0, 4); // 40 - how big is this data chunk
            out_stream.write(pixels); // 44 - the actual data itself - just a long string of numbers

            out_stream.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to write out .wav file.";
        }
        
        return "";
    }
    
    public String translate_audio() {
        try{
            DataInputStream in_stream = new DataInputStream(
                new FileInputStream(new File(in_path))
            );
            
            /*for(int i = 0; i < 11; i++) {
                byte[] length_bytes = new byte[4];
                in_stream.read(length_bytes, 0, 4);
                
                int data_length = byteArrayToInt(length_bytes);
                
                System.out.println("offset " + (i*4) + " value " + data_length);
            }*/
            
            in_stream.skipBytes(16);
            
            byte[] length_bytes = new byte[4];
            in_stream.read(length_bytes, 0, 4);
            
            int header_size = byteArrayToInt(length_bytes);
            
            in_stream.skipBytes(header_size+4);
            
            length_bytes = new byte[4];
            in_stream.read(length_bytes, 0, 4);
            
            int data_length = byteArrayToInt(length_bytes);
            
            System.out.println("Length " + data_length);
            
            byte[] samples = new byte[data_length];
            
            int num_read = in_stream.read(samples, 0, data_length);
            
            System.out.println("Read in " + num_read);
            
            // Double check that the header is correct
            if(num_read != data_length) {
                throw new IOException(
                    "Number of bytes read didn't match file header."
                );
            }
            
            BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB
            );
            
            // Convert samples byte array to int array
            int[] pixels = new int[samples.length];
            for(int i = 0; i < pixels.length; i+=3) {
                pixels[i+2] = samples[i] & 0xff;
                pixels[i+1] = samples[i+1] & 0xff;
                pixels[i] = samples[i+2] & 0xff;
            }
            
            
            // dump the pixels and write out to disk
            WritableRaster raster = (WritableRaster) image.getData();
            raster.setPixels(0, 0, width, height, pixels);
            image.setData(raster);
            ImageIO.write(image , "jpg", new File(out_path));
            
        } catch(Exception e) {
            e.printStackTrace(System.out);
            return "Failed to read in .wav file.";
        }
        
        return "";
    }
    
    private static byte[] intToByteArray(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0x00FF);
        b[1] = (byte) ((i >> 8) & 0x000000FF);
        b[2] = (byte) ((i >> 16) & 0x000000FF);
        b[3] = (byte) ((i >> 24) & 0x000000FF);
        return b;
    }
    
    private static int byteArrayToInt(byte[] bytes) {
        return (
            bytes[3] << 24 | 
            (bytes[2] & 0xFF) << 16 | 
            (bytes[1] & 0xFF) << 8 | 
            (bytes[0] & 0xFF)
        );
    }
    
    public static byte[] shortToByteArray(short data) {
        return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
    }
}
