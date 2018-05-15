package com.extwo.src;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import javax.swing.*;

/*
 * Excercise 2 handin:
 * participants:
 * Daniel Ben Zvi 301770640
 * Keren Gold 305277287
 * Sagi Fridman 305010969
 * Sharon ? ?
 */
public class Screen extends JFrame implements Runnable, MouseListener, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static Screen instance = null;
	
	/*
	 * Singleton pattern
	 */
	public static Screen getInstance() {
		if (null == instance)
			instance = new Screen();
		
		return instance;
	}
	
	//dimensions
	private int width = 800;
	private int height = 600;
	
	//how many lines to use to draw
	private int granularity = 10;
	
	//all the visual components
	private JFrame controls = null;
	
	private JPanel content = null;
	private JPanel canvas = null;
	
	private JButton clear = null;
	private JTextField granularityF = null;
	private JComboBox<String> colorB = null;
	private JComboBox<String> shapeB = null;
	
	private JLabel translateL = null;
	private JTextField offsetXF = null;
	private JTextField offsetYF = null;
	private JLabel scaleL = null;
	private JTextField scaleXF = null;
	private JTextField scaleYF = null;
	private JLabel rotateL = null;
	private JTextField rotateF = null;
	
	private JCheckBox mirrX = null;
	private JCheckBox mirrY = null;
	
	private JLabel shearL = null;
	private JTextField shearXF = null;
	private JTextField shearYF = null;
	
	//the graphics used
	private BufferedImage img = null;
	private Graphics2D g = null;
	
	//the data collected
	private Shape shape = null;
	private Point[] points = new Point[4];
	private int count = 0, cap = 2;
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	/*
	 * defining the basic parameters before drawing
	 */
	public void draw() {
		//default value is 10
		try {
			granularity = Integer.parseInt(granularityF.getText());
			if (0 == granularity)
				granularity = 10;
		} catch (NumberFormatException e) {
			granularity = 10;
		}
	}

	/*
	 * drawing a single pixel with the selected color
	 */
	public void drawPixel(int x, int y) {
		switch (colorB.getSelectedItem().toString()) {
			case "white":
				g.setColor(Color.white);
				break;
			case "green":
				g.setColor(Color.green);
				break;
			case "yellow":
				g.setColor(Color.yellow);
				break;
		}
		
		//drawing a single pixel
		g.drawLine(x, y, x, y);
	}
	
	/*
	 * drawing a single pixel
	 */
	public void drawPixel(Point point) {
		drawPixel(point.x, point.y);
	}
	
	/*
	 * drawing a group of pixels
	 */
	public void drawPixels(Point... points) {
		for (Point point : points)
			drawPixel(point);
	}
	
	public void drawPixels(Matrix vector) {
		drawPixel((int)vector.matrix[0][0], (int)vector.matrix[0][1]);
	}
	
	/*
	 * drawing a single line
	 */
	public void drawLine(int x1, int y1, int x2, int y2, java.util.List<Function<Matrix, Matrix>> transformations) {
		Matrix vector = new Matrix(1,3,new double[][]{{0,0,1}});
		for (double t = 0, d = 1./Point2D.distance(x1, y1, x2, y2); t <= 1; t += d){

			vector.matrix[0][0] = Math.round(x1 + t*(x2-x1));
			vector.matrix[0][1] = Math.round(y1 + t*(y2-y1));
			
			for (Function<Matrix, Matrix> transformation: transformations)
				vector = transformation.apply(vector);
			
			drawPixels(vector);
		}
	}
	
	/*
	 * drawing a single line
	 */
	public void drawLine(Point point1, Point point2, java.util.List<Function<Matrix, Matrix>> transformations) {
		drawLine(point1.x, point1.y, point2.x, point2.y, transformations);
	}
	
	/*
	 * drawing a single line
	 */
	public void drawLine(MyLine line) {
		drawLine(line.getStart(), line.getFinish(), line.getTransformations());
	}
	
	/*
	 * deprecated, ignore this method!
	 * drawing a single circle using straight lines
	 */
	@Deprecated
	public void drawCircleLines(MyCircle circle) {
		draw();
		
		int x = 0, y = 0;
		Point base = (Point)circle.base.clone();
		
		//the process for calculating the x & y, loops 4 times for the point mirroring
		for (int i = 0; i < 4; ++i) {
			
			//the calculation of the current quarter 
			for (double t = 0; t <= Math.PI; t += Math.PI/(granularity/2.)) {
				if (t != 0)
					points[0] = new Point(x, y);
				
				int m1 = 1;
				int m2 = 1;
				
				switch (i) {
					case 1:
						m1 *= -1;
						break;
					case 2:
						m1 *= -1;
						m2 *= -1;
						break;
					case 3:
						m2 *= -1;
						break;
				}
				
				x = base.x + m1*(int)Math.round(circle.rad * Math.cos(t));
				y = base.y + m2*(int)Math.round(circle.rad * Math.sin(t));
				
				if (t != 0) {
					points[1] = new Point(x, y);
					drawLine(points[0], points[1],null);
				}
			}
		}
	}
	
	/*
	 * drawing a single circle using pixels
	 */
	public void drawCirclePixels(MyCircle circle) {
		draw();
		
		Matrix vector = new Matrix(1,3,new double[][]{{0,0,1}});
		Point base = (Point)circle.base.clone();
		
		for (double t = 0; t <= 1.; t += 1./((2.*Math.PI*circle.rad)/8.)) {
			
			double offsetX = Math.round(circle.rad * Math.cos(t));
			double offsetY = Math.round(circle.rad * Math.sin(t));
			
			for (int i = 0; i < 8; ++i) {
				switch (i) {
					case 4:
						offsetY = (int)Math.round(circle.rad * Math.cos(t));
						offsetX = (int)Math.round(circle.rad * Math.sin(t));
					case 0:
						vector.matrix[0][0] = base.x + offsetX;
						vector.matrix[0][1] = base.y + offsetY;
						break;
					case 1:
					case 5:
						vector.matrix[0][0] = base.x - offsetX;
						vector.matrix[0][1] = base.y + offsetY;
						break;
					case 2:
					case 6:
						vector.matrix[0][0] = base.x + offsetX;
						vector.matrix[0][1] = base.y - offsetY;
						break;
					case 3:
					case 7:
						vector.matrix[0][0] = base.x - offsetX;
						vector.matrix[0][1] = base.y - offsetY;
						break;
				}
				
				for (Function<Matrix, Matrix> transformation: circle.getTransformations())
					vector = transformation.apply(vector);
				
				drawPixels(vector);
			}
		}
	}
	
	/*
	 * drawing a single curve
	 */
	public void drawCurve(MyCurve curve) {
		draw();
		
		int x = 0, y = 0;
		double ax = 0, bx = 0, cx = 0, dx = 0;
		double ay = 0, by = 0, cy = 0, dy = 0;
		
		//calculation of ax bx cx dx
		ax = -curve.points.get(0).x + 3*curve.points.get(1).x -3*curve.points.get(2).x + curve.points.get(3).x;
		bx = 3*curve.points.get(0).x -6*curve.points.get(1).x + 3*curve.points.get(2).x;
		cx = -3*curve.points.get(0).x + 3*curve.points.get(1).x;
		dx = curve.points.get(0).x;
		
		//calculation of ay by cy dy
		ay = -curve.points.get(0).y + 3*curve.points.get(1).y -3*curve.points.get(2).y + curve.points.get(3).y;
		by = 3*curve.points.get(0).y -6*curve.points.get(1).y + 3*curve.points.get(2).y;
		cy = -3*curve.points.get(0).y + 3*curve.points.get(1).y;
		dy = curve.points.get(0).y;
		
		//processing and calculating the following points and lines to draw base on the "Bezzier Curve"
		//however there seems to be a problem with drawing a looped curve, i would appriciate help with that
		for (double t = 0; t <= 1; t += 1./granularity) {
			if (t != 0)
				points[0] = new Point(x, y);
			
			x = (int)Math.round(ax*Math.pow(t, 3) + bx*Math.pow(t, 2) + cx*t + dx);
			y = (int)Math.round(ay*Math.pow(t, 3) + by*Math.pow(t, 2) + cy*t + dy);
			
			if (t != 0) {
				points[1] = new Point(x, y);
				drawLine(points[0], points[1], curve.getTransformations());
			}
		}
	}
	
	public void drawRectangle(MyRectangle rectangle) {
		Point p1 = rectangle.p1, p2 = rectangle.p2;
		drawLine(p1.x, p1.y, p1.x, p1.y+(p2.y-p1.y), rectangle.getTransformations());
		drawLine(p1.x, p1.y, p1.x+(p2.x-p1.x), p1.y, rectangle.getTransformations());
		drawLine(p1.x+(p2.x-p1.x), p1.y, p2.x, p2.y, rectangle.getTransformations());
		drawLine(p1.x, p1.y+(p2.y-p1.y), p2.x, p2.y, rectangle.getTransformations());
	}
	
	public void drawComposite(CompositeShape cs) {
		cs.prepare();
		for (Shape shape: cs.shapes) {
			shape.setTransformations(cs.getTransformations());
			shape.draw(this);
		}
	}
	
	/*
	 * displaying the window
	 */
	public void display() {
		try {
			SwingUtilities.invokeAndWait(this);
			g = (Graphics2D) img.getGraphics();
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * preparation of all visual components
	 */
	public void init() {
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		shapeB = new JComboBox<>();
		shapeB.addItem("line");
		shapeB.addItem("circle");
		shapeB.addItem("curve");
		shapeB.addItem("rect");
		shapeB.addItem("composite");
		shapeB.addActionListener(this);
		
		clear = new JButton("clear");
		clear.addActionListener(this);
		
		granularityF = new JTextField("10", 3);
		
		colorB = new JComboBox<>();
		colorB.addItem("white");
		colorB.addItem("green");
		colorB.addItem("yellow");
		
		canvas = new JPanel(new FlowLayout()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				((Graphics2D)g).drawImage(img, null, 0, 0);
				repaint();
			}
			
		};
		canvas.setSize(width, height);
		canvas.addMouseListener(this);
		
		content = new JPanel(new FlowLayout());
		content.add(shapeB);
		content.add(clear);
		content.add(new JLabel("granularity"));
		content.add(granularityF);
		content.add(colorB);
		content.setSize(width + 18, height + 48);
		
		translateL = new JLabel("transelate x,y");
		offsetXF = new JTextField("0", 3);
		offsetYF = new JTextField("0", 3);
		scaleL = new JLabel("scale% x,y (0-1)");
		scaleXF = new JTextField("1", 2);
		scaleYF = new JTextField("1", 2);
		rotateL = new JLabel("deg");
		rotateF = new JTextField("0", 2);
		
		content.add(translateL);
		content.add(offsetXF);
		content.add(offsetYF);
		content.add(scaleL);
		content.add(scaleXF);
		content.add(scaleYF);
		content.add(rotateL);
		content.add(rotateF);
		
		mirrX = new JCheckBox("mX");
		mirrY = new JCheckBox("mY");
		
		content.add(mirrX);
		content.add(mirrY);
		
		shearL = new JLabel("shear x,y");
		shearXF = new JTextField("0", 3);
		shearYF = new JTextField("0", 3);
		
		content.add(shearL);
		content.add(shearXF);
		content.add(shearYF);
		
		setSize(width + 7, height + 38);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		setVisible(true);
		
		controls = new JFrame("controls");
		controls.setSize(500, 100);
		controls.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		controls.setLocationRelativeTo(this);
		controls.setResizable(false);
		controls.setLayout(new BorderLayout());
		controls.add(content, BorderLayout.CENTER);
		controls.setVisible(true);
	}

	@Override
	public void run() {
		run(width, height);
	}
	
	public void run(int width, int height) {
		this.width = width;
		this.height = height;
		init();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		points[count] = new Point(e.getPoint());
		drawPixel(points[count++]);
		if (count == cap) {
			shape = makeShape();
			shape.translate(-shape.getCenter().getX(), -shape.getCenter().getY());
			shape.shear(Double.parseDouble(shearXF.getText()),Double.parseDouble(shearYF.getText()));
			shape.rotate(Double.parseDouble(rotateF.getText()));
			shape.scale(Double.parseDouble(scaleXF.getText()),Double.parseDouble(scaleYF.getText()));
			shape.mirror(mirrX.isSelected(), mirrY.isSelected());
			shape.translate(shape.getCenter().getX(), shape.getCenter().getY());
			shape.translate(Double.parseDouble(offsetXF.getText()),-Double.parseDouble(offsetYF.getText()));
			shape.draw(this);
			count = 0;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if (str.equals("comboBoxChanged"))
			str = shapeB.getSelectedItem().toString();
		
		switch (str) {
			case "line":
				count = 0;
				cap = 2;
				break;
			case "circle":
				count = 0;
				cap = 2;
				break;
			case "curve":
				count = 0;
				cap = 4;
				break;
			case "rect":
				count = 0;
				cap = 2;
				break;
			case "polygon":
				break;
			case "composite":
				count = 0;
				cap = 1;
				break;
			case "clear":
				g.clearRect(0, 0, width, height);
				break;
		}
	}
	
	private Shape makeShape() {
		switch (shapeB.getSelectedItem().toString()) {
			case "line":
				return new MyLine(points);
			case "circle":
				return new MyCircle(points);
			case "curve":
				return new MyCurve(points);
			case "rect":
				return new MyRectangle(points);
			case "composite":
				return new CompositeShape(points[0]);
			default:
				return null;
		}
	}
}
