package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client client;
    private String bucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.client = s3Client;
        this.bucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType(blob.contentType);
        client.putObject(this.bucket,blob.name,blob.inputStream,omd);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        String contentType = client.getObjectMetadata(this.bucket,name).getContentType();
        System.out.println(contentType);
        System.out.println(name);
        return Optional.of(new Blob(name, client.getObject(this.bucket, name).getObjectContent(), contentType));

    }

    @Override
    public void deleteAll() {
        client.deleteBucket(this.bucket);
    }
}
