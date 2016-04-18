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

For an introduction into Retrieve and Rank, and how it can help your business, see [this video](https://www.youtube.com/watch?v=pupGatYlfqQ). Then, you can read [this blog post](blog.pdf) for more information about the business need for the application, the process behind making it, and how using it can improve your results. Please note, this link allows you to quickly see the PDF. If you wish to click on any of the included links, please click the raw button to download a full functional copy. You can also use a [running instance of the application](http://deepqa-test-2.mybluemix.net/).

Give it a try! Click the button below to fork into IBM DevOps Services and deploy your own copy of this application on Bluemix.  
[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.ibm.com/dbanda/professor-languo&branch=WDC-master)  


Use case 
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

The following information explains how to set up the Retrieve and Rank Application.

Prerequisites 
-------------

### Before you begin

Ensure that you have the prerequisites before you start.

-   An IBM® Bluemix™ account. If you don't have one, sign up for it
    here. For more information about the process, see Developing Watson™
    applications with Bluemix.
-   Java™ Development Kit 1.7 or later releases
-   Eclipse IDE for Java EE Developers
-   Apache Maven 3.1 or later releases
-   Git
-   Websphere Liberty Profile server, if you want to run the app in your
    local environment.

Get the project from GitHub
---------------------------

In order to run the Retrieve and Rank demo app, you need to have a
configured an instance of the Retrieve and Rank service. The following
steps will guide you through the process. The instructions use Eclipse,
but you can use the IDE of your choice.”

### Procedure

1.  Clone the Search application from GitHub by issuing the
    following command in your terminal:

   git clone git@github.ibm.com:dbanda/rnr-ega.git  

2.  Add the newly cloned repository to your local Eclipse workspace.

Creating a new service instance
-------------------------------

Complete the following sets of steps to add an instance of the Retrieve
and Rank service.

### Procedure

1.  Log in to Bluemix and navigate to the Dashboard on the top panel.
2.  Click USE SERVICES OR APIS.
3.  Select the Watson category, and select the Retrieve and Rank
    service.
4.  Type a unique name for your service in the Service name field, such
    as rnr-sample-service.
5.  Click CREATE. 

SolrCreationManager
-------------------

To run the app, you need a collection on a Solr cluster where you upload
and index your data from the corpus. The cluster is created on the
instance of your Retrieve and Rank service

### About this task

Running solrCreationMangaer creates a cluster on the server using
configuration settings you enable.

### Procedure

1.  [Get the project from
    GitHub](file:///C:/Users/IBM_ADMIN/workspace/Watson/com.ibm.watson.core.doc/developercloud/ega/search/out/rnr_ega_master.html#rnr_ega_get_github "In order to run the Retrieve and Rank demo app, you need to have a configured an instance of the Retrieve and Rank service. The following steps will guide you through the process. The instructions use Eclipse, but you can use the IDE of your choice.”")

Set up your environment variables.

2.  Open src/it/resources/server.env
3.  Set CLUSTER\_SIZE to the desired size of the cluster. This will be
    an integer from 1-14.
4.  Set CLUSTER\_NAME to the name of your choice.
5.  Set COLLECTION to the name of your choice.
6.  Set CONFIG\_NAME to the name of your choice. Each collection is
    linked to one configuration.
7.  Set CONFIG\_PATH to the path that contains the compressed zip file, 
    your schema.xml, and solrconfig.xml files. These will be uploaded to
    create a configuration setting with the specified configuration
    name.
8.  In the VCAP\_SERVICES variable definition line, enter a USERNAME and
    a PASSWORD to the credentials of your Retrieve and Rank service.
    Ensure these entries are in double quotes. For example,
    "iamtheuser".
9.  In the VCAP\_SERVICES variable definition line, enter the URL to the
    API endpoint of your Retrieve and Rank service. This is the “url”
    field of your service credentials and is usually
    https://gateway.watsonplatform.net/retrieve-and-rank/api. Ensure
    this entry is in double quotes.

Run solrCreationManager

10. From the menu bar, click Run Configurations.
11. Search for SolrCreationManager and select SolrCreationManager.java.
12. Click Open.
13. From the menu bar, click Run.

### Results

This creates a cluster with your specified size. The program prints out
your SOLR\_CLUSTER\_ID. After creating the cluster, SolrCreationManager
uploads your configuration files to create a configuration setting with
the specified configuration name. Once the upload is done, a collection
with your specified collection name is created using the configuration
settings. The ID of your cluster will be printed to standard output
after the cluster is created.

This is used to connect to your cluster. After creating the cluster, the
script uploads your configuration files to create a configuration
setting with the specified configuration name. Once the upload is done,
a collection with the specified collection name is created using the
configuration settings.

### What to do next

Add your SOLR\_CLUSTER\_ID to the server.env file, save the file, then
complete the Ingestion Runner setup.

Ingestion Driver
----------------

### Before you begin

-   Ensure that you have run solrCreationManager.
-   During this task, a ‘duplicateThreads’ directory is created in the
    RESULTS\_FILE\_PATH in src/main/resources/app\_config properties.
    Before running these steps, ensure that you have the space available
    for your corpus. For the English Corpus, a minimum of 60 MB of space
    is required.

Obtain stack exchange data and accept their terms.

1.  Go to the [Stack Exchange
    website](https://archive.org/details/stackexchange "(Opens in a new tab or window)").
2.  Read the legal terms and ensure that you accept them.
3.  In the top right, click 7z.
4.  Choose the site you want then download and save it.
    Note: The demo application uses the English Corpus
    (english.stackexchange.com.7z).
5.  Extract and save the files. Remember this location and use it in
    Step 1 of the Procedure.

### About this task

For each thread in the XML files, the Ingestion Driver first writes the
threads to file. If a thread is a duplicate, it is written to a
duplicates folder. Likewise, if a thread is unique, it is written to a
unique threads folder. The threads in the unique threads folder are then
uploaded to the previously created Solr cluster that is specified by the
SOLR\_CLUSTER\_ID in your server.env file. The duplicate threads are
stored in their folder and used later by other parts of the pipeline for
testing and validation purposes.

### Procedure

1.  Open src/main/resources/app\_config properties and change these
    constants:
    -   site: the site from which the corpus comes from (step 4 in
        Before you begin). This is used for naming purposes.
    -   XML\_DIR\_PATH: the directory where the StackExchange’s XML dump
        was extracted and saved.

2.  Ensure that the correct details for your collection, cluster id ,
    username, url, and password are in your server.env file that is
    located in the src/it/resources folder.
3.  From the menu bar, click Run Configurations.
4.  Search for Ingestion and choose the IngestionDriver\_runner.java.
5.  Click Open.
6.  Click Run.

### Results

This begins ingestion by reading data from the XML files and sending the
data to the Solr cluster for indexing. A ‘duplicateThreads’ directory is
created in RESULTS\_FILE\_PATH.

### What to do next

Go to src/main/resources/server.env and specify the RANKER\_NAME of your
choosing.

Run the Pipeline Driver.

Running the pipeline
--------------------

### Before you begin

Ensure that you have run both the solrCreationManager and the Ingestion
Driver.

### About this task

Use these steps to train a ranker with duplicate threads on which users
have already marked as duplicate questions and linked the original
thread.

### Procedure

1.  From the menu bar, click Run Configurations.
2.  Search for Pipeline and choose the Pipeline\_runner.java.
3.  Click Open.
4.  Click Run. When the training starts, the console is prompted with a
    series of ranker statuses, waiting to change from ‘Training’ to
    ‘Complete’.

### Results

The output will show:

-   Training data: A copy of the training data that is sent to the
    ranker to train will be saved in TRAINING\_DATA\_PATH
    (src/it/resources/server.env).

The RANKER\_ID is printed in the console.

### What to do next

Add the RANKER\_ID to src/it/resources/server.env.

Edit the manifest.yml.file

Editing the manifest.yml file.
--------------------------

### About this task

The properties in manifest.yml are used for connecting and uploading the app to Bluemix via Cloud Foundry (CF).

### Procedure

1.  Navigate to deepqa-ega/manifest.yml.
2.  Edit host to be the name of the application.
3.  Edit name to be the application's name.

### What to do next

Build the application.

Bluemix steps
-------------

Steps for setting up Bluemix....

### Creating an app

Set up a local development environment so that you can test and deploy
your application before you deploy it to Bluemix.

#### Procedure

1.  Log in to Bluemix.
2.  Navigate to the Dashboard on the top panel.
3.  Create your app.
    1.  Click CREATE APP.
    2.  Select WEB.
    3.  Select the starter Liberty for Java, and click CONTINUE.
    4.  Type a unique name for your app, such as search-sample-app, and
        click Finish.
    5.  Select CF Command Line Interface. If you do not already have it,
        click Download CF Command Line Interface. This link opens a
        GitHub repository. Download and install it locally.

### Binding to an existing service instance

#### Procedure

1.  Log in to Bluemix and navigate to the Dashboard on the top panel.
2.  Locate and click on the app that you created in the previous
    section.
3.  Click BIND A SERVICE OR API.
4.  Select the existing Retrieve and Rank service that you want to bind
    to your app, and click ADD. The Restage Application window is
    displayed.
5.  Click RESTAGE to restart your app.

### Setting up environment variables in Bluemix

In order to run the Search application on Bluemix, you must add the
environment variables to your app.

Navigate to the application dashboard in Bluemix. Locate and select the
application that you created previously. Click on the Environment
Variables section. Switch to the USER-DEFINED tab within the user
interface.

Copy the variables from the server.env file into this interface. Do this
for all of the variables except the VCAP\_SERVICES, as those are already
defined on the VCAP\_SERVICES tab

1.  Click ADD.
2.  Add the name of an environment variable.
3.  Assign the value, from the server.env file.
4.  Click SAVE.

### Deploying your app with the Cloud Foundry command line interface

If you are using Bluemix, you can use the Cloud Foundry command line
interface to deploy and modify applications and service instances.

#### Before you begin

[Install the cf command line
tool](https://github.com/cloudfoundry/cli/releases "(Opens in a new tab or window)").

Restriction: The Cloud Foundry command line interface is not supported
by Cygwin. Use the Cloud Foundry command line interface in a command
line window other than the Cygwin command line window.

#### Procedure

1.  Change to your new directory: cd deepqa-ega\_project\_directory
2.  Connect to Bluemix cf api https://api.ng.bluemix.net
3.  Log in to Bluemix cf login -u \<username\> -o \<organization\> -s
    \<space\>
4.  Provide your password.
5.  Deploy your application to Bluemix. cf push \<app\_name\>
6.  Access your app by entering the URL into your browser:
    http://\<app\_name\>.mybluemix.net

Liberty Steps
-------------

In order to develop applications on Bluemix, it is desirable to have a
local development environment against which you can test and deploy your
code before deploying changes to the Bluemix environment. For Gallery
Applications, the intent is to develop server side code using Java,
which runs on WAS Liberty. In order to set up the development
environment, it is necessary to install these add-ons and plugins:

-   Eclipse Marketplace
-   Bluemix Tools for Eclipse
-   Liberty Server add-on
-   Cloud Foundry (CF) command line tool

### Install Eclipse Marketplace

The Bluemix Tools and Liberty Server add-on are availabe as plugins in the Eclipse Marketplace. Therefore, you need to ensure that the Eclipse Marketplace Client is installed first in order to download the plugins.

#### Procedure

1.  From within Eclipse, select Help \> Install New Software.
2.  Select Add....
3.  In the Location field, enter
    http://download.eclipse.org/mpc/kepler/.
4.  In the Name field, enter an easily identifiable name, such as
    Eclipse Marketplace.
    Note: If you are using the latest core Eclipse (Luna), you must use
    the Luna version for the marketplace, located at
    http://download.eclipse.org/mpc/luna/
5.  Click OK.
6.  “Eclipse Marketplace http://download.eclipse.org/mpc/kepler/ “ should
    be automatically selected in the "Work with” field. If not, select
    it from the drop down menu.
7.  Mark the checkbox next to EPP Marketplace Client.
8.  Click Next.
9.  On the Install Details page, click Next.
10. On the Review Licenses page, accept the terms and click Finish.
11. Click Yes to restart Eclipse and complete the installation.

### Set up local Liberty 

In the Eclipse Servers view you can set up a Liberty local server.

#### Before you begin

Ensure that you have install the Eclipse Marketplace Client

#### About this task 

Installing the Liberty Server add-on will enable us to run the application on a local server.

#### Procedure

1.  From the Eclipse Servers View, right-click and select New \> Server.
    The New Server wizard opens.
2.  Expand the IBM folder and select WebSphere Application Server
    Liberty Profile.
3.  Click Next.
4.  Select Install from an archive or a repository.
5.  Click Next.
6.  Enter a destination path for where you want to install the Liberty
    runtime on your local file system.
7.  Select Download and install a new runtime environment from ibm.com.
    It may take a few moments for this list to populate.
8.  Once the list is populated, select Liberty Profile V8.5.5 Runtime.
9.  Click Next.
10. A list of add-ons is available. Select these:
    -   Java API for WebSocket 1.0
    -   Java API for WebSocket 1.1
    -   JavaScript Object Notation Processing

11. Click Next.
12. On the Review Licenses page, accept the terms and click Next.
13. Keep the default values then click Next.
14. Click Finish.

#### Results

Once the installation is complete you are prompted with the names of the
add-ons that have been installed. The Servers view is also populated
with the “Websphere Application Server V8.5….” server that you just
created.

The typical Bluemix Liberty server has the following features enabled by
default:

``` {.pre .codeblock}
<featureManager>
    <feature>jsf2.0</feature>
    <feature>jsp-2.2</feature>
    <feature>servlet-3.0</feature>
    <feature>ejbLite-3.1</feature>
   <feature>cdi-1.0</feature>
   <feature>jpa-2.0</feature>
    <feature>jdbc-4.0</feature>
    <feature>jndi-1.0</feature>
    <feature>managedBeans-1.0</feature>
    <feature>jaxrs-1.1</feature>
    <feature>appstate-1.0</feature>
    <feature>icap:managementConnector-1.0</feature>
</featureManager>
```

You must ensure that the same features are enabled when running on your
local Liberty instance. To edit the configuration, expand “Websphere
Application Server…” in the servers view. Right-click “Server
Configuration [server.xml] new server” . Select Open. Switch to
the Source view. Set the following to be the content of the
featureManager:

``` {.pre .codeblock}
<featureManager>
    <feature>jsf2.0</feature>
    <feature>jsp-2.2</feature>
    <feature>servlet-3.0</feature>
    <feature>ejbLite-3.1</feature>
   <feature>cdi-1.0</feature>
   <feature>jpa-2.0</feature>
    <feature>jdbc-4.0</feature>
    <feature>jndi-1.0</feature>
    <feature>managedBeans-1.0</feature>
    <feature>jaxrs-1.1</feature>
</featureManager>
```

Note: cap:managementConnector and appstate are not added as features
since they are not available in the local instance.

#### What to do next

To save the server configuration and republish the server, right-click
Websphere server and select Publish.

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