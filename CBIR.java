/*
 * Assignment 2
 * By Noel Beraki
 * CSS 484
 * 
 * CBIR.java
 * Description: Provides a GUI for querying image retrievals and ordering display by either:
 * - The intensity method
 * - The color code method
 * - color code + intensity (using relevance feedback)
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

public class CBIR extends JFrame {
  // instance variables
  private final int GUIWidth = 1100;       // frame width
  private final int GUIHeight = 750;       // frame height
  private final int numImages = 100;       // total number of images in database
  private final int numImagesPerPage = 20; // total number of images displayed in on page

  private int picNo = -1;      // stores the current image being displayed (-1 if none) 
  private int imageCount = 0;  // keeps up with the number of images displayed since the first page.
  private int pageNo = 1;      // represents what page number the user is currently on 

  private Color bgColor;           // background color of the canvas
  private Color imageBg;           // background color of the image displayed panel
  private JLabel photographImage;  // container to hold an image
  private JLabel currPage;         // label to display what page the user is currently on 
  private JButton [] button;       // creates an array of JButtons
  private int [] buttonOrder;      // creates an array to keep up with the image order
  private JPanel [] panelButtons;  // planel to hold each image and its corresponding checkbox
  private JPanel [] checkboxes;    // checkboxes for each image
  private GridLayout gridLayout1;  // grid layout for whole canvas 
  private GridLayout gridLayout2;  // grid layout for displaying images database (panelLeft object)
  private GridLayout gridLayout3;  // grid layout for all buttons (buttonPanel object)
  private GridLayout gridLayout4;  // grid layout for filter buttons (filterPanel object)
  private GridLayout gridLayout5;  // grid layout for change page buttons (next, previous, and reset) (changePagePanel object)
  private GridLayout gridLayout6;  // grid layout for displaying the image (imagePanel object)
  private JPanel panelRight;       // panel for displaying retrieved image and buttons
  private JPanel panelLeft;        // panel for displaying images database
  private JPanel buttonPanel;      // panel for displaying all buttons
  private JPanel filterPanel;      // panel for displaying filter buttons
  private JPanel changePagePanel;  // panel for displaying change page buttons
  private JPanel currPagePanel;    // panel for displaying text of what page the user is currently on 
  private JPanel imagePanel;       // panel for displaying the retrieved image

  private int [] imageSize;                           // holds number of pixels for each image
  private int [][] intensityMatrix;                   // contains count bins based on intensity method for each image
  private int [][] colorCodeMatrix;                   // contains count in bins based on color code method for each image
  private double [][] mergedFeaturesMatrix;           // matrix combinind data in intensityMatrix and colorCodeMatrix
  private double [] averagesMatrix;                   // array for average of each column in mergedFeaturesMatrix
  private double [] standardDeviationMatrix;          // array for standard deviation of each column in mergedFeaturesMatrix
  private double [][] normalizedFeaturesMatrix;       // normalized feature values for each image's features 
  private double [] weights;                          // holds weights for each feature column
  private Map<Integer, List<Integer>> markedRelevant; // keeps a list of relevant images for each queried image
  private int numTotalBins;                           // intensity bins + colorCode bins

  public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        CBIR app = new CBIR();
        app.setVisible(true);
      }
    });
  }
    
  public CBIR() {
    // The following lines set up the interface including the layout of the buttons and JPanels.
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("CBIR Icon Demo: Please Select an Image");       

    imageSize = new int[numImages]; 
    intensityMatrix = new int [numImages][25]; 
    colorCodeMatrix = new int [numImages][64]; 

    numTotalBins = intensityMatrix[0].length + colorCodeMatrix[0].length; // 89
    mergedFeaturesMatrix = new double [numImages][numTotalBins];
    averagesMatrix = new double [numTotalBins]; 
    standardDeviationMatrix = new double [numTotalBins]; 
    normalizedFeaturesMatrix = new double [numImages][numTotalBins];

    weights = new double[numTotalBins]; 
    markedRelevant = new HashMap<Integer, List<Integer>>();

    // initial weights is 1 / N where N is the number of total features
    for (int i = 0; i < weights.length; i++) {
      weights[i] = 1.0 / numTotalBins;
    }

    bgColor = new Color(177,220,253); // close to light blue
    imageBg = new Color(200,200,200); // close to grey

    panelLeft = new JPanel();
    panelLeft.setBackground(bgColor);

    panelRight = new JPanel();
    panelRight.setBackground(bgColor);

    buttonPanel = new JPanel();
    buttonPanel.setBackground(bgColor);

    filterPanel = new JPanel();
    filterPanel.setBackground(bgColor);

    changePagePanel = new JPanel();
    changePagePanel.setBackground(bgColor);

    currPagePanel = new JPanel();
    currPagePanel.setBackground(bgColor);

    imagePanel = new JPanel();
    imagePanel.setBackground(imageBg);

    photographImage = new JLabel();

    gridLayout1 = new GridLayout(1, 2, 5, 5);
    gridLayout2 = new GridLayout(4, 5, 5, 5);
    gridLayout3 = new GridLayout(3, 1, 5, 5);
    gridLayout4 = new GridLayout(2, 2, 5, 5);
    gridLayout5 = new GridLayout(1, 3, 5, 5);
    gridLayout6 = new GridLayout(1, 1, 5, 5);

    setLayout(gridLayout1);
    panelRight.setLayout(new BorderLayout());
    panelLeft.setLayout(gridLayout2);
    buttonPanel.setLayout(gridLayout3);
    filterPanel.setLayout(gridLayout4);
    changePagePanel.setLayout(gridLayout5);
    imagePanel.setLayout(gridLayout6);
   
    add(panelLeft);
    add(panelRight);
    
    photographImage.setText("SELECT AN IMAGE");
    photographImage.setFont(new Font("Ariel", Font.BOLD, 36));
    
    // set text position for displayed image
    photographImage.setHorizontalTextPosition(JLabel.CENTER);
    photographImage.setVerticalTextPosition(JLabel.BOTTOM);
    // set image position for displayed image
    photographImage.setHorizontalAlignment(JLabel.CENTER);
    photographImage.setVerticalAlignment(JLabel.TOP);

    photographImage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    imagePanel.add(photographImage, BorderLayout.CENTER);

    panelRight.add(imagePanel, BorderLayout.NORTH);
    panelRight.add(buttonPanel, BorderLayout.CENTER);
    
    buttonPanel.add(filterPanel);
    buttonPanel.add(changePagePanel);
    buttonPanel.add(currPagePanel);
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 5, 5, 5));

    currPage = new JLabel("Page " + pageNo + " of 5");
    currPagePanel.add(currPage);

    JButton previousPage = new JButton("Previous Page");
    JButton nextPage = new JButton("Next Page");
    JButton reset = new JButton("Reset to First Page");
    JButton intensity = new JButton("Retrieve by Intensity Method");
    JButton colorCode = new JButton("Retrieve by Color Code Method");
    JButton relevanceFeedback = new JButton("Color Code and Intensity");
    JCheckBox relevanceBox = new JCheckBox("Revelance");

    filterPanel.add(intensity);
    filterPanel.add(colorCode);
    filterPanel.add(relevanceFeedback);
    filterPanel.add(relevanceBox);

    changePagePanel.add(previousPage);
    changePagePanel.add(nextPage);
    changePagePanel.add(reset);
      
    nextPage.addActionListener(new nextPageHandler());
    previousPage.addActionListener(new previousPageHandler());
    intensity.addActionListener(new intensityHandler());
    colorCode.addActionListener(new colorCodeHandler());
    reset.addActionListener(new resetHandler());
    relevanceFeedback.addActionListener(new relevanceFeedbackHandler());
    relevanceBox.addActionListener(new revelanceBoxHandler());

    setSize(GUIWidth, GUIHeight);
    // this centers the frame on the screen
    setLocationRelativeTo(null);
    

    button = new JButton[numImages];
    buttonOrder = new int [numImages];
    panelButtons = new JPanel[numImages];
    checkboxes = new JPanel[numImages];
    /*
     * This for loop goes through the images in the database and stores them as icons and adds
     * the images to JButtons and then to the JButton array
     */
    for (int i = 0; i < numImages; i++) {
      ImageIcon icon;
      int imgNum = i+1;

      icon = new ImageIcon(getClass().getResource("images/" + imgNum + ".jpg"));
      if(icon != null) {
        button[i] = new JButton(imgNum + ".jpg");

        // to scale the icon to fit in the grid cell and also display the name
        int imageWidth = 90;
        int imageHeight = 90; 

        Image image = icon.getImage();
        Image scaledImage = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        button[i].setIcon(scaledIcon);
        button[i].setHorizontalTextPosition(SwingConstants.CENTER);
        button[i].setVerticalTextPosition(SwingConstants.BOTTOM);

        // make button background transparent 
        button[i].setOpaque(false);        
        button[i].setBorderPainted(false);        
        button[i].setFocusPainted(false); 
      
        button[i].addActionListener(new IconButtonHandler(i, icon));
        buttonOrder[i] = i;

        panelButtons[i] = new JPanel(new BorderLayout());
        JCheckBox relevantBox = new JCheckBox("Relevant");
        relevantBox.addActionListener(new relevantCheckBoxHandler(i));

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        checkboxPanel.add(relevantBox);
        checkboxes[i] = checkboxPanel;
        checkboxes[i].setBackground(new Color(200,200,200));
        panelButtons[i].add(button[i], BorderLayout.CENTER);
        // panelButtons[i].setBackground(new Color(200,200,200));
      }
    }

    readImageSizeFile();
    readIntensityFile();
    readColorCodeFile();
    relevanceFeedbackSetup();
    displayFirstPage();
  }

  /*
   * This method opens the intensity text file containing the intensity matrix with the histogram bin values 
   * for each image. The contents of the matrix are processed and stored in a two dimensional array 
   * called intensityMatrix.
   */
  public void readIntensityFile() {
    Scanner read;
    int intensityBin;
    String line = "";
    int imageNum = 0;

    try {
      read = new Scanner(new File("intensity.txt"));

      while (read.hasNextLine()) {
        line = read.nextLine();
        String[] split = line.split(",");

        for (int i = 0; i < split.length; i++) {
          intensityBin = Integer.valueOf(split[i].trim());
          intensityMatrix[imageNum][i] = intensityBin;

          double val = (double) intensityBin / imageSize[imageNum];
          mergedFeaturesMatrix[imageNum][i] = val;
        }
        imageNum++;
      }
    }
    catch(FileNotFoundException EE){
      System.out.println("The file intensity.txt does not exist");
      EE.printStackTrace();
    }
  }
    
  /*
   * This method opens the colorCodes text file containing the colorCodes matrix with the histogram bin values 
   * for each image. The contents of the matrix are processed and stored in a two dimensional array 
   * called colorCodeMatrix.
   */
  private void readColorCodeFile() {
    Scanner read;
    int colorCodeBin;
    String line = "";
    int imageNum = 0;

    try {
      read = new Scanner(new File("colorCodes.txt"));
  
      while (read.hasNextLine()) {
        line = read.nextLine();
        String[] split = line.split(",");
        
        for (int i = 0; i < split.length; i++) {
          colorCodeBin = Integer.valueOf(split[i].trim());
          colorCodeMatrix[imageNum][i] = colorCodeBin;
          
          double val = (double) colorCodeBin / imageSize[imageNum];
          mergedFeaturesMatrix[imageNum][i + intensityMatrix[0].length] = val;
        }
        imageNum++;
      }
    }
    catch(FileNotFoundException EE){
      System.out.println("The file colorCodes.txt does not exist");
      EE.printStackTrace();
    }
  }

  /*
   * This method opens the image size text file containing the number of pixels for each image.
   * The contents of the file are stored in an array called imageSize where imageSize at i is the number
   * of pixels for the ith image (i.jpg).
   */
  private void readImageSizeFile() {
    Scanner read;
    int imagePixNum;
    int imageNum = 0;

    try {
      read = new Scanner(new File("imageSizes.txt"));
  
      while (read.hasNextLine()) {
        imagePixNum = Integer.valueOf(read.nextLine());
        imageSize[imageNum] = imagePixNum;
        imageNum++;
      }
    }
    catch(FileNotFoundException EE){
      System.out.println("The file colorCodes.txt does not exist");
      EE.printStackTrace();
    }
  }
    
  /*
   * This method displays the first twenty images in the panelLeft. The for loop starts at number one 
   * and gets the image number stored in the buttonOrder array and assigns the value to imageButNo. 
   * 
   * The button associated with the image is then added to panelLeft.  
   * The for loop continues this process until twenty images are displayed in the panelLeft
   */
  private void displayFirstPage() {
    int imageButNo = 0;
    panelLeft.removeAll(); 
    for(int i = 0; i < numImagesPerPage; i++){
      imageButNo = buttonOrder[i];
      panelLeft.add(panelButtons[imageButNo]);
      imageCount++;
    }
    panelLeft.revalidate();  
    panelLeft.repaint();
  }
    
  /*
   * This class implements an ActionListener for each iconButton.  
   * When an icon button is clicked, the image on the button is added to the 
   * photographImage and the picNo is set to the image number selected and being displayed.
   */ 
  private class IconButtonHandler implements ActionListener {
    int pNo = 0;
    ImageIcon iconUsed;
    
    IconButtonHandler(int i, ImageIcon j) {
      pNo = i;
      iconUsed = j;  //sets the icon to the one used in the button
    }
    
    public void actionPerformed(ActionEvent e) {
      photographImage.setIcon(iconUsed);
      photographImage.setText((pNo + 1) + ".jpg");
      picNo = pNo;
    }
  }
    
  /*
   * This class implements an ActionListener for the nextPageButton. The last image number to be 
   * displayed is set to the current image count plus 20.  If the endImage number equals 101, 
   * then the next page button does not display any new images because there are only 100 images to 
   * be displayed.  The first picture on the next page is the image located in the buttonOrder array 
   * at the imageCount.
   */
  private class nextPageHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int imageButNo = 0;
      int endImage = imageCount + 20;
      if (endImage <= numImages) {
        panelLeft.removeAll(); 
        for (int i = imageCount; i < endImage; i++) {
          imageButNo = buttonOrder[i];
          panelLeft.add(panelButtons[imageButNo]);
          imageCount++;
        }
  
        panelLeft.revalidate();  
        panelLeft.repaint();

        pageNo++;
        currPage.setText("Page " + pageNo + " of 5");
      }
    }
  }
    
  /*
   * This class implements an ActionListener for the previousPageButton. The last image number to 
   * be displayed is set to the current image count minus 40. If the endImage number is less than 1, 
   * then the previous page button does not display any new images because the starting image is 1.  
   * The first picture on the next page is the image located in the buttonOrder array at the imageCount.
   */
  private class previousPageHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int imageButNo = 0;
      int startImage = imageCount - 40;
      int endImage = imageCount - 20;
      if (startImage >= 0) {
        panelLeft.removeAll();
        /*
         * The for loop goes through the buttonOrder array starting with the startImage value
         * and retrieves the image at that place and then adds the button to the panelLeft.
         */
        for (int i = startImage; i < endImage; i++) {
          imageButNo = buttonOrder[i];
          panelLeft.add(panelButtons[imageButNo]);
          imageCount--;
        }
  
        panelLeft.revalidate();  
        panelLeft.repaint();

        pageNo--;
        currPage.setText("Page " + pageNo + " of 5");
      }
    }
  }

  /*
   * This function implements the histogram comparision for the comparision method bins stored in the
   * matrix. Based on the matrix, the buttonOrder array will reorder the images from smallest to largest
   * in terms of the difference in their manhattan distance with the pic parameter.
   */
  private void sortButtonsByFilter(int [][] matrix, int pic) {
    Map<Double, LinkedList<Integer>> map = new HashMap<Double, LinkedList<Integer>>();
    int picSize = imageSize[pic];

    for (int i = 0; i < matrix.length; i++) {
      double diff = 0;

      // manhattan distance
      for (int j = 0; j < matrix[pic].length; j++) {
        double d = ((double)matrix[pic][j] / picSize) - ((double)matrix[i][j] / imageSize[i]);
        diff += Math.abs(d); 
      }
      map.putIfAbsent(diff, new LinkedList<Integer>());
      map.get(diff).add(i);
    }
    
    Set<Double> set = map.keySet();
    List<Double> sortedDistances = new ArrayList<>(set);
    Collections.sort(sortedDistances);

    // reorder buttons
    int n = 0;
    for (double distance : sortedDistances) {
      LinkedList<Integer> list = map.get(distance);
      for (int imageNum : list) {
        buttonOrder[n++] = imageNum;
      }
    }
  }
    
  /*
   * This class implements an ActionListener when the user selects the intensityHandler button.  
   * The image number that the user would like to find similar images for is stored in the variable pic.  
   * pic takes the image number associated with the image selected and subtracts one to account for the fact 
   * that the intensityMatrix starts with zero and not one. The size of the image is retrieved from 
   * the imageSize array. The selected image's intensity bin values are compared to all the other image's 
   * intensity bin values and a score is determined for how well the images compare.
   * The images are then arranged from most similar to the least.
   */
  private class intensityHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (picNo == -1) { return; } // no image was selected

      sortButtonsByFilter(intensityMatrix, picNo);
      imageCount = 0;
      displayFirstPage();

      pageNo = 1;
      currPage.setText("Page " + pageNo + " of 5");
    }
  }
    
  /*
   * This class implements an ActionListener when the user selects the colorCode button. The image number 
   * that the user would like to find similar images for is stored in the variable pic. pic takes the 
   * image number associated with the image selected and subtracts one to account for the fact that
   * the intensityMatrix starts with zero and not one. The size of the image is retrieved from 
   * the imageSize array.  The selected image's intensity bin values are compared to all the other 
   * image's intensity bin values and a score is determined for how well the images compare. 
   * The images are then arranged from most similar to the least.
   */ 
  private class colorCodeHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (picNo == -1) { return; } // no image was selected

      sortButtonsByFilter(colorCodeMatrix, picNo);
      imageCount = 0;
      displayFirstPage();

      pageNo = 1;
      currPage.setText("Page " + pageNo + " of 5");
    }
  }

  /*
   * This class implements an ActionListener when the user selects the reset to first page button. 
   * The order of images is based on the number the image corresponds with. So when first 
   * running the program, it is sorted in sequential order from left to right, top to bottom. 
   * If you click any of the change page or sort by feature buttons, you can reset to the 
   * original sequential ordering of the image icons by clicking 
   * the “Reset to First Page” button.
   */ 
  private class resetHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      for (int i = 0; i < numImages; i++) {
        buttonOrder[i] = i;
      }
      imageCount = 0;
      displayFirstPage();

      pageNo = 1;
      currPage.setText("Page " + pageNo + " of 5");
    }
  }

  /*
   * This class implements an ActionListener when the user selects the relevance checkbox. 
   * If the relevance checkbox was selected, then for each image in the database, a
   * relevant checkbox will be below it. If the relevance button was deselected,
   * then all the relevant checkboxes will be undisplayed. 
   * 
   * NOTE: Any checkboxes clicked will not still be clicked if the relevance checkbox
   * is unclicked. For example:
   *  - You initially click the relevance checkbox and checkboxes appear for each image.
   *  - You then start checking off some of the images and deselect the relevance checkbox.
   *  - When you select the relevance checkbox again, those previously selected images will 
   *    NOT still be selected. 
   */ 
  private class revelanceBoxHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JCheckBox box = (JCheckBox) e.getSource();
      if (box.isSelected()) {
        for (int i = 0; i < numImages; i++) {
          panelButtons[i].add(checkboxes[i], BorderLayout.AFTER_LAST_LINE);
          panelButtons[i].revalidate();  
          panelButtons[i].repaint();
        }
      }
      else {
        for (int i = 0; i < numImages; i++) {
          JCheckBox checkbox = (JCheckBox) checkboxes[i].getComponent(0);
          if (checkbox.isSelected()) {
            checkbox.setSelected(false);
          }
          panelButtons[i].remove(checkboxes[i]);
          panelButtons[i].revalidate();  
          panelButtons[i].repaint();
        }
      }
    }
  }

  /*
   * This class implements an ActionListener when the user selects the relevant checkbox
   * for an image. If an image is selected, then it will be considered 
   * relevant to the queried image.
   */ 
  private class relevantCheckBoxHandler implements ActionListener {
    // Map<Integer, List<Integer>> markedRelevant
    int relevantImg;

    relevantCheckBoxHandler(int i) {
      relevantImg = i;
    }

    public void actionPerformed(ActionEvent e) {
      JCheckBox box = (JCheckBox) e.getSource();
      if (box.isSelected()) {
        // checked boxes are irrelevant if there is no queried image
        if (picNo == -1) { return; }

        // create list of relevant images for queried images
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.add(picNo);
        markedRelevant.putIfAbsent(picNo, list);
        markedRelevant.get(picNo).add(relevantImg);
      }
      else { 
        if (markedRelevant.containsKey(picNo) && markedRelevant.get(picNo).contains(relevantImg)) {
          markedRelevant.get(picNo).remove((Integer) relevantImg);
        }
      }
    }
  } 

  /*
   * This function established the normalized feature values for
   * each image. 
   * Steps:
   *  0. Matrix size is numImages (100) * # of intensityMatrix bins (25) + # of colorCode Bins (64). For the first 25
   *     indexes for each image, the value at the ith index is intensityMatrix at the ith index divided by the image size.
   *     For the remaining 64 indexes, the value at the ith index is colorCode at ith index + 25 divided by the image size. 
   *  1. Gets the averages for each column (each feature)
   *  2. Gets the standard deviation for each column (each feature)
   *  3. Compute the normalized feature for each feature using Guassian normalization.
   */ 
  private void relevanceFeedbackSetup() {
    getAveragesFromMergedMatrix();
    getStandardDeviationsFromMergedMatrix();
    normalizeFeatures();
  }
  
  /*
   * This function gets the averages for each column (each feature) in the merged matrix.
   */ 
  private void getAveragesFromMergedMatrix() {
    // double [] averagesMatrix = new double [intensityMatrix[0].length + colorCodeMatrix[0].length]; 

    for (int i = 0; i < mergedFeaturesMatrix[0].length; i++) {
      double sum = 0;
      for (int j = 0; j < mergedFeaturesMatrix.length; j++) {
        sum += mergedFeaturesMatrix[j][i];
      }
      
      double avg = sum / mergedFeaturesMatrix.length;
      averagesMatrix[i] = avg;
    }
  }

  /* 
   * This function gets the standard deviation for each column (each feature) in the merged matrix.
   */ 
  private void getStandardDeviationsFromMergedMatrix() {
    // double [] standardDeviationMatrix = new double [intensityMatrix[0].length + colorCodeMatrix[0].length]; 

    for (int i = 0; i < mergedFeaturesMatrix[0].length; i++) {
      double val = 0;
      for (int j = 0; j < mergedFeaturesMatrix.length; j++) {
        val += Math.pow(mergedFeaturesMatrix[j][i] - averagesMatrix[i], 2);
      }
      
      val = Math.sqrt(val / (mergedFeaturesMatrix.length - 1));
      standardDeviationMatrix[i] = val;
    }
  }

  /*
   * This function computes the normalized feature for each feature using Guassian normalization.
   */ 
  private void normalizeFeatures() {
    // normalizedFeaturesMatrix = new double [numImages][intensityMatrix[0].length + colorCodeMatrix[0].length];

    for (int i = 0; i < mergedFeaturesMatrix.length; i++) {
      for (int j = 0; j < mergedFeaturesMatrix[0].length; j++) {
        double feature = mergedFeaturesMatrix[i][j];
        double val = 0;
        // special case 1: normalized val is 0 if standard deviation is 0 (which means average is also 0)
        if (averagesMatrix[j] != 0 && standardDeviationMatrix[j] != 0) {
          // Guassian normalization 
          val = (feature - averagesMatrix[j]) / standardDeviationMatrix[j];
        }
        normalizedFeaturesMatrix[i][j] = val;
      }
    }
  }

  /*
   * This class implements an ActionListener when the user selects the 
   * "colorCode and Intensity" button.
   * This button utilizes relevance feedback to improve query results. 
   */ 
  private class relevanceFeedbackHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (picNo == -1) { return; } // no image was selected
      
      updateWeights(picNo);
      computeRFDistances(picNo);
      imageCount = 0;
      displayFirstPage();

      pageNo = 1;
      currPage.setText("Page " + pageNo + " of 5");
    }
  }

  /*
   * updates the weights using normalized features
   * of the relevant images.
   */ 
  private void updateWeights(int pic) {
    if (!markedRelevant.containsKey(pic)) { return; }
    
    // get the normalized feature rows of the relevant images
    double [][] relevantImgFeatures = new double [markedRelevant.get(pic).size()][numTotalBins];
    for (int i = 0; i < markedRelevant.get(pic).size(); i++) {
      Integer img = markedRelevant.get(pic).get(i);
      relevantImgFeatures[i] = normalizedFeaturesMatrix[img];
    }
    
    // calculate the averages for each column only using normalized
    // features of relevant images
    double [] averages = new double[numTotalBins];
    for (int i = 0; i < relevantImgFeatures[0].length; i++) {
      double sum = 0;
      for (int j = 0; j < relevantImgFeatures.length; j++) {
        sum += relevantImgFeatures[j][i];
      }

      averages[i] = sum / relevantImgFeatures.length;
    }

    // calculate the standard deviation for each column only using normalized
    // features of relevant images
    double [] sds = new double [numTotalBins];
    double minSDSNot0 = Double.MAX_VALUE;
    for (int i = 0; i < relevantImgFeatures[0].length; i++) {
      double val = 0;
      for (int j = 0; j < relevantImgFeatures.length; j++) {
        val += Math.pow(relevantImgFeatures[j][i] - averages[i], 2);
      }
      
      sds[i] = Math.sqrt(val / (relevantImgFeatures.length - 1));
      if (sds[i] != 0 && minSDSNot0 > sds[i]) {
        minSDSNot0 = sds[i];
      }
    }

    double [] updatedWeights = new double[numTotalBins];
    double summation = 0;
    for (int i = 0; i < sds.length; i++) {
      // special case 2: if standard deviation is 0
      if (sds[i] == 0) {
        /*
         * special case 2a : if the average is not 0,
         * take the smallest standard deviation not 0 
         * among features and divide by half.
         */
        if (averages[i] != 0) {
          sds[i] = 0.5 * minSDSNot0;
          updatedWeights[i] = 1.0 / sds[i];
        }
        /*
         * special case 2b : if the average for the feature
         * is 0, just set the updated weight to 0
         */
        else {
          updatedWeights[i] = 0.0;
        }
      }
      else {
        updatedWeights[i] = 1.0 / sds[i];
      }
      summation += updatedWeights[i];
    }

    // final update for weights
    for (int i = 0; i < markedRelevant.get(pic).size(); i++) {
      for (int j = 0; j < updatedWeights.length; j++) {
        weights[j] = updatedWeights[j] / summation;
      }
    }
  }

  /*
   * Reorders images based on normalized features using
   * updated weights at each iteration
   */ 
  private void computeRFDistances(int pic) {
    Map<Double, LinkedList<Integer>> rfDistances = new HashMap<Double, LinkedList<Integer>>();

    for (int i = 0; i < normalizedFeaturesMatrix.length; i++) {
      double computedWeight = 0;

      // weighted manhattan distance
      for (int j = 0; j < normalizedFeaturesMatrix[0].length; j++) {
        double diff = normalizedFeaturesMatrix[pic][j] - normalizedFeaturesMatrix[i][j];
        computedWeight += (weights[j]) * Math.abs(diff);
      }

      rfDistances.putIfAbsent(computedWeight, new LinkedList<Integer>());
      rfDistances.get(computedWeight).add(i);
    }

    Set<Double> set = rfDistances.keySet();
    List<Double> sortedDistances = new ArrayList<>(set);
    Collections.sort(sortedDistances);

    // reorder buttons
    int n = 0;
    for (double distance : sortedDistances) {
      LinkedList<Integer> list = rfDistances.get(distance);
      Collections.sort(list);
      for (int imageNum : list) {
        buttonOrder[n++] = imageNum;
      }
    }
  }
}