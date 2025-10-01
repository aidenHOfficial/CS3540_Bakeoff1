package cs3540;

import java.awt.AWTException;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
import processing.core.PImage;
import java.awt.Point;

public class BakeOff1 extends PApplet {
	// when in doubt, consult the Processsing reference:
	// https://processing.org/reference/
	// The argument passed to main must match the class name
	public static void main(String[] args) {
		// Tell processing what class we want to run.
		PApplet.main("cs3540.BakeOff1");
	}

	int margin = 200; // set the margin around the squares
	final int padding = 50; // padding between buttons and also their width/height
	final int buttonSize = 40; // padding between buttons and also their width/height
	ArrayList<Integer> trials = new ArrayList<Integer>(); // contains the order of buttons that activate in the test
	int trialNum = 0; // the current trial number (indexes into trials array above)
	int startTime = 0; // time starts when the first click is captured
	int finishTime = 0; // records the time of the final click
	int hits = 0; // number of successful clicks
	int misses = 0; // number of missed clicks
	Robot robot; // initialized in setup
	int windowSize = 700;
	
	PImage arrowImage;

	int numRepeats = 1; // sets the number of times each button repeats in the test

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
		// noCursor(); // hides the system cursor if you want
		noStroke(); // turn off all strokes, we're just using fills here (can change this if you
					// want)
		textFont(createFont("Arial", 16)); // sets the font to Arial size 16
		textAlign(CENTER);
		frameRate(60); // normally you can't go much higher than 60 FPS.
		ellipseMode(CENTER); // ellipses are drawn from the center (BUT RECTANGLES ARE NOT!)
		// rectMode(CENTER); //enabling will break the scaffold code, but you might find
		// it easier to work with centered rects

		try {
			robot = new Robot(); // create a "Java Robot" class that can move the system cursor
		} catch (AWTException e) {
			e.printStackTrace();
		}

		// ===DON'T MODIFY MY RANDOM ORDERING CODE==
		for (int i = 0; i < 16; i++) // generate list of targets and randomize the order
			// number of buttons in 4x4 grid
			for (int k = 0; k < numRepeats; k++)
				// number of times each button repeats
				trials.add(i);

		Collections.shuffle(trials); // randomize the order of the buttons
		System.out.println("trial order: " + trials); // print out order for reference

		surface.setLocation(0, 0);// put window in top left corner of screen (doesn't always work)
		
		arrowImage = loadImage("arrow.png"); // Replace with your image file
		imageMode(CENTER);
		arrowImage.resize(buttonSize-10, buttonSize-10);
	}

	public void draw() {
		background(0); // set background to black

		if (trialNum >= trials.size()) // check to see if test is over
		{
			float timeTaken = (finishTime - startTime) / 1000f;
			float penalty = constrain(((95f - ((float) hits * 100f / (float) (hits + misses))) * .2f), 0, 100);
			fill(255); // set fill color to white
			// write to screen (not console)
			text("Finished!", width / 2, height / 2);
			text("Hits: " + hits, width / 2, height / 2 + 20);
			text("Misses: " + misses, width / 2, height / 2 + 40);
			text("Accuracy: " + (float) hits * 100f / (float) (hits + misses) + "%", width / 2, height / 2 + 60);
			text("Total time taken: " + timeTaken + " sec", width / 2, height / 2 + 80);
			text("Average time for each button: " + nf((timeTaken) / (float) (hits + misses), 0, 3) + " sec", width / 2,
					height / 2 + 100);
			text("Average time for each button + penalty: "
					+ nf(((timeTaken) / (float) (hits + misses) + penalty), 0, 3) + " sec", width / 2,
					height / 2 + 140);
			return; // return, nothing else to do now test is over
		}

		fill(255); // set fill color to white
		text((trialNum + 1) + " of " + trials.size(), 40, 20); // display what trial the user is on

		drawOmbreBackground();
		
		for (int i = 0; i < 16; i++) {
			drawOmbreButton(i);
			drawArrowButton(i);
		}
		
		fill(255, 0, 0, 200); // set fill color to translucent red
		ellipse(mouseX, mouseY, 20, 20); // draw user cursor as a circle with a diameter of 20

	}

	public void mousePressed() // test to see if hit was in target!
	{
		if (trialNum >= trials.size()) // check if task is done
			return;

		if (trialNum == 0) // check if first click, if so, record start time
			startTime = millis();

		if (trialNum == trials.size() - 1) // check if final click
		{
			finishTime = millis();
			// write to terminal some output:
			System.out.println("we're all done!");
		}

		Rectangle bounds = getButtonLocation(trials.get(trialNum));

		// check to see if cursor was inside button
		if ((mouseX > bounds.x && mouseX < bounds.x + bounds.width)
				&& (mouseY > bounds.y && mouseY < bounds.y + bounds.height)) // test to see if hit was within bounds
		{
			System.out.println("HIT! " + trialNum + " " + (millis() - startTime)); // success
			hits++;
		} else {
			System.out.println("MISSED! " + trialNum + " " + (millis() - startTime)); // fail
			misses++;
		}

		trialNum++; // Increment trial number

		// in this example design, I move the cursor back to the middle after each click
		// Note. When running from eclipse the robot class affects the whole screen not
		// just the GUI, so the mouse may move outside of the GUI.
//		robot.mouseMove(width/2, (height)/2); //on click, move cursor to roughly
		// center of window!
	}

	// probably shouldn't have to edit this method
	public Rectangle getButtonLocation(int i) // for a given button ID, what is its location and size
	{
		int x = (i % 4) * (padding + buttonSize) + margin;
		int y = (i / 4) * (padding + buttonSize) + margin;

		return new Rectangle(x, y, buttonSize, buttonSize);
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

	    double segmentDist = (endDist - 100) / steps;
	    double targetXCenter = xTarget + (buttonSize / 2.0);
	    double targetYCenter = yTarget + (buttonSize / 2.0);

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
	                                               20, startDist, endDist, buttonSize);
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

	    double relativeX = bounds.x - targetBounds.x;
	    double relativeY = bounds.y - targetBounds.y;

	    float ang = (float) ((180 * Math.atan2(relativeY, relativeX)) / Math.PI) - 180;

	    if (trials.get(trialNum) != i) {
	        // Just draw the arrow over the ombre
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
