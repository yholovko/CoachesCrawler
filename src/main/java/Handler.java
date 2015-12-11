import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private Document doc;

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
            Document detailsDoc = Main.connectTo(coach.getDetailsPageUrl()).get();
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

                if (bio.trim().replace(",", "").equalsIgnoreCase(coach.getFullName()) || bio.trim().replace(",", "").equalsIgnoreCase(coach.getFullNameInverse())){
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
        }

        try {
            Database.insertInformationAboutCoach(coach);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(coach.toString());
    }
}