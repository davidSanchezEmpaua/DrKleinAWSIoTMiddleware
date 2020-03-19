docker build -t kex-push-service:1.0 .
docker run -it -v "$(pwd)/resources:/usr/app/resources" kex-push-service:1.0

