package com.elance.coaches;

import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private Document doc;
    private Document detailsDoc = null;

    public Handler(Document doc) {
        this.doc = doc;
    }

    private Element getAdjacentColumnElement(Elements elements, int from) {
        for (int i = from; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (!element.ownText().trim().isEmpty()) {
                return element;
            }
        }
        return elements.get(from);
    }

    private String getCoachBiography(Coach coach, Element startFromElement) {
        List<String> possibleBio = new ArrayList<>();

        if (coach.getDetailsPageUrl() != null && !coach.getDetailsPageUrl().isEmpty()) {
            if (detailsDoc == null) {
                detailsDoc = Main.connectTo(coach.getDetailsPageUrl()).get();
            }
            Elements elements = detailsDoc.getElementsContainingOwnText(coach.getFullName());

            String details1 = "";
            for (Element el : elements) {
                if (!el.ownText().trim().equals(coach.getFullName())) {
                    details1 += el.ownText().trim();
                }
            }
            possibleBio.add(details1.trim());

            String details2 = "";

            for (Element el : elements) {
                for (Element child : el.parent().children()) {
                    if (!child.ownText().isEmpty() &&
                            !details2.startsWith(child.ownText()) &&
                            !child.ownText().trim().equalsIgnoreCase("return to staff") &&
                            !child.ownText().trim().equals(coach.getFullName())) {
                        details2 += child.ownText().trim() + " ";
                    }
                }
            }
            possibleBio.add(details2.trim());

            String details3 = "";
            for (Element el : elements) { // trying to extract text between tags
                for (TextNode textNode : el.parent().textNodes()) {
                    if (!textNode.getWholeText().trim().isEmpty() &&
                            !textNode.getWholeText().trim().equalsIgnoreCase("return to staff") &&
                            !textNode.getWholeText().trim().equals(coach.getFullName())) {
                        details3 += textNode.getWholeText().trim() + " ";
                    }
                }
            }
            possibleBio.add(details3.trim());
//            String details4 = "";
//            for (Element el : elements) { // trying to extract text between tags
//                for (Node childNode : el.parent().childNodes()) {
//                    if (childNode instanceof Element) {
//                        String s = ((Element) childNode).ownText().trim();
//                        if (!s.isEmpty() && !s.equalsIgnoreCase("return to staff") && !s.equals(coach.getFullName())) {
//                            details4 += s + " ";
//                        }
//                    }
//                    if (childNode instanceof TextNode) {
//                        String s = ((TextNode) childNode).text().trim();
//                        if (!s.isEmpty() && !s.equalsIgnoreCase("return to staff") && !s.equals(coach.getFullName())) {
//                            details4 += s + " ";
//                        }
//                    }
//                    for (Node childChildNode : childNode.childNodes()) {
//                        if (childChildNode instanceof Element) {
//                            String s = ((Element) childChildNode).ownText().trim();
//                            if (!s.isEmpty() && !s.equalsIgnoreCase("return to staff") && !s.equals(coach.getFullName())) {
//                                details4 += s + " ";
//                            }
//                        }
//                        if (childChildNode instanceof TextNode) {
//                            String s = ((TextNode) childChildNode).text().trim();
//                            if (!s.isEmpty() && !s.equalsIgnoreCase("return to staff") && !s.equals(coach.getFullName())) {
//                                details4 += s + " ";
//                            }
//                        }
//                    }
//                }
//            }
//            possibleBio.add(details4.trim());

            String details5 = "";
            for (Element paragraph : detailsDoc.getElementsByTag("p")) {
                if (!paragraph.ownText().trim().isEmpty() && !paragraph.ownText().trim().startsWith("Copyright")) {
                    details5 += paragraph.ownText() + " ";
                }
            }
            possibleBio.add(details5.trim());
        } else {
            String currentElementText = startFromElement.ownText().replace(",", "").trim();
            if (!currentElementText.equalsIgnoreCase(coach.getFullName()) && !currentElementText.equalsIgnoreCase(coach.getFullNameInverse()) && currentElementText.length() != coach.getFullName().length()) {
                possibleBio.add(startFromElement.ownText().trim());
            }

            int startFrom = doc.getAllElements().indexOf(startFromElement);
            String className = doc.getAllElements().get(startFrom).className();

            if (className.isEmpty()) {
                className = doc.getAllElements().get(startFrom).parent().className();
            }

            if (!className.isEmpty()) {
                String bio = "";
                for (int i = startFrom; i < doc.getAllElements().size(); i++) {
                    Element element = doc.getAllElements().get(i);

                    String elemText = element.ownText().trim();
                    if (!elemText.isEmpty() && !elemText.equalsIgnoreCase(coach.getFullName()) && !elemText.equalsIgnoreCase(coach.getFullNameInverse())) {
                        bio += element.ownText().trim() + " ";
                    }

                    if (element.className().equals(className)) {
                        break;
                    }
                }

                if (bio.trim().replace(",", "").equalsIgnoreCase(coach.getFullName()) || bio.trim().replace(",", "").equalsIgnoreCase(coach.getFullNameInverse())) {
                    bio = "";
                }

                possibleBio.add(bio);
            }

            String bio = getAdjacentColumnElement(doc.getAllElements(), startFrom + 1).ownText().trim().replace(" ", "");
            if (bio.equalsIgnoreCase(coach.getFirstName()) || bio.equalsIgnoreCase(coach.getLastName()) || bio.isEmpty()) {
                bio = getAdjacentColumnElement(doc.getAllElements(), startFrom + 2).ownText().trim().replace(" ", "");
            }
            possibleBio.add(bio);

        }

        return (possibleBio.size() != 0) ? Collections.max(possibleBio, (o1, o2) -> {
            if (o1.length() > o2.length()) {
                return 1;
            } else if (o1.length() < o2.length()) {
                return -1;
            } else {
                return 0;
            }
        }) : "";
    }

    private Pair<byte[], String> downloadImage(Element image) {
        final String IMAGE_PATTERN = "\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP)";
        Pattern pattern = Pattern.compile(IMAGE_PATTERN);

        String src = image.absUrl("src");
        String extension = ".jpg";

        Matcher matcher = pattern.matcher(src);
        if (matcher.find()) {
            extension = src.substring(matcher.start(), matcher.end());
        }

        try {
            byte[] resultImageResponse = Jsoup.connect(src)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .execute().bodyAsBytes();

            return new Pair<>(resultImageResponse, extension);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isPortrait(Element image) {
        String url = image.absUrl("src");

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .execute().bodyAsBytes()));
            if (img != null) {
                int width = img.getWidth();
                int height = img.getHeight();

                if (width < height)
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Pair<byte[], String> bestImage(Coach coach, Elements images) {
        for (Element image : images) {
            if (image.attr("alt").equalsIgnoreCase(coach.getFullName()) || image.attr("alt").equalsIgnoreCase(coach.getFullNameInverse()) ||
                    (image.attr("alt").toLowerCase().contains(coach.getFirstName().toLowerCase()) && image.attr("alt").toLowerCase().contains(coach.getLastName().toLowerCase()))) {
                if (isPortrait(image)) {
                    return downloadImage(image);
                }
            }
        }

        for (Element image : images) {
            if (image.attr("alt").toLowerCase().contains(coach.getFirstName().toLowerCase()) || image.attr("alt").toLowerCase().contains(coach.getLastName().toLowerCase())) {
                if (isPortrait(image)) {
                    return downloadImage(image);
                }
            }
        }

        for (Element image : images) {
            if (image.absUrl("src").toLowerCase().contains(coach.getFirstName().toLowerCase()) || image.absUrl("src").toLowerCase().contains(coach.getLastName().toLowerCase())) {
                if (isPortrait(image)) {
                    return downloadImage(image);
                }
            }
        }

        Pair<byte[], String> resultPortraitImage = getPortraitImage(detailsDoc.getElementsByTag("img"));
        if (resultPortraitImage != null) {
            return resultPortraitImage;
        }
        return null;
    }

    private Pair<byte[], String> getPortraitImage(Elements images) {
        class PortraitImage {
            public Element image;
            public int width;
            public int height;

            public PortraitImage(Element image, int width, int height) {
                this.image = image;
                this.width = width;
                this.height = height;
            }

            public int getResolutionSize() {
                return width * height;
            }
        }

        List<PortraitImage> possiblePortraitImages = new ArrayList<>();

        for (Element image : images) {
            String url = image.absUrl("src");

            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .execute().bodyAsBytes()));
                if (img != null) {
                    int width = img.getWidth();
                    int height = img.getHeight();

                    if (width < height)
                        possiblePortraitImages.add(new PortraitImage(image, width, height));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (possiblePortraitImages.size() != 0) ? downloadImage(Collections.max(possiblePortraitImages, (o1, o2) -> {
            if (o1.getResolutionSize() > o2.getResolutionSize()) {
                return 1;
            } else if (o1.getResolutionSize() < o2.getResolutionSize()) {
                return -1;
            } else {
                return 0;
            }
        }).image) : null;
    }

    public Pair<byte[], String> getImage(Coach coach) {
        if (coach.getDetailsPageUrl() != null && !coach.getDetailsPageUrl().isEmpty()) {
            if (detailsDoc == null) {
                detailsDoc = Main.connectTo(coach.getDetailsPageUrl()).get();
            }

            for (Element el : detailsDoc.getElementsContainingOwnText(coach.getFullName())) { // possible matches
                Elements images = el.parent().getElementsByTag("img");
                if (images.size() == 0) continue;

                Pair<byte[], String> result = bestImage(coach, images);
                if (result != null) {
                    return result;
                }
            }

            for (Element hardDeepImage : detailsDoc.getElementsContainingOwnText(coach.getFullName()).parents()) { //all parents
                Elements images = hardDeepImage.getElementsByTag("img");
                if (images.size() == 0) continue;

                Pair<byte[], String> result = bestImage(coach, images);
                if (result != null) {
                    return result;
                }
            }

            Pair<byte[], String> resultPortraitImage = getPortraitImage(detailsDoc.getElementsByTag("img"));
            if (resultPortraitImage != null) {
                return resultPortraitImage;
            }
        } else {
            //todo on the same page
        }
        return null;
    }

    public void run(Coach coach) {
        Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        final int[] startFrom = {0};
        int emailCounter = 0;
        int emailStartClassNameIterator = 0;
        String emailClassName = "";
        Element coachNameElement = null;

        doc.getElementsContainingOwnText(coach.getFullName()).stream().filter(element -> element.ownText().equalsIgnoreCase(coach.getFullName())).forEach(element ->
                        startFrom[0] = doc.getAllElements().indexOf(element)
        );

        for (int i = startFrom[0]; i < doc.getAllElements().size(); i++) {
            Element element = doc.getAllElements().get(i);

            // name
            if (!coach.isCoachFound() && element.ownText().toLowerCase().contains(coach.getFirstName().toLowerCase())) {
                if (element.ownText().toLowerCase().equalsIgnoreCase(coach.getFullName().toLowerCase())) {
                    coach.setIsFound(true); //name and surname in the same column and equals
                    coach.setDetailsPageUrl(element.attr("abs:href"));
                } else if (element.ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                    coach.setIsFound(true); //name and surname in the same column
                    coach.setDetailsPageUrl(element.attr("abs:href"));
                } else {
                    if (getAdjacentColumnElement(doc.getAllElements(), i + 1).ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                        coach.setIsFound(true); //name and surname in the adjacent columns
                        coach.setDetailsPageUrl(element.attr("abs:href"));
                    }
                }
                coachNameElement = (coach.isCoachFound()) ? element : null;
            }

            // email
            if (coach.isCoachFound() && coach.getEmail() == null && emailCounter < 30) { // (deep) next elements where we can find coach email
                Matcher matcher = emailPattern.matcher(element.ownText());
                if (matcher.matches()) {
                    coach.setEmail(element.ownText());
                    break;
                }
                if (element.ownText().toLowerCase().contains("mail")) {
                    matcher = emailPattern.matcher(element.attr("href").replaceAll("mailto:", ""));
                    if (matcher.matches()) {
                        coach.setEmail(element.attr("href").replaceAll("mailto:", ""));
                        break;
                    }
                }
                for (String s : element.ownText().split(" ")) {
                    s = s.toLowerCase().replaceAll("email", "").replaceAll("e-mail", "").replaceAll(":", "").replaceAll("[^\\x00-\\x7F]", "").trim();
                    matcher = emailPattern.matcher(s);
                    if (matcher.matches()) {
                        coach.setEmail(s);
                        break;
                    }
                }
                if (emailClassName.isEmpty() && !element.ownText().isEmpty()) {
                    emailClassName = element.className();
                    emailStartClassNameIterator = emailCounter;
                }
                if (emailClassName.isEmpty() && emailCounter == 7) {
                    break;
                }
                if (element.className().equals(emailClassName) && emailStartClassNameIterator < emailCounter && !emailClassName.isEmpty()) { // new coach
                    break;
                }

                emailCounter++;
            }
        }

        // biography
        if (coach.isCoachFound()) {
            coach.setBiography(getCoachBiography(coach, coachNameElement));
            Pair<byte[], String> image = getImage(coach);
            if (image != null) {
                coach.setImage(image.getKey());
                coach.setImageExtension(image.getValue());
            }
        }

        try {
            Database.insertInformationAboutCoach(coach);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(coach.toString());
    }
}