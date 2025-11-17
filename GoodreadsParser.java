import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Random;

import java.io.IOException;
import java.util.*;

public class GoodreadsParser {
    private final String baseURL;
    private Document currentDoc;
    private HashMap<String, Integer[]> pageMap;
    public String currGenre = null;

    // Constructor
    public GoodreadsParser() {
        this.baseURL = "https://www.goodreads.com";

        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.pageMap = new HashMap<>();
        pageMap.put("novella", new Integer[]{0, 149});
        pageMap.put("short", new Integer[]{150, 299});
        pageMap.put("mid-length", new Integer[]{300, 499});
        pageMap.put("long", new Integer[]{500});
        this.currGenre = null;

    }

    //return the url of the genre's page
    public String findGenres(String keyword) {
        //ensure baseURL is a valid link
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String genreURL = null;
        try {
            //navigate to genre list page from main page
            Element genreTag = currentDoc.selectFirst("h2:contains(Search and browse books)")
                    .parent();
            Elements genreLinks = genreTag.select("div > div > a");
            String genreInput;
            //check to ensure elements are not null
            if (genreTag != null && genreLinks != null) {
                //keyword is the genre or surprise me
                //if the user chose "surprise me", randomly generate a number and find the
                // associated genre page
                Random rand = new Random();
                int randomGenreNum = rand.nextInt(29);
                if (keyword.equals("surprise me")) {
                    int linkCounter = 0;
                    for (Element genre : genreLinks) {
                        //if the randomly-generated number of genres to iterate through has
                        // been reached,
                        //output the corresponding link

                        if (linkCounter == randomGenreNum) {
                            currGenre = genre.text();
                            genreURL = genre.absUrl("href");
                            return genreURL;
                        } else {
                            linkCounter++;
                        }
                    }
                } else {
                    // otherwise search for genre on first page, if not found go to next page and
                    // repeat
                    // clean keyword (ex. "Historical Fiction" becomes "historical-fiction")
                    keyword = keyword.toLowerCase();
                    keyword = keyword.replace(" ", "-");
                    genreInput = keyword;
                    currGenre = genreInput;

                    Document genreDoc;
                    //navigate to the genre feature page
                    for (Element genre : genreLinks) {
                        if (genre.text().contains("More genres")) {
                            String genreURLTemp = genre.absUrl("href");
                            Document genreDocTemp = Jsoup.connect(genreURLTemp).get();

                            //navigate to the genre list page
                            Element genreTagTwo = genreDocTemp.selectFirst("h2:contains(Browse)")
                                    .parent().nextElementSibling();
                            if (genreTagTwo != null) {
                                Elements genreLinksTwo = genreTagTwo.
                                        select("div > div > a");
                                for (Element genreTwo : genreLinksTwo) {
                                    if (genreTwo.text().contains("More genres")) {
                                        String genreURLTwo = genreTwo.absUrl("href");
                                        genreDoc = Jsoup.connect(genreURLTwo).get();
                                        //create boolean variable to keep track of whether
                                        // specified genre is found
                                        boolean found = false;
                                        Element nextEl = genreDoc.selectFirst("a.next_page");

                                        //iterate through the pages of genres
                                        while (!found && nextEl != null) {
                                            nextEl = genreDoc.selectFirst("a.next_page");
                                            Element genreContainer = genreDoc.
                                                    selectFirst("div.leftContainer");
                                            Elements genres = genreContainer.
                                                    select("div > div > div > a");

                                            for (Element g : genres) {
                                                //for the current genre, check if it's the one
                                                // we're looking for
                                                String genreName = g.text();
                                                if (genreName.equalsIgnoreCase(genreInput)) {
                                                    genreURL = g.absUrl("href");
                                                    found = true;
                                                }
                                            }

                                            if (!found) {
                                                // go to next page if the genre is not on this page
                                                String nextPageURL = baseURL +
                                                        nextEl.attr("href");
                                                genreDoc = Jsoup.connect(nextPageURL).get();

                                            }
                                        }

                                        if (found) {
                                            return genreURL;
                                        } else {
                                            return "Sorry, this genre doesn't exist.";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ("Sorry, this genre doesn't exist");
    }


    public String findRecommendations(String keyword0, String keyword1, String keyword2) {
        // keyword 0 = genre
        // keyword 1 = new release or most read
        // keyword 2 = length of book

        String res = findGenres(keyword0);
        if (res.equalsIgnoreCase("Sorry, this genre doesn't exist")) {
            return res;
        }

        try {
            String newURL = res;
            this.currentDoc = Jsoup.connect(newURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // hashmap 1 structure
        // key = title of book
        // value = rating of book

        // hashmap 2 structure
        // key = title of book
        // value = list of strings
        // (string 1 = author, string 2 = # pages, string 3 = rating,
        // string 4 = publication date, string 5 = summary)

        HashMap<String, Double> ratingMap = new HashMap<>();
        HashMap<String, List<String>> infoMap = new HashMap<>();

        // iterate through books in the appropriate section and add to hashmap
        if (keyword1.equalsIgnoreCase("new release")) {
            Element newReleaseHeader = this.currentDoc.
                    selectFirst("h2:contains(New Releases Tagged)");
            if (newReleaseHeader == null) {
                return "Sorry, this genre does not have new releases. Please select a " +
                        "different genre.";
            }
            Element newReleaseDiv = newReleaseHeader.parent().nextElementSibling();
            Elements newReleases = newReleaseDiv.select("div > div > div > div > a");

            // iterate through new releases
            for (Element n : newReleases) {

                if (n.text().contains("More new releases")) {
                    break;
                }
                // navigate to book page
                String bookURL = n.attr("href");


                try {
                    String newURL = baseURL + bookURL;

                    this.currentDoc = Jsoup.connect(newURL).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // add book info to each hashmap
                // title
                Element titleEl = this.currentDoc.selectFirst("h1[data-testid='bookTitle']");
                String title = titleEl.text();

                // page length
                // only add if page length within range
                Element pageInfo1 = this.currentDoc.selectFirst("p[data-testid='pagesFormat']");
                if (pageInfo1 == null) {
                    continue;
                }
                String pageInfo = pageInfo1.text();
                String[] pages = pageInfo.split(",");
                String[] pagesSplit = pages[0].split(" ");

                try {
                    Integer numPages = Integer.parseInt(pagesSplit[0]);
                    boolean validLen = false;

                    if (keyword2.equalsIgnoreCase("surprise me")) {
                        validLen = true;
                    } else {
                        // check if number of pages is within specified range

                        Integer[] range = pageMap.get(keyword2.toLowerCase());

                        if (range.length == 1) {
                            // long book
                            if (numPages > range[0]) {
                                validLen = true;
                            }
                        } else {
                            Integer lo = range[0];
                            Integer hi = range[1];

                            if (numPages >= lo && numPages <= hi) {
                                validLen = true;
                            }
                        }
                    }

                    if (validLen) {
                        // get other info about book to be displayed

                        Element authorSection = this.currentDoc.
                                selectFirst("div.BookPageMetadataSection__contributor");
                        String author = "";
                        if (authorSection == null) {
                            author = "Unknown author";
                        } else {
                            Elements authors = authorSection.
                                    select("h3 > div > span > a > " +
                                            "span.ContributorLink__name");

                            StringBuilder authorSB = new StringBuilder();
                            for (int i = 0; i < authors.size(); i++) {
                                Element a = authors.get(i);
                                authorSB.append(a.text());
                                if (i != authors.size() - 1) {
                                    authorSB.append(", ");
                                }
                            }

                            author = authorSB.toString();
                        }

                        // publication date
                        Element pubDate = this.currentDoc.
                                selectFirst("p[data-testid='publicationInfo']");
                        String publicationDate = "";
                        if (pubDate == null) {
                            publicationDate = "Publication Date Unknown";
                        } else {
                            publicationDate = pubDate.text();
                        }


                        // summary
                        Element desc = this.currentDoc.
                                selectFirst("div[data-testid='description']");
                        String summary = "";
                        if (desc == null) {
                            summary = "Description not available.";
                        } else {
                            summary = desc.selectFirst("div > div > div > div > span").text();
                        }

                        Element ratingDoub = this.currentDoc.
                                selectFirst("div.RatingStatistics__rating");
                        if (ratingDoub == null) {
                            continue;
                        }

                        Double rating = Double.parseDouble(ratingDoub.text());

                        ratingMap.put(title, rating);

                        List<String> info = new ArrayList<>();
                        info.add(author);
                        info.add(String.valueOf(numPages));
                        info.add(String.valueOf(rating));
                        info.add(publicationDate);
                        info.add(summary);

                        infoMap.put(title, info);
                    }
                } catch (NumberFormatException e) {
                    // for books that don't have page number listed
                }

            }
        } else {
            Element mostReadTag = this.currentDoc.selectFirst("a:contains(Most Read This Week)");
            if (mostReadTag == null) {
                return "Sorry this genre does not have any most read books. Please select a " +
                        "different genre.";
            }
            Element mostReadSection = mostReadTag.parent().parent().nextElementSibling();

            Elements mostReadBooks = mostReadSection.select("div > div > div > div > a");

            for (Element m : mostReadBooks) {
                String bookURL = m.attr("href");

                if (m.text().contains("More most read this week")) {
                    break;
                }

                try {
                    String newURL = baseURL + bookURL;
                    this.currentDoc = Jsoup.connect(newURL).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Element titleEl = this.currentDoc.selectFirst("h1[data-testid='bookTitle']");
                String title = titleEl.text();

                // page length
                // only add if page length within range
                Element pageInfo1 = this.currentDoc.selectFirst("p[data-testid='pagesFormat']");
                if (pageInfo1 == null) {
                    continue;
                }
                String pageInfo = pageInfo1.text();
                String[] pages = pageInfo.split(",");
                String[] pagesSplit = pages[0].split(" ");

                try {
                    Integer numPages = Integer.parseInt(pagesSplit[0]);

                    boolean validLen = false;

                    if (keyword2.equalsIgnoreCase("surprise me")) {
                        validLen = true;
                    } else {
                        // check if number of pages is within specified range

                        Integer[] range = pageMap.get(keyword2.toLowerCase());

                        if (range.length == 1) {
                            // long book
                            if (numPages > range[0]) {
                                validLen = true;
                            }
                        } else {
                            Integer lo = range[0];
                            Integer hi = range[1];

                            if (numPages >= lo && numPages <= hi) {
                                validLen = true;
                            }
                        }
                    }

                    if (validLen) {
                        // get other info about book to be displayed

                        Element authorSection = this.currentDoc.
                                selectFirst("div.BookPageMetadataSection__contributor");
                        String author = "";
                        if (authorSection == null) {
                            author = "Author Unknown";
                        } else {
                            Elements authors = authorSection.select("h3 > div > " +
                                    "span > a > span.ContributorLink__name");

                            StringBuilder authorSB = new StringBuilder();
                            for (int i = 0; i < authors.size(); i++) {
                                Element a = authors.get(i);
                                authorSB.append(a.text());
                                if (i != authors.size() - 1) {
                                    authorSB.append(", ");
                                }
                            }


                            author = authorSB.toString();
                        }


                        // publication date
                        Element pubDate = this.currentDoc.
                                selectFirst("p[data-testid='publicationInfo']");
                        String publicationDate = "";
                        if (pubDate == null) {
                            publicationDate = "Publication Date Unknown";
                        } else {
                            publicationDate = pubDate.text();
                        }


                        // summary
                        Element desc = this.currentDoc.
                                selectFirst("div[data-testid='description']");
                        String summary = "";
                        if (desc == null) {
                            summary = "Description not available.";
                        } else {
                            summary = desc.selectFirst("div > div > div > div > span").text();
                        }

                        Element ratingDoub = this.currentDoc.
                                selectFirst("div.RatingStatistics__rating");
                        if (ratingDoub == null) {
                            continue;
                        }

                        Double rating = Double.parseDouble(ratingDoub.text());
                        ratingMap.put(title, rating);

                        List<String> info = new ArrayList<>();
                        info.add(author);
                        info.add(String.valueOf(numPages));
                        info.add(String.valueOf(rating));
                        info.add(publicationDate);
                        info.add(summary);

                        infoMap.put(title, info);
                    }
                } catch (NumberFormatException e){
                    // this book will be ignored since it doesn't have a page number specified
                }
            }
        }

        // sort rating map in descending order
        if (ratingMap.size() == 0) {
            return "Sorry, there are no books that match your criteria in the " + this.currGenre +
                    " genre. Please try again.";
        }


        List<Map.Entry<String, Double>> ratingList = new LinkedList<Map.Entry<String, Double>>
                (ratingMap.entrySet());

        Collections.sort(ratingList, new Comparator<>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        StringBuilder sb = new StringBuilder();
        int lim = Math.min(3, ratingList.size());
        for (int i = 0; i < lim; i++) {
            sb.append("\n");
            sb.append("\n");
            Map.Entry<String, Double> e = ratingList.get(i);
            sb.append("Title: ");
            sb.append(e.getKey());
            sb.append("\n");
            List<String> info = infoMap.get(e.getKey());
            sb.append("Author: ");
            sb.append(info.get(0));
            sb.append("\n");
            sb.append("Length: ");
            sb.append(info.get(1));
            sb.append(" pages");
            sb.append("\n");
            sb.append("Rating: ");
            sb.append(info.get(2));
            sb.append(" out of 5 stars");
            sb.append("\n");
            sb.append("Publication Date: ");
            sb.append(info.get(3));
            sb.append("\n");
            sb.append("Summary: ");
            sb.append(info.get(4));
            sb.append("\n");
        }

        return sb.toString();
    }

    public String findAwardBooks(String keyword) throws IOException {
        HashMap<String, List<String>> infoMap = new HashMap<>();
        // keyword = genre
        // if genre is on the list of goodreads choice awards genres output recommendations
        // from that

        // reset the currentDoc to the homepage of Goodreads
        this.currentDoc = Jsoup.connect(this.baseURL).get();
        Elements genres = currentDoc.
                select("h2:contains(Goodreads Choice Awards: The Best Books 2023)");

        keyword = keyword.toLowerCase();

        Element genreWanted = null;

        for (Element genre : genres) {
            Elements aTags = genre.siblingElements().select("a");
            for (Element a : aTags) {
                String convertedElementA = a.text().toLowerCase();
                // check to see if the genre input matches a category
                if(convertedElementA.contains(keyword)) {
                    genreWanted = a;
                    break;
                }
            }
        }

        if (genreWanted == null) {
            return "\n This genre does not exist in the Goodreads Best Choice Awards." +
                    " Please try with a different genre.";
        }

        // navigate to specific Best Genre page
        String bestGenreURL = genreWanted.absUrl("href");
        Document bestGenreDoc = Jsoup.connect(bestGenreURL).get();

        Element x = bestGenreDoc.selectFirst("div.pollContents");
        Elements bookTags = x.select("div > div > div > a.pollAnswer__bookLink");
        // Visit the top 3 books' pages
        for (int j = 0; j <= 2; j++) {
            Element b = bookTags.get(j);
            String bestBookURL = baseURL + b.attr("href");
            try {
                Document bestBookDoc = Jsoup.connect(bestBookURL).get();

            // get book summary info for top 3

            // get title
            String title = bestBookDoc.select("h1[data-testid='bookTitle']").text();

            // get author
            Element authorSection = bestBookDoc.
                    selectFirst("div.BookPageMetadataSection__contributor");
            Elements authors = authorSection.
                    select("h3 > div > span > a > span.ContributorLink__name");

            String author = "no author";
            if (authors == null) {
               continue;
            } else {
                StringBuilder authorSB = new StringBuilder();
                for (int i = 0; i < authors.size(); i++) {
                    Element a = authors.get(i);
                    authorSB.append(a.text());
                    if (i != authors.size() - 1) {
                        authorSB.append(", ");
                    }
                }

                author = authorSB.toString();
            }


            // publication date
            String publicationDate = bestBookDoc.
                    selectFirst("p[data-testid='publicationInfo']").text();
            if (publicationDate == null) {
                publicationDate = "no publication date available";
            }

            // summary
            Element desc = bestBookDoc.selectFirst("div[data-testid='description']");
            String summary = desc.selectFirst("div > div > div > div > span").text();
            if (summary == null) {
                summary = "no available summary";
            }


            List<String> info = new ArrayList<>();
            info.add(author);
            info.add(publicationDate);
            info.add(summary);

                infoMap.put(title, info);
            } catch (IOException e) {
                // if URL cannot be found
            }



        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : infoMap.entrySet()) {
            sb.append("\n");
            List<String> info = e.getValue();
            sb.append("Title: ");
            sb.append(e.getKey());
            sb.append("\n");
            sb.append("Author: ");
            sb.append(info.get(0));
            sb.append("\n");
            sb.append("Publication Date: ");
            sb.append(info.get(1));
            sb.append("\n");
            sb.append("Summary: ");
            sb.append(info.get(2));
            sb.append("\n");
        }

        return sb.toString();

    }


}
