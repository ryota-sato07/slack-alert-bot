package example

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder
import com.amazonaws.services.costexplorer.model.{GetCostAndUsageRequest, DateInterval, Granularity}
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import java.time.LocalDate

class AwsSlackBot extends RequestHandler[Object, Unit] {
  def handleRequest(input: Object, context: Context): Unit = {
    val costExplorer = AWSCostExplorerClientBuilder.defaultClient()
    val startDate    = LocalDate.now().minusDays(1).toString
    val endDate      = LocalDate.now().toString
    val request      = new GetCostAndUsageRequest()
      .withTimePeriod(new DateInterval().withStart(startDate).withEnd(endDate))
      .withGranularity(Granularity.DAILY)
      .withMetrics("UnblendedCost")

    val result  = costExplorer.getCostAndUsage(request)
    val cost    = result.getResultsByTime.get(0).getTotal.get("UnblendedCost").getAmount
    val message = s"昨日のAWS料金は $$cost です。"

    val slackWebhookUrl = System.getenv("SLACK_WEBHOOK_URL")
    val slack           = Slack.getInstance()
    val payload         = Payload.builder().text(message).build()

    slack.send(slackWebhookUrl, payload)
  }
}
