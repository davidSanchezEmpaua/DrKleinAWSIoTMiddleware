#Middleware - Dr. Klein Salesforce <-> Europace

## Setup
1. Install gradle (https://gradle.org/install/)
2. Start gradle to build project: `./gradlew build`

## Build Docker image
1. `docker build -t kex-push-service:1.0 .`

## Middleware to AWS ECR
What do you need to push a docker image (kex-push-service:1.0) to AWS ECR:

1. Login in to AWS
2. Get ECR Login token: `aws ecr get-login --profile awsauth --no-include-email`
3. Execute response of step 2. `docker login ...`
4. Tag docker image: `docker tag $imageId 831269439992.dkr.ecr.eu-central-1.amazonaws.com/kex-push-service:1.0` 
5. Push docker image: `docker push 831269439992.dkr.ecr.eu-central-1.amazonaws.com/kex-push-service`
