package com.example.api.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
	private final String hash;
	private final String idContainer;

	public BusinessException(String message, String hash, String idContainer){
		super(message);
		this.hash = hash;
		this.idContainer = idContainer;
	}

}
