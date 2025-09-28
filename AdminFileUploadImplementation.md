# Admin File Upload Implementation Plan
## Insurance Management System - Bulk Data Import Feature

### Executive Summary
This document outlines the detailed implementation plan for adding bulk file upload functionality to the insurance management system. The feature will allow **admin users only** to upload Excel/CSV files containing policy and beneficiary data, with advanced mapping, validation, and transformation capabilities.

### System Architecture Overview
- **Frontend**: Angular 19.2.3 with Angular Material UI
- **Backend**: Spring Boot (Java) with REST API
- **Authentication**: JWT-based with role-based access control
- **Current Admin Role**: `Role.Admin` (defined in `/src/app/interfaces/roles.ts:3`)

---

## 1. Security & Access Control

### Admin Authentication Requirements
- **Current Implementation**: Admin access controlled via `Role.Admin` enum
- **Admin Component**: `/src/app/components/admin/admin.component.ts` (existing)
- **Admin Service**: `/src/app/services/admin.service.ts` (existing)
- **Route Protection**: Admin routes must be protected with `AuthGuard`

### Security Implementation
```typescript
// New: AdminGuard service
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private globals: GlobalProvider) {}

  canActivate(): boolean {
    const user = this.globals.currentUserValue;
    return user?.roles?.some(role => role.authority === 'Admin') || false;
  }
}
```

### Route Configuration Update
```typescript
// app.routes.ts modification
{
  path: 'admin/file-upload',
  component: FileUploadComponent,
  canActivate: [AuthGuard, AdminGuard]
}
```

---

## 2. Dependencies & Installation

### Required NPM Packages
```bash
npm install xlsx papaparse @types/papaparse
```

### Package Justification
- **xlsx**: Excel file parsing (.xlsx, .xls)
- **papaparse**: Robust CSV parsing with error handling
- **@types/papaparse**: TypeScript definitions

---

## 3. Data Models & DTOs

### Backend DTOs (Existing)
```java
// AddPolicy.java - Already defined
class AddPolicy {
  String policyNumber, insuranceCompany, policyType;
  Double coverageAmount, monthlyPremium;
  String status, addressLine1, city, region, postalCode, country;
  String uuid;
  Boolean deceased;
  LocalDate deceasedDate;
  List<AddBeneficiary> beneficiaries;
}

// AddBeneficiary.java - Already defined
class AddBeneficiary {
  String uuid, fullName, idNumber, relationship;
  LocalDate dateOfBirth;
  Double sharePercentage;
  String idType, countryCode, phone, email;
  Boolean loginAllowed, deceased;
  LocalDate deceasedDate;
}
```

### Frontend Interfaces
```typescript
// New: file-upload.interfaces.ts
export interface ColumnMapping {
  [fileColumn: string]: string; // Maps file column to DTO field
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
}
```

---

## 4. Component Architecture

### File Upload Component Structure
```
/src/app/components/admin/file-upload/
├── file-upload.component.ts
├── file-upload.component.html
├── file-upload.component.scss
├── services/
│   ├── file-parser.service.ts
│   ├── column-mapper.service.ts
│   └── upload-validator.service.ts
└── interfaces/
    └── file-upload.interfaces.ts
```

---

## 5. Implementation Steps

### Step 1: Core File Upload Component
```typescript
@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {
  // Core properties
  selectedFile: File | null = null;
  rawData: any[] = [];
  headers: string[] = [];
  columnMapping: ColumnMapping = {};
  entityType: 'policy' | 'beneficiary' = 'policy';

  // Mapping templates
  savedTemplates: MappingTemplate[] = [];

  // Validation & preview
  validationErrors: ValidationError[] = [];
  transformedData: any[] = [];
  previewData: any[] = [];

  // UI state
  currentStep: 'upload' | 'mapping' | 'validation' | 'preview' = 'upload';
  isProcessing = false;

  constructor(
    private fileParser: FileParserService,
    private columnMapper: ColumnMapperService,
    private validator: UploadValidatorService,
    private adminService: AdminService,
    private global: GlobalProvider
  ) {}
}
```

