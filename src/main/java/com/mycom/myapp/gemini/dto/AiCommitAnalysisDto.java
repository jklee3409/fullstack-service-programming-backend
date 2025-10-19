package com.mycom.myapp.gemini.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AiCommitAnalysisDto {
    private String summary;
    private String commitType;
    private List<ChangeDetail> mainChanges;

    @Getter @Setter @ToString
    public static class ChangeDetail {
        private String file;
        private String changeDescription;
    }
}
