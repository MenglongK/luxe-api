package co.istad.corefeature.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class MinioConfiguration {

    @Value("${minio.url:http://localhost:9000}")
    private String url;

    @Value("${minio.access-key:luxe}")
    private String accessKey;

    @Value("${minio.secret-key:luxe@admin168}")
    private String secretKey;

    @Value("${minio.bucket-name:luxe-bucket}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                log.info("Provisioning MinIO bucket: {}", bucketName);
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                String publicReadPolicy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

                client.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucketName).config(publicReadPolicy).build()
                );
                log.info("Public read policy configured for bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO bucket initialization notice: {}. Ensure Docker MinIO container is running.", e.getMessage());
        }

        return client;
    }
}