### Step 2: File Parser Service
```typescript
@Injectable({ providedIn: 'root' })
export class FileParserService {

  parseFile(file: File): Promise<{data: any[], headers: string[]}> {
    const extension = file.name.split('.').pop()?.toLowerCase();

    if (extension === 'csv') {
      return this.parseCSV(file);
    } else if (['xlsx', 'xls'].includes(extension || '')) {
      return this.parseExcel(file);
    } else {
      throw new Error('Unsupported file format');
    }
  }

  private parseCSV(file: File): Promise<{data: any[], headers: string[]}> {
    return new Promise((resolve, reject) => {
      Papa.parse(file, {
        header: true,
        skipEmptyLines: true,
        transformHeader: (header: string) => header.trim(),
        complete: (result) => {
          if (result.errors.length > 0) {
            reject(new Error(`CSV parsing error: ${result.errors[0].message}`));
            return;
          }

          const headers = result.meta.fields || [];
          resolve({ data: result.data, headers });
        },
        error: (error) => reject(error)
      });
    });
  }

  private parseExcel(file: File): Promise<{data: any[], headers: string[]}> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        try {
          const workbook = XLSX.read(e.target.result, { type: 'binary' });
          const worksheet = workbook.Sheets[workbook.SheetNames[0]];
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { defval: '' });

          if (jsonData.length === 0) {
            reject(new Error('Excel file is empty'));
            return;
          }

          const headers = Object.keys(jsonData[0]);
          resolve({ data: jsonData, headers });
        } catch (error) {
          reject(new Error(`Excel parsing error: ${error}`));
        }
      };

      reader.onerror = () => reject(new Error('File reading failed'));
      reader.readAsBinaryString(file);
    });
  }
}
```

### Step 3: Advanced Column Mapper Service
```typescript
@Injectable({ providedIn: 'root' })
export class ColumnMapperService {

  // Available DTO fields based on backend models
  private readonly policyFields = [
    'policyNumber', 'insuranceCompany', 'policyType', 'coverageAmount',
    'monthlyPremium', 'status', 'addressLine1', 'city', 'region',
    'postalCode', 'country', 'deceased', 'deceasedDate'
  ];

  private readonly beneficiaryFields = [
    'fullName', 'idNumber', 'relationship', 'dateOfBirth', 'sharePercentage',
    'idType', 'countryCode', 'phone', 'email', 'loginAllowed', 'deceased', 'deceasedDate'
  ];

  getAvailableFields(entityType: 'policy' | 'beneficiary'): string[] {
    return entityType === 'policy' ? this.policyFields : this.beneficiaryFields;
  }

  // Auto-mapping with enhanced algorithms
  autoMapColumns(headers: string[], entityType: 'policy' | 'beneficiary'): ColumnMapping {
    const mapping: ColumnMapping = {};
    const availableFields = this.getAvailableFields(entityType);

    headers.forEach(header => {
      const suggestion = this.findBestMatch(header, availableFields);
      if (suggestion) {
        mapping[header] = suggestion;
      }
    });

    return mapping;
  }

  private findBestMatch(header: string, fields: string[]): string | null {
    const cleanHeader = this.normalizeString(header);
    let bestMatch = null;
    let highestScore = 0;

    // Enhanced matching algorithms
    for (const field of fields) {
      const cleanField = this.normalizeString(field);

      // Exact match (highest priority)
      if (cleanHeader === cleanField) {
        return field;
      }

      // Levenshtein distance
      const levenshteinScore = this.levenshteinSimilarity(cleanHeader, cleanField);

      // Substring matching
      const substringScore = this.substringSimilarity(cleanHeader, cleanField);

      // Keyword matching
      const keywordScore = this.keywordSimilarity(cleanHeader, cleanField);

      // Weighted combined score
      const combinedScore = (levenshteinScore * 0.4) + (substringScore * 0.3) + (keywordScore * 0.3);

      if (combinedScore > highestScore && combinedScore > 0.6) {
        highestScore = combinedScore;
        bestMatch = field;
      }
    }

    return bestMatch;
  }

  private normalizeString(str: string): string {
    return str.toLowerCase()
              .replace(/[^a-z0-9]/g, '')
              .replace(/\s+/g, '');
  }

  private levenshteinSimilarity(a: string, b: string): number {
    const distance = this.levenshteinDistance(a, b);
    const maxLength = Math.max(a.length, b.length);
    return maxLength === 0 ? 1 : 1 - (distance / maxLength);
  }

  private levenshteinDistance(a: string, b: string): number {
    const matrix = Array(b.length + 1).fill(null).map(() => Array(a.length + 1).fill(null));

    for (let i = 0; i <= a.length; i += 1) matrix[0][i] = i;
    for (let j = 0; j <= b.length; j += 1) matrix[j][0] = j;

    for (let j = 1; j <= b.length; j += 1) {
      for (let i = 1; i <= a.length; i += 1) {
        const indicator = a[i - 1] === b[j - 1] ? 0 : 1;
        matrix[j][i] = Math.min(
          matrix[j][i - 1] + 1,
          matrix[j - 1][i] + 1,
          matrix[j - 1][i - 1] + indicator
        );
      }
    }

    return matrix[b.length][a.length];
  }

  private substringSimilarity(a: string, b: string): number {
    if (a.includes(b) || b.includes(a)) return 0.8;
    return 0;
  }

  private keywordSimilarity(header: string, field: string): number {
    const headerKeywords = this.extractKeywords(header);
    const fieldKeywords = this.extractKeywords(field);

    const commonKeywords = headerKeywords.filter(kw => fieldKeywords.includes(kw));
    const totalKeywords = new Set([...headerKeywords, ...fieldKeywords]).size;

    return totalKeywords > 0 ? commonKeywords.length / totalKeywords : 0;
  }

  private extractKeywords(str: string): string[] {
    return str.toLowerCase()
              .replace(/[^a-z\s]/g, '')
              .split(/\s+/)
              .filter(word => word.length > 2);
  }
}
```

