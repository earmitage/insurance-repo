import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { first } from 'rxjs/operators';

import { NavigationComponent } from '../../../navigation/navigation.component';
import { GlobalProvider } from '../../../services/globals';
import { AdminService } from '../../../services/admin.service';
import { CompanyService } from '../../../services/company.service';
import { FileParserService } from '../../../services/file-upload/file-parser.service';
import { ColumnMapperService } from '../../../services/file-upload/column-mapper.service';
import { UploadValidatorService } from '../../../services/file-upload/upload-validator.service';
import { TemplateStorageService } from '../../../services/file-upload/template-storage.service';
import {
  ColumnMapping,
  ValidationError,
  MappingTemplate,
  UploadResult
} from '../../../services/file-upload/file-upload.interfaces';
import { Company } from '../../../interfaces/company';

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NavigationComponent,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatIconModule,
    MatProgressBarModule,
    MatTabsModule,
    MatChipsModule,
    MatExpansionModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.scss'
})
export class FileUploadComponent implements OnInit {
  // Core properties
  selectedFile: File | null = null;
  rawData: any[] = [];
  headers: string[] = [];
  columnMapping: ColumnMapping = {};
  entityType: 'policy' | 'beneficiary' | 'combined' = 'policy';

  // Company selection for policies
  companies: Company[] = [];
  selectedCompanyId: string = '';

  // Template management
  savedTemplates: MappingTemplate[] = [];
  selectedTemplate: string = '';
  newTemplateName: string = '';

  // Validation & preview
  validationErrors: ValidationError[] = [];
  transformedData: any[] = [];
  previewData: any[] = [];

  // UI state
  currentStep: number = 0;
  isProcessing: boolean = false;
  isDragOver: boolean = false;

  // Table data
  headerMappingData: any[] = [];
  mappingColumns: string[] = ['fileColumn', 'systemField', 'sample'];

  constructor(
    private router: Router,
    public global: GlobalProvider,
    private adminService: AdminService,
    private companyService: CompanyService,
    private fileParser: FileParserService,
    private columnMapper: ColumnMapperService,
    private validator: UploadValidatorService,
    private templateStorage: TemplateStorageService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.companyService.getCompanies()
      .pipe(first())
      .subscribe({
        next: (companies) => {
          this.companies = companies;
        },
        error: (error) => {
          this.global.showError('Failed to load companies', 'Error');
        }
      });
  }

