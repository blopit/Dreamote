import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class videoCamera extends JPanel {

	VideoCapture camera;
	static final int hbins = 30;
	ArrayList<imm_line> proc_imm = new ArrayList<imm_line>();
	Point mx = new Point(0, 0);
	int minTime = 5;
	serialCommunicate test = new serialCommunicate();
	boolean inited = false;
	int fire = 0;
	boolean debug = false;
	
	public videoCamera(VideoCapture cam) {

		camera = cam;
		inited = test.initialize();
	}

	public class imm_line {
		MatOfPoint2f point;
		int val;
		boolean fired;

		public imm_line(MatOfPoint2f p) {
			val = 2;
			point = p;
			fired = false;
		}

		public void plus() {
			val += 2;
			if (val > 30) {
				val = 30;
			}
		}

		public void minus() {
			val--;
		}

	}

	public double polygonArea(ArrayList<Point> pnts) {
		double area = 0; 
		int j = pnts.size() - 1; 

		for (int i = 0; i < pnts.size(); i++) {
			area = area + (pnts.get(j).x + pnts.get(i).x)
					* (pnts.get(j).y - pnts.get(i).y);
			j = i; 
		}
		return area / 2;
	}

	public BufferedImage Mat2BufferedImage(Mat m) {

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); 
		BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) img.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return img;
	}

	public double pDistance(double x, double y, double x1, double y1,
			double x2, double y2) {

		double A = x - x1;
		double B = y - y1;
		double C = x2 - x1;
		double D = y2 - y1;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = -1;
		if (len_sq != 0) 
			param = dot / len_sq;

		double xx, yy;

		if (param < 0) {
			xx = x1;
			yy = y1;
		} else if (param > 1) {
			xx = x2;
			yy = y2;
		} else {
			xx = x1 + param * C;
			yy = y1 + param * D;
		}

		double dx = x - xx;
		double dy = y - yy;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double pointToLineDistance(Point v, Point w, Point p) {
		return pDistance(p.x, p.y, v.x, v.y, w.x, w.y);
	}

	public void paintComponent(Graphics g) {
		fire--;
		
		

		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		Mat mat = new Mat();
		camera.read(mat);
		Core.flip(mat, mat, 1);

		Mat source = mat.clone();

		Mat hist = source.clone();

		Mat l_src = source.clone();

		// Core.inRange(l_src, new Scalar(0, 0, 0), new Scalar(64, 64, 64),
		// l_src);
		/*
		 * l_src = threash(l_src); Imgproc.cvtColor(l_src, l_src,
		 * Imgproc.COLOR_RGB2GRAY); Mat destination = new
		 * Mat(source.rows(),source.cols(),source.type());
		 * Imgproc.equalizeHist(l_src, destination); Mat l_st =
		 * destination.clone();
		 */

		Imgproc.GaussianBlur(l_src, l_src, new Size(3, 3), 0);
		Imgproc.Canny(l_src, l_src, 64, 180);
		/*
		 * Imgproc.cvtColor(hist, hist, Imgproc.COLOR_RGB2GRAY); Mat roi =
		 * Mat.zeros(rows, cols, type)
		 * 
		 * Imgproc.calcHist(images, new MatOfInt(0, 1), null, hist, histSize,
		 * new MatOfFloat( 0,180,0,256 ));
		 * 
		 * //Imgproc.calcHist(fillImgs, channels, new Mat(), hist, histSize,
		 * ranges); //Imgproc.calcBackProject(dst, channels, hist, calcFrame,
		 * ranges, 1);
		 * 
		 * ArrayList<Mat> images = new ArrayList<Mat>(); images.add(source3);
		 * Imgproc.calcBackProject(images, new MatOfInt(0, 1), new MatOfFloat(
		 * 0,180,0,256 ), sourceo, ranges, 1);
		 */
		// Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2HSV);
		
		
		Imgproc.cvtColor(hist, hist, Imgproc.COLOR_RGB2GRAY);
		Mat bwise = new Mat();
		source.copyTo(bwise, hist);

		Mat dst = new Mat();
		Imgproc.cvtColor(bwise, dst, Imgproc.COLOR_RGB2HSV, 0);
		Mat H = new Mat();
		Core.extractChannel(dst, H, 0); // this extract hsv to one channel
		Core.inRange(dst, new Scalar(0, 58, 89), new Scalar(128, 255, 255),
				bwise);// output inRange in one channel
		// Imgproc.cvtColor(H, bwise, Imgproc.COLOR_RGB2RGBA, 0);//H in 1

		Imgproc.blur(source, source, new Size(9, 9));
		Imgproc.Canny(source, source, 0, 60);

		// Mat source2 = source.clone();
		source = bwise;

		// Imgproc.erode(source, kernel, kernel);

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(10, 10));
		Imgproc.morphologyEx(source, source, Imgproc.MORPH_ERODE, kernel);
		Imgproc.morphologyEx(l_src, l_src, Imgproc.MORPH_CLOSE, kernel);

		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,
				3));
		Imgproc.morphologyEx(l_src, l_src, Imgproc.MORPH_ERODE, kernel);

		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(
				23, 23));
		Imgproc.morphologyEx(source, source, Imgproc.MORPH_CLOSE, kernel);

		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,
				10));
		Imgproc.erode(source, kernel, kernel);
		// Imgproc.erode(l_src, kernel, kernel);

		Mat hands = source.clone();

		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,
				3));

		// Imgproc.erode(source, kernel, kernel);

		// Imgproc.erode(source, kernel, kernel);

		// Imgproc.morphologyEx(source, source, Imgproc.MORPH_OPEN, kernel);
		// Core.bitwise_not(source, source);
		// Imgproc.erode(source, kernel, kernel);
		// Imgproc.erode(source, kernel, kernel);

		ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
		Imgproc.findContours(source, contour, kernel, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		ArrayList<MatOfPoint> l_contour = new ArrayList<MatOfPoint>();
		Imgproc.findContours(l_src, l_contour, kernel, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		/*
		 * Mat lines = new Mat(); int threshold = 50; int minLineSize = 200; int
		 * lineGap = 20;
		 */

		// Imgproc.HoughLines(source, lines, 1, Math.PI / 180, threshold,
		// minLineSize, lineGap);

		BufferedImage image = Mat2BufferedImage(mat);
		g.drawImage(image, 10, 10, image.getWidth(), image.getHeight(), null);

		/*
		 * for (int x = 0; x < lines.cols(); x++) { double[] vec = lines.get(0,
		 * x); int x1 = (int) vec[0], y1 = (int) vec[1], x2 = (int) vec[2], y2 =
		 * (int) vec[3]; g.setColor(Color.GREEN); g.drawLine(x1, y1, x2, y2);
		 * 
		 * }
		 */
		g2.setStroke(new BasicStroke(4));

		ArrayList<imm_line> proc_imm_clone = new ArrayList<imm_line>(proc_imm);

		for (imm_line k : proc_imm_clone) {

			MatOfPoint2f approx = k.point;
			boolean draw = true;

			double dis = 4000;
			if (approx.total() == 2) {
				Point p1 = new Point(approx.get(0, 0)[0], approx.get(0, 0)[1]);
				Point p2 = new Point(approx.get(1, 0)[0], approx.get(1, 0)[1]);
				dis = pointToLineDistance(p1, p2, mx);
				
				if (dis < 32  && fire < 0) {
					fire = 1;
					int val =  (int)(100*percentageOfProjection(p1, p2, mx));
					if (inited) {
						test.sendData(Character.toString ((char) (100 + val)));
					}
				}
				
				
			} else {
				ArrayList<Point> pnts = new ArrayList<Point>();
				pnts.add(new Point(approx.get(0, 0)[0], approx.get(0, 0)[1]));
				pnts.add(new Point(approx.get(1, 0)[0], approx.get(1, 0)[1]));
				pnts.add(new Point(approx.get(2, 0)[0], approx.get(2, 0)[1]));
				pnts.add(new Point(approx.get(3, 0)[0], approx.get(3, 0)[1]));
				
				double ar = polygonArea(pnts) ;
				if (ar < 512 || ar > 5000 ) {
					draw = false;
				}
				
				if (contains(pnts, mx) && draw) {
					dis = 0;
					if (inited && !k.fired && fire < 0) {
						
						k.fired = true;
						fire = 5;
						
						double[] rgb = mat.get((int)mx.y, (int)mx.x);
						double max = Math.max(Math.max(rgb[0], rgb[1]), rgb[2]);
						
						if (max == rgb[0]) {
							test.sendData("b");
						} else if (max == rgb[1]) {
							test.sendData("c");
						} else if (max == rgb[2]) {
							test.sendData("a");
						} else {
							//test.sendData("");
						}
						
					}
				} else {
					k.fired = false;
				}
				
				
			}
			// g2.setStroke(new BasicStroke((float) (dis/2)));

			g2.setColor(Color.yellow);

			if (k.val > minTime && draw) {
				if (dis < 32) {
					k.plus();
					g2.setColor(Color.orange);
				}

				double[] temp_double = approx.get((int) approx.total() - 1, 0);
				Point p = new Point(temp_double[0], temp_double[1]);
				Point n;
				for (int i = 0; i < approx.total(); i++) {
					temp_double = approx.get(i, 0);
					n = new Point(temp_double[0], temp_double[1]);
					
					if (debug) {
						g2.drawLine((int) p.x, (int) p.y, (int) n.x, (int) n.y);
					}
					p = n;
				}
			}

			k.minus();
			if (k.val < 0) {
				proc_imm.remove(k);
			}
		}
		g2.setStroke(new BasicStroke(2));

		for (MatOfPoint c : l_contour) {
			MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
			MatOfPoint2f approx = new MatOfPoint2f();
			MatOfPoint approxf1 = new MatOfPoint();

			Imgproc.approxPolyDP(c2f, approx,
					Imgproc.arcLength(c2f, true) * 0.055, true);

			// Imgproc.drawContours(source, contour, 0, new Scalar(0, 255, 0));

			if (approx.total() == 2 || approx.total() == 4) {

				boolean done = false;
				for (imm_line p : proc_imm) {
					if (compareMatOfPoint(p.point, approx)) {
						p.plus();
						done = true;
					}
				}
				if (!done) {
					proc_imm.add(new imm_line(approx));
				}

				ArrayList<Point> cur = new ArrayList<Point>();
				double[] temp_double = approx.get((int) approx.total() - 1, 0);
				Point p = new Point(temp_double[0], temp_double[1]);
				cur.add(p);
				Point n;
				for (int i = 0; i < approx.total(); i++) {
					temp_double = approx.get(i, 0);
					n = new Point(temp_double[0], temp_double[1]);
					/*
					 * if (approx.total() == 4) { g.setColor(Color.green);
					 * g.drawLine((int) p.x, (int) p.y, (int) n.x, (int) n.y); }
					 */
					p = n;
				}
			}

		}

		ArrayList<Point> pnts = new ArrayList<Point>();
		double max = 0;

		for (MatOfPoint c : contour) {
			MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
			MatOfPoint2f approx = new MatOfPoint2f();
			MatOfPoint approxf1 = new MatOfPoint();

			Imgproc.approxPolyDP(c2f, approx,
					Imgproc.arcLength(c2f, true) * 0.02, true);

			// Imgproc.drawContours(source, contour, 0, new Scalar(0, 255, 0));

			if (approx.total() < 200) {
				ArrayList<Point> cur = new ArrayList<Point>();
				double[] temp_double = approx.get((int) approx.total() - 1, 0);
				Point p = new Point(temp_double[0], temp_double[1]);
				cur.add(p);
				Point n;
				for (int i = 0; i < approx.total(); i++) {
					temp_double = approx.get(i, 0);
					n = new Point(temp_double[0], temp_double[1]);
					p = n;
					cur.add(p);
				}
				if (polygonArea(cur) > max) {
					max = polygonArea(cur);
					pnts = cur;
				}
			}

		}

		double tx = 0, ty = 0;
		max = 0;
		if (pnts.size() >= 3) {

			for (int i = 0; i < pnts.size(); i++) {
				Point c = pnts.get(i);
				Point n = new Point(0, 0);
				Point p = new Point(0, 0);

				if (i - 1 < 0) {
					p = pnts.get(pnts.size() - 1);
					n = pnts.get(i + 1);
				} else if (i + 1 > pnts.size() - 1) {
					p = pnts.get(i - 1);
					n = pnts.get(0);
				} else {
					p = pnts.get(i - 1);
					n = pnts.get(i + 1);
				}

				tx += c.x;
				ty += c.y;
				g.setColor(Color.RED);
				if (debug) {
					g.drawLine((int) p.x, (int) p.y, (int) c.x, (int) c.y);
				}
			}
		}
		tx /= pnts.size();
		ty /= pnts.size();

		for (Point p : pnts) {
			double dis = Math.sqrt(Math.pow(p.x - tx, 2)
					+ Math.pow(p.y - ty, 2))
					+ p.y / 2;
			if (dis > max) {
				max = dis;
				mx = p;
			}
		}

		g.fillOval((int) mx.x - 12, (int) mx.y - 12, 24, 24);

	}

	public boolean compareMatOfPoint(MatOfPoint2f point, MatOfPoint2f approx) {
		MatOfPoint2f c1f = new MatOfPoint2f(point.toArray());
		MatOfPoint2f c2f = new MatOfPoint2f(approx.toArray());

		Point p1A = new Point(c1f.get(0, 0)[0], c1f.get(0, 0)[1]);
		Point p1B = new Point(c1f.get(1, 0)[0], c1f.get(1, 0)[1]);
		Point p2A = new Point(c2f.get(0, 0)[0], c2f.get(0, 0)[1]);
		Point p2B = new Point(c2f.get(1, 0)[0], c2f.get(1, 0)[1]);

		if (distance(p1A, p2A) < 4 && distance(p1B, p2B) < 4) {
			return true;
		} else if (distance(p1A, p2B) < 4 && distance(p1B, p2A) < 4) {
			return true;
		}
		return false;
	}

	public double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}

	public double angleBetween(Point center, Point current, Point previous) {

		return Math.toDegrees(Math.atan2(current.x - center.x, current.y
				- center.y)
				- Math.atan2(previous.x - center.x, previous.y - center.y));
	}

	public static boolean contains(ArrayList<Point> points, Point test) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
			if ((points.get(i).y > test.y) != (points.get(j).y > test.y)
					&& (test.x < (points.get(j).x - points.get(i).x)
							* (test.y - points.get(i).y)
							/ (points.get(j).y - points.get(i).y)
							+ points.get(i).x)) {
				result = !result;
			}
		}
		return result;
	}
	
	public double percentageOfProjection(Point startPoint, Point endPoint, Point testPoint) {

		double scalar = ((endPoint.x - startPoint.x) * (testPoint.x - startPoint.x)
				+ (endPoint.y - startPoint.y) * (testPoint.y - startPoint.y))
				/ (Math.pow((endPoint.x - startPoint.x), 2)
						+ Math.pow((endPoint.y - startPoint.y), 2));
		int ProjectionX = (int) (startPoint.x + ((endPoint.x - startPoint.x) * scalar));
		int ProjectionY = (int) (startPoint.y + ((endPoint.y - startPoint.y) * scalar));
		Point newProjectionPoint = new Point(ProjectionX, ProjectionY);
		double linefromStarttoEnd = distance(startPoint, endPoint);
		double linefromProjectiontoEnd = distance(startPoint, newProjectionPoint);
		return (double) (linefromProjectiontoEnd / linefromStarttoEnd);
	}

	public Mat turnGray(Mat img)

	{
		Mat mat1 = new Mat();
		Imgproc.cvtColor(img, mat1, Imgproc.COLOR_RGB2GRAY);
		return mat1;
	}

	public Mat threash(Mat img) {
		Mat threshed = new Mat();
		int SENSITIVITY_VALUE = 100;
		Imgproc.threshold(img, threshed, SENSITIVITY_VALUE, 255,
				Imgproc.THRESH_BINARY);
		return threshed;
	}
}