### Step 4: Comprehensive Validation Service
```typescript
@Injectable({ providedIn: 'root' })
export class UploadValidatorService {

  validateData(data: any[], entityType: 'policy' | 'beneficiary'): ValidationError[] {
    const errors: ValidationError[] = [];

    data.forEach((row, index) => {
      if (entityType === 'policy') {
        errors.push(...this.validatePolicyRow(row, index + 1));
      } else {
        errors.push(...this.validateBeneficiaryRow(row, index + 1));
      }
    });

    return errors;
  }

  private validatePolicyRow(row: any, rowNumber: number): ValidationError[] {
    const errors: ValidationError[] = [];

    // Required field validations
    if (!row.policyNumber?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyNumber',
        message: 'Policy number is required',
        severity: 'error'
      });
    }

    if (!row.insuranceCompany?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'insuranceCompany',
        message: 'Insurance company is required',
        severity: 'error'
      });
    }

    if (!row.policyType?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyType',
        message: 'Policy type is required',
        severity: 'error'
      });
    }

    // Numeric validations
    if (row.coverageAmount && (isNaN(row.coverageAmount) || row.coverageAmount <= 0)) {
      errors.push({
        row: rowNumber,
        field: 'coverageAmount',
        message: 'Coverage amount must be a positive number',
        severity: 'error'
      });
    }

    if (row.monthlyPremium && (isNaN(row.monthlyPremium) || row.monthlyPremium <= 0)) {
      errors.push({
        row: rowNumber,
        field: 'monthlyPremium',
        message: 'Monthly premium must be a positive number',
        severity: 'error'
      });
    }

    // Date validations
    if (row.deceasedDate && !this.isValidDate(row.deceasedDate)) {
      errors.push({
        row: rowNumber,
        field: 'deceasedDate',
        message: 'Invalid deceased date format',
        severity: 'error'
      });
    }

    // Boolean validations
    if (row.deceased !== undefined && !this.isValidBoolean(row.deceased)) {
      errors.push({
        row: rowNumber,
        field: 'deceased',
        message: 'Deceased field must be true/false',
        severity: 'error'
      });
    }

    return errors;
  }

  private validateBeneficiaryRow(row: any, rowNumber: number): ValidationError[] {
    const errors: ValidationError[] = [];

    // Required field validations
    if (!row.fullName?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'fullName',
        message: 'Full name is required',
        severity: 'error'
      });
    }

    if (!row.idNumber?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'idNumber',
        message: 'ID number is required',
        severity: 'error'
      });
    }

    // ID number format validation
    if (row.idType === 'SAID' && row.idNumber && !/^\d{13}$/.test(row.idNumber)) {
      errors.push({
        row: rowNumber,
        field: 'idNumber',
        message: 'South African ID must be 13 digits',
        severity: 'error'
      });
    }

    // Share percentage validation
    if (row.sharePercentage) {
      const percentage = parseFloat(row.sharePercentage);
      if (isNaN(percentage) || percentage <= 0 || percentage > 100) {
        errors.push({
          row: rowNumber,
          field: 'sharePercentage',
          message: 'Share percentage must be between 0 and 100',
          severity: 'error'
        });
      }
    }

    // Email validation
    if (row.email && !this.isValidEmail(row.email)) {
      errors.push({
        row: rowNumber,
        field: 'email',
        message: 'Invalid email format',
        severity: 'error'
      });
    }

    // Phone or email requirement
    if (!row.phone?.trim() && !row.email?.trim()) {
      errors.push({
        row: rowNumber,
        field: 'contact',
        message: 'Either phone or email must be provided',
        severity: 'error'
      });
    }

    // Date validations
    if (row.dateOfBirth && !this.isValidDate(row.dateOfBirth)) {
      errors.push({
        row: rowNumber,
        field: 'dateOfBirth',
        message: 'Invalid date of birth format',
        severity: 'error'
      });
    }

    return errors;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private isValidDate(dateStr: string): boolean {
    const date = new Date(dateStr);
    return !isNaN(date.getTime());
  }

  private isValidBoolean(value: any): boolean {
    return typeof value === 'boolean' ||
           ['true', 'false', '1', '0', 'yes', 'no'].includes(String(value).toLowerCase());
  }
}
```