  // File handling methods
  onFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file) {
      this.processSelectedFile(file);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.processSelectedFile(files[0]);
    }
  }

  private processSelectedFile(file: File): void {
    try {
      this.fileParser.validateFileBeforeParsing(file);
      this.selectedFile = file;
      this.parseFile();
    } catch (error: any) {
      this.global.showError(error.message, 'File Error');
    }
  }

  private async parseFile(): Promise<void> {
    if (!this.selectedFile) return;

    this.isProcessing = true;
    try {
      const result = await this.fileParser.parseFile(this.selectedFile);
      this.rawData = result.data;
      this.headers = result.headers;
      
      // Auto-detect the entity type based on headers
      const detectedType = this.columnMapper.detectEntityType(this.headers);
      if (detectedType !== this.entityType) {
        this.entityType = detectedType;
        this.global.showSuccess(
          'Entity type auto-detected', 
          `Detected as '${detectedType}' format based on column headers`
        );
      }
      
      this.prepareHeaderMappingData();
      this.autoMapColumns();
      this.currentStep = 1;
      this.global.showSuccess('File parsed successfully', `Found ${this.rawData.length} rows`);
    } catch (error: any) {
      this.global.showError(error.message, 'Parsing Error');
    } finally {
      this.isProcessing = false;
    }
  }

  prepareHeaderMappingData(): void {
    this.headerMappingData = this.headers.map(header => ({
      header,
      sampleData: this.getSampleData(header)
    }));
  }

  getSampleData(header: string): string {
    const sampleValues = this.rawData
      .slice(0, 3)
      .map(row => row[header])
      .filter(value => value !== null && value !== undefined && value !== '')
      .slice(0, 2);

    return sampleValues.length > 0 ? sampleValues.join(', ') : 'No data';
  }

  autoMapColumns(): void {
    this.columnMapping = this.columnMapper.autoMapColumns(this.headers, this.entityType);
  }

  getAvailableFields(): string[] {
    return this.columnMapper.getAvailableFields(this.entityType);
  }

  formatFieldName(field: string): string {
    return this.columnMapper.formatFieldName(field);
  }

  // Template management
  loadTemplates(): void {
    this.savedTemplates = this.templateStorage.getTemplates(this.entityType);
  }

  onEntityTypeChange(): void {
    this.columnMapping = {};
    this.validationErrors = [];
    this.transformedData = [];
    this.selectedCompanyId = ''; // Reset company selection
    this.loadTemplates();
    if (this.headers.length > 0) {
      this.autoMapColumns();
    }
  }

  loadTemplate(): void {
    if (!this.selectedTemplate) return;

    const template = this.templateStorage.loadTemplate(this.selectedTemplate, this.entityType);
    if (template) {
      this.columnMapping = { ...template.mapping };
      this.global.showSuccess('Template loaded', `Applied mapping: ${template.name}`);
    }
  }

  saveTemplate(): void {
    if (!this.newTemplateName.trim()) {
      this.global.showError('Please enter a template name', 'Template Error');
      return;
    }

    if (Object.keys(this.columnMapping).length === 0) {
      this.global.showError('No column mappings to save', 'Template Error');
      return;
    }

    const template: MappingTemplate = {
      name: this.newTemplateName.trim(),
      mapping: { ...this.columnMapping },
      entityType: this.entityType,
      createdDate: new Date()
    };

    this.templateStorage.saveTemplate(template);
    this.loadTemplates();
    this.newTemplateName = '';
    this.global.showSuccess('Template saved', `Template "${template.name}" saved successfully`);
  }

  // Validation methods
  validateData(): void {
    this.transformedData = this.transformData();
    this.validationErrors = this.validator.validateData(this.transformedData, this.entityType);
    this.preparePreviewData();
    this.currentStep = 2;
  }

  private transformData(): any[] {
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
    if (value === null || value === undefined || value === '') {
      return null;
    }

    // Numeric fields
    if (['coverageAmount', 'monthlyPremium', 'sharePercentage', 'policyCoverageAmount', 'beneficiaryCoveragePercent'].includes(field)) {
      const numValue = parseFloat(value);
      return isNaN(numValue) ? null : numValue;
    }

    // Boolean fields
    if (['deceased', 'loginAllowed'].includes(field)) {
      if (typeof value === 'boolean') return value;
      const strValue = String(value).toLowerCase().trim();
      return ['true', '1', 'yes', 'y'].includes(strValue);
    }

    // Date fields
    if (field.includes('Date') || field === 'dateOfBirth') {
      const date = new Date(value);
      return isNaN(date.getTime()) ? null : date.toISOString().split('T')[0];
    }

    // String fields (trim whitespace)
    return String(value).trim();
  }

  private preparePreviewData(): void {
    this.previewData = this.transformedData.slice(0, 10);
  }

  // Upload methods
  async uploadData(): Promise<void> {
    if (this.getErrorCount() > 0) {
      this.global.showError('Please fix all errors before uploading', 'Upload Error');
      return;
    }

    // For policy and combined uploads, company selection is required
    /*
    if ((this.entityType === 'policy' || this.entityType === 'combined') && !this.selectedCompanyId) {
      this.global.showError('Please select a company for policy upload', 'Upload Error');
      return;
    }
    */

    this.isProcessing = true;
    try {
      const endpoint = this.entityType === 'combined'
        ? '/admin/upload/combined-policies'
        : this.entityType === 'policy'
          ? '/admin/upload/policies'
          : '/admin/upload/beneficiaries';
      const companyId = (this.entityType === 'policy' || this.entityType === 'combined') ? this.selectedCompanyId : undefined;

      const result = await this.adminService.uploadData(endpoint, this.getValidRows(), companyId).toPromise();

      if (result) {
        this.global.showSuccess('Upload completed', `Successfully uploaded ${result.successCount} records`);
      }
      this.resetComponent();
    } catch (error: any) {
      this.global.showError(error.error?.message || 'Upload failed', 'Upload Error');
    } finally {
      this.isProcessing = false;
    }
  }

  private getValidRows(): any[] {
    const errorRows = new Set(this.validationErrors
      .filter(error => error.severity === 'error')
      .map(error => error.row));

    return this.transformedData.filter((_, index) => !errorRows.has(index + 1));
  }

  // Utility methods
  getMappedFields(): string[] {
    return Array.from(new Set(Object.values(this.columnMapping).filter(field => field)));
  }

  getErrorCount(): number {
    return this.validationErrors.filter(error => error.severity === 'error').length;
  }

  getWarningCount(): number {
    return this.validationErrors.filter(error => error.severity === 'warning').length;
  }

  getValidRowCount(): number {
    const errorRows = new Set(this.validationErrors
      .filter(error => error.severity === 'error')
      .map(error => error.row));

    return this.rawData.length - errorRows.size;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  removeFile(): void {
    this.selectedFile = null;
    this.resetComponent();
  }

  private resetComponent(): void {
    this.rawData = [];
    this.headers = [];
    this.columnMapping = {};
    this.validationErrors = [];
    this.transformedData = [];
    this.previewData = [];
    this.headerMappingData = [];
    this.currentStep = 0;
    this.selectedTemplate = '';
  }

  // Navigation methods
  goToStep(step: number): void {
    if (step === 1 && this.headers.length === 0) {
      this.global.showError('Please upload a file first', 'Navigation Error');
      return;
    }
    if (step === 2 && Object.keys(this.columnMapping).length === 0) {
      this.global.showError('Please map at least one column', 'Navigation Error');
      return;
    }

    if (step === 2) {
      this.validateData();
    } else {
      this.currentStep = step;
    }
  }

  canProceedToMapping(): boolean {
    return this.headers.length > 0;
  }

  canProceedToValidation(): boolean {
    return Object.keys(this.columnMapping).length > 0;
  }
}