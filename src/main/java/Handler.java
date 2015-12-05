import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
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
            if (!element.ownText().isEmpty()) {
                return element;
            }
        }
        return elements.get(from);
    }

    private String getCoachBiography(Coach coach, Element element) {
        String details = "";
        if (coach.getDetailsPageUrl() != null && !coach.getDetailsPageUrl().isEmpty()) {
            Document detailsDoc = Main.connectTo(coach.getDetailsPageUrl()).get();
            for (Element el : detailsDoc.getElementsContainingOwnText(coach.getFullName())) {
                if (!el.ownText().equals(coach.getFullName())) {
                    details += el.ownText();
                }
            }
            if (details.isEmpty() || details.equalsIgnoreCase(coach.getFullName())) {

            }
        } else {
            //todo start from element
            //find info on the current page
        }

        return details;
    }

    public void run(Coach coach) {
        Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        final int[] startFrom = {0};
        int emailCounter = 0;
        int emailStartClassNameIterator = 0;
        String emailClassName = "";

        doc.getElementsContainingOwnText(coach.getFullName()).stream().filter(element -> element.ownText().equalsIgnoreCase(coach.getFullName())).forEach(element ->
                        startFrom[0] = doc.getAllElements().indexOf(element)
        );

        for (int i = startFrom[0]; i < doc.getAllElements().size(); i++) {
            Element element = doc.getAllElements().get(i);

            // name
            if (!coach.isCoachFound() && element.ownText().toLowerCase().contains(coach.getFirstName().toLowerCase())) {
                if (element.ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                    coach.setIsFound(true); //name and surname in the same column
                    coach.setDetailsPageUrl(element.attr("abs:href"));
                    coach.setBiography(getCoachBiography(coach, element));
                } else {
                    if (getAdjacentColumnElement(doc.getAllElements(), i + 1).ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                        coach.setIsFound(true); //name and surname in the adjacent columns
                        coach.setDetailsPageUrl(element.attr("abs:href"));
                        coach.setBiography(getCoachBiography(coach, element));
                    }
                }
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
                if (emailClassName.isEmpty()) {
                    emailClassName = element.className();
                    emailStartClassNameIterator = emailCounter;
                }
                if (element.className().equals(emailClassName) && emailStartClassNameIterator < emailCounter) { // new coach
                    break;
                }

                emailCounter++;
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