### Step 5: Data Transformation
```typescript
// In FileUploadComponent
transformData(): any[] {
  return this.rawData.map(row => {
    const transformed: any = {};

    Object.entries(this.columnMapping).forEach(([fileCol, systemField]) => {
      if (systemField && row[fileCol] !== undefined) {
        transformed[systemField] = this.parseFieldValue(systemField, row[fileCol]);
      }
    });

    return transformed;
  });
}

private parseFieldValue(field: string, value: any): any {
  // Numeric fields
  if (['coverageAmount', 'monthlyPremium', 'sharePercentage'].includes(field)) {
    return value ? parseFloat(value) : null;
  }

  // Boolean fields
  if (['deceased', 'loginAllowed'].includes(field)) {
    if (typeof value === 'boolean') return value;
    const strValue = String(value).toLowerCase();
    return ['true', '1', 'yes', 'y'].includes(strValue);
  }

  // Date fields
  if (field.includes('Date') || field === 'dateOfBirth') {
    return value ? new Date(value).toISOString().split('T')[0] : null;
  }

  // String fields (trim whitespace)
  return value ? String(value).trim() : null;
}
```

---

## 6. Template Management System

### Template Storage Service
```typescript
@Injectable({ providedIn: 'root' })
export class TemplateStorageService {
  private readonly STORAGE_KEY = 'file-upload-templates';

  saveTemplate(template: MappingTemplate): void {
    const templates = this.getTemplates();
    const existingIndex = templates.findIndex(t => t.name === template.name);

    if (existingIndex >= 0) {
      templates[existingIndex] = template;
    } else {
      templates.push(template);
    }

    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(templates));
  }

  getTemplates(): MappingTemplate[] {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  }

  deleteTemplate(name: string): void {
    const templates = this.getTemplates().filter(t => t.name !== name);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(templates));
  }

  loadTemplate(name: string): MappingTemplate | null {
    const templates = this.getTemplates();
    return templates.find(t => t.name === name) || null;
  }
}
```

