package cs3540;

import java.awt.AWTException;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PImage;
import java.awt.Point;

public class BakeOff1 extends PApplet {
	final int padding = 50; // padding between buttons and also their width/height
	final int buttonSize = 40; // padding between buttons and also their width/height
	final int selectedButtonSize = 140;
	int margin = 200; // set the margin around the squares
	int windowSize = 700;
	int trialNum = 0; // the current trial number (indexes into trials array above)
	int startTime = 0; // time starts when the first click is captured
	int finishTime = 0; // records the time of the final click
	float timeTaken = 0;
	double cursorTrialStartingX = 0.0;
	double cursorTrialStartingY = 0.0;
	int hits = 0; // number of successful clicks
	static int misses = 0; // number of missed clicks
	static int numRepeats = 10; // sets the number of times each button repeats in the test
	static int allowedMisses = 0;
	static int id = 0; // identifies user of group 28 0=Aiden 1=Bianca 2=Koosha
	
	boolean failed = false;
	boolean fininshed = false;
	
	ArrayList<Integer> trials = new ArrayList<Integer>(); // contains the order of buttons that activate in the test

	String csvData = "";
	
	Robot robot; // initialized in setup
	
	PImage arrowImage;
	
	public static void main(String[] args) {
	    Scanner scanner = new Scanner(System.in);
	    String name = "";
	    	
	    System.out.println("Welcome to the test. The button size has increased significantly, and there is a new rule:\n\tYour accuracy at the end of the test must be >= 99%. If it dips below this theoretical best, the test will end prematurely.\n\nGood Luck!");
	    while (!(name.equals("aiden") || name.equals("bianca") || name.equals("koosha") || name.equals("exit"))) {
	        System.out.println("Enter your name: (Aiden, Bianca, Koosha, or \"Exit\" to leave)");
	        name = scanner.nextLine().trim().toLowerCase();
	    }	    
	    scanner.close();

	    switch (name) {
	        case "aiden":
	            id = 0;
	            break;
	        case "bianca":
	            id = 1;
	            break;
	        case "koosha":
	            id = 2;
	            break;
	        case "exit":
	        	return;
	    }
	    
		int totalTrials = 16 * numRepeats - 1;
	    allowedMisses = -((99 * totalTrials)/(100))+totalTrials-1;

	    PApplet.main("cs3540.BakeOff1");
	}
	
