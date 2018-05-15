package com.extwo.src;

import java.io.Serializable;
import java.util.Arrays;

/*
 * Excercise 2 handin:
 * participants:
 * Daniel Ben Zvi 301770640
 * Keren Gold 305277287
 * Sagi Fridman 305010969
 * Sharon ? ?
 */
public class Matrix implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final int n;
	private final int m;
	
	public double[][] matrix = null;
	
	public Matrix(int n, int m, double[][] values) {
		this.n = n;
		this.m = m;
		
		matrix = new double[n][m];
		
		if (null != values)
			for (int i = 0; i < Math.min(n, values.length); ++i)
				for (int j = 0; j < Math.min(m, values[i].length); ++j)
					matrix[i][j] = values[i][j];
	}
	
	public Matrix(int n, int m, double value) {
		this.n = n;
		this.m = m;
		
		matrix = new double[n][m];
		
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < m; ++j)
				if (i == j) matrix[i][j] = value;
	}
	
	public Matrix(int n, int m) {
		this(n, m, null);
	}
	
	public Matrix(Matrix a) {
		this(a.n, a.m, a.matrix);
	}
	
	public static Matrix fill(Matrix a, double val) {
		for (int i = 0; i < a.n; ++i)
			for (int j = 0; j < a.m; ++j)
				a.matrix[i][j] = val;
		return a;
	}
	
	public static Matrix addition(Matrix a, double b) {
		return addition(a, fill(new Matrix(a.n, a.m), b));
	}
	
	public static Matrix addition(Matrix a, Matrix b) {
		assert(a.n == b.n && a.m == b.m);
		
		Matrix c = new Matrix(a.n, a.m);
		
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				c.matrix[i][j] = a.matrix[i][j] + b.matrix[i][j];
		
		return c;
	}
	
	public static Matrix negative(Matrix a) {
		Matrix c = new Matrix(a.n, a.m, a.matrix);
		
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				c.matrix[i][j] = -a.matrix[i][j];
		
		return c;
	}
	
	public static Matrix substraction(Matrix a, double b) {
		return substraction(a, new Matrix(a.n, a.m, b));
	}
	
	public static Matrix substraction(Matrix a, Matrix b) {
		return addition(a, negative(b));
	}
	
	public static Matrix multiplication(Matrix a, double b) {
		return multiplication(a, new Matrix(a.m, a.m, b));
	}
	
	public static Matrix multiplication(Matrix a, Matrix b) {
		assert(a.m == b.n);
		
		Matrix c = new Matrix(a.n, b.m);
		
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				for (int k = 0; k < a.m; ++k)
					c.matrix[i][j] += (a.matrix[i][k] * b.matrix[k][j]);
		return c;
	}
	
	public static Matrix division(Matrix a, double b) {
		return division(a, new Matrix(a.m, a.m, b));
	}
	
	public static Matrix division(Matrix a, Matrix b) {
		assert (a.m == b.n);
		
		Matrix c = new Matrix(b.n, b.m);
		
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				c.matrix[i][j] = 1. / b.matrix[i][j];
		
		return multiplication(a, c);
	}
	
	public static Matrix transpose(Matrix a) {
		Matrix c = new Matrix(a.m, a.n);
		
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				c.matrix[i][j] = a.matrix[j][i];
		
		return c;
	}
	
	public static Matrix matrixCut(Matrix a, int n, int m) {
		assert(a.n == a.m);
		double[][] values = new double[a.n-1][a.m-1];
		
		for (int k = 0, f = 0; k < a.n; ++k) {
			for (int l = 0, g = 0; l < a.m; ++l) {
				if (k != n && l != m) {
					values[f][g] = a.matrix[k][l];
					if (values.length-1 == g)
						f = (f+1)%values.length;
					g = (g+1)%values[0].length;
				}
			}
		}
		
		return new Matrix(a.n-1, a.m-1, values);
	}
	
	public static double minor(Matrix a, int n, int m) {
		return determinant(matrixCut(a, n, m));
	}
	
	public static double coFactor(Matrix a, int n, int m) {
		return Math.pow(-1, n+m) * minor(a, n, m);
	}
	
	public static Matrix coFactor(Matrix a) {
		Matrix c = new Matrix(a.n, a.m);
		for (int i = 0; i < c.n; ++i)
			for (int j = 0; j < c.m; ++j)
				c.matrix[i][j] = coFactor(a, i, j);
		return c;
	}
	
	public static double determinant(Matrix a) {
		assert(a.n == a.m);
		
		if (a.n == 2) {
			return a.matrix[0][0] * a.matrix[1][1] - a.matrix[0][1] * a.matrix[1][0];
		}
		
		double result = 0;
		for (int level = a.n; level > 2; --level)
			for (int j = 0; j < a.m; ++j)
				result = result + (((0+j)%2==0?1:-1) * a.matrix[0][j]) * determinant(matrixCut(a, 0, j));//new Matrix(level-1, level-1, values));
		
		return result;
	}
	
	public static Matrix adjugate(Matrix a) {
		return transpose(coFactor(a));
	}
	
	public static Matrix inverse(Matrix a) {	//	TODO
		assert(a.n == a.m);
		return multiplication(adjugate(a), 1./determinant(a));
	}
	
	public static Matrix unitMatrix(int n) {
		return new Matrix(n, n, 1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j)
				builder.append(matrix[i][j] + "\t");
			builder.append("\n");
		}
				
		
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m;
		result = prime * result + Arrays.deepHashCode(matrix);
		result = prime * result + n;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix other = (Matrix) obj;
		if (m != other.m)
			return false;
		if (!Arrays.deepEquals(matrix, other.matrix))
			return false;
		if (n != other.n)
			return false;
		return true;
	}
	
}