---

## 7. User Interface Design

### Main Upload Component Template
```html
<!-- file-upload.component.html -->
<div class="file-upload-container">
  <mat-card>
    <mat-card-header>
      <mat-card-title>
        <mat-icon>cloud_upload</mat-icon>
        Bulk Data Import
      </mat-card-title>
    </mat-card-header>

    <mat-card-content>
      <!-- Step Indicator -->
      <mat-horizontal-stepper [selectedIndex]="currentStepIndex" [linear]="true">

        <!-- Step 1: File Upload -->
        <mat-step [completed]="currentStepIndex > 0">
          <ng-template matStepLabel>Upload File</ng-template>
          <div class="upload-section">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Select Entity Type</mat-label>
              <mat-select [(value)]="entityType">
                <mat-option value="policy">Policies</mat-option>
                <mat-option value="beneficiary">Beneficiaries</mat-option>
              </mat-select>
            </mat-form-field>

            <div class="file-drop-zone"
                 [class.drag-over]="isDragOver"
                 (dragover)="onDragOver($event)"
                 (dragleave)="onDragLeave($event)"
                 (drop)="onDrop($event)">
              <mat-icon class="upload-icon">cloud_upload</mat-icon>
              <p>Drag and drop your file here or click to select</p>
              <input type="file"
                     #fileInput
                     (change)="onFileSelected($event)"
                     accept=".xlsx,.xls,.csv"
                     hidden>
              <button mat-raised-button color="primary" (click)="fileInput.click()">
                Select File
              </button>
            </div>

            <div *ngIf="selectedFile" class="file-info">
              <mat-icon>description</mat-icon>
              <span>{{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})</span>
              <button mat-icon-button (click)="removeFile()">
                <mat-icon>close</mat-icon>
              </button>
            </div>
          </div>
        </mat-step>

        <!-- Step 2: Column Mapping -->
        <mat-step [completed]="currentStepIndex > 1">
          <ng-template matStepLabel>Map Columns</ng-template>
          <div class="mapping-section">

            <!-- Template Management -->
            <div class="template-controls">
              <mat-form-field appearance="outline">
                <mat-label>Load Template</mat-label>
                <mat-select [(value)]="selectedTemplate" (selectionChange)="loadTemplate($event.value)">
                  <mat-option value="">-- Select Template --</mat-option>
                  <mat-option *ngFor="let template of savedTemplates" [value]="template">
                    {{ template.name }}
                  </mat-option>
                </mat-select>
              </mat-form-field>

              <button mat-raised-button color="accent" (click)="autoMapColumns()">
                <mat-icon>auto_fix_high</mat-icon>
                Auto Map
              </button>

              <button mat-raised-button (click)="openSaveTemplateDialog()">
                <mat-icon>save</mat-icon>
                Save Template
              </button>
            </div>

            <!-- Column Mapping Table -->
            <table mat-table [dataSource]="headerMappingData" class="mapping-table">
              <!-- File Column -->
              <ng-container matColumnDef="fileColumn">
                <th mat-header-cell *matHeaderCellDef>File Column</th>
                <td mat-cell *matCellDef="let element">
                  <strong>{{ element.header }}</strong>
                  <br>
                  <small class="sample-data">Sample: {{ element.sampleData }}</small>
                </td>
              </ng-container>

              <!-- System Field -->
              <ng-container matColumnDef="systemField">
                <th mat-header-cell *matHeaderCellDef>Maps To</th>
                <td mat-cell *matCellDef="let element">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-select [(value)]="columnMapping[element.header]"
                               [placeholder]="'Select field...'">
                      <mat-option value="">-- No Mapping --</mat-option>
                      <mat-option *ngFor="let field of getAvailableFields()"
                                 [value]="field">
                        {{ formatFieldName(field) }}
                      </mat-option>
                    </mat-select>
                  </mat-form-field>
                </td>
              </ng-container>

              <!-- Confidence -->
              <ng-container matColumnDef="confidence">
                <th mat-header-cell *matHeaderCellDef>Confidence</th>
                <td mat-cell *matCellDef="let element">
                  <div *ngIf="getConfidenceScore(element.header) > 0"
                       class="confidence-indicator">
                    <mat-progress-bar mode="determinate"
                                     [value]="getConfidenceScore(element.header) * 100"
                                     [color]="getConfidenceColor(element.header)">
                    </mat-progress-bar>
                    <small>{{ (getConfidenceScore(element.header) * 100) | number:'1.0-0' }}%</small>
                  </div>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="['fileColumn', 'systemField', 'confidence']"></tr>
              <tr mat-row *matRowDef="let row; columns: ['fileColumn', 'systemField', 'confidence']"></tr>
            </table>
          </div>
        </mat-step>

        <!-- Step 3: Validation -->
        <mat-step [completed]="currentStepIndex > 2">
          <ng-template matStepLabel>Validate Data</ng-template>
          <div class="validation-section">

            <!-- Validation Summary -->
            <div class="validation-summary">
              <mat-card *ngIf="validationErrors.length > 0" class="error-summary">
                <mat-card-content>
                  <h3>
                    <mat-icon color="warn">error</mat-icon>
                    Validation Issues Found
                  </h3>
                  <p>{{ getErrorCount() }} errors, {{ getWarningCount() }} warnings</p>
                </mat-card-content>
              </mat-card>

              <mat-card *ngIf="validationErrors.length === 0" class="success-summary">
                <mat-card-content>
                  <h3>
                    <mat-icon color="primary">check_circle</mat-icon>
                    Data Validation Passed
                  </h3>
                  <p>All {{ rawData.length }} rows are valid</p>
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Error List -->
            <div *ngIf="validationErrors.length > 0" class="error-list">
              <mat-accordion>
                <mat-expansion-panel *ngFor="let error of validationErrors; let i = index">
                  <mat-expansion-panel-header>
                    <mat-panel-title>
                      <mat-icon [color]="error.severity === 'error' ? 'warn' : 'accent'">
                        {{ error.severity === 'error' ? 'error' : 'warning' }}
                      </mat-icon>
                      Row {{ error.row }}: {{ error.field }}
                    </mat-panel-title>
                    <mat-panel-description>
                      {{ error.message }}
                    </mat-panel-description>
                  </mat-expansion-panel-header>

                  <!-- Show row data context -->
                  <div class="error-context">
                    <pre>{{ getRowContext(error.row) | json }}</pre>
                  </div>
                </mat-expansion-panel>
              </mat-accordion>
            </div>
          </div>
        </mat-step>

        <!-- Step 4: Preview & Upload -->
        <mat-step>
          <ng-template matStepLabel>Preview & Upload</ng-template>
          <div class="preview-section">

            <!-- Upload Summary -->
            <div class="upload-summary">
              <mat-card>
                <mat-card-content>
                  <h3>Upload Summary</h3>
                  <ul>
                    <li>Entity Type: <strong>{{ entityType | titlecase }}</strong></li>
                    <li>Total Rows: <strong>{{ rawData.length }}</strong></li>
                    <li>Valid Rows: <strong>{{ getValidRowCount() }}</strong></li>
                    <li>Errors: <strong>{{ getErrorCount() }}</strong></li>
                  </ul>
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Data Preview -->
            <div class="data-preview">
              <h4>Data Preview (First 10 rows)</h4>
              <table mat-table [dataSource]="previewData" class="preview-table">
                <ng-container *ngFor="let field of getMappedFields()" [matColumnDef]="field">
                  <th mat-header-cell *matHeaderCellDef>{{ formatFieldName(field) }}</th>
                  <td mat-cell *matCellDef="let element">{{ element[field] }}</td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="getMappedFields()"></tr>
                <tr mat-row *matRowDef="let row; columns: getMappedFields()"></tr>
              </table>
            </div>

            <!-- Upload Actions -->
            <div class="upload-actions">
              <button mat-raised-button
                     color="primary"
                     [disabled]="getErrorCount() > 0 || isUploading"
                     (click)="uploadData()">
                <mat-icon>cloud_upload</mat-icon>
                Upload {{ getValidRowCount() }} Records
              </button>

              <button mat-raised-button
                     *ngIf="getErrorCount() > 0"
                     (click)="downloadErrorReport()">
                <mat-icon>download</mat-icon>
                Download Error Report
              </button>
            </div>
          </div>
        </mat-step>

      </mat-horizontal-stepper>
    </mat-card-content>
  </mat-card>

  <!-- Loading Overlay -->
  <div *ngIf="isProcessing" class="loading-overlay">
    <mat-spinner></mat-spinner>
    <p>Processing file...</p>
  </div>
</div>
```

