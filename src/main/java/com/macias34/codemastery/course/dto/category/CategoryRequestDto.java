package com.macias34.codemastery.course.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Create or update category
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CategoryRequestDto {
    private String name;
}
