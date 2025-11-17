# goodreads-recommender
An application to recommend books depending on user preferences (genre, book length, new release, etc). 

Overview of Project:
This is a book recommender that uses Goodreads’ as the main source of information. It asks the reader to
provide preferences about genre, page length/type of book, and popular vs. new releases in order to provide a tailored
set of recommendations.

As a secondary feature, if the genre that the user input is also one of the 15 genres that are part of Goodreads’
“Best Books 2023” awards, the program also asks if the user wants to get a recommendation from that as well.

Class Concepts Included:
Document Search (aka Information Retrieval)
Advanced topics related to the class (recommendations)

Addressing TA Feedback:
1) Check terms of service to make sure the site is ok to scrap: We used robots.txt to check what parts of Goodreads we
are able to scrap and changed our project to give recommendations based on genres rather than user reviews as a result.
2) Determine similarity: based on genre, page length, and popularity vs. new release
3) Splitting up the work: Each of us took on one of the main methods mentioned below (3 of them), and while one person
did the GUI, the other two did the User Manual.

Methods Explained:
findGenres()
Parameter(s): String of user-inputted genre or “surprise me”
Output: String of the genre’s URL
Description: This function navigates from Goodreads home page to the genre page of the user-inputted genre, or the
genre of a randomly chosen genre if the user asks to be surprised. It expects the input to be cleaned according to
how goodreads labels genres (ex. If the user inputs “Historical Fiction”, the input should be “historical-fiction”).

findRecommendations()
Parameter(s): 3 strings, the user-inputted genre or “surprise me”; “new release” or “most read”; and page length preference
Output: String with up to 3 book recommendations
Description: This function uses findGenres() as a helper method to navigate to the user’s chosen genre (or a random
one for ‘surprise me’). It then uses the user parameters of the type of book (popular v. new release) and the page
count in order to provide up to 3 tailored recommendations for the user. It will provide the title, author,
page length, rating out of 5 stars, publication date, and summary of the book recommendations.

findAwardBook()
Parameter(s): user inputted genre
Output: String with the 3 book recommendations
Description: This function will parse through the available Goodreads Choice Award categories to see if there was a
vote on the user’s genre of interest. It will then provide the title, author, publication date, and summary of the
top 3 books for the chosen category. If the user’s genre is not an official category, the program will say: “This
genre does not exist in the Goodreads Best Choice Awards. Please run the program again.”

General Notes:
1) Do not add any punctuation when entering an input (i.e. spell out & as and, no !.,)
2) If a genre exists as a Goodreads Choice Awards category but is not registered in Goodreads as a category, the program
will say the genre is not found.