---

## 8. Backend API Endpoints

### Required API Extensions
```java
// AdminController.java - New endpoints
@RestController
@RequestMapping("/admin")
public class AdminController {

    @PostMapping("/upload/policies")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<UploadResult> uploadPolicies(
        @RequestBody List<AddPolicy> policies) {
        // Bulk insert policies with validation
        return ResponseEntity.ok(policyService.bulkInsert(policies));
    }

    @PostMapping("/upload/beneficiaries")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<UploadResult> uploadBeneficiaries(
        @RequestBody List<AddBeneficiary> beneficiaries) {
        // Bulk insert beneficiaries with validation
        return ResponseEntity.ok(beneficiaryService.bulkInsert(beneficiaries));
    }

    @PostMapping("/upload/validate")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ValidationResult> validateData(
        @RequestBody ValidationRequest request) {
        // Server-side validation without persisting
        return ResponseEntity.ok(validationService.validate(request));
    }
}

// UploadResult.java - Response DTO
public class UploadResult {
    private int successCount;
    private int errorCount;
    private List<ValidationError> errors;
    private List<String> warnings;
}
```

---

## 9. Testing Strategy

### Unit Tests
```typescript
// file-upload.component.spec.ts
describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;
  let mockFileParser: jasmine.SpyObj<FileParserService>;

  beforeEach(() => {
    const fileParserSpy = jasmine.createSpyObj('FileParserService', ['parseFile']);

    TestBed.configureTestingModule({
      declarations: [FileUploadComponent],
      providers: [
        { provide: FileParserService, useValue: fileParserSpy }
      ]
    });

    mockFileParser = TestBed.inject(FileParserService) as jasmine.SpyObj<FileParserService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse CSV file correctly', async () => {
    const mockFile = new File(['name,email\nJohn,john@test.com'], 'test.csv');
    const expectedResult = {
      data: [{ name: 'John', email: 'john@test.com' }],
      headers: ['name', 'email']
    };

    mockFileParser.parseFile.and.returnValue(Promise.resolve(expectedResult));

    await component.onFileSelected({ target: { files: [mockFile] } });

    expect(component.rawData).toEqual(expectedResult.data);
    expect(component.headers).toEqual(expectedResult.headers);
  });
});
```

