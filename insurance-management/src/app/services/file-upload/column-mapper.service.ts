import { Injectable } from '@angular/core';
import { ColumnMapping } from './file-upload.interfaces';

@Injectable({
  providedIn: 'root'
})
export class ColumnMapperService {

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

    for (const field of fields) {
      const cleanField = this.normalizeString(field);

      // Exact match (highest priority)
      if (cleanHeader === cleanField) {
        return field;
      }

      // Calculate similarity scores
      const levenshteinScore = this.levenshteinSimilarity(cleanHeader, cleanField);
      const substringScore = this.substringSimilarity(cleanHeader, cleanField);
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

  formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }
}