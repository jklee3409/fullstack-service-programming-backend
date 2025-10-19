package com.mycom.myapp.commit.dto;

import com.mycom.myapp.commit.dto.response.CommitResponseDto;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommitGroupDto {
    private LocalDate date;
    private List<CommitResponseDto> commits;
}
