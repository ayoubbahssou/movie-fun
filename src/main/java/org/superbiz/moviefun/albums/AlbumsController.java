package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    private BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, @Autowired BlobStore blobStore) {
        this.blobStore = blobStore;
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, albumId);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        System.out.println(albumId);
        System.out.println(String.valueOf(albumId));
        Optional<Blob> blobOptional = this.blobStore.get(String.valueOf(albumId));
        System.out.println(String.valueOf(albumId));
        if (blobOptional.isPresent()) {
            Blob b = blobOptional.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while (true) {
                if (!((nRead = b.inputStream.read(data, 0, data.length)) != -1)) break;
                baos.write(data, 0, nRead);
            }
            System.out.println(nRead);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(b.contentType));
            headers.setContentLength(baos.toByteArray().length);

            return new HttpEntity<>(baos.toByteArray(), headers);
        }
        throw new IOException();
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, Long id) throws IOException {

        Blob blob = new Blob(String.valueOf(id), uploadedFile.getInputStream(), uploadedFile.getContentType());
        this.blobStore.put(blob);
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) throws IOException {

        String coverFileName = format("covers/%d", albumId);
        this.blobStore.get(coverFileName);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException, IOException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}
