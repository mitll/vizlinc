This is the repository for the main VizLinc Application.  Vizlinc is a visual analytics platform that takes as input a corpus of text documents, extracts named entities (people, locations, and organizations) and the relations between those entites from the documents, and allows a user to explore the information contained in the documents from both a high-level corpus view point and with respect to more narrow queries.  It provides several different visualizations of the data that are linked together in order to provide a more complete analytics capability.

There are 2 additional repositories that you will also want to check out if you are interested in working with VizLinc.
The VizLinc Ingest repository is located at https://github.com/mitll/vizlinc_ingester.  This tool does the heavy lifting analytics to extract information from your documents so that it can be explored through VizLinc.  You must ingest your documents with the ingester before using the main VizLinc application.
The VizLinc Database repository is located at https://github.com/mitll/vizlinc_db.  This is the database framework for VizLinc and details how the Ingest tool and the main VizLinc application interact with eacahother.

The documentation for all of the VizLinc tools is located in the documentation forlder of this repository.  


