

This is the repository for the main VizLinc Application. VizLinc is a visual analytics platform that takes as input a corpus of text documents, extracts named entities (people, locations, and organizations) and the relations between those entities from the documents, and allows a user to explore the information contained in the documents from both a high-level corpus view point and with respect to more narrow queries. It provides several different visualizations of the data that are linked together in order to provide a more complete analytics capability.

There are 2 additional repositories that you will also want to check out if you are interested in working with VizLinc. The VizLinc Ingest repository is located at https://github.com/mitll/vizlinc_ingester. This tool does the heavy lifting analytics to extract information from your documents so that it can be explored through VizLinc. You must ingest your documents with the ingester before using the main VizLinc application. The VizLinc Database repository is located at https://github.com/mitll/vizlinc_db. This is the database framework for VizLinc and details how the Ingest tool and the main VizLinc application interact with eacahother.

If you're interested in running the tools, begin by downloading the VizLinc release (https://github.com/mitll/vizlinc/releases/tag/v1.5) and the VizLinc Ingester release (https://github.com/mitll/vizlinc_ingester/releases/tag/ingest). Unzip both releases. First run the ingester executable; this is located at vizlinc_ingest\ingester\dist\vizlinc-ingester.exe. The video guide on how to use the ingester can be found here: https://www.youtube.com/watch?v=rYsvVBLd3tw. After you have run your documents through the ingest process, you're ready to interact with them via VizLinc. The executable for VizLinc can be found in vizlinc\bin\vizlinc64.exe. Before running VizLinc, you will need to make sure that youh have Java properly installed. You can download the latest JDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html. After installing the jdk, you will need to either globally set your jdkhome environment variable to point to the installation or just set it for VizLinc. To set it for VizLinc, edit the Vizlinc configuration file. This should be located in the vizlinc/etc folder called vizlinc.conf. In this file, there is a line that says #jdkhome="/path/to/jdk" Uncomment it (remove the leading #) and change this to point to your jdk. For reference, the line would need to look something like: jdkhome=”C:\Program Files\Java\jdk1.7.0_55” Just make sure that the version numbers match the one that you downloaded.

An introductory video for VizLinc is located at https://www.youtube.com/watch?v=6W5DJ4DoG4Q.

For anyone who is interested in editing or contributing to the VizLinc source code (or just understanding it better), a paper from the IDEA Workshop at KDD 2014 can be found at https://github.com/mitll/vizlinc/blob/master/documentation/vizlinc.pdf; this paper describes all of the components of VizLinc and showcases its effectiveness on multiple datasets. There are 2 primary paths for integrating your code with VizLinc. The first is to augment the VizLinc Ingester to extract new or different information, or the same information but in a different way. The ingester is written in Groovy and was designed from the ground up to easily have components swapped in and out, so this should be relatively straight forward. The second method of integrating with VizLinc is to augment the main user facing application. This was written as a Gephi plug-in and is consequently Java-based. A great deal of information on writing Gephi plug-ins can be found at http://gephi.github.io/developers/.

## License

Copyright 2014-2016 MIT Lincoln Laboratory, Massachusetts Institute of Technology 

Licensed under the Apache License, Version 2.0 (the "License"); you may not use these files except in compliance with the License.

You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
