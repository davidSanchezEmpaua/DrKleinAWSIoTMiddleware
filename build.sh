docker build -t kex-push:1.0 .
docker run -it -v "$(pwd)/resources:/usr/app/resources" kex-push:1.0

