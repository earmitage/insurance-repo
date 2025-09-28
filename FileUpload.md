Implementation Strategy: Upload & Map Excel/CSV Data in Angular

npm install xlsx papaparse

File types supported, .xlsx, .xls (Excel),.csv

File Upload and Parsing
✔️ Step 1: Create a file upload input
<input type="file" (change)="onFileChange($event)" accept=".xlsx,.xls,.csv" />

✔️ Step 2: Detect file type and parse
onFileChange(event: Event): void {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;

  const ext = file.name.split('.').pop()?.toLowerCase();
  ext === 'csv' ? this.parseCSV(file) : this.parseExcel(file);
}

✔️ Step 3: Parse CSV using papaparse
parseCSV(file: File): void {
  Papa.parse(file, {
    header: true,
    skipEmptyLines: true,
    complete: (result) => {
      this.rawData = result.data;
      this.headers = Object.keys(this.rawData[0] || {});
      this.initColumnMapping();
    }
  });
}

✔️ Step 4: Parse Excel using xlsx
parseExcel(file: File): void {
  const reader = new FileReader();
  reader.onload = (e: any) => {
    const wb = XLSX.read(e.target.result, { type: 'binary' });
    const sheet = wb.Sheets[wb.SheetNames[0]];
    this.rawData = XLSX.utils.sheet_to_json(sheet, { defval: '' });
    this.headers = Object.keys(this.rawData[0] || {});
    this.initColumnMapping();
  };
  reader.readAsBinaryString(file);
}

🧠 3. Column Mapping
✔️ Step 5: Define possible DTO fields
dtoFields = {
  policy: [...],         // fields from AddPolicy
  beneficiary: [...]     // fields from AddBeneficiary
};

✔️ Step 6: Build UI to map columns
<table>
  <tr *ngFor="let header of headers">
    <td>{{ header }}</td>
    <td>
      <select [(ngModel)]="columnMapping[header]">
        <option [value]="''">-- Select Field --</option>
        <option *ngFor="let field of selectedDtoFields" [value]="field">{{ field }}</option>
      </select>
    </td>
  </tr>
</table>

🤖 4. Auto-Mapping Suggestions (Enhanced Feature)
✔️ Step 7: Auto-map based on string similarity
autoMapColumns(): void {
  this.columnMapping = {};
  for (const header of this.headers) {
    const suggestion = this.findClosestField(header);
    if (suggestion) this.columnMapping[header] = suggestion;
  }
}

findClosestField(header: string): string | null {
  const allFields = [...this.dtoFields.policy, ...this.dtoFields.beneficiary];
  const cleanedHeader = header.toLowerCase().replace(/[^a-z]/g, '');

  let bestMatch = null;
  let highestScore = 0;

  for (const field of allFields) {
    const cleanedField = field.toLowerCase().replace(/[^a-z]/g, '');
    const score = this.similarity(cleanedHeader, cleanedField);
    if (score > highestScore && score > 0.5) {
      highestScore = score;
      bestMatch = field;
    }
  }

  return bestMatch;
}

similarity(a: string, b: string): number {
  let matches = 0;
  for (let i = 0; i < Math.min(a.length, b.length); i++) {
    if (a[i] === b[i]) matches++;
  }
  return matches / Math.max(a.length, b.length);
}

💾 5. Template Saving & Reuse (Enhanced Feature)
✔️ Step 8: Save mapping templates in localStorage or backend
saveMappingTemplate(name: string): void {
  const template = {
    name,
    mapping: this.columnMapping
  };
  localStorage.setItem(`mapping-${name}`, JSON.stringify(template));
}

loadMappingTemplate(name: string): void {
  const template = localStorage.getItem(`mapping-${name}`);
  if (template) {
    const { mapping } = JSON.parse(template);
    this.columnMapping = mapping;
  }
}


UI idea: dropdown of saved templates, and a “Save Template” button.

🔁 6. Data Transformation to DTOs
✔️ Step 9: Transform rawData into AddPolicy or AddBeneficiary objects
transformToDTO(): any[] {
  return this.rawData.map(row => {
    const obj: any = {};
    for (const [fileCol, systemField] of Object.entries(this.columnMapping)) {
      if (systemField) {
        obj[systemField] = this.parseFieldValue(systemField, row[fileCol]);
      }
    }
    return obj;
  });
}

parseFieldValue(field: string, value: any): any {
  if (['coverageAmount', 'monthlyPremium', 'sharePercentage'].includes(field)) {
    return parseFloat(value);
  }
  if (['deceased', 'loginAllowed'].includes(field)) {
    return value.toString().toLowerCase() === 'true';
  }
  if (field.includes('Date')) {
    return new Date(value);
  }
  return value;
}

✅ 7. Validation (Enhanced Feature)
✔️ Step 10: Validate required fields and field types
validateRows(rows: any[]): string[] {
  const errors: string[] = [];
  rows.forEach((row, index) => {
    if (!row.policyNumber) {
      errors.push(`Row ${index + 1}: Missing policyNumber`);
    }
    if (row.sharePercentage && row.sharePercentage > 100) {
      errors.push(`Row ${index + 1}: sharePercentage > 100`);
    }
    // Add more validations
  });
  return errors;
}


Display errors before upload and block submission if critical.

👁️ 8. Preview Transformed Data
✔️ Step 11: Preview first few mapped rows
<table *ngIf="transformedData.length">
  <thead>
    <tr>
      <th *ngFor="let key of displayedFields">{{ key }}</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let row of transformedData.slice(0, 5)">
      <td *ngFor="let key of displayedFields">{{ row[key] }}</td>
    </tr>
  </tbody>
</table>

☁️ 9. Upload to Backend
✔️ Step 12: POST transformed data
upload(): void {
  const data = this.transformToDTO();
  const errors = this.validateRows(data);

  if (errors.length > 0) {
    alert('Fix errors before uploading:\n' + errors.join('\n'));
    return;
  }

  this.http.post('/api/policies/upload', data).subscribe({
    next: () => alert('Upload successful'),
    error: err => alert('Upload failed: ' + err.message)
  });
}


Backend should expect a list of AddPolicy or AddBeneficiary.

🔗 10. Support Multiple Entities (Enhanced Feature)
✔️ Step 13: Detect entity type per file or per row

Option A: Let user specify file type (Policy or Beneficiary)

Option B: Detect based on mapped fields (if mapping includes policyNumber, assume Policy)

detectEntityType(): 'policy' | 'beneficiary' {
  const fields = Object.values(this.columnMapping);
  return fields.includes('policyNumber') ? 'policy' : 'beneficiary';
}


If both exist, support nested mapping (e.g., group beneficiaries by policyNumber).


🧩 Summary: Master Checklist ✅
#	Step	Description
1	Setup	Install xlsx, papaparse
2	File Upload	Handle file selection & extension check
3	Parsing	Parse CSV/Excel into JS objects
4	Column Mapping	UI to map uploaded columns to DTO fields
5	Auto-Mapping	Suggest mappings via string similarity
6	Save/Load Templates	Store mapping configs for reuse
7	Data Transformation	Convert raw data → DTO-compliant objects
8	Validation	Ensure required fields and correct types