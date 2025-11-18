package com.octal.fsm.service;

import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class S3PresignedUrlService {

    @Value(value = "${aws.secret-key}")
    private final String awsAccessKey;

    @Value(value = "${aws.access-key}")
    private final String awsSecretKey;

    @Value("${aws.region}")
    private final String awsRegion;

    @Value("${aws.s3-bucket}")
    private final String awsS3BucketName;

    @Value("${aws.base-url}")
    private final String awsS3BaseUrl;


    private final S3Presigner presigner;
    //private final String bucketName = "yutka-fence";

    public S3PresignedUrlService(@Value("${aws.access-key}") String awsAccessKey,
                                 @Value("${aws.secret-key}") String awsSecretKey,
                                 @Value("${aws.region}") String awsRegion,
                                 @Value("${aws.s3-bucket}") String awsS3BucketName,
                                 @Value("${aws.base-url}") String awsS3BaseUrl
    ) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.awsRegion = awsRegion;
        this.awsS3BucketName = awsS3BucketName;
        this.awsS3BaseUrl = awsS3BaseUrl;
        this.presigner = S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                ))
                .build();
    }

    public String generatePresignedUrl(String path, String contentType) {
        try {
            String extension = getExtensionFromContentType(contentType);
            String objectKey = !TextUtils.isEmpty(path) ? path : "default/" + System.currentTimeMillis() + "-file." + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsS3BucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(r -> r
                    .signatureDuration(Duration.ofHours(1))
                    .putObjectRequest(putObjectRequest)
            );

            URL url = presignedRequest.url();
            return url.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage(), e);
        }
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return "bin";
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "video/mp4":
                return "mp4";
            case "application/pdf":
                return "pdf";
            default:
                return "bin";
        }
    }
}
