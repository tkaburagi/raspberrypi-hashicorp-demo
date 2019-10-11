package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
public class DemoApplicationController {


    private final RestTemplate restTemplate;

    public DemoApplicationController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Autowired
    VaultTemplate vaultTemplate;

    @RequestMapping("/api/v1/encrypt-pic")
    @Scheduled(initialDelay = 30000, fixedDelay = 10000)
    public String transitter() throws Exception {
        File f = new File("/home/pi/pics/face.jpg");
        BufferedImage image = ImageIO.read(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        image.flush();

        ImageIO.write(image, "jpg", bos);
        bos.flush();
        bos.close();
        byte[] bImage = baos.toByteArray();

        byte[] encoded = Base64.getEncoder().encode(bImage);
        String base64Image = new String(encoded);

        VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();

        FileWriter fw = new FileWriter("/home/pi/pics/encrypted-face.jpg",false);
        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
        pw.println(vaultTransitUtil.encryptData(base64Image));
        pw.close();

        return vaultTransitUtil.encryptData(base64Image);
    }

    @RequestMapping("/api/v1/upstream-pic")
    @Scheduled(initialDelay = 30000, fixedDelay = 10000)
    public String putToBootifier() throws Exception{

        Path file = Paths.get("/home/pi/pics/encrypted-face.jpg");

        String ctext = Files.readString(file);

        URI url = new URI("http://192.168.3.209:8080/bootifier");

        HttpEntity<String> request = new HttpEntity<>(ctext);

        System.out.println(ctext);

        ResponseEntity<String> fileUpload = restTemplate.postForEntity(url, request, String.class);

        Files.deleteIfExists(file);

        return fileUpload.getBody();
    }
}
