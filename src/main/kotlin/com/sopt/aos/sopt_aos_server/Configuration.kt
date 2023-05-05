package com.sopt.aos.sopt_aos_server

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource


@Configuration
@EnableAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class
    ]
)
class Configuration {

    @Bean
    fun datasource(): DataSource {
        val embeddedDataSourceFactory = EmbeddedDatabaseFactory()
        embeddedDataSourceFactory.setDatabaseName("sopt")
        embeddedDataSourceFactory.setDatabaseType(EmbeddedDatabaseType.H2)

        return embeddedDataSourceFactory.database
    }
}