#### Requirements

* maven 
* docker
* docker-compose

#### Build & Run

```bash
mvn clean install -T 1C
docker build . -t neuralswarm
docker-compose up
```

or use corresponding bash scripts in `/bin`
