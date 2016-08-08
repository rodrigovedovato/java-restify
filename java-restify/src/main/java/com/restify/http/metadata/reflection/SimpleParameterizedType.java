package com.restify.http.metadata.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class SimpleParameterizedType implements ParameterizedType {

	private final Type rawType;
	private final Type ownerType;
	private final Type[] typeArguments;

	public SimpleParameterizedType(Type rawType, Type ownerType, Type...typeArguments) {
		this.rawType = rawType;
		this.ownerType = ownerType;
		this.typeArguments = typeArguments;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return typeArguments;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rawType, ownerType, typeArguments);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParameterizedType) {
			ParameterizedType that = (ParameterizedType) obj;
			
			return Objects.equals(rawType, that.getRawType())
				&& Objects.equals(ownerType, that.getOwnerType())
				&& Arrays.equals(typeArguments, that.getActualTypeArguments());
			
		} else return false;
	}

}