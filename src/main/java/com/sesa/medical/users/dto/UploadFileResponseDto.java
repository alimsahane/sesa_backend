package com.sesa.medical.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UploadFileResponseDto {
	
	public UploadFileResponseDto(String fileName, String fileType, long size) {
		super();
		this.fileName = fileName;
		this.fileType = fileType;
		this.size = size;
	}
	private String fileName;
	private String fileDownloadUri;
	private String fileType;
	private long size;

}
