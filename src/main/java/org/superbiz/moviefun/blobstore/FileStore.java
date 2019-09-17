package org.superbiz.moviefun.blobstore;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class FileStore implements BlobStore {
/*
    MinioClient minioClient;

    public FileStore(@Autowired MinioClient minioClient){
        this.minioClient = minioClient;
    }
*/
    @Override
    public void put(Blob blob) throws IOException {
        try {
            MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F",
                    "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
            minioClient.putObject("my-bucketname", blob.name, blob.inputStream, blob.contentType);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F",
                    "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
            Blob result = null;
            InputStream is = minioClient.getObject("my-bucketname", name);
            String contentType = minioClient.statObject("my-bucketname", name).contentType();
            result = new Blob(name, is, contentType);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void deleteAll() {
        // ...
        try {
            MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F",
                    "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
            minioClient.deleteBucketLifeCycle("my-bucketname");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
