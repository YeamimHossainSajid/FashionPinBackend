package com.fashionpin.mediaservice.storage;

import com.fashionpin.mediaservice.port.StoragePort;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3ObjectStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3ObjectStorageAdapter.class);

    private final String endpoint;
    private final String publicEndpoint;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;
    private final String regionStr;
    private final boolean pathStyleAccess;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    public S3ObjectStorageAdapter(
            @Value("${storage.s3.endpoint:http://minio:9000}") String endpoint,
            @Value("${storage.s3.public-endpoint:http://194.163.166.16:9000}") String publicEndpoint,
            @Value("${storage.s3.bucket-name:fashionpin-media}") String bucketName,
            @Value("${storage.s3.access-key:minioadmin}") String accessKey,
            @Value("${storage.s3.secret-key:FashionPinS3SecureKey2026!}") String secretKey,
            @Value("${storage.s3.region:us-east-1}") String regionStr,
            @Value("${storage.s3.path-style-access:true}") boolean pathStyleAccess) {
        this.endpoint = endpoint;
        this.publicEndpoint = publicEndpoint;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.regionStr = regionStr;
        this.pathStyleAccess = pathStyleAccess;
    }

    @PostConstruct
    public void init() {
        try {
            Region region = Region.of(regionStr);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );

            S3Configuration s3Config = S3Configuration.builder()
                    .pathStyleAccessEnabled(pathStyleAccess)
                    .build();

            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(s3Config)
                    .build();

            this.s3Presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(publicEndpoint != null && !publicEndpoint.isBlank() ? publicEndpoint : endpoint))
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(s3Config)
                    .build();

            ensureBucketExists();
            log.info("Initialized S3 Object Storage Adapter connected to endpoint={} bucket={}", endpoint, bucketName);
        } catch (Exception e) {
            log.error("Failed to initialize S3 Object Storage Client: {}", e.getMessage(), e);
        }
    }

    private void ensureBucketExists() {
        if (s3Client == null) return;
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 Bucket '{}' already exists and is ready.", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("Creating S3 Bucket '{}'...", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Successfully created S3 Bucket '{}'.", bucketName);
        } catch (Exception e) {
            log.warn("Bucket check/creation encountered warning for '{}': {}", bucketName, e.getMessage());
        }
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, String contentType, long length) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, length));
            log.info("Uploaded object to S3 bucket={} key={} size={} bytes", bucketName, key, length);
            return generatePublicUrl(key);
        } catch (Exception e) {
            log.error("S3 upload failed for key={}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to upload object to S3 storage", e);
        }
    }

    @Override
    public byte[] downloadFile(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 download failed for key={}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to download object from S3 storage", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted object from S3 bucket={} key={}", bucketName, key);
        } catch (Exception e) {
            log.warn("Failed to delete S3 object key={}: {}", key, e.getMessage());
        }
    }

    @Override
    public String generatePreSignedUrl(String key, long expirationMinutes) {
        try {
            if (s3Presigner != null) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expirationMinutes))
                        .getObjectRequest(getObjectRequest)
                        .build();

                return s3Presigner.presignGetObject(presignRequest).url().toString();
            }
        } catch (Exception e) {
            log.warn("Pre-signed URL generation failed for key={}, returning public endpoint fallback: {}", key, e.getMessage());
        }
        return generatePublicUrl(key);
    }

    public String generatePublicUrl(String key) {
        String base = (publicEndpoint != null && !publicEndpoint.isBlank()) ? publicEndpoint : endpoint;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + bucketName + "/" + key;
    }
}
