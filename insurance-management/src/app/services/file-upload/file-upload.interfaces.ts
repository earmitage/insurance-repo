export interface ColumnMapping {
  [fileColumn: string]: string;
}

export interface MappingTemplate {
  name: string;
  mapping: ColumnMapping;
  entityType: 'policy' | 'beneficiary';
  createdDate: Date;
}

export interface ValidationError {
  row: number;
  field: string;
  message: string;
  severity: 'error' | 'warning';
}

export interface UploadResult {
  successCount: number;
  errorCount: number;
  errors: ValidationError[];
  skippedRows: number[];
  message: string;
}

export interface FileParseResult {
  data: any[];
  headers: string[];
}