package com.extwo.src;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/*
 * Excercise 2 handin:
 * participants:
 * Daniel Ben Zvi 301770640
 * Keren Gold 305277287
 * Sagi Fridman 305010969
 * Sharon ? ?
 */
public interface Shape {
	
	public static double deg2rad(double deg) {
		return deg*(Math.PI/180.);
	}
	
	public List<Function<Matrix, Matrix>> getTransformations();
	
	public void setTransformations(List<Function<Matrix, Matrix>> list);
	
	public void draw(Screen sc);
	
	public default void clearTransformations() {
		getTransformations().clear();
	}
	
	public Point getCenter();
	
	public Point getStart();

	public Point getFinish();
	
	public default void translate(double offsetX, double offsetY) {
		getTransformations().add(new Function<Matrix, Matrix>() {
			
			Matrix offset = new Matrix(3,3,new double[][]{{1,0,0},{0,1,0},{offsetX,offsetY,1}});

			@Override
			public Matrix apply(Matrix a) {
				return Matrix.multiplication(a, offset);
			}
			
		});
	}
	public default void translate(Point offset) {
		translate(offset.x, offset.y);
	}
	
	public default void scale(double sX, double sY) {
		getTransformations().add(new Function<Matrix, Matrix>() {
			
			Matrix size = new Matrix(3,3,new double[][]{{sX,0,0},{0,sY,0},{0,0,1}});

			@Override
			public Matrix apply(Matrix a) {
				return Matrix.multiplication(a, size);
			}
			
		});
	}
	public default void scale(Point size) {
		translate(size.x, size.y);
	}
	
	public default void rotate(double alpha) {
		final double rad = Shape.deg2rad(alpha);
		getTransformations().add(new Function<Matrix, Matrix>() {
			
			Matrix size = new Matrix(3,3,new double[][]{{Math.cos(rad),Math.sin(rad),0},{-Math.sin(rad),Math.cos(rad),0},{0,0,1}});

			@Override
			public Matrix apply(Matrix a) {
				return Matrix.multiplication(a, size);
			}
			
		});
	}
	
	public default void mirror(boolean x, boolean y) {
		getTransformations().add(new Function<Matrix, Matrix>() {
			
			Matrix mirrMat = new Matrix(3,3,new double[][]{{x?-1:1,0,0},{0,y?-1:1,0},{0,0,1}});

			@Override
			public Matrix apply(Matrix a) {
				if (!(x || y))
					return a;
				else
					return Matrix.multiplication(a, mirrMat);
			}
			
		});
	}
	
	public default void shear(double a, double b) {
		getTransformations().add(new Function<Matrix, Matrix>() {
			
			Matrix move = new Matrix(3,3,new double[][]{{1,b,0},{a,1,0},{0,0,1}});

			@Override
			public Matrix apply(Matrix a) {
				return Matrix.multiplication(a, move);
			}
			
		});
	}
	public default void shear(Point offset) {
		shear(offset.x, offset.y);
	}
}

class MyLine implements Shape {
	
	public List<Function<Matrix, Matrix>> transformations = new ArrayList<>();
	
	public Point p1 = new Point(), p2 = new Point();
	
	public MyLine() {}
	
	public MyLine(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public MyLine(Point... points) {
		this(points[0], points[1]);
	}

	@Override
	public Point getCenter() {
		return new Point(p1.x+(p2.x-p1.x)/2, p1.y+(p2.y-p1.y)/2);
	}

	@Override
	public List<Function<Matrix, Matrix>> getTransformations() {
		return transformations;
	}

	@Override
	public void setTransformations(List<Function<Matrix, Matrix>> list) {
		transformations = list;
	}

	@Override
	public void draw(Screen sc) {
		sc.drawLine(this);
	}

	@Override
	public Point getStart() {
		return p1;
	}

	@Override
	public Point getFinish() {
		return p2;
	}

	@Override
	public String toString() {
		return "MyLine [p1=" + p1 + ", p2=" + p2 + "]";
	}
	
}

class MyCircle implements Shape {
	
	public List<Function<Matrix, Matrix>> transformations = new ArrayList<>();
	
	public Point base = new Point();
	public double rad = 0;
	
	public MyCircle() {}
	
	public MyCircle(Point p1, double rad) {
		this.base = (Point)p1.clone();
		this.rad = rad;
	}
	
	public MyCircle(Point p1, Point p2) {
		this.base = (Point)p1.clone();
		this.rad = Point.distance(p1.x, p1.y, p2.x, p2.y);
	}
	
	public MyCircle(Point... points) {
		this(points[0], points[1]);
	}
	
	@Override
	public List<Function<Matrix, Matrix>> getTransformations() {
		return transformations;
	}
	
