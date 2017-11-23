package application;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.LineUnavailableException;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utilities.Utilities;

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
	protected void createSTI(){
	}
	@FXML
	protected void stiByCopyingPixels(ActionEvent event) throws LineUnavailableException {

		height = (int)capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		width = (int)capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int length = (int)capture.get(Videoio.CAP_PROP_FRAME_COUNT);
		System.out.println("Has " + (int)length + " frames");
		System.out.println("Has " + height + " height");
		System.out.println("Has " + width + " width");

		image = new Mat();
		Mat output = new Mat();
		for (int i=0; i<length; i++) {
			capture2.set(Videoio.CAP_PROP_POS_FRAMES, i);
			//capture.set(2, 1);
			capture2.read(image);
			Imgcodecs.imwrite("frame"+i+".png", image);
		}
		// insert code for 1.1
	}
	@FXML
	protected void stiByHistogramDifferences(ActionEvent event) throws LineUnavailableException {
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
