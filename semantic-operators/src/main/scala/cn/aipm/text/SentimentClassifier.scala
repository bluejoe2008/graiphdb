package cn.aipm.text

import cn.pidb.blob.PropertyExtractor
import cn.aipm.service.ServiceInitializer
import cn.pidb.util.Configuration
import cn.pidb.util.ConfigurationUtils._

class SentimentClassifier extends PropertyExtractor with ServiceInitializer {

  override def declareProperties() = Map("sentiment" -> classOf[String])

  override def extract(text: Any): Map[String, Any] = {

    val sentiment = service.sentimentClassifier(text.asInstanceOf[String])
    Map("sentiment" -> sentiment)
  }

}

