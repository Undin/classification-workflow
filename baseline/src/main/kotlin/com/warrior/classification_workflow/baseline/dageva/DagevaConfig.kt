package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DagevaConfig(
        @JsonProperty("server") val serverSettings: ServerSettings,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("jar") val jar: String,
        @JsonProperty("log_folder") val logFolder: String,
        @JsonProperty("config_template") val configTemplate: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServerSettings(
        @JsonProperty("interpreter") val interpreter: String,
        @JsonProperty("script") val script: String,
        @JsonProperty("log_folder") val logFolder: String,
        @JsonProperty("port") val port: Int,
        @JsonProperty("workers") val workers: Int
)
