package com.dastanapps.javascriptengine

import com.api.jsonata4java.expressions.EvaluateException
import com.api.jsonata4java.expressions.Expressions
import com.api.jsonata4java.expressions.ParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class JsonataTest {

    @Test
    fun testJsonataExpression() {
//        val expression = "\$sum(example.value)"
        val expression = """

(
  ${'$'}language := config.lang;
  ${'$'}localization := config.localization;
  ${'$'}formatNumFunc := function(${'$'}val) {
    (
      ${'$'}formattedNum := ${'$'}formatNumber(${'$'}val, "#,##0.00");
      ${'$'}formattedNum ? ${'$'}formattedNum : ${'$'}string(0.00)
    )
  };
  ${'$'}transformedData := data.result
    .[
       memberRight!=null ?{
        "props": {
          "alignment": "vertical",
          "header": { "title": memberRight.title },
          "rows": [
            { "value": memberRight.totalPeriod, "title": memberRight.totalPeriodTitle },
            { "value": ${'$'}formatNumFunc(memberRight.pensionableSalary), "title": memberRight.pensionableSalaryTitle },
            { "value": memberRight.accrualRate, "title": memberRight.accrualRateTitle },
            { "value": ${'$'}formatNumFunc(memberRight.acquiredPension), "title": memberRight.acquiredPensionTitle },
            { "value": ${'$'}formatNumFunc(memberRight.gratuityAmount), "title": memberRight.gratuityAmountTitle },
            { "value": memberRight.note, "title": ${'$'}lookup(${'$'}localization, "note.label") }
          ][${'$'}exists(value) and value != null and value != ""]
        },
        "metadata": {}
      },
      memberLongService!=null ? {
    "props": {
      "alignment": "vertical",
      "header": { "title": memberLongService.title },
      "rows": [
        { "value": memberLongService.period, "title": memberLongService.periodTitle },
        { "value": ${'$'}formatNumFunc(memberLongService.benefits), "title": memberLongService.benefitsTitle }
      ][${'$'}exists(value) and value != null and value != ""]
    },
    "metadata": {}
  }
    ][${'$'}exists(props.rows) and ${'$'}count(props.rows) > 0];
  {
    "status": "success",
    "data": { "list": ${'$'}type(${'$'}transformedData) = "array" ? ${'$'}transformedData : [${'$'}transformedData], "filters": data.filters }
  }
)
        """.trimIndent()
        val data = """
            {
    "success": true,
    "message": "Success",
    "data": {
        "error": false,
        "status": true,
        "responseAt": "2025-12-18T06:01:52.805Z",
        "result": {
            "maintenance": null,
            "message": null,
            "memberLongService": null,
            "memberRight": {
                "accrualRate": "74.4%",
                "accrualRateTitle": "The percentage of entitlement",
                "acquiredPension": 138690.36,
                "acquiredPensionTitle": "Acquired Pension until 30/11/2023",
                "cardTitle": "Know about your rights acquired before 01 Dec 2023",
                "gratuityAmount": 0,
                "gratuityAmountTitle": "Value of the Benefit for a Period of Service Exceeding  (25 )Years.",
                "note": "• At the end of the Active Members service, the above-mentioned acquired entitlements will be added to the rights acquired based on the period of service after that date, according to the maximum pensionable salary specified in Law No. (2) of 2000 Regarding Civil Retirement Pensions and Benefits in the Emirate of Abu Dhabi.\r\n• The above-mentioned acquired entitlements are calculated using data and documents supplied by entities to the Abu Dhabi Pension Fund. If the data is found to be inaccurate, the Fund reserves the right to seek reimbursement from the entities for any contribution disparities and additional amounts that may arise.\r\n• The percentages of entitlement to pension that are indicated have been computed under the assumption that Active Members will meet the pension eligibility requirements upon the end of their service. If this requirement is not satisfied, Active Members will receive only retirement benefits upon their end of service.\r\n• The retirement pension and benefit calculation for more than a term of service exceeding 25 years is based on the total added service periods as well as the service periods under installment payment, with the assumption that Active Members would continue to pay the full amount. If the added service period payment is not fully settled, the pension and benefits will be recalculated based on the actual amounts paid.\r\n• Pensionable Salary: The average of the last three years, basic salary plus the last allowances until 30/11/2023 (Before the Effective Date of the Amending Law).",
                "pensionableSalary": 186411.78,
                "pensionableSalaryTitle": "Pensionable Salary until 30/11/2023",
                "title": "Rights acquired before 01 Dec 2023",
                "totalPeriod": "23 Years 2 Months 19 Days",
                "totalPeriodTitle": "Term of Service until 30/11/2023"
            }
        },
        "statusCode": 200,
        "messageEn": "",
        "messageAr": ""
    },
    "error": {},
    "requestId": "8a32914b-9162-4a98-9d97-01c2e6149391"
}
            """.trimIndent()

        try {
            val expr = Expressions.parse(expression)
            val mapper = ObjectMapper()
            val input: JsonNode = mapper.readTree(data)
            val result: JsonNode? = expr.evaluate(input)
            println(result)
        } catch (e: IOException) {
            e.printStackTrace()
            org.junit.Assert.fail("IOException occurred: ${e.message}")
        } catch (e: ParseException) {
            e.printStackTrace()
            org.junit.Assert.fail("ParseException occurred: ${e.message}")
        } catch (e: EvaluateException) {
            e.printStackTrace()
            org.junit.Assert.fail("EvaluateException occurred: ${e.message}")
        }
    }
}
