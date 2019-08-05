# Multi-language Annotation Suggester (ASIA-MAS)

This project implements a multi-language header suggestion service that interacts with [Grafterizer](https://github.com/UNIMIBInside/asia-backend) and [ASIA](https://github.com/UNIMIBInside/asia-backend) ecosystem to help the used the annotate a table at schema level in several languagese.
This work is part of [EuBusinessGraph](https://www.eubusinessgraph.eu/) EU project. 

## Requirements and Prerequisites 
- **JDk 8**  
- An **Azure subscription key for Translator Text** (to get one follow this [tutorial](https://crunchify.com/microsoft-translator-text-api-example/))


## Build and Run
From root directory:
```
$ cd suggester
$ ./mvnw package
$ java -jar target/suggester-0.1.jar --suggester.translator.subscription-key=XXXXXXXXXXXXXXX
```

The Azure subscription key can also be set as an environment variable:

```
(on Linux)
$ SUGGESTER_TRANSLATOR_SUBSCRIPTION_KEY=XXXXX
$ echo $SUGGESTER_TRANSLATOR_SUBSCRIPTION_KEY
$ java -jar target/suggester-0.1.jar
```


By default, ASIA-MAS will run on port 8085. To use a different port, set the server.port property as follows when running the program:
```
java -jar target/suggester-0.1.jar -Dserver.port=7000
```


To create a Docker container from sources:
```
$ cd suggester
$ ./mvnw clean package
$ ./mvnw docker:build -Ddocker.account.name=<ACCOUNT_NAME>
```

To run the just created Docker container:
```
$ docker run -e SUGGESTER_TRANSLATOR_SUBSCRIPTION_KEY=XXXX  <ACCOUNT_NAME>/asiasuggester
```

A precompiled Docker image of ASIA-MAS can be found at:
````
https://hub.docker.com/r/miciav/asiasuggester
````