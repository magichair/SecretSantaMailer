This is my first GitHub submission so I'll be updating this as things move along.

Note that you'll need to import java.mail jar into the project for this to run.

Future tasks:
* Move configuration of m_participants out into an external file (get rid of need for hard coded assignment) [DONE]
  see participants.txt
* Move mail settings out into external configuration file [DONE]
  see config.txt
* Move Subject/Body out into external configuration file [DONE]
  see config.txt

Limitations:
* The algorithm is simply a random trial and error for pairing. If your restraints on partners are too strict (ie. no solutions avaialble) the run of the program will do it's best to match and fail. This will report failure. If you think there is a solution avaiable given the partner restraints and you get failures, just keep re-trying, random is random.
* I'd love to make this algorithm automatically detect all possible acceptable arrangements then randomly select from all arrangements (rather than starting at a random place and randomly pairing adhering to restraints)

Enjoy!
--John Ibsen
j{dt}ibsen{at}projectfuse{dt}net