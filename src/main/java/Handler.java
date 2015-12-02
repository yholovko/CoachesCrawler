import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    public void run(Coach coach) {
        Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        int emailCounter = 0;

        for (int i = 0; i < doc.getAllElements().size(); i++) {
            Element element = doc.getAllElements().get(i);

            // name
            if (!coach.isFound() && element.ownText().toLowerCase().contains(coach.getFirstName().toLowerCase())) {
                if (element.ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                    coach.setIsFound(true); //name and surname in the same column
                    coach.setDetailsPageUrl(element.attr("href"));
                } else {
                    if (getAdjacentColumnElement(doc.getAllElements(), i + 1).ownText().toLowerCase().contains(coach.getLastName().toLowerCase())) {
                        coach.setIsFound(true); //name and surname in the adjacent columns
                        coach.setDetailsPageUrl(element.attr("href"));
                    }
                }
            }

            // email
            if (coach.isFound() && coach.getEmail() == null && emailCounter < 6) { // 5 (deep) next elements where we can find coach email
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
                if (!element.ownText().isEmpty()) {
                    emailCounter++;
                }
            }
        }

        System.out.println(coach.toString());
    }
}