### Integration Tests
```typescript
// Admin access test
it('should restrict file upload to admin users only', () => {
  // Test that non-admin users cannot access upload route
  // Test that admin guard correctly validates admin role
});

// End-to-end upload test
it('should successfully upload valid policy data', () => {
  // Test complete upload flow from file selection to successful upload
});
```

---

## 10. Error Handling & User Experience

### Error Scenarios
1. **File Format Errors**: Unsupported file types, corrupted files
2. **Parsing Errors**: Invalid CSV/Excel structure, encoding issues
3. **Validation Errors**: Missing required fields, invalid data types
4. **Server Errors**: Network issues, backend validation failures
5. **Permission Errors**: Non-admin access attempts

### Error Recovery
```typescript
// Comprehensive error handling
private handleUploadError(error: any): void {
  let userMessage = 'Upload failed. Please try again.';

  if (error.status === 403) {
    userMessage = 'Access denied. Admin privileges required.';
  } else if (error.status === 413) {
    userMessage = 'File too large. Maximum size is 10MB.';
  } else if (error.error?.message) {
    userMessage = error.error.message;
  }

  this.global.showError(userMessage, 'Upload Error');
  this.isUploading = false;
}
```

---

## 11. Performance Considerations

### File Size Limits
- **Maximum file size**: 10MB
- **Maximum rows**: 10,000 records per upload
- **Chunked processing**: For large files, process in batches of 1,000 records

