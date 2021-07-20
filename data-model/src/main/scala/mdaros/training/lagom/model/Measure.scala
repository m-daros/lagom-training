package mdaros.training.lagom.model

import java.util.Date

case class Measure ( deviceId: String, metric: String, timestamp: Date, value: Double )