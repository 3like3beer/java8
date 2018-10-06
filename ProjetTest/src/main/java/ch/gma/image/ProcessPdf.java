package ch.gma.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.LoggerFactory;

public class ProcessPdf {



	private static final int MAX_DISTANCE = 90;
	private static final int MAX_FEATURES = 2000;
	private static final long GOOD_MATCH_PERCENT = (long) 10;
	private static final double WEIGHT_ONLINE = 0.5;

	private static org.slf4j.Logger LOG = LoggerFactory.getLogger(ProcessPdf.class.getName());

	public List<BufferedImage> toJpg(byte[] input) throws IOException {
		List<BufferedImage> images = new ArrayList<> (); 
		PDDocument doc = PDDocument.load ( input);
		PDFRenderer d = new PDFRenderer (doc);

		int index = doc.getNumberOfPages ();
		for ( int i = 0; i <= index - 1; i ++ ) {
			images.add(d.renderImageWithDPI(i,450,ImageType.GRAY));
		}
		doc.close();

		return images;
	}

	public static Mat getDiff(Mat online, Mat aligned) {
		Mat ssimMap = new Mat();
		SsimComputer comp = new SsimComputer(online, aligned);
		Scalar norm = new Scalar(255);
		Core.multiply(comp.getOut(), norm, ssimMap);

		Imgproc.blur(ssimMap, ssimMap, new Size(10,15));
		Imgproc.blur(ssimMap, ssimMap, new Size(10,10));
		Imgproc.blur(ssimMap, ssimMap, new Size(10,10));
		Imgproc.blur(ssimMap, ssimMap, new Size(10,10));
		Imgproc.threshold(ssimMap, ssimMap,40, 255, Imgproc.THRESH_BINARY);
		ssimMap.convertTo(ssimMap, CvType.CV_8UC1);
		Mat color = addContours(ssimMap,aligned);
		return color;
	}

	private static Mat addContours(Mat diff, Mat aligned) {
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(diff, contours, new Mat(), 1 //Imgproc.CV_RETR_EXTERNAL 
				, 2 //Imgproc.CV_CHAIN_APPROX_SIMPLE 
				);

		Mat color = new Mat();
		Imgproc.cvtColor(aligned, color, Imgproc.COLOR_GRAY2BGR,0);

		contours.stream()
		.map(Imgproc::boundingRect)
		.filter(rect -> rect.tl().x>0)
		.forEach(rect -> Imgproc.rectangle(color, rect.tl(),rect.br(),new Scalar(0, 0, 255),5));

		LOG.debug("Contour size : {}",contours.size());
		return color;
	}

	public static double getSsimMat(Mat online, Mat alined) {
		SsimComputer comp = new SsimComputer(online, alined);
		return comp.getSsim();
	}

	public static Mat align(Mat online, Mat paper) {
		OrbUtility orbOnline = new OrbUtility(MAX_FEATURES, online);
		OrbUtility orbPaper = new OrbUtility(MAX_FEATURES, paper );

		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(orbOnline.getDescriptors(), orbPaper.getDescriptors(), matches);

		List<DMatch> listOfMatches = matches.toList();
		listOfMatches.sort((DMatch o1, DMatch o2) ->
		Float.valueOf(o1.distance).compareTo(Float.valueOf(o2.distance))
				);
		LOG.debug("Initial listOfMatches size : {}",listOfMatches.size());
		listOfMatches = listOfMatches.stream().limit((long) listOfMatches.size() * GOOD_MATCH_PERCENT /100).collect(Collectors.toList());;
		LOG.debug("Best listOfMatches size : {}",listOfMatches.size());
		matches.fromList(listOfMatches);

		Mat outImg = new Mat();
		//Features2d.drawMatches(online, orbOnline.getKeyPoints(), 
		//		paper, orbpaper.getKeyPoints(), matches, outImg);
		// Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\outImg.png", outImg);

		List<Point> onlineList = new LinkedList<>();
		List<Point> paperList = new LinkedList<>();
		List<DMatch> listOfGoodMatches = new LinkedList<>(); 
		for (DMatch dMatch : listOfMatches) {
			Point ptOnline = orbOnline.getKeyPoints().toList().get(dMatch.queryIdx).pt;
			Point ptPaper = orbPaper.getKeyPoints().toList().get(dMatch.trainIdx).pt;
			if (Math.abs(ptOnline.x - ptPaper.x) + Math.abs(ptOnline.y - ptPaper.y)< MAX_DISTANCE ) {
				onlineList.add(ptOnline);
				paperList.add(ptPaper);
				listOfGoodMatches.add(dMatch);
			}
		}
		LOG.debug("listOfGoodMatches size : {}",listOfGoodMatches.size());

		MatOfDMatch goodMatches = new MatOfDMatch();
		goodMatches.fromList(listOfGoodMatches);
		//		Features2d.drawMatches(online, orbOnline.getKeyPoints(), 
		//				paper, orbpaper.getKeyPoints(), goodMatches, outImg);
		// Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\goodMatches.png", outImg);

		MatOfPoint2f onlinePoints = new MatOfPoint2f();
		onlinePoints.fromList(onlineList);

		MatOfPoint2f paperPoints = new MatOfPoint2f();
		paperPoints.fromList(paperList);

		Mat H = Calib3d.findHomography(paperPoints,onlinePoints);
		Mat paperAligned = new Mat();
		Imgproc.warpPerspective(paper, paperAligned , H, new Size(online.cols(),online.rows()));

		// Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\paperAlined.png", paperAlined);
		Mat fusion = new Mat();
		Core.addWeighted(online, WEIGHT_ONLINE, paperAligned, 1-WEIGHT_ONLINE, 0, fusion);
		// Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\fusion.png", fusion);
		return fusion;
	}

	public static Mat readImageIntoMat(BufferedImage image,String name) throws Exception {
		BufferedImage imageCopy =
				new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		imageCopy.getGraphics().drawImage(image, 0, 0, null);
		byte[] data = ((DataBufferByte) imageCopy.getRaster().getDataBuffer()).getData();  

		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);
		Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC3);
		Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

		Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\" + name, mat1);

		return mat1;
	}

	static BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {        
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".png", matrix, mob);
		byte ba[] = mob.toArray();

		BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
		return bi;
	}

	protected byte[] readBytesFromFile(String filePath) {
		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;
		try {
			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];
			// read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytesArray;
	}

}