### Memory Management
```typescript
// Efficient large file handling
private processLargeFile(data: any[]): void {
  const chunkSize = 1000;
  const chunks = this.chunkArray(data, chunkSize);

  chunks.forEach((chunk, index) => {
    setTimeout(() => {
      this.processChunk(chunk);
    }, index * 100); // Avoid blocking UI
  });
}
```

### Caching Strategy
- **Template caching**: Store mapping templates in localStorage
- **Field suggestions**: Cache auto-mapping results
- **Validation caching**: Cache validation results per row hash

---

## 12. Security Considerations

### File Upload Security
```typescript
// File validation
private validateFile(file: File): boolean {
  // Check file extension
  const allowedExtensions = ['.csv', '.xlsx', '.xls'];
  const extension = file.name.toLowerCase().substr(file.name.lastIndexOf('.'));

  if (!allowedExtensions.includes(extension)) {
    throw new Error('File type not allowed');
  }

  // Check file size
  if (file.size > 10 * 1024 * 1024) { // 10MB
    throw new Error('File too large');
  }

  // Check MIME type
  const allowedMimeTypes = [
    'text/csv',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  ];

  if (!allowedMimeTypes.includes(file.type)) {
    throw new Error('Invalid file type');
  }

  return true;
}
```

### Data Sanitization
```typescript
// Input sanitization
private sanitizeInput(value: any): any {
  if (typeof value === 'string') {
    return value.trim()
                .replace(/[<>]/g, '') // Remove potential HTML
                .substr(0, 1000); // Limit length
  }
  return value;
}
```

---

## 13. Deployment Checklist

### Frontend Deployment
- [ ] Install required npm packages: `xlsx`, `papaparse`, `@types/papaparse`
- [ ] Add file upload component to admin module
- [ ] Update routing with admin guard protection
- [ ] Configure file size limits in nginx/Apache
- [ ] Test file upload functionality

### Backend Deployment
- [ ] Implement bulk upload endpoints
- [ ] Add admin role validation
- [ ] Configure file upload limits
- [ ] Implement server-side validation
- [ ] Add comprehensive error handling
- [ ] Performance testing with large files

### Security Deployment
- [ ] Verify admin-only access enforcement
- [ ] Test file type restrictions
- [ ] Validate input sanitization
- [ ] Configure CSP headers for file uploads
- [ ] Test error handling for edge cases

---

## 14. Future Enhancements

### Phase 2 Features
1. **Batch Processing**: Background job processing for very large files
2. **Import History**: Track and audit all import operations
3. **Data Diff**: Show changes before applying updates
4. **Advanced Mapping**: Support for computed fields and transformations
5. **Export Templates**: Generate sample CSV/Excel templates
6. **Multi-sheet Support**: Handle Excel files with multiple sheets
7. **Progress Tracking**: Real-time upload progress with cancellation

### Integration Opportunities
1. **Email Notifications**: Notify admins of upload results
2. **Reporting Dashboard**: Analytics on import success rates
3. **API Integration**: Connect with external insurance systems
4. **Webhook Support**: Trigger external systems after import

---

## Conclusion

This implementation plan provides a comprehensive, secure, and user-friendly bulk file upload system specifically designed for the insurance management application. The solution emphasizes:

- **Admin-only access** with proper role-based security
- **Intelligent column mapping** with auto-suggestions and templates
- **Comprehensive validation** at both client and server levels
- **Excellent user experience** with step-by-step guidance
- **Robust error handling** and recovery mechanisms
- **Performance optimization** for large file processing

The modular architecture ensures maintainability and allows for future enhancements while maintaining security and performance standards expected in enterprise insurance applications.