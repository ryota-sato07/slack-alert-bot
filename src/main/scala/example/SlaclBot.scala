package lambda

import java.time.LocalDate
import play.api.{ Logging, Configuration }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import software.amazon.awssdk.services.costexplorer.model._
import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest

/**
 * AWSの利用料金を算出して通知する
 */
object CostNotification extends Logging {

  // --[ Properties ]-----------------------------------------------------------
  protected val config  = Configuration()
  val SLACK_WEBHOOK_URL = config.get[String]("ws.slack.endpoint")

  // --[ Methods ]--------------------------------------------------------------
  /**
   * テスト用
   */
  def main(args: Array[String]): Unit = {
    val cost = getCost()
    postToSlack(SLACK_WEBHOOK_URL, cost)
  }

  /**
   * AWS利用料金の取得
   */
  def getCost(): Double = {
    val costExplorerClient = CostExplorerClient.builder().region(Region.US_EAST_1).build()

    val today     = LocalDate.now()
    val firstDate = today.withDayOfMonth(1)
    val yesterday = today.minusMonths(1)

    val request   = GetCostAndUsageRequest.builder()
      .timePeriod(
        DateInterval.builder()
          .start(firstDate)
          .end(yesterday)
          .build()
      )
      .granularity(Granularity.DAILY)
      .metrics("UnblendedCost")
      .build()

    val response = costExplorerClient.getCostAndUsage(request)

    response.resultsByTime().asScala.map { result =>
      result.total().get("UnblendedCost").amount().toDouble
    }.sum
  }

  /**
   * Slackへ通知を飛ばす
   */
  def postToSlack(webhookUrl: String, cost: Double): Unit = {
    val methodsClient: MethodsClient = Slack.getInstance().methods()

    val message = s"AWS の料金は、現在合計 $cost ドルです。"
    val request = ChatPostMessageRequest.builder()
      .channel("#general")
      .text(message)
      .build()

    methodsClient.chatPostMessage(request)
  }
}

