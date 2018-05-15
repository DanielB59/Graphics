package com.extwo.src;

@FunctionalInterface
public interface TriFunction<S extends Shape,T,U,R> {
	
	R apply(S s, T t, U u);
}
