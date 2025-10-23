package com.mycom.myapp.commit.dto;

import com.mycom.myapp.commit.entity.CommitFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommitFileDto {
    private Long commitFileId;
    private String filename;

    public static CommitFileDto fromEntity(CommitFile commitFile) {
        if (commitFile == null) return null;
        return CommitFileDto.builder()
                .commitFileId(commitFile.getId())
                .filename(commitFile.getFilename())
                .build();
    }
}
