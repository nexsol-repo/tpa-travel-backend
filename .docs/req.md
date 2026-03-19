```json
{
  "contractId": 162,
  "cardNo": "9440325500885595",
  "efctPrd": "203009",
  "dporNm": "이정진",
  "dporCd": "941118"
},
//errror response
{
  "contract": null,
  "errCd": "69999",
  "errMsg": "카드승인 처리시 오류가 발생하였습니다. 계약자코드은(는) 필수입력항목 입니다",
  "insurer": null,
  "ok": false,
  "plan": null
}


/** 여행자보험 가입(결제) */
@PostMapping("/travel/contract/completed")
public ContractCompletedResponse completed(@RequestParam(defaultValue = "TPA") String companyCode,
@RequestBody ContractCompletedRequest request) {
return service.completed(companyCode, request);
}
```

```text

⏺ 에러 메시지가 "계약자코드은(는) 필수입력항목 입니다"인데, 요청에 계약자코드 관련 필드가 빠져있는 것 같습니다. 코드를 확인해보겠습니다.

⏺ Explore(Find ContractCompletedRequest)
  ⎿  Done (34 tool uses · 74.7k tokens · 58s)
  (ctrl+o to expand)

⏺ Searched for 6 patterns, read 6 files (ctrl+o to expand)

⏺ 에러 "계약자코드은(는) 필수입력항목 입니다"는 메리츠 API(013 카드승인)에서 반환된 에러입니다.

  분석 결과:

  1. 에러코드 69999 = 기타 비즈니스 오류로, 메리츠 내부 validation에서 필수 필드가 누락된 경우입니다.
  2. req.md의 요청 구조를 보면 cardNo, efctPrd, dporNm, dporCd가 flat하게 들어오고 있는데, 브릿지의 CrdApvRequest는 이 필드들을 ctrTrsInfBcVo 객체 안에 중첩해서 받도록 되어
  있습니다.
  3. **"계약자코드"**는 메리츠 내부에서 dporCd(예금주코드)를 지칭하는 것으로 보입니다. 문서 기준:
    - 개인: 생년월일 6자리
    - 법인: 사업자번호 10자리
    - 암호화 필수

  가능한 원인들:

  ┌──────────────────────┬─────────────────────────────────────────────────────────────────────────────────────┐
  │         원인         │                                        설명                                         │
  ├──────────────────────┼─────────────────────────────────────────────────────────────────────────────────────┤
  │ ctrTrsInfBcVo가 null │ travel-api에서 카드정보를 ctrTrsInfBcVo 객체로 감싸지 않고 flat하게 보냈을 가능성   │
  ├──────────────────────┼─────────────────────────────────────────────────────────────────────────────────────┤
  │ dporCd 미암호화      │ 941118이 평문으로 들어가면 메리츠에서 파싱 실패 → 필수값 누락으로 처리              │
  ├──────────────────────┼─────────────────────────────────────────────────────────────────────────────────────┤
  │ bizpeNo 누락         │ 계약자사업자번호가 config에서 자동세팅되는데, 해당 company config가 비어있을 가능성 │
  └──────────────────────┴─────────────────────────────────────────────────────────────────────────────────────┘

  travel-api에서 브릿지로 보내는 실제 요청 JSON (특히 ctrTrsInfBcVo 구조와 암호화 여부)을 확인해보시겠어요? 스냅샷 테이블에 요청 로그가 남아있을 수도 있습니다.

```