package co.za.insurance.admin;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadResult {
    private int successCount;
    private int errorCount;
    private List<String> errors;
    private String message;
}