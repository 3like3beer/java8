package ch.gma.image;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.ORB;

public class OrbUtility {

	private Mat descriptors = new Mat();
	
	private MatOfKeyPoint keyPoints = new MatOfKeyPoint();
	
	private ORB orb = org.opencv.features2d.ORB.create();
	
	private Mat mask = new Mat();

	private Mat inputMat;
	

	public OrbUtility(int maxFeatures, Mat inputMat) {
		this.inputMat = inputMat;
		orb.setMaxFeatures(maxFeatures);
		orb.detectAndCompute(this.inputMat,mask,keyPoints,descriptors);
	}

	public Mat getDescriptors() {
		return descriptors;
	}
	

	public MatOfKeyPoint getKeyPoints() {
		return keyPoints;
	}
	
	
}
