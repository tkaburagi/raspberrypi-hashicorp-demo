package am.ik.demo.facebootifier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.function.Function;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.fillConvexPoly;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class FaceBootifier implements Function<String, byte[]> {

    private final Counter counter;
    private final String dir = "/Users/kabu/Desktop/";

    @Autowired
    SlackClient slackClient;

    public FaceBootifier(MeterRegistry meterRegistry) {
        this.counter = meterRegistry.counter("bootified.count");
    }

    @Override
    public byte[] apply(String s) {

        File file = new File(dir);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File f, String name)
            {
                return name.startsWith("face-");
            }
        };

        String f[] = file.list(filter);



        try {
//            VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();
//            byte[] decode = Base64.getDecoder().decode(vaultTransitUtil.decryptData(s));
            byte[] decode = Base64.getDecoder().decode(s);
            try (ByteArrayInputStream stream = new ByteArrayInputStream(decode)) {
                Path input = Files.createTempFile("input-", ".png");
                String original_filename = "face-" + RandomStringUtils.randomAlphabetic(5) + ".png";
                String filename = "bootified-face-" + RandomStringUtils.randomAlphabetic(5) + ".png";
                Path output = Files.createFile(Paths.get(dir + filename));
                Path output_original = Files.createFile(Paths.get(dir + original_filename));

                input.toFile().deleteOnExit();
                output.toFile().deleteOnExit();
                Files.copy(stream, input, StandardCopyOption.REPLACE_EXISTING);
                FaceDetector faceDetector = new FaceDetector(counter);

                Mat source = imread(input.toAbsolutePath().toString());
                Mat source_original = imread(input.toAbsolutePath().toString());

                faceDetector.detectFaces(source, FaceBootifier::bootify);
                imwrite(output.toFile().getAbsolutePath(), source);
                imwrite(output_original.toFile().getAbsolutePath(), source_original);

                System.out.println(output.toFile().getAbsolutePath());
                byte[] bootified = Base64.getEncoder()
                        .encode(Files.readAllBytes(output));
                Files.deleteIfExists(input);

                slackClient.sendMessage(Files.readAllBytes(output), filename);

                return bootified;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void bootify(Mat source, Rect r) {
        int x = r.x(), y = r.y(), w = r.width();
        int a = (int) (w / 2 * Math.cos(Math.PI / 3));
        int h = (int) (w * Math.sin(Math.PI / 3));

        Point points = new Point(6);
        int offset = (r.height() - h) / 2;
        points.position(0).x(x + a).y(y + offset);
        points.position(1).x(x + w - a).y(y + offset);
        points.position(2).x(x + w).y(y + h / 2 + offset);
        points.position(3).x(x + w - a).y(y + h + offset);
        points.position(4).x(x + a).y(y + h + offset);
        points.position(5).x(x).y(y + h / 2 + offset);

        fillConvexPoly(source, points.position(0), 6, Scalar.GREEN, CV_AA, 0);

        circle(source, new Point(x + w / 2, y + w / 2), w / 4,
                Scalar.WHITE, -1, CV_AA, 0);
        circle(source, new Point(x + w / 2, y + w / 2), w / 6,
                Scalar.GREEN, -1, CV_AA, 0);

        rectangle(source, new Point(x + w / 2 - w / 12, y + w / 5),
                new Point(x + w / 2 + w / 12, y + w / 2),
                Scalar.GREEN, -1, CV_AA, 0);
        rectangle(source, new Point(x + w / 2 - w / 20, y + w / 5),
                new Point(x + w / 2 + w / 20, y + w / 2),
                Scalar.WHITE, -1, CV_AA, 0);
    }
}