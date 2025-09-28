import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import * as Papa from 'papaparse';
import { FileParseResult } from './file-upload.interfaces';

@Injectable({
  providedIn: 'root'
})
export class FileParserService {

  parseFile(file: File): Promise<FileParseResult> {
    const extension = file.name.split('.').pop()?.toLowerCase();

    if (extension === 'csv') {
      return this.parseCSV(file);
    } else if (['xlsx', 'xls'].includes(extension || '')) {
      return this.parseExcel(file);
    } else {
      return Promise.reject(new Error('Unsupported file format. Please use CSV, XLS, or XLSX files.'));
    }
  }

  private parseCSV(file: File): Promise<FileParseResult> {
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
          const data = result.data as any[];

          if (headers.length === 0 || data.length === 0) {
            reject(new Error('CSV file appears to be empty or has no valid data.'));
            return;
          }

          resolve({ data, headers });
        },
        error: (error) => reject(new Error(`CSV parsing failed: ${error.message}`))
      });
    });
  }

  private parseExcel(file: File): Promise<FileParseResult> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        try {
          const workbook = XLSX.read(e.target.result, { type: 'binary' });

          if (workbook.SheetNames.length === 0) {
            reject(new Error('Excel file contains no worksheets.'));
            return;
          }

          const worksheet = workbook.Sheets[workbook.SheetNames[0]];
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { defval: '' });

          if (jsonData.length === 0) {
            reject(new Error('Excel worksheet is empty or contains no valid data.'));
            return;
          }

          const headers = jsonData.length > 0 ? Object.keys(jsonData[0] as object) : [];
          resolve({ data: jsonData, headers });
        } catch (error) {
          reject(new Error(`Excel parsing error: ${error}`));
        }
      };

      reader.onerror = () => reject(new Error('File reading failed. Please check if the file is corrupted.'));
      reader.readAsBinaryString(file);
    });
  }

  validateFileBeforeParsing(file: File): void {
    const allowedExtensions = ['.csv', '.xlsx', '.xls'];
    const extension = file.name.toLowerCase().substr(file.name.lastIndexOf('.'));

    if (!allowedExtensions.includes(extension)) {
      throw new Error('Invalid file type. Only CSV, XLS, and XLSX files are allowed.');
    }

    if (file.size > 10 * 1024 * 1024) { // 10MB
      throw new Error('File too large. Maximum file size is 10MB.');
    }

    if (file.size === 0) {
      throw new Error('File is empty. Please select a valid file.');
    }
  }
}