	public void writeToCSV(String s) {
        String csvFile = "preformance.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile, true))) {
        	writer.println(s);
        } 
        catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
	}

	/**
	 * https://processing.org/reference/settings_.html#:~:text=The%20settings()%20method%20runs,commands%20in%20the%20Processing%20API.
	 */
	public void settings() {
		size(windowSize, windowSize);
	}

	/**
	 * // https://processing.org/reference/setup_.html
	 */
	public void setup() {
		noStroke(); 
		textFont(createFont("Arial", 16));
		textAlign(CENTER);
		frameRate(60);
		ellipseMode(CENTER);

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 16; i++)
			for (int k = 0; k < numRepeats; k++)
				trials.add(i);

		Collections.shuffle(trials);
		System.out.println("trial order: " + trials);

		surface.setLocation(0, 0);
		
		arrowImage = loadImage("arrow.png");
		imageMode(CENTER);
		arrowImage.resize(buttonSize-10, buttonSize-10);        
	}

	public void draw() {
		background(0);

		if (trialNum >= trials.size())
		{
			float timeTaken = (finishTime - startTime) / 1000f;
			fill(255);
			text("Finished!", width / 2, height / 2);
			text("Hits: " + hits, width / 2, height / 2 + 20);
			text("Misses: " + misses, width / 2, height / 2 + 40);
			text("Accuracy: " + (float) hits * 100f / (float) (hits + misses) + "%", width / 2, height / 2 + 60);
			text("Total time taken: " + timeTaken + " sec", width / 2, height / 2 + 80);
			text("Average time for each button: " + nf((timeTaken) / (float) (hits + misses), 0, 3) + " sec", width / 2,
					height / 2 + 100);
			text("Successfully written scores to shared csv!", width / 2, height / 2 + 120);
			
			if (!finished) {
				finished = true;
				writeToCSV(csvData);
			}

			return;
		}
		if (failed) {
			fill(255);
			text("Accuracy went below 99%. Test failed!", width / 2, height / 2);
			return;
		}

		fill(255);
		drawOmbreBackground();
		
	    text("Allowed misses: " + allowedMisses, 75, 40);
		text((trialNum + 1) + " of " + trials.size(), 40, 20); // display what trial the user is on
		
		for (int i = 0; i < 16; i++) {
			drawOmbreButton(i);
			drawArrowButton(i);
		}
	}

	public void mousePressed() // test to see if hit was in target!
	{	
		if (trialNum >= trials.size() || failed) // check if task is done
			return;

		if (trialNum == 0)
			startTime = millis();

		if (trialNum == trials.size() - 1) // check if final click
		{
			finishTime = millis();
			System.out.println("we're all done!");
		}

		Rectangle bounds = getButtonLocation(trials.get(trialNum));
		
		boolean inBounds = ((mouseX > bounds.x && mouseX < bounds.x + bounds.width)
				&& (mouseY > bounds.y && mouseY < bounds.y + bounds.height));
		
		timeTaken = ((float) millis() / (float) 1000) - timeTaken;
		
		String csvString = trialNum + "," + id + "," + cursorTrialStartingX + "," +  cursorTrialStartingY + "," +  bounds.getCenterX() + "," +  bounds.getCenterY() + "," +  selectedButtonSize + "," +  timeTaken + "," +  inBounds + "\n";
		System.out.println(csvString);
		
		if (trialNum > 0) { // First trial doesn't count to the total
			csvData += csvString;
			
			timeTaken = ((float) millis() / (float) 1000);
			
			if (inBounds)
			{
				hits++;
			} 
			else {
				misses++;
				allowedMisses--;
				
				int totalTrials = (16 * numRepeats) - 1;
				if (((float) (totalTrials - misses) * 100f / (float) totalTrials) < 99){
					failed = true;
				}
			}
		}

		cursorTrialStartingX = mouseX;
		cursorTrialStartingY = mouseY;
		trialNum++;
	}

	// probably shouldn't have to edit this method
	public Rectangle getButtonLocation(int i) {
	    int baseX = (i % 4) * (padding + buttonSize) + margin;
	    int baseY = (i / 4) * (padding + buttonSize) + margin;
	    int size = (i == trials.get(trialNum)) ? selectedButtonSize : buttonSize;

	    // Offset to keep larger button centered
	    int offset = (buttonSize - size) / 2;
	    return new Rectangle(baseX + offset, baseY + offset, size, size);
	}
	
	public void drawButton(int i) {
		Rectangle bounds = getButtonLocation(i);
		
		if (trials.get(trialNum) == i) // see if current button is the target
			fill(0, 255, 255); // if so, fill cyan
		else
			fill(200); // if not, fill gray

		rect(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public void drawOmbreBackground() {
		Rectangle targetBounds = getButtonLocation(trials.get(trialNum));
		
		double startDist = Math.sqrt(2 * Math.pow(selectedButtonSize, 2));
		
		Rectangle topLeftLocation = getButtonLocation(0);
		Rectangle topRightLocation = getButtonLocation(3);
		Rectangle bottomLeftLocation = getButtonLocation(11);
		Rectangle bottomRightLocation = getButtonLocation(15);
		Point[] corners = {
				new Point(topLeftLocation.x, topLeftLocation.y), 
				new Point(topRightLocation.x + buttonSize, topRightLocation.y), 
				new Point(bottomLeftLocation.x, bottomLeftLocation.y + buttonSize), 
				new Point(bottomRightLocation.x + buttonSize, bottomRightLocation.y + buttonSize)
		};
		
		double endDist = 0;
		for (Point corner : corners) {
            double cornerDist = Math.sqrt(Math.pow((targetBounds.x + (buttonSize / 2.0)) - corner.x, 2) +
                    					  Math.pow((targetBounds.y + (buttonSize / 2.0)) - corner.y, 2));
            if (cornerDist > endDist) {
            	endDist = cornerDist;
            }
		}
	    
	    PImage ombreImage;
		try {
			ombreImage = generateOmbreButtonImage(0.0, 0.0, targetBounds.x, targetBounds.y, 100, startDist, endDist, windowSize);
		} catch (Exception e) {
			return;
		}
	    drawImage(ombreImage, (windowSize / 2), (windowSize / 2), 0);
	}
	
	public void drawImage(PImage image, int x, int y, float angle) {
		pushMatrix();
		translate(x, y);
		if (angle != 0.0) {
			rotate(radians(angle));
		}
		image(image, 0, 0);
		popMatrix();
	}
	
	public PImage generateOmbreButtonImage(double xOrigin, double yOrigin, int xTarget, int yTarget, int steps, double startDist, double endDist, int imageSize) throws Exception {
	    int[] startColor = {102, 255, 0}; // GREEN (R,G,B)
	    int[] endColor   = {255, 102, 0}; // RED

	    double redStep   = (endColor[0] - startColor[0]) / (double)steps;
	    double greenStep = (endColor[1] - startColor[1]) / (double)steps;
	    double blueStep  = (endColor[2] - startColor[2]) / (double)steps;

	    double segmentDist = (endDist) / steps;
	    double targetXCenter = xTarget + (selectedButtonSize / 2.0);
	    double targetYCenter = yTarget + (selectedButtonSize / 2.0);

	    PImage ombreImage = new PImage(imageSize, imageSize, ARGB);

	    ombreImage.loadPixels();
	    for (int yPos = 0; yPos < ombreImage.height; yPos++) {
	        for (int xPos = 0; xPos < ombreImage.width; xPos++) {
	            int pixelColor;
	            if (xPos == 0 || yPos == 0 || xPos == ombreImage.width - 1 || yPos == ombreImage.height - 1) {
	                pixelColor = 0xFF000000; // black border
	            } 
	            else {
	                double dist = Math.sqrt(Math.pow(targetXCenter - (xOrigin + xPos), 2) +
	                                        Math.pow(targetYCenter - (yOrigin + yPos), 2));
	                double segment = Math.min(steps - 1, Math.max(0, Math.ceil(dist / segmentDist)));

	                int r = Math.min(255, Math.max(0, (int)Math.round(startColor[0] + redStep * segment)));
	                int g = Math.min(255, Math.max(0, (int)Math.round(startColor[1] + greenStep * segment)));
	                int b = Math.min(255, Math.max(0, (int)Math.round(startColor[2] + blueStep * segment)));

	                pixelColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
	            }
	            ombreImage.pixels[yPos * ombreImage.width + xPos] = pixelColor;
	        }
	    }
	    
	    ombreImage.updatePixels();
	    return ombreImage;
	}
	
	public void drawOmbreButton(int i) {
	    Rectangle bounds = getButtonLocation(i);
	    Rectangle targetBounds = getButtonLocation(trials.get(trialNum));
	    int size = buttonSize;
	    if (trials.get(trialNum) == i) {
	    	size = selectedButtonSize;
	    }

	    double startDist = Math.sqrt(2 * Math.pow(buttonSize, 2));

	    Rectangle topLeftLocation = getButtonLocation(0);
	    Rectangle topRightLocation = getButtonLocation(3);
	    Rectangle bottomLeftLocation = getButtonLocation(11);
	    Rectangle bottomRightLocation = getButtonLocation(15);
	    Point[] corners = {
	        new Point(topLeftLocation.x, topLeftLocation.y), 
	        new Point(topRightLocation.x + buttonSize, topRightLocation.y), 
	        new Point(bottomLeftLocation.x, bottomLeftLocation.y + buttonSize), 
	        new Point(bottomRightLocation.x + buttonSize, bottomRightLocation.y + buttonSize)
	    };

	    double endDist = 0;
	    for (Point corner : corners) {
	        double cornerDist = Math.sqrt(Math.pow((targetBounds.x + (buttonSize / 2.0)) - corner.x, 2) +
	                                      Math.pow((targetBounds.y + (buttonSize / 2.0)) - corner.y, 2));
	        if (cornerDist > endDist) {
	            endDist = cornerDist;
	        }
	    }

	    PImage buttonImage;
	    try {
	        buttonImage = generateOmbreButtonImage(bounds.x, bounds.y, targetBounds.x, targetBounds.y, 
	                                               20, startDist, endDist, size);
	    } catch (Exception e) {
	        return;
	    }

	    // Draw ombre background
	    drawImage(buttonImage, bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2), 0);

	    // Highlight the target button with a translucent overlay (optional)
	    if (trials.get(trialNum) == i) {
	        fill(255, 255, 255, 120); // semi-transparent white
	        rect(bounds.x, bounds.y, bounds.width, bounds.height);
	    }
	}
	
	public void drawArrowButton(int i) {
	    Rectangle bounds = getButtonLocation(i);
	    Rectangle targetBounds = getButtonLocation(trials.get(trialNum));

	    double relativeX = bounds.getCenterX() - targetBounds.getCenterX();
	    double relativeY = bounds.getCenterY() - targetBounds.getCenterY();

	    float ang = (float) ((180 * Math.atan2(relativeY, relativeX)) / Math.PI) - 180;

	    if (trials.get(trialNum) != i) {
	        drawImage(arrowImage, bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2), ang);
	    }
	}


	public void drawButtonCross(int i) {
		Rectangle bounds = getButtonLocation(i);
		
		int row = (int) Math.floor(i / 4);
		int col = i % 4;
		
		int target = trials.get(trialNum);
		int target_row = (int) Math.floor(target / 4);
		int target_col = target % 4;
		
		boolean is_in_row = row == target_row;
		boolean is_in_col = col == target_col;
		
		if (is_in_row && is_in_col) {
			//GREEN
			fill(102, 255, 0);
		}
		else if (is_in_row || is_in_col) {
			//YELLOW
			fill(255, 234, 0);
		}
		else {
			//GRAY
			fill(200);
		}

		rect(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public void mouseMoved() {
		// can do stuff everytime the mouse is moved (i.e., not clicked)
		// https://processing.org/reference/mouseMoved_.html
	}

	public void mouseDragged() {
		// can do stuff everytime the mouse is dragged
		// https://processing.org/reference/mouseDragged_.html
	}

	public void keyPressed() {
		// can use the keyboard if you wish
		// https://processing.org/reference/keyTyped_.html
		// https://processing.org/reference/keyCode.html
	}
}
