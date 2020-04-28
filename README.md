# WebOpinionVisualiser

1. Callable interface:  
Every time 'thread' returns a future/map it overwrites the previous 
(the next map is the sum of the previous ones).
The problem I encountered is: although the last map returned is the map 
that has all the most frequent words, still all the previous maps get 
returned as well. 
In order to return just the last map, 'futures'/maps get removed from the joblist when the task is done
until joblist is 0.
```
            while (!jobList.isEmpty()) {
               jobList.removeIf(this::isDone);
           }
``` 
2. Best First Search   
Performance of the algorithm depends on how well the cost function is designed (fuzzy logic).
In my case, if score/relevance of the link is greater than 20, then a link can be expanded, otherwise it 
is ignored.  

3. Fuzzy Logic  
Returns a heuristic value - score, which identifies if the node needs to be expanded or not.
Takes frequency of the search term in heading, title and body as an input and depending on it returns relevance 
of the link.
Center of Gravity singleton is chosen as a defuzzifier, because it is fast, still very accurate and simple to implement.

4. Ignore words  
Cloud displays the most frequent words, ignoring all the words containing in ignore file + all the words that are less
than 4 characters long in order to eliminate domains and such(e.g. com, ie, etc).

5. TomCat  
**NB** Bug: Tomcat finds the path of files(fuzzy.fcl & ignorewords.txt) only if they are located in 
apache-tomcat-x.x.xx\bin\res.  
It took me 2 days to configure tomcat and trying solve this issue, I just gave up to be honest.  

*I would like to thank Mindaugas Sarskus for helping me with Callable interface, testing program as a standalone application
 (code for saving an image is provided by him) and creating jar in Intellij Idea.*

1. Callable ref: https://www.baeldung.com/java-runnable-callable

Memo 
How to create a jar file in IntellijIdea
1. Project Structure -> Artifacts -> + -> web application: exploded from modules->  Change type to Archive -> save in apache-tomcat/webapps
2. Build -> build artifacts
in apache-tomcat/bin run command "startup"
change url to http://localhost:8080/artifactName
