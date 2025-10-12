import { Injectable } from '@angular/core';
import { ValidationError } from './file-upload.interfaces';

@Injectable({
  providedIn: 'root'
})
export class UploadValidatorService {

  validateData(data: any[], entityType: 'policy' | 'beneficiary' | 'combined'): ValidationError[] {
    const errors: ValidationError[] = [];

    data.forEach((row, index) => {
      if (entityType === 'policy') {
        errors.push(...this.validatePolicyRow(row, index + 1));
      } else if (entityType === 'beneficiary') {
        errors.push(...this.validateBeneficiaryRow(row, index + 1));
      } else if (entityType === 'combined') {
        errors.push(...this.validateCombinedRow(row, index + 1));
      }
    });

    return errors;
  }

  private validatePolicyRow(row: any, rowNumber: number): ValidationError[] {
    const errors: ValidationError[] = [];

    // Required field validations
    if (!row.policyNumber?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyNumber',
        message: 'Policy number is required',
        severity: 'error'
      });
    }

    /*
    if (!row.insuranceCompany?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'insuranceCompany',
        message: 'Insurance company is required',
        severity: 'error'
      });
    }
      */

    if (!row.policyType?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyType',
        message: 'Policy type is required',
        severity: 'error'
      });
    }

    // Numeric validations
    if (row.coverageAmount !== undefined && row.coverageAmount !== null && row.coverageAmount !== '') {
      const coverage = parseFloat(row.coverageAmount);
      if (isNaN(coverage) || coverage <= 0) {
        errors.push({
          row: rowNumber,
          field: 'coverageAmount',
          message: 'Coverage amount must be a positive number',
          severity: 'error'
        });
      }
    }

    if (row.monthlyPremium !== undefined && row.monthlyPremium !== null && row.monthlyPremium !== '') {
      const premium = parseFloat(row.monthlyPremium);
      if (isNaN(premium) || premium <= 0) {
        errors.push({
          row: rowNumber,
          field: 'monthlyPremium',
          message: 'Monthly premium must be a positive number',
          severity: 'error'
        });
      }
    }

    // Date validations
    if (row.deceasedDate && !this.isValidDate(row.deceasedDate)) {
      errors.push({
        row: rowNumber,
        field: 'deceasedDate',
        message: 'Invalid deceased date format (use YYYY-MM-DD)',
        severity: 'error'
      });
    }

    // Boolean validations
    if (row.deceased !== undefined && !this.isValidBoolean(row.deceased)) {
      errors.push({
        row: rowNumber,
        field: 'deceased',
        message: 'Deceased field must be true/false, yes/no, or 1/0',
        severity: 'error'
      });
    }

    return errors;
  }

  private validateBeneficiaryRow(row: any, rowNumber: number): ValidationError[] {
    const errors: ValidationError[] = [];

    // Required field validations
    if (!row.fullName?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'fullName',
        message: 'Full name is required',
        severity: 'error'
      });
    }

    if (!row.idNumber?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'idNumber',
        message: 'ID number is required',
        severity: 'error'
      });
    }

    if (!row.relationship?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'relationship',
        message: 'Relationship is required',
        severity: 'error'
      });
    }

    // ID number format validation
    if (row.idType === 'SAID' && row.idNumber && !/^\d{13}$/.test(row.idNumber.toString())) {
      errors.push({
        row: rowNumber,
        field: 'idNumber',
        message: 'South African ID must be exactly 13 digits',
        severity: 'error'
      });
    }

    // Share percentage validation
    if (row.sharePercentage !== undefined && row.sharePercentage !== null && row.sharePercentage !== '') {
      const percentage = parseFloat(row.sharePercentage);
      if (isNaN(percentage) || percentage <= 0 || percentage > 100) {
        errors.push({
          row: rowNumber,
          field: 'sharePercentage',
          message: 'Share percentage must be between 0.1 and 100',
          severity: 'error'
        });
      }
    }

    // Email validation
    if (row.email && !this.isValidEmail(row.email.toString())) {
      errors.push({
        row: rowNumber,
        field: 'email',
        message: 'Invalid email format',
        severity: 'error'
      });
    }

    // Phone or email requirement
    const hasPhone = row.phone?.toString().trim();
    const hasEmail = row.email?.toString().trim();
    if (!hasPhone && !hasEmail) {
      errors.push({
        row: rowNumber,
        field: 'contact',
        message: 'Either phone number or email must be provided',
        severity: 'error'
      });
    }

    // Date validations
    if (row.dateOfBirth && !this.isValidDate(row.dateOfBirth)) {
      errors.push({
        row: rowNumber,
        field: 'dateOfBirth',
        message: 'Invalid date of birth format (use YYYY-MM-DD)',
        severity: 'error'
      });
    }

    if (row.deceasedDate && !this.isValidDate(row.deceasedDate)) {
      errors.push({
        row: rowNumber,
        field: 'deceasedDate',
        message: 'Invalid deceased date format (use YYYY-MM-DD)',
        severity: 'error'
      });
    }

    return errors;
  }

  private validateCombinedRow(row: any, rowNumber: number): ValidationError[] {
    const errors: ValidationError[] = [];

    // Validate policyholder required fields
    if (!row.policyholderName?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyholderName',
        message: 'Policyholder name is required',
        severity: 'error'
      });
    }

    if (!row.policyholderSurname?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyholderSurname',
        message: 'Policyholder surname is required',
        severity: 'error'
      });
    }

    if (!row.policyholderEmail?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyholderEmail',
        message: 'Policyholder email is required',
        severity: 'error'
      });
    } else if (!this.isValidEmail(row.policyholderEmail.toString())) {
      errors.push({
        row: rowNumber,
        field: 'policyholderEmail',
        message: 'Invalid policyholder email format',
        severity: 'error'
      });
    }

    // Validate policy required fields
    if (!row.policyNumber?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyNumber',
        message: 'Policy number is required',
        severity: 'error'
      });
    }

    if (!row.insuranceProvider?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'insuranceProvider',
        message: 'Insurance provider is required',
        severity: 'error'
      });
    }

    if (!row.policyType?.toString().trim()) {
      errors.push({
        row: rowNumber,
        field: 'policyType',
        message: 'Policy type is required',
        severity: 'error'
      });
    } else {
      const validTypes = ['life', 'funeral', 'LIFE', 'FUNERAL'];
      if (!validTypes.includes(row.policyType.toString().trim())) {
        errors.push({
          row: rowNumber,
          field: 'policyType',
          message: 'Policy type must be LIFE or FUNERAL',
          severity: 'error'
        });
      }
    }

    // Validate coverage amount
    if (row.policyCoverageAmount !== undefined && row.policyCoverageAmount !== null && row.policyCoverageAmount !== '') {
      const coverage = parseFloat(row.policyCoverageAmount);
      if (isNaN(coverage) || coverage <= 0) {
        errors.push({
          row: rowNumber,
          field: 'policyCoverageAmount',
          message: 'Coverage amount must be a positive number',
          severity: 'error'
        });
      }
    }

    // Validate beneficiary fields (if provided)
    if (row.beneficiaryName?.toString().trim()) {
      if (row.beneficiaryCoveragePercent !== undefined && row.beneficiaryCoveragePercent !== null && row.beneficiaryCoveragePercent !== '') {
        const percentage = parseFloat(row.beneficiaryCoveragePercent);
        if (isNaN(percentage) || percentage <= 0 || percentage > 100) {
          errors.push({
            row: rowNumber,
            field: 'beneficiaryCoveragePercent',
            message: 'Beneficiary coverage percent must be between 0.1 and 100',
            severity: 'error'
          });
        }
      }
    }

    return errors;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private isValidDate(dateStr: string): boolean {
    if (!dateStr) return false;
    const date = new Date(dateStr);
    return !isNaN(date.getTime()) && date.getTime() > 0;
  }

  private isValidBoolean(value: any): boolean {
    if (typeof value === 'boolean') return true;
    const strValue = String(value).toLowerCase().trim();
    return ['true', 'false', '1', '0', 'yes', 'no', 'y', 'n'].includes(strValue);
  }
}