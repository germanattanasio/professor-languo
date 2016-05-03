Retrieve and Rank application overview
======================================

The Retrieve and Rank Application uses the Retrieve and Rank Service to
help users find the most relevant information for their query by using a
combination of search and machine learning algorithms to detect
"signals" in the data. Built on top of Apache Solr, developers load
their data into the service, train a machine learning model based on
known relevant results, then leverage this model to provide improved
results to their end users based on their question or query. Users
summarize their question and submit it to the Retrieve and Rank
Application. The system searches the corpus, and generates, scores and
ranks the answers. The system presents the most relevant answers from
the corpus to the user, who is spared the time and effort of enlisting
the community to provide an answer.

For an introduction into Retrieve and Rank, and how it can help your business, see [this video](https://www.youtube.com/watch?v=pupGatYlfqQ). Then, you can read [this blog post](blog.pdf) for more information about the business need for the application, the process behind making it, and how using it can improve your results. Please note, this link allows you to quickly see the PDF. If you wish to click on any of the included links, please click the raw button to download a full functional copy. You can also use a [running instance of the application](https://professor-languo.mybluemix.net/).

Give it a try! Click the button below to fork into IBM DevOps Services and deploy your own copy of this application on Bluemix.  
[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/watson-developer-cloud/professor-languo)  

Approaches to using Retrieve & Rank
======================================

The IBM Watson™ Retrieve and Rank service combines two information retrieval components in a single service: the power of Apache Solr and a sophisticated machine learning capability. This combination provides users with more relevant results by automatically reranking them by using these machine learning algorithms. The purpose of the Retrieve and Rank service is to help you find documents that are more relevant than those that you might get with standard information retrieval techniques. The ranker modes takes advantage of rich data in your documents to provide more relevant answers to queries.

### Simple case
The Retrieve & Rank service is used to find potential answers, generate numerical features to score those answers, and rank order those answers using a machine learning model to turn the numerical features associated with each feature into a confidence score. In this process, the simplified approach, you submit the user's query and the Retrieve & Rank service returns a ranked set of results. This combines the searching, scoring, and ranking into a single API call. For many applications, these steps are good enough to get good performance.

You can see a demo of this case [here](http://retrieve-and-rank-demo.mybluemix.net/rnr-demo/dist/?cm_mc_uid=69157823067314555654765&cm_mc_sid_50200000=1458917290#/).

### Advanced case
The Retrieve & Rank Application uses an advanced approach. In some cases you may want to exploit knowledge that is specific to your corpus, domain, or application. In the advanced approach, the search, scoring, and ranking stages are split into separate steps. This allows more fine grain control over each stage and adds complexity. This additional control is what allows you to exploit knowledge specific to your domain. Instead of simply making a single REST call to have Retrieve & Rank search and return results, the application make an initial call to the "retrieve" portion of the service to obtain candidate answers. The application then scores each answer using customized answer scorers that are unique to the use-case. In our application's case, these scorers make use of detailed metadata associated with our corpus. The scorers take into account information specific to the corpus that the Retrieve & Rank service would be unable to account for by itself. Once the answers are scored, they are sent to the "rank" portion of the Retrieve & Rank service. This allows the custom answer scores to be considered during the final ranking of answers and returns customized results.

For more information on how we use custom scorers to achieve better results, see [our blog](blog.pdf).

Note that when the information is sent to the ranking segment, there is an additional cost for use. For information, see [Retrieve & Rank's Pricing page](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/retrieve-rank.html#pricing-block).

Application use case 
========

People frequently post new questions on topics that have already been
answered. These people are likely overwhelmed by the size and scope of
the content they can search through and have difficulty finding related
questions that have been answered. To reduce the occurrence of
“duplicated questions” and help users find more rapid resolution to
their problems, the Retrieve and Rank Application uses Professor Languo
as a virtual search agent. After a question is asked, the app presents a
small set of answers to questions that may be related. The user then has
a short list of relevant answers, reducing the time they may spend
searching through the community.


Getting started
===============

To set get started with this application, see the [detailed documentation](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/ega_docs/rnr_ega_index.shtml).


## License

  This sample code is licensed under Apache 2.0.  
  Full license text is available in [LICENSE](LICENSE).

## Contributing

  See [CONTRIBUTING](CONTRIBUTING.md).


## Reference information
* Retrieve and Rank service [documentation](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/retrieve-rank/)
* [Configuring](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/retrieve-rank/configure.shtml) the Retrieve and Rank service
* Retrieve and Rank [API reference](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/retrieve-and-rank/api/v1/)

## Open Source @ IBM

  Find more open source projects on the
  [IBM Github Page](http://ibm.github.io/).
