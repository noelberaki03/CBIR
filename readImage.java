/*
 * Assignment 2
 * By Noel Beraki
 * CSS 484
 * 
 * readImage.java
 * Description: Reads from the images directory and performs the intensity and color code method on each
 * image. Calculations are then outputed to text files.
 */
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class readImage {
  private int imageCount = 1;       // to iterated through the images database
  private int [][] intensityMatrix; // stores the count for each bin for each image based on the intensity method
  private int [][] colorCodeMatrix; // stores the count for each bin for each image based on the color code method
  private int [] imageSize;         // stores the size (number of pixels) for each image

  /*
   * Each image is retrieved from the file.  
   * The height and width are found for the image and the getIntensity and
   * getColorCode methods are called.
   */
  public readImage() {
    intensityMatrix = new int[100][25];
    colorCodeMatrix = new int[100][64]; 
    imageSize = new int[100];

    while(imageCount < 101){
      try {
        // the line that reads the image file
        String imagePath = "images/" + imageCount + ".jpg";
        File file = new File(imagePath);
        BufferedImage image = ImageIO.read(file);

        if (image == null) { 
          System.err.println("NULL ERROR when trying to open " + imagePath);
          return;
        }

        getIntensityAndColorCode(image, image.getHeight(), image.getWidth());

        imageSize[imageCount-1] = image.getHeight() * image.getWidth();
        imageCount++;
      } 
      catch (IOException e) {
        System.err.println("Error occurred when reading the file.");
        e.printStackTrace();
      }
    }
    
    writeIntensity();
    writeColorCode();
    writeImageSize();
  }

  /*
   * For each pixel in the image, this function is getting the rgb value 
   * and calling the getIntensity and getColorCode methods to store the result in two matrixes.
   */
  public void getIntensityAndColorCode(BufferedImage image, int height, int width) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int rgb = image.getRGB(j, i);
        int red = (rgb >> 16) & 0xFF;  // shift right by 16 bits and store decimal value of rightmost 8 bits
        int green = (rgb >> 8) & 0xFF; // shift right by 8 bits and store decimal value of rightmost 8 bits
        int blue = rgb & 0xFF; // stores decimal value of rightmost 8 bits

        getIntensity(red, green, blue);
        getColorCode(red, green, blue);
      }
    }
  }
  
  // intensity method 
  public void getIntensity(int red, int green, int blue) {
    double intensity = (0.299 * red) + (0.587 * green) + (0.114 * blue);
    int binIndex;
    if (intensity >= 250) {
      binIndex = intensityMatrix[0].length - 1;
    }
    else {
      binIndex = (int)intensity / 10;
    }
    intensityMatrix[imageCount-1][binIndex]++;
  }
  
  // color code method
  public void getColorCode(int red, int green, int blue) {
    // adds leading 0's if not fully 8 bits and takes 2 most significant bits and stores them in strings
    String redBinary = String.format("%8s", Integer.toBinaryString(red)).replace(' ', '0').substring(0, 2);
    String greenBinary = String.format("%8s", Integer.toBinaryString(green)).replace(' ', '0').substring(0, 2);
    String blueBinary = String.format("%8s", Integer.toBinaryString(blue)).replace(' ', '0').substring(0, 2);

    String sixBitBinary = redBinary + greenBinary + blueBinary;
    int colorCodeIndex = Integer.parseInt(sixBitBinary, 2);
    colorCodeMatrix[imageCount-1][colorCodeIndex]++;
  }

  /*
   * Writes the content of the matrix to a file
   */
  public void writeMatrix(int [][] matrix, String filePath) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

      for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix[i].length; j++) {
          writer.write(matrix[i][j] + "");
          if (j < matrix[i].length - 1) {
            writer.write(", ");
          }
        }
        writer.newLine();
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println("Error occurred when reading the file.");
      e.printStackTrace();
    }
  }
  
  // this method writes the contents of the colorCode matrix to a file named colorCodes.txt.
  public void writeColorCode() {
    writeMatrix(colorCodeMatrix, "colorCodes.txt");
  }
  
  // this method writes the contents of the intensity matrix to a file called intensity.txt
  public void writeIntensity() {
    writeMatrix(intensityMatrix, "intensity.txt");
  }

  // this method writes the contents of the image size array to a file called imageSizes.txt
  public void writeImageSize() {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter("imageSizes.txt"));
      for (int i = 0; i < imageSize.length; i++) {
        writer.write(imageSize[i] + "");
        writer.newLine();
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println("Error occurred when reading the file.");
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    new readImage();
  }
}