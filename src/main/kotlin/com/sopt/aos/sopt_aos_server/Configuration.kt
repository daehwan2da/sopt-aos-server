package com.sopt.aos.sopt_aos_server

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.ConstructorBinding
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
@ConfigurationPropertiesScan
class Configuration(
    private val awsProperties: AwsProperties
) {

    @Bean
    fun datasource(): DataSource {
        val embeddedDataSourceFactory = EmbeddedDatabaseFactory()
        embeddedDataSourceFactory.setDatabaseName("sopt")
        embeddedDataSourceFactory.setDatabaseType(EmbeddedDatabaseType.H2)

        return embeddedDataSourceFactory.database
    }

    @Bean("s3C")
    fun s3(): AmazonS3Client {
        val basicAWSCredentials = BasicAWSCredentials(awsProperties.accessKey, awsProperties.secretKey)

        return AmazonS3ClientBuilder.standard()
            .withRegion("ap-northeast-2")
            .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
            .build() as AmazonS3Client
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "cloud.aws.credentials")
    data class AwsProperties(
        val accessKey: String,
        val secretKey: String
    )
}