	@Override
	public void setTransformations(List<Function<Matrix, Matrix>> list) {
		transformations = list;
	}

	@Override
	public void draw(Screen sc) {
		//sc.drawCircleLines(this);
		sc.drawCirclePixels(this);
	}

	@Override
	public Point getCenter() {
		return base;
	}

	@Override
	public Point getStart() {
		return base;
	}

	@Override
	public Point getFinish() {
		return getStart();
	}

	public double getRad() {
		return rad;
	}

	@Override
	public String toString() {
		return "MyCircle [base=" + base + ", rad=" + rad + "]";
	}
	
}

class MyCurve implements Shape {
	
	public List<Function<Matrix, Matrix>> transformations = new ArrayList<>();
	
	public List<Point> points = new LinkedList<Point>();
	
	public MyCurve() {}
	
	public MyCurve(Point... points) {
		this.points = new LinkedList<Point>();
		for (Point point : points)
			this.points.add(point);
	}
	
	@Override
	public Point getCenter() {
		int maxX = points.get(0).x, maxY = points.get(0).y, minX = points.get(0).x, minY = points.get(0).y;
		for (Point point: points) {
			if (point.x > maxX)
				maxX = point.x;
			if (point.y > maxY)
				maxY = point.y;
			if (point.x < minX)
				minX = point.x;
			if (point.y < minY)
				minY = point.y;
		}
		
		Point center = new Point();
		center.x = minX+(maxX-minX)/2;
		center.y = minY+(maxY-minY)/2;
		return center;
	}

	@Override
	public Point getStart() {
		return points.get(0);
	}

	@Override
	public Point getFinish() {
		return points.get(3);
	}

	@Override
	public List<Function<Matrix, Matrix>> getTransformations() {
		return transformations;
	}
	
	@Override
	public void setTransformations(List<Function<Matrix, Matrix>> list) {
		transformations = list;
	}

	@Override
	public void draw(Screen sc) {
		sc.drawCurve(this);
	}
	
	public List<Point> getPoints() {
		return points;
	}
	
}

class MyRectangle implements Shape {
	
	public List<Function<Matrix, Matrix>> transformations = new ArrayList<>();
	
	public Point p1 = new Point(), p2 = new Point();
	
	public MyRectangle() {}
	
	public MyRectangle(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public MyRectangle(Point... points) {
		this(points[0], points[1]);
	}

	@Override
	public Point getStart() {
		return p1;
	}

	@Override
	public Point getFinish() {
		return p2;
	}

	@Override
	public List<Function<Matrix, Matrix>> getTransformations() {
		return transformations;
	}
	
	@Override
	public void setTransformations(List<Function<Matrix, Matrix>> list) {
		transformations = list;
	}

	@Override
	public void draw(Screen sc) {
		sc.drawRectangle(this);
	}

	@Override
	public Point getCenter() {
		return new Point(p1.x+(p2.x-p1.x)/2, p1.y+(p2.y-p1.y)/2);
	}
	
}

class CompositeShape implements Shape {
	
	public Point[] points = null;
	public List<Shape> shapes = new ArrayList<>();
	
	public List<Function<Matrix, Matrix>> transformations = new ArrayList<>();
	
	public Point center = new Point();
	
	public CompositeShape() {}
	
	public CompositeShape(Point center) {
		this.center = center;
	}

	@Override
	public List<Function<Matrix, Matrix>> getTransformations() {
		return transformations;
	}
	
	@Override
	public void setTransformations(List<Function<Matrix, Matrix>> list) {
		transformations = list;
	}

	@Override
	public void draw(Screen sc) {
		sc.drawComposite(this);
	}

	@Override
	public Point getCenter() {
		return center;
	}

	@Override
	public Point getStart() {
		return center;
	}

	@Override
	public Point getFinish() {
		return center;
	}
	
	/*
	 * is responsible for reading all the coordinates from file
	 */
	public void load() {
		//	TODO
	}
	
	public void prepare() {
		load();
		
		int i = 0;
		for (; i < 3; ++i) {
			shapes.add(new MyLine(points[i], points[i+1]));
		}
		
		shapes.add(new MyCurve(points[i], points[++i], points[++i], points[++i]));
		
		for (; i < 11; ++i) {
			shapes.add(new MyLine(points[i], points[i+1]));
		}
		
		shapes.add(new MyCurve(points[i], points[++i], points[++i], points[++i]));
		shapes.add(new MyLine(points[i], points[++i]));
		shapes.add(new MyCurve(points[i], points[++i], points[++i], points[++i]));
		shapes.add(new MyLine(points[i], points[++i]));
		shapes.add(new MyLine(points[i], points[0]));
		
		shapes.add(new MyCircle(points[++i], points[++i]));
		shapes.add(new MyCircle(points[++i], points[++i]));
	}
	
}