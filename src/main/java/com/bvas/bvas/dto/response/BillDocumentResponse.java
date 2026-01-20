package com.bvas.bvas.dto.response;

import com.bvas.bvas.model.enums.DocumentType;
import lombok.Data;

@Data
public class BillDocumentResponse {
    private Long id;
    private DocumentType documentType;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private String description;
}