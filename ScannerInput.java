import java.io.IOException;
import java.util.Scanner;
import java.io.IOException;

public class ScannerInput {
    public static void main(String[] args) throws IOException {
        String q1 = "\n What genre do you enjoy reading? [input a genre like fantasy or historical fiction " +
                "or say 'surprise me']";
        String q2 = "\n Are you looking for a popular books or recently released books [type: most read or new release]?";
        String q3 = "\n  What length of book do you prefer? Your options are: \n" +
                "Novella (<150 pages)\n" +
                "Short (150-300)\n" +
                "Mid-length (300-500)\n" +
                "Long (500+)\n" +
                "Surprise me\n";
        String q4 = "If you genre belongs to the Goodreads Choice Awards, are you interested" +
                "in seeing 3 more book recommendations of the top books from this year? [yes or no]";

        // Ask first question about what genre (keyword = genre / surprise me)
        Scanner scanner = new Scanner(System.in);
        System.out.println(q1);
        String keyword0 = scanner.nextLine();

        // ask second question: popular vs. newly released book
        System.out.println(q2);
        String keyword1 = scanner.nextLine();

        //ask third question: length of book
        System.out.println(q3);
        String keyword2 = scanner.nextLine();

        scanner.close();

        System.out.print("Our book recommendations are as follows: ");
        // answer question
        GoodreadsParser parser = new GoodreadsParser();

        String res = parser.findRecommendations(keyword0, keyword1, keyword2);
        System.out.println(res);

        Scanner scanner1 = new Scanner(System.in);
        System.out.println(q4);
        String keyword4 = scanner1.nextLine();
        if(keyword4.equalsIgnoreCase("yes")) {
            parser.findAwardBooks(keyword4);
        } else if (keyword4.equalsIgnoreCase("no")) {
            System.out.println("Thank you for using our book recommender! We hope you read" +
                    "one of our recommendations!");
        } else {
            System.out.println("That was an invalid input");
        }

    }
}
