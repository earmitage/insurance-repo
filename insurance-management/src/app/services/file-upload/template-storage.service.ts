import { Injectable } from '@angular/core';
import { MappingTemplate } from './file-upload.interfaces';

@Injectable({
  providedIn: 'root'
})
export class TemplateStorageService {
  private readonly STORAGE_KEY = 'insurance-file-upload-templates';

  saveTemplate(template: MappingTemplate): void {
    const templates = this.getTemplates();
    const existingIndex = templates.findIndex(t => t.name === template.name && t.entityType === template.entityType);

    if (existingIndex >= 0) {
      templates[existingIndex] = { ...template, createdDate: new Date() };
    } else {
      templates.push({ ...template, createdDate: new Date() });
    }

    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(templates));
  }

  getTemplates(entityType?: 'policy' | 'beneficiary' | 'combined'): MappingTemplate[] {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    const templates = stored ? JSON.parse(stored) : [];

    if (entityType) {
      return templates.filter((t: MappingTemplate) => t.entityType === entityType);
    }

    return templates;
  }

  deleteTemplate(name: string, entityType: 'policy' | 'beneficiary' | 'combined'): void {
    const templates = this.getTemplates().filter(t => !(t.name === name && t.entityType === entityType));
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(templates));
  }

  loadTemplate(name: string, entityType: 'policy' | 'beneficiary' | 'combined'): MappingTemplate | null {
    const templates = this.getTemplates();
    return templates.find(t => t.name === name && t.entityType === entityType) || null;
  }

  templateExists(name: string, entityType: 'policy' | 'beneficiary' | 'combined'): boolean {
    return this.getTemplates().some(t => t.name === name && t.entityType === entityType);
  }
}