package com.challenge.files.configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfiguration {
    @Value("${AWS.accessKey}")
    private String AWSAccessKey;

    @Value("${AWS.secret}")
    private String AWSSecretKey;

    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials myCredentials = new BasicAWSCredentials(
                AWSAccessKey, AWSSecretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(myCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}
