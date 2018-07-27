FROM redis:4.0.10-alpine
MAINTAINER Daniel Fuevesi

RUN apk add --no-cache \
        curl \
        bash

ADD docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

EXPOSE 26379

ENTRYPOINT ["/docker-entrypoint.sh"]
