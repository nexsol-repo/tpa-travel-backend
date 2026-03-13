package com.nexsol.tpa.core.api.controller.v1.request;

import java.util.List;

import com.nexsol.tpa.core.domain.premium.PlanCondition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlanRequest {

    @NotNull private Long insurerId;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$")
    private String insBgnDt;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$")
    private String insEdDt;

    @NotBlank private String trvArCd;

    @NotNull
    @Min(0)
    private Integer representativeIndex = 0;

    @NotEmpty @Valid private List<Insured> insuredList;

    private Boolean silsonExclude = false;

    public PlanCondition toPlanCondition() {
        List<PlanCondition.Insured> insured =
                insuredList.stream()
                        .map(i -> new PlanCondition.Insured(i.getBirth(), i.getGender()))
                        .toList();
        return new PlanCondition(
                insurerId, insBgnDt, insEdDt, trvArCd, representativeIndex, insured, silsonExclude);
    }

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
