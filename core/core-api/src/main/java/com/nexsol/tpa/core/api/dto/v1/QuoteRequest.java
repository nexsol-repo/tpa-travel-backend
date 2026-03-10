package com.nexsol.tpa.core.api.dto.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class QuoteRequest {

    @NotNull
    private Long insurerId;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$")
    private String insBgnDt;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$")
    private String insEdDt;

    @NotBlank
    private String trvArCd;

    @NotNull
    @Min(0)
    private Integer representativeIndex = 0;

    @NotEmpty
    @Valid
    private List<Insured> insuredList;

    @Data
    public static class Insured {

        @NotBlank
        @Pattern(regexp = "^\\d{8}$")
        private String birth;

        @NotBlank
        @Pattern(regexp = "^[12]$")
        private String gender;

    }

}
