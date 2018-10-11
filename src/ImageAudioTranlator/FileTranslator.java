/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAudioTranlator;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    private boolean is_image;
    
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
    
    public String translate(boolean is_image) {
        return is_image ? translate_image() : translate_audio();
    }
    
    private String translate_image() {
        // read in image
        BufferedImage img;
        try {
            img = ImageIO.read(new File(in_path));
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Failed to open image file";
        }
        
        // get image data
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        
        short num_channels = 1;
        
        try {
            /*DataOutputStream outStream = new DataOutputStream(
                new FileOutputStream(new File(out_path))
            );
            

            // write the wav file per the wav file format
            outStream.writeBytes("RIFF"); // 00 - RIFF
            outStream.write(intToByteArray(36 + pixels.length), 0, 4); // 04 - how big is the rest of this file?
            outStream.writeBytes("WAVE"); // 08 - WAVE
            outStream.writeBytes("fmt "); // 12 - fmt
            outStream.write(intToByteArray(16), 0, 4); // 16 - size of this chunk
            outStream.write(shortToByteArray((short) 1), 0, 2); // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            outStream.write(shortToByteArray(num_channels), 0, 2); // 22 - mono or stereo? 1 or 2? (or 5 or ???)
            outStream.write(intToByteArray(dim1), 0, 4); // 24 - samples per second (numbers per second)
            outStream.write(intToByteArray(SAMPLE_RATE * numChannels), 0, 4); // 28 - bytes per second
            outStream.write(shortToByteArray((short) ((BITS_PER_SAMPLE / 8) * numChannels)), 0, 2); // 32 - # of bytes in one sample, for all channels
            outStream.write(shortToByteArray((short) BITS_PER_SAMPLE), 0, 2); // 34 - how many bits in a sample(number)? usually 16 or 24
            outStream.writeBytes("data"); // 36 - data
            outStream.write(intToByteArray(byteSamples.length), 0, 4); // 40 - how big is this data chunk
            outStream.write(byteSamples); // 44 - the actual data itself - just a long string of numbers

            outStream.close();
            */
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to write out .wav file.";
        }
        
        return "";
    }
    
    private String translate_audio() {
        System.out.println("Translating audio to image");
        
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
    
    public static byte[] shortToByteArray(short data) {
        return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
    }
}
