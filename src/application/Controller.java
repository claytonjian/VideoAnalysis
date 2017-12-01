package application;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.LineUnavailableException;

import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utilities.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Controller {

	@FXML
	private ImageView imageView; // the image display window in the GUI
	@FXML
	private Slider slider;
	@FXML
	private VBox vBox;
	@FXML
	private Label title;

	private Mat image;
	private VideoCapture capture;
	private VideoCapture capture2; // temp duplicate
	private ScheduledExecutorService timer;

	private int width;
	private int height;
	private double currentFrameNumber;
	private double framePerSecond;
	private double totalFrameCount;

	@FXML
	private void initialize() {
		width = 32;
		height = 32;
		title.setText("CMPT 365 Project");
	}

	private String getVideoFilename(File video) {
		return video.getAbsolutePath();
	}

	@FXML
	protected void openImage(ActionEvent event) throws InterruptedException {
		Stage openStage = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select file to open");
		File file = null;

		// Open dialog screen to open files
		try{
			file = fileChooser.showOpenDialog(openStage);
		}
		catch (Exception e){
			System.out.println("Open Dialog cannot be shown");
		}
		if (file == null){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("No file selected");
			alert.setContentText("Please choose a file before continuing!");
			alert.showAndWait();
		}
		else{
			capture = new VideoCapture(getVideoFilename(file)); // open video file

			// temp second file			
			capture2 = new VideoCapture(getVideoFilename(file)); // open video file
			//			

			title.setText(file.getName());
			if (capture.isOpened()) { // open successfully
				createFrameGrabber(0,0);
			}
			else{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("File is not a video");
				alert.setContentText("Please select a video file to proceed!");
				alert.showAndWait();
			}
		}
	}
	@FXML
	protected void stiByCopyingPixels(ActionEvent event) throws LineUnavailableException {
		if(capture != null){
			List<String> stiChoices = new ArrayList<>();
			stiChoices.add("STI from copying center columns");
			stiChoices.add("STI from copying center rows");
			stiChoices.add("STI from copying diagonals");

			ChoiceDialog<String> stiChoiceDialog = new ChoiceDialog<>("", stiChoices);
			stiChoiceDialog.setTitle("STI Type");
			stiChoiceDialog.setHeaderText("Select a method for generating an STI");
			stiChoiceDialog.setContentText("Choose your STI type");
			Optional<String> stiChoiceResult = stiChoiceDialog.showAndWait();
			if(stiChoiceResult.isPresent() && !stiChoiceResult.toString().substring(9, stiChoiceResult.toString().length() - 1).equals("")){
				String stiMethod = stiChoiceResult.toString().substring(9, stiChoiceResult.toString().length() - 1);
				// read image properties:		
				height = (int)capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				width = (int)capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
				int length = (int)capture.get(Videoio.CAP_PROP_FRAME_COUNT); // video length in frames
				image = new Mat();
				if(stiMethod.equals("STI from copying center columns")){
					// Read in first column as base for output:
					capture2.set(Videoio.CAP_PROP_POS_FRAMES, 0);
					capture2.read(image);
					Mat output = image.col(width/2).clone(); // first column
					for (int i=1; i<length; i++) {
						capture2.set(Videoio.CAP_PROP_POS_FRAMES, i);
						capture2.read(image);

						// concatenate each column with existing output:			
						Mat tempmat = image.col(width/2).clone();
						List<Mat> mats = Arrays.asList(output, tempmat);
						Core.hconcat(mats, output);
						Imgcodecs.imwrite("/Users/Clayton/Desktop/output.png", output);
						//						Imgcodecs.imwrite("output.png", output);

					}
				}
				else if(stiMethod.equals("STI from copying center rows")){
					// Read in first row as base for output:
					capture2.set(Videoio.CAP_PROP_POS_FRAMES, 0);
					capture2.read(image);
					Mat output = image.row(height/2).clone(); // first row
					for (int i=1; i<length; i++) {
						capture2.set(Videoio.CAP_PROP_POS_FRAMES, i);
						capture2.read(image);

						// concatenate each row with existing output:			
						Mat tempmat = image.row(height/2).clone();
						List<Mat> mats = Arrays.asList(output, tempmat);
						Core.vconcat(mats, output);
						//						Imgcodecs.imwrite("/Users/Clayton/Desktop/output.png", output);
						Imgcodecs.imwrite("output.png", output);

					}
					output = output.t();
					Imgcodecs.imwrite("/Users/Clayton/Desktop/output.png", output);
				}
				else{
					capture2.set(Videoio.CAP_PROP_POS_FRAMES, 0);
					capture2.read(image);
					Mat output = image.diag(0).clone(); // first row
					Size s = output.size();
					System.out.println("Rows: " + s.height + "\nColumns: " + s.width);
					for (int i=1; i<length; i++) {
						capture2.set(Videoio.CAP_PROP_POS_FRAMES, i);
						capture2.read(image);

						// concatenate each row with existing output:			
						Mat tempmat = image.diag(0).clone();
						List<Mat> mats = Arrays.asList(output, tempmat);
						Core.hconcat(mats, output);
						Imgcodecs.imwrite("/Users/Clayton/Desktop/output.png", output);
						//						Imgcodecs.imwrite("output.png", output);

					}
				}
			}
			else{
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("No STI method selected");
				alert.setContentText("Please select an STI method to continue");
				alert.showAndWait();
			}
		}
		else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("No video chosen");
			alert.setContentText("Please select a video file to proceed!");
			alert.showAndWait();
		}
	}
	@FXML
	protected void stiByHistogramDifferences(ActionEvent event) throws LineUnavailableException {
		height = (int)capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		width = (int)capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int length = (int)capture.get(Videoio.CAP_PROP_FRAME_COUNT);
		image = new Mat();
		Mat image2 = new Mat();
		Mat output = new Mat();
		for (int i=1; i<length; i++) { // in order to do differences, must begin at frame 1, not 0
			capture2.set(Videoio.CAP_PROP_POS_FRAMES, i-1);
			capture2.read(image);
			capture2.set(Videoio.CAP_PROP_POS_FRAMES, i);
			capture2.read(image2); // image2 is the next frame
// go through the image and change colour to rg chromaticity
			for (int j = 0;j<height;j++) {
				for (int k=0;k<width;k++) {
					double[] pixel = image.get(j,k);
					double r = pixel[0];
					double g = pixel[1];
					double b = pixel[2];
					double rgb = r+g+b;
					if (rgb == 0) { // avoid divide by zero in case of black
						pixel[0] = 255/3;
						pixel[1] = 255/3;
						pixel[2] = 0;
					}
					else {
						pixel[0] = 255*r/rgb;
						pixel[1] = 255*g/rgb;
						pixel[2] = 0;//b/rgb;
					}

					image.put(j, k, pixel); // set pixel to new value
				}
			}
			//			Imgcodecs.imwrite("frame"+i+".png", image2); 
			for (int j = 0;j<height;j++) { // same process for next frame
				for (int k=0;k<width;k++) {
					double[] pixel = image2.get(j,k);
					double r = pixel[0];
					double g = pixel[1];
					double b = pixel[2];
					double rgb = r+g+b;
					//					System.out.println("rgb is: "+rgb);
					if (rgb == 0) { // avoid divide by zero in case of black
						pixel[0] = 255/3;
						pixel[1] = 255/3;
						pixel[2] = 0;
					}
					else {
						pixel[0] = 255*r/rgb;
						pixel[1] = 255*g/rgb;
						pixel[2] = 0;//b/rgb;
					}

					image2.put(j, k, pixel); // set pixel to new value
				}
			}
			Mat histsti = new Mat(width, length, CvType.CV_32FC1);
			for (int r = 0; r<width; r++) {
				List<Mat> mat1 = Arrays.asList(image.col(r));
				List<Mat> mat2 = Arrays.asList(image2.col(r));
				MatOfInt channels = new MatOfInt(0,1);
				MatOfInt bins = new MatOfInt(7,7);
				MatOfFloat range = new MatOfFloat(0,256,0,256);
				Mat h0 = new Mat();
				Mat h1 = new Mat();

				Imgproc.calcHist(mat1, channels, new Mat(), h0, bins, range, false);

//				if (i==80) {
//					Imgcodecs.imwrite("histo"+i+".png", h0);
//				}
				
				Imgproc.calcHist(mat2, channels, new Mat(), h1, bins, range, false);
				Core.normalize(h0, h0, 1, 0, Core.NORM_L1, -1, new Mat());
				Core.normalize(h1, h1, 1, 0, Core.NORM_L1, -1, new Mat());
//				Scalar sum0 = Core.sumElems(h0);
//				Scalar sum1 = Core.sumElems(h1);
//				System.out.println("Sum of h0 is " + sum0);
//				System.out.println("Sum of h1 is " + sum0);
//				Imgcodecs.imwrite("histo"+r+".png", h0);
//				Imgcodecs.imwrite("histo1"+r+".png", h1);
				
				double histdiff = Imgproc.compareHist(h1, h0, Imgproc.CV_COMP_INTERSECT);
				histsti.put(r, i, histdiff);
			}
			Imgcodecs.imwrite("histdiff.png", histsti);
			


		}
		// insert code for 1.2
	}
	protected void createFrameGrabber(double curFrameNumber, double totFrameCount) throws InterruptedException {
		if (capture != null && capture.isOpened()) { // the video must be open
			framePerSecond = capture.get(Videoio.CAP_PROP_FPS);
			slider.setValue(0);
			// create a runnable to fetch new frames periodically
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					Mat frame = new Mat();
					if (capture.read(frame)) { // decode successfully
						Image im = Utilities.mat2Image(frame);
						Utilities.onFXThread(imageView.imageProperty(), im);
						currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
						totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
						slider.setValue(currentFrameNumber / totalFrameCount * (slider.getMax() - slider.getMin()));
					}
					else { // reach the end of the video
						capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);	// start from desired frame
					}
				}
			};
			// terminate the timer if it is running
			if (timer != null && !timer.isShutdown()) {
				timer.shutdown();
				timer.awaitTermination(Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
			}
			// run the frame grabber
			timer = Executors.newSingleThreadScheduledExecutor();
			timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
		}
	}
}
