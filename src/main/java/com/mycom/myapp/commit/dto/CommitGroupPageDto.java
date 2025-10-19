package com.mycom.myapp.commit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommitGroupPageDto {
    private List<CommitGroupDto> groups;
    private int currentPage;
    private int totalPages;
    private boolean hasNext